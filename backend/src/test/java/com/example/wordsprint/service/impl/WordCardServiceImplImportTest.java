package com.example.wordsprint.service.impl;

import com.example.wordsprint.common.BusinessException;
import com.example.wordsprint.entity.WordCard;
import com.example.wordsprint.mapper.WordCardMapper;
import com.example.wordsprint.vo.WordCardImportResultVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class WordCardServiceImplImportTest {

    @Mock
    private WordCardMapper wordCardMapper;

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @InjectMocks
    private WordCardServiceImpl wordCardService;

    @Test
    void shouldImportCsvWithHeader() {
        String csv = "word,meaning,phonetic,exampleSentence,tags,isPublic\n"
                + "abandon,放弃,/əˈbændən/,He had to abandon the plan.,cet4|verb,false\n"
                + "benefit,好处,/ˈbenɪfɪt/,Regular exercise will benefit your health.,cet4|noun,1\n";
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "words.csv",
                "text/csv",
                csv.getBytes(StandardCharsets.UTF_8)
        );

        WordCardImportResultVO result = wordCardService.importCsv(1L, file);

        assertEquals(2, result.getTotalRows());
        assertEquals(2, result.getSuccessCount());
        assertEquals(0, result.getFailedCount());
        assertEquals(0, result.getErrors().size());

        ArgumentCaptor<WordCard> captor = ArgumentCaptor.forClass(WordCard.class);
        verify(wordCardMapper, times(2)).insert(captor.capture());
        List<WordCard> inserted = captor.getAllValues();
        assertEquals("abandon", inserted.get(0).getWord());
        assertEquals("放弃", inserted.get(0).getMeaning());
        assertFalse(inserted.get(0).getIsPublic() == 1);
        assertEquals(1, inserted.get(1).getIsPublic());
    }

    @Test
    void shouldCollectErrorsForInvalidRows() {
        String csv = "word,meaning,phonetic,exampleSentence,tags,isPublic\n"
                + "valid,有效词义,,,,\n"
                + "invalidOnlyWord,,,,,\n"
                + "alsoInvalid,有词义,,,,maybe\n";
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "invalid.csv",
                "text/csv",
                csv.getBytes(StandardCharsets.UTF_8)
        );

        WordCardImportResultVO result = wordCardService.importCsv(2L, file);

        assertEquals(3, result.getTotalRows());
        assertEquals(1, result.getSuccessCount());
        assertEquals(2, result.getFailedCount());
        assertEquals(2, result.getErrors().size());
        verify(wordCardMapper, times(1)).insert(any(WordCard.class));
    }

    @Test
    void shouldRejectEmptyFile() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "empty.csv",
                "text/csv",
                new byte[0]
        );

        assertThrows(BusinessException.class, () -> wordCardService.importCsv(1L, file));
    }
}
