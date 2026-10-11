FROM node:24-alpine AS frontend
WORKDIR /src/frontend
COPY frontend/package*.json ./
# Resolve optional platform-specific Angular/esbuild packages for the Linux image.
RUN npm install
COPY frontend/ ./
RUN npm run build
FROM gradle:9.6-jdk25 AS backend
WORKDIR /src
COPY gradle gradle
COPY gradlew settings.gradle build.gradle ./
COPY backend backend
COPY --from=frontend /src/frontend/dist/frontend/browser backend/src/main/resources/static
RUN ./gradlew :backend:bootJar --no-daemon -x buildFrontend
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app
COPY --from=backend /src/backend/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]
