package com.example.wordsprint.service;

import com.example.wordsprint.common.PageResult;
import com.example.wordsprint.dto.PublicWordPageQuery;
import com.example.wordsprint.vo.PublicWordCsvImportResultVO;
import com.example.wordsprint.vo.PublicWordImportResultVO;
import com.example.wordsprint.vo.PublicWordVO;
import org.springframework.web.multipart.MultipartFile;

public interface PublicWordService {

    PageResult<PublicWordVO> page(PublicWordPageQuery query);

    PublicWordImportResultVO importToUserWordCard(Long userId, Long publicWordId);

    PublicWordCsvImportResultVO importCsv(MultipartFile file);
}
