package model;

/**
 * Enumerazione che definisce i livelli di permesso disponibili per la condivisione di un ToDo.
 * <p>
 * Questa enum viene utilizzata per gestire i diritti di accesso degli utenti
 * su attività condivise, distinguendo tra chi può solo visualizzare e chi può anche apportare modifiche.
 */
public enum PermessoCondivisione {

    /**
     * Permesso di sola lettura.
     * <p>
     * L'utente può visualizzare i dettagli del ToDo ma non può modificarlo,
     * completarlo o eliminarlo.
     */
    SOLO_LETTURA("Solo Lettura"),

    /**
     * Permesso di modifica.
     * <p>
     * L'utente ha diritti di gestione sul ToDo, inclusa la possibilità
     * di modificare titolo, descrizione, scadenze e stato di completamento.
     */
    MODIFICA("Modifica");

    /**
     * La rappresentazione testuale leggibile del permesso.
     */
    private final String label;

    /**
     * Costruttore privato dell'enum.
     * Associa una stringa descrittiva (label) a ciascun livello di permesso.
     *
     * @param label La rappresentazione testuale del permesso, utile per l'interfaccia utente.
     */
    PermessoCondivisione(String label) {
        this.label = label;
    }

    /**
     * Restituisce la rappresentazione testuale del permesso.
     * <p>
     * Questo metodo è utile per visualizzare il tipo di permesso in componenti
     * dell'interfaccia grafica come etichette o liste a discesa.
     *
     * @return La stringa associata alla costante enum (es. "Solo Lettura").
     */
    @Override
    public String toString() {
        return label;
    }

    /**
     * Converte una stringa in un valore dell'enumerazione {@link PermessoCondivisione}.
     * <p>
     * Questo metodo è utilizzato principalmente per convertire i valori di testo
     * memorizzati nel database nei corrispondenti oggetti Enum Java.
     * La conversione è case-insensitive (non distingue tra maiuscole e minuscole).
     *
     * @param text La stringa da convertire (es. "MODIFICA" o "SOLO_LETTURA").
     * @return Il valore Enum corrispondente. Se la stringa è {@code null} o non corrisponde
     * a nessun valore valido, restituisce {@link #SOLO_LETTURA} come default di sicurezza.
     */
    public static PermessoCondivisione fromString(String text) {
        if (text != null) {
            for (PermessoCondivisione p : PermessoCondivisione.values()) {
                if (text.equalsIgnoreCase(p.name())) {
                    return p;
                }
            }
        }
        return SOLO_LETTURA;
    }
}