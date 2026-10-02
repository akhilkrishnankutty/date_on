## 2026-05-02 - [CRITICAL] Prevented IDOR and Data Exposure in User Profile Endpoint
**Vulnerability:** The `/user/{id}` endpoint allowed unauthenticated arbitrary data exposure and IDOR. It relied on an optional `requestorId` parameter instead of the Spring Security `Authentication` context, meaning any authenticated user could fetch the full profile (including email, password hash, etc.) of any other user simply by calling the endpoint.
**Learning:** Never trust client-provided parameters (like `requestorId`) for authorization checks. Spring Security's context must be used to identify the current user. Also, endpoints returning user entities should be careful not to serialize sensitive fields unless explicitly required for the current user.
**Prevention:** Use `org.springframework.security.core.Authentication` injected into controller methods to identify the user making the request. Apply role/ownership checks server-side. Map Entities to DTOs to prevent accidental exposure of fields like `password` or `mail`.

## 2026-06-16 - [CRITICAL] Hardcoded Credentials in application.properties
**Vulnerability:** Real Cloudinary API credentials (cloud_name, api_key, api_secret) and a default database password ("4567") were hardcoded in `src/main/resources/application.properties`. This exposes production or sensitive third-party service credentials to anyone with read access to the repository.
**Learning:** Hardcoding credentials in properties files committed to version control is a critical security risk. Environment variables must be used for all secrets.
**Prevention:** Use environment variable placeholders (e.g., `${CLOUDINARY_API_KEY:dummy}`) with safe dummy fallback values to ensure local execution doesn't break while keeping real secrets entirely out of the codebase.
