## 2026-05-02 - [CRITICAL] Prevented IDOR and Data Exposure in User Profile Endpoint
**Vulnerability:** The `/user/{id}` endpoint allowed unauthenticated arbitrary data exposure and IDOR. It relied on an optional `requestorId` parameter instead of the Spring Security `Authentication` context, meaning any authenticated user could fetch the full profile (including email, password hash, etc.) of any other user simply by calling the endpoint.
**Learning:** Never trust client-provided parameters (like `requestorId`) for authorization checks. Spring Security's context must be used to identify the current user. Also, endpoints returning user entities should be careful not to serialize sensitive fields unless explicitly required for the current user.
**Prevention:** Use `org.springframework.security.core.Authentication` injected into controller methods to identify the user making the request. Apply role/ownership checks server-side. Map Entities to DTOs to prevent accidental exposure of fields like `password` or `mail`.
## 2026-09-24 - [CRITICAL] Fixed Hardcoded Cloudinary Credentials
**Vulnerability:** Found hardcoded Cloudinary credentials (cloud_name, api_key, api_secret) in src/main/resources/application.properties.
**Learning:** Hardcoding secrets in application properties files is a critical security risk as it exposes sensitive keys to anyone with access to the source code repository.
**Prevention:** Always use environment variable substitution with safe dummy defaults for sensitive properties in configuration files (e.g., ${CLOUDINARY_API_SECRET:dummy}).
