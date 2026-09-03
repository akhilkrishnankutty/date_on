#!/bin/bash
cat src/main/java/com/example/dateon/Controllers/UserController.java | grep -n "public ResponseEntity<?> getUser" -A 45
