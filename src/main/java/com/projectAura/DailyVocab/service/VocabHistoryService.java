package com.projectAura.DailyVocab.service;

import com.projectAura.DailyVocab.model.VocabWord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.io.File;
import java.util.Collections;
import java.util.List;

@Service
public class VocabHistoryService {

    private static final Logger log = LoggerFactory.getLogger(VocabHistoryService.class);
    private final ObjectMapper objectMapper;

    @Value("${vocab.history.path:vocab-history.json}")
    private String historyPath;

    public VocabHistoryService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public List<VocabWord> loadYesterdaysWords() {
        try {
            File file = new File(historyPath);
            if (!file.exists()) return Collections.emptyList();
            return objectMapper.readValue(
                    file,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, VocabWord.class)
            );
        } catch (Exception e) {
            log.warn("Could not load vocab history: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    public void saveTodaysWords(List<VocabWord> words) {
        try {
            objectMapper.writeValue(new File(historyPath), words);
        } catch (Exception e) {
            log.warn("Could not save vocab history: {}", e.getMessage());
        }
    }
}
