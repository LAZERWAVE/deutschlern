package com.deutschlern.event;

import java.time.LocalDateTime;

public record WordLearnedEvent(
        Long studentId,
        Long vocabularyId,
        String germanWord,
        String level,
        LocalDateTime occurredAt
) {}
