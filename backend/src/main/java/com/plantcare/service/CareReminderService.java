package com.plantcare.service;

import com.plantcare.dto.CompleteReminderRequest;
import com.plantcare.dto.DeferReminderRequest;
import com.plantcare.entity.CareLog;
import com.plantcare.entity.CareReminder;
import com.plantcare.entity.PlantArchive;
import com.plantcare.repository.CareLogRepository;
import com.plantcare.repository.CareReminderRepository;
import com.plantcare.repository.PlantArchiveRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CareReminderService {

    private static final String TYPE_WATER = "浇水";
    private static final String TYPE_FERTILIZE = "施肥";
    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_COMPLETED = "COMPLETED";
    private static final String STATUS_DEFERRED = "DEFERRED";
    private static final String STATUS_CANCELLED = "CANCELLED";

    private final CareReminderRepository careReminderRepository;
    private final PlantArchiveRepository plantArchiveRepository;
    private final CareLogRepository careLogRepository;

    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void generateDailyReminders() {
        log.info("Starting daily care reminder generation at {}", LocalDateTime.now());
        LocalDate today = LocalDate.now();
        List<PlantArchive> archives = plantArchiveRepository.findAll();

        int waterCount = 0;
        int fertilizeCount = 0;

        for (PlantArchive archive : archives) {
            if (archive.getReminderEnabled() == null || !archive.getReminderEnabled()) {
                continue;
            }

            if (archive.getWaterInterval() != null && archive.getWaterInterval() > 0) {
                if (shouldGenerateReminder(archive, TYPE_WATER, today)) {
                    if (createReminderIfNotExists(archive, TYPE_WATER, today)) {
                        waterCount++;
                    }
                }
            }

            if (archive.getFertilizeInterval() != null && archive.getFertilizeInterval() > 0) {
                if (shouldGenerateReminder(archive, TYPE_FERTILIZE, today)) {
                    if (createReminderIfNotExists(archive, TYPE_FERTILIZE, today)) {
                        fertilizeCount++;
                    }
                }
            }
        }

        log.info("Daily reminder generation completed. Water: {}, Fertilize: {}", waterCount, fertilizeCount);
    }

    private boolean shouldGenerateReminder(PlantArchive archive, String reminderType, LocalDate today) {
        CareLog lastLog = careLogRepository.findTopByPlantArchiveIdAndOperationTypeOrderByLogDateDescCreatedAtDesc(
                archive.getId(), reminderType);

        int interval = TYPE_WATER.equals(reminderType) ? archive.getWaterInterval() : archive.getFertilizeInterval();

        LocalDate lastActionDate = null;
        if (lastLog != null && lastLog.getLogDate() != null) {
            lastActionDate = lastLog.getLogDate();
        } else if (archive.getPurchaseDate() != null) {
            lastActionDate = archive.getPurchaseDate();
        }

        if (lastActionDate == null) {
            return true;
        }

        long daysBetween = ChronoUnit.DAYS.between(lastActionDate, today);
        return daysBetween >= interval;
    }

    private boolean createReminderIfNotExists(PlantArchive archive, String reminderType, LocalDate reminderDate) {
        boolean exists = careReminderRepository.existsByPlantArchiveIdAndReminderTypeAndReminderDateAndStatusNot(
                archive.getId(), reminderType, reminderDate, STATUS_CANCELLED);
        if (exists) {
            return false;
        }

        CareReminder reminder = new CareReminder();
        reminder.setUserId(archive.getUserId());
        reminder.setPlantArchiveId(archive.getId());
        reminder.setReminderType(reminderType);
        reminder.setReminderDate(reminderDate);
        reminder.setStatus(STATUS_PENDING);
        reminder.setDeferredCount(0);

        careReminderRepository.save(reminder);
        log.info("Created {} reminder for plant archive {} on {}", reminderType, archive.getId(), reminderDate);
        return true;
    }

    public List<CareReminder> getUserReminders(Long userId) {
        return careReminderRepository.findByUserIdWithPlantArchive(userId);
    }

    public Page<CareReminder> getUserRemindersPaged(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(
                Sort.Order.desc("reminderDate"),
                Sort.Order.desc("createdAt")
        ));
        return careReminderRepository.findByUserId(userId, pageable);
    }

    public List<CareReminder> getUserRemindersByStatus(Long userId, String status) {
        return careReminderRepository.findByUserIdAndStatusWithPlantArchive(userId, status);
    }

    public Page<CareReminder> getUserRemindersByStatusPaged(Long userId, String status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(
                Sort.Order.asc("reminderDate"),
                Sort.Order.desc("createdAt")
        ));
        return careReminderRepository.findByUserIdAndStatus(userId, status, pageable);
    }

    public List<CareReminder> getPendingReminders(Long userId) {
        return getUserRemindersByStatus(userId, STATUS_PENDING);
    }

    public List<CareReminder> getPlantReminders(Long plantArchiveId) {
        return careReminderRepository.findByPlantArchiveIdOrderByReminderDateDescCreatedAtDesc(plantArchiveId);
    }

    public Optional<CareReminder> getReminderById(Long id) {
        return careReminderRepository.findByIdWithPlantArchive(id);
    }

    @Transactional
    public Optional<CareReminder> completeReminder(Long id, CompleteReminderRequest request) {
        return careReminderRepository.findById(id).map(reminder -> {
            if (STATUS_COMPLETED.equals(reminder.getStatus())) {
                return reminder;
            }

            CareLog careLog = new CareLog();
            careLog.setPlantArchiveId(reminder.getPlantArchiveId());
            careLog.setUserId(reminder.getUserId());
            careLog.setLogDate(LocalDate.now());
            careLog.setOperationType(reminder.getReminderType());

            if (request != null) {
                careLog.setDetails(request.getDetails());
                careLog.setGrowthStatus(request.getGrowthStatus());
                careLog.setDiseaseStatus(request.getDiseaseStatus());
                careLog.setImageUrls(request.getImageUrls());
            }

            if (careLog.getDetails() == null || careLog.getDetails().isEmpty()) {
                careLog.setDetails("完成" + reminder.getReminderType() + "提醒任务");
            }

            CareLog savedLog = careLogRepository.save(careLog);

            reminder.setStatus(STATUS_COMPLETED);
            reminder.setCompletedAt(LocalDateTime.now());
            reminder.setCareLogId(savedLog.getId());

            return careReminderRepository.save(reminder);
        });
    }

    @Transactional
    public Optional<CareReminder> deferReminder(Long id, DeferReminderRequest request) {
        return careReminderRepository.findById(id).map(reminder -> {
            if (STATUS_COMPLETED.equals(reminder.getStatus()) || STATUS_CANCELLED.equals(reminder.getStatus())) {
                return reminder;
            }

            LocalDate newDate;
            if (request != null && request.getNewReminderDate() != null) {
                newDate = request.getNewReminderDate();
            } else if (request != null && request.getDeferDays() != null && request.getDeferDays() > 0) {
                newDate = reminder.getReminderDate().plusDays(request.getDeferDays());
            } else {
                newDate = reminder.getReminderDate().plusDays(1);
            }

            LocalDate today = LocalDate.now();
            if (newDate.isBefore(today)) {
                throw new IllegalArgumentException("延期提醒日期不能早于今天（" + today + "），当前选择的日期为：" + newDate);
            }

            reminder.setReminderDate(newDate);
            reminder.setStatus(STATUS_DEFERRED);
            reminder.setDeferredCount(reminder.getDeferredCount() + 1);

            return careReminderRepository.save(reminder);
        });
    }

    @Transactional
    public Optional<CareReminder> cancelReminder(Long id) {
        return careReminderRepository.findById(id).map(reminder -> {
            if (STATUS_COMPLETED.equals(reminder.getStatus())) {
                return reminder;
            }
            reminder.setStatus(STATUS_CANCELLED);
            return careReminderRepository.save(reminder);
        });
    }

    public void triggerScan() {
        generateDailyReminders();
    }
}
