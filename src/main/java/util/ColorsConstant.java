package util;

import java.awt.Color;

/**
 * Classe di utilità che definisce la palette di colori standard per l'applicazione.
 * <p>
 * Questa classe centralizza le definizioni dei colori utilizzati nell'interfaccia grafica (GUI),
 * garantendo coerenza visiva in tutte le viste e facilitando eventuali modifiche future al tema.
 * I colori sono definiti come costanti statiche accessibili globalmente.
 */
public class ColorsConstant {

    /**
     * Rappresenta una tonalità di grigio scuro.
     * <p>
     * Utilizzato principalmente per elementi in primo piano, bordi, o testi che richiedono
     * un contrasto moderato rispetto agli sfondi chiari.
     * <br>
     * <b>Valore RGB:</b> 113, 112, 112.
     */
    public static final Color GREY = new Color(113, 112, 112);

    /**
     * Rappresenta una tonalità di grigio molto chiaro.
     * <p>
     * Utilizzato tipicamente come colore di sfondo per pannelli, aree di contenuto
     * e dashboard, per fornire un aspetto pulito e una leggera separazione visiva dal bianco puro.
     * <br>
     * <b>Valore RGB:</b> 235, 235, 235.
     */
    public static final Color LIGHT_GREY = new Color(235, 235, 235);

    /**
     * Costruttore privato per nascondere quello pubblico implicito.
     * <p>
     * Questa è una classe di utilità contenente solo costanti statiche
     * e non deve essere istanziata.
     */
    private ColorsConstant() {
        throw new IllegalStateException("Utility class");
    }
}