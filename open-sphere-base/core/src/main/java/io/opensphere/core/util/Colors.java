package io.opensphere.core.util;

import java.awt.Color;

/** Colors. */
public class Colors
{
    /** Application light. */
    public static final Color OPENSPHERE_LIGHT = Color.decode("#777777");

    /** Application medium. */
    public static final Color OPENSPHERE_MEDIUM = Color.decode("#666666");

    /** Application dark. */
    public static final Color OPENSPHERE_DARK = Color.decode("#555555");

    /** Application very dark. */
    public static final Color OPENSPHERE_VERY_DARK = Color.decode("#2e3236");

    /** Selected HUD left edge, scroll bar border (128, 128, 162). */
    public static final Color LF_PRIMARY1 = Color.decode("#8080A2");

    /**
     * Check-box background, general hover/focus, scroll bars (141, 132, 189).
     */
    public static final Color LF_PRIMARY2 = Color.decode("#8D84BD");

    /** Selected HUD right edge, selected tabs (204, 204, 204). */
    public static final Color LF_PRIMARY3 = Color.decode("#CCCCCC");

    /** Edging color. */
    public static final Color LF_SECONDARY1 = LF_PRIMARY1;

    /** Other edging (tabs, tables), disabled text (128, 128, 128). */
    public static final Color LF_SECONDARY2 = Color.GRAY;

    /** Background, menu bar (83, 83, 102). */
    public static final Color LF_SECONDARY3 = Color.decode("#535366");

    /** Color from opensphere.css: -fx-control-inner-background. */
    public static final Color LF_INNER_BACKGROUND = Color.decode("#626278");

    /** Color from opensphere.css: -fx-control-inner-background-alt. */
    public static final Color LF_INNER_BACKGROUND_ALT = Color.decode("#696981");

    /** Look and feel white (58, 58, 71). */
    public static final Color LF_WHITE = Color.decode("#3A3A47");

    /** Look and feel black (255, 255, 255). */
    public static final Color LF_BLACK = Color.decode("#FFFFFF");

    /** JavaFX modena accent. */
    public static final Color FX_ACCENT = Color.decode("#0096C9");

    /** A color that is almost transparent. */
    public static final Color ALL_BUT_TRANSPARENT_BLACK = new Color(1 << 24, true);

    /** A color which is used to give contrast next to dark colors. */
    public static final Color RELATIVE_LIGHT_COLOR = new Color(212, 212, 212);

    /** Transparent black. */
    public static final Color TRANSPARENT_BLACK = new Color(0, true);

    /** Query region. */
    public static final Color QUERY_REGION = Color.ORANGE;

    /** Warning message color. */
    public static final javafx.scene.paint.Color WARNING = javafx.scene.paint.Color.YELLOW;

    /** Error message color. */
    public static final javafx.scene.paint.Color ERROR = javafx.scene.paint.Color.TOMATO.brighter();

    /** Info message color. */
    public static final javafx.scene.paint.Color INFO = javafx.scene.paint.Color.WHITE;

    /** Constructor. */
    protected Colors()
    {
    }
}
