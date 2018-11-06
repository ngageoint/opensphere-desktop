package io.opensphere.core.util;

import java.awt.Font;
import java.awt.GraphicsEnvironment;

import io.opensphere.core.util.swing.SwingUtilities;

/**
 * An enumeration over the set of available FontAwesome Regular icons.
 */
public enum AwesomeIconRegular implements FontIconEnum
{
    /** A constant used to reference the 'address_book' icon. */
    ADDRESS_BOOK("\uf2b9"),

    /** A constant used to reference the 'address_card' icon. */
    ADDRESS_CARD("\uf2bb"),

    /** A constant used to reference the 'arrow_alt_circle_down' icon. */
    ARROW_ALT_CIRCLE_DOWN("\uf358"),

    /** A constant used to reference the 'arrow_alt_circle_left' icon. */
    ARROW_ALT_CIRCLE_LEFT("\uf359"),

    /** A constant used to reference the 'arrow_alt_circle_right' icon. */
    ARROW_ALT_CIRCLE_RIGHT("\uf35a"),

    /** A constant used to reference the 'arrow_alt_circle_up' icon. */
    ARROW_ALT_CIRCLE_UP("\uf35b"),

    /** A constant used to reference the 'bell' icon. */
    BELL("\uf0f3"),

    /** A constant used to reference the 'bell_slash' icon. */
    BELL_SLASH("\uf1f6"),

    /** A constant used to reference the 'bookmark' icon. */
    BOOKMARK("\uf02e"),

    /** A constant used to reference the 'building' icon. */
    BUILDING("\uf1ad"),

    /** A constant used to reference the 'calendar' icon. */
    CALENDAR("\uf133"),

    /** A constant used to reference the 'calendar_alt' icon. */
    CALENDAR_ALT("\uf073"),

    /** A constant used to reference the 'calendar_check' icon. */
    CALENDAR_CHECK("\uf274"),

    /** A constant used to reference the 'calendar_minus' icon. */
    CALENDAR_MINUS("\uf272"),

    /** A constant used to reference the 'calendar_plus' icon. */
    CALENDAR_PLUS("\uf271"),

    /** A constant used to reference the 'calendar_times' icon. */
    CALENDAR_TIMES("\uf273"),

    /** A constant used to reference the 'caret_square_down' icon. */
    CARET_SQUARE_DOWN("\uf150"),

    /** A constant used to reference the 'caret_square_left' icon. */
    CARET_SQUARE_LEFT("\uf191"),

    /** A constant used to reference the 'caret_square_right' icon. */
    CARET_SQUARE_RIGHT("\uf152"),

    /** A constant used to reference the 'caret_square_up' icon. */
    CARET_SQUARE_UP("\uf151"),

    /** A constant used to reference the 'chart_bar' icon. */
    CHART_BAR("\uf080"),

    /** A constant used to reference the 'check_circle' icon. */
    CHECK_CIRCLE("\uf058"),

    /** A constant used to reference the 'check_square' icon. */
    CHECK_SQUARE("\uf14a"),

    /** A constant used to reference the 'circle' icon. */
    CIRCLE("\uf111"),

    /** A constant used to reference the 'clipboard' icon. */
    CLIPBOARD("\uf328"),

    /** A constant used to reference the 'clock' icon. */
    CLOCK("\uf017"),

    /** A constant used to reference the 'clone' icon. */
    CLONE("\uf24d"),

    /** A constant used to reference the 'closed_captioning' icon. */
    CLOSED_CAPTIONING("\uf20a"),

    /** A constant used to reference the 'comment' icon. */
    COMMENT("\uf075"),

    /** A constant used to reference the 'comment_alt' icon. */
    COMMENT_ALT("\uf27a"),

    /** A constant used to reference the 'comment_dots' icon. */
    COMMENT_DOTS("\uf4ad"),

    /** A constant used to reference the 'comments' icon. */
    COMMENTS("\uf086"),

    /** A constant used to reference the 'compass' icon. */
    COMPASS("\uf14e"),

    /** A constant used to reference the 'copy' icon. */
    COPY("\uf0c5"),

    /** A constant used to reference the 'copyright' icon. */
    COPYRIGHT("\uf1f9"),

    /** A constant used to reference the 'credit_card' icon. */
    CREDIT_CARD("\uf09d"),

    /** A constant used to reference the 'dot_circle' icon. */
    DOT_CIRCLE("\uf192"),

    /** A constant used to reference the 'edit' icon. */
    EDIT("\uf044"),

    /** A constant used to reference the 'envelope' icon. */
    ENVELOPE("\uf0e0"),

