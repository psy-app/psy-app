# Приклад Java-програми ведення розкладу психологічних сеансів з використанням СКБД MongoDB

## Кроки для виконання

1. Завантажте і встановіть Java Development Kit 17 (або новішу версію) для Windows.
2. Завантажте Maven з https://dlcdn.apache.org/maven/maven-3/3.8.9/binaries/apache-maven-3.8.9-bin.zip і розпакуйте його на локальний комп'ютер.
3. В Windows в параметрах системи додайте системну змінну `MAVEN_HOME=<шлях до папки з Maven>`.
4. В Windows в параметрах системи додайте `;<шлях до папки з Maven>\bin` в системну змінну PATH.
5. Встановіть СКБД MongoDB Community Server — вибрати варіант установки Complete → "Run service as Network Service user".
6. Встановіть MongoDB Compass (GUI).
7. Створіть базу даних `psy-db` і колекцію в ній `psy-collection`.
8. Зберіть програму використовуючи команду `mvn clean install`.
9. Запустіть програму за допомогою команди `mvn spring-boot:run`.
10. Відкрийте веб-браузер і перейдіть за адресою `http://localhost:8081` для перегляду розкладу.

## Веб-інтерфейс

Після запуску програми веб-інтерфейс доступний у браузері за адресою `http://localhost:8081`.

Сторінки:
1. **Головна сторінка** (`http://localhost:8081`) — відображає розклад психологічних сеансів у вигляді таблиці з усіма колонками.
2. **Додати сеанс** (`http://localhost:8081/add`) — форма для додавання нового запису до розкладу.

## Docker

Локальне збирання образу Docker-контейнера:

```bash
docker build -f deploy/Dockerfile -t psy-app .
```

Локальний запуск образу Docker-контейнера:

```bash
docker run --rm -p 8081:8081 -e MONGO_URI=mongodb://host.docker.internal:27017/psy-db psy-app
```

Після запуску застосунок буде доступний за адресою `http://localhost:8081`.

Для збірки з основної гілки як тег образу Docker-контейнера використовується значення `version` з `pom.xml`, наприклад `0.3.0-snapshot`.
Тег `latest` публікується лише тоді, коли значення `version` у `pom.xml` не містить `SNAPSHOT`.

Перед тим, як завантажити образ, потрібно залогінитись в GitHub Package використовуючи свій GitHub-токен. Цей токен повинен мати дозвіл `read:packages`, бути створеним для того самого користувача GitHub, ім'я якого вказано в команді `docker login`, і цей користувач повинен мати принаймні read-доступ до пакета.

```cmd
set GITHUB_TOKEN=your-github-token
echo %GITHUB_TOKEN% | docker login ghcr.io -u your-github-username --password-stdin

docker pull ghcr.io/<your-github-username>/psy-app:0.3.0-snapshot
```

Запуск контейнера з образу Docker-контейнера, опублікованого у GitHub Packages:

```bash
docker run --rm -p 8081:8081 -e MONGO_URI=mongodb://host.docker.internal:27017/psy-db ghcr.io/<your-github-username>/psy-app:0.3.0-snapshot
```

## Налаштування `MONGO_URI`

За замовчуванням програма використовує адресу `mongodb://localhost:27017/psy-db` для підключення до локальної бази даних.

### Підключення до MongoDB Atlas

Для підключення MongoDB Atlas потрібно записати connection string кластера в змінну середовища `MONGO_URI`, наприклад:

```text
mongodb+srv://<db-username>:<db-password>@<cluster-url>/psy-db?retryWrites=true&w=majority
```

Порядок налаштування:
1. Створіть кластер у MongoDB Atlas (тариф Free).
2. Створіть користувача бази даних у розділі Database Access і збережіть пароль.
3. Скопіюйте connection string з кнопки Connect → Drivers і збережіть його.
4. Створіть базу даних `psy-db` і колекцію `psy-collection` в розділі Data Explorer.
5. Підставте в URI свої `username`, `password` і назву бази даних `psy-db` та збережіть у змінній середовища `MONGO_URI`.
6. Визначте зовнішню IP-адресу (`curl -s https://api.ipify.org && echo`) і додайте її в Network Access.

Приклад запуску з MongoDB Atlas.

Windows Command Prompt:

```cmd
set MONGO_URI=mongodb+srv://db-username:db-password@cluster0.abcde.mongodb.net/psy-db?retryWrites=true^&w=majority
mvn spring-boot:run
```

PowerShell:

```powershell
$env:MONGO_URI="mongodb+srv://db-username:db-password@cluster0.abcde.mongodb.net/psy-db?retryWrites=true&w=majority"
mvn spring-boot:run
```

