package GUI;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

/**
 * DocumentFilter che impedisce di inserire più di maxLength caratteri.
 */
public class JTextLengthFilter extends DocumentFilter {
    private final int maxLength;

    public JTextLengthFilter(int maxLength) {
        this.maxLength = maxLength;
    }

    @Override
    public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr)
            throws BadLocationException {
        if (string == null) return;
        int currentLength = fb.getDocument().getLength();
        int overLimit = (currentLength + string.length()) - maxLength;
        if (overLimit <= 0) {
            // dentro il limite: inserisce tutto
            super.insertString(fb, offset, string, attr);
        } else if (currentLength < maxLength) {
            // taglia la parte in eccesso e inserisce solo fino al max
            String cut = string.substring(0, string.length() - overLimit);
            super.insertString(fb, offset, cut, attr);
        }
        // altrimenti non inserisce nulla
    }

    @Override
    public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
            throws BadLocationException {
        if (text == null) return;
        int currentLength = fb.getDocument().getLength();
        int overLimit = (currentLength + text.length() - length) - maxLength;
        if (overLimit <= 0) {
            super.replace(fb, offset, length, text, attrs);
        } else if (currentLength < maxLength) {
            String cut = text.substring(0, text.length() - overLimit);
            super.replace(fb, offset, length, cut, attrs);
        }
        // altrimenti non sostituisce nulla
    }

    @Override
    public void remove(FilterBypass fb, int offset, int length)
            throws BadLocationException {
        super.remove(fb, offset, length);
    }
}

