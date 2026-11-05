# Инструкции по проекту bitrix24-bots-java

## Использование MCP-серверов (опционально, если подключены)

1. **Документация Битрикс24 (`b24-dev-mcp`) — если подключен:**
   - Если MCP-сервер `b24-dev-mcp` доступен в текущем окружении, при любых вопросах, написании документации, добавлении или изменении DTO, методов API (`imbot.v2.*`, `im.v2.*`), событий или ошибок Битрикс24 в первую очередь обращайся к его инструментам (`bitrix-search`, `bitrix-method-details`, `bitrix-article-details`, `bitrix-event-details`, `bitrix-app-development-doc-details`).
   - Если `b24-dev-mcp` не подключен, опирайся на существующий код проекта и официальную документацию `apidocs.bitrix24.ru`.

2. **Интеграция с IDE (`jetbrains_air_ide`) — если подключен:**
   - Если MCP-сервер `jetbrains_air_ide` доступен в текущем окружении, используй его инструменты (`execute_tool`) при необходимости для задач IDE (анализ модулей, инспекции, поиск использований и запуск конфигураций).
   - Если `jetbrains_air_ide` не подключен, используй стандартные файловые и терминальные инструменты.

## Стек и архитектурные соглашения проекта

- **Язык и сборка:** Java 17+, Maven Wrapper (`./mvnw test`, `./mvnw clean package`).
- **Сериализация JSON:** Jackson 3 (`tools.jackson.databind.*`) совместно с аннотациями `com.fasterxml.jackson.annotation.*` (`@JsonInclude`, `@JsonIgnoreProperties`, `@JsonAlias`) и Lombok (`@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`, `@Jacksonized`).
- **Структура модулей:**
  - `bitrix24-bots-api` — низкоуровневый и типизированный клиент REST API (`BitrixApiClient`), HTTP-клиент (`OkHttpBitrixHttpClient`), модели запросов/ответов и единая модель событий `Event`. Не должен зависеть от `bitrix24-bots-sdk`.
  - `bitrix24-bots-sdk` — высокоуровневый SDK: `BitrixBot`, `BotEventDispatcher`, `PollingBotRunner`, `WebhookHandler`, `EmbeddedWebhookServer`.
