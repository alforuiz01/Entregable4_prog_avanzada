# Adopción de mascotas – Pipeline CI/CD con Jenkins

Aplicación web (Java 17 + Spring Boot 3) para registrar mascotas encontradas y buscarlas por especie, género y edad.
El foco del proyecto es el **pipeline declarativo de Jenkins** (`Jenkinsfile`, versionado con el código).

## Estructura
```
mascotas-parent (pom)
├── mascotas-core   Dominio, reglas de negocio, persistencia (JPA/H2)   -> tests unitarios (Mockito)
├── mascotas-web    API REST + UI estática, depende de core             -> tests unitarios (@WebMvcTest) + integración (*IT)
├── Jenkinsfile     Pipeline declarativo
├── Dockerfile      Imagen de la app (producción simulada)
└── jenkins/        Dockerfile + docker-compose para levantar Jenkins
```

## Ejecución local (sin Jenkins)
Requisitos: JDK 17+, Maven 3.9+.
```bash
mvn clean verify                      # build + unitarias + integración
java -jar mascotas-web/target/mascotas-web.jar
# http://localhost:8080
```
API: `GET /api/mascotas?especie=PERRO&genero=MACHO&edadMin=1&edadMax=5`, `POST /api/mascotas`, `GET /api/mascotas/{id}`, `POST /api/mascotas/{id}/adoptar`, `GET /actuator/health`.

## Levantar Jenkins
```bash
cd jenkins
docker compose up -d --build
docker exec jenkins cat /var/jenkins_home/secrets/initialAdminPassword
```
1. Abrir http://localhost:8080, instalar plugins sugeridos (incluye Git, Pipeline, GitHub, Timestamper, JUnit).
2. *Nuevo ítem → Pipeline* → "Pipeline script from SCM" → Git → URL del repo → rama `*/main` → Script Path `Jenkinsfile`.
3. En *Build Triggers* marcar **GitHub hook trigger for GITScm polling**.
4. Webhook en GitHub (*Settings → Webhooks*): `http://<url-publica>/github-webhook/`, content type `application/json`, evento *push*.
   Si Jenkins corre en tu PC, exponelo con `ngrok http 8080` (o `cloudflared`). Mientras tanto `pollSCM` (cada ~5 min) sirve de respaldo.
5. Hacer un push: el pipeline corre solo. La app desplegada queda en **http://localhost:8081**.

## Etapas del pipeline
Checkout → Build → Pruebas unitarias → Pruebas de integración → Empaquetado → Deploy (contenedor `mascotas-prod`, solo rama `main`) → Smoke test (`/actuator/health`).

## Flujo de trabajo con Git
- `main`: siempre estable; solo se integra por Pull Request con el pipeline en verde.
- Ramas `feature/<tema>`, `fix/<tema>`; commits pequeños con mensajes descriptivos (`feat:`, `fix:`, `test:`, `ci:`, `docs:`).
- Cada integrante trabaja en su rama y abre PR; revisión cruzada antes del merge.
- Ramas distintas a `main` ejecutan build + tests, pero no despliegan.

## Registro de errores del pipeline
Ir completando `docs/errores-pipeline.md` a medida que aparezcan fallos (insumo para el PDF).
