package com.psy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true) // Вимагає запущеного Docker Desktop
class PsyFlowIntegrationTests {

    @Container
    private static final MongoDBContainer MONGO_DB_CONTAINER =
        new MongoDBContainer(DockerImageName.parse("mongo:7.0"));

    @Autowired
    private PsyRepository psyRepository;

    @Autowired
    private MockMvc mockMvc;

    @DynamicPropertySource
    static void configureMongo(DynamicPropertyRegistry registry) {
        // Динамічно налаштовує URI підключення до контейнера MongoDB
        registry.add("spring.data.mongodb.uri", MONGO_DB_CONTAINER::getReplicaSetUrl);
    }

    @AfterEach
    void cleanDatabase() {
        psyRepository.deleteAll();
    }

    @Test
    void addEndpointPersistsSessionToMongo() throws Exception {
        mockMvc.perform(post("/add")
                .param("psychologistName", "Дмитро Сидоренко")
                .param("clientName", "Олена Коваленко")
                .param("sessionDate", "2024-05-20")
                .param("sessionPackage", "Стандарт")
                .param("sessionTopic", "Консультація")
                .param("MeetingPlatform", "Zoom") // Згідно з полем у PSession.java
                .param("subscription", "Так")
                .param("psyExperience", "5 років")
                .param("clinicAddress", "вул. Шевченка, 10")
                .param("clinicPhone", "+380501112233"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/"));

        assertThat(psyRepository.findAll())
            .hasSize(1)
            .extracting(PSession::getClientName)
            .containsExactly("Олена Коваленко");
    }

    @Test
    void savedSessionsAreRenderedOnTheMainPage() throws Exception {
        psyRepository.save(new PSession(
            "Дмитро Сидоренко",
            "Олена Коваленко",
            "2024-05-20",
            "Стандарт",
            "Консультація",
            "Zoom",
            "Так",
            "5 років",
            "вул. Шевченка, 10",
            "+380501112233"
        ));

        mockMvc.perform(get("/"))
            .andExpect(status().isOk())
            .andExpect(view().name("schedule")) // Згідно з контролером
            .andExpect(content().string(org.hamcrest.Matchers.containsString("Олена Коваленко")))
            .andExpect(content().string(org.hamcrest.Matchers.containsString("Консультація")));
    }

    @Test
    void addPageRendersFormWithEmptySessionModel() throws Exception {
        mockMvc.perform(get("/add"))
            .andExpect(status().isOk())
            .andExpect(view().name("add"))
            .andExpect(model().attributeExists("schedule")); // Згідно з методом showAddForm
    }

    @Test
    void deleteEndpointRemovesPersistedSessionFromMongo() throws Exception {
        PSession saved = psyRepository.save(new PSession(
            "Дмитро Сидоренко",
            "Олена Коваленко",
            "2024-05-20",
            "Стандарт",
            "Депресія",
            "Google Meet",
            "Ні",
            "10 років",
            "вул. Лесі Українки, 5",
            "+380679998877"
        ));

        mockMvc.perform(post("/delete/{id}", saved.getId()))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/"));

        assertThat(psyRepository.findById(saved.getId())).isEmpty();
    }
}