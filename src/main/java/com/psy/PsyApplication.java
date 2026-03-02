package com.psy;

import com.opencsv.CSVReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Додаток для редагування бази даних розкладу коледжу.
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
public class PsyApplication implements CommandLineRunner {

    @Autowired
    private PsyRepository PsyRepository;

    public static void main(String[] args) {
        SpringApplication.run(PsyApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("1. Додати розклад з CSV-файлу");
            System.out.println("2. Подивитись розклад");
            System.out.println("3. Видалити розклад");
            System.out.println("4. Вихід");
            System.out.print("Введіть номер команди (1-4): ");
            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    addScheduleFromCsv();
                    break;
                case 2:
                    viewAllSchedules();
                    break;
                case 3:
                    dropAllSchedules();
                    break;
                case 4:
                    System.exit(0);
                    break;
                default:
                    System.out.println("Номер команди некоректний. Спробуй ще.");
            }
        }
    }

    private void addScheduleFromCsv() {
        try (CSVReader reader = new CSVReader(new InputStreamReader(getClass().getClassLoader().getResourceAsStream("psychology_sessions.csv")))) {
            List<String[]> records = reader.readAll();
            
            records.remove(0); // Видалити перший рядок з назвами стовпців

            List<PSession> scheduleList = new ArrayList<>();
            for (String[] record : records) {
                PSession schedule = new PSession(
                    record[0], // clientName
                    record[1], // psychologistName
                    record[2], // sessionPackage
                    record[3], // sessionTopic
                    record[4], // MeetingPlatform
                    record[5], // subscription
                    record[6], // psyExperience
                    record[7], // clinicAddress
                    record[8], // clinicPhone
                    record[9] // sessionDate
);

                scheduleList.add(schedule);
            }
            
            PsyRepository.saveAll(scheduleList);
            System.out.println(scheduleList.size() + " документів з рядками з розкладу завантажено з CSV.");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Не вдалось завантажити рядок розкладу з CSV.");
        }
    }

    private void viewAllSchedules() {
        List<PSession> schedules = PsyRepository.findAll();
        if (schedules.isEmpty()) {
            System.out.println("Документи з рядками розкладу не знайдено.");
        } else {
            System.out.println("Знайдено " + schedules.size() + " документів розкладу:");
            schedules.forEach(System.out::println);
        }
    }

    private void dropAllSchedules() {
        PsyRepository.deleteAll();
        System.out.println("Розклад видалено.");
    }
}