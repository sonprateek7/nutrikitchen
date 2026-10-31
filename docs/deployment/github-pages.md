# GitHub Pages deployment

## What is deployed

The `pages.yml` workflow publishes the static browser application from `frontend/`. It runs on pushes to `master` and can also be started manually from the Actions tab. The app has no build-time secrets and no third-party runtime dependencies. Without an API URL configured, user data is stored in the current browser's local storage.

GitHub Pages does not run Java, Spring Boot, or PostgreSQL. To use the API, host it separately, set `window.NUTRIKITCHEN_API_BASE` in `frontend/config.js`, and set the backend's `CORS_ALLOWED_ORIGINS` to the exact `https://<owner>.github.io` origin. Do not put database credentials or private API keys in the frontend.

**The API currently has no authentication. Do not connect this public Pages site to an internet-accessible backend containing personal data until authentication and authorization have been implemented.** CORS is not an access-control mechanism. Until then, run the API locally or keep it on a trusted private network.

For a local PostgreSQL deployment, `docker compose up --build` runs the Spring Boot API and a persistent PostgreSQL volume. A hosted backend deployment will additionally need managed PostgreSQL, TLS, backups, secrets, and authentication before public access.

## Initial repository setup

1. Push the workflow and frontend to `master`.
2. In **Settings → Pages**, choose **GitHub Actions** as the build and deployment source.
3. Check the `Deploy to GitHub Pages` workflow in the Actions tab. The deployed URL appears in the Pages settings when deployment succeeds.

The workflow uses the repository's built-in `GITHUB_TOKEN` with Pages deployment permissions. No user secret is required for this static release.

## Recovery and privacy

The app stores data only in the current browser profile. Clearing site data, changing browsers, or changing devices does not transfer it. Use the download control in the app to save a JSON backup before clearing browser data. The export can include personal health-related information; store it somewhere private.

Nutrition numbers and meal ideas are estimates for general information, not medical advice. They should not be used to diagnose or treat a medical condition.
