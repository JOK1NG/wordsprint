package com.example.wordsprint.service.impl;

import com.example.wordsprint.common.BusinessException;
import com.example.wordsprint.entity.PublicWordLibrary;
import com.example.wordsprint.mapper.PublicWordLibraryMapper;
import com.example.wordsprint.mapper.WordCardMapper;
import com.example.wordsprint.vo.PublicWordCsvImportResultVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PublicWordServiceImplCsvImportTest {

    @Mock
    private PublicWordLibraryMapper publicWordLibraryMapper;

    @Mock
    private WordCardMapper wordCardMapper;

    @InjectMocks
    private PublicWordServiceImpl publicWordService;

    @Test
    void shouldInsertAndUpdateRows() {
        String csv = "word,meaning,phonetic,exampleSentence,levelTag,sourceName,status\n"
                + "abandon,放弃,/əˈbændən/,He had to abandon the plan.,CET4,seed,1\n"
                + "benefit,好处,/ˈbenɪfɪt/,Regular exercise benefits your health.,CET4,seed,1\n";
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "public_words.csv",
                "text/csv",
                csv.getBytes(StandardCharsets.UTF_8)
        );

        PublicWordLibrary existing = new PublicWordLibrary();
        existing.setId(9L);
        existing.setWord("benefit");
        existing.setLevelTag("CET4");

        when(publicWordLibraryMapper.selectOne(any())).thenReturn(null, existing);

        PublicWordCsvImportResultVO result = publicWordService.importCsv(file);

        assertEquals(2, result.getTotalRows());
        assertEquals(1, result.getInsertedCount());
        assertEquals(1, result.getUpdatedCount());
        assertEquals(0, result.getFailedCount());

        ArgumentCaptor<PublicWordLibrary> insertCaptor = ArgumentCaptor.forClass(PublicWordLibrary.class);
        verify(publicWordLibraryMapper, times(1)).insert(insertCaptor.capture());
        assertEquals("abandon", insertCaptor.getValue().getWord());

        verify(publicWordLibraryMapper, times(1)).updateById(existing);
        assertEquals("好处", existing.getMeaning());
    }

    @Test
    void shouldCollectRowErrorsAndContinue() {
        String csv = "word,meaning,phonetic,exampleSentence,levelTag,sourceName,status\n"
                + "valid,有效词义,,,,,\n"
                + "invalid_only_word,,,,,,\n"
                + "bad_status,坏状态,,,,,MAYBE\n";
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "public_words_bad.csv",
                "text/csv",
                csv.getBytes(StandardCharsets.UTF_8)
        );

        when(publicWordLibraryMapper.selectOne(any())).thenReturn(null);

        PublicWordCsvImportResultVO result = publicWordService.importCsv(file);

        assertEquals(3, result.getTotalRows());
        assertEquals(1, result.getInsertedCount());
        assertEquals(0, result.getUpdatedCount());
        assertEquals(2, result.getFailedCount());
        assertEquals(2, result.getErrors().size());
    }

    @Test
    void shouldRejectEmptyFile() {
        MockMultipartFile file = new MockMultipartFile("file", "empty.csv", "text/csv", new byte[0]);
        assertThrows(BusinessException.class, () -> publicWordService.importCsv(file));
    }
}
