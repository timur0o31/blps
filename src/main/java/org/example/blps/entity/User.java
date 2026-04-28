package org.example.blps.entity;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Getter;
import lombok.Setter;
import org.example.blps.enums.Role;

@Getter
@Setter
@JacksonXmlRootElement(localName = "user")
public class User {
    @JacksonXmlProperty(isAttribute = true, localName = "id")
    private Long id;

    @JacksonXmlProperty(localName = "name")
    private String name;

    @JacksonXmlProperty(localName = "surname")
    private String surname;

    @JacksonXmlProperty(localName = "email")
    private String email;

    @JacksonXmlProperty(localName = "password")
    private String password;

    @JacksonXmlProperty(localName = "phone_number")
    private String phoneNumber;

    @JacksonXmlProperty(localName = "role")
    private Role role;
}
