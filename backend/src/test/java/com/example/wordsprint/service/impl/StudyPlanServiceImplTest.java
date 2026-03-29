package com.example.wordsprint.service.impl;

import com.example.wordsprint.common.BusinessException;
import com.example.wordsprint.dto.StudyPlanUpdateRequest;
import com.example.wordsprint.entity.StudyPlan;
import com.example.wordsprint.mapper.StudyPlanMapper;
import com.example.wordsprint.vo.StudyPlanVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StudyPlanServiceImplTest {

    @Mock
    private StudyPlanMapper studyPlanMapper;

    @InjectMocks
    private StudyPlanServiceImpl studyPlanService;

    @Test
    void shouldReturnDefaultPlanWhenNoRecordExists() {
        when(studyPlanMapper.selectOne(any())).thenReturn(null);

        StudyPlanVO result = studyPlanService.get(1L);

        assertEquals(20, result.getDailyTargetCount());
        assertEquals(10, result.getReviewTargetCount());
        assertFalse(result.getReminderEnabled());
        assertNull(result.getReminderTime());
    }

    @Test
    void shouldCreatePlanWhenUpdatingWithoutExistingRecord() {
        when(studyPlanMapper.selectOne(any())).thenReturn(null);
        StudyPlanUpdateRequest request = new StudyPlanUpdateRequest();
        request.setDailyTargetCount(30);
        request.setReviewTargetCount(15);
        request.setReminderEnabled(true);
        request.setReminderTime(LocalTime.of(8, 30));

        StudyPlanVO result = studyPlanService.update(2L, request);

        ArgumentCaptor<StudyPlan> captor = ArgumentCaptor.forClass(StudyPlan.class);
        verify(studyPlanMapper).insert(captor.capture());
        verify(studyPlanMapper, never()).updateById(any(StudyPlan.class));
        StudyPlan saved = captor.getValue();
        assertEquals(2L, saved.getUserId());
        assertEquals(30, saved.getDailyTargetCount());
        assertEquals(15, saved.getReviewTargetCount());
        assertEquals(1, saved.getReminderEnabled());
        assertEquals(LocalTime.of(8, 30), saved.getReminderTime());
        assertTrue(result.getReminderEnabled());
        assertEquals(LocalTime.of(8, 30), result.getReminderTime());
    }

    @Test
    void shouldClearReminderTimeWhenReminderDisabled() {
        StudyPlan existing = new StudyPlan();
        existing.setId(9L);
        existing.setUserId(3L);
        existing.setReminderTime(LocalTime.of(21, 0));
        when(studyPlanMapper.selectOne(any())).thenReturn(existing);

        StudyPlanUpdateRequest request = new StudyPlanUpdateRequest();
        request.setDailyTargetCount(25);
        request.setReviewTargetCount(12);
        request.setReminderEnabled(false);
        request.setReminderTime(LocalTime.of(7, 0));

        StudyPlanVO result = studyPlanService.update(3L, request);

        verify(studyPlanMapper).updateById(existing);
        assertEquals(0, existing.getReminderEnabled());
        assertNull(existing.getReminderTime());
        assertFalse(result.getReminderEnabled());
        assertNull(result.getReminderTime());
    }

    @Test
    void shouldRejectMissingReminderTimeWhenReminderEnabled() {
        StudyPlanUpdateRequest request = new StudyPlanUpdateRequest();
        request.setDailyTargetCount(20);
        request.setReviewTargetCount(10);
        request.setReminderEnabled(true);

        assertThrows(BusinessException.class, () -> studyPlanService.update(1L, request));
    }
}
