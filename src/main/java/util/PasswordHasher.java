package util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Logger;

/**
 * Classe di utilità responsabile della sicurezza delle password tramite crittografia one-way (hashing).
 * <p>
 * Utilizza l'algoritmo <b>SHA-256</b> (Secure Hash Algorithm 256-bit) per trasformare le password
 * in chiaro in una stringa di digest esadecimale di lunghezza fissa.
 * <p>
 * Questa classe è definita come <i>utility class</i>: possiede un costruttore privato
 * e tutti i suoi metodi sono statici.
 */
public class PasswordHasher {

    /**
     * Logger per la registrazione di eventuali errori critici di sistema (es. algoritmo non trovato).
     */
    private static final Logger LOGGER = Logger.getLogger(PasswordHasher.class.getName());

    /**
     * Costruttore privato per impedire l'istanziazione della classe.
     * I metodi di questa classe sono statici e devono essere acceduti direttamente.
     */
    private PasswordHasher() {}

    /**
     * Genera un hash SHA-256 per la password fornita.
     * <p>
     * Il metodo converte la password in byte utilizzando la codifica UTF-8,
     * calcola il digest crittografico e lo converte in una rappresentazione
     * esadecimale leggibile (stringa).
     *
     * @param password La password in chiaro da proteggere.
     * @return Una stringa di 64 caratteri che rappresenta l'hash esadecimale della password.
     * @throws RuntimeException Se l'algoritmo SHA-256 non è disponibile nell'ambiente Java corrente.
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
     * Verifica la validità di una password confrontando la versione in chiaro con un hash memorizzato.
     * <p>
     * Questo metodo esegue l'hashing della password in chiaro inserita dall'utente e controlla
     * se il risultato corrisponde esattamente all'hash salvato nel database.
     *
     * @param plainPassword  La password in chiaro inserita dall'utente (es. durante il login).
     * @param hashedPassword L'hash della password salvato precedentemente nel database.
     * @return {@code true} se l'hash della password inserita corrisponde all'hash salvato, {@code false} altrimenti.
     */
    public static boolean checkPassword(String plainPassword, String hashedPassword) {
        String hashOfPlainPassword = hashPassword(plainPassword);
        return hashOfPlainPassword.equals(hashedPassword);
    }
}