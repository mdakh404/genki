package genki.utils;

import genki.models.RegisterModel;

import javafx.concurrent.Task;



public class RegistrationTask extends Task<RegisterResult> {

      private final RegisterModel registerModel;
      private final String username;
      private final String password;
      private final String bio;

      public RegistrationTask(RegisterModel registerModel, String username, String password, String bio) {
          this.registerModel = registerModel;
          this.username = username;
          this.password = password;
          this.bio = bio;
      }

      @Override
      protected RegisterResult call() throws Exception {
          return registerModel.Register(username, password, bio);
      }
}
