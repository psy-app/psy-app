package com.psy;

import static org.assertj.core.api.Assertions.assertThat;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.ScreenshotType;
import com.microsoft.playwright.options.WaitForSelectorState;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.List;
import java.util.function.BooleanSupplier;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PsyHappyPathE2ETests {

    private static final String BASE_URL_PROPERTY = "e2e.base-url";
    private static final String BASE_URL_ENV = "E2E_BASE_URL";
    private static final String SELENIUM_CDP_URL_PROPERTY = "e2e.selenium-cdp-url";
    private static final String SELENIUM_CDP_URL_ENV = "E2E_SELENIUM_CDP_URL";
    private static final String ARTIFACTS_DIR_PROPERTY = "e2e.artifacts-dir";
    private static final String ARTIFACTS_DIR_ENV = "E2E_ARTIFACTS_DIR";
    private static final String PAGE_READY_TIMEOUT_MILLIS_PROPERTY = "e2e.page-ready-timeout-millis";
    private static final String PAGE_READY_TIMEOUT_MILLIS_ENV = "E2E_PAGE_READY_TIMEOUT_MILLIS";
    
    // Адаптовано під ваш заголовок у schedule.html
    private static final String SCHEDULE_PAGE_TITLE = "Розклад психологічних сеансів"; 
    private static final String RENDER_HOST_SUFFIX = ".onrender.com";
    private static final int WAIT_TIMEOUT_MILLIS = 15000;
    private static final int POLL_INTERVAL_MILLIS = 1200;
    private static final int DEFAULT_PAGE_READY_TIMEOUT_MILLIS = 180000;
    private static final int DEFAULT_TIMEOUT_MILLIS = 30000;
    private static final int RENDER_TIMEOUT_MILLIS = 180000;
    private static final int VIEWPORT_WIDTH = 1440;
    private static final int VIEWPORT_HEIGHT = 1200;
    private static final int VIDEO_WIDTH = 1280;
    private static final int VIDEO_HEIGHT = 720;

    private String baseUrl;
    private String seleniumCdpUrl;
    private int pageReadyTimeoutMillis;
    private Path artifactsDir;
    private Path videosDir;
    private Path screenshotsDir;
    private Playwright playwright;
    private Browser browser;
    private BrowserContext browserContext;
    private Page page;
    private String createdClientName;

    @BeforeEach
    void setUp() {
        baseUrl = resolveBaseUrl();
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalStateException("Set e2e.base-url or E2E_BASE_URL to run E2E UI tests.");
        }
        seleniumCdpUrl = resolveOptionalValue(SELENIUM_CDP_URL_PROPERTY, SELENIUM_CDP_URL_ENV);
        pageReadyTimeoutMillis = resolvePageReadyTimeoutMillis();
        artifactsDir = resolveArtifactsDir();
        videosDir = artifactsDir.resolve("videos");
        screenshotsDir = artifactsDir.resolve("screenshots");
        createDirectory(videosDir);
        createDirectory(screenshotsDir);

        playwright = Playwright.create();
        browser = createBrowser();
        
        browserContext = browser.newContext(new Browser.NewContextOptions()
            .setRecordVideoDir(videosDir)
            .setRecordVideoSize(VIDEO_WIDTH, VIDEO_HEIGHT)
            .setViewportSize(VIEWPORT_WIDTH, VIEWPORT_HEIGHT));
        page = browserContext.newPage();
        page.setDefaultTimeout(resolvePlaywrightTimeoutMillis());
        page.setDefaultNavigationTimeout(pageReadyTimeoutMillis);
    }

    @AfterEach
    void tearDown() {
        if (page != null && createdClientName != null) {
            try {
                deleteScheduleRow(createdClientName);
            } catch (Exception exception) {
                // Best effort cleanup
            } finally {
                createdClientName = null;
            }
        }
        if (browserContext != null) browserContext.close();
        if (browser != null) browser.close();
        if (playwright != null) playwright.close();
    }

    @Test
    void scheduleHappyPathCreatesAndDeletesEntry() {
        runWithDiagnostics("schedule-happy-path-creates-and-deletes-entry", () -> {
            String uniqueSuffix = String.valueOf(Instant.now().toEpochMilli());
            // Використовуємо clientName як унікальний ідентифікатор для пошуку в таблиці
            String uniqueClientName = "E2E Клієнт " + uniqueSuffix;
            createdClientName = uniqueClientName;

            navigateTo(baseUrl + "/add");
            waitForAddPageForm();
            assertThat(page.locator("form").count()).isEqualTo(1);

            // Заповнюємо поля відповідно до PSession.java та add.html
            setInputValue("psychologistName", "Д-р Тестовий");
            setInputValue("clientName", uniqueClientName);
            setInputValue("sessionDate", "2024-12-25");
            setInputValue("sessionPackage", "Поглиблений");
            setInputValue("sessionTopic", "E2E Тестування");
            setInputValue("MeetingPlatform", "Zoom");
            setInputValue("subscription", "Преміум");
            setInputValue("psyExperience", "10 років");
            setInputValue("clinicAddress", "Київ, вул. Тестова 1");
            setInputValue("clinicPhone", "+380000000000");

            page.locator("button[type='submit']").click();
            page.waitForURL(baseUrl + "/");
            waitForSchedulePageHeader();

            // Перевірка появи запису в таблиці schedule.html
            Locator createdRow = waitForRowContaining(uniqueClientName,
                "Запис для клієнта не з'явився на головній сторінці.");
            assertThat(createdRow.textContent())
                .contains(uniqueClientName)
                .contains("E2E Тестування");

            // Видалення
            deleteScheduleRow(uniqueClientName);
            createdClientName = null;
        });
    }

    private void deleteScheduleRow(String textToFind) {
        navigateTo(baseUrl + "/");
        waitForSchedulePageHeader();

        Locator rowToDelete = findRowContaining(textToFind);
        if (rowToDelete == null) return;

        // У вашому schedule.html кнопка "Видалити" знаходиться всередині форми
        rowToDelete.locator("button.btn-danger").click();
        
        page.waitForURL(baseUrl + "/");
        waitForSchedulePageHeader();
        waitFor(() -> findRowContaining(textToFind) == null,
            "Запис все ще видимий після видалення.");

        assertThat(page.locator("body").textContent()).doesNotContain(textToFind);
    }

    private Locator waitForRowContaining(String text, String failureMessage) {
        waitFor(() -> findRowContaining(text) != null, failureMessage);
        return findRowContaining(text);
    }

    private Locator findRowContaining(String text) {
        Locator rows = page.locator("tbody tr");
        int count = rows.count();
        for (int index = 0; index < count; index++) {
            Locator row = rows.nth(index);
            String rowText = row.textContent();
            if (rowText != null && rowText.contains(text)) {
                return row;
            }
        }
        return null;
    }

    private void setInputValue(String fieldName, String value) {
        // Playwright знайде поле за атрибутом name="psychologistName" тощо
        page.locator("[name='" + fieldName + "']").fill(value);
    }

    private void waitForSchedulePageHeader() {
        page.locator("h1")
            .filter(new Locator.FilterOptions().setHasText(SCHEDULE_PAGE_TITLE))
            .first()
            .waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE)
                .setTimeout((double) pageReadyTimeoutMillis));
    }

    private void waitForAddPageForm() {
        page.locator("form")
            .first()
            .waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE)
                .setTimeout((double) pageReadyTimeoutMillis));
    }

    private void waitFor(BooleanSupplier condition, String failureMessage) {
        long deadline = System.currentTimeMillis() + WAIT_TIMEOUT_MILLIS;
        while (System.currentTimeMillis() < deadline) {
            if (condition.getAsBoolean()) return;
            page.waitForTimeout(POLL_INTERVAL_MILLIS);
        }
        throw new AssertionError(failureMessage);
    }

    private void navigateTo(String url) {
        long deadline = System.currentTimeMillis() + pageReadyTimeoutMillis;
        RuntimeException lastFailure = null;
        while (System.currentTimeMillis() < deadline) {
            try {
                page.navigate(url);
                return;
            } catch (RuntimeException exception) {
                lastFailure = exception;
                page.waitForTimeout(POLL_INTERVAL_MILLIS);
            }
        }
        throw new AssertionError("Додаток за адресою " + url + " не став доступним.", lastFailure);
    }

    private String resolveBaseUrl() {
        String configuredUrl = resolveOptionalValue(BASE_URL_PROPERTY, BASE_URL_ENV);
        if (configuredUrl == null || configuredUrl.isBlank()) return null;
        return normalizeBaseUrl(configuredUrl);
    }

    private String normalizeBaseUrl(String configuredUrl) {
        String normalizedUrl = configuredUrl.endsWith("/") ? configuredUrl.substring(0, configuredUrl.length() - 1) : configuredUrl;
        URI uri = URI.create(normalizedUrl);
        if (uri.getScheme() == null || uri.getHost() == null) {
            throw new IllegalStateException("E2E base URL must be absolute (e.g., http://localhost:8081)");
        }
        return normalizedUrl;
    }

    private String resolveOptionalValue(String propertyName, String envName) {
        String val = System.getProperty(propertyName);
        return (val == null || val.isBlank()) ? System.getenv(envName) : val;
    }

    private Browser createBrowser() {
        if (seleniumCdpUrl != null && !seleniumCdpUrl.isBlank()) {
            return playwright.chromium().connectOverCDP(seleniumCdpUrl);
        }
        return playwright.chromium().launch(new BrowserType.LaunchOptions()
            .setHeadless(true)
            .setArgs(List.of("--disable-dev-shm-usage", "--no-sandbox")));
    }

    private Path resolveArtifactsDir() {
        String dir = resolveOptionalValue(ARTIFACTS_DIR_PROPERTY, ARTIFACTS_DIR_ENV);
        if (dir == null || dir.isBlank()) dir = "target/e2e-artifacts";
        return Paths.get(dir).toAbsolutePath().normalize();
    }

    private int resolvePageReadyTimeoutMillis() {
        String timeout = resolveOptionalValue(PAGE_READY_TIMEOUT_MILLIS_PROPERTY, PAGE_READY_TIMEOUT_MILLIS_ENV);
        if (timeout == null || timeout.isBlank()) {
            return isRenderBaseUrl() ? RENDER_TIMEOUT_MILLIS : DEFAULT_PAGE_READY_TIMEOUT_MILLIS;
        }
        return Integer.parseInt(timeout);
    }

    private double resolvePlaywrightTimeoutMillis() {
        return isRenderBaseUrl() ? RENDER_TIMEOUT_MILLIS : DEFAULT_TIMEOUT_MILLIS;
    }

    private boolean isRenderBaseUrl() {
        if (baseUrl == null) return false;
        return baseUrl.contains(RENDER_HOST_SUFFIX);
    }

    private void createDirectory(Path directory) {
        try {
            Files.createDirectories(directory);
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to create artifacts directory " + directory, exception);
        }
    }

    private void runWithDiagnostics(String testName, Runnable testBody) {
        try {
            testBody.run();
        } catch (Throwable throwable) {
            captureFailureScreenshot(testName);
            rethrowUnchecked(throwable);
        }
    }

    private void captureFailureScreenshot(String testName) {
        if (page == null) return;
        Path path = screenshotsDir.resolve(sanitizeFileName(testName) + "-" + Instant.now().toEpochMilli() + ".png");
        page.screenshot(new Page.ScreenshotOptions().setPath(path).setFullPage(true).setType(ScreenshotType.PNG));
    }

    private String sanitizeFileName(String value) {
        return value.replaceAll("[^a-zA-Z0-9-_]+", "-");
    }

    private void rethrowUnchecked(Throwable throwable) {
        if (throwable instanceof RuntimeException) throw (RuntimeException) throwable;
        if (throwable instanceof Error) throw (Error) throwable;
        throw new IllegalStateException(throwable);
    }
}