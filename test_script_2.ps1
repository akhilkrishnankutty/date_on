$ErrorActionPreference = "Stop"

# 1. Test Protected Endpoint (Should fail with 401 or 403)
Write-Host "1. Testing Protected Endpoint (Unauthenticated)..."
try {
    Invoke-RestMethod -Uri "http://localhost:8080/users/1/questions" -Method Post -Body '{}' -ContentType "application/json"
    Write-Host "Failed: Should have been denied."
}
catch {
    $statusCode = $_.Exception.Response.StatusCode.value__
    if ($statusCode -eq 401 -or $statusCode -eq 403) {
        Write-Host "Success: Access denied as expected (Status: $statusCode)."
    }
    else {
        Write-Host "Caught exception: $_" 
    }
}

# 2. Test Create User (Might hang if Kafka is down)
$json = '{"mail": "testuser_sec@example.com", "password": "password123", "name": "Test User Sec", "age": 25, "gender": "male", "compatibilityScore": 0.0, "lock": false}'
Write-Host "`n2. Testing Create User Endpoint..."
try {
    # Set a timeout for this request
    # Powershell Invoke-RestMethod doesn't have simple timeout param in older versions, but we can try.
    $response = Invoke-RestMethod -Uri "http://localhost:8080/user/create" -Method Post -Body $json -ContentType "application/json" -TimeoutSec 5
    Write-Host "Success: User created."
    Write-Host $response
}
catch {
    Write-Host "Failed or Timed out: $_"
}
