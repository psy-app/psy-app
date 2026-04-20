package com.psy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import java.util.List;

/**
 * Web-Додаток для редагування бази даних розкладу прийому на сеанс у психолога.
 * 
 * @SpringBootApplication - анотація для позначення головного класу Spring Boot додатку.
 * 
 * Клас реалізує CommandLineRunner для виконання коду після запуску додатку.
 * 
 * Методи:
 * - main(String[] args): запускає додаток Spring Boot.
 * - run(String... args): метод, що виконується після запуску додатку. Виводить меню для користувача.
 * - addScheduleFromCsv(): додає розклад з CSV-файлу до бази даних.
 * - viewAllSchedules(): виводить всі розклади з бази даних.
 * - dropAllSchedules(): видаляє всі розклади з бази даних.
 * 
 * Поля:
 * - PsyRepository: репозиторій для роботи з розкладами.
 * 
 * Використовує:
 * - Scanner для зчитування вводу користувача.
 * - CSVReader для зчитування даних з CSV-файлу.
 * - PSession для представлення документу розкладу.
 * - PsyRepository для взаємодії з базою даних.
 */
@SpringBootApplication
public class PsyApplication{
    @Autowired
        private PsyRepository psyRepository;

    public static void main(String[] args) {
        SpringApplication.run(PsyApplication.class, args);
    }

    private void viewAllSchedules() {
        List<PSession> schedules = psyRepository.findAll();
        if (schedules.isEmpty()) {
            System.out.println("Документи з рядками розкладу не знайдено.");
        } else {
            System.out.println("Знайдено " + schedules.size() + " документів розкладу:");
            schedules.forEach(System.out::println);
        }
    }

    private void dropAllSchedules() {
        psyRepository.deleteAll();
        System.out.println("Розклад видалено.");
    }
}