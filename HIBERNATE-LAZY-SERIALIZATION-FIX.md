# GET /api/plant-archives/user/{userId} 500 异常修复说明

## 1. 问题现象

调用 `GET /api/plant-archives/user/1` 返回 **HTTP 500**，前端无法获取真实植物档案列表，导致：

- 「我的绿植」页面植物卡片为空
- 日历组件「单株植物」模式下植物下拉选择框不出现（`v-if="archives.length > 0"` 不满足）
- 页面级验收被前端 fallback 的假数据掩盖，无法走真实链路

## 2. 根因分析

### 2.1 异常堆栈

```
InvalidDefinitionException: No serializer found for class
org.hibernate.proxy.pojo.bytebuddy.ByteBuddyInterceptor

through reference chain:
  java.util.ArrayList[0]
    -> com.plantcare.entity.PlantArchive["plantCategory"]
    -> com.plantcare.entity.PlantCategory$HibernateProxy["hibernateLazyInitializer"]
```

### 2.2 根因：Hibernate LAZY 代理 + Jackson 序列化

在 [PlantArchive.java](file:///Users/Admin/Desktop/solo-0601/xgs-0601/xgs-03/backend/src/main/java/com/plantcare/entity/PlantArchive.java#L53-L55) 中：

```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "plant_category_id", insertable = false, updatable = false)
private PlantCategory plantCategory;
```

- `fetch = FetchType.LAZY`：Hibernate 返回的 `plantCategory` 不是真实对象，而是 **ByteBuddy 动态代理对象**，内部包含 `hibernateLazyInitializer`（类型为 `ByteBuddyInterceptor`）
- 当 Jackson 序列化 `PlantArchive` 时，会遍历 `plantCategory` 的所有字段，包括 `hibernateLazyInitializer`
- Jackson 不知道如何序列化 `ByteBuddyInterceptor` → 抛出 `InvalidDefinitionException` → HTTP 500

### 2.3 同类问题存在范围

所有带 `@ManyToOne(fetch = LAZY)` 的实体均存在此隐患：

| 实体 | 关联字段 |
|------|---------|
| [PlantArchive.java](file:///Users/Admin/Desktop/solo-0601/xgs-0601/xgs-03/backend/src/main/java/com/plantcare/entity/PlantArchive.java) | `plantCategory` |
| [Post.java](file:///Users/Admin/Desktop/solo-0601/xgs-0601/xgs-03/backend/src/main/java/com/plantcare/entity/Post.java) | `user` |
| [Comment.java](file:///Users/Admin/Desktop/solo-0601/xgs-0601/xgs-03/backend/src/main/java/com/plantcare/entity/Comment.java) | `user` |

## 3. 修复方案（四层保险）

### 3.1 依赖层：引入 jackson-datatype-hibernate6

[backend/pom.xml](file:///Users/Admin/Desktop/solo-0601/xgs-0601/xgs-03/backend/pom.xml#L49-L52) 新增：

```xml
<dependency>
    <groupId>com.fasterxml.jackson.datatype</groupId>
    <artifactId>jackson-datatype-hibernate6</artifactId>
</dependency>
```

`Hibernate6Module` 是 Jackson 官方提供的 Hibernate 6 集成模块，能够：
- 识别 LAZY 代理对象，未初始化时序列化为 `null` 而非抛出异常
- 正确处理 Hibernate 集合类型（PersistentSet / PersistentList 等）
- 忽略 `hibernateLazyInitializer` 和 `handler` 内部字段

### 3.2 配置层：注册 Hibernate6Module 并关闭空 Bean 序列化失败

新增 [JacksonConfig.java](file:///Users/Admin/Desktop/solo-0601/xgs-0601/xgs-03/backend/src/main/java/com/plantcare/config/JacksonConfig.java)：

```java
@Configuration
public class JacksonConfig {
    @Bean
    @Primary
    public ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder) {
        ObjectMapper objectMapper = builder.createXmlMapper(false).build();
        objectMapper.registerModule(new Hibernate6Module());
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        return objectMapper;
    }
}
```

同时在 [application.yml](file:///Users/Admin/Desktop/solo-0601/xgs-0601/xgs-03/backend/src/main/resources/application.yml#L48-L53) 加固：

```yaml
spring:
  jackson:
    serialization:
      write-dates-as-timestamps: false
      fail-on-empty-beans: false
```

### 3.3 实体层：加 @JsonIgnoreProperties 忽略代理内部字段

对所有 JPA 实体类统一添加：

```java
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
```

覆盖的实体文件：
- [PlantArchive.java](file:///Users/Admin/Desktop/solo-0601/xgs-0601/xgs-03/backend/src/main/java/com/plantcare/entity/PlantArchive.java)
- [PlantCategory.java](file:///Users/Admin/Desktop/solo-0601/xgs-0601/xgs-03/backend/src/main/java/com/plantcare/entity/PlantCategory.java)
- [Post.java](file:///Users/Admin/Desktop/solo-0601/xgs-0601/xgs-03/backend/src/main/java/com/plantcare/entity/Post.java)
- [Comment.java](file:///Users/Admin/Desktop/solo-0601/xgs-0601/xgs-03/backend/src/main/java/com/plantcare/entity/Comment.java)
- [User.java](file:///Users/Admin/Desktop/solo-0601/xgs-0601/xgs-03/backend/src/main/java/com/plantcare/entity/User.java)
- [CareLog.java](file:///Users/Admin/Desktop/solo-0601/xgs-0601/xgs-03/backend/src/main/java/com/plantcare/entity/CareLog.java)
- [Favorite.java](file:///Users/Admin/Desktop/solo-0601/xgs-0601/xgs-03/backend/src/main/java/com/plantcare/entity/Favorite.java)

即使未来实体新增 LAZY 关联，此注解也能保证 Jackson 不会误入代理内部字段。

### 3.4 查询层：JOIN FETCH 预加载关联（前端需要 plantCategory.name）

在 [PlantArchiveRepository.java](file:///Users/Admin/Desktop/solo-0601/xgs-0601/xgs-03/backend/src/main/java/com/plantcare/repository/PlantArchiveRepository.java) 使用 JPQL `LEFT JOIN FETCH`：

```java
@Query("SELECT pa FROM PlantArchive pa LEFT JOIN FETCH pa.plantCategory WHERE pa.userId = :userId ORDER BY pa.createdAt DESC")
List<PlantArchive> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);

@Query("SELECT pa FROM PlantArchive pa LEFT JOIN FETCH pa.plantCategory WHERE pa.id = :id")
Optional<PlantArchive> findById(@Param("id") Long id);
```

好处：
- SQL 层面一次查询加载 `plantCategory`，避免 N+1 问题
- 返回的是**真实对象**而非代理，Jackson 序列化无异常
- 前端 `archive.plantCategory?.name` 能拿到真实品种名（如「绿萝」），不会回退到「未知品种」

### 3.5 清理层：移除所有前端假数据 fallback 和后端静默吞异常

**后端** — [PlantArchiveService.java](file:///Users/Admin/Desktop/solo-0601/xgs-0601/xgs-03/backend/src/main/java/com/plantcare/service/PlantArchiveService.java) 还原为异常透明抛出（保留 log.error 完整堆栈）：

```java
public List<PlantArchive> getUserArchives(Long userId) {
    try {
        return plantArchiveRepository.findByUserIdOrderByCreatedAtDesc(userId);
    } catch (Exception e) {
        log.error("getUserArchives failed for userId={}", userId, e);
        throw e;   // 真实异常上抛，避免被空列表掩盖
    }
}
```

**前端** — 移除两个文件中的假植物兜底：

- [Archives.vue](file:///Users/Admin/Desktop/solo-0601/xgs-0601/xgs-03/frontend/src/views/Archives.vue)：`loadArchives()` catch 分支原来硬编码的「小绿」「肉肉」假数据已删除，失败时仅置空数组
- [CareCalendar.vue](file:///Users/Admin/Desktop/solo-0601/xgs-0601/xgs-03/frontend/src/components/CareCalendar.vue)：`fallbackPlants` 数组已删除，`plantList` computed 直接使用 `props.archives`

## 4. 验证结果

### 4.1 后端接口

| API | 修复前 | 修复后 |
|-----|--------|--------|
| `GET /api/plant-archives/user/1` | HTTP 500 | **HTTP 200**，返回 2 条真实档案，`plantCategory` 完整加载（含 `name: "绿萝"`） |
| `GET /api/care-logs/user/1` | HTTP 200 | HTTP 200（未受影响） |
| `GET /api/plant-categories` | HTTP 200 | HTTP 200（未受影响） |

### 4.2 前端页面级闭环

| 验证场景 | 结果 |
|----------|------|
| 档案列表卡片显示真实数据（「我的小绿萝」品种名正确显示「绿萝」、「审计测试薄荷」购入时间 2026/5/20 正确） | ✅ |
| 「单株植物」模式下拉框出现并显示真实植物（「我的小绿萝」「审计测试薄荷」） | ✅ |
| 选择「审计测试薄荷」（id=1）后，6 号格内圆点正确出现（对应数据库 plantArchiveId=1 的浇水/施肥/修剪记录） | ✅ |
| 单株模式详情浮层正确显示 4 条记录（含真实新增的蓝色「浇水 - 今天浇了300ml水」） | ✅ |
| 切回「全部植物」模式，详情浮层每条记录右侧显示植物名「审计测试薄荷」 | ✅ |
| 前端 `npm run build` | ✅ exit code 0 |
| 后端 `docker compose up -d --build backend` | ✅ 构建并启动成功 |

## 5. 后续编码规范建议

1. **所有 JPA 实体类默认加 `@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})`**，这是 Spring Boot + JPA + Jackson 组合的行业标准实践
2. **前端严禁用硬编码假数据兜底接口失败**，真实 API 异常应显式暴露（用 `alert` / 空状态页 / 错误提示等方式），否则验收时问题会被掩盖
3. **Service 层 catch 后必须 `throw e`**，禁止静默返回假对象/空列表，除非是用户可见的降级场景（如网络中断重试）
4. **前端需要关联字段时（如 plantCategory.name）**，Repository 层必须用 `JOIN FETCH` 预加载，既避免序列化异常也避免 N+1 性能问题
