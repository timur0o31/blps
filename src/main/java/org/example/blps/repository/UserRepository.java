package org.example.blps.repository;
import org.example.blps.entity.User;
import org.example.blps.service.XmlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


import java.io.IOException;
import java.util.Optional;

@Component
public class UserRepository {

    private final XmlService xmlService;

    @Autowired
    public UserRepository(XmlService xmlService) {
        this.xmlService = xmlService;
    }

    public boolean existsByEmail(String email) {
        return xmlService.isExistsByEmailInXmlFile(email);
    }

    public boolean existsByPhoneNumber(String phoneNumber) {
        return xmlService.isExistByPhoneNumberInXmlFile(phoneNumber);
    }

    public void saveUser(User user) throws IOException {
        xmlService.save(user);
    }

    public Optional<User> findByEmail(String email) {
        return xmlService.findByEmailInXmlFile(email);
    }
    public Optional<User> findById(Long id){
        return xmlService.findByIdInXmlFile(id);
    }
}
