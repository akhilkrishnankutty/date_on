## 2026-05-02 - [CRITICAL] Prevented IDOR and Data Exposure in User Profile Endpoint
**Vulnerability:** The `/user/{id}` endpoint allowed unauthenticated arbitrary data exposure and IDOR. It relied on an optional `requestorId` parameter instead of the Spring Security `Authentication` context, meaning any authenticated user could fetch the full profile (including email, password hash, etc.) of any other user simply by calling the endpoint.
**Learning:** Never trust client-provided parameters (like `requestorId`) for authorization checks. Spring Security's context must be used to identify the current user. Also, endpoints returning user entities should be careful not to serialize sensitive fields unless explicitly required for the current user.
**Prevention:** Use `org.springframework.security.core.Authentication` injected into controller methods to identify the user making the request. Apply role/ownership checks server-side. Map Entities to DTOs to prevent accidental exposure of fields like `password` or `mail`.

## 2026-05-02 - [CRITICAL] Prevented IDOR on Match Profile Pictures
**Vulnerability:** The `/user/{id}` endpoint returned the unblurred profile picture URL regardless of the match duration. This meant users could bypass the 5-day frontend blurring constraint by calling the API directly.
**Learning:** Never trust the frontend to hide sensitive data or enforce business rules. Attackers can bypass frontend UI and inspect raw API responses.
**Prevention:** Enforce business logic (like time-based blurring or masking) on the backend before returning data to the client.

## 2026-05-02 - [MEDIUM] Removed Hardcoded Cloudinary Placeholders
**Vulnerability:** Hardcoded URLs in logic (e.g., `https://res.cloudinary.com/dquj7szs3/image/upload/v1/blurred_placeholder.jpg`) create technical debt and can easily break or expose internal environment details if the service/account changes.
**Learning:** Hardcoded placeholders should be moved to properties/environment configurations or replaced with an established static internal asset link.
**Prevention:** Inject property values or use relative application paths for fallback image resources.
