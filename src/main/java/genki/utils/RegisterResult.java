package genki.utils;

public class RegisterResult {

      private final RegisterStatus status;
      private final String message;

      public RegisterResult(RegisterStatus status, String message) {
          this.status = status;
          this.message = message;
      }

      public RegisterStatus getStatus() {
          return this.status;
      }

      public String getMessage() {
          return this.message;
      }

}
