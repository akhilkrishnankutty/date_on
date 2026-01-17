$baseUrl = "http://localhost:8080"
$randomNum = Get-Random -Minimum 1000 -Maximum 9999
$dummyUser = @{
    name = "Dummy User $randomNum"
    mail = "dummy$randomNum@example.com"
    password = "password"
    age = 25
    gender = "Female"
    status = "REGISTERED"
}

# 1. Create User
try {
    Write-Host "Creating dummy user..."
    $jsonUser = $dummyUser | ConvertTo-Json
    $response = Invoke-RestMethod -Uri "$baseUrl/user/create" -Method Post -Body $jsonUser -ContentType "application/json"
    $userId = $response.id
    Write-Host "Created user with ID: $userId"
} catch {
    Write-Host "Error creating user: $_"
    exit
}

# 2. Complete Profile (Triggers Matcher)
try {
    Write-Host "Completing profile for user $userId..."
    $completeUrl = "$baseUrl/user/$userId/complete"
    # The complete endpoint might be under /user/{id}/complete or /users/{id}/complete depending on Controller mapping.
    # UserController map is "user", method is "/{userId}/complete", so "/user/{userId}/complete"
    # BUT wait, the permission change was "/users/**" but controller is "user".
    # Checking UserController: @RequestMapping("user")
    # Checking QuestionController: @RequestMapping("/users/{userId}/questions")
    # Checking SecurityConfig: permitted "/users/**" AND "/user/create", "/user/login"
    # Uh oh. "/user/{id}/complete" might NOT be permitted if I only permitted "/users/**"?
    # Let's check SecurityConfig again. It permitted "/user/create", "/user/login".
    # It does NOT permit "/user/{id}/complete".
    # I should update SecurityConfig to permit "/user/**" instead of just create/login if I want seamless access without auth token for this flow.
    # However, let's try calling it. If 401, I know why.
    
    Invoke-RestMethod -Uri $completeUrl -Method Post
    Write-Host "Profile completed. User should be in matching queue."
} catch {
    Write-Host "Error completing profile: $_"
    if ($_.Exception.Response.StatusCode -eq [System.Net.HttpStatusCode]::Unauthorized) {
        Write-Host "Caught 401 Unauthorized. This is expected if specific endpoint isn't whitelisted."
    }
}
