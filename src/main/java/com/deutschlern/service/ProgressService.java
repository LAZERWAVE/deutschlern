package com.deutschlern.service;

import com.deutschlern.repository.LearningActivityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class ProgressService {

    private final LearningActivityRepository repository;
    private final StringRedisTemplate redisTemplate;

    private static final Duration CACHE_TTL =
            Duration.ofMinutes(10);

    public long getStudentWordCount(Long studentId) {

        String cacheKey = "progress:" + studentId;

        // 1. Check Redis
        String cachedCount = redisTemplate
                .opsForValue()
                .get(cacheKey);

        if (cachedCount != null) {
            return Long.parseLong(cachedCount);
        }

        // 2. Cache miss: query PostgreSQL
        long wordCount = repository.countByStudentId(studentId);

        // 3. Save result in Redis for 10 minutes
        redisTemplate.opsForValue().set(
                cacheKey,
                String.valueOf(wordCount),
                CACHE_TTL
        );

        return wordCount;
    }
}
