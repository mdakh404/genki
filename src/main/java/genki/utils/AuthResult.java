package genki.utils;


public class AuthResult {

    public final AuthStatus status;
    private String username;
    private String userId;
    private String userRole;
    private String imageUrl;


    public AuthResult(AuthStatus status) {
        this.status = status;
    }

    public AuthResult(AuthStatus status, String username, String userId, String userRole, String imageUrl) {
        this.status = status;
        this.username = username;
        this.userId = userId;
        this.userRole = userRole;
        this.imageUrl = imageUrl;
    }


    public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}

	public String getUsername() {
        return this.username;
    }

    public String getUserId() {
        return this.userId;
    }


    public String getUserRole() {
        return this.userRole;
    }


    public AuthStatus getStatus() {
        return this.status;
    }


}
