package com.example.wordsprint.service;

import com.example.wordsprint.dto.StudyPlanUpdateRequest;
import com.example.wordsprint.vo.StudyPlanVO;

public interface StudyPlanService {

    StudyPlanVO get(Long userId);

    StudyPlanVO update(Long userId, StudyPlanUpdateRequest request);
}
