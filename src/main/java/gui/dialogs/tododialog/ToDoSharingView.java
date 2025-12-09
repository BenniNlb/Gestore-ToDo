package gui.dialogs.tododialog;

import controllers.MainController;
import model.PermessoCondivisione;
import model.ToDo;
import model.Utente;
import util.ColorsConstant;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;

/**
 * Pannello grafico (View) dedicato alla gestione delle condivisioni di un {@link ToDo}.
 * <p>
 * Questa classe offre un'interfaccia per:
 * <ul>
 * <li>Visualizzare l'elenco degli utenti con cui l'attività è attualmente condivisa.</li>
 * <li>Cercare nuovi utenti nel sistema per nome.</li>
 * <li>Aggiungere nuove condivisioni specificando il livello di permesso.</li>
 * <li>Modificare i permessi esistenti o revocare la condivisione (rimuovere utenti).</li>
 * </ul>
 */
public class ToDoSharingView extends JPanel {

    /**
     * Riferimento al controller principale per eseguire le operazioni di business.
     */
    private final MainController ctrl;

    /**
     * L'attività di cui si stanno gestendo le condivisioni.
     */
    private final ToDo toEdit;

    /**
     * Modello per la lista degli utenti attualmente condivisi.
     * Contiene entry di mappa (Utente -> Permesso).
     */
    private DefaultListModel<Map.Entry<Utente, PermessoCondivisione>> sharedListModel;

    /**
     * Componente grafico per visualizzare gli utenti condivisi.
     */
    private JList<Map.Entry<Utente, PermessoCondivisione>> sharedList;

    /**
     * Modello per la lista dei risultati della ricerca utenti.
     */
    private DefaultListModel<Utente> searchListModel;

    /**
     * Componente grafico per visualizzare i risultati della ricerca.
     */
    private JList<Utente> searchList;

    /**
     * Campo di testo per inserire la query di ricerca (username).
     */
    private JTextField searchField;

    /**
     * Menu a tendina per selezionare il livello di permesso da assegnare.
     */
    private JComboBox<PermessoCondivisione> permessoCombo;

    /**
     * Costruisce il pannello di gestione condivisioni.
     * <p>
     * Inizializza il layout, costruisce i componenti grafici e carica immediatamente
     * la lista degli utenti con cui il ToDo è già condiviso.
     *
     * @param ctrl   Il controller principale.
     * @param toEdit Il ToDo di cui gestire le condivisioni.
     */
    public ToDoSharingView(MainController ctrl, ToDo toEdit){
        this.ctrl = ctrl;
        this.toEdit = toEdit;

        setLayout(new BorderLayout(10,10));
        setBackground(ColorsConstant.LIGHT_GREY);
        setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        add(createSharingPanel(), BorderLayout.CENTER);
        loadSharedUsers();
    }

