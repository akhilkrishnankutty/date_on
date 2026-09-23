## 2026-05-02 - [CRITICAL] Prevented IDOR and Data Exposure in User Profile Endpoint
**Vulnerability:** The `/user/{id}` endpoint allowed unauthenticated arbitrary data exposure and IDOR. It relied on an optional `requestorId` parameter instead of the Spring Security `Authentication` context, meaning any authenticated user could fetch the full profile (including email, password hash, etc.) of any other user simply by calling the endpoint.
**Learning:** Never trust client-provided parameters (like `requestorId`) for authorization checks. Spring Security's context must be used to identify the current user. Also, endpoints returning user entities should be careful not to serialize sensitive fields unless explicitly required for the current user.
**Prevention:** Use `org.springframework.security.core.Authentication` injected into controller methods to identify the user making the request. Apply role/ownership checks server-side. Map Entities to DTOs to prevent accidental exposure of fields like `password` or `mail`.

## 2024-05-20 - Prevent IDOR on Matched User Profile Pictures
**Vulnerability:** Unblurred profile pictures of matches were accessible via direct API ID manipulation before the 5-day chat period by exploiting the UserController.getUser endpoint.
**Learning:** Returning full object graphs for matches without applying backend time-based data masking allows the frontend to bypass business logic rules.
**Prevention:** Always fail closed and enforce data masking (like returning a generic placeholder URL) directly in the backend controller.
