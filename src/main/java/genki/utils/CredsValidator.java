package genki.utils;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class CredsValidator {

    public static boolean validateUser(String userName) {

           final String USERNAME_REGEX = "^[a-zA-Z0-9][a-zA-Z0-9_]{4,14}$";

           Pattern pattern = Pattern.compile(USERNAME_REGEX);
           Matcher matcher = pattern.matcher(userName);

           return matcher.matches();
    }

    public static boolean validatePass(String password) {

        final String PASSWORD_REGEX = "^(?=.*[A-Z])(?=.*\\d)(?=.*[^a-zA-Z0-9\\s]).{8,20}$";

        Pattern pattern = Pattern.compile(PASSWORD_REGEX);
        Matcher matcher = pattern.matcher(password);

        return matcher.matches();

    }
}
