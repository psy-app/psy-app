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
@Testcontainers(disabledWithoutDocker = true)
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
        registry.add("spring.data.mongodb.uri", MONGO_DB_CONTAINER::getReplicaSetUrl);
    }

    @AfterEach
    void cleanDatabase() {
        psyRepository.deleteAll();
    }

    @Test
    void addEndpointPersistsSessionToMongo() throws Exception {
        mockMvc.perform(post("/add")
                .param("clientName", "Олександр")
                .param("psychologistName", "Марія")
                .param("sessionDate", "2024-05-15")
                .param("sessionPackage", "Індивідуальний")
                .param("sessionTopic", "Особистий розвиток")
                .param("meetingPlatform", "Zoom")
                .param("subscription", "Преміум")
                .param("psyExperience", "10 років")
                .param("clinicAddress", "вул. Хрещатик, 1")
                .param("clinicPhone", "0441234567"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/"));

        assertThat(psyRepository.findAll())
            .hasSize(1)
            .extracting(PSession::getSessionTopic)
            .containsExactly("Особистий розвиток");
    }

    @Test
    void savedSessionsAreRenderedOnTheMainPage() throws Exception {
        psyRepository.save(new PSession(
            "Марія",
            "Олександр",
            "2024-05-15",
            "Індивідуальний",
            "Особистий розвиток",
            "Zoom",
            "Преміум",
            "10 років",
            "вул. Хрещатик, 1",
            "0441234567"
        ));

        mockMvc.perform(get("/"))
            .andExpect(status().isOk())
            .andExpect(view().name("schedule"))
            .andExpect(content().string(org.hamcrest.Matchers.containsString("Олександр")))
            .andExpect(content().string(org.hamcrest.Matchers.containsString("Особистий розвиток")));
    }

    @Test
    void addPageRendersFormWithEmptySessionModel() throws Exception {
        mockMvc.perform(get("/add"))
            .andExpect(status().isOk())
            .andExpect(view().name("add"))
            .andExpect(model().attributeExists("schedule"));
    }

    @Test
    void deleteEndpointRemovesPersistedSessionFromMongo() throws Exception {
        PSession saved = psyRepository.save(new PSession(
            "Марія",
            "Олександр",
            "2024-05-15",
            "Індивідуальний",
            "Особистий розвиток",
            "Zoom",
            "Преміум",
            "10 років",
            "вул. Хрещатик, 1",
            "0441234567"
        ));

        mockMvc.perform(post("/delete/{id}", saved.getId()))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/"));

        assertThat(psyRepository.findById(saved.getId())).isEmpty();
    }
}