package genki.utils;

public class RegisterResult {

      private final RegisterStatus status;
      private String username;
      private String userId;
      private String userRole;

      public RegisterResult(RegisterStatus status) {
          this.status = status;
      }

      public RegisterResult(RegisterStatus status, String username, String userId, String userRole) {
          this.status = status;
          this.username = username;
          this.userId = userId;
          this.userRole = userRole;
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

      public RegisterStatus getStatus() {
          return this.status;
      }




}
