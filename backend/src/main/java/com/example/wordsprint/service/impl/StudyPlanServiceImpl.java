package com.example.wordsprint.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.wordsprint.common.BusinessException;
import com.example.wordsprint.common.ErrorCode;
import com.example.wordsprint.dto.StudyPlanUpdateRequest;
import com.example.wordsprint.entity.StudyPlan;
import com.example.wordsprint.mapper.StudyPlanMapper;
import com.example.wordsprint.service.StudyPlanService;
import com.example.wordsprint.vo.StudyPlanVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;

@Service
@RequiredArgsConstructor
public class StudyPlanServiceImpl implements StudyPlanService {

    private static final int DEFAULT_DAILY_TARGET_COUNT = 20;
    private static final int DEFAULT_REVIEW_TARGET_COUNT = 10;

    private final StudyPlanMapper studyPlanMapper;

    @Override
    public StudyPlanVO get(Long userId) {
        StudyPlan studyPlan = findByUserId(userId);
        if (studyPlan == null) {
            return defaultPlan();
        }
        return toVO(studyPlan);
    }

    @Override
    @Transactional
    public StudyPlanVO update(Long userId, StudyPlanUpdateRequest request) {
        validateReminder(request.getReminderEnabled(), request.getReminderTime());

        StudyPlan studyPlan = findByUserId(userId);
        if (studyPlan == null) {
            studyPlan = new StudyPlan();
            studyPlan.setUserId(userId);
        }

        studyPlan.setDailyTargetCount(request.getDailyTargetCount());
        studyPlan.setReviewTargetCount(request.getReviewTargetCount());
        studyPlan.setReminderEnabled(Boolean.TRUE.equals(request.getReminderEnabled()) ? 1 : 0);
        studyPlan.setReminderTime(Boolean.TRUE.equals(request.getReminderEnabled()) ? request.getReminderTime() : null);

        if (studyPlan.getId() == null) {
            studyPlanMapper.insert(studyPlan);
        } else {
            studyPlanMapper.updateById(studyPlan);
        }

        return toVO(studyPlan);
    }

    private StudyPlan findByUserId(Long userId) {
        return studyPlanMapper.selectOne(new LambdaQueryWrapper<StudyPlan>()
                .eq(StudyPlan::getUserId, userId)
                .last("LIMIT 1"));
    }

    private void validateReminder(Boolean reminderEnabled, LocalTime reminderTime) {
        if (Boolean.TRUE.equals(reminderEnabled) && reminderTime == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "开启提醒时 reminderTime 不能为空");
        }
    }

    private StudyPlanVO defaultPlan() {
        return new StudyPlanVO(DEFAULT_DAILY_TARGET_COUNT, DEFAULT_REVIEW_TARGET_COUNT, false, null);
    }

    private StudyPlanVO toVO(StudyPlan studyPlan) {
        return new StudyPlanVO(
                studyPlan.getDailyTargetCount(),
                studyPlan.getReviewTargetCount(),
                studyPlan.getReminderEnabled() != null && studyPlan.getReminderEnabled() == 1,
                studyPlan.getReminderTime()
        );
    }
}
