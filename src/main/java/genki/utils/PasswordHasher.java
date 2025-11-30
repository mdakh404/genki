package genki.utils;

import org.mindrot.jbcrypt.BCrypt;

/**
 * PasswordHasher.hashPassword() encrypts plain text passwords using BCrypt
 * PasswordHasher.checkPassword() checks the provided plain text password against the hashed password
 */

public class PasswordHasher {

    /**
     *
     * @param plainPassword is the plain text password provided by the user during login
     * @return the hashed password using the BCrypt algorithm
     */
    public static String hashPassword(String plainPassword) {

        return BCrypt.hashpw(plainPassword, BCrypt.gensalt());
    }

    /**
     *
     * @param plainPassword is the plain text password provided by the user during login
     * @param hashedPassword is the hashed password retrieved from the database
     * @return a boolean value: true if the two passwords match, else otherwise
     */
    public static boolean checkPassword(String plainPassword, String hashedPassword) {
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }

}
