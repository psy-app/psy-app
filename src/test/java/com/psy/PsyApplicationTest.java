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
        PsyRepository repository = mock(PsyRepository.class);
        when(repository.findAll()).thenReturn(Collections.emptyList());
        PsyApplication app = appWithRepository(repository);

        invokePrivate(app, "viewAllSchedules");

        assertTrue(outputStreamState.value().contains("не знайдено"));
        verify(repository, times(1)).findAll();
    }

    @Test
    void viewAllSchedulesPrintsRowsWhenRepositoryHasData() throws Exception {
        PsyRepository repository = mock(PsyRepository.class);
        // Створюємо тестову сесію згідно з вашим конструктором
        PSession session = new PSession(
            "д-р Антонюк", "Олег Гнатюк", "2024-09-14", 
            "12 сеансів", "тривога", "Zoom", "Basic", "9", 
            "м. Київ", "380449940101"
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

    // Допоміжні методи для роботи з рефлексією (щоб протестувати private методи)
    private static PsyApplication appWithRepository(PsyRepository repository) throws Exception {
        PsyApplication app = new PsyApplication();
        // Встановлюємо mock-репозиторій у private поле
        Field field = PsyApplication.class.getDeclaredField("psyRepository");
        field.setAccessible(true);
        field.set(app, repository);
        return app;
    }

    private static void invokePrivate(PsyApplication app, String methodName) throws Exception {
        Method method = PsyApplication.class.getDeclaredMethod(methodName);
        method.setAccessible(true);
        method.invoke(app);
    }

    // Клас для перехоплення консольного виводу
    private static final class OutputStreamState {
        private final PrintStream original = System.out;
        private final ByteArrayOutputStream captured = new ByteArrayOutputStream();

        OutputStreamState() {
            System.setOut(new PrintStream(captured));
        }

        String value() {
            return captured.toString();
        }

        void restore() {
            System.setOut(original);
        }
    }
}