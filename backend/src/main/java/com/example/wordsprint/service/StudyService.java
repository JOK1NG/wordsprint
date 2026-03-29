package com.example.wordsprint.service;

import com.example.wordsprint.dto.StudyRandomQuery;
import com.example.wordsprint.dto.StudySubmitRequest;
import com.example.wordsprint.vo.StudyRandomResponse;
import com.example.wordsprint.vo.StudySubmitResponse;

public interface StudyService {

    StudyRandomResponse random(Long userId, StudyRandomQuery query);

    StudySubmitResponse submit(Long userId, StudySubmitRequest request);
}
