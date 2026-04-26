package net.nullpointer.exchange.data;

import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

public record ExchangeRate(String base, LocalDate date, Map<String, BigDecimal> rates) {
    public @Nullable Rate getRate(String code) {
        code = code.toLowerCase();
        BigDecimal rate = rates.get(code);
        if (rate == null) return null;
        return new Rate(
                code,
                rate
        );
    }

    public record Rate(String code, BigDecimal value) {}
}
