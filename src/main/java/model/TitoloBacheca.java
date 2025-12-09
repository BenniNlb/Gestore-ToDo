package model;

/**
 * Enumerazione che definisce le categorie predefinite (titoli) per le bacheche del sistema.
 * <p>
 * Le bacheche possono assumere solo uno di questi valori, garantendo una struttura
 * organizzativa fissa per le attività dell'utente: Università, Lavoro e Tempo Libero.
 * Questa enum viene utilizzata per garantire la consistenza dei dati e facilitare
 * il filtraggio e l'organizzazione nella logica di business.
 */
public enum TitoloBacheca {

    /**
     * Rappresenta la categoria dedicata alle attività universitarie (es. esami, progetti).
     */
    UNIVERSITA("Università"),

    /**
     * Rappresenta la categoria dedicata alle attività lavorative e professionali.
     */
    LAVORO("Lavoro"),

    /**
     * Rappresenta la categoria dedicata alle attività personali e al tempo libero.
     */
    TEMPO_LIBERO("Tempo libero");

    /**
     * La rappresentazione testuale leggibile del titolo.
     */
    private final String label;

    /**
     * Costruttore privato dell'enum.
     * Associa una stringa descrittiva (label) a ciascuna costante dell'enumerazione.
     *
     * @param label La rappresentazione testuale del titolo della bacheca,
     * utilizzata per la visualizzazione nell'interfaccia grafica.
     */
    TitoloBacheca(String label) {
        this.label = label;
    }

    /**
     * Restituisce la rappresentazione testuale del titolo della bacheca.
     * <p>
     * Questo override permette di visualizzare il nome della categoria in un formato
     * leggibile (es. "Tempo libero" invece di "TEMPO_LIBERO") all'interno di
     * componenti grafici come JComboBox o JLabel.
     *
     * @return La stringa associata alla costante enum.
     */
    @Override
    public String toString() {
        return label;
    }
}