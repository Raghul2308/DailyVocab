package com.projectAura.DailyVocab.model;

import java.util.List;

public record VocabWord(
        String word,
        String part_of_speech,
        String meaning,
        String example_sentence,
        List<String> synonyms
) {}
