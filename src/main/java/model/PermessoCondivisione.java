package model;

/**
 * Definisce i livelli di permesso per la condivisione di un ToDo.
 */
public enum PermessoCondivisione {
    SOLO_LETTURA("Solo Lettura"),
    MODIFICA("Modifica");

    private final String label;

    PermessoCondivisione(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }

    /**
     * Converte una stringa (es. dal DB) nel relativo Enum.
     * @param text La stringa (es. "MODIFICA")
     * @return L'enum corrispondente, o SOLO_LETTURA come default.
     */
    public static PermessoCondivisione fromString(String text) {
        if (text != null) {
            for (PermessoCondivisione p : PermessoCondivisione.values()) {
                if (text.equalsIgnoreCase(p.name())) {
                    return p;
                }
            }
        }
        // Default di sicurezza
        return SOLO_LETTURA;
    }
}