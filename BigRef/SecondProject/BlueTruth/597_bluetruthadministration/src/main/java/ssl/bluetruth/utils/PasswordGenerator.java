/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ssl.bluetruth.utils;

import java.util.Random;

/**
 *
 * @author svenkataramanappa, wingc
 */
public class PasswordGenerator {

    private static final String PASSWORD_SETS = getPasswordSets();

    public static String generate(int numberOfDigits) {
        String password = "";
        Random rg = new Random();
        for (int i = 0; i < numberOfDigits; i++) {
            password += PASSWORD_SETS.charAt(rg.nextInt(PASSWORD_SETS.length()));
        }
        return password;
    }

    private static String getPasswordSets() {
        String passwordSets = "";
        for (int i = 0; i < 26; i++) {
            char c = (char) ('A' + i);
            passwordSets += c;
        }
        for (int i = 0; i < 26; i++) {
            char c = (char) ('a' + i);
            passwordSets += c;
        }
        for (int i = 0; i < 10; i++) {
            char c = (char) ('0' + i);
            passwordSets += c;
        }
        return passwordSets;
    }

}
