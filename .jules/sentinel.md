## 2026-05-02 - [CRITICAL] Prevented IDOR and Data Exposure in User Profile Endpoint
**Vulnerability:** The `/user/{id}` endpoint allowed unauthenticated arbitrary data exposure and IDOR. It relied on an optional `requestorId` parameter instead of the Spring Security `Authentication` context, meaning any authenticated user could fetch the full profile (including email, password hash, etc.) of any other user simply by calling the endpoint.
**Learning:** Never trust client-provided parameters (like `requestorId`) for authorization checks. Spring Security's context must be used to identify the current user. Also, endpoints returning user entities should be careful not to serialize sensitive fields unless explicitly required for the current user.
**Prevention:** Use `org.springframework.security.core.Authentication` injected into controller methods to identify the user making the request. Apply role/ownership checks server-side. Map Entities to DTOs to prevent accidental exposure of fields like `password` or `mail`.

## 2026-09-14 - IDOR in Profile Picture Fetching
**Vulnerability:** The /user/{id} endpoint exposed the original profile picture URLs to matches before the 5-day period was met, causing an IDOR.
**Learning:** Frontend blurring is insufficient as attackers can strip parameters. Backend logic must enforce data masking.
**Prevention:** Return a hardcoded placeholder URL when matchTime is null or under 5 days, failing closed.