    /**
     * Crea e configura il pannello principale contenente le due sezioni:
     * utenti condivisi e ricerca nuovi utenti.
     *
     * @return Il pannello configurato.
     */
    private JPanel createSharingPanel(){
        JPanel mainPanel = new JPanel(new BorderLayout(10,10));
        mainPanel.setBackground(ColorsConstant.LIGHT_GREY);

        JPanel currentUsersPanel = new JPanel(new BorderLayout(0,5));
        currentUsersPanel.setOpaque(false);
        currentUsersPanel.setBorder(BorderFactory.createTitledBorder("Attualmente Condiviso Con"));

        sharedListModel = new DefaultListModel<>();
        sharedList = new JList<>(sharedListModel);
        sharedList.setCellRenderer(new SharedUserRenderer());

        JScrollPane sharedScroll = new JScrollPane(sharedList);
        sharedScroll.setPreferredSize(new Dimension(300,150));
        currentUsersPanel.add(sharedScroll, BorderLayout.CENTER);

        JPanel sharedButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT,5,0));
        sharedButtons.setOpaque(false);
        JButton modificaPermessoBtn = new JButton("Modifica Permesso");
        JButton rimuoviCondivisioneBtn = new JButton("Rimuovi");
        sharedButtons.add(modificaPermessoBtn);
        sharedButtons.add(rimuoviCondivisioneBtn);
        currentUsersPanel.add(sharedButtons, BorderLayout.SOUTH);

        mainPanel.add(currentUsersPanel, BorderLayout.CENTER);

        JPanel addUserPanel = new JPanel(new BorderLayout(0,5));
        addUserPanel.setOpaque(false);
        addUserPanel.setBorder(BorderFactory.createTitledBorder("Aggiungi Condivisione"));

        JPanel searchPanel = new JPanel(new BorderLayout(5,0));
        searchPanel.setOpaque(false);
        searchField = new JTextField();
        JButton searchBtn = new JButton("Cerca");
        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.add(searchBtn, BorderLayout.EAST);
        addUserPanel.add(searchPanel, BorderLayout.NORTH);

        searchListModel = new DefaultListModel<>();
        searchList = new JList<>(searchListModel);
        searchList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane searchScroll = new JScrollPane(searchList);
        searchScroll.setPreferredSize(new Dimension(300,100));
        addUserPanel.add(searchScroll, BorderLayout.CENTER);

        JPanel addActionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT,5,0));
        addActionPanel.setOpaque(false);
        permessoCombo = new JComboBox<>(PermessoCondivisione.values());
        JButton aggiungiCondivisioneBtn = new JButton("Aggiungi");
        addActionPanel.add(new JLabel("Permesso:"));
        addActionPanel.add(permessoCombo);
        addActionPanel.add(aggiungiCondivisioneBtn);
        addUserPanel.add(addActionPanel, BorderLayout.SOUTH);

        mainPanel.add(addUserPanel, BorderLayout.SOUTH);

        searchBtn.addActionListener(e->cercaUtenti());
        searchField.addActionListener(e->cercaUtenti());
        aggiungiCondivisioneBtn.addActionListener(e->aggiungiCondivisione());
        rimuoviCondivisioneBtn.addActionListener(e->rimuoviCondivisione());
        modificaPermessoBtn.addActionListener(e->modificaPermesso());

        return mainPanel;
    }

    /**
     * Carica (o ricarica) la lista degli utenti condivisi dal modello del ToDo.
     * Pulisce la lista attuale e la ripopola con i dati aggiornati.
     */
    private void loadSharedUsers(){
        if(toEdit==null) return;
        sharedListModel.clear();
        Map<Utente,PermessoCondivisione> condivisioni = toEdit.getCondivisioni();
        if(condivisioni!=null) sharedListModel.addAll(condivisioni.entrySet());
    }

    /**
     * Esegue la ricerca degli utenti nel database tramite il controller.
     * <p>
     * Filtra i risultati per escludere gli utenti con cui il ToDo è già stato condiviso,
     * evitando duplicati e confusione.
     */
    private void cercaUtenti(){
        String query = searchField.getText().trim();
        if(query.isEmpty()) return;
        searchListModel.clear();
        List<Utente> risultati = ctrl.cercaUtenti(query);

        Map<Utente,PermessoCondivisione> giaCondivisi = toEdit.getCondivisioni();
        List<Integer> idGia = giaCondivisi.keySet().stream().map(Utente::getIdUtente).toList();

        for(Utente u: risultati){
            if(!idGia.contains(u.getIdUtente())) searchListModel.addElement(u);
        }
    }

    /**
     * Aggiunge una nuova condivisione per l'utente selezionato nella lista di ricerca.
     * Invoca il controller per aggiornare il database e ricarica la lista.
     */
    private void aggiungiCondivisione(){
        Utente u = searchList.getSelectedValue();
        if(u==null){ JOptionPane.showMessageDialog(this,"Seleziona un utente dai risultati della ricerca.","Errore",JOptionPane.WARNING_MESSAGE); return; }
        PermessoCondivisione p = (PermessoCondivisione) permessoCombo.getSelectedItem();
        ctrl.onAggiungiCondivisione(toEdit,u,p);
        loadSharedUsers();
        searchListModel.removeElement(u);
    }

    /**
     * Rimuove la condivisione per l'utente selezionato nella lista delle condivisioni attive.
     * Richiede una conferma prima di procedere.
     */
    private void rimuoviCondivisione(){
        Map.Entry<Utente,PermessoCondivisione> sel = sharedList.getSelectedValue();
        if(sel==null){ JOptionPane.showMessageDialog(this,"Seleziona un utente dalla lista.","Errore",JOptionPane.WARNING_MESSAGE); return; }
        int conf = JOptionPane.showConfirmDialog(this,"Rimuovere "+sel.getKey().getUsername()+" dalla condivisione?","Conferma Rimozione",JOptionPane.YES_NO_OPTION);
        if(conf==JOptionPane.YES_OPTION){
            ctrl.onRimuoviCondivisione(toEdit,sel.getKey());
            loadSharedUsers();
        }
    }

    /**
     * Modifica il livello di permesso per l'utente selezionato.
     * Apre un dialog di input per scegliere il nuovo permesso.
     */
    private void modificaPermesso(){
        Map.Entry<Utente,PermessoCondivisione> sel = sharedList.getSelectedValue();
        if(sel==null){ JOptionPane.showMessageDialog(this,"Seleziona un utente dalla lista.","Errore",JOptionPane.WARNING_MESSAGE); return; }
        PermessoCondivisione nuovo = (PermessoCondivisione) JOptionPane.showInputDialog(this,"Seleziona il nuovo permesso per "+sel.getKey().getUsername()+":","Modifica Permesso",JOptionPane.QUESTION_MESSAGE,null,PermessoCondivisione.values(),sel.getValue());
        if(nuovo!=null && nuovo!=sel.getValue()){
            ctrl.onModificaPermesso(toEdit,sel.getKey(),nuovo);
            loadSharedUsers();
        }
    }
}