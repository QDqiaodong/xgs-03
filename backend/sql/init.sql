CREATE DATABASE IF NOT EXISTS plant_care DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE plant_care;
SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS plant_category (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL COMMENT '品种名称',
    scientific_name VARCHAR(200) COMMENT '学名',
    category VARCHAR(50) COMMENT '分类',
    light_requirement VARCHAR(100) COMMENT '光照需求',
    water_requirement VARCHAR(100) COMMENT '浇水需求',
    temperature_range VARCHAR(100) COMMENT '适宜温度',
    humidity VARCHAR(100) COMMENT '湿度要求',
    fertilization VARCHAR(200) COMMENT '施肥建议',
    pruning VARCHAR(200) COMMENT '修剪建议',
    common_diseases TEXT COMMENT '常见病害',
    description TEXT COMMENT '品种描述',
    image_url VARCHAR(500) COMMENT '图片URL',
    difficulty_level INT DEFAULT 1 COMMENT '养护难度等级:1-入门,2-简单,3-中等,4-较难,5-专家',
    popularity_score INT DEFAULT 0 COMMENT '人气分数',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_name (name),
    INDEX idx_category (category),
    INDEX idx_difficulty (difficulty_level),
    INDEX idx_popularity (popularity_score)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    nickname VARCHAR(50),
    avatar VARCHAR(500),
    email VARCHAR(100),
    phone VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS plant_archive (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    plant_category_id BIGINT,
    custom_name VARCHAR(100) COMMENT '自定义昵称',
    purchase_date DATE COMMENT '购入时间',
    location VARCHAR(200) COMMENT '摆放位置',
    environment VARCHAR(100) COMMENT '摆放环境',
    reminder_enabled BOOLEAN DEFAULT FALSE COMMENT '是否开启提醒',
    water_interval INT COMMENT '浇水周期(天)',
    fertilize_interval INT COMMENT '施肥周期(天)',
    notes TEXT COMMENT '备注',
    image_url VARCHAR(500) COMMENT '图片',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
    FOREIGN KEY (plant_category_id) REFERENCES plant_category(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS care_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    plant_archive_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    log_date DATE NOT NULL COMMENT '记录日期',
    operation_type VARCHAR(50) NOT NULL COMMENT '操作类型:浇水/施肥/修剪/换盆/其他',
    details TEXT COMMENT '操作详情',
    growth_status VARCHAR(200) COMMENT '长势描述',
    disease_status VARCHAR(200) COMMENT '病害情况',
    image_urls TEXT COMMENT '图片URLs,逗号分隔',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_plant_id (plant_archive_id),
    INDEX idx_user_id (user_id),
    INDEX idx_log_date (log_date),
    FOREIGN KEY (plant_archive_id) REFERENCES plant_archive(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS post (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL COMMENT '帖子标题',
    content TEXT NOT NULL COMMENT '帖子内容',
    plant_category_id BIGINT COMMENT '相关植物品种',
    post_type VARCHAR(50) DEFAULT 'question' COMMENT '类型:question/experience',
    image_urls TEXT COMMENT '图片URLs,逗号分隔',
    view_count INT DEFAULT 0 COMMENT '浏览量',
    like_count INT DEFAULT 0 COMMENT '点赞数',
    comment_count INT DEFAULT 0 COMMENT '评论数',
    is_resolved BOOLEAN DEFAULT FALSE COMMENT '是否已解决',
    hotness_score DOUBLE DEFAULT 0.0 COMMENT '热度分',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_post_type (post_type),
    INDEX idx_created_at (created_at),
    INDEX idx_hotness_score (hotness_score),
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
    FOREIGN KEY (plant_category_id) REFERENCES plant_category(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS comment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    post_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    parent_id BIGINT COMMENT '父评论ID',
    content TEXT NOT NULL COMMENT '评论内容',
    like_count INT DEFAULT 0 COMMENT '点赞数',
    is_best_answer BOOLEAN DEFAULT FALSE COMMENT '是否最佳答案',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_post_id (post_id),
    INDEX idx_user_id (user_id),
    FOREIGN KEY (post_id) REFERENCES post(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS favorite (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    target_type VARCHAR(50) NOT NULL COMMENT '收藏类型:post/solution',
    target_id BIGINT NOT NULL COMMENT '收藏目标ID',
    title VARCHAR(200) COMMENT '收藏标题',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_target (user_id, target_type, target_id),
    INDEX idx_user_id (user_id),
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS comment_like (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    comment_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_comment (user_id, comment_id),
    INDEX idx_user_id (user_id),
    INDEX idx_comment_id (comment_id),
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
    FOREIGN KEY (comment_id) REFERENCES comment(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS care_reminder (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    plant_archive_id BIGINT NOT NULL,
    reminder_type VARCHAR(50) NOT NULL COMMENT '提醒类型:浇水/施肥',
    reminder_date DATE NOT NULL COMMENT '提醒日期',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态:PENDING待处理/COMPLETED已完成/DEFERRED已延期/CANCELLED已取消',
    completed_at TIMESTAMP NULL COMMENT '完成时间',
    deferred_count INT DEFAULT 0 COMMENT '延期次数',
    care_log_id BIGINT NULL COMMENT '关联的养护日志ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_plant_id (plant_archive_id),
    INDEX idx_status (status),
    INDEX idx_reminder_date (reminder_date),
    UNIQUE KEY uk_plant_type_date (plant_archive_id, reminder_type, reminder_date),
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
    FOREIGN KEY (plant_archive_id) REFERENCES plant_archive(id) ON DELETE CASCADE,
    FOREIGN KEY (care_log_id) REFERENCES care_log(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS plant_photo (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    plant_archive_id BIGINT NOT NULL COMMENT '植物档案ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    image_url VARCHAR(500) NOT NULL COMMENT '图片URL',
    description TEXT COMMENT '照片描述',
    photo_date DATE COMMENT '拍摄日期',
    is_cover BOOLEAN DEFAULT FALSE COMMENT '是否为封面图',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_plant_archive_id (plant_archive_id),
    INDEX idx_user_id (user_id),
    INDEX idx_photo_date (photo_date),
    INDEX idx_is_cover (is_cover),
    FOREIGN KEY (plant_archive_id) REFERENCES plant_archive(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS browse_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    target_type VARCHAR(50) NOT NULL COMMENT '浏览类型:plant_category/post',
    target_id BIGINT NOT NULL COMMENT '浏览目标ID',
    view_count INT DEFAULT 1 COMMENT '浏览次数',
    last_viewed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '最后浏览时间',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_target (user_id, target_type, target_id),
    INDEX idx_user_id (user_id),
    INDEX idx_target_type (target_type),
    INDEX idx_last_viewed (last_viewed_at),
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO plant_category (name, scientific_name, category, light_requirement, water_requirement, temperature_range, humidity, fertilization, pruning, common_diseases, description, difficulty_level, popularity_score) VALUES
('绿萝', 'Epipremnum aureum', '观叶植物', '散射光，耐阴', '保持土壤微湿，避免积水', '15-30℃', '40-70%', '生长季每月施一次稀薄液肥', '定期修剪过长藤蔓', '叶斑病、根腐病', '绿萝是非常常见的室内观叶植物，具有很强的空气净化能力，能有效吸收甲醛。', 1, 95),
('吊兰', 'Chlorophytum comosum', '观叶植物', '明亮散射光', '保持土壤湿润，忌积水', '15-25℃', '50-70%', '春夏每月施一次复合肥', '修剪枯黄叶片', '叶尖干枯、根腐病', '吊兰适应性强，是新手养植的首选，垂吊的枝条极具观赏性。', 1, 90),
('多肉植物', 'Succulent', '多肉植物', '充足阳光，每天至少4小时', '干透浇透，宁干勿湿', '10-30℃', '30-50%', '生长季每月施一次稀释多肉专用肥', '摘除枯叶，修根', '黑腐病、介壳虫', '多肉植物品种繁多，形态各异，需水量少，适合忙碌的上班族。', 2, 98),
('君子兰', 'Clivia miniata', '观花植物', '明亮散射光，忌强光直射', '见干见湿，避免积水', '15-25℃', '60-80%', '花期前后施磷钾肥', '修剪老叶残花', '叶斑病、根腐病', '君子兰叶片肥厚有光泽，花期长，是常见的室内观赏花卉。', 3, 75),
('发财树', 'Pachira aquatica', '观叶植物', '散射光，耐阴', '宁干勿湿，干透浇透', '20-30℃', '50-70%', '生长季每月施一次复合肥', '修剪造型，摘除黄叶', '叶斑病、根腐病', '发财树寓意招财进宝，是办公室和家居装饰的热门选择。', 2, 92),
('虎皮兰', 'Sansevieria trifasciata', '观叶植物', '适应性强，喜光耐阴', '耐干旱，干透浇透', '15-30℃', '40-60%', '生长季每月施一次稀薄液肥', '修剪枯黄叶片', '根腐病', '虎皮兰夜间释放氧气，非常适合放置在卧室，有"天然清道夫"之称。', 1, 88),
('月季', 'Rosa chinensis', '观花植物', '充足阳光，每天6小时以上', '保持土壤湿润，避免积水', '15-26℃', '60-80%', '生长期每周施一次液肥，花期增施磷钾肥', '花后修剪，冬季强剪', '黑斑病、白粉病、蚜虫', '月季品种繁多，花色丰富，花期长，被誉为"花中皇后"。', 4, 85),
('栀子花', 'Gardenia jasminoides', '观花植物', '充足散射光', '喜湿润，保持土壤微酸', '18-28℃', '70-85%', '开花前后施磷钾肥', '花后修剪整形', '黄化病、叶斑病', '栀子花花香浓郁，叶片翠绿，是深受喜爱的芳香花卉。', 3, 80),
('芦荟', 'Aloe vera', '多肉植物', '充足阳光', '耐旱，干透浇透', '15-30℃', '40-60%', '生长季每月施一次稀薄肥', '摘除老叶', '根腐病、介壳虫', '芦荟不仅具有观赏价值，还有美容、药用等多种功效。', 1, 87),
('文竹', 'Asparagus setaceus', '观叶植物', '散射光，忌强光', '保持土壤湿润，忌积水', '15-25℃', '60-80%', '生长季每月施一次稀薄液肥', '修剪枯黄枝条', '叶枯病、介壳虫', '文竹姿态优美，气质文雅，是书房装饰的理想植物。', 2, 70);

CREATE TABLE IF NOT EXISTS care_checkin (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    checkin_date DATE NOT NULL COMMENT '打卡日期',
    care_log_ids TEXT COMMENT '关联的养护日志IDs,逗号分隔',
    care_count INT NOT NULL DEFAULT 0 COMMENT '当日养护操作数',
    remark VARCHAR(500) COMMENT '打卡备注',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_date (user_id, checkin_date),
    INDEX idx_user_id (user_id),
    INDEX idx_checkin_date (checkin_date),
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS achievement_badge (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    badge_type VARCHAR(50) NOT NULL COMMENT '徽章类型:STREAK_7/STREAK_30/STREAK_100',
    badge_name VARCHAR(100) NOT NULL COMMENT '徽章名称',
    badge_icon VARCHAR(200) COMMENT '徽章图标',
    streak_days INT NOT NULL COMMENT '所需连续天数',
    description VARCHAR(500) COMMENT '徽章描述',
    unlocked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '解锁时间',
    UNIQUE KEY uk_user_badge (user_id, badge_type),
    INDEX idx_user_id (user_id),
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO user (username, nickname, avatar) VALUES
('plant_lover', '绿植爱好者', 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=cute%20gardener%20avatar&image_size=square');
