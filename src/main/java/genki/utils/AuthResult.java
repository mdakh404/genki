package genki.utils;


public class AuthResult {

    private final AuthStatus status;

    public AuthResult(AuthStatus status, String message) {
        this.status = status;
    }

    public AuthStatus getStatus() {
        return this.status;
    }

}
