package Model;

import java.awt.Color;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.swing.ImageIcon;

// La classe ToDo rappresenta un’attività o compito da completare.
// È l’elemento centrale del sistema, ispirato a Trello.
// Ogni ToDo ha informazioni dettagliate e può essere condiviso con più utenti.

public class ToDo {
    private UUID idToDo;
    private String titolo;
    private LocalDate dataScadenza;
    private Color coloreSfondo;
    private String linkURL;
    private String descrizione;
    private ImageIcon immagine;
    private boolean stato;
    private int posizione;

    // Lista degli utenti con cui il ToDo è condiviso
    private List<Utente> condivisoCon;

    // Costruttore minimale, permette di creare un ToDo con il titolo obbligatorio
    public ToDo(String titolo) {
        this.idToDo = UUID.randomUUID();
        this.titolo = titolo;
        this.stato = false;// Di default il ToDo non è completato
        this.condivisoCon = new ArrayList<>();
    }

    // Getter e setter per tutti gli attributi
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

    public String getLinkURL() {
        return linkURL;
    }

    public void setLinkURL(String linkURL) {
        this.linkURL = linkURL;
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

    // Metodo per condividere il ToDo con un nuovo utente
    public void aggiungiCondivisione(Utente utente) {
        if (!condivisoCon.contains(utente)) {
            condivisoCon.add(utente);
        }
    }

    // Metodo per rimuovere la condivisione con un utente
    public void rimuoviCondivisione(Utente utente) {
        condivisoCon.remove(utente);
    }
}


