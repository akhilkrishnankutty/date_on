## 2026-05-02 - [CRITICAL] Prevented IDOR and Data Exposure in User Profile Endpoint
**Vulnerability:** The `/user/{id}` endpoint allowed unauthenticated arbitrary data exposure and IDOR. It relied on an optional `requestorId` parameter instead of the Spring Security `Authentication` context, meaning any authenticated user could fetch the full profile (including email, password hash, etc.) of any other user simply by calling the endpoint.
**Learning:** Never trust client-provided parameters (like `requestorId`) for authorization checks. Spring Security's context must be used to identify the current user. Also, endpoints returning user entities should be careful not to serialize sensitive fields unless explicitly required for the current user.
**Prevention:** Use `org.springframework.security.core.Authentication` injected into controller methods to identify the user making the request. Apply role/ownership checks server-side. Map Entities to DTOs to prevent accidental exposure of fields like `password` or `mail`.

## 2026-05-03 - [CRITICAL] Fixed IDOR via Match Time Exposure in User Profile Endpoint
**Vulnerability:** The `/user/{id}` endpoint exposed the real profile picture URL of a matched user immediately, ignoring the 5-day blurring business rule. It also didn't fail-close if `matchTime` was null, causing an IDOR data exposure.
**Learning:** Business logic security rules (like time-based image blurring) must be enforced server-side. Do not inject frontend-strippable transformations into original URLs; return a generic placeholder URL to prevent IDOR.
**Prevention:** Enforce time-based data exposure rules explicitly in the controller, ensuring a 'fail closed' default for edge cases like null timestamps.
