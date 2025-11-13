package dao;

import model.Bacheca;
import model.TitoloBacheca;
import java.util.List;

/** * Interfaccia per la gestione delle operazioni sulle bacheche (Bacheca).
 * Adattata da BoardDAO del professore per usare model.Bacheca.
 */
public interface BachecaDAO {

    void addBacheca(Bacheca bacheca);

    Bacheca getBachecaById(int id);

    List<Bacheca> getAllBacheche();

    void updateBacheca(Bacheca bacheca);

    void deleteBacheca(int id);

    Bacheca getBachecaByTitoloAndUtente(TitoloBacheca titolo, int idUtente);

    List<Bacheca> getBachecheByUtente(int idUtente);

    List<Bacheca> getBachecheByUsername(String username);

    void deleteAllBachecheByUserId(int userId);
}