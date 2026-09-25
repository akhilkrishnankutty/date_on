## 2026-05-02 - [CRITICAL] Prevented IDOR and Data Exposure in User Profile Endpoint
**Vulnerability:** The `/user/{id}` endpoint allowed unauthenticated arbitrary data exposure and IDOR. It relied on an optional `requestorId` parameter instead of the Spring Security `Authentication` context, meaning any authenticated user could fetch the full profile (including email, password hash, etc.) of any other user simply by calling the endpoint.
**Learning:** Never trust client-provided parameters (like `requestorId`) for authorization checks. Spring Security's context must be used to identify the current user. Also, endpoints returning user entities should be careful not to serialize sensitive fields unless explicitly required for the current user.
**Prevention:** Use `org.springframework.security.core.Authentication` injected into controller methods to identify the user making the request. Apply role/ownership checks server-side. Map Entities to DTOs to prevent accidental exposure of fields like `password` or `mail`.

## 2026-05-03 - [CRITICAL] Fixed IDOR in Profile Picture Delivery
**Vulnerability:** The /user/{id} endpoint allowed matched users to fetch their partner's original unblurred profile picture before the required 5-day match duration by bypassing frontend masking.
**Learning:** Never trust the frontend to mask or hide sensitive data. Backend-only blurring using injected placeholder URLs must be enforced.
**Prevention:** Apply fail-closed authorization checks on server-side entities before serializing them into DTOs or responses.
