## 2026-05-02 - [CRITICAL] Prevented IDOR and Data Exposure in User Profile Endpoint
**Vulnerability:** The `/user/{id}` endpoint allowed unauthenticated arbitrary data exposure and IDOR. It relied on an optional `requestorId` parameter instead of the Spring Security `Authentication` context, meaning any authenticated user could fetch the full profile (including email, password hash, etc.) of any other user simply by calling the endpoint.
**Learning:** Never trust client-provided parameters (like `requestorId`) for authorization checks. Spring Security's context must be used to identify the current user. Also, endpoints returning user entities should be careful not to serialize sensitive fields unless explicitly required for the current user.
**Prevention:** Use `org.springframework.security.core.Authentication` injected into controller methods to identify the user making the request. Apply role/ownership checks server-side. Map Entities to DTOs to prevent accidental exposure of fields like `password` or `mail`.

## 2026-08-25 - [CRITICAL] Prevented IDOR in Match Profile Picture URL
**Vulnerability:** Users could fetch the unblurred profile picture of a match before the 5-day period by directly calling the `/user/{id}` endpoint.
**Learning:** Backend-only blurring by injecting Cloudinary transformations is insufficient; a hardcoded blurred placeholder must be returned until the time condition is met to fail securely.
**Prevention:** Always enforce time-based access control rules on sensitive fields server-side before serializing the response, failing closed by default.
