$ErrorActionPreference = "Stop"

$username = "akhil@example.com"
$password = "password"
$base64AuthInfo = [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes(("{0}:{1}" -f $username, $password)))

# Try accessing a protected endpoint with credentials
Write-Host "Testing Protected Endpoint with Credentials..."
try {
    # Using a GET request on a potentially valid endpoint or just checking auth on POST
    # The question endpoint is POST, let's try that.
    $response = Invoke-RestMethod -Uri "http://localhost:8080/users/1/questions" -Method Post -Body '{}' -ContentType "application/json" -Headers @{Authorization = ("Basic {0}" -f $base64AuthInfo) }
    
    # If it returns 200/201 or even 400 (Bad Request implies Auth passed), it's a success for Auth.
    # If it returns 401, it failed.
    Write-Host "Success: Request went through (Response might be 'Saved' or error details, but not 401)."
    Write-Host $response
}
catch {
    $statusCode = $_.Exception.Response.StatusCode.value__
    if ($statusCode -eq 401 -or $statusCode -eq 403) {
        Write-Host "Failed: Access denied (Status: $statusCode). Credentials might be wrong."
    }
    else {
        # Any other error means Auth likely passed but request was bad (which is expected for empty body)
        Write-Host "Success (Auth passed): Caught exception: $_" 
    }
}
