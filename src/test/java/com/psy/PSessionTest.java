package com.psy;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import org.junit.jupiter.api.Test;

class PSessionTest {

    @Test
    void toStringContainsAllImportantFields() throws Exception {
        PSession session = new PSession(
            "Іван Іванов",
            "Олена Петренко",
            "2024-10-15",
            "Індивідуальний",
            "Тривожність",
            "Zoom",
            "Так",
            "10 років",
            "вул. Київська, 10",
            "067-123-45-67"
        );

        Field idField = PSession.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(session, "id-123");

        String value = session.toString();

        // Додано пробіл на початку та перевірку формату з лапками, як у вашому toString()
        assertTrue(value.contains(" id=\"id-123\""));
        assertTrue(value.contains(" psychologistName=\"Іван Іванов\""));
        assertTrue(value.contains(" clientName=\"Олена Петренко\""));
        assertTrue(value.contains(" sessionTopic=\"Тривожність\""));
        assertTrue(value.contains(" MeetingPlatform=\"Zoom\""));
    }

    @Test
    void constructorStoresGivenValuesForSecondCsvRow() {
        PSession session = new PSession(
            "Марія Коваль",
            "Дмитро Сидорчук",
            "2024-11-20",
            "Груповий",
            "Депресія",
            "Google Meet",
            "Ні",
            "5 років",
            "вул. Львівська, 5",
            "050-987-65-43"
        );

        String value = session.toString();
        
        // Зверніть увагу на велику літеру в MeetingPlatform, як у вашому класі
        assertTrue(value.contains(" psychologistName=\"Марія Коваль\""));
        assertTrue(value.contains(" sessionTopic=\"Депресія\""));
        assertTrue(value.contains(" MeetingPlatform=\"Google Meet\""));
    }
}
