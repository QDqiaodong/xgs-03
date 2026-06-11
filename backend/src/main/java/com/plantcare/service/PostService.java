package com.plantcare.service;

import com.plantcare.dto.TopicDTO;
import com.plantcare.entity.PlantCategory;
import com.plantcare.entity.Post;
import com.plantcare.repository.PlantCategoryRepository;
import com.plantcare.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final PlantCategoryRepository plantCategoryRepository;
    private final HotnessService hotnessService;

    private static final List<String> PROBLEM_KEYWORDS = Arrays.asList(
            "发黄", "枯萎", "烂根", "黑腐", "虫害", "病害", "掉叶", "落叶", "不开花",
            "叶斑", "白粉", "蚜虫", "介壳虫", "红蜘蛛", "根腐", "炭疽", "干枯", "萎缩", "烂心"
    );

    private static final List<String> CARE_KEYWORDS = Arrays.asList(
            "浇水", "施肥", "光照", "换盆", "修剪", "扦插", "繁殖", "过冬", "度夏",
            "土壤", "肥料", "湿度", "温度", "通风", "晒太阳", "遮阳", "移盆", "分株"
    );

    public Page<Post> getPostsByType(String postType, Pageable pageable) {
        return postRepository.findByPostTypeOrderByCreatedAtDesc(postType, pageable);
    }

    public Page<Post> getPostsFiltered(String postType, Long plantCategoryId, String keyword, Pageable pageable) {
        if (plantCategoryId != null) {
            if (postType != null && !postType.isEmpty()) {
                return postRepository.findByPostTypeAndPlantCategoryIdOrderByCreatedAtDesc(postType, plantCategoryId, pageable);
            }
            return postRepository.findByPlantCategoryIdOrderByCreatedAtDesc(plantCategoryId, pageable);
        }
        if (keyword != null && !keyword.isEmpty()) {
            if (postType != null && !postType.isEmpty()) {
                return postRepository.findByPostTypeAndKeywordOrderByCreatedAtDesc(postType, keyword, pageable);
            }
            return postRepository.findByKeywordOrderByCreatedAtDesc(keyword, pageable);
        }
        return postRepository.findByPostTypeOrderByCreatedAtDesc(postType, pageable);
    }

    public Page<Post> getPostsFilteredByHotness(String postType, Long plantCategoryId, String keyword, Pageable pageable) {
        if (plantCategoryId != null) {
            if (postType != null && !postType.isEmpty()) {
                return postRepository.findByPostTypeAndPlantCategoryIdOrderByHotnessScoreDesc(postType, plantCategoryId, pageable);
            }
            return postRepository.findByPlantCategoryIdOrderByHotnessScoreDesc(plantCategoryId, pageable);
        }
        if (keyword != null && !keyword.isEmpty()) {
            if (postType != null && !postType.isEmpty()) {
                return postRepository.findByPostTypeAndKeywordOrderByHotnessScoreDesc(postType, keyword, pageable);
            }
            return postRepository.findByKeywordOrderByHotnessScoreDesc(keyword, pageable);
        }
        if (postType != null && !postType.isEmpty()) {
            return postRepository.findByPostTypeOrderByHotnessScoreDesc(postType, pageable);
        }
        return postRepository.findAllByOrderByHotnessScoreDesc(pageable);
    }

    public List<TopicDTO> getHotTopics() {
        List<Post> allPosts = postRepository.findAll();
        List<PlantCategory> allCategories = plantCategoryRepository.findAll();
        Map<Long, String> categoryIdToName = allCategories.stream()
                .collect(Collectors.toMap(PlantCategory::getId, PlantCategory::getName));

        List<TopicDTO> plantTopics = aggregatePlantTopics(allPosts, categoryIdToName);
        List<TopicDTO> problemTopics = aggregateKeywordTopics(allPosts, PROBLEM_KEYWORDS, "常见问题");
        List<TopicDTO> careTopics = aggregateKeywordTopics(allPosts, CARE_KEYWORDS, "养护技巧");

        List<TopicDTO> allTopics = new ArrayList<>();
        allTopics.addAll(plantTopics);
        allTopics.addAll(problemTopics);
        allTopics.addAll(careTopics);

        return allTopics.stream()
                .sorted((a, b) -> Long.compare(b.getCount(), a.getCount()))
                .collect(Collectors.toList());
    }

    private List<TopicDTO> aggregatePlantTopics(List<Post> posts, Map<Long, String> categoryIdToName) {
        Map<Long, Long> countMap = posts.stream()
                .filter(p -> p.getPlantCategoryId() != null)
                .collect(Collectors.groupingBy(Post::getPlantCategoryId, Collectors.counting()));

        return countMap.entrySet().stream()
                .filter(e -> e.getValue() > 0)
                .map(e -> {
                    String name = categoryIdToName.getOrDefault(e.getKey(), "未知品种");
                    return new TopicDTO(name, "热门品种", e.getValue(), "plant_" + e.getKey());
                })
                .sorted((a, b) -> Long.compare(b.getCount(), a.getCount()))
                .limit(15)
                .collect(Collectors.toList());
    }

    private List<TopicDTO> aggregateKeywordTopics(List<Post> posts, List<String> keywords, String category) {
        Map<String, Long> countMap = new HashMap<>();

        for (Post post : posts) {
            String text = (post.getTitle() == null ? "" : post.getTitle()) + " " +
                    (post.getContent() == null ? "" : post.getContent());
            for (String keyword : keywords) {
                if (text.contains(keyword)) {
                    countMap.merge(keyword, 1L, Long::sum);
                }
            }
        }

        return countMap.entrySet().stream()
                .filter(e -> e.getValue() > 0)
                .map(e -> new TopicDTO(e.getKey(), category, e.getValue(), "keyword_" + e.getKey()))
                .sorted((a, b) -> Long.compare(b.getCount(), a.getCount()))
                .limit(15)
                .collect(Collectors.toList());
    }

    public List<Post> getUserPosts(Long userId) {
        return postRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public Optional<Post> getPostById(Long id) {
        Optional<Post> postOpt = postRepository.findById(id);
        postOpt.ifPresent(post -> {
            post.setViewCount(post.getViewCount() + 1);
            postRepository.save(post);
        });
        return postOpt;
    }

    public Post createPost(Post post) {
        post.setHotnessScore(hotnessService.calculateInitialHotness());
        return postRepository.save(post);
    }

    public Post updatePost(Long id, Post post) {
        post.setId(id);
        return postRepository.save(post);
    }

    public void deletePost(Long id) {
        postRepository.deleteById(id);
    }

    public Post incrementCommentCount(Long postId) {
        Optional<Post> postOpt = postRepository.findById(postId);
        if (postOpt.isPresent()) {
            Post post = postOpt.get();
            post.setCommentCount(post.getCommentCount() + 1);
            return postRepository.save(post);
        }
        return null;
    }
}
