package com.psy;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class PsyApplicationTest {
    private final OutputStreamState outputStreamState = new OutputStreamState();

    @AfterEach
    void restoreSystemStreams() {
        outputStreamState.restore();
    }

    @Test
    void viewAllSchedulesPrintsNotFoundForEmptyRepository() throws Exception {
        // Створюємо мок репозиторію та імітуємо порожній список
        PsyRepository repository = mock(PsyRepository.class);
        when(repository.findAll()).thenReturn(Collections.emptyList());
        PsyApplication app = appWithRepository(repository);

        // Викликаємо приватний метод через рефлексію
        invokePrivate(app, "viewAllSchedules");

        // Перевіряємо повідомлення в консолі
        assertTrue(outputStreamState.value().contains("не знайдено"));
        verify(repository, times(1)).findAll();
    }

    @Test
    void viewAllSchedulesPrintsRowsWhenRepositoryHasData() throws Exception {
        PsyRepository repository = mock(PsyRepository.class);
        // Створюємо тестовий об'єкт сесії
        PSession session = new PSession(
            "Дмитро Сидоренко", "Олена Коваленко", "2024-05-20", 
            "Стандарт", "Консультація", "Zoom", "Так", 
            "5 років", "вул. Шевченка, 10", "+380501112233"
        );
        when(repository.findAll()).thenReturn(Collections.singletonList(session));
        PsyApplication app = appWithRepository(repository);

        invokePrivate(app, "viewAllSchedules");

        String output = outputStreamState.value();
        assertTrue(output.contains("Знайдено 1"));
        assertTrue(output.contains("PSession {"));
        verify(repository, times(1)).findAll();
    }

    @Test
    void dropAllSchedulesDeletesDataAndPrintsMessage() throws Exception {
        PsyRepository repository = mock(PsyRepository.class);
        PsyApplication app = appWithRepository(repository);

        invokePrivate(app, "dropAllSchedules");

        verify(repository, times(1)).deleteAll();
        assertTrue(outputStreamState.value().contains("видалено"));
    }

    // Допоміжний метод для впровадження мок-репозиторію в PsyApplication
    private static PsyApplication appWithRepository(PsyRepository repository) throws Exception {
        PsyApplication app = new PsyApplication();
        Field field = PsyApplication.class.getDeclaredField("psyRepository");
        field.setAccessible(true);
        field.set(app, repository);
        return app;
    }

    // Допоміжний метод для виклику приватних методів
    private static void invokePrivate(PsyApplication app, String methodName) throws Exception {
        Method method = PsyApplication.class.getDeclaredMethod(methodName);
        method.setAccessible(true);
        method.invoke(app);
    }

    // Клас для перехоплення System.out
    private static final class OutputStreamState {
    private final PrintStream original = System.out;
    private final ByteArrayOutputStream captured = new ByteArrayOutputStream();

    private OutputStreamState() {
        try {
            // Використовуємо рядок "UTF-8" замість об'єкта Charset для кращої сумісності
            System.setOut(new PrintStream(captured, true, "UTF-8"));
        } catch (java.io.UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    private String value() {
        try {
            return captured.toString("UTF-8");
        } catch (java.io.UnsupportedEncodingException e) {
            return captured.toString();
        }
    }

    private void restore() {
        System.setOut(original);
    }
}
}