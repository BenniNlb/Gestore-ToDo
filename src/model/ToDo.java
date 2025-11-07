package model;

import java.awt.Color;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.swing.ImageIcon;

// La classe ToDo rappresenta un’attività o compito da completare.
// Ora supporta più link (List<String>) e un'immagine allegata (ImageIcon).
public class ToDo {
    private UUID idToDo;
    private String titolo;
    private LocalDate dataScadenza;
    private Color coloreSfondo;
    // lista di link (ora supportiamo più link)
    private List<String> linkURLs;
    private String descrizione;
    private ImageIcon immagine;
    private boolean stato;
    private int posizione;

    private List<Utente> condivisoCon;

    // Costruttore minimale
    public ToDo(String titolo) {
        this.idToDo = UUID.randomUUID();
        this.titolo = titolo;
        this.stato = false;
        this.condivisoCon = new ArrayList<>();
        this.linkURLs = new ArrayList<>();
    }

    // Getter e setter
    public UUID getIdToDo() {
        return idToDo;
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

    // Link multipli
    public List<String> getLinkURLs() {
        return linkURLs;
    }

    public void setLinkURLs(List<String> linkURLs) {
        this.linkURLs = linkURLs != null ? new ArrayList<>(linkURLs) : new ArrayList<>();
    }

    public void aggiungiLink(String link) {
        if (link == null) return;
        if (this.linkURLs == null) this.linkURLs = new ArrayList<>();
        if (!this.linkURLs.contains(link)) this.linkURLs.add(link);
    }

    public void rimuoviLink(String link) {
        if (this.linkURLs != null) this.linkURLs.remove(link);
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

    public List<Utente> getCondivisoCon() {
        return condivisoCon;
    }

    public void aggiungiCondivisione(Utente utente) {
        if (!condivisoCon.contains(utente)) {
            condivisoCon.add(utente);
        }
    }

    public void rimuoviCondivisione(Utente utente) {
        condivisoCon.remove(utente);
    }
}
