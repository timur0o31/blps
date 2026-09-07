package org.example.blps.CamundaRequestProperties;

public record CamundaCreateUserRequest(CamundaUserProfile profile, CamundaCredentials credentials) {
}