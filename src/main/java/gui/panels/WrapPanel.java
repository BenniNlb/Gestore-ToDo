package gui.panels;

import javax.swing.*;
import java.awt.*;

/**
 * Un pannello personalizzato che estende {@link JPanel} e implementa l'interfaccia {@link Scrollable}.
 * <p>
 * Questa classe è progettata per risolvere un problema specifico di Swing: l'utilizzo di
 * layout basati sul flusso (come {@link FlowLayout}) all'interno di uno {@link JScrollPane}.
 * <p>
 * Di default, uno scroll pane permette al contenuto di espandersi orizzontalmente all'infinito.
 * Questa classe forza il pannello ad adattarsi alla larghezza della viewport dello scroll pane,
 * costringendo così i componenti interni ad andare a capo (wrap) su nuove righe quando
 * lo spazio orizzontale finisce, invece di nascondersi oltre il bordo destro.
 */
public class WrapPanel extends JPanel implements Scrollable {

    /**
     * Costruisce un nuovo {@code WrapPanel} con un layout predefinito.
     * <p>
     * Utilizza un {@link FlowLayout} allineato a sinistra con un gap
     * orizzontale e verticale di 8 pixel.
     */
    public WrapPanel() {
        super(new FlowLayout(FlowLayout.LEFT, 8, 8));
    }

    /**
     * Costruisce un nuovo {@code WrapPanel} con un gestore di layout specifico.
     *
     * @param layout Il {@link LayoutManager} da utilizzare per il pannello.
     */
    public WrapPanel(LayoutManager layout) {
        super(layout);
    }

    /**
     * Restituisce la dimensione preferita della viewport per questo componente.
     *
     * @return La dimensione preferita ({@link Dimension}) del pannello.
     */
    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    /**
     * Calcola l'incremento di scorrimento unitario (es. click sulle frecce della scrollbar).
     *
     * @param visibleRect L'area attualmente visibile del componente.
     * @param orientation L'orientamento dello scorrimento (orizzontale o verticale).
     * @param direction   La direzione (negativa per su/sinistra, positiva per giù/destra).
     * @return L'incremento in pixel (fissato a 16).
     */
    @Override
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        return 16;
    }

    /**
     * Calcola l'incremento di scorrimento a blocchi (es. click nello spazio vuoto della scrollbar).
     *
     * @param visibleRect L'area attualmente visibile del componente.
     * @param orientation L'orientamento dello scorrimento.
     * @param direction   La direzione.
     * @return L'altezza dell'area visibile (scorre di una pagina intera).
     */
    @Override
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        return visibleRect.height;
    }

    /**
     * Determina se il pannello deve forzare la sua larghezza per adattarsi alla viewport.
     * <p>
     * Restituendo {@code true}, questo metodo è il cuore della funzionalità di wrapping:
     * impedisce lo scorrimento orizzontale e costringe il layout a ridistribuire i componenti
     * su più righe verticali.
     *
     * @return {@code true} sempre, per forzare l'adattamento alla larghezza.
     */
    @Override
    public boolean getScrollableTracksViewportWidth() {
        return true;
    }

    /**
     * Determina se il pannello deve forzare la sua altezza per adattarsi alla viewport.
     * <p>
     * Restituendo {@code false}, permette al pannello di crescere verticalmente quanto necessario
     * per contenere tutti i componenti che sono andati a capo.
     *
     * @return {@code false}, permettendo lo scorrimento verticale.
     */
    @Override
    public boolean getScrollableTracksViewportHeight() {
        return false;
    }
}