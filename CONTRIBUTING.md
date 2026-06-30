# Contributing to ProcessPro

We welcome community contributions. To contribute:

## Development Guidelines

1. **Layer Integrity**: Ensure that package boundaries are strictly respected. Controllers should never call repositories directly.
2. **Type Safety**: Maintain 100% typecheck safety on the frontend. Never use implicit `any` where custom types can be defined.
3. **Database Migrations**: Add all database changes as Flyway versioned SQL scripts.
4. **Testing**: Run backend and frontend tests before submitting a PR.
5. **Linting**: Ensure code conforms to code quality guidelines.
