package net.nullpointer.exchange.command;

import net.nullpointer.exchange.config.ExchangeConfig;
import net.nullpointer.exchange.service.ExchangeService;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class ExchangeRateCommand extends AbstractExchangeCommand {
    public ExchangeRateCommand(ExchangeService exchangeService, ExchangeConfig config) {
        super(exchangeService, config);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, @NotNull String[] args) {
        if (!sender.hasPermission("exchange.get")) {
            sender.sendMessage(Bukkit.getPermissionMessage());
            return true;
        }
        if (args.length != 2) {
            return false;
        }
        handle(sender, args[0], args[1], (rate, target) ->
                sendExchange(
                        sender,
                        rate,
                        target,
                        BigDecimal.ONE,
                        target.value().setScale(2, RoundingMode.HALF_UP)
                )
        );
        return true;
    }
}