Linux/macOS:

```bash
export MONGO_URI="mongodb+srv://db-username:db-password@cluster0.abcde.mongodb.net/psy-db?retryWrites=true&w=majority"
mvn spring-boot:run
```

Примітка: якщо пароль містить спеціальні символи, їх потрібно URL-кодувати в connection string. Наприклад, символ `@` потрібно замінити на `%40`.

## Запуск тестів

Локальні команди Maven:
1. Лише unit-тести:
`mvn test`
2. Лише integration-тести:
`mvn -DskipUnitTests=true verify`
3. Повна перевірка перед merge або release:
`mvn verify`

Примітка: integration-тести використовують **Testcontainers MongoDB**. Для локального запуску `mvn verify` має бути запущений Docker Desktop.

## Тестування

Поточний стан:
1. Unit-тести написані на **JUnit 5** і запускаються через **Surefire**.
2. Integration-тести запускаються через **Failsafe** і використовують **Testcontainers MongoDB**.
3. E2E UI-тести написані на **Playwright for Java** у класі `src/test/java/com/psy/PsyHappyPathE2ETests.java`.
4. Для покриття використовується **JaCoCo**.
5. У `pom.xml` налаштовано мінімальне покриття **75% line coverage** для unit-тестів і **100% line coverage** для інтеграційних тестів для `com.psy.PSession` та `com.psy.ScheduleController`.

Локальний запуск:
1. Тільки unit-тести:
`mvn test`
2. Тільки integration-тести:
`mvn -DskipUnitTests=true verify`
3. Тільки E2E UI-тести проти вже розгорнутого середовища за адресою https://your-app.onrender.com:
```
set DEBUG=pw:api,pw:browser
mvn -B -P e2e-ui "-Dcheckstyle.skip=true" "-DskipUnitTests=true" "-DskipIntegrationTests=true" "-De2e.base-url=https://your-app.onrender.com" clean verify
```
4. Повна перевірка:
`mvn clean verify`

Звіти:
1. Unit test reports: `target/surefire-reports/`
2. Integration and E2E test reports: `target/failsafe-reports/`
3. E2E media: `target/e2e-artifacts/videos/`, `target/e2e-artifacts/screenshots/`
4. Unit coverage: `target/site/jacoco/`
5. Integration coverage: `target/site/jacoco-integration-tests/`

## E2E UI тести

Конфігурація:
1. URL розгорнутого застосунку передається через JVM property `e2e.base-url` або змінну середовища `E2E_BASE_URL`.
2. Якщо URL не передано, тести завершуються з помилкою `IllegalStateException`.
3. Для запуску через Selenium Grid Playwright може під'єднатися до Chromium CDP endpoint, переданого через `e2e.selenium-cdp-url` або `E2E_SELENIUM_CDP_URL`.
4. Відео проходження тестів записується в `target/e2e-artifacts/videos/`.
5. При падінні тесту screenshot записується в `target/e2e-artifacts/screenshots/`.

Локальний запуск проти Render або іншого deployed URL в PowerShell:

```powershell
$env:E2E_BASE_URL="https://your-app.onrender.com"
mvn -B -P e2e-ui `
  '-Dcheckstyle.skip=true' `
  '-DskipUnitTests=true' `
  '-DskipIntegrationTests=true' `
  verify
```

## CI (GitHub Actions)

Основний workflow: `.github/workflows/ci.yml`

CI запускається автоматично для:
1. Pull request (`opened`, `synchronize`, `reopened`) у гілки `main` та `release/*`
2. Push у гілки `main` та `release/*`

Актуальна послідовність job:
1. `static-analysis`
2. `unit-tests`
3. `integration-tests`
4. `build`
5. `publish`
6. `publish-docker`
7. `deploy-render-pr` для pull request
8. `e2e-ui-tests` для pull request після успішного `deploy-render-pr`

Пов'язані workflow:
1. `.github/workflows/run-e2e-tests.yml` приймає `application_url`, піднімає `selenium/standalone-chromium:latest` як service container, викликає `.github/workflows/scripts/run-e2e-test.sh` і завантажує artifacts
2. `.github/workflows/scripts/run-e2e-test.sh` створює Selenium session, налаштовує CDP URL для Playwright і запускає `mvn -P e2e-ui` на GitHub runner
3. У CI workflow job `e2e-ui-tests` отримує URL з виводу job `deploy-render-pr`

GitHub Actions:
1. `run-e2e-tests` також підтримує ручний запуск через `workflow_dispatch` з обов'язковим параметром `application_url`
2. E2E workflow завантажує screenshots, відео, `failsafe`-звіти та логи як pipeline artifacts
