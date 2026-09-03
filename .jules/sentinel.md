## 2026-05-02 - [CRITICAL] Prevented IDOR and Data Exposure in User Profile Endpoint
**Vulnerability:** The `/user/{id}` endpoint allowed unauthenticated arbitrary data exposure and IDOR. It relied on an optional `requestorId` parameter instead of the Spring Security `Authentication` context, meaning any authenticated user could fetch the full profile (including email, password hash, etc.) of any other user simply by calling the endpoint.
**Learning:** Never trust client-provided parameters (like `requestorId`) for authorization checks. Spring Security's context must be used to identify the current user. Also, endpoints returning user entities should be careful not to serialize sensitive fields unless explicitly required for the current user.
**Prevention:** Use `org.springframework.security.core.Authentication` injected into controller methods to identify the user making the request. Apply role/ownership checks server-side. Map Entities to DTOs to prevent accidental exposure of fields like `password` or `mail`.

## 2024-05-28 - [CRITICAL] Enforce Data Masking on Backend to Prevent IDOR
**Vulnerability:** The `/user/{id}` endpoint relied on the frontend to mask (blur) the profile picture of new matches (less than 5 days old), exposing the unblurred URL to direct API requests.
**Learning:** Never trust the frontend to mask or hide sensitive data. Injecting transformations (like `e_blur`) is insufficient as attackers can strip them.
**Prevention:** Always implement data masking on the backend and fail closed (e.g., return a generic placeholder URL if conditions are not met or state is null).
