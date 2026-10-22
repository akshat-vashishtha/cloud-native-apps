package com.learning.kpi.presentation.rest;

import com.learning.kpi.application.dto.UserVelocityDto;
import com.learning.kpi.infrastructure.config.KpiKafkaProperties;
import com.learning.kpi.infrastructure.messaging.TransactionKpiConsumer;
import com.learning.kpi.infrastructure.persistence.criteria.KpiCriteriaRepository;
import com.learning.kpi.infrastructure.persistence.document.DailyMetricDocument;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/v1/kpi")
@Slf4j
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('kpi:read_analytics')")
public class KpiInteractiveQueryController {

    private final StringRedisTemplate redisTemplate;
    private final KpiCriteriaRepository kpiCriteriaRepository;
    private final KpiKafkaProperties kafkaProperties;

    @GetMapping("/realtime/velocity/{accountId}")
    public ResponseEntity<UserVelocityDto> getAccountRealtimeVelocity(@PathVariable String accountId) {
        String velocityKey = TransactionKpiConsumer.VELOCITY_KEY_PREFIX + accountId;
        String countStr = redisTemplate.opsForValue().get(velocityKey);
        long count = 0L;
        if (countStr != null) {
            try {
                count = Long.parseLong(countStr);
            } catch (NumberFormatException e) {
                count = 0L;
            }
        }

        boolean alert = count >= kafkaProperties.velocity().threshold();
        return ResponseEntity.ok(new UserVelocityDto(accountId, count, alert, Instant.now()));
    }

    @GetMapping("/historical/daily")
    public ResponseEntity<List<DailyMetricDocument>> getHistoricalDailyMetrics(
            @RequestParam(defaultValue = "USD") String currency) {
        log.info("Fetching historical daily metrics for currency: {}", currency);
        List<DailyMetricDocument> metrics = kpiCriteriaRepository.findRecentMetricsByCurrency(currency, 30);
        return ResponseEntity.ok(metrics);
    }
}
