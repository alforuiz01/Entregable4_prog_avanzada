# Fallos para la demo / defensa (rama `demo/fallo-*`)
1. **Test unitario roto**: en `MascotaServiceTest`, cambiar un assert -> falla etapa "Pruebas unitarias".
2. **Test de integración roto**: en `MascotaIT`, esperar 200 en vez de 404 -> falla "Pruebas de integración".
3. **Error de compilación**: borrar un `;` en `MascotaService` -> falla "Build".
4. **Smoke test**: poner una URL de BD inválida en `application.properties` -> la app no levanta, falla "Smoke test".
Cada uno: mostrar el log, corregir, push, ver el pipeline en verde.
