package net.nullpointer.exchange.service.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import net.nullpointer.exchange.data.ExchangeRate;
import net.nullpointer.exchange.service.ExchangeService;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class ExchangeServiceImpl implements ExchangeService {
    private static final String CURRENCIES_ENDPOINT =
            "https://latest.currency-api.pages.dev/v1/currencies.min.json";
    private static final String EXCHANGE_ENDPOINT =
            "https://latest.currency-api.pages.dev/v1/currencies/%s.min.json";

    private final JavaPlugin plugin;
    private final HttpClient client;
    private final Cache<String, ExchangeRate> exchangeCache = CacheBuilder.newBuilder()
            .expireAfterWrite(Duration.ofMinutes(30))
            .build();
    private final Cache<String, String> currenciesCache = CacheBuilder.newBuilder()
            .expireAfterWrite(Duration.ofHours(12))
            .build();

    public ExchangeServiceImpl(JavaPlugin plugin, HttpClient client) {
        this.plugin = plugin;
        this.client = client;
    }

    @Override
    public CompletableFuture<@Nullable ExchangeRate> getExchangeRate(String base) {
        ExchangeRate rate = exchangeCache.getIfPresent(base.toLowerCase());
        if (rate != null) return CompletableFuture.completedFuture(rate);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(EXCHANGE_ENDPOINT.formatted(base.toLowerCase())))
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();
        return client.sendAsync(request, HttpResponse.BodyHandlers.ofInputStream())
                .thenApply(res -> {
                    if (res.statusCode() == 404) return null;
                    if (res.statusCode() != 200) {
                        plugin.getLogger().warning("Cannot fetch exchange rate from API");
                        return null;
                    }
                    try (JsonReader reader = new JsonReader(new InputStreamReader(res.body()))) {
                        JsonObject object = JsonParser.parseReader(reader).getAsJsonObject();
                        LocalDate date = LocalDate.parse(object.get("date").getAsString());
                        Map<String, BigDecimal> rates = object.get(base.toLowerCase()).getAsJsonObject().entrySet()
                                .stream()
                                .collect(Collectors.toMap(Map.Entry::getKey, e ->
                                        e.getValue().getAsBigDecimal()));
                        ExchangeRate exchangeRate = new ExchangeRate(base.toLowerCase(), date, rates);
                        exchangeCache.put(base.toLowerCase(), exchangeRate);
                        return exchangeRate;
                    } catch (IOException e) {
                        plugin.getLogger().log(Level.WARNING, "Cannot read exchange rates", e);
                        return null;
                    }
                }).exceptionally(e -> {
                    plugin.getLogger().log(Level.SEVERE, "Cannot execute API request for fetch exchange rates!", e);
                    return null;
                });
    }

    @Override
    public CompletableFuture<@Nullable Collection<String>> getCurrencies() {
        if (!currenciesCache.asMap().isEmpty())
            return CompletableFuture.completedFuture(currenciesCache.asMap().values());
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(CURRENCIES_ENDPOINT))
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();
        return client.sendAsync(request, HttpResponse.BodyHandlers.ofInputStream())
                .thenApply(res -> {
                    if (res.statusCode() != 200) {
                        plugin.getLogger().warning("Something is wrong");
                        return null;
                    }
                    try (JsonReader reader = new JsonReader(new InputStreamReader(res.body()))) {
                        JsonObject object = JsonParser.parseReader(reader).getAsJsonObject();
                        Map<String, String> map = object.entrySet().stream()
                                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getAsString()));
                        currenciesCache.putAll(map);
                        return map.values();
                    } catch (IOException e) {
                        plugin.getLogger().log(Level.WARNING, "Cannot read currencies", e);
                        return null;
                    }
                }).exceptionally(e -> {
                    plugin.getLogger().log(Level.SEVERE, "Cannot execute API request", e);
                    return null;
                });
    }

    @Override
    public @Nullable ExchangeRate getCachedExchangeRate(String base) {
        return exchangeCache.getIfPresent(base.toLowerCase());
    }

    @Override
    public Collection<String> getCachedCurrencies() {
        return currenciesCache.asMap().keySet();
    }
}
