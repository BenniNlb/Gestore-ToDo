package model;

import java.util.ArrayList;
import java.util.List;

public class Bacheca {
    private int idBacheca;
    private TitoloBacheca titolo;
    private String descrizione;
    private int idUtente; // A quale utente appartiene
    private List<ToDo> toDos;
    private int posizioneB; // --- MODIFICA: Rinominato in posizioneB ---

    /**
     * Costruttore per una NUOVA bacheca.
     */
    public Bacheca(TitoloBacheca titolo, String descrizione, int idUtente, int posizioneB) { // --- MODIFICA ---
        this.titolo = titolo;
        this.descrizione = descrizione;
        this.idUtente = idUtente;
        this.posizioneB = posizioneB; // --- MODIFICA ---
        this.toDos = new ArrayList<>();
    }

    /**
     * Costruttore per una bacheca LETTA DAL DATABASE (ID esistente).
     * Usato dal DAO per "idratare" l'oggetto.
     */
    public Bacheca(int idBacheca, TitoloBacheca titolo, String descrizione, int idUtente, int posizioneB) { // --- MODIFICA ---
        this.idBacheca = idBacheca;
        this.titolo = titolo;
        this.descrizione = descrizione;
        this.idUtente = idUtente;
        this.posizioneB = posizioneB; // --- MODIFICA ---
        this.toDos = new ArrayList<>();
    }

    // --- Metodi Getter ---

    public int getIdBacheca() {
        return idBacheca;
    }

    public TitoloBacheca getTitolo() {
        return titolo;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public int getIdUtente() {
        return idUtente;
    }

    public List<ToDo> getToDos() {
        return toDos;
    }

    // --- MODIFICA: Metodi GET/SET per posizioneB ---
    public int getPosizioneB() {
        return posizioneB;
    }

    public void setPosizioneB(int posizioneB) {
        this.posizioneB = posizioneB;
    }
    // --- FINE MODIFICA ---

    // --- Metodi Setter (Richiesti dai DAO) ---

    public void setId(int idBacheca) {
        this.idBacheca = idBacheca;
    }

    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }

    /**
     * Usato dal DAO per "popolare" la bacheca con i suoi ToDo.
     */
    public void setToDos(List<ToDo> toDos) {
        this.toDos = toDos;
    }

    // Metodi di logica (invariati)
    public void aggiungiToDo(ToDo todo) {
        toDos.add(todo);
    }

    public void rimuoviToDo(ToDo todo) {
        toDos.remove(todo);
    }

    public void svuotaToDos() {
        this.toDos.clear();
    }
}