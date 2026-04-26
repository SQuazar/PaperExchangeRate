package net.nullpointer.exchange.service;

import net.nullpointer.exchange.data.ExchangeRate;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public interface ExchangeService {
    CompletableFuture<@Nullable ExchangeRate> getExchangeRate(String base);

    CompletableFuture<@Nullable Collection<String>> getCurrencies();

    @Nullable ExchangeRate getCachedExchangeRate(String base);

    Collection<String> getCachedCurrencies();
}
