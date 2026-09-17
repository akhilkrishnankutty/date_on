## 2026-05-02 - [CRITICAL] Prevented IDOR and Data Exposure in User Profile Endpoint
**Vulnerability:** The `/user/{id}` endpoint allowed unauthenticated arbitrary data exposure and IDOR. It relied on an optional `requestorId` parameter instead of the Spring Security `Authentication` context, meaning any authenticated user could fetch the full profile (including email, password hash, etc.) of any other user simply by calling the endpoint.
**Learning:** Never trust client-provided parameters (like `requestorId`) for authorization checks. Spring Security's context must be used to identify the current user. Also, endpoints returning user entities should be careful not to serialize sensitive fields unless explicitly required for the current user.
**Prevention:** Use `org.springframework.security.core.Authentication` injected into controller methods to identify the user making the request. Apply role/ownership checks server-side. Map Entities to DTOs to prevent accidental exposure of fields like `password` or `mail`.

## 2026-09-17 - [CRITICAL] Prevented Profile Picture IDOR in UserController
**Vulnerability:** Found lack of backend profile picture blurring based on match time, allowing direct object reference to unblurred images.
**Learning:** Backend-only blurring via a generic placeholder URL is necessary to prevent client-side parameter manipulation (like stripping e_blur) and unauthorized image access.
**Prevention:** Use a hardcoded, realistic generic placeholder blurred image URL for premature requests instead of exposing the original URL or appending client-side transform parameters.
