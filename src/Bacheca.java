
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

// La classe Bacheca rappresenta una raccolta di ToDo categorizzata.
// Ogni utente ha da 1 a 3 bacheche, e ogni bacheca ha:
// - un titolo (uno dei tre dell’enum),
// - una descrizione,
// - e una lista di ToDo.

// Secondo il diagramma UML, la relazione tra Bacheca e ToDo è di tipo composizione:
// ciò significa che i ToDo esistono solo se appartengono a una Bacheca.

public class Bacheca {
    private UUID idBacheca;
    private TitoloBacheca titolo;
    private String descrizione;
    private List<ToDo> toDos; // Lista dei ToDo contenuti in questa bacheca

    // Costruttore: inizializza la bacheca con un titolo e una descrizione.
    public Bacheca(TitoloBacheca titolo, String descrizione) {
        this.idBacheca = UUID.randomUUID();
        this.titolo = titolo;
        this.descrizione = descrizione;
        this.toDos = new ArrayList<>();
    }

    // Getter per accedere ai campi privati
    public UUID getIdBacheca() {
        return idBacheca;
    }

    public TitoloBacheca getTitolo() {
        return titolo;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public List<ToDo> getToDos() {
        return toDos;
    }

    }

