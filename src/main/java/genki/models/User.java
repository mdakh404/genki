package genki.models;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import com.google.gson.annotations.Expose;

public class User {
    @Expose
    private String id;
    @Expose
    private String username;
    @Expose
    private String password;
    @Expose
    private String bio;
    @Expose
    private String role;
    @Expose
    private String photoUrl;
    @Expose
    private LocalDateTime createdAt;
    @Expose
    private List<String> friends;
    @Expose(serialize = false, deserialize = false)
    private ArrayList<Group> groups;

    public User() {}

    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.createdAt = LocalDateTime.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<String> getFriends() {
        return friends;
    }

    public void setFriends(List<String> friends) {
        this.friends = friends;
    }

	@Override
	public String toString() {
		return "User [id=" + id + ", username=" + username + ", password=" + password + ", bio=" + bio + ", role="
				+ role + ", photoUrl=" + photoUrl + ", createdAt=" + createdAt + ", friends=" + friends + ", groups="
				+ groups + "]";
	}
}
