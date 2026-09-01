package org.example.blps.service;

import org.example.blps.entity.User;
import org.example.blps.enums.Role;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.Map;

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
    private void createCamundaUser(User user, String rawPassword, String camundaUserId) {
        Map<String, Object> profile = new HashMap<>();
        profile.put("id", camundaUserId);
        profile.put("firstName", user.getName());
        profile.put("lastName", user.getSurname());
        profile.put("email", user.getEmail());

        Map<String, Object> credentials = new HashMap<>();
        credentials.put("password", rawPassword);

        Map<String, Object> request = new HashMap<>();
        request.put("profile", profile);
        request.put("credentials", credentials);

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