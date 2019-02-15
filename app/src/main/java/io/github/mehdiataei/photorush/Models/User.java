package io.github.mehdiataei.photorush.Models;


public class User {

    private String user_id;
    private String email;
    private String username;
    private String bio;

    public User(String user_id, String email, String username, String bio) {
        this.user_id = user_id;
        this.email = email;
        this.username = username;
        this.bio = bio;
    }

    public User() {

    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getBio() {
        return bio;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }


    @Override
    public String toString() {
        return "User{" +
                "user_id='" + user_id + '\'' +
                ", email='" + email + '\'' +
                ", username='" + username + '\'' +
                '}';
    }
}