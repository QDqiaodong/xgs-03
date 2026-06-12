package com.plantcare.service;

import com.plantcare.entity.PointRecord;
import com.plantcare.entity.User;
import com.plantcare.repository.PointRecordRepository;
import com.plantcare.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PointService {

    private final UserRepository userRepository;
    private final PointRecordRepository pointRecordRepository;

    public static final int BEST_ANSWER_REWARD = 10;

    @Transactional
    public User addPoints(Long userId, int points, String reason, String relatedType, Long relatedId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return null;
        }
        User user = userOpt.get();
        user.setPoints(user.getPoints() + points);
        userRepository.save(user);

        PointRecord record = new PointRecord();
        record.setUserId(userId);
        record.setPoints(points);
        record.setReason(reason);
        record.setRelatedType(relatedType);
        record.setRelatedId(relatedId);
        pointRecordRepository.save(record);

        return user;
    }

    @Transactional
    public User deductPoints(Long userId, int points, String reason, String relatedType, Long relatedId) {
        return addPoints(userId, -points, reason, relatedType, relatedId);
    }

    public List<PointRecord> getUserPointRecords(Long userId) {
        return pointRecordRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public Page<PointRecord> getUserPointRecords(Long userId, Pageable pageable) {
        return pointRecordRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    public Integer getUserPoints(Long userId) {
        return userRepository.findById(userId)
                .map(User::getPoints)
                .orElse(0);
    }
}
