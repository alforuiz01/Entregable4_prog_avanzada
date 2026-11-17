# Adopción de mascotas – Pipeline CI/CD con Jenkins

Aplicación web (Java 17 + Spring Boot 3) para registrar mascotas encontradas y buscarlas por especie, género y edad.
El foco del proyecto es el **pipeline declarativo de Jenkins** (`Jenkinsfile`, versionado junto al código) que compila,
prueba, empaqueta y despliega la aplicación en un ambiente de producción simulado (contenedor Docker).

## Arquitectura

```
Desarrollador (IntelliJ + Git) --push--> GitHub --polling--> Jenkins (Docker :8080) --docker run--> mascotas-prod (Docker :8081)
                                                                                                        |
                                                                                              volumen Docker (datos H2)
```

- **Jenkins** corre en un contenedor Docker que monta el socket de Docker del anfitrión, para poder construir la imagen
  y levantar el contenedor de producción simulada como contenedor hermano (red `cicd-net`).
- **Persistencia:** H2 en archivo (`./data/mascotas`); en producción simulada, en el volumen `mascotas-data`.

## Estructura del repositorio

```
mascotas-cicd/
├── pom.xml                     Parent Maven (Spring Boot 3.3.5, Java 17)
├── mascotas-core/              Dominio, reglas de negocio y persistencia (JPA/H2)
│   └── test: MascotaServiceTest (8 pruebas unitarias, Mockito)
├── mascotas-web/               API REST + pantalla HTML; depende de core
│   └── test: MascotaControllerTest (4 unitarias, @WebMvcTest)
│             MascotaIT (6 de integración, @SpringBootTest + H2 en memoria)
├── Jenkinsfile                 Pipeline declarativo
├── Dockerfile                  Imagen de la aplicación (producción simulada)
├── jenkins/
│   ├── Dockerfile              Jenkins LTS + Maven + curl + docker-cli
│   └── docker-compose.yml      Jenkins en :8080, red cicd-net, volumen jenkins_home
└── docs/
    ├── errores-pipeline.md     Registro de errores del pipeline
    └── provocar-fallos.md      Fallos sugeridos para la demo
```

Convención de pruebas: las clases `*Test` son unitarias (Surefire, fase `test`) y las `*IT` son de integración
(Failsafe, fase `verify`). Total: **18 pruebas**.

## La aplicación

Funcionalidades: registrar una mascota, buscarla por especie, género y rango de edad, ver el catálogo con foto
(por URL) y marcarla como adoptada (deja de aparecer en el catálogo).

Reglas de negocio: nombre obligatorio, edad entre 0 y 40 años, foto por defecto si no se indica ninguna y no se puede
adoptar dos veces la misma mascota.

| Método | Ruta | Descripción |
|--------|------|-------------|
| POST | `/api/mascotas` | Registra una mascota (201) |
| GET | `/api/mascotas?especie=&genero=&edadMin=&edadMax=` | Busca mascotas disponibles |
| GET | `/api/mascotas/{id}` | Obtiene una mascota (404 si no existe) |
| POST | `/api/mascotas/{id}/adoptar` | Marca la mascota como adoptada |
| GET | `/actuator/health` | Estado de la aplicación (lo usa el smoke test) |

## Ejecución local (sin Jenkins)

Requisitos: JDK 17 o superior y Maven 3.9+ (o IntelliJ IDEA, que trae Maven integrado).

```
mvn clean verify
java -jar mascotas-web\target\mascotas-web.jar
```

Abrir http://localhost:8080. Si el puerto está ocupado: `--server.port=9090`.
En IntelliJ: panel Maven → *Lifecycle → verify*, y ejecutar la clase `MascotasApplication`.

## Levantar Jenkins

Requisitos: Docker Desktop abierto.

```
cd jenkins
docker compose up -d --build
docker exec jenkins cat /var/jenkins_home/secrets/initialAdminPassword
```

1. Abrir http://localhost:8080, ingresar la clave inicial e instalar los plugins sugeridos.
2. Crear el usuario administrador.
3. *Nuevo ítem* → nombre `mascotas-pipeline` → tipo **Pipeline**.
4. Sección Pipeline: *Pipeline script from SCM* → Git → URL del repositorio.
   Si es privado: credenciales *Username with password* (usuario de GitHub + token personal con permiso `repo`).
5. *Branch Specifier*: `*/main`. *Script Path*: `Jenkinsfile`. Guardar y ejecutar **Build Now**.

La aplicación desplegada queda en http://localhost:8081.

> **No usar `docker compose down -v`:** el `-v` borra el volumen `jenkins_home` y se pierde toda la configuración de Jenkins.

### Apagar y volver a encender

```
docker stop jenkins mascotas-prod
docker start jenkins mascotas-prod
```

## El pipeline

