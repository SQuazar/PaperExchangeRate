package net.nullpointer.exchange;

import net.nullpointer.exchange.command.ConvertCommand;
import net.nullpointer.exchange.command.CurrencyTabCompleter;
import net.nullpointer.exchange.command.ExchangeRateCommand;
import net.nullpointer.exchange.config.ExchangeConfig;
import net.nullpointer.exchange.papi.ExchangeRateExpansion;
import net.nullpointer.exchange.service.ExchangeService;
import net.nullpointer.exchange.service.impl.ExchangeServiceImpl;
import net.nullpointer.exchange.util.UpdateChecker;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.net.http.HttpClient;
import java.time.Duration;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class PaperExchangeRate extends JavaPlugin {
    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern(
            "dd.MM.yyyy HH:mm"
    ).withZone(ZoneId.systemDefault());

    private ExchangeConfig config;
    private ExecutorService executor;
    private ExchangeService exchangeService;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        config = new ExchangeConfig(this);
        config.load();

        setupService();
        registerCommands();

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null)
            new ExchangeRateExpansion(exchangeService);

        UpdateChecker.fetchUpdate(this, info -> {
            String version = info.tag().substring(1);
            if (!getDescription().getVersion().equals(version)) {
                getLogger().warning("New version for %s is available:".formatted(info.name()));
                getLogger().warning(info.downloadUrl());
                getLogger().warning("Published at: %s".formatted(FORMAT.format(info.publishedAt())));
                if (info.preRelease())
                    getLogger().warning("This is pre release version!");
            } else getLogger().info("You used latest version!");
        });
    }

    private void registerCommands() {
        TabCompleter tabCompleter = new CurrencyTabCompleter(exchangeService);
        getCommand("rate").setExecutor(new ExchangeRateCommand(exchangeService, config));
        getCommand("rate").setTabCompleter(tabCompleter);

        getCommand("convert").setExecutor(new ConvertCommand(exchangeService, config));
        getCommand("convert").setTabCompleter(tabCompleter);
    }

    private void setupService() {
        executor = Executors.newSingleThreadExecutor(r -> {
            Thread thread = new Thread(r, "ExchangeService-HTTP-API");
            thread.setDaemon(true);
            return thread;
        });
        exchangeService = new ExchangeServiceImpl(
                this,
                HttpClient.newBuilder()
                        .connectTimeout(Duration.ofSeconds(10))
                        .executor(Executors.newSingleThreadExecutor())
                        .build()
        );
        Bukkit.getServicesManager().register(
                ExchangeService.class,
                exchangeService,
                this,
                ServicePriority.Normal
        );
    }

    @Override
    public void onDisable() {
        Bukkit.getServicesManager().unregisterAll(this);
        if (executor != null) executor.shutdownNow();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        reloadConfig();
        config.load();
        sender.sendMessage("§aConfig reload successfully");
        return true;
    }
}
