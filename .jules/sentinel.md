## 2026-05-02 - [CRITICAL] Prevented IDOR and Data Exposure in User Profile Endpoint
**Vulnerability:** The `/user/{id}` endpoint allowed unauthenticated arbitrary data exposure and IDOR. It relied on an optional `requestorId` parameter instead of the Spring Security `Authentication` context, meaning any authenticated user could fetch the full profile (including email, password hash, etc.) of any other user simply by calling the endpoint.
**Learning:** Never trust client-provided parameters (like `requestorId`) for authorization checks. Spring Security's context must be used to identify the current user. Also, endpoints returning user entities should be careful not to serialize sensitive fields unless explicitly required for the current user.
**Prevention:** Use `org.springframework.security.core.Authentication` injected into controller methods to identify the user making the request. Apply role/ownership checks server-side. Map Entities to DTOs to prevent accidental exposure of fields like `password` or `mail`.

## 2024-05-02 - [CRITICAL] Prevented IDOR on Match Profile Pictures
**Vulnerability:** The `/user/{id}` endpoint returned unblurred profile picture URLs for matches regardless of how much time had passed since the match, exposing sensitive data to users manipulating the API.
**Learning:** Never trust the frontend to hide sensitive data like unblurred images. The backend must enforce business logic (e.g., 5-day wait period) by modifying the data returned in the DTO or response entity directly (e.g., providing a placeholder URL).
**Prevention:** Enforce server-side checks for time-based access restrictions and fail closed (return placeholder/null) if conditions are not met, before returning the response payload.
