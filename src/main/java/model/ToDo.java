package model;

import java.awt.Color;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.ImageIcon;

public class ToDo {
    private int idToDo;
    private String titolo;
    private LocalDate dataScadenza;
    private Color coloreSfondo;
    private List<String> linkURLs;
    private String descrizione;
    private ImageIcon immagine;
    private boolean stato; // true = completato
    private int posizione;

    private int idBacheca; // A quale bacheca appartiene
    private int idUtenteCreatore; // Chi ha creato il ToDo

    // --- MODIFICA CHIAVE ---
    // Sostituiamo la Lista con una Mappa per i permessi
    private Map<Utente, PermessoCondivisione> condivisioni;
    // --- FINE MODIFICA ---


    /**
     * Costruttore per un NUOVO ToDo.
     */
    public ToDo(String titolo, int idBacheca, int idUtenteCreatore) {
        this.titolo = titolo;
        this.idBacheca = idBacheca;
        this.idUtenteCreatore = idUtenteCreatore;
        this.stato = false;
        // Inizializza le nuove strutture dati
        this.condivisioni = new HashMap<>();
        this.linkURLs = new ArrayList<>();
    }

    /**
     * Costruttore per un ToDo LETTO DAL DATABASE (ID esistente).
     * Usato dal DAO per "idratare" l'oggetto.
     */
    public ToDo(int idToDo, String titolo, String descrizione, LocalDate dataScadenza,
                Color coloreSfondo, boolean stato, int posizione,
                int idBacheca, int idUtenteCreatore) {
        this.idToDo = idToDo;
        this.titolo = titolo;
        this.descrizione = descrizione;
        this.dataScadenza = dataScadenza;
        this.coloreSfondo = coloreSfondo;
        this.stato = stato;
        this.posizione = posizione;
        this.idBacheca = idBacheca;
        this.idUtenteCreatore = idUtenteCreatore;
        // Inizializza le nuove strutture dati
        this.condivisioni = new HashMap<>();
        this.linkURLs = new ArrayList<>();
    }

    // --- Getters e Setters (Necessari per il DAO e la UI) ---

    public int getIdToDo() {
        return idToDo;
    }

    public void setId(int idToDo) {
        this.idToDo = idToDo;
    }

    public String getTitolo() {
        return titolo;
    }

    public void setTitolo(String titolo) {
        this.titolo = titolo;
    }

    public LocalDate getDataScadenza() {
        return dataScadenza;
    }

    public void setDataScadenza(LocalDate dataScadenza) {
        this.dataScadenza = dataScadenza;
    }

    public Color getColoreSfondo() {
        return coloreSfondo;
    }

    public void setColoreSfondo(Color coloreSfondo) {
        this.coloreSfondo = coloreSfondo;
    }

    public List<String> getLinkURLs() {
        return linkURLs;
    }

    public void setLinkURLs(List<String> linkURLs) {
        this.linkURLs = linkURLs;
    }

    public void aggiungiLink(String link) {
        if (this.linkURLs == null) this.linkURLs = new ArrayList<>();
        this.linkURLs.add(link);
    }

    public void setLinksDalDB(List<String> links) {
        this.linkURLs = links;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }

    public ImageIcon getImmagine() {
        return immagine;
    }

    public void setImmagine(ImageIcon immagine) {
        this.immagine = immagine;
    }

    public boolean isCompletato() {
        return stato;
    }

    public void setCompletato(boolean stato) {
        this.stato = stato;
    }

    public int getPosizione() {
        return posizione;
    }

    public void setPosizione(int posizione) {
        this.posizione = posizione;
    }

    public int getIdBacheca() {
        return idBacheca;
    }

    public void setIdBacheca(int idBacheca) {
        this.idBacheca = idBacheca;
    }

    public int getIdUtenteCreatore() {
        return idUtenteCreatore;
    }

    public void setIdUtenteCreatore(int idUtenteCreatore) {
        this.idUtenteCreatore = idUtenteCreatore;
    }


    // --- METODI DI CONDIVISIONE AGGIORNATI ---

    /**
     * Ritorna la mappa di utenti e i loro permessi.
     */
    public Map<Utente, PermessoCondivisione> getCondivisioni() {
        return condivisioni;
    }

    /**
     * Usato dal DAO per popolare la mappa delle condivisioni lette dal DB.
     */
    public void setCondivisioniDalDB(Map<Utente, PermessoCondivisione> mappaPermessi) {
        this.condivisioni = mappaPermessi;
    }

    /**
     * Aggiunge o aggiorna una condivisione per un utente.
     */
    public void aggiungiOModificaCondivisione(Utente utente, PermessoCondivisione permesso) {
        condivisioni.put(utente, permesso);
    }

    /**
     * Rimuove una condivisione per un utente.
     */
    public void rimuoviCondivisione(Utente utente) {
        condivisioni.remove(utente);
    }

    /**
     * Metodo helper per ottenere il permesso di un utente specifico.
     * @param utente L'utente da controllare.
     * @return Il PermessoCondivisione (MODIFICA, SOLO_LETTURA) o null se non è condiviso.
     */
    public PermessoCondivisione getPermessoPerUtente(Utente utente) {
        if (utente == null) return null;

        // Cerca l'utente nella mappa per ID,
        // perché gli oggetti Utente potrebbero essere istanze diverse
        for (Utente u : condivisioni.keySet()) {
            if (u.getIdUtente() == utente.getIdUtente()) {
                return condivisioni.get(u);
            }
        }
        return null; // Non trovato
    }
}