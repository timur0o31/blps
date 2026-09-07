package org.example.blps.service;

import org.example.blps.CamundaRequestProperties.CamundaCreateUserRequest;
import org.example.blps.CamundaRequestProperties.CamundaCredentials;
import org.example.blps.CamundaRequestProperties.CamundaUserProfile;
import org.example.blps.entity.User;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class CamundaIdentityService {

    private final RestClient camundaRestClient;

    public CamundaIdentityService(RestClient camundaRestClient) {
        this.camundaRestClient = camundaRestClient;
    }

    public void createUser(User user, String rawPassword, String groupId) {
        String camundaUserId = "user" + user.getId();
        createCamundaUser(user, rawPassword, camundaUserId);
        addUserToGroup(camundaUserId, groupId);
    }

    private void createCamundaUser(
            User user,
            String rawPassword,
            String camundaUserId) {
            CamundaCreateUserRequest request = new CamundaCreateUserRequest(new CamundaUserProfile(
                                camundaUserId,
                                user.getName(),
                                user.getSurname(),
                                user.getEmail()
                        ),
                        new CamundaCredentials(rawPassword)
                );
        camundaRestClient.post()
                .uri("/user/create")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .toBodilessEntity();
    }

    private void addUserToGroup(String userId, String groupId) {
        camundaRestClient.put()
                .uri("/group/{groupId}/members/{userId}", groupId, userId)
                .retrieve()
                .toBodilessEntity();
    }
}
