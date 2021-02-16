package nz.xinsolutions.jwt.models;

import java.util.List;

/**
 * Author: Marnix Kok <marnix@xinsolutions.co.nz>
 *
 * Purpose:
 *
 *      To contain information about a Hippo user that will end up in the
 *      JWT claims section.
 */
public class JwtUserInfo {

    private String username;
    private List<String> groups;
    private String email;
    private String firstName;
    private String lastName;
    private boolean active;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setGroups(List<String> groups) {
        this.groups = groups;
    }

    public List<String> getGroups() {
        return groups;
    }

    @Override
    public String toString() {
        return "JwtUserInfo{" +
            "username='" + username + '\'' +
            ", groups=" + groups +
            '}';
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isActive() {
        return active;
    }
}
