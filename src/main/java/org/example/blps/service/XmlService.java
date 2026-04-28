package org.example.blps.service;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.example.blps.entity.User;
import org.example.blps.entity.UsersWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class XmlService {
    private static final String USERS_FILE = "src/main/resources/users.xml";
    private final XmlMapper xmlMapper;

    @Autowired
    public XmlService(XmlMapper xmlMapper) {
        this.xmlMapper = xmlMapper;
    }

    public List<User> readUsers() {
        try {
            UsersWrapper usersWrapper = xmlMapper.readValue(new File(USERS_FILE), UsersWrapper.class);
            if (usersWrapper.getUsers() != null) {
                return usersWrapper.getUsers();
            } else {
                return new ArrayList<>();
            }
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    public Optional<User> findByEmailInXmlFile(String email) {
        return readUsers().stream().filter(u ->u.getEmail().equals(email)).findFirst();
    }

    private void saveUsers(List<User> users) throws IOException {
        UsersWrapper usersWrapper = new UsersWrapper();
        usersWrapper.setUsers(users);
        File file = new File(USERS_FILE);
        xmlMapper.writerWithDefaultPrettyPrinter()
                .writeValue(file, usersWrapper);
    }

    private Long generateNextId(List<User> users) {
        long maxId = 0;
        for (User user : users) {
            if (user.getId() != null && user.getId() > maxId) {
                maxId = user.getId();
            }
        }
        maxId++;
        return maxId;
    }

    public void save(User user) throws IOException {
        List<User> users = readUsers();
        user.setId(generateNextId(users));
        users.add(user);
        saveUsers(users);
    }

    public boolean isExistsByEmailInXmlFile(String email) {
        return readUsers().stream().anyMatch(u -> u.getEmail().equals(email));
    }

    public boolean isExistByPhoneNumberInXmlFile(String phoneNumber) {
        return readUsers().stream().anyMatch(u -> u.getPhoneNumber().equals(phoneNumber));
    }

}
