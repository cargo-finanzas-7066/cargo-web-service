# CarGo Financial API

Backend Spring Boot 3 / Java 17 para cotizar y guardar créditos vehiculares. La API usa PostgreSQL, Flyway y JWT Bearer.

## Inicio rápido

1. Copia `.env.example` a `.env` y cambia contraseñas y `JWT_SECRET`.
2. Para desarrollo, inicia solo PostgreSQL con `docker compose up -d postgres`.
3. Ejecuta la API local con `./mvnw spring-boot:run -Dspring-boot.run.profiles=dev`.
4. Abre Swagger en `http://localhost:8080/swagger-ui`.
4. Inicia sesión con `ADMIN_EMAIL` y `ADMIN_PASSWORD` del entorno.

Para desarrollo local sin Docker:

```bash
export JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home
export PATH="$JAVA_HOME/bin:$PATH"
set -a
source .env
set +a
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

El perfil `dev` incluye DevTools: si el IDE recompila al guardar, Spring reinicia automáticamente. Para ejecutar todo dentro de Docker use `docker compose --profile full up -d --build`; ese modo requiere reconstruir la imagen después de cambiar código.

## Flujo del frontend

1. `POST /api/v1/auth/sign-in` y guardar `token`.
2. Enviar `Authorization: Bearer <token>` en operaciones protegidas.
3. Consultar vehículos y productos financieros.
4. Crear o seleccionar un cliente.
5. Enviar `POST /api/v1/quotes` para comparar productos sin persistir.
6. Enviar la alternativa elegida a `POST /api/v1/simulations`; el servidor recalcula y guarda un snapshot inmutable.

Los porcentajes se expresan en puntos porcentuales: `teaPercent: 12.5` significa 12.5%, no `0.125`.

## Endpoints principales

- Públicos: `POST /api/v1/auth/sign-in`, `sign-up`; `GET /vehicles`, `/financial-products`, `/financial-institutions`, `/actuator/health`.
- Autenticados: `/auth/me`, `/clients`, `/quotes`, `/simulations`, `/analytics/dashboard`.
- Administración: altas/versiones/bajas de vehículos y productos financieros.

## Calidad y migraciones

`./mvnw test` ejecuta pruebas del motor. `./mvnw verify` genera cobertura y exige 80% de líneas en `FinancialEngine`. Las migraciones Flyway preservan tablas existentes, incorporan ownership/FKs y registran anomalías heredadas en `migration_issues`.
