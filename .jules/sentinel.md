## 2026-05-02 - [CRITICAL] Prevented IDOR and Data Exposure in User Profile Endpoint
**Vulnerability:** The `/user/{id}` endpoint allowed unauthenticated arbitrary data exposure and IDOR. It relied on an optional `requestorId` parameter instead of the Spring Security `Authentication` context, meaning any authenticated user could fetch the full profile (including email, password hash, etc.) of any other user simply by calling the endpoint.
**Learning:** Never trust client-provided parameters (like `requestorId`) for authorization checks. Spring Security's context must be used to identify the current user. Also, endpoints returning user entities should be careful not to serialize sensitive fields unless explicitly required for the current user.
**Prevention:** Use `org.springframework.security.core.Authentication` injected into controller methods to identify the user making the request. Apply role/ownership checks server-side. Map Entities to DTOs to prevent accidental exposure of fields like `password` or `mail`.

## 2026-09-16 - [CRITICAL] Fix IDOR in Match Profile Picture Exposure
**Vulnerability:** The `/user/{id}` endpoint returned the real `profilePictureUrl` of a match immediately, allowing users to fetch unblurred images before the required 5-day period.
**Learning:** Never trust the frontend to mask sensitive data. Cloudinary URL transformations can be stripped by attackers, so blurring must be enforced via hardcoded generic placeholders on the backend.
**Prevention:** Apply business rules server-side by validating `matchTime` and returning a hardcoded production-style blurred placeholder if the match is too recent or if the timestamp is missing (fail closed).
