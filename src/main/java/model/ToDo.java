package model;

import java.awt.Color;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.swing.ImageIcon;
// Rimosso import java.util.UUID;

public class ToDo {
    private int idToDo;
    private String titolo;
    private LocalDate dataScadenza;
    private Color coloreSfondo;
    private List<String> linkURLs; // Lo teniamo, ma il DAO dovrà gestirlo
    private String descrizione;
    private ImageIcon immagine; // Lo teniamo, ma il DAO dovrà gestirlo
    private boolean stato; // true = completato
    private int posizione;

    private int idBacheca; // A quale bacheca appartiene
    private int idUtenteCreatore; // Chi ha creato il ToDo
    private List<Utente> condivisoCon; // Chi altro può vederlo

    /**
     * Costruttore per un NUOVO ToDo.
     */
    public ToDo(String titolo, int idBacheca, int idUtenteCreatore) {
        this.titolo = titolo;
        this.idBacheca = idBacheca;
        this.idUtenteCreatore = idUtenteCreatore;
        this.stato = false;
        this.condivisoCon = new ArrayList<>();
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
        this.condivisoCon = new ArrayList<>();
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

    public List<Utente> getCondivisoCon() {
        return condivisoCon;
    }

    public void setCondivisoConDalDB(List<Utente> utenti) {
        this.condivisoCon = utenti;
    }

    public void aggiungiCondivisione(Utente utente) {
        if (!condivisoCon.contains(utente)) {
            condivisoCon.add(utente);
        }
    }
}