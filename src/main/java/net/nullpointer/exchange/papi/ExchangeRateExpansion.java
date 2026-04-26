package net.nullpointer.exchange.papi;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.nullpointer.exchange.data.ExchangeRate;
import net.nullpointer.exchange.service.ExchangeService;
import net.nullpointer.exchange.util.CurrencyUtil;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ExchangeRateExpansion extends PlaceholderExpansion {
    private final ExchangeService exchangeService;
    private final Map<String, Boolean> loadingMap = new ConcurrentHashMap<>();

    public ExchangeRateExpansion(ExchangeService exchangeService) {
        this.exchangeService = exchangeService;
        register();
    }

    @Override
    public @NotNull String getIdentifier() {
        return "exchange";
    }

    @Override
    public @NotNull String getAuthor() {
        return "NullPointer";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0.0";
    }

    @Override
    public @NotNull List<String> getPlaceholders() {
        return List.of(
                "symbol_<currency>",
                "rate_<base>_<target>",
                "convert_<base>_<target>_<amount>"
        );
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
        String[] args = params.toLowerCase().split("_");
        if (args.length < 2) return null;
        String type = args[0];
        String base = args[1];

        if (type.equals("symbol"))
            return CurrencyUtil.getSymbol(base, base.toUpperCase());

        if (args.length < 3) return null;

        String target = args[2];

        ExchangeRate rate = getOrLoad(base);
        if (rate == null) return "...";

        ExchangeRate.Rate targetRate = rate.getRate(target);
        if (targetRate == null) return null;

        if (type.equals("rate")) {
            int halfUp = -1;
            if (args.length == 4) {
                BigDecimal safe = getSafe(args[3]);
                if (safe != null) halfUp = safe.intValue();
            }
            return format(targetRate.value(), halfUp);
        }
        if (type.equals("convert")) {
            if (args.length != 4) return null;

            BigDecimal amount;
            try {
                amount = new BigDecimal(args[3]);
            } catch (NumberFormatException e) {
                return null;
            }

            return format(targetRate.value()
                    .multiply(amount)
                    .setScale(2, RoundingMode.HALF_UP), -1);
        }
        return null;
    }

    private @Nullable BigDecimal getSafe(String s) {
        try {
            return new BigDecimal(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private @Nullable ExchangeRate getOrLoad(String base) {
        ExchangeRate rate = exchangeService.getCachedExchangeRate(base);
        if (rate != null) return rate;

        if (loadingMap.putIfAbsent(base, true) == null) {
            exchangeService.getExchangeRate(base)
                    .whenComplete((r, e) -> loadingMap.remove(base));
        }

        return null;
    }

    private String format(BigDecimal value, int halfUp) {
        value = value.stripTrailingZeros();
        if (halfUp < 0) return value.toPlainString();
        return value.setScale(halfUp, RoundingMode.HALF_UP).toPlainString();
    }
}
