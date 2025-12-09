package model;

import java.awt.Color;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.ImageIcon;

/**
 * Rappresenta una singola attività (ToDo) all'interno del sistema.
 * <p>
 * Questa classe è l'entità centrale dell'applicazione e contiene tutte le informazioni
 * relative a un compito, inclusi titolo, descrizione, scadenze, elementi grafici (colore, immagine)
 * e la logica di condivisione con altri utenti.
 */
public class ToDo {

    /**
     * Identificativo univoco dell'attività nel database.
     */
    private int idToDo;

    /**
     * Il titolo breve dell'attività.
     */
    private String titolo;

    /**
     * La data di scadenza prevista per il completamento dell'attività.
     */
    private LocalDate dataScadenza;

    /**
     * Il colore di sfondo utilizzato per visualizzare l'attività nell'interfaccia grafica.
     */
    private Color coloreSfondo;

    /**
     * Lista di link (URL) associati all'attività come risorse esterne.
     */
    private List<String> linkURLs;

    /**
     * Descrizione testuale dettagliata dell'attività.
     */
    private String descrizione;

    /**
     * Immagine opzionale allegata all'attività.
     */
    private ImageIcon immagine;

    /**
     * Stato di completamento dell'attività.
     * {@code true} se completata, {@code false} altrimenti.
     */
    private boolean stato;

    /**
     * Indice che determina la posizione dell'attività all'interno della lista della bacheca.
     */
    private int posizione;

    /**
     * Identificativo della bacheca a cui appartiene l'attività.
     */
    private int idBacheca;

    /**
     * Identificativo dell'utente che ha creato l'attività.
     */
    private int idUtenteCreatore;

    /**
     * Mappa che gestisce le condivisioni dell'attività con altri utenti.
     * Associa un oggetto {@link Utente} al relativo {@link PermessoCondivisione}.
     */
    private Map<Utente, PermessoCondivisione> condivisioni;


    /**
     * Costruisce un nuovo ToDo.
     * <p>
     * Questo costruttore viene utilizzato quando un utente crea una nuova attività
     * nell'interfaccia grafica. L'ID verrà assegnato successivamente dal database.
     *
     * @param titolo           Il titolo dell'attività (obbligatorio).
     * @param idBacheca        L'ID della bacheca in cui il ToDo viene creato.
     * @param idUtenteCreatore L'ID dell'utente che sta creando l'attività.
     */
    public ToDo(String titolo, int idBacheca, int idUtenteCreatore) {
        this.titolo = titolo;
        this.idBacheca = idBacheca;
        this.idUtenteCreatore = idUtenteCreatore;
        this.stato = false;
        this.condivisioni = new HashMap<>();
        this.linkURLs = new ArrayList<>();
    }

    /**
     * Costruisce un ToDo con dati esistenti (ricostruzione dal database).
     * <p>
     * Utilizzato dai DAO per mappare i record del database in oggetti Java.
     *
     * @param idToDo           L'identificativo univoco del ToDo.
     * @param titolo           Il titolo dell'attività.
     * @param descrizione      La descrizione dettagliata.
     * @param dataScadenza     La data di scadenza.
     * @param coloreSfondo     Il colore di sfondo per la visualizzazione.
     * @param stato            Lo stato di completamento (true se completato).
     * @param posizione        L'indice di ordinamento nella lista.
     * @param idBacheca        L'ID della bacheca di appartenenza.
     * @param idUtenteCreatore L'ID dell'autore.
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
        this.condivisioni = new HashMap<>();
        this.linkURLs = new ArrayList<>();
    }

    // --- Getters e Setters ---

    /**
     * Restituisce l'identificativo univoco del ToDo.
     *
     * @return L'ID del ToDo.
     */
    public int getIdToDo() {
        return idToDo;
    }

    /**
     * Imposta l'identificativo univoco del ToDo.
     * Solitamente invocato dopo l'inserimento nel database.
     *
     * @param idToDo Il nuovo ID.
     */
    public void setId(int idToDo) {
        this.idToDo = idToDo;
    }

    /**
     * Restituisce il titolo dell'attività.
     *
     * @return Il titolo.
     */
    public String getTitolo() {
        return titolo;
    }

    /**
     * Imposta il titolo dell'attività.
     *
     * @param titolo Il nuovo titolo.
     */
    public void setTitolo(String titolo) {
        this.titolo = titolo;
    }

    /**
     * Restituisce la data di scadenza dell'attività.
     *
     * @return La data di scadenza, o null se non impostata.
     */
    public LocalDate getDataScadenza() {
        return dataScadenza;
    }

    /**
     * Imposta la data di scadenza dell'attività.
     *
     * @param dataScadenza La nuova data di scadenza.
     */
    public void setDataScadenza(LocalDate dataScadenza) {
        this.dataScadenza = dataScadenza;
    }

    /**
     * Restituisce il colore di sfondo associato all'attività per la visualizzazione grafica.
     *
     * @return Il colore di sfondo, o null se non impostato.
     */
    public Color getColoreSfondo() {
        return coloreSfondo;
    }

    /**
     * Imposta il colore di sfondo dell'attività.
     *
     * @param coloreSfondo Il nuovo colore.
     */
    public void setColoreSfondo(Color coloreSfondo) {
        this.coloreSfondo = coloreSfondo;
    }

    /**
     * Restituisce la lista degli URL (link esterni) associati all'attività.
     *
     * @return Una lista di stringhe rappresentanti gli URL.
     */
    public List<String> getLinkURLs() {
        return linkURLs;
    }

