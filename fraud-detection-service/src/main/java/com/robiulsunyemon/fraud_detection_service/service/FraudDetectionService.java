package com.robiulsunyemon.fraud_detection_service.service;

import com.robiulsunyemon.fraud_detection_service.dto.TransactionEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class FraudDetectionService {

    private final StringRedisTemplate redisTemplate;


    private static final BigDecimal DEFAULT_GLOBAL_BASELINE = new BigDecimal("1000.00");

    private static final BigDecimal MAX_SPIKE_MULTIPLIER = new BigDecimal("20.00");


    public boolean validateTransaction(TransactionEvent event) {
        Long userId = event.getSenderUserId();
        BigDecimal currentAmount = event.getAmount();

        log.info("[FRAUD_CHECK] Processing transaction validation for User: {}, Amount: {}", userId, currentAmount);


        if (isVelocityLimitExceeded(userId)) {
            log.warn("[FRAUD_DETECTED] [VELOCITY_RULE] Limit exceeded for User: {}", userId);
            return false;
        }


        if (isAmountSpikeDetected(userId, currentAmount)) {
            log.warn("[FRAUD_DETECTED] [AMOUNT_SPIKE_RULE] Suspiciously high amount for User: {}", userId);
            return false;
        }


        if (isImpossibleTravelDetected(userId, event.getIpAddress())) {
            log.warn("[FRAUD_DETECTED] [IMPOSSIBLE_TRAVEL] Travel constraint anomaly for User: {}", userId);
            return false;
        }


        updateUserMovingAverage(userId, currentAmount);

        return true;
    }


    private boolean isVelocityLimitExceeded(Long userId) {
        String oneMinKey = "fraud:velocity:" + userId + ":1m";
        String oneHourKey = "fraud:velocity:" + userId + ":1h";
        long now = System.currentTimeMillis();

        redisTemplate.opsForZSet().add(oneMinKey, String.valueOf(now), now);
        redisTemplate.opsForZSet().add(oneHourKey, String.valueOf(now), now);


        redisTemplate.opsForZSet().removeRangeByScore(oneMinKey, 0, now - 60000);
        redisTemplate.opsForZSet().removeRangeByScore(oneHourKey, 0, now - 3600000);

        Long oneMinCount = redisTemplate.opsForZSet().zCard(oneMinKey);
        Long oneHourCount = redisTemplate.opsForZSet().zCard(oneHourKey);

        return (oneMinCount != null && oneMinCount > 3) || (oneHourCount != null && oneHourCount > 10);
    }


    private boolean isAmountSpikeDetected(Long userId, BigDecimal currentAmount) {
        String statsKey = "fraud:stats:" + userId;


        String cachedAverage = (String) redisTemplate.opsForHash().get(statsKey, "daily_average");
        BigDecimal effectiveAverage;

        if (cachedAverage == null || new BigDecimal(cachedAverage).compareTo(BigDecimal.ZERO) == 0) {
            effectiveAverage = DEFAULT_GLOBAL_BASELINE;
            log.info("[COLD_START] New user detected. Applying default baseline average: {} BDT", effectiveAverage);
        } else {
            effectiveAverage = new BigDecimal(cachedAverage);
            log.info("[PROFILE_FOUND] User profile average found: {} BDT", effectiveAverage);
        }

        BigDecimal maxAllowedAmount = effectiveAverage.multiply(MAX_SPIKE_MULTIPLIER);

        return currentAmount.compareTo(maxAllowedAmount) > 0;
    }



    private boolean isImpossibleTravelDetected(Long userId, String currentIp) {
        String locationKey = "fraud:location:" + userId;
        long currentTime = System.currentTimeMillis();


        double currentLat = currentIp.equals("192.168.10.5") ? 23.8103 : 22.3569;
        double currentLon = currentIp.equals("192.168.10.5") ? 90.4125 : 91.7832;

        Map<Object, Object> lastLocation = redisTemplate.opsForHash().entries(locationKey);

        if (!lastLocation.isEmpty()) {
            double lastLat = Double.parseDouble((String) lastLocation.get("lat"));
            double lastLon = Double.parseDouble((String) lastLocation.get("lon"));
            long lastTime = Long.parseLong((String) lastLocation.get("timestamp"));


            double earthRadius = 6371;
            double dLat = Math.toRadians(currentLat - lastLat);
            double dLon = Math.toRadians(currentLon - lastLon);
            double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                    Math.cos(Math.toRadians(lastLat)) * Math.cos(Math.toRadians(currentLat)) *
                            Math.sin(dLon / 2) * Math.sin(dLon / 2);
            double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
            double distance = earthRadius * c;

            long timeDiffSeconds = Math.abs(currentTime - lastTime) / 1000;

            if (timeDiffSeconds > 0 && timeDiffSeconds < 3600) {
                double speedKmh = (distance / timeDiffSeconds) * 3600;
                if (speedKmh > 900) {
                    log.error("[IMPOSSIBLE_TRAVEL] Calculated speed: {} km/h between requests!", speedKmh);
                    return true;
                }
            }
        }


        redisTemplate.opsForHash().putAll(locationKey, Map.of(
                "lat", String.valueOf(currentLat),
                "lon", String.valueOf(currentLon),
                "timestamp", String.valueOf(currentTime)
        ));

        return false;
    }


    private void updateUserMovingAverage(Long userId, BigDecimal currentAmount) {
        String statsKey = "fraud:stats:" + userId;

        String cachedAverage = (String) redisTemplate.opsForHash().get(statsKey, "daily_average");
        String cachedCount = (String) redisTemplate.opsForHash().get(statsKey, "tx_count");

        BigDecimal oldAverage = (cachedAverage == null) ? DEFAULT_GLOBAL_BASELINE : new BigDecimal(cachedAverage);
        long count = (cachedCount == null) ? 0 : Long.parseLong(cachedCount);

        count++;


        BigDecimal difference = currentAmount.subtract(oldAverage);
        BigDecimal adjustment = difference.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);
        BigDecimal newAverage = oldAverage.add(adjustment);


        redisTemplate.opsForHash().putAll(statsKey, Map.of(
                "daily_average", newAverage.toString(),
                "tx_count", String.valueOf(count)
        ));

        log.info("[STATS_UPDATED] User: {} updated. Total Tx Count: {}, New Dynamic Average: {} BDT",
                userId, count, newAverage);
    }
}