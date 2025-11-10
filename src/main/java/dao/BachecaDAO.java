package dao;

import model.Bacheca; // MODIFICATO: Usa il tuo modello
import model.TitoloBacheca;
import java.util.List;

/** * Interfaccia per la gestione delle operazioni sulle bacheche (Bacheca).
 * Adattata da BoardDAO del professore.
 */
public interface BachecaDAO {

    void addBacheca(Bacheca bacheca); // MODIFICATO: Usa Bacheca

    Bacheca getBachecaById(int id); // MODIFICATO: Usa Bacheca

    List<Bacheca> getAllBacheche(); // MODIFICATO: Usa Bacheca

    void updateBacheca(Bacheca bacheca); // MODIFICATO: Usa Bacheca

    void deleteBacheca(int id);

    Bacheca getBachecaByTitoloAndUtente(TitoloBacheca titolo, int idUtente); // MODIFICATO: Usa TitoloBacheca

    List<Bacheca> getBachecheByUtente(int idUtente); // Rinominato da getBoardsByBachecaId

    List<Bacheca> getBachecheByUsername(String username); // MODIFICATO: Usa Bacheca

    void deleteAllBachecheByUserId(int userId);
}