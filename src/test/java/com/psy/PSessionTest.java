package com.psy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import org.junit.jupiter.api.Test;

class PSessionTest {

    @Test
    void toStringContainsAllImportantFields() throws Exception {
        PSession session = new PSession(
            "Іван Іванов", "Олена Петренко", "2024-10-15",
            "Індивідуальний", "Тривожність", "Zoom",
            "Так", "10 років", "вул. Київська, 10", "067-123-45-67"
        );

        Field idField = PSession.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(session, "id-123");

        String value = session.toString();

        assertTrue(value.contains(" id=\"id-123\""));
        assertTrue(value.contains(" psychologistName=\"Іван Іванов\""));
        assertTrue(value.contains(" clientName=\"Олена Петренко\""));
        assertTrue(value.contains(" sessionTopic=\"Тривожність\""));
    }

    @Test
    void testGettersAndSetters() {
        PSession session = new PSession();

        // Тестуємо сеттери
        session.setPsychologistName("Дмитро");
        session.setClientName("Олексій");
        session.setSessionDate("2024-12-01");
        session.setSessionPackage("Преміум");
        session.setSessionTopic("Стрес");
        session.setMeetingPlatform("Google Meet");
        session.setSubscription("Gold");
        session.setPsyExperience("15 років");
        session.setClinicAddress("Львів, Центр");
        session.setClinicPhone("0931112233");

        // Тестуємо геттери
        assertEquals("Дмитро", session.getPsychologistName());
        assertEquals("Олексій", session.getClientName());
        assertEquals("2024-12-01", session.getSessionDate());
        assertEquals("Преміум", session.getSessionPackage());
        assertEquals("Стрес", session.getSessionTopic());
        assertEquals("Google Meet", session.getMeetingPlatform());
        assertEquals("Gold", session.getSubscription());
        assertEquals("15 років", session.getPsyExperience());
        assertEquals("Львів, Центр", session.getClinicAddress());
        assertEquals("0931112233", session.getClinicPhone());
    }

    @Test
    void constructorStoresGivenValuesForSecondCsvRow() {
        PSession session = new PSession(
            "Марія Коваль", "Дмитро Сидорчук", "2024-11-20",
            "Груповий", "Депресія", "Google Meet",
            "Ні", "5 років", "вул. Львівська, 5", "050-987-65-43"
        );

        String value = session.toString();
        
        assertTrue(value.contains(" psychologistName=\"Марія Коваль\""));
        assertTrue(value.contains(" sessionTopic=\"Депресія\""));
        assertTrue(value.contains(" MeetingPlatform=\"Google Meet\""));
    }
}