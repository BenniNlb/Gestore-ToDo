package gui.panels;

import javax.swing.*;
import java.awt.*;

/**
 * Un pannello che usa FlowLayout ma che va a capo (wrap)
 * correttamente all'interno di uno JScrollPane.
 */
public class WrapPanel extends JPanel implements Scrollable {

    public WrapPanel() {
        super(new FlowLayout(FlowLayout.LEFT, 8, 8)); // 8px di gap
    }

    public WrapPanel(LayoutManager layout) {
        super(layout);
    }

    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    @Override
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        return 16; // Scorre di 16 pixel
    }

    @Override
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        return visibleRect.height; // Scorre di un'intera pagina
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        // Questa è la magia: forza il pannello ad essere largo quanto lo JScrollPane
        return true;
    }

    @Override
    public boolean getScrollableTracksViewportHeight() {
        // Permette al pannello di essere alto quanto serve
        return false;
    }
}