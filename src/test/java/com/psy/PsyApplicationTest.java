package com.psy;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class PsyApplicationTest {
    private final InputStreamState inputStreamState = new InputStreamState();
    private final OutputStreamState outputStreamState = new OutputStreamState();

    @AfterEach
    void restoreSystemStreams() {
        inputStreamState.restore();
        outputStreamState.restore();
    }

    @Test
    void addScheduleFromCsvReplacesExistingDataAndSavesParsedRows() throws Exception {
        PsyRepository repository = mock(PsyRepository.class);
        PsyApplication app = appWithRepository(repository);
        invokePrivate(app, "addScheduleFromCsv");
        verify(repository, times(1)).saveAll(anyList());
        assertTrue(outputStreamState.value().contains("CSV"));
    }

    @Test
    void addScheduleFromCsvPrintsFailureMessageWhenRepositoryThrows() throws Exception {
        PsyRepository repository = mock(PsyRepository.class);
        doThrow(new RuntimeException()).when(repository).saveAll(anyList());
        PsyApplication app = appWithRepository(repository);
        invokePrivate(app, "addScheduleFromCsv");
        assertTrue(outputStreamState.value().contains("Не вдалось"));
    }

    @Test
    void viewAllSchedulesPrintsNotFoundForEmptyRepository() throws Exception {
        PsyRepository repository = mock(PsyRepository.class);
        when(repository.findAll()).thenReturn(Collections.emptyList());
        PsyApplication app = appWithRepository(repository);
        invokePrivate(app, "viewAllSchedules");
        assertTrue(outputStreamState.value().contains("не знайдено"));
    }

    @Test
    void viewAllSchedulesPrintsRowsWhenRepositoryHasData() throws Exception {
        PsyRepository repository = mock(PsyRepository.class);
        PSession session = new PSession("1", "Cl", "Ps", "Pk", "Tp", "Pl", "Sb", "Ex", "Ad", "Dt");
        when(repository.findAll()).thenReturn(Collections.singletonList(session));
        PsyApplication app = appWithRepository(repository);
        invokePrivate(app, "viewAllSchedules");
        assertTrue(outputStreamState.value().contains("Знайдено 1"));
    }

    @Test
    void dropAllSchedulesDeletesDataAndPrintsMessage() throws Exception {
        PsyRepository repository = mock(PsyRepository.class);
        PsyApplication app = appWithRepository(repository);
        invokePrivate(app, "dropAllSchedules");
        verify(repository, times(1)).deleteAll();
        assertTrue(outputStreamState.value().contains("видалено"));
    }

    @Test
    void runStopsWhenChoiceIsFour() throws Exception {
        PsyRepository repository = mock(PsyRepository.class);
        PsyApplication app = appWithRepository(repository);
        inputStreamState.replace("4\n"); // Імітуємо вихід
        app.run();
        assertTrue(outputStreamState.value().contains("Вихід з програми"));
    }

    private static PsyApplication appWithRepository(PsyRepository repository) throws Exception {
        PsyApplication app = new PsyApplication();
        Field field = PsyApplication.class.getDeclaredField("PsyRepository");
        field.setAccessible(true);
        field.set(app, repository);
        return app;
    }

    private static void invokePrivate(PsyApplication app, String methodName) throws Exception {
        Method method = PsyApplication.class.getDeclaredMethod(methodName);
        method.setAccessible(true);
        method.invoke(app);
    }

    private static final class InputStreamState {
        private final java.io.InputStream original = System.in;
        void replace(String input) {
            System.setIn(new ByteArrayInputStream(input.getBytes()));
        }
        void restore() { System.setIn(original); }
    }

    private static final class OutputStreamState {
        private final PrintStream original = System.out;
        private final ByteArrayOutputStream captured = new ByteArrayOutputStream();
        OutputStreamState() {
            System.setOut(new PrintStream(captured));
        }
        String value() { return captured.toString(); }
        void restore() { System.setOut(original); }
    }
}
