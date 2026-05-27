## 2026-05-02 - [CRITICAL] Prevented IDOR and Data Exposure in User Profile Endpoint
**Vulnerability:** The `/user/{id}` endpoint allowed unauthenticated arbitrary data exposure and IDOR. It relied on an optional `requestorId` parameter instead of the Spring Security `Authentication` context, meaning any authenticated user could fetch the full profile (including email, password hash, etc.) of any other user simply by calling the endpoint.
**Learning:** Never trust client-provided parameters (like `requestorId`) for authorization checks. Spring Security's context must be used to identify the current user. Also, endpoints returning user entities should be careful not to serialize sensitive fields unless explicitly required for the current user.
**Prevention:** Use `org.springframework.security.core.Authentication` injected into controller methods to identify the user making the request. Apply role/ownership checks server-side. Map Entities to DTOs to prevent accidental exposure of fields like `password` or `mail`.

## 2024-05-18 - [CRITICAL] Prevented Hardcoded Secret Exposure
**Vulnerability:** The `application.properties` file contained hardcoded secrets including the PostgreSQL database password, JWT secret, and real Cloudinary API keys, leading to potential complete data compromise if the repository is ever exposed.
**Learning:** Never commit real secrets to source control, even temporarily or as defaults. These are easily leaked and hard to revoke once committed.
**Prevention:** Use environment variables (e.g. `${DB_PASSWORD:}`) in `application.properties` so secrets must be injected at runtime. For local testing, add dummy or local values to a git-ignored file or a specific profile configuration like `application-local.properties`.
