package com.plantcare.service;

import com.plantcare.entity.Comment;
import com.plantcare.entity.Post;
import com.plantcare.repository.CommentRepository;
import com.plantcare.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class HotnessService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    private static final double W_VIEW = 0.1;
    private static final double W_LIKE = 1.5;
    private static final double W_COMMENT = 2.0;
    private static final double BEST_ANSWER_BONUS = 50.0;
    private static final double INITIAL_BOOST = 30.0;
    private static final double INITIAL_BOOST_HOURS = 24.0;
    private static final double DECAY_FACTOR = 0.02;

    public double calculateHotness(Post post, boolean hasBestAnswer) {
        double viewScore = post.getViewCount() * W_VIEW;
        double likeScore = post.getLikeCount() * W_LIKE;
        double commentScore = post.getCommentCount() * W_COMMENT;
        double bestAnswerBonus = hasBestAnswer ? BEST_ANSWER_BONUS : 0.0;

        double hoursSinceCreation = getHoursSince(post.getCreatedAt());
        double initialBoost = 0.0;
        if (hoursSinceCreation < INITIAL_BOOST_HOURS) {
            double ratio = 1.0 - (hoursSinceCreation / INITIAL_BOOST_HOURS);
            initialBoost = INITIAL_BOOST * ratio;
        }

        double rawScore = viewScore + likeScore + commentScore + bestAnswerBonus + initialBoost;
        double timeDecay = 1.0 / (1.0 + hoursSinceCreation * DECAY_FACTOR);

        return rawScore * timeDecay;
    }

    public double calculateHotness(Post post) {
        return calculateHotness(post, false);
    }

    public double calculateInitialHotness() {
        return INITIAL_BOOST;
    }

    public void recalculateAllHotness() {
        log.info("Starting hourly hotness recalculation...");
        List<Post> allPosts = postRepository.findAll();
        if (allPosts.isEmpty()) {
            log.info("No posts found, skipping hotness recalculation.");
            return;
        }

        List<Long> postIds = allPosts.stream()
                .map(Post::getId)
                .collect(Collectors.toList());

        Set<Long> postsWithBestAnswer = findPostsWithBestAnswer(postIds);

        int updated = 0;
        for (Post post : allPosts) {
            boolean hasBestAnswer = postsWithBestAnswer.contains(post.getId());
            double newHotness = calculateHotness(post, hasBestAnswer);
            post.setHotnessScore(newHotness);
            updated++;
        }

        postRepository.saveAll(allPosts);
        log.info("Hotness recalculation complete. Updated {} posts.", updated);
    }

    private Set<Long> findPostsWithBestAnswer(List<Long> postIds) {
        List<Comment> bestAnswers = commentRepository.findByIsBestAnswerTrue();
        return bestAnswers.stream()
                .map(Comment::getPostId)
                .filter(postIds::contains)
                .collect(Collectors.toSet());
    }

    private double getHoursSince(LocalDateTime createdAt) {
        if (createdAt == null) {
            return 0.0;
        }
        Duration duration = Duration.between(createdAt, LocalDateTime.now());
        return duration.toMinutes() / 60.0;
    }
}
