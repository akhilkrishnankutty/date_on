## 2026-05-02 - [CRITICAL] Prevented IDOR and Data Exposure in User Profile Endpoint
**Vulnerability:** The `/user/{id}` endpoint allowed unauthenticated arbitrary data exposure and IDOR. It relied on an optional `requestorId` parameter instead of the Spring Security `Authentication` context, meaning any authenticated user could fetch the full profile (including email, password hash, etc.) of any other user simply by calling the endpoint.
**Learning:** Never trust client-provided parameters (like `requestorId`) for authorization checks. Spring Security's context must be used to identify the current user. Also, endpoints returning user entities should be careful not to serialize sensitive fields unless explicitly required for the current user.
**Prevention:** Use `org.springframework.security.core.Authentication` injected into controller methods to identify the user making the request. Apply role/ownership checks server-side. Map Entities to DTOs to prevent accidental exposure of fields like `password` or `mail`.

## 2024-05-24 - [CRITICAL] Removed Hardcoded Credentials
**Vulnerability:** Found hardcoded PostgreSQL passwords, Cloudinary API keys, and JWT secrets directly in application.properties.
**Learning:** Hardcoding secrets inside source control makes them vulnerable to exposure if the code is leaked or accessed by unauthorized individuals.
**Prevention:** Always use environment variable placeholders (e.g. `${DB_PASSWORD:}`) in configuration files. Provide dummy values only in strictly local/test profiles.
