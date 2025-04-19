
import java.awt.Color;
import java.time.LocalDate;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // 1. CREAZIONE UTENTE
        // L’utente inserisce login e password da tastiera
        System.out.print("Inserisci il tuo login: ");
        String login = scanner.nextLine();

        System.out.print("Inserisci la tua password: ");
        String password = scanner.nextLine();

        // Viene creato l’oggetto Utente con i dati forniti
        Utente utente = new Utente(login, password);
        System.out.println("Utente creato con successo.");

        // 2. SCELTA TITOLO BACHECA
        // Uso dell’enum TitoloBacheca per guidare l’utente nella selezione
        TitoloBacheca titoloBacheca = null;

        while (titoloBacheca == null) {
            System.out.println("Scegli il titolo della bacheca tra i seguenti:");
            for (TitoloBacheca titolo : TitoloBacheca.values()) {
                System.out.println("- " + titolo.name());
            }

            System.out.print("Scrivi il titolo esattamente come sopra: ");
            String input = scanner.nextLine().toUpperCase();

            try {
                // Si tenta di convertire l’input nel valore enum corrispondente
                titoloBacheca = TitoloBacheca.valueOf(input);
            } catch (IllegalArgumentException e) {
                // Se non è valido, si mostra un messaggio e si ripete
                System.out.println("Titolo non valido. Riprova.");
            }
        }

        // 3. DESCRIZIONE BACHECA
        // L’utente inserisce una descrizione per la bacheca
        System.out.print("Inserisci la descrizione della bacheca: ");
        String descrizione = scanner.nextLine();

        // Viene creata la Bacheca con titolo e descrizione
        Bacheca bacheca = new Bacheca(titoloBacheca, descrizione);
        System.out.println("Bacheca creata.");

        // 4. CREAZIONE TODO
        // Inserimento titolo del ToDo
        System.out.print("Inserisci il titolo del ToDo: ");
        String titoloToDo = scanner.nextLine();

        // Creazione oggetto ToDo
        ToDo todo = new ToDo(titoloToDo);

        // Inserimento descrizione del ToDo
        System.out.print("Inserisci la descrizione del ToDo: ");
        todo.setDescrizione(scanner.nextLine());

        // INSERIMENTO DATA DI SCADENZA con controllo di formato
        while (true) {
            System.out.print("Inserisci la data di scadenza (formato: YYYY-MM-DD): ");
            String dataInput = scanner.nextLine();

            try {
                // Parsing della data, se corretta si imposta e si esce dal ciclo
                LocalDate data = LocalDate.parse(dataInput);
                todo.setDataScadenza(data);
                break;
            } catch (Exception e) {
                // In caso di errore nel formato, si segnala e si ripete
                System.out.println("Formato data non valido. Riprova.");
            }
        }

        // Imposta un colore fisso (può essere personalizzato in futuro)
        todo.setColoreSfondo(Color.CYAN);

        // Aggiunta del ToDo alla Bacheca
        bacheca.aggiungiToDo(todo);
        System.out.println("ToDo aggiunto con successo.");

        // 5. RIEPILOGO FINALE
        // Visualizzazione di tutte le informazioni inserite
        System.out.println("===== RIEPILOGO =====");
        System.out.println("Utente: " + utente.getLogin());
        System.out.println("Bacheca: " + bacheca.getTitolo() + " - " + bacheca.getDescrizione());
        System.out.println("ToDo: " + todo.getTitolo());
        System.out.println("Descrizione: " + todo.getDescrizione());
        System.out.println("Scadenza: " + todo.getDataScadenza());
        System.out.println("Completato? " + (todo.isCompletato() ? "Sì" : "No"));
        System.out.println("=====================");
    }
}

