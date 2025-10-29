package Model;

public enum TitoloBacheca {
    UNIVERSITA("Università"),
    LAVORO("Lavoro"),
    TEMPO_LIBERO("Tempo libero");

    private final String label;

    TitoloBacheca(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }


}
