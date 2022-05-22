package classes;

import java.sql.Timestamp;

public class User {

    private String name;
    private String password;
    private Timestamp timestamp;

    public User(String name, String password, Timestamp timestamp) {
        this.name = name;
        this.password = password;
        this.timestamp = timestamp;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Timestamp getTimestamp() {
        return this.timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public String toString() {
        return String.format("*User Info* Name: %s Password: %s Create Time: %s", name, password, timestamp);
    }
}
