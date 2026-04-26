package net.nullpointer.exchange.command;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.nullpointer.exchange.config.ExchangeConfig;
import net.nullpointer.exchange.service.ExchangeService;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class ConvertCommand extends AbstractExchangeCommand {
    public ConvertCommand(ExchangeService exchangeService, ExchangeConfig config) {
        super(exchangeService, config);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, @NotNull String[] args) {
        if (args.length != 3) {
            return false;
        }

        BigDecimal value;
        try {
            value = new BigDecimal(args[2]).setScale(2, RoundingMode.HALF_UP);
        } catch (NumberFormatException e) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize(
                    config.getMessages().getInvalidNumber()
            ));
            return true;
        }

        if (value.compareTo(BigDecimal.ZERO) <= 0) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize(
                    config.getMessages().getInvalidNumber2()
            ));
            return true;
        }

        handle(sender, args[0], args[1], (rate, target) ->
                sendExchange(
                        sender,
                        rate,
                        target,
                        value,
                        target.value().multiply(value).setScale(2, RoundingMode.HALF_UP)
                )
        );
        return true;
    }
}
