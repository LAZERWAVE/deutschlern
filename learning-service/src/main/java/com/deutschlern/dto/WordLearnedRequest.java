
package com.deutschlern.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record WordLearnedRequest(
        @NotNull Long studentId,
        @NotNull Long vocabularyId,
        @NotBlank String germanWord,
        @NotBlank String level
) {}
