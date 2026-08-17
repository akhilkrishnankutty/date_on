## 2026-05-02 - [CRITICAL] Prevented IDOR and Data Exposure in User Profile Endpoint
**Vulnerability:** The `/user/{id}` endpoint allowed unauthenticated arbitrary data exposure and IDOR. It relied on an optional `requestorId` parameter instead of the Spring Security `Authentication` context, meaning any authenticated user could fetch the full profile (including email, password hash, etc.) of any other user simply by calling the endpoint.
**Learning:** Never trust client-provided parameters (like `requestorId`) for authorization checks. Spring Security's context must be used to identify the current user. Also, endpoints returning user entities should be careful not to serialize sensitive fields unless explicitly required for the current user.
**Prevention:** Use `org.springframework.security.core.Authentication` injected into controller methods to identify the user making the request. Apply role/ownership checks server-side. Map Entities to DTOs to prevent accidental exposure of fields like `password` or `mail`.

## 2026-05-02 - [CRITICAL] Prevented IDOR on Match Profile Pictures
**Vulnerability:** The backend exposed the exact unblurred profile picture URL to a newly matched user immediately upon matching. The frontend was trusted to apply a blur effect until 5 days passed. Attackers could bypass this by inspecting the API response to access the original image.
**Learning:** Never rely on the client to mask sensitive data. If business logic dictates data should be hidden for a certain period, the backend must return a completely different payload or a hardcoded placeholder URL.
**Prevention:** Return a generic placeholder URL until the time-based condition (e.g., 5 days post-match) is fully satisfied server-side.
