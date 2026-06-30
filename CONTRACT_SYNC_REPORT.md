# Contract Synchronization Report — ProcessPro

This report documents the synchronization audit performed between the frontend Register page payload and the backend `RegisterRequest` DTO validation specifications.

---

## 1. Backend DTO Specifications (`RegisterRequest.java`)

```java
public record RegisterRequest(
    @NotBlank(message = "Email is required")
    @Email(message = "Must be a valid email address")
    String email,

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    String password,

    @NotBlank(message = "Display name is required")
    @Size(max = 100, message = "Display name cannot exceed 100 characters")
    String displayName
) {}
```

---

## 2. Frontend Registration Payload (`Register.tsx`)

Before this fix, the React Register page collected and sent only:
```json
{
  "email": "...",
  "password": "..."
}
```

---

## 3. Differences Found & Mismatches
1. **Missing Display Name**: The backend had `@NotBlank` validation for `displayName`, which was entirely absent from the React Register page. This caused all registration requests to fail with a `400 Bad Request` validation error.
2. **Password Length Validation**: The frontend did not validate that the password must be at least 8 characters long, whereas the backend enforced a `@Size(min = 8)` constraint. This mismatch allowed users to submit invalid passwords, causing validation failure exceptions.

---

## 4. Fixes Applied & Sync Steps
- **State Integration**: Added `displayName` state hooks and standard text input fields inside the register form.
- **Payload Alignment**: Updated `authService.register` inside [Register.tsx](file:///d:/New%20folder%20(4)/processpro/frontend/src/pages/Register.tsx) to send the complete `{ email, password, displayName }` object payload.
- **Frontend Validation**:
  - Enforced required checks on all inputs.
  - Added password length constraint check (`password.length >= 8`) on submit.
  - Verified confirm password matches the typed password.
- **Dynamic Alerts**: Configured the component error panel to display detailed backend field validation error responses if returned.

---

## 5. Verification Performed
- **TypeScript Typechecking**: Verified type definitions successfully using `npm run typecheck` (0 errors).
- **Vite compilation**: Built the production client successfully in 3.04s.
- **Backend Test Suite**: Executed `mvn clean test` successfully confirming the service mapper and controller validate registration requests correctly (BUILD SUCCESS).
- **E2E Flow**: Verified registration creates new users with display name metadata, permitting immediate login with the matching password.
