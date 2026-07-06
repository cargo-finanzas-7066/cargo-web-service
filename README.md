# CarGo Financial API

Backend Spring Boot 3 / Java 17 para cotizar y guardar créditos vehiculares. La API usa PostgreSQL, Flyway y JWT Bearer.

## Prerrequisitos

- **Docker Desktop** (o Docker Engine + Compose plugin) — para la base de datos PostgreSQL y, opcionalmente, para correr toda la API en contenedor.
- **JDK 17** — solo necesario si vas a compilar/correr la API fuera de Docker (ej. `brew install openjdk@17` en macOS).
- No necesitas instalar Maven: el repo incluye el wrapper (`./mvnw`), que descarga la versión correcta automáticamente.

## Inicio rápido

1. Copia `.env.example` a `.env` y cambia contraseñas y `JWT_SECRET` (mínimo 32 caracteres).
2. Levanta la base de datos con `docker compose up -d postgres` (descarga la imagen `postgres:16-alpine` la primera vez y crea el volumen `cargo-platform-postgres`).
3. Compila y ejecuta la API local con `./mvnw spring-boot:run -Dspring-boot.run.profiles=dev`.
4. Abre Swagger en `http://localhost:8080/swagger-ui/index.html`.
5. Inicia sesión con `ADMIN_EMAIL` y `ADMIN_PASSWORD` del `.env`.

Para desarrollo local sin Docker (API corriendo directo con Java, solo Postgres en Docker):

```bash
export JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home
export PATH="$JAVA_HOME/bin:$PATH"
set -a
source .env
set +a
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

> En macOS, si `./mvnw` falla con `Unable to locate a Java Runtime`, exporta `JAVA_HOME` explícitamente como arriba antes de correr cualquier comando `mvnw`.

El perfil `dev` incluye DevTools: si el IDE recompila al guardar (`./mvnw compile` en otra terminal), Spring reinicia automáticamente sin que tengas que parar/levantar el proceso.

## Compilación (build) del proyecto

```bash
./mvnw compile          # solo compila, rápido, útil junto con DevTools mientras editas
./mvnw test              # compila + corre pruebas unitarias
./mvnw verify             # test + reporte de cobertura (exige 80% de líneas en FinancialEngine)
./mvnw clean package      # genera el jar ejecutable en target/*.jar (salta tests con -DskipTests)
```

El jar generado se ejecuta con `java -jar target/api-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev` (ajusta el nombre exacto del jar según la versión en `pom.xml`).

## Construir e instalar la imagen Docker

El `Dockerfile` es multi-stage: compila con `maven:3.9.11-eclipse-temurin-17` y empaqueta el resultado en una imagen liviana `eclipse-temurin:17-jre-jammy` corriendo como usuario no-root.

**Opción A — todo orquestado con Docker Compose (recomendado para levantar API + Postgres juntos):**

```bash
docker compose --profile full up -d --build
```

Esto construye la imagen de la API (`cargo-platform-api`) usando el `Dockerfile`, levanta Postgres (`cargo-platform-postgres`), espera el healthcheck de la base de datos antes de arrancar la API, y expone la API en `http://localhost:8080`. Cada vez que cambies código fuente, vuelve a correr este mismo comando con `--build` para reconstruir la imagen (no hay hot-reload dentro del contenedor).

**Opción B — construir la imagen manualmente sin Compose:**

```bash
docker build -t cargo-platform-api .
docker run --rm -p 8080:8080 \
  -e DATABASE_URL=jdbc:postgresql://host.docker.internal:5432/cargo_platform \
  -e DATABASE_PASSWORD=postgres \
  -e JWT_SECRET=development-secret-key-change-me-32-chars \
  cargo-platform-api
```

Usa `host.docker.internal` para que el contenedor de la API alcance un Postgres que corre en tu host (o en otro contenedor de la misma red de Compose si usas `docker compose up -d postgres` por separado).

**Verificar que quedó arriba:**

```bash
curl http://localhost:8080/actuator/health   # {"status":"UP"}
```

Y abre `http://localhost:8080/swagger-ui/index.html` para probar los endpoints interactivamente.

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
