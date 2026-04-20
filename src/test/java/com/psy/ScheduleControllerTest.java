package com.psy;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;

import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Тести для ScheduleController.
 * Перевіряють коректність повернення представлень (HTML-сторінок) та роботу з моделлю.
 */
@WebMvcTest(ScheduleController.class)
class ScheduleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PsyRepository psyRepository;

    @Test
    void viewScheduleReturnsCorrectView() throws Exception {
        // Налаштовуємо мок репозиторію
        when(psyRepository.findAll()).thenReturn(Collections.emptyList());

        // Виконуємо запит до головної сторінки
        mockMvc.perform(get("/"))
            .andExpect(status().isOk())
            .andExpect(view().name("schedule"))
            .andExpect(model().attributeExists("schedules"));
    }

    @Test
    void showAddFormReturnsAddView() throws Exception {
        // Перевіряємо сторінку додавання запису
        mockMvc.perform(get("/add"))
            .andExpect(status().isOk())
            .andExpect(view().name("add"))
            .andExpect(model().attributeExists("schedule"));
    }
}