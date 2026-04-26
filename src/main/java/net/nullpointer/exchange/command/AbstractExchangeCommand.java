package net.nullpointer.exchange.command;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.nullpointer.exchange.config.ExchangeConfig;
import net.nullpointer.exchange.data.ExchangeRate;
import net.nullpointer.exchange.service.ExchangeService;
import net.nullpointer.exchange.util.CurrencyUtil;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.math.BigDecimal;
import java.util.function.BiConsumer;

public abstract class AbstractExchangeCommand implements CommandExecutor {
    static final MiniMessage MM = MiniMessage.miniMessage();

    private final ExchangeService exchangeService;
    protected final ExchangeConfig config;

    public AbstractExchangeCommand(ExchangeService exchangeService, ExchangeConfig config) {
        this.exchangeService = exchangeService;
        this.config = config;
    }

    protected void notFound(CommandSender sender, String currency) {
        sender.sendMessage(MM.deserialize(
                config.getMessages().getNotFound(),
                Placeholder.unparsed("currency", currency)
        ));
    }

    protected String format(BigDecimal value) {
        return value.stripTrailingZeros().toPlainString();
    }

    protected void sendExchange(
            CommandSender sender,
            ExchangeRate rate,
            ExchangeRate.Rate target,
            BigDecimal baseValue,
            BigDecimal targetValue
    ) {
        sender.sendMessage(MM.deserialize(
                config.getMessages().getFormat(),
                Placeholder.unparsed("date", rate.date().toString()),
                Placeholder.unparsed("base_currency", rate.base()),
                Placeholder.unparsed("base_value", format(baseValue)),
                Placeholder.unparsed("base_currency_symbol", CurrencyUtil.getSymbol(rate.base(), " " + rate.base().toUpperCase())),
                Placeholder.unparsed("target_currency", target.code()),
                Placeholder.unparsed("target_value", format(targetValue)),
                Placeholder.unparsed("target_currency_symbol", CurrencyUtil.getSymbol(target.code(), " " + target.code().toUpperCase()))
        ));
    }

    protected void handle(
            CommandSender sender,
            String base,
            String target,
            BiConsumer<ExchangeRate, ExchangeRate.Rate> action
    ) {
        exchangeService.getExchangeRate(base.toLowerCase()).thenAccept(rate -> {
            if (rate == null) {
                notFound(sender, base);
                return;
            }

            ExchangeRate.Rate targetRate = rate.getRate(target.toLowerCase());
            if (targetRate == null) {
                notFound(sender, target);
                return;
            }

            action.accept(rate, targetRate);
        }).exceptionally(e -> {
            sender.sendMessage(MM.deserialize(
                    config.getMessages().getError()
            ));
            return null;
        });
    }
}
