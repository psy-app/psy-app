package com.psy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.repository.MongoRepository;

class PsyRepositoryTest {

    @Test
    void repositoryExtendsMongoRepositoryWithExpectedGenericTypes() {
        // Перевіряємо, що PsyRepository наслідується від MongoRepository
        assertTrue(MongoRepository.class.isAssignableFrom(PsyRepository.class));

        // Отримуємо інформацію про Generic-типи інтерфейсу
        Type type = PsyRepository.class.getGenericInterfaces()[0];
        ParameterizedType parameterizedType = (ParameterizedType) type;

        // Перевіряємо, що це саме MongoRepository
        assertEquals(MongoRepository.class, parameterizedType.getRawType());
        
        // Перевіряємо тип сутності (має бути PSession)
        assertEquals(PSession.class, parameterizedType.getActualTypeArguments()[0]);
        
        // Перевіряємо тип ID (має бути String)
        assertEquals(String.class, parameterizedType.getActualTypeArguments()[1]);
    }
}
