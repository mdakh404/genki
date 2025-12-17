package genki.utils;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import com.mongodb.client.model.Filters;

public class FindUsers {
	
	private String id;
	private String username;
	private String bio;
	private String role;
	private String photo_url;
	
	private DBConnection DBConnect = new DBConnection("genki_testing");
	private MongoDatabase database = DBConnect.getDatabase();
	private MongoCollection<Document> Users = database.getCollection("users");
	
	public FindUsers(String username) {
		Document user = findUser(username);
		if (user == null) {
			throw new IllegalArgumentException("User not found: " + username);
		}
		this.id = user.getObjectId("_id").toHexString();
		this.username = user.getString("username");
		this.bio = user.getString("bio");
		this.photo_url = user.getString("photo_url");
		this.role = user.getString("role");
	}
	
	
	public Document findUser(String username) {
		Document user = this.Users.find(Filters.eq("username", username)).first();
		if(user == null) {
			//
			return null;
		}
		return user;
		
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


	public String getPhoto_url() {
		return photo_url;
	}


	public void setPhoto_url(String photo_url) {
		this.photo_url = photo_url;
	}


	@Override
	public String toString() {
		return "User [id=" + id + ", username=" + username + ", bio=" + bio + ", role=" + role + ", photo_url="
				+ photo_url + "]";
	}
	
	
	
}
