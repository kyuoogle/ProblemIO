package com.problemio.ranking.service;

import com.problemio.ranking.domain.RankingPeriod;
import com.problemio.ranking.dto.RankingResponseDto;
import com.problemio.ranking.dto.RankingRowDto;
import com.problemio.ranking.mapper.RankingMapper;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class RankingServiceImpl implements RankingService {

    private final RankingMapper rankingMapper;

    private static final double ACCURACY_BASE = 0.3;   // weight floor
    private static final double ACCURACY_WEIGHT = 0.7; // weight for accuracy
    private static final double PRIOR_QUESTIONS = 20.0;
    private static final double PRIOR_ACCURACY = 0.6;

    public RankingServiceImpl(RankingMapper rankingMapper) {
        this.rankingMapper = rankingMapper;
    }

    @Override
    @Cacheable(
            value = "ranking",
            key = "#period.name() + '_' + #limit",
            unless = "#result == null || #result.isEmpty()"
    )
    public List<RankingResponseDto> getRanking(RankingPeriod period, int limit) {
        int topN = (limit > 0 && limit <= 100) ? limit : 20;

        LocalDate today = LocalDate.now();
        LocalDateTime start;
        LocalDateTime end;

        switch (period) {
            case TODAY -> {
                start = today.atStartOfDay();
                end = today.plusDays(1).atStartOfDay();
            }
            case YESTERDAY -> {
                start = today.minusDays(1).atStartOfDay();
                end = today.atStartOfDay();
            }
            case WEEK -> {
                LocalDate weekStart = today.with(DayOfWeek.MONDAY);
                start = weekStart.atStartOfDay();
                end = weekStart.plusWeeks(1).atStartOfDay();
            }
            default -> throw new IllegalArgumentException("Unsupported period: " + period);
        }

        List<RankingRowDto> rows = rankingMapper.findRankingInPeriod(start, end, topN);

        return rows.stream()
                .map(r -> RankingResponseDto.of(r, calcScore(r)))
                .sorted((a, b) -> Integer.compare(b.getScore(), a.getScore()))
                .toList();
    }

    private int calcScore(RankingRowDto r) {
        double smoothedAccuracy = calcSmoothedAccuracy(r.getTotalCorrect(), r.getTotalQuestions());
        double score = r.getSolvedQuizCount() * (ACCURACY_BASE + ACCURACY_WEIGHT * smoothedAccuracy) * 10;
        return (int) Math.round(score);
    }

    private double calcSmoothedAccuracy(int totalCorrect, int totalQuestions) {
        double correct = Math.max(0, totalCorrect);
        double questions = Math.max(0, totalQuestions);
        return (correct + PRIOR_QUESTIONS * PRIOR_ACCURACY) / (questions + PRIOR_QUESTIONS);
    }
}