    /** A constant used to reference the 'envelope_open' icon. */
    ENVELOPE_OPEN("\uf2b6"),

    /** A constant used to reference the 'eye' icon. */
    EYE("\uf06e"),

    /** A constant used to reference the 'eye_slash' icon. */
    EYE_SLASH("\uf070"),

    /** A constant used to reference the 'file' icon. */
    FILE("\uf15b"),

    /** A constant used to reference the 'file_alt' icon. */
    FILE_ALT("\uf15c"),

    /** A constant used to reference the 'file_archive' icon. */
    FILE_ARCHIVE("\uf1c6"),

    /** A constant used to reference the 'file_audio' icon. */
    FILE_AUDIO("\uf1c7"),

    /** A constant used to reference the 'file_code' icon. */
    FILE_CODE("\uf1c9"),

    /** A constant used to reference the 'file_excel' icon. */
    FILE_EXCEL("\uf1c3"),

    /** A constant used to reference the 'file_image' icon. */
    FILE_IMAGE("\uf1c5"),

    /** A constant used to reference the 'file_pdf' icon. */
    FILE_PDF("\uf1c1"),

    /** A constant used to reference the 'file_powerpoint' icon. */
    FILE_POWERPOINT("\uf1c4"),

    /** A constant used to reference the 'file_video' icon. */
    FILE_VIDEO("\uf1c8"),

    /** A constant used to reference the 'file_word' icon. */
    FILE_WORD("\uf1c2"),

    /** A constant used to reference the 'flag' icon. */
    FLAG("\uf024"),

    /** A constant used to reference the 'folder' icon. */
    FOLDER("\uf07b"),

    /** A constant used to reference the 'folder_open' icon. */
    FOLDER_OPEN("\uf07c"),

    /** A constant used to reference the 'font_awesome_logo_full' icon. */
    FONT_AWESOME_LOGO_FULL("\uf4e6"),

    /** A constant used to reference the 'frown' icon. */
    FROWN("\uf119"),

    /** A constant used to reference the 'futbol' icon. */
    FUTBOL("\uf1e3"),

    /** A constant used to reference the 'gem' icon. */
    GEM("\uf3a5"),

    /** A constant used to reference the 'hand_lizard' icon. */
    HAND_LIZARD("\uf258"),

    /** A constant used to reference the 'hand_paper' icon. */
    HAND_PAPER("\uf256"),

    /** A constant used to reference the 'hand_peace' icon. */
    HAND_PEACE("\uf25b"),

    /** A constant used to reference the 'hand_point_down' icon. */
    HAND_POINT_DOWN("\uf0a7"),

    /** A constant used to reference the 'hand_point_left' icon. */
    HAND_POINT_LEFT("\uf0a5"),

    /** A constant used to reference the 'hand_point_right' icon. */
    HAND_POINT_RIGHT("\uf0a4"),

    /** A constant used to reference the 'hand_point_up' icon. */
    HAND_POINT_UP("\uf0a6"),

    /** A constant used to reference the 'hand_pointer' icon. */
    HAND_POINTER("\uf25a"),

    /** A constant used to reference the 'hand_rock' icon. */
    HAND_ROCK("\uf255"),

    /** A constant used to reference the 'hand_scissors' icon. */
    HAND_SCISSORS("\uf257"),

    /** A constant used to reference the 'hand_spock' icon. */
    HAND_SPOCK("\uf259"),

    /** A constant used to reference the 'handshake' icon. */
    HANDSHAKE("\uf2b5"),

    /** A constant used to reference the 'hdd' icon. */
    HDD("\uf0a0"),

    /** A constant used to reference the 'heart' icon. */
    HEART("\uf004"),

    /** A constant used to reference the 'hospital' icon. */
    HOSPITAL("\uf0f8"),

    /** A constant used to reference the 'hourglass' icon. */
    HOURGLASS("\uf254"),

    /** A constant used to reference the 'id_badge' icon. */
    ID_BADGE("\uf2c1"),

    /** A constant used to reference the 'id_card' icon. */
    ID_CARD("\uf2c2"),

    /** A constant used to reference the 'image' icon. */
    IMAGE("\uf03e"),

    /** A constant used to reference the 'images' icon. */
    IMAGES("\uf302"),

    /** A constant used to reference the 'keyboard' icon. */
    KEYBOARD("\uf11c"),

    /** A constant used to reference the 'lemon' icon. */
    LEMON("\uf094"),

    /** A constant used to reference the 'life_ring' icon. */
    LIFE_RING("\uf1cd"),

    /** A constant used to reference the 'lightbulb' icon. */
    LIGHTBULB("\uf0eb"),

    /** A constant used to reference the 'list_alt' icon. */
    LIST_ALT("\uf022"),

