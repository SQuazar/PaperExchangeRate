package net.nullpointer.exchange.config;

import net.nullpointer.exchange.PaperExchangeRate;
import org.bukkit.configuration.ConfigurationSection;

public final class ExchangeConfig {
    private final PaperExchangeRate plugin;

    private final Messages messages = new Messages();

    public ExchangeConfig(PaperExchangeRate plugin) {
        this.plugin = plugin;
    }

    public Messages getMessages() {
        return messages;
    }

    public void load() {
        messages.load(plugin.getConfig().getConfigurationSection("messages"));
    }

    public static final class Messages {
        private String notFound;
        private String format;
        private String invalidNumber;
        private String invalidNumber2;
        private String error;

        Messages() {

        }

        public String getNotFound() {
            return notFound;
        }

        public String getFormat() {
            return format;
        }

        public String getInvalidNumber() {
            return invalidNumber;
        }

        public String getInvalidNumber2() {
            return invalidNumber2;
        }

        public String getError() {
            return error;
        }

        void load(ConfigurationSection section) {
            if (section == null) return;
            this.notFound = section.getString("currency-not-found", "<red>Currency <currency> not found!");
            this.format = section.getString("format", "<date>: <base_value> <base_currency_symbol> = <target_value> <target_currency_symbol>");
            this.invalidNumber = section.getString("invalid-number", "<red>Invalid number input");
            this.invalidNumber2 = section.getString("invalid-number-2", "<red>Number cannot be less or equal zero");
            this.error = section.getString("error", "<red>Something is wrong...");
        }
    }
}