    /**
     * Imposta l'intera lista dei link associati.
     *
     * @param linkURLs La nuova lista di URL.
     */
    public void setLinkURLs(List<String> linkURLs) {
        this.linkURLs = linkURLs;
    }

    /**
     * Aggiunge un singolo link alla lista degli URL associati.
     * Se la lista non è inizializzata, viene creata.
     *
     * @param link La stringa dell'URL da aggiungere.
     */
    public void aggiungiLink(String link) {
        if (this.linkURLs == null) this.linkURLs = new ArrayList<>();
        this.linkURLs.add(link);
    }

    /**
     * Imposta la lista dei link caricati dal database.
     * Metodo di utilità per i DAO.
     *
     * @param links La lista di URL recuperata dal DB.
     */
    public void setLinksDalDB(List<String> links) {
        this.linkURLs = links;
    }

    /**
     * Restituisce la descrizione dettagliata dell'attività.
     *
     * @return La descrizione.
     */
    public String getDescrizione() {
        return descrizione;
    }

    /**
     * Imposta la descrizione dettagliata dell'attività.
     *
     * @param descrizione La nuova descrizione.
     */
    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }

    /**
     * Restituisce l'immagine allegata all'attività.
     *
     * @return L'oggetto {@link ImageIcon}, o null se non presente.
     */
    public ImageIcon getImmagine() {
        return immagine;
    }

    /**
     * Imposta l'immagine allegata all'attività.
     *
     * @param immagine La nuova immagine.
     */
    public void setImmagine(ImageIcon immagine) {
        this.immagine = immagine;
    }

    /**
     * Verifica se l'attività è stata completata.
     *
     * @return {@code true} se completata, {@code false} altrimenti.
     */
    public boolean isCompletato() {
        return stato;
    }

    /**
     * Imposta lo stato di completamento dell'attività.
     *
     * @param stato {@code true} per completato, {@code false} per in corso.
     */
    public void setCompletato(boolean stato) {
        this.stato = stato;
    }

    /**
     * Restituisce la posizione ordinale dell'attività all'interno della bacheca.
     *
     * @return L'indice di posizione.
     */
    public int getPosizione() {
        return posizione;
    }

    /**
     * Imposta la posizione ordinale dell'attività.
     *
     * @param posizione Il nuovo indice di posizione.
     */
    public void setPosizione(int posizione) {
        this.posizione = posizione;
    }

    /**
     * Restituisce l'ID della bacheca a cui appartiene l'attività.
     *
     * @return L'ID della bacheca.
     */
    public int getIdBacheca() {
        return idBacheca;
    }

    /**
     * Imposta l'ID della bacheca di appartenenza.
     * Utile quando un ToDo viene spostato in un'altra bacheca.
     *
     * @param idBacheca Il nuovo ID della bacheca.
     */
    public void setIdBacheca(int idBacheca) {
        this.idBacheca = idBacheca;
    }

    /**
     * Restituisce l'ID dell'utente che ha creato l'attività.
     *
     * @return L'ID dell'autore.
     */
    public int getIdUtenteCreatore() {
        return idUtenteCreatore;
    }

    /**
     * Imposta l'ID dell'utente creatore.
     *
     * @param idUtenteCreatore L'ID dell'autore.
     */
    public void setIdUtenteCreatore(int idUtenteCreatore) {
        this.idUtenteCreatore = idUtenteCreatore;
    }


    // --- METODI DI CONDIVISIONE ---

    /**
     * Restituisce la mappa delle condivisioni attive per questo ToDo.
     * La mappa associa ogni utente condiviso al suo livello di permesso.
     *
     * @return Una mappa {@code Utente -> PermessoCondivisione}.
     */
    public Map<Utente, PermessoCondivisione> getCondivisioni() {
        return condivisioni;
    }

    /**
     * Imposta la mappa delle condivisioni caricata dal database.
     *
     * @param mappaPermessi La mappa {@code Utente -> PermessoCondivisione}.
     */
    public void setCondivisioniDalDB(Map<Utente, PermessoCondivisione> mappaPermessi) {
        this.condivisioni = mappaPermessi;
    }

    /**
     * Aggiunge o aggiorna la condivisione per un utente specifico.
     * Se l'utente è già presente, il suo permesso viene aggiornato.
     *
     * @param utente   L'utente con cui condividere.
     * @param permesso Il livello di permesso da assegnare.
     */
    public void aggiungiOModificaCondivisione(Utente utente, PermessoCondivisione permesso) {
        condivisioni.put(utente, permesso);
    }

    /**
     * Rimuove la condivisione per un utente specifico.
     *
     * @param utente L'utente da rimuovere dalla condivisione.
     */
    public void rimuoviCondivisione(Utente utente) {
        condivisioni.remove(utente);
    }

    /**
     * Recupera il livello di permesso associato a un determinato utente per questo ToDo.
     * Esegue una ricerca basata sull'ID utente per gestire istanze diverse dello stesso utente.
     *
     * @param utente L'utente di cui verificare i permessi.
     * @return Il {@link PermessoCondivisione} associato, o {@code null} se l'utente non ha accesso.
     */
    public PermessoCondivisione getPermessoPerUtente(Utente utente) {
        if (utente == null) return null;

        for (Utente u : condivisioni.keySet()) {
            if (u.getIdUtente() == utente.getIdUtente()) {
                return condivisioni.get(u);
            }
        }
        return null;
    }
}