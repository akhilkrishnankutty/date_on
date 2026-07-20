## 2026-05-02 - [CRITICAL] Prevented IDOR and Data Exposure in User Profile Endpoint
**Vulnerability:** The `/user/{id}` endpoint allowed unauthenticated arbitrary data exposure and IDOR. It relied on an optional `requestorId` parameter instead of the Spring Security `Authentication` context, meaning any authenticated user could fetch the full profile (including email, password hash, etc.) of any other user simply by calling the endpoint.
**Learning:** Never trust client-provided parameters (like `requestorId`) for authorization checks. Spring Security's context must be used to identify the current user. Also, endpoints returning user entities should be careful not to serialize sensitive fields unless explicitly required for the current user.
**Prevention:** Use `org.springframework.security.core.Authentication` injected into controller methods to identify the user making the request. Apply role/ownership checks server-side. Map Entities to DTOs to prevent accidental exposure of fields like `password` or `mail`.

## 2025-05-24 - [CRITICAL] Prevented IDOR via API payload masking for Profile Images
**Vulnerability:** The API returned the raw profile picture URL before the 5-day privacy window, relying on the frontend to blur it. Attackers could intercept the API response to bypass the blur.
**Learning:** Never trust the frontend to mask sensitive data. Masking must occur on the backend before the response is sent to the client.
**Prevention:** Enforce server-side logic to replace sensitive URLs with generic placeholders until privacy conditions (e.g., match time elapsed) are met.
