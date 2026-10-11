# Restaurant POS

Restaurant point-of-sale for a Venezuelan menu, built with Angular 21, Spring Boot 4.0.8, Java 25, PostgreSQL, Keycloak, and Podman Compose. Spanish (Mexico) is the default UI language; the language toggle persists the choice in `localStorage`.

## Prerequisites

- macOS with Podman Desktop (or Podman CLI), Java 25, Node 24+, and npm.
- A Compose provider: install `podman-compose` or Docker Compose. `podman compose` delegates to one of these providers.
- Start the Podman VM once with `podman machine start`.

## Run the complete application

```bash
cp .env.example .env
./scripts/dev-up.sh
```

Open http://localhost:8080. Keycloak is at http://localhost:8081. The imported administrator is `admin` / `admin123`; change it before production use. Stop with `./scripts/dev-down.sh`, inspect with `./scripts/dev-status.sh`, or delete database data with `./scripts/dev-reset.sh`.

The Compose topology deliberately has exactly three services: `app`, `keycloak`, and `postgres`. PostgreSQL data is persistent in the `postgres-data` volume. Keycloak imports `infrastructure/keycloak/realm.json`, including `ADMIN`, `WAITER`, a PKCE browser client, and the initial administrator.

If `podman compose` reports that no provider is available, install `podman-compose` (for example, `brew install podman-compose`) and rerun `./scripts/dev-up.sh`.

## Local development

Start only backing services with `podman compose up -d postgres keycloak`, then in separate terminals:

```bash
./gradlew :backend:bootRun
cd frontend && npm start
```

Angular serves at http://localhost:4200 and proxies `/api` to port 8080. `bootRun` does not invoke the Angular build. A distributable executable application is produced by:

```bash
./gradlew clean :backend:bootJar
java -jar backend/build/libs/backend-1.0.0.jar
```

`bootJar` builds Angular and copies the browser build into Spring Boot static resources, so API and UI use one origin in production.

## Daily POS flow

1. Sign in and open a shift, recording opening cash.
2. Create a ticket from available catalog items. Prices are copied into ticket lines.
3. Set no tip, a percentage (10/15/20/custom via API), or fixed MXN via the API; Spring performs all monetary arithmetic with `BigDecimal`.
4. Open the check route and print using the browser print dialog. Printing never records payment.
5. Use cash payment once. An idempotency key prevents duplicate charges, records the payment, and closes the ticket.
6. Enter counted cash when closing the shift. The system computes expected cash and over/short difference, and refuses closure while open tickets remain.

Administrators can manage products through `/api/catalog` and employees through the protected `/api/admin/users` endpoint, which uses Keycloak’s Admin API. Waiters can operate tickets and their own shifts but cannot write catalog or users. The server enforces JWT role checks in addition to Angular guards.

## Architecture

- `backend/`: Spring REST resource server, JPA entities/repositories, Flyway migrations, ticket/shift business rules, and Groovy/Spock unit tests.
- `frontend/`: Angular standalone components, reactive-ready form infrastructure, Bootstrap 5, Bootstrap Icons, SCSS, Keycloak JS adapter, mobile sidebar/navigation, and print CSS.
- `infrastructure/keycloak/`: realm and client bootstrap.
- `scripts/`: local lifecycle and verification commands.

The Flyway migration seeds Empanadas, Arepas, Tequeños, Soda, Juice and sample products. Spring Boot 4 uses `spring-boot-starter-flyway` to activate migrations before Hibernate validates the schema. Hibernate uses `ddl-auto=validate`; credentials are never stored in PostgreSQL.

## Testing and troubleshooting

```bash
./gradlew :backend:test
cd frontend && npm run build
./scripts/verify-local.sh
```

Backend unit tests use Spock 2.4 with Groovy 5 on the JUnit Platform; place new specifications under `backend/src/test/groovy` and name them `*Spec.groovy`.

If the app rejects a JWT, confirm `KEYCLOAK_ISSUER` is `http://localhost:8081/realms/restaurant-pos`; this external issuer must match what browser tokens contain. In containers, `KEYCLOAK_JWK_SET_URI` must use the internal service address (`http://keycloak:8080/.../certs`) so Spring can fetch signing keys while still validating the public issuer. If ports are occupied, stop the conflicting local service. `podman compose logs -f app keycloak postgres` is the fastest diagnostic path. The Angular build can report the default initial-bundle budget warning because Bootstrap is global; it remains a successful build.
