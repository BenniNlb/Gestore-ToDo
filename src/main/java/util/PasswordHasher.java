package util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Logger;

/**
 * Classe di utilità per l'hashing delle password usando SHA-256.
 */
public class PasswordHasher {

    private static final Logger LOGGER = Logger.getLogger(PasswordHasher.class.getName());

    private PasswordHasher() {}

    /**
     * Esegue l'hash di una password in chiaro usando SHA-256.
     * @param password La password da criptare.
     * @return La stringa esadecimale dell'hash (64 caratteri).
     */
    public static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder(2 * hash.length);
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            LOGGER.severe("Errore: Algoritmo SHA-256 non trovato.");
            throw new RuntimeException("Errore critico di hashing", e);
        }
    }

    /**
     * Confronta una password in chiaro con un hash salvato.
     * @param plainPassword La password inserita dall'utente.
     * @param hashedPassword L'hash salvato nel database.
     * @return true se le password corrispondono, false altrimenti.
     */
    public static boolean checkPassword(String plainPassword, String hashedPassword) {
        String hashOfPlainPassword = hashPassword(plainPassword);
        return hashOfPlainPassword.equals(hashedPassword);
    }
}