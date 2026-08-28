#!/bin/bash
./mvnw clean test -Dtest="EndpointIntegrationTests#testGetUserEndpoint" -Dspring.profiles.active=test
