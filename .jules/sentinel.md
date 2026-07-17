## 2026-05-02 - [CRITICAL] Prevented IDOR and Data Exposure in User Profile Endpoint
**Vulnerability:** The `/user/{id}` endpoint allowed unauthenticated arbitrary data exposure and IDOR. It relied on an optional `requestorId` parameter instead of the Spring Security `Authentication` context, meaning any authenticated user could fetch the full profile (including email, password hash, etc.) of any other user simply by calling the endpoint.
**Learning:** Never trust client-provided parameters (like `requestorId`) for authorization checks. Spring Security's context must be used to identify the current user. Also, endpoints returning user entities should be careful not to serialize sensitive fields unless explicitly required for the current user.
**Prevention:** Use `org.springframework.security.core.Authentication` injected into controller methods to identify the user making the request. Apply role/ownership checks server-side. Map Entities to DTOs to prevent accidental exposure of fields like `password` or `mail`.

## 2026-07-17 - [CRITICAL] Prevented IDOR on Unblurred Profile Pictures
**Vulnerability:** The `getUser` endpoint returned the unblurred `profilePictureUrl` for matched users regardless of the `matchTime`, relying on the frontend to blur it before the 5-day period.
**Learning:** Never trust the frontend to mask sensitive data like unblurred images. A user could inspect network traffic or manipulate API IDs to fetch the raw image URL.
**Prevention:** Apply data masking (such as modifying Cloudinary URLs with a blur transformation) on the backend before the payload is sent to the client.
