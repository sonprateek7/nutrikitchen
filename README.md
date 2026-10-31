# NutriKitchen

NutriKitchen is a single-user nutrition and kitchen companion. The first usable browser release includes a daily nutrition dashboard, meal journal, personal food library, pantry estimates, recipes, and ingredient-aware meal ideas.

## Run the full app locally

The Java 21/Spring Boot application serves both the browser UI and REST API at `http://localhost:8080`. It uses a local H2 database file for development:

```sh
./gradlew bootRun
```

Open [http://localhost:8080](http://localhost:8080). The default H2 database survives application restarts. To run the application against PostgreSQL instead, start Docker and run:

```sh
docker compose up --build
```

Compose starts PostgreSQL and the app with Flyway-managed schema migrations. The sample Compose password is for local development only.

Run the API and frontend tests with:

```sh
./gradlew test
```

The REST API covers foods, goals, meals and daily totals, pantry stock, recipes, recommendations, backup import, and health. All routes are under `/api`; `GET /api/health` is the readiness check. The UI connects to the API automatically when served by Spring Boot.

For static hosting, the UI remains usable with browser-local storage. Configure `window.NUTRIKITCHEN_API_BASE` in `frontend/config.js` to connect it to a separately hosted API, and set that API's `CORS_ALLOWED_ORIGINS` to the exact Pages origin. The backend currently has no authentication; **do not expose it to the public internet or store sensitive personal data on an internet-accessible deployment until authentication and authorization are implemented.**

## GitHub Pages

`.github/workflows/pages.yml` publishes `frontend/` to GitHub Pages when code is pushed to `master`. GitHub Pages hosts only static assets, not the Spring Boot API or database. On first setup, enable **Settings → Pages → Build and deployment → Source → GitHub Actions**. The workflow will publish the site and expose its URL in the Pages settings.

See [deployment notes](docs/deployment/github-pages.md) for what Pages does and does not host.

## Project docs

- [MVP scope and requirements](requirements/mvp-scope.md)
- [Initial domain model](docs/architecture/domain-model.md)
- [ADR 0001: modular monolith + Spring Boot](docs/adr/0001-single-user-modular-monolith.md)
- [REST API reference](docs/api.md)
- [GitHub Pages deployment](docs/deployment/github-pages.md)
