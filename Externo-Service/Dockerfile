# --- Estágio 1: Build (Java 8) ---
# Usamos a imagem oficial do Maven com Java 8 (compatível com o pom)
FROM maven:3.9-eclipse-temurin-8 AS build

# Define o diretório de trabalho
WORKDIR /app

# Copia o pom.xml e baixa as dependências (para cache)
COPY pom.xml .
RUN mvn dependency:go-offline

# Copia o resto do código e constrói o .jar
COPY src ./src
RUN mvn clean package

# --- Estágio 2: Run (Java 8) ---
# Usamos uma imagem leve, apenas com o Java 8 para rodar
FROM eclipse-temurin:8-jre-jammy

WORKDIR /app

# Copia o .jar correto que você definiu no pom.xml
COPY --from=build /app/target/*.jar app.jar

# O Render define a porta; este comando diz ao Spring para usá-la
ENV PORT 8080
ENTRYPOINT ["java", "-jar", "app.jar", "--server.port=${PORT}"]