$ErrorActionPreference = "Stop"

# DATA
$json = '{"mail": "testuser@example.com", "password": "password123", "name": "Test User", "age": 25, "gender": "male", "compatibilityScore": 0.0, "lock": false}'

# 1. Test Create User (Should be allowed)
Write-Host "1. Testing Create User Endpoint..."
try {
    $response = Invoke-RestMethod -Uri "http://localhost:8080/user/create" -Method Post -Body $json -ContentType "application/json"
    Write-Host "Success: User created."
    Write-Host $response
} catch {
    Write-Host "Failed: $_"
    exit 1
}

# 2. Test Protected Endpoint (Should fail with 401/403 or redirect to login)
Write-Host "`n2. Testing Protected Endpoint (Unauthenticated)..."
try {
    Invoke-RestMethod -Uri "http://localhost:8080/users/1/questions" -Method Post -Body '{}' -ContentType "application/json"
    Write-Host "Failed: Should have been denied."
} catch {
    $statusCode = $_.Exception.Response.StatusCode.value__
    if ($statusCode -eq 401 -or $statusCode -eq 403) {
        Write-Host "Success: Access denied as expected (Status: $statusCode)."
    } else {
         # Spring Security default might redirect to login page (302) if formLogin is enabled and no explicit exception handling for REST
         # But Invoke-RestMethod might follow redirects.
         Write-Host "Caught exception: $_" 
    }
}