    /** A constant used to reference the 'map' icon. */
    MAP("\uf279"),

    /** A constant used to reference the 'meh' icon. */
    MEH("\uf11a"),

    /** A constant used to reference the 'minus_square' icon. */
    MINUS_SQUARE("\uf146"),

    /** A constant used to reference the 'money_bill_alt' icon. */
    MONEY_BILL_ALT("\uf3d1"),

    /** A constant used to reference the 'moon' icon. */
    MOON("\uf186"),

    /** A constant used to reference the 'newspaper' icon. */
    NEWSPAPER("\uf1ea"),

    /** A constant used to reference the 'object_group' icon. */
    OBJECT_GROUP("\uf247"),

    /** A constant used to reference the 'object_ungroup' icon. */
    OBJECT_UNGROUP("\uf248"),

    /** A constant used to reference the 'paper_plane' icon. */
    PAPER_PLANE("\uf1d8"),

    /** A constant used to reference the 'pause_circle' icon. */
    PAUSE_CIRCLE("\uf28b"),

    /** A constant used to reference the 'play_circle' icon. */
    PLAY_CIRCLE("\uf144"),

    /** A constant used to reference the 'plus_square' icon. */
    PLUS_SQUARE("\uf0fe"),

    /** A constant used to reference the 'question_circle' icon. */
    QUESTION_CIRCLE("\uf059"),

    /** A constant used to reference the 'registered' icon. */
    REGISTERED("\uf25d"),

    /** A constant used to reference the 'save' icon. */
    SAVE("\uf0c7"),

    /** A constant used to reference the 'share_square' icon. */
    SHARE_SQUARE("\uf14d"),

    /** A constant used to reference the 'smile' icon. */
    SMILE("\uf118"),

    /** A constant used to reference the 'snowflake' icon. */
    SNOWFLAKE("\uf2dc"),

    /** A constant used to reference the 'square' icon. */
    SQUARE("\uf0c8"),

    /** A constant used to reference the 'star' icon. */
    STAR("\uf005"),

    /** A constant used to reference the 'star_half' icon. */
    STAR_HALF("\uf089"),

    /** A constant used to reference the 'sticky_note' icon. */
    STICKY_NOTE("\uf249"),

    /** A constant used to reference the 'stop_circle' icon. */
    STOP_CIRCLE("\uf28d"),

    /** A constant used to reference the 'sun' icon. */
    SUN("\uf185"),

    /** A constant used to reference the 'thumbs_down' icon. */
    THUMBS_DOWN("\uf165"),

    /** A constant used to reference the 'thumbs_up' icon. */
    THUMBS_UP("\uf164"),

    /** A constant used to reference the 'times_circle' icon. */
    TIMES_CIRCLE("\uf057"),

    /** A constant used to reference the 'trash_alt' icon. */
    TRASH_ALT("\uf2ed"),

    /** A constant used to reference the 'user' icon. */
    USER("\uf007"),

    /** A constant used to reference the 'user_circle' icon. */
    USER_CIRCLE("\uf2bd"),

    /** A constant used to reference the 'window_close' icon. */
    WINDOW_CLOSE("\uf410"),

    /** A constant used to reference the 'window_maximize' icon. */
    WINDOW_MAXIMIZE("\uf2d0"),

    /** A constant used to reference the 'window_minimize' icon. */
    WINDOW_MINIMIZE("\uf2d1"),

    /** A constant used to reference the 'window_restore' icon. */
    WINDOW_RESTORE("\uf2d2");

    static
    {
        GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(SwingUtilities.FONT_AWESOME_REGULAR_FONT);
    }

    /**
     * The font code defining the icon.
     */
    private String myFontCode;

    /**
     * Creates a new font code enum instance.
     *
     * @param pFontCode the font code defining the icon.
     */
    private AwesomeIconRegular(String pFontCode)
    {
        myFontCode = pFontCode;
    }

    /**
     * Gets the value of the {@link #myFontCode} field.
     *
     * @return the value stored in the {@link #myFontCode} field.
     */
    @Override
    public String getFontCode()
    {
        return myFontCode;
    }

    @Override
    public Font getFont()
    {
        return SwingUtilities.FONT_AWESOME_REGULAR_FONT;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.util.FontIconEnum#getXDrawingOffset()
     */
    @Override
    public float getXDrawingOffset()
    {
        return 0;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.util.FontIconEnum#getYDrawingOffset()
     */
    @Override
    public float getYDrawingOffset()
    {
        return 0;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.util.FontIconEnum#getGlyphName()
     */
    @Override
    public String getGlyphName()
    {
        return name();
    }
}
