# ADR 0001: Start with a single-user modular monolith on Spring Boot

- Status: Accepted
- Date: 2026-10-18

## Context

The project is a personal nutrition and meal tracking application with learning goals, a narrow early scope, and a clear desire to focus on backend fundamentals before adding frontend complexity or external integrations. The first milestone is to support meal logging and daily nutrition totals without overbuilding the architecture.

## Decision

We will start with a modular monolith in Java using Spring Boot and Spring Data JPA, backed by PostgreSQL. The application will be single-user for the initial release and will keep clear module boundaries for user data, nutrition data, meal logging, and future pantry/recipe features.

## Rationale

- The product is initially a personal project, not a multi-tenant SaaS product.
- Spring Boot and JPA are a practical, learnable stack for a real project with a relational database.
- A modular monolith avoids premature service decomposition while keeping domain boundaries explicit.
- PostgreSQL is a natural fit for structured nutrition and meal data and aligns with the project brief.
- Starting with a single-user model reduces security and identity complexity while preserving a clean path to multi-user support later.

## Consequences

### Positive

- Faster learning and faster iteration on the core domain.
- Clearer separation of responsibilities across modules.
- Easy persistence for meal totals, food items, and user goals.
- Better fit for the project brief's Phase 1 and Phase 2 milestones.

### Negative

- The application will need discipline to avoid mixing domain concepts across modules.
- The chosen architecture may need refactoring later if the app grows in user scale or operational complexity.
- Some features such as authentication and integrations will be intentionally deferred.

## Follow-up decisions

- Add persistence entities and repositories after requirements validation.
- Introduce Flyway when schema evolution becomes necessary.
- Revisit multi-user and authentication requirements when the MVP is ready to expand.
