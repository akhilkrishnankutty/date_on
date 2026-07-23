## 2026-05-02 - [CRITICAL] Prevented IDOR and Data Exposure in User Profile Endpoint
**Vulnerability:** The `/user/{id}` endpoint allowed unauthenticated arbitrary data exposure and IDOR. It relied on an optional `requestorId` parameter instead of the Spring Security `Authentication` context, meaning any authenticated user could fetch the full profile (including email, password hash, etc.) of any other user simply by calling the endpoint.
**Learning:** Never trust client-provided parameters (like `requestorId`) for authorization checks. Spring Security's context must be used to identify the current user. Also, endpoints returning user entities should be careful not to serialize sensitive fields unless explicitly required for the current user.
**Prevention:** Use `org.springframework.security.core.Authentication` injected into controller methods to identify the user making the request. Apply role/ownership checks server-side. Map Entities to DTOs to prevent accidental exposure of fields like `password` or `mail`.

## 2026-07-23 - [CRITICAL] Prevented IDOR on Match Profile Pictures
**Vulnerability:** The `/user/{id}` endpoint returned the unblurred `profilePictureUrl` of a matched user immediately, relying on the frontend to blur the image if the match was less than 5 days old. This allowed an attacker to inspect the network response and access the original image url directly.
**Learning:** Never trust the frontend to mask or hide sensitive data. Transformations like blurring must be enforced at the backend.
**Prevention:** Replace the original URL with a hardcoded generic placeholder blurred URL at the API level until the time condition (5 days) is met.
