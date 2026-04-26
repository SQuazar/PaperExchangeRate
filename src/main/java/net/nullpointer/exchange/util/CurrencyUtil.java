package net.nullpointer.exchange.util;

import java.util.Currency;

public final class CurrencyUtil {
    private CurrencyUtil() {}

    public static String getSymbol(String code, String fallback) {
        try {
            Currency currency = Currency.getInstance(code.toUpperCase());
            if (currency == null) return fallback;
            return currency.getSymbol();
        } catch (IllegalArgumentException e) {
            return fallback;
        }
    }
}
