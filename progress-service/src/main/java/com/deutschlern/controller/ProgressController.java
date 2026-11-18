package com.deutschlern.controller;

import com.deutschlern.service.ProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/progress")
@RequiredArgsConstructor
public class ProgressController {

    private final ProgressService progressService;

    @GetMapping("/{studentId}/word-count")
    public long getStudentWordCount(
            @PathVariable Long studentId) {

        return progressService.getStudentWordCount(studentId);
    }
}
