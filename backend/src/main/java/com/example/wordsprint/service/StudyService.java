package com.example.wordsprint.service;

import com.example.wordsprint.dto.StudyRandomQuery;
import com.example.wordsprint.dto.StudyStatisticsQuery;
import com.example.wordsprint.dto.StudySubmitRequest;
import com.example.wordsprint.vo.StudyRandomResponse;
import com.example.wordsprint.vo.StudyStatisticsVO;
import com.example.wordsprint.vo.StudySubmitResponse;
import com.example.wordsprint.vo.StudyTodaySummaryVO;

public interface StudyService {

    StudyRandomResponse random(Long userId, StudyRandomQuery query);

    StudySubmitResponse submit(Long userId, StudySubmitRequest request);

    StudyTodaySummaryVO todaySummary(Long userId);

    StudyStatisticsVO statistics(Long userId, StudyStatisticsQuery query);
}
