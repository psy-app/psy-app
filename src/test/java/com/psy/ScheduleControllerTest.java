package com.psy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

class ScheduleControllerTest {

    @Test
    void viewScheduleReturnsScheduleViewName() {
        // Використовуємо PsyRepository замість ScheduleRepository
        PsyRepository repository = mock(PsyRepository.class);
        when(repository.findAll()).thenReturn(Collections.emptyList());
        ScheduleController controller = new ScheduleController(repository);
        Model model = new ExtendedModelMap();

        String viewName = controller.viewSchedule(model);

        // Перевіряємо повернення імені шаблону "schedule"
        assertEquals("schedule", viewName);
    }

    @Test
    void viewScheduleAddsAllSchedulesToModel() {
        PsyRepository repository = mock(PsyRepository.class);
        // Створюємо об'єкт PSession з вашими параметрами
        PSession psession = new PSession("Psyc", "Client", "2024-09-14", "Pkg", "Topic", "Zoom", "Basic", "9", "Addr", "123");
        when(repository.findAll()).thenReturn(Collections.singletonList(psession));
        ScheduleController controller = new ScheduleController(repository);
        Model model = new ExtendedModelMap();

        controller.viewSchedule(model);

        verify(repository, times(1)).findAll();
        // Перевіряємо атрибут "schedules" у моделі
        assertNotNull(model.getAttribute("schedules"));
        assertEquals(1, ((List<?>) model.getAttribute("schedules")).size());
    }

    @Test
    void showAddFormReturnsAddViewName() {
        PsyRepository repository = mock(PsyRepository.class);
        ScheduleController controller = new ScheduleController(repository);
        Model model = new ExtendedModelMap();

        String viewName = controller.showAddForm(model);

        assertEquals("add", viewName);
    }

    @Test
    void showAddFormAddsEmptyScheduleToModel() {
        PsyRepository repository = mock(PsyRepository.class);
        ScheduleController controller = new ScheduleController(repository);
        Model model = new ExtendedModelMap();

        controller.showAddForm(model);

        // Перевіряємо, що в модель додано об'єкт "schedule"
        assertNotNull(model.getAttribute("schedule"));
    }

    @Test
    void addScheduleSavesToRepositoryAndRedirects() {
        PsyRepository repository = mock(PsyRepository.class);
        ScheduleController controller = new ScheduleController(repository);
        PSession session = new PSession("Psyc", "Client", "2024-09-14", "Pkg", "Topic", "Zoom", "Basic", "9", "Addr", "123");

        String viewName = controller.addSchedule(session);

        // Перевіряємо збереження через psyRepository
        verify(repository, times(1)).save(session);
        assertEquals("redirect:/", viewName);
    }

    @Test
    void deleteScheduleRemovesFromRepositoryAndRedirects() {
        PsyRepository repository = mock(PsyRepository.class);
        ScheduleController controller = new ScheduleController(repository);

        String viewName = controller.deleteSchedule("id-123");

        // Перевіряємо видалення за ID
        verify(repository, times(1)).deleteById("id-123");
        assertEquals("redirect:/", viewName);
    }
}