# Етап збірки застосунку за допомогою Maven і Java 17
FROM maven:3.9.9-eclipse-temurin-17 AS build

# Робоча директорія для збірки
WORKDIR /app

# Копіюємо файли збірки і вихідний код застосунку
COPY pom.xml ./
COPY checkstyle.xml ./
COPY src ./src

# Пакуємо застосунок і копіюємо runtime-залежності окремо
RUN mvn -B -DskipTests package dependency:copy-dependencies

# Мінімальний runtime-образ з Java 17
FROM eclipse-temurin:17-jre

# Робоча директорія контейнера
WORKDIR /app

# Копіюємо зібраний jar файл і залежності з етапу збірки
COPY --from=build /app/target/*.jar ./app.jar
COPY --from=build /app/target/dependency ./lib

# Значення порту за замовчуванням для локального запуску
ENV PORT=8081

# Порт, який слухає застосунок
EXPOSE 8081

# Старт Spring Boot застосунку через main class і classpath
CMD ["java", "-cp", "app.jar:lib/*", "com.psy.PsyApplication"]