package genki.utils;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * CredsValidator class contains two methods to validate registration info (username, password)
 * validateUser() validates username
 * validatePass() validates password
 */
public class CredsValidator {

    /**
     * validateUser() validates userName against a USERNAME_REGEX using Pattern and Matcher classes
     * @param userName is the username that is provided during the registration process
     * @return a boolean value, true if the userName matches the USERNAME_REGEX pattern, false otherwise
     */
    public static boolean validateUser(String userName) {

           /*
            - minimum of 5 characters, maximum of 15 characters, first letter starts with
            - alphanumerical characters, alphanumerical and underscores are allowed
            */
           final String USERNAME_REGEX = "^[a-zA-Z0-9][a-zA-Z0-9_]{4,14}$";

           Pattern pattern = Pattern.compile(USERNAME_REGEX);
           Matcher matcher = pattern.matcher(userName);

           return matcher.matches();
    }

    /**
     * validatePass() validates password against a PASSWORD_REGEX using Pattern and Matcher classes
     * @param password is provided during the login/registration process
     * @return a boolean value, true if the password matches the PASSWORD_REGEX pattern, false otherwise
     */
    public static boolean validatePass(String password) {

        /* Genki password policy:
         * minimum of 8 characters, maximum of 20 characters
         * password contains at least 1 capital letter, one symbol and one digit
         */
        final String PASSWORD_REGEX = "^(?=.*[A-Z])(?=.*\\d)(?=.*[^a-zA-Z0-9\\s]).{8,20}$";

        Pattern pattern = Pattern.compile(PASSWORD_REGEX);
        Matcher matcher = pattern.matcher(password);

        return matcher.matches();

    }
}