| Etapa | Qué hace |
|-------|----------|
| Checkout | Descarga el código de GitHub |
| Build | `mvn clean compile` de los dos módulos |
| Pruebas unitarias | `mvn test` (clases `*Test`) y publica los reportes JUnit |
| Pruebas de integración | `mvn verify` (clases `*IT`, contexto Spring completo) y publica los reportes |
| Empaquetado | Genera `mascotas-web.jar` y lo archiva |
| Deploy | Solo rama `main`: construye la imagen y reemplaza el contenedor `mascotas-prod` (:8081) |
| Smoke test | Consulta `/actuator/health` hasta 60 s; si no responde UP, el build falla |

Si una etapa falla, las siguientes no se ejecutan: el código roto no llega a producción simulada.

**Disparo automático:** el `Jenkinsfile` declara `githubPush()` y `pollSCM(...)`. En esta instalación se usa el *polling*:
Jenkins consulta GitHub periódicamente (el intervalo se configura en `pollSCM`) y, si hay cambios, inicia el build
("Started by an SCM change"). Un webhook evitaría la espera, pero exige exponer Jenkins a internet; al correr en local,
el polling cumple el requisito sin exponer nada.

## Flujo de trabajo con Git

- `main` se mantiene estable; los cambios entran por ramas y Pull Request.
- Ramas: `feature/*`, `fix/*`, `docs/*`. Una rama por propósito.
- Commits con prefijo: `feat:`, `fix:`, `test:`, `docs:`, `ci:`.
- Antes de commitear, `git status`; preferir `git add ruta/archivo` a `git add .`.
- Proceso: `git checkout main; git pull` → crear rama → commit → `git push -u origin RAMA` → Pull Request → Merge →
  esperar el build en verde → `git checkout main; git pull` → borrar la rama local.

## Guía de demo (defensa)

Preparación: abrir Docker Desktop, correr `docker start jenkins mascotas-prod` y comprobar que respondan
http://localhost:8080 (Jenkins) y http://localhost:8081 (aplicación). Antes de la defensa conviene dejar las ramas
y los Pull Requests ya creados, sin hacer *Merge*, para que en vivo solo haya que integrarlos.

### Caso 1: integración exitosa

```
git checkout main; git pull
git checkout -b feature/demo-titulo
# editar el h1 de mascotas-web/src/main/resources/static/index.html
git add -A; git commit -m "feat: cambiar titulo"
git push -u origin feature/demo-titulo
```

En GitHub: abrir el Pull Request y hacer *Merge*. Jenkins detecta el cambio solo, corre todas las etapas en verde
y la aplicación en :8081 muestra el título nuevo (recargar con `Ctrl + F5`).

### Caso 2: fallo en las pruebas

```
git checkout main; git pull
git checkout -b fix/demo-test-roto
# en MascotaServiceTest (buscarFiltraPorEspecieGeneroYEdad): assertEquals(3, pasa a assertEquals(99,
git add -A; git commit -m "test: romper test"
git push -u origin fix/demo-test-roto
```

En GitHub: abrir el Pull Request y hacer *Merge*. El build queda en rojo en *Pruebas unitarias*
(`expected: <99> but was: <3>`), no hay Deploy y la aplicación en :8081 sigue con la última versión buena.
Para arreglarlo: en el Pull Request ya integrado apretar *Revert*, crear el Pull Request y hacer *Merge*;
el pipeline vuelve a verde.

## Errores encontrados en el pipeline

| # | Build | Etapa | Síntoma | Causa | Solución |
|---|-------|-------|---------|-------|----------|
| 1 | #2 | Deploy | `docker: not found` (exit 127) | La imagen de Jenkins no incluía el cliente Docker | Instalar `docker-cli` en `jenkins/Dockerfile` y reconstruir con `docker compose up -d --build` |
| 2 | #7 | Pruebas unitarias | `expected: <99> but was: <3>` en `MascotaServiceTest` | Aserción incorrecta introducida a propósito | Corregir el valor esperado por Pull Request; el pipeline vuelve a verde |

El detalle está en `docs/errores-pipeline.md` y en el PDF de documentación.

## Problemas frecuentes

| Problema | Qué revisar |
|----------|-------------|
| `Invalid trigger type 'githubPush'` | Falta el plugin **GitHub**: *Manage Jenkins → Plugins → Available plugins* |
| `Authentication failed` en Checkout | Usuario o token de GitHub mal cargados, o sin permiso `repo` |
| `docker: not found` en Deploy | Reconstruir Jenkins: `cd jenkins; docker compose up -d --build` |
| `permission denied ... docker.sock` | El contenedor de Jenkins debe correr como `root` (ya está en el compose) |
| Puerto 8080 ocupado | Detener la aplicación local o usar `--server.port=9090` |
| Jenkins no arranca solo tras un push | Esperar al intervalo del polling, o forzar con **Build Now** |
| `mvn` no se reconoce en PowerShell | Usar el panel Maven de IntelliJ o agregar Maven al PATH |
