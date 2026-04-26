package net.nullpointer.exchange.command;

import net.nullpointer.exchange.service.ExchangeService;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

public class CurrencyTabCompleter implements TabCompleter {
    private final ExchangeService exchangeService;
    private volatile boolean currenciesLoading = false;

    public CurrencyTabCompleter(ExchangeService exchangeService) {
        this.exchangeService = exchangeService;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, @NotNull String[] args) {
        if (args.length == 0) return null;
        Collection<String> cached = exchangeService.getCachedCurrencies();
        if (cached.isEmpty()) {
            loadCurrencies();
            return null;
        }
        String prefix = args[0].toLowerCase();
        if (args.length == 2) prefix = args[1].toLowerCase();

        final String fPrefix = prefix;

        return cached.stream()
                .filter(c -> c.startsWith(fPrefix))
                .limit(50)
                .toList();
    }

    private void loadCurrencies() {
        if (currenciesLoading) return;

        currenciesLoading = true;
        exchangeService.getCurrencies().whenComplete((c, e) -> currenciesLoading = false);
    }
}
