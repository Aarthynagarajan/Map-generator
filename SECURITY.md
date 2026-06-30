# SecurityHardening Document

ProcessPro enforces modern security best practices across both backend and frontend.

## Authentication & Sessions

- **Password Hashing**: Cryptographically salts passwords utilizing BCrypt (strength factor 12) inside `AuthService`.
- **Stateless JWT**: Access tokens expire in 15 minutes. Refresh tokens expire in 7 days and are checked against DB.
- **Refresh Token Rotation**: Refreshing access tokens generates a new, single-use refresh token and invalidates the previous one in the database (rotation).

---

## Authorization

- **Route Protection**: All routes are default closed except for `/auth/*` and `/share/*`.
- **Ownership Validation**: Projects, diagrams, and history checks explicitly enforce matching ownership `user_id` inside the Service layer.

---

## Input Protection

- **SQL Injection**: Prevented using Spring Data JPA parameterized queries.
- **Cross-Site Scripting (XSS)**: Prevented via React's default text interpolation escaping and sanitizing inputs.
- **CORS**: Strictly configured to block domains except the approved `FRONTEND_URL` environment parameter.
