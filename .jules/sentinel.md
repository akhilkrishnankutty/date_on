## 2026-05-02 - [CRITICAL] Prevented IDOR and Data Exposure in User Profile Endpoint
**Vulnerability:** The `/user/{id}` endpoint allowed unauthenticated arbitrary data exposure and IDOR. It relied on an optional `requestorId` parameter instead of the Spring Security `Authentication` context, meaning any authenticated user could fetch the full profile (including email, password hash, etc.) of any other user simply by calling the endpoint.
**Learning:** Never trust client-provided parameters (like `requestorId`) for authorization checks. Spring Security's context must be used to identify the current user. Also, endpoints returning user entities should be careful not to serialize sensitive fields unless explicitly required for the current user.
**Prevention:** Use `org.springframework.security.core.Authentication` injected into controller methods to identify the user making the request. Apply role/ownership checks server-side. Map Entities to DTOs to prevent accidental exposure of fields like `password` or `mail`.

## 2024-05-24 - [CRITICAL] Prevented IDOR via Profile Picture Exposure
**Vulnerability:** The `/user/{id}` endpoint exposed the unblurred `profilePictureUrl` to matched users before the 5-day waiting period expired.
**Learning:** Never trust the frontend to mask or hide sensitive data. Injecting Cloudinary transformations is not enough, as attackers can easily strip the parameter to access the original image.
**Prevention:** Implement backend-only blurring by returning a hardcoded generic blurred placeholder URL until the time restriction is met.
