## 2026-05-02 - [CRITICAL] Prevented IDOR and Data Exposure in User Profile Endpoint
**Vulnerability:** The `/user/{id}` endpoint allowed unauthenticated arbitrary data exposure and IDOR. It relied on an optional `requestorId` parameter instead of the Spring Security `Authentication` context, meaning any authenticated user could fetch the full profile (including email, password hash, etc.) of any other user simply by calling the endpoint.
**Learning:** Never trust client-provided parameters (like `requestorId`) for authorization checks. Spring Security's context must be used to identify the current user. Also, endpoints returning user entities should be careful not to serialize sensitive fields unless explicitly required for the current user.
**Prevention:** Use `org.springframework.security.core.Authentication` injected into controller methods to identify the user making the request. Apply role/ownership checks server-side. Map Entities to DTOs to prevent accidental exposure of fields like `password` or `mail`.

## 2026-08-31 - [CRITICAL] Prevented IDOR in Match Profile Picture URL
**Vulnerability:** The `/user/{id}` endpoint returned the unblurred `profilePictureUrl` of a match regardless of the 5-day waiting period, allowing attackers to access the original image directly.
**Learning:** Backend-only filtering (e.g., hardcoded placeholder URLs) must be used to mask sensitive resources when business rules restrict access based on time. Client-side URL transformations can be bypassed.
**Prevention:** Enforce strict business logic (like date checks) server-side and fail closed by returning generic placeholders for sensitive URLs.
