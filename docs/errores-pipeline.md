# Registro de errores del pipeline

| # | Build | Etapa | Síntoma (log) | Causa | Solución | Commit |
|---|-------|-------|---------------|-------|----------|--------|
|   |       |       |               |       |          |        |
| 1 | #2 | Deploy | `docker: not found` (exit code 127) | La imagen de Jenkins no tenía instalado el cliente Docker | Instalar `docker-cli` en `jenkins/Dockerfile` y reconstruir la imagen | 4b22729 |