# Сборка бэкенда AlgoSchool.
#
# Собирается в два этапа: сначала jar в образе с JDK, затем — только он сам
# в образ с JRE, чтобы Maven и исходники не ехали в рантайм.
#
# Интеграционные тесты требуют живой БД, поэтому здесь прогоняются только
# обычные (профиль integration не активируется).

FROM eclipse-temurin:21-jdk AS build
WORKDIR /build

# Сначала — только описание зависимостей: слой с ними переиспользуется,
# пока pom.xml не изменился.
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -B -q dependency:go-offline

COPY src/ src/
RUN ./mvnw -B -q clean package -DskipTests=false

FROM eclipse-temurin:21-jre
WORKDIR /app

# Не запускаем приложение от root
RUN useradd --system --create-home --shell /usr/sbin/nologin algoschool
USER algoschool

COPY --from=build --chown=algoschool:algoschool /build/target/*.jar /app/application.jar

EXPOSE 8080

# Секреты и адрес БД приходят через окружение (см. .env.example):
# DB_URL, DB_USERNAME, DB_PASSWORD, JWT_SECRET
ENTRYPOINT ["java", "-jar", "/app/application.jar"]
