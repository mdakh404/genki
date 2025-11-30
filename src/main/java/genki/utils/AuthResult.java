package genki.utils;


public class AuthResult {

    private final AuthStatus status;
    private final String message;

    public AuthResult(AuthStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    public AuthStatus getStatus() {
        return this.status;
    }

    public String getMessage() {
        return this.message;
    }
}
