package com.plantcare.service;

import com.plantcare.dto.UserStatsDTO;
import com.plantcare.entity.User;
import com.plantcare.repository.CareCheckInRepository;
import com.plantcare.repository.CareLogRepository;
import com.plantcare.repository.CommentRepository;
import com.plantcare.repository.PlantArchiveRepository;
import com.plantcare.repository.PostRepository;
import com.plantcare.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PlantArchiveRepository plantArchiveRepository;
    private final CareLogRepository careLogRepository;
    private final CareCheckInRepository careCheckInRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public User createUser(User user) {
        return userRepository.save(user);
    }

    public User updateUser(Long id, User user) {
        user.setId(id);
        return userRepository.save(user);
    }

    public UserStatsDTO getUserStats(Long userId) {
        UserStatsDTO stats = new UserStatsDTO();
        stats.setUserId(userId);

        Long totalCareDays = careCheckInRepository.countByUserId(userId);
        stats.setTotalCareDays(totalCareDays != null ? totalCareDays.intValue() : 0);

        Long totalCareTimes = careLogRepository.countByUserId(userId);
        stats.setTotalCareTimes(totalCareTimes != null ? totalCareTimes.intValue() : 0);

        Long totalPlants = plantArchiveRepository.countByUserId(userId);
        stats.setTotalPlants(totalPlants != null ? totalPlants.intValue() : 0);

        Long totalPosts = postRepository.countByUserId(userId);
        stats.setTotalPosts(totalPosts != null ? totalPosts.intValue() : 0);

        Long postLikes = postRepository.sumLikeCountByUserId(userId);
        Long commentLikes = commentRepository.sumLikeCountByUserId(userId);
        int totalLikes = (postLikes != null ? postLikes.intValue() : 0)
                + (commentLikes != null ? commentLikes.intValue() : 0);
        stats.setTotalLikes(totalLikes);

        Long bestAnswerCount = commentRepository.countBestAnswerByUserId(userId);
        stats.setBestAnswerCount(bestAnswerCount != null ? bestAnswerCount.intValue() : 0);

        return stats;
    }
}
