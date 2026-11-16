# Imagen de la aplicacion (ambiente simulado de produccion)
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY mascotas-web/target/mascotas-web.jar app.jar
VOLUME /app/data
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
