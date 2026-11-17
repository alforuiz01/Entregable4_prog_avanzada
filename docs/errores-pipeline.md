# Registro de errores del pipeline

| # | Build | Etapa | Síntoma (log) | Causa | Solución | Commit |
|---|-------|-------|---------------|-------|----------|--------|
|   |       |       |               |       |          |        |
| 1 | #2 | Deploy | `docker: not found` (exit code 127) | La imagen de Jenkins no tenía instalado el cliente Docker | Instalar `docker-cli` en `jenkins/Dockerfile` y reconstruir la imagen | 4b22729 |
| 2 | #7 | Pruebas unitarias | `There are test failures` (surefire); `expected: <99> but was: <3>` en `MascotaServiceTest.java:72`; etapas siguientes omitidas | Assertion incorrecta (99 en vez de 3) introducida a propósito| Corregir el valor esperado en un nuevo PR (rama `fix/arreglar-test`); el pipeline volvió a SUCCESS |f7be0ae|