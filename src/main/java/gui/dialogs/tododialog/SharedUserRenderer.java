package gui.dialogs.tododialog;

import model.PermessoCondivisione;
import model.Utente;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

/**
 * Renderer personalizzato per la visualizzazione degli utenti condivisi all'interno di una {@link JList}.
 * <p>
 * Questa classe estende {@link DefaultListCellRenderer} per personalizzare il testo mostrato
 * per ogni elemento della lista. Si aspetta che gli elementi della lista siano istanze di
 * {@link Map.Entry}, dove la chiave è un oggetto {@link Utente} e il valore è un
 * {@link PermessoCondivisione}.
 * <p>
 * Il formato di visualizzazione prodotto è: <b>"Username (Permesso)"</b>.
 */
public class SharedUserRenderer extends DefaultListCellRenderer {

    /**
     * Costruttore di default.
     * Richiama il costruttore della superclasse {@link DefaultListCellRenderer}.
     */
    public SharedUserRenderer() {
        super();
    }

    /**
     * Restituisce il componente configurato per renderizzare la cella specificata.
     * <p>
     * Questo metodo sovrascrive l'implementazione di default per intercettare il valore
     * dell'oggetto da visualizzare. Se l'oggetto è una {@link Map.Entry}, estrae
     * l'username dell'utente e il tipo di permesso associato per comporre una stringa leggibile.
     * In caso di errore durante il casting o l'accesso ai dati, viene mostrato un messaggio di errore generico.
     *
     * @param list         La {@link JList} che stiamo dipingendo.
     * @param value        Il valore restituito dal modello della lista all'indice specificato.
     * Si attende che sia di tipo {@code Map.Entry<Utente, PermessoCondivisione>}.
     * @param index        L'indice della cella da renderizzare.
     * @param isSelected   {@code true} se la cella specificata è stata selezionata dall'utente.
     * @param cellHasFocus {@code true} se la cella specificata ha il focus.
     * @return Un componente (l'istanza corrente di {@code JLabel}) configurato per disegnare la cella.
     */
    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus){
        super.getListCellRendererComponent(list,value,index,isSelected,cellHasFocus);
        if(value instanceof Map.Entry){
            try{
                Map.Entry<?,?> entry = (Map.Entry<?,?>) value;
                Utente u = (Utente) entry.getKey();
                PermessoCondivisione p = (PermessoCondivisione) entry.getValue();
                setText(u.getUsername()+" ("+p.toString()+")");
            } catch(Exception e){ setText("Errore nel rendering"); }
        }
        return this;
    }
}