
import java.util.UUID;

// La classe Utente rappresenta un account personale nel sistema.
// Ogni utente ha un ID univoco, un login (nome utente) e una password.
// Questo serve per autenticare l’accesso al sistema.

public class Utente {
    private UUID idUtente;
    private String login;
    private String password;

    // Il costruttore genera l’ID automaticamente e assegna login e password.
    public Utente(String login, String password) {
        this.idUtente = UUID.randomUUID();
        this.login = login;
        this.password = password;
    }

    // Metodi di accesso ai campi (getter)
    public UUID getIdUtente() {
        return idUtente;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }
}

