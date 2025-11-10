package dao;

import model.Utente;
import java.util.List;

public interface UtenteDAO {

    void addUtente(Utente utente);

    Utente getUtenteById(int id);

    Utente getUtenteByUsername(String username);

    List<Utente> getAllUtenti();

    void updateUtente(Utente utente);

    void deleteUtenteById(int id);

    /**
     * Controlla se un username è già in uso.
     * @param username L'username da controllare.
     * @return true se l'utente esiste, false altrimenti.
     */
    boolean utenteEsiste(String username);
}