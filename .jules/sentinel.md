## 2026-05-02 - [CRITICAL] Prevented IDOR and Data Exposure in User Profile Endpoint
**Vulnerability:** The `/user/{id}` endpoint allowed unauthenticated arbitrary data exposure and IDOR. It relied on an optional `requestorId` parameter instead of the Spring Security `Authentication` context, meaning any authenticated user could fetch the full profile (including email, password hash, etc.) of any other user simply by calling the endpoint.
**Learning:** Never trust client-provided parameters (like `requestorId`) for authorization checks. Spring Security's context must be used to identify the current user. Also, endpoints returning user entities should be careful not to serialize sensitive fields unless explicitly required for the current user.
**Prevention:** Use `org.springframework.security.core.Authentication` injected into controller methods to identify the user making the request. Apply role/ownership checks server-side. Map Entities to DTOs to prevent accidental exposure of fields like `password` or `mail`.

## 2026-08-13 - [CRITICAL] Prevented IDOR on Match Profile Pictures
**Vulnerability:** The `/user/{id}` endpoint returned the unblurred profile picture URL to matches regardless of the 5-day wait period, trusting the frontend to apply blur transformations. This allowed attackers to extract the original URL and view the image immediately.
**Learning:** Never trust the frontend to mask sensitive data. Cloudinary transformations in the URL can be easily bypassed. The backend must strictly control the data returned.
**Prevention:** Apply a hardcoded blurred placeholder URL on the backend until the time condition (e.g., 5 days) is met. Do not send the original URL with blur parameters.
