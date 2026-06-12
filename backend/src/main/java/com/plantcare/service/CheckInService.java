package com.plantcare.service;

import com.plantcare.dto.AchievementBadgeDTO;
import com.plantcare.dto.CheckInCalendarDTO;
import com.plantcare.dto.CheckInRequestDTO;
import com.plantcare.dto.CheckInStatusDTO;
import com.plantcare.entity.AchievementBadge;
import com.plantcare.entity.CareCheckIn;
import com.plantcare.entity.CareLog;
import com.plantcare.repository.AchievementBadgeRepository;
import com.plantcare.repository.CareCheckInRepository;
import com.plantcare.repository.CareLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CheckInService {

    private final CareCheckInRepository checkInRepository;
    private final AchievementBadgeRepository achievementRepository;
    private final CareLogRepository careLogRepository;

    private static final Map<Integer, String[]> MILESTONE_DEFINITIONS = Map.of(
            7, new String[]{"STREAK_7", "🌱 初心萌芽", "连续打卡7天", "7天坚持，绿植养护之路已启程"},
            30, new String[]{"STREAK_30", "🌿 生机盎然", "连续打卡30天", "30天不懈，你的绿植茁壮成长"},
            100, new String[]{"STREAK_100", "🌳 绿意满园", "连续打卡100天", "100天守护，你是真正的绿植守护者"}
    );

    private static final List<Integer> MILESTONE_DAYS = Arrays.asList(7, 30, 100);

    @Transactional
    public CareCheckIn checkIn(CheckInRequestDTO request) {
        LocalDate checkinDate = request.getCheckinDate() != null ? request.getCheckinDate() : LocalDate.now();

        if (checkInRepository.existsByUserIdAndCheckinDate(request.getUserId(), checkinDate)) {
            throw new IllegalStateException("今日已打卡，请勿重复打卡");
        }

        int careCount = 0;
        String careLogIdsStr = null;
        if (request.getCareLogIds() != null && !request.getCareLogIds().isEmpty()) {
            careLogIdsStr = request.getCareLogIds().stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(","));
            careCount = request.getCareLogIds().size();
        } else {
            List<CareLog> todayLogs = careLogRepository.findByUserIdOrderByLogDateDesc(request.getUserId())
                    .stream()
                    .filter(log -> log.getLogDate().equals(checkinDate))
                    .collect(Collectors.toList());
            careCount = todayLogs.size();
            if (!todayLogs.isEmpty()) {
                careLogIdsStr = todayLogs.stream()
                        .map(log -> String.valueOf(log.getId()))
                        .collect(Collectors.joining(","));
            }
        }

        CareCheckIn checkIn = new CareCheckIn();
        checkIn.setUserId(request.getUserId());
        checkIn.setCheckinDate(checkinDate);
        checkIn.setCareLogIds(careLogIdsStr);
        checkIn.setCareCount(careCount);
        checkIn.setRemark(request.getRemark());
        CareCheckIn saved = checkInRepository.save(checkIn);

        int currentStreak = calculateCurrentStreak(request.getUserId());
        checkAndUnlockAchievements(request.getUserId(), currentStreak);

        return saved;
    }

    public CheckInStatusDTO getCheckInStatus(Long userId) {
        CheckInStatusDTO status = new CheckInStatusDTO();
        status.setUserId(userId);

        LocalDate today = LocalDate.now();
        status.setTodayCheckedIn(checkInRepository.existsByUserIdAndCheckinDate(userId, today));

        int currentStreak = calculateCurrentStreak(userId);
        status.setCurrentStreak(currentStreak);

        int maxStreak = calculateMaxStreak(userId);
        status.setMaxStreak(maxStreak);

        Long totalDays = checkInRepository.countByUserId(userId);
        status.setTotalCheckInDays(totalDays != null ? totalDays.intValue() : 0);

        List<LocalDate> allDates = checkInRepository.findAllCheckinDatesByUserId(userId);
        status.setLastCheckInDate(allDates.isEmpty() ? null : allDates.get(0));

        List<LocalDate> recentDates = allDates.stream().limit(7).collect(Collectors.toList());
        Collections.reverse(recentDates);
        status.setRecentCheckInDates(recentDates);

        List<AchievementBadge> unlocked = achievementRepository.findByUserIdOrderByStreakDaysAsc(userId);
        List<AchievementBadgeDTO> unlockedDTOs = unlocked.stream()
                .map(this::toBadgeDTO)
                .collect(Collectors.toList());
        status.setAchievements(unlockedDTOs);

        List<AchievementBadgeDTO> lockedDTOs = MILESTONE_DAYS.stream()
                .filter(days -> !achievementRepository.existsByUserIdAndBadgeType(userId, MILESTONE_DEFINITIONS.get(days)[0]))
                .map(days -> {
                    String[] def = MILESTONE_DEFINITIONS.get(days);
                    AchievementBadgeDTO dto = new AchievementBadgeDTO();
                    dto.setBadgeType(def[0]);
                    dto.setBadgeName(def[1]);
                    dto.setBadgeIcon(def[1]);
                    dto.setStreakDays(days);
                    dto.setDescription(def[3]);
                    dto.setUnlocked(false);
                    return dto;
                })
                .collect(Collectors.toList());
        status.setLockedAchievements(lockedDTOs);

        return status;
    }

    public CheckInCalendarDTO getCheckInCalendar(Long userId, int year, int month) {
        CheckInCalendarDTO calendar = new CheckInCalendarDTO();
        calendar.setUserId(userId);
        calendar.setYear(year);
        calendar.setMonth(month);

        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        List<CareCheckIn> checkIns = checkInRepository.findByUserIdAndCheckinDateBetweenOrderByCheckinDateAsc(userId, startDate, endDate);
        List<LocalDate> dates = checkIns.stream()
                .map(CareCheckIn::getCheckinDate)
                .collect(Collectors.toList());

        calendar.setCheckInDates(dates);
        calendar.setMonthCheckInCount(dates.size());

        return calendar;
    }

    public List<CareCheckIn> getUserCheckIns(Long userId) {
        return checkInRepository.findByUserIdOrderByCheckinDateDesc(userId);
    }

    public List<AchievementBadge> getUserAchievements(Long userId) {
        return achievementRepository.findByUserIdOrderByStreakDaysAsc(userId);
    }

    private int calculateCurrentStreak(Long userId) {
        List<LocalDate> allDates = checkInRepository.findAllCheckinDatesByUserId(userId);
        if (allDates.isEmpty()) {
            return 0;
        }

        LocalDate today = LocalDate.now();
        int streak = 0;
        LocalDate checkDate = today;

        if (!allDates.contains(today)) {
            checkDate = today.minusDays(1);
        }

        while (allDates.contains(checkDate)) {
            streak++;
            checkDate = checkDate.minusDays(1);
        }

        return streak;
    }

    private int calculateMaxStreak(Long userId) {
        List<LocalDate> allDates = checkInRepository.findAllCheckinDatesByUserId(userId);
        if (allDates.isEmpty()) {
            return 0;
        }

        List<LocalDate> sorted = new ArrayList<>(allDates);
        Collections.sort(sorted);

        int maxStreak = 1;
        int currentStreak = 1;

        for (int i = 1; i < sorted.size(); i++) {
            if (sorted.get(i).equals(sorted.get(i - 1).plusDays(1))) {
                currentStreak++;
                maxStreak = Math.max(maxStreak, currentStreak);
            } else {
                currentStreak = 1;
            }
        }

        return maxStreak;
    }

    @Transactional
    public void checkAndUnlockAchievements(Long userId, int currentStreak) {
        for (Integer milestone : MILESTONE_DAYS) {
            if (currentStreak >= milestone) {
                String[] def = MILESTONE_DEFINITIONS.get(milestone);
                if (!achievementRepository.existsByUserIdAndBadgeType(userId, def[0])) {
                    AchievementBadge badge = new AchievementBadge();
                    badge.setUserId(userId);
                    badge.setBadgeType(def[0]);
                    badge.setBadgeName(def[1]);
                    badge.setBadgeIcon(def[1]);
                    badge.setStreakDays(milestone);
                    badge.setDescription(def[3]);
                    achievementRepository.save(badge);
                }
            }
        }
    }

    private AchievementBadgeDTO toBadgeDTO(AchievementBadge badge) {
        AchievementBadgeDTO dto = new AchievementBadgeDTO();
        dto.setId(badge.getId());
        dto.setBadgeType(badge.getBadgeType());
        dto.setBadgeName(badge.getBadgeName());
        dto.setBadgeIcon(badge.getBadgeIcon());
        dto.setStreakDays(badge.getStreakDays());
        dto.setDescription(badge.getDescription());
        dto.setUnlocked(true);
        dto.setUnlockedAt(badge.getUnlockedAt());
        return dto;
    }
}
