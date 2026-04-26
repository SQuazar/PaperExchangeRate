# PaperExchangeRate - Курсы валют на вашем сервере!

PaperExchangeRate использует публичный API для получения информации о курсе валют.
Вся работа с REST API происходит асинхронно и кэшируется для ускорения работы плагина.

## Основные возможности

- Получение информации о доступных курсах
- Получение курса валют
- Конвертация валют
- Асихнронная работа с REST API
- Кэширование данных
- Интеграция с PlaceholderAPI

## Конфигурация плагина

```yaml
# Только MiniMessage формат
messages:
  currency-not-found: "<red>Валюта <currency> не найдена"
  format: "<yellow><date>: <aqua><base_value><base_currency_symbol></aqua> <yellow>=</yellow> <aqua><target_value><target_currency_symbol></aqua>"
  invalid-number: "<red>Неверный формат ввода"
  invalid-number-2: "<red>Число не должно быть меньше или равным нулю"
  error: "<red>Что-то пошло не так. Обратитесь к администратору"
```

> [!WARNING]
> Конфигурация принимает только [MiniMessage Format](https://docs.papermc.io/adventure/minimessage/)!

## Команды и права

| Команда                             | Право            | Описание                                  |
|-------------------------------------|------------------|-------------------------------------------|
| `/rate <base> <target>`             | exchange.rate    | Получить курс валюты относительно искомой |
| `/convert <base> <target> <amount>` | exchange.convert | Ковертация валют                          |
| `/paperexchangerate`                | exchange.reload  | Перезагрузить конфигурацию                |

## PlaceholderAPI

Плагин имеет небольшой набор плейсхолдеров для отображения игрокам

| Плейсхолдер                                 | Описание               |
|---------------------------------------------|------------------------|
| `exchange_symbol_<currency>`                | Получить символ валюты |
| `exchange_rate_<base>_<target>`             | Получить курс валют    |
| `exchange_convert_<base>_<target>_<amount>` | Конвертировать валюты  |

## Сборка

Для сборки плагина необходимо воспользоваться следующей командой:
```shell
./gradlew clean build
```

Итоговый `jar` файл будет лежать в директории `build/libs/`

## Баги и обратная связь

Для информировании об ошибках или недочетах, используйте [GitHub Issues](https://github.com/SQuazar/PaperExchangeRate/issues)

## Контакты

- [Telegram](https://t.me/squazar)