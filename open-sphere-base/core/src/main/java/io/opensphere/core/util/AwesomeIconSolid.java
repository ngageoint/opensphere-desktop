package io.opensphere.core.util;

import java.awt.Font;
import java.awt.GraphicsEnvironment;

import io.opensphere.core.util.swing.SwingUtilities;

/**
 *  An enumeration over the set of available FontAwesome Solid icons.
 */
public enum AwesomeIconSolid implements FontIconEnum
{
    /** A constant used to reference the 'address_book' icon. */
    ADDRESS_BOOK("\uf2b9"),

    /** A constant used to reference the 'address_card' icon. */
    ADDRESS_CARD("\uf2bb"),

    /** A constant used to reference the 'adjust' icon. */
    ADJUST("\uf042"),

    /** A constant used to reference the 'align_center' icon. */
    ALIGN_CENTER("\uf037"),

    /** A constant used to reference the 'align_justify' icon. */
    ALIGN_JUSTIFY("\uf039"),

    /** A constant used to reference the 'align_left' icon. */
    ALIGN_LEFT("\uf036"),

    /** A constant used to reference the 'align_right' icon. */
    ALIGN_RIGHT("\uf038"),

    /** A constant used to reference the 'allergies' icon. */
    ALLERGIES("\uf461"),

    /** A constant used to reference the 'ambulance' icon. */
    AMBULANCE("\uf0f9"),

    /**
     * A constant used to reference the 'american_sign_language_interpreting'
     * icon.
     */
    AMERICAN_SIGN_LANGUAGE_INTERPRETING("\uf2a3"),

    /** A constant used to reference the 'anchor' icon. */
    ANCHOR("\uf13d"),

    /** A constant used to reference the 'angle_double_down' icon. */
    ANGLE_DOUBLE_DOWN("\uf103"),

    /** A constant used to reference the 'angle_double_left' icon. */
    ANGLE_DOUBLE_LEFT("\uf100"),

    /** A constant used to reference the 'angle_double_right' icon. */
    ANGLE_DOUBLE_RIGHT("\uf101"),

    /** A constant used to reference the 'angle_double_up' icon. */
    ANGLE_DOUBLE_UP("\uf102"),

    /** A constant used to reference the 'angle_down' icon. */
    ANGLE_DOWN("\uf107"),

    /** A constant used to reference the 'angle_left' icon. */
    ANGLE_LEFT("\uf104"),

    /** A constant used to reference the 'angle_right' icon. */
    ANGLE_RIGHT("\uf105"),

    /** A constant used to reference the 'angle_up' icon. */
    ANGLE_UP("\uf106"),

    /** A constant used to reference the 'archive' icon. */
    ARCHIVE("\uf187"),

    /** A constant used to reference the 'arrow_alt_circle_down' icon. */
    ARROW_ALT_CIRCLE_DOWN("\uf358"),

    /** A constant used to reference the 'arrow_alt_circle_left' icon. */
    ARROW_ALT_CIRCLE_LEFT("\uf359"),

    /** A constant used to reference the 'arrow_alt_circle_right' icon. */
    ARROW_ALT_CIRCLE_RIGHT("\uf35a"),

    /** A constant used to reference the 'arrow_alt_circle_up' icon. */
    ARROW_ALT_CIRCLE_UP("\uf35b"),

    /** A constant used to reference the 'arrow_circle_down' icon. */
    ARROW_CIRCLE_DOWN("\uf0ab"),

    /** A constant used to reference the 'arrow_circle_left' icon. */
    ARROW_CIRCLE_LEFT("\uf0a8"),

    /** A constant used to reference the 'arrow_circle_right' icon. */
    ARROW_CIRCLE_RIGHT("\uf0a9"),

    /** A constant used to reference the 'arrow_circle_up' icon. */
    ARROW_CIRCLE_UP("\uf0aa"),

    /** A constant used to reference the 'arrow_down' icon. */
    ARROW_DOWN("\uf063"),

    /** A constant used to reference the 'arrow_left' icon. */
    ARROW_LEFT("\uf060"),

    /** A constant used to reference the 'arrow_right' icon. */
    ARROW_RIGHT("\uf061"),

    /** A constant used to reference the 'arrow_up' icon. */
    ARROW_UP("\uf062"),

    /** A constant used to reference the 'arrows_alt' icon. */
    ARROWS_ALT("\uf0b2"),

    /** A constant used to reference the 'arrows_alt_h' icon. */
    ARROWS_ALT_H("\uf337"),

    /** A constant used to reference the 'arrows_alt_v' icon. */
    ARROWS_ALT_V("\uf338"),

    /** A constant used to reference the 'assistive_listening_systems' icon. */
    ASSISTIVE_LISTENING_SYSTEMS("\uf2a2"),

    /** A constant used to reference the 'asterisk' icon. */
    ASTERISK("\uf069"),

    /** A constant used to reference the 'at' icon. */
    AT("\uf1fa"),

    /** A constant used to reference the 'audio_description' icon. */
    AUDIO_DESCRIPTION("\uf29e"),

    /** A constant used to reference the 'backward' icon. */
    BACKWARD("\uf04a"),

    /** A constant used to reference the 'balance_scale' icon. */
    BALANCE_SCALE("\uf24e"),

    /** A constant used to reference the 'ban' icon. */
    BAN("\uf05e"),

    /** A constant used to reference the 'band_aid' icon. */
    BAND_AID("\uf462"),

    /** A constant used to reference the 'barcode' icon. */
    BARCODE("\uf02a"),

    /** A constant used to reference the 'bars' icon. */
    BARS("\uf0c9"),

    /** A constant used to reference the 'baseball_ball' icon. */
    BASEBALL_BALL("\uf433"),

    /** A constant used to reference the 'basketball_ball' icon. */
    BASKETBALL_BALL("\uf434"),

    /** A constant used to reference the 'bath' icon. */
    BATH("\uf2cd"),

    /** A constant used to reference the 'battery_empty' icon. */
    BATTERY_EMPTY("\uf244"),

    /** A constant used to reference the 'battery_full' icon. */
    BATTERY_FULL("\uf240"),

    /** A constant used to reference the 'battery_half' icon. */
    BATTERY_HALF("\uf242"),

    /** A constant used to reference the 'battery_quarter' icon. */
    BATTERY_QUARTER("\uf243"),

    /** A constant used to reference the 'battery_three_quarters' icon. */
    BATTERY_THREE_QUARTERS("\uf241"),

    /** A constant used to reference the 'bed' icon. */
    BED("\uf236"),

    /** A constant used to reference the 'beer' icon. */
    BEER("\uf0fc"),

    /** A constant used to reference the 'bell' icon. */
    BELL("\uf0f3"),

    /** A constant used to reference the 'bell_slash' icon. */
    BELL_SLASH("\uf1f6"),

    /** A constant used to reference the 'bicycle' icon. */
    BICYCLE("\uf206"),

    /** A constant used to reference the 'binoculars' icon. */
    BINOCULARS("\uf1e5"),

    /** A constant used to reference the 'birthday_cake' icon. */
    BIRTHDAY_CAKE("\uf1fd"),

    /** A constant used to reference the 'blender' icon. */
    BLENDER("\uf517"),

    /** A constant used to reference the 'blind' icon. */
    BLIND("\uf29d"),

    /** A constant used to reference the 'bold' icon. */
    BOLD("\uf032"),

    /** A constant used to reference the 'bolt' icon. */
    BOLT("\uf0e7"),

    /** A constant used to reference the 'bomb' icon. */
    BOMB("\uf1e2"),

    /** A constant used to reference the 'book' icon. */
    BOOK("\uf02d"),

    /** A constant used to reference the 'book_open' icon. */
    BOOK_OPEN("\uf518"),

    /** A constant used to reference the 'bookmark' icon. */
    BOOKMARK("\uf02e"),

    /** A constant used to reference the 'bowling_ball' icon. */
    BOWLING_BALL("\uf436"),

    /** A constant used to reference the 'box' icon. */
    BOX("\uf466"),

    /** A constant used to reference the 'box_open' icon. */
    BOX_OPEN("\uf49e"),

    /** A constant used to reference the 'boxes' icon. */
    BOXES("\uf468"),

    /** A constant used to reference the 'braille' icon. */
    BRAILLE("\uf2a1"),

    /** A constant used to reference the 'briefcase' icon. */
    BRIEFCASE("\uf0b1"),

    /** A constant used to reference the 'briefcase_medical' icon. */
    BRIEFCASE_MEDICAL("\uf469"),

    /** A constant used to reference the 'broadcast_tower' icon. */
    BROADCAST_TOWER("\uf519"),

    /** A constant used to reference the 'broom' icon. */
    BROOM("\uf51a"),

    /** A constant used to reference the 'bug' icon. */
    BUG("\uf188"),

    /** A constant used to reference the 'building' icon. */
    BUILDING("\uf1ad"),

    /** A constant used to reference the 'bullhorn' icon. */
    BULLHORN("\uf0a1"),

    /** A constant used to reference the 'bullseye' icon. */
    BULLSEYE("\uf140"),

    /** A constant used to reference the 'burn' icon. */
    BURN("\uf46a"),

    /** A constant used to reference the 'bus' icon. */
    BUS("\uf207"),

    /** A constant used to reference the 'calculator' icon. */
    CALCULATOR("\uf1ec"),

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

    /** A constant used to reference the 'camera' icon. */
    CAMERA("\uf030"),

    /** A constant used to reference the 'camera_retro' icon. */
    CAMERA_RETRO("\uf083"),

    /** A constant used to reference the 'capsules' icon. */
    CAPSULES("\uf46b"),

    /** A constant used to reference the 'car' icon. */
    CAR("\uf1b9"),

    /** A constant used to reference the 'caret_down' icon. */
    CARET_DOWN("\uf0d7"),

    /** A constant used to reference the 'caret_left' icon. */
    CARET_LEFT("\uf0d9"),

    /** A constant used to reference the 'caret_right' icon. */
    CARET_RIGHT("\uf0da"),

    /** A constant used to reference the 'caret_square_down' icon. */
    CARET_SQUARE_DOWN("\uf150"),

    /** A constant used to reference the 'caret_square_left' icon. */
    CARET_SQUARE_LEFT("\uf191"),

    /** A constant used to reference the 'caret_square_right' icon. */
    CARET_SQUARE_RIGHT("\uf152"),

    /** A constant used to reference the 'caret_square_up' icon. */
    CARET_SQUARE_UP("\uf151"),

    /** A constant used to reference the 'caret_up' icon. */
    CARET_UP("\uf0d8"),

    /** A constant used to reference the 'cart_arrow_down' icon. */
    CART_ARROW_DOWN("\uf218"),

    /** A constant used to reference the 'cart_plus' icon. */
    CART_PLUS("\uf217"),

    /** A constant used to reference the 'certificate' icon. */
    CERTIFICATE("\uf0a3"),

    /** A constant used to reference the 'chalkboard' icon. */
    CHALKBOARD("\uf51b"),

    /** A constant used to reference the 'chalkboard_teacher' icon. */
    CHALKBOARD_TEACHER("\uf51c"),

    /** A constant used to reference the 'chart_area' icon. */
    CHART_AREA("\uf1fe"),

    /** A constant used to reference the 'chart_bar' icon. */
    CHART_BAR("\uf080"),

    /** A constant used to reference the 'chart_line' icon. */
    CHART_LINE("\uf201"),

    /** A constant used to reference the 'chart_pie' icon. */
    CHART_PIE("\uf200"),

    /** A constant used to reference the 'check' icon. */
    CHECK("\uf00c"),

    /** A constant used to reference the 'check_circle' icon. */
    CHECK_CIRCLE("\uf058"),

    /** A constant used to reference the 'check_square' icon. */
    CHECK_SQUARE("\uf14a"),

    /** A constant used to reference the 'chess' icon. */
    CHESS("\uf439"),

    /** A constant used to reference the 'chess_bishop' icon. */
    CHESS_BISHOP("\uf43a"),

    /** A constant used to reference the 'chess_board' icon. */
    CHESS_BOARD("\uf43c"),

    /** A constant used to reference the 'chess_king' icon. */
    CHESS_KING("\uf43f"),

    /** A constant used to reference the 'chess_knight' icon. */
    CHESS_KNIGHT("\uf441"),

    /** A constant used to reference the 'chess_pawn' icon. */
    CHESS_PAWN("\uf443"),

    /** A constant used to reference the 'chess_queen' icon. */
    CHESS_QUEEN("\uf445"),

    /** A constant used to reference the 'chess_rook' icon. */
    CHESS_ROOK("\uf447"),

    /** A constant used to reference the 'chevron_circle_down' icon. */
    CHEVRON_CIRCLE_DOWN("\uf13a"),

    /** A constant used to reference the 'chevron_circle_left' icon. */
    CHEVRON_CIRCLE_LEFT("\uf137"),

    /** A constant used to reference the 'chevron_circle_right' icon. */
    CHEVRON_CIRCLE_RIGHT("\uf138"),

    /** A constant used to reference the 'chevron_circle_up' icon. */
    CHEVRON_CIRCLE_UP("\uf139"),

    /** A constant used to reference the 'chevron_down' icon. */
    CHEVRON_DOWN("\uf078"),

    /** A constant used to reference the 'chevron_left' icon. */
    CHEVRON_LEFT("\uf053"),

    /** A constant used to reference the 'chevron_right' icon. */
    CHEVRON_RIGHT("\uf054"),

    /** A constant used to reference the 'chevron_up' icon. */
    CHEVRON_UP("\uf077"),

    /** A constant used to reference the 'child' icon. */
    CHILD("\uf1ae"),

    /** A constant used to reference the 'church' icon. */
    CHURCH("\uf51d"),

    /** A constant used to reference the 'circle' icon. */
    CIRCLE("\uf111"),

    /** A constant used to reference the 'circle_notch' icon. */
    CIRCLE_NOTCH("\uf1ce"),

    /** A constant used to reference the 'clipboard' icon. */
    CLIPBOARD("\uf328"),

    /** A constant used to reference the 'clipboard_check' icon. */
    CLIPBOARD_CHECK("\uf46c"),

    /** A constant used to reference the 'clipboard_list' icon. */
    CLIPBOARD_LIST("\uf46d"),

    /** A constant used to reference the 'clock' icon. */
    CLOCK("\uf017"),

    /** A constant used to reference the 'clone' icon. */
    CLONE("\uf24d"),

    /** A constant used to reference the 'closed_captioning' icon. */
    CLOSED_CAPTIONING("\uf20a"),

    /** A constant used to reference the 'cloud' icon. */
    CLOUD("\uf0c2"),

    /** A constant used to reference the 'cloud_download_alt' icon. */
    CLOUD_DOWNLOAD_ALT("\uf381"),

    /** A constant used to reference the 'cloud_upload_alt' icon. */
    CLOUD_UPLOAD_ALT("\uf382"),

    /** A constant used to reference the 'code' icon. */
    CODE("\uf121"),

    /** A constant used to reference the 'code_branch' icon. */
    CODE_BRANCH("\uf126"),

    /** A constant used to reference the 'coffee' icon. */
    COFFEE("\uf0f4"),

    /** A constant used to reference the 'cog' icon. */
    COG("\uf013"),

    /** A constant used to reference the 'cogs' icon. */
    COGS("\uf085"),

    /** A constant used to reference the 'coins' icon. */
    COINS("\uf51e"),

    /** A constant used to reference the 'columns' icon. */
    COLUMNS("\uf0db"),

    /** A constant used to reference the 'comment' icon. */
    COMMENT("\uf075"),

    /** A constant used to reference the 'comment_alt' icon. */
    COMMENT_ALT("\uf27a"),

    /** A constant used to reference the 'comment_dots' icon. */
    COMMENT_DOTS("\uf4ad"),

    /** A constant used to reference the 'comment_slash' icon. */
    COMMENT_SLASH("\uf4b3"),

    /** A constant used to reference the 'comments' icon. */
    COMMENTS("\uf086"),

    /** A constant used to reference the 'compact_disc' icon. */
    COMPACT_DISC("\uf51f"),

    /** A constant used to reference the 'compass' icon. */
    COMPASS("\uf14e"),

    /** A constant used to reference the 'compress' icon. */
    COMPRESS("\uf066"),

    /** A constant used to reference the 'copy' icon. */
    COPY("\uf0c5"),

    /** A constant used to reference the 'copyright' icon. */
    COPYRIGHT("\uf1f9"),

    /** A constant used to reference the 'couch' icon. */
    COUCH("\uf4b8"),

    /** A constant used to reference the 'credit_card' icon. */
    CREDIT_CARD("\uf09d"),

    /** A constant used to reference the 'crop' icon. */
    CROP("\uf125"),

    /** A constant used to reference the 'crosshairs' icon. */
    CROSSHAIRS("\uf05b"),

    /** A constant used to reference the 'crow' icon. */
    CROW("\uf520"),

    /** A constant used to reference the 'crown' icon. */
    CROWN("\uf521"),

    /** A constant used to reference the 'cube' icon. */
    CUBE("\uf1b2"),

    /** A constant used to reference the 'cubes' icon. */
    CUBES("\uf1b3"),

    /** A constant used to reference the 'cut' icon. */
    CUT("\uf0c4"),

    /** A constant used to reference the 'database' icon. */
    DATABASE("\uf1c0"),

    /** A constant used to reference the 'deaf' icon. */
    DEAF("\uf2a4"),

    /** A constant used to reference the 'desktop' icon. */
    DESKTOP("\uf108"),

    /** A constant used to reference the 'diagnoses' icon. */
    DIAGNOSES("\uf470"),

    /** A constant used to reference the 'dice' icon. */
    DICE("\uf522"),

    /** A constant used to reference the 'dice_five' icon. */
    DICE_FIVE("\uf523"),

    /** A constant used to reference the 'dice_four' icon. */
    DICE_FOUR("\uf524"),

    /** A constant used to reference the 'dice_one' icon. */
    DICE_ONE("\uf525"),

    /** A constant used to reference the 'dice_six' icon. */
    DICE_SIX("\uf526"),

    /** A constant used to reference the 'dice_three' icon. */
    DICE_THREE("\uf527"),

    /** A constant used to reference the 'dice_two' icon. */
    DICE_TWO("\uf528"),

    /** A constant used to reference the 'divide' icon. */
    DIVIDE("\uf529"),

    /** A constant used to reference the 'dna' icon. */
    DNA("\uf471"),

    /** A constant used to reference the 'dollar_sign' icon. */
    DOLLAR_SIGN("\uf155"),

    /** A constant used to reference the 'dolly' icon. */
    DOLLY("\uf472"),

    /** A constant used to reference the 'dolly_flatbed' icon. */
    DOLLY_FLATBED("\uf474"),

    /** A constant used to reference the 'donate' icon. */
    DONATE("\uf4b9"),

    /** A constant used to reference the 'door_closed' icon. */
    DOOR_CLOSED("\uf52a"),

    /** A constant used to reference the 'door_open' icon. */
    DOOR_OPEN("\uf52b"),

    /** A constant used to reference the 'dot_circle' icon. */
    DOT_CIRCLE("\uf192"),

    /** A constant used to reference the 'dove' icon. */
    DOVE("\uf4ba"),

    /** A constant used to reference the 'download' icon. */
    DOWNLOAD("\uf019"),

    /** A constant used to reference the 'dumbbell' icon. */
    DUMBBELL("\uf44b"),

    /** A constant used to reference the 'edit' icon. */
    EDIT("\uf044"),

    /** A constant used to reference the 'eject' icon. */
    EJECT("\uf052"),

    /** A constant used to reference the 'ellipsis_h' icon. */
    ELLIPSIS_H("\uf141"),

    /** A constant used to reference the 'ellipsis_v' icon. */
    ELLIPSIS_V("\uf142"),

    /** A constant used to reference the 'envelope' icon. */
    ENVELOPE("\uf0e0"),

    /** A constant used to reference the 'envelope_open' icon. */
    ENVELOPE_OPEN("\uf2b6"),

    /** A constant used to reference the 'envelope_square' icon. */
    ENVELOPE_SQUARE("\uf199"),

    /** A constant used to reference the 'equals' icon. */
    EQUALS("\uf52c"),

    /** A constant used to reference the 'eraser' icon. */
    ERASER("\uf12d"),

    /** A constant used to reference the 'euro_sign' icon. */
    EURO_SIGN("\uf153"),

    /** A constant used to reference the 'exchange_alt' icon. */
    EXCHANGE_ALT("\uf362"),

    /** A constant used to reference the 'exclamation' icon. */
    EXCLAMATION("\uf12a"),

    /** A constant used to reference the 'exclamation_circle' icon. */
    EXCLAMATION_CIRCLE("\uf06a"),

    /** A constant used to reference the 'exclamation_triangle' icon. */
    EXCLAMATION_TRIANGLE("\uf071"),

    /** A constant used to reference the 'expand' icon. */
    EXPAND("\uf065"),

    /** A constant used to reference the 'expand_arrows_alt' icon. */
    EXPAND_ARROWS_ALT("\uf31e"),

    /** A constant used to reference the 'external_link_alt' icon. */
    EXTERNAL_LINK_ALT("\uf35d"),

    /** A constant used to reference the 'external_link_square_alt' icon. */
    EXTERNAL_LINK_SQUARE_ALT("\uf360"),

    /** A constant used to reference the 'eye' icon. */
    EYE("\uf06e"),

    /** A constant used to reference the 'eye_dropper' icon. */
    EYE_DROPPER("\uf1fb"),

    /** A constant used to reference the 'eye_slash' icon. */
    EYE_SLASH("\uf070"),

    /** A constant used to reference the 'fast_backward' icon. */
    FAST_BACKWARD("\uf049"),

    /** A constant used to reference the 'fast_forward' icon. */
    FAST_FORWARD("\uf050"),

    /** A constant used to reference the 'fax' icon. */
    FAX("\uf1ac"),

    /** A constant used to reference the 'feather' icon. */
    FEATHER("\uf52d"),

    /** A constant used to reference the 'female' icon. */
    FEMALE("\uf182"),

    /** A constant used to reference the 'fighter_jet' icon. */
    FIGHTER_JET("\uf0fb"),

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

    /** A constant used to reference the 'file_medical' icon. */
    FILE_MEDICAL("\uf477"),

    /** A constant used to reference the 'file_medical_alt' icon. */
    FILE_MEDICAL_ALT("\uf478"),

    /** A constant used to reference the 'file_pdf' icon. */
    FILE_PDF("\uf1c1"),

    /** A constant used to reference the 'file_powerpoint' icon. */
    FILE_POWERPOINT("\uf1c4"),

    /** A constant used to reference the 'file_video' icon. */
    FILE_VIDEO("\uf1c8"),

    /** A constant used to reference the 'file_word' icon. */
    FILE_WORD("\uf1c2"),

    /** A constant used to reference the 'film' icon. */
    FILM("\uf008"),

    /** A constant used to reference the 'filter' icon. */
    FILTER("\uf0b0"),

    /** A constant used to reference the 'fire' icon. */
    FIRE("\uf06d"),

    /** A constant used to reference the 'fire_extinguisher' icon. */
    FIRE_EXTINGUISHER("\uf134"),

    /** A constant used to reference the 'first_aid' icon. */
    FIRST_AID("\uf479"),

    /** A constant used to reference the 'flag' icon. */
    FLAG("\uf024"),

    /** A constant used to reference the 'flag_checkered' icon. */
    FLAG_CHECKERED("\uf11e"),

    /** A constant used to reference the 'flask' icon. */
    FLASK("\uf0c3"),

    /** A constant used to reference the 'folder' icon. */
    FOLDER("\uf07b"),

    /** A constant used to reference the 'folder_open' icon. */
    FOLDER_OPEN("\uf07c"),

    /** A constant used to reference the 'font' icon. */
    FONT("\uf031"),

    /** A constant used to reference the 'font_awesome_logo_full' icon. */
    FONT_AWESOME_LOGO_FULL("\uf4e6"),

    /** A constant used to reference the 'football_ball' icon. */
    FOOTBALL_BALL("\uf44e"),

    /** A constant used to reference the 'forward' icon. */
    FORWARD("\uf04e"),

    /** A constant used to reference the 'frog' icon. */
    FROG("\uf52e"),

    /** A constant used to reference the 'frown' icon. */
    FROWN("\uf119"),

    /** A constant used to reference the 'futbol' icon. */
    FUTBOL("\uf1e3"),

    /** A constant used to reference the 'gamepad' icon. */
    GAMEPAD("\uf11b"),

    /** A constant used to reference the 'gas_pump' icon. */
    GAS_PUMP("\uf52f"),

    /** A constant used to reference the 'gavel' icon. */
    GAVEL("\uf0e3"),

    /** A constant used to reference the 'gem' icon. */
    GEM("\uf3a5"),

    /** A constant used to reference the 'genderless' icon. */
    GENDERLESS("\uf22d"),

    /** A constant used to reference the 'gift' icon. */
    GIFT("\uf06b"),

    /** A constant used to reference the 'glass_martini' icon. */
    GLASS_MARTINI("\uf000"),

    /** A constant used to reference the 'glasses' icon. */
    GLASSES("\uf530"),

    /** A constant used to reference the 'globe' icon. */
    GLOBE("\uf0ac"),

    /** A constant used to reference the 'golf_ball' icon. */
    GOLF_BALL("\uf450"),

    /** A constant used to reference the 'graduation_cap' icon. */
    GRADUATION_CAP("\uf19d"),

    /** A constant used to reference the 'greater_than' icon. */
    GREATER_THAN("\uf531"),

    /** A constant used to reference the 'greater_than_equal' icon. */
    GREATER_THAN_EQUAL("\uf532"),

    /** A constant used to reference the 'h_square' icon. */
    H_SQUARE("\uf0fd"),

    /** A constant used to reference the 'hand_holding' icon. */
    HAND_HOLDING("\uf4bd"),

    /** A constant used to reference the 'hand_holding_heart' icon. */
    HAND_HOLDING_HEART("\uf4be"),

    /** A constant used to reference the 'hand_holding_usd' icon. */
    HAND_HOLDING_USD("\uf4c0"),

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

    /** A constant used to reference the 'hands' icon. */
    HANDS("\uf4c2"),

    /** A constant used to reference the 'hands_helping' icon. */
    HANDS_HELPING("\uf4c4"),

    /** A constant used to reference the 'handshake' icon. */
    HANDSHAKE("\uf2b5"),

    /** A constant used to reference the 'hashtag' icon. */
    HASHTAG("\uf292"),

    /** A constant used to reference the 'hdd' icon. */
    HDD("\uf0a0"),

    /** A constant used to reference the 'heading' icon. */
    HEADING("\uf1dc"),

    /** A constant used to reference the 'headphones' icon. */
    HEADPHONES("\uf025"),

    /** A constant used to reference the 'heart' icon. */
    HEART("\uf004"),

    /** A constant used to reference the 'heartbeat' icon. */
    HEARTBEAT("\uf21e"),

    /** A constant used to reference the 'helicopter' icon. */
    HELICOPTER("\uf533"),

    /** A constant used to reference the 'history' icon. */
    HISTORY("\uf1da"),

    /** A constant used to reference the 'hockey_puck' icon. */
    HOCKEY_PUCK("\uf453"),

    /** A constant used to reference the 'home' icon. */
    HOME("\uf015"),

    /** A constant used to reference the 'hospital' icon. */
    HOSPITAL("\uf0f8"),

    /** A constant used to reference the 'hospital_alt' icon. */
    HOSPITAL_ALT("\uf47d"),

    /** A constant used to reference the 'hospital_symbol' icon. */
    HOSPITAL_SYMBOL("\uf47e"),

    /** A constant used to reference the 'hourglass' icon. */
    HOURGLASS("\uf254"),

    /** A constant used to reference the 'hourglass_end' icon. */
    HOURGLASS_END("\uf253"),

    /** A constant used to reference the 'hourglass_half' icon. */
    HOURGLASS_HALF("\uf252"),

    /** A constant used to reference the 'hourglass_start' icon. */
    HOURGLASS_START("\uf251"),

    /** A constant used to reference the 'i_cursor' icon. */
    I_CURSOR("\uf246"),

    /** A constant used to reference the 'id_badge' icon. */
    ID_BADGE("\uf2c1"),

    /** A constant used to reference the 'id_card' icon. */
    ID_CARD("\uf2c2"),

    /** A constant used to reference the 'id_card_alt' icon. */
    ID_CARD_ALT("\uf47f"),

    /** A constant used to reference the 'image' icon. */
    IMAGE("\uf03e"),

    /** A constant used to reference the 'images' icon. */
    IMAGES("\uf302"),

    /** A constant used to reference the 'inbox' icon. */
    INBOX("\uf01c"),

    /** A constant used to reference the 'indent' icon. */
    INDENT("\uf03c"),

    /** A constant used to reference the 'industry' icon. */
    INDUSTRY("\uf275"),

    /** A constant used to reference the 'infinity' icon. */
    INFINITY("\uf534"),

    /** A constant used to reference the 'info' icon. */
    INFO("\uf129"),

    /** A constant used to reference the 'info_circle' icon. */
    INFO_CIRCLE("\uf05a"),

    /** A constant used to reference the 'italic' icon. */
    ITALIC("\uf033"),

    /** A constant used to reference the 'key' icon. */
    KEY("\uf084"),

    /** A constant used to reference the 'keyboard' icon. */
    KEYBOARD("\uf11c"),

    /** A constant used to reference the 'kiwi_bird' icon. */
    KIWI_BIRD("\uf535"),

    /** A constant used to reference the 'language' icon. */
    LANGUAGE("\uf1ab"),

    /** A constant used to reference the 'laptop' icon. */
    LAPTOP("\uf109"),

    /** A constant used to reference the 'leaf' icon. */
    LEAF("\uf06c"),

    /** A constant used to reference the 'lemon' icon. */
    LEMON("\uf094"),

    /** A constant used to reference the 'less_than' icon. */
    LESS_THAN("\uf536"),

    /** A constant used to reference the 'less_than_equal' icon. */
    LESS_THAN_EQUAL("\uf537"),

    /** A constant used to reference the 'level_down_alt' icon. */
    LEVEL_DOWN_ALT("\uf3be"),

    /** A constant used to reference the 'level_up_alt' icon. */
    LEVEL_UP_ALT("\uf3bf"),

    /** A constant used to reference the 'life_ring' icon. */
    LIFE_RING("\uf1cd"),

    /** A constant used to reference the 'lightbulb' icon. */
    LIGHTBULB("\uf0eb"),

    /** A constant used to reference the 'link' icon. */
    LINK("\uf0c1"),

    /** A constant used to reference the 'lira_sign' icon. */
    LIRA_SIGN("\uf195"),

    /** A constant used to reference the 'list' icon. */
    LIST("\uf03a"),

    /** A constant used to reference the 'list_alt' icon. */
    LIST_ALT("\uf022"),

    /** A constant used to reference the 'list_ol' icon. */
    LIST_OL("\uf0cb"),

    /** A constant used to reference the 'list_ul' icon. */
    LIST_UL("\uf0ca"),

    /** A constant used to reference the 'location_arrow' icon. */
    LOCATION_ARROW("\uf124"),

    /** A constant used to reference the 'lock' icon. */
    LOCK("\uf023"),

    /** A constant used to reference the 'lock_open' icon. */
    LOCK_OPEN("\uf3c1"),

    /** A constant used to reference the 'long_arrow_alt_down' icon. */
    LONG_ARROW_ALT_DOWN("\uf309"),

    /** A constant used to reference the 'long_arrow_alt_left' icon. */
    LONG_ARROW_ALT_LEFT("\uf30a"),

    /** A constant used to reference the 'long_arrow_alt_right' icon. */
    LONG_ARROW_ALT_RIGHT("\uf30b"),

    /** A constant used to reference the 'long_arrow_alt_up' icon. */
    LONG_ARROW_ALT_UP("\uf30c"),

    /** A constant used to reference the 'low_vision' icon. */
    LOW_VISION("\uf2a8"),

    /** A constant used to reference the 'magic' icon. */
    MAGIC("\uf0d0"),

    /** A constant used to reference the 'magnet' icon. */
    MAGNET("\uf076"),

    /** A constant used to reference the 'male' icon. */
    MALE("\uf183"),

    /** A constant used to reference the 'map' icon. */
    MAP("\uf279"),

    /** A constant used to reference the 'map_marker' icon. */
    MAP_MARKER("\uf041"),

    /** A constant used to reference the 'map_marker_alt' icon. */
    MAP_MARKER_ALT("\uf3c5"),

    MAP_MARKED_ALT("\uf5a0"),

    /** A constant used to reference the 'map_pin' icon. */
    MAP_PIN("\uf276"),

    /** A constant used to reference the 'map_signs' icon. */
    MAP_SIGNS("\uf277"),

    /** A constant used to reference the 'mars' icon. */
    MARS("\uf222"),

    /** A constant used to reference the 'mars_double' icon. */
    MARS_DOUBLE("\uf227"),

    /** A constant used to reference the 'mars_stroke' icon. */
    MARS_STROKE("\uf229"),

    /** A constant used to reference the 'mars_stroke_h' icon. */
    MARS_STROKE_H("\uf22b"),

    /** A constant used to reference the 'mars_stroke_v' icon. */
    MARS_STROKE_V("\uf22a"),

    /** A constant used to reference the 'medkit' icon. */
    MEDKIT("\uf0fa"),

    /** A constant used to reference the 'meh' icon. */
    MEH("\uf11a"),

    /** A constant used to reference the 'memory' icon. */
    MEMORY("\uf538"),

    /** A constant used to reference the 'mercury' icon. */
    MERCURY("\uf223"),

    /** A constant used to reference the 'microchip' icon. */
    MICROCHIP("\uf2db"),

    /** A constant used to reference the 'microphone' icon. */
    MICROPHONE("\uf130"),

    /** A constant used to reference the 'microphone_alt' icon. */
    MICROPHONE_ALT("\uf3c9"),

    /** A constant used to reference the 'microphone_alt_slash' icon. */
    MICROPHONE_ALT_SLASH("\uf539"),

    /** A constant used to reference the 'microphone_slash' icon. */
    MICROPHONE_SLASH("\uf131"),

    /** A constant used to reference the 'minus' icon. */
    MINUS("\uf068"),

    /** A constant used to reference the 'minus_circle' icon. */
    MINUS_CIRCLE("\uf056"),

    /** A constant used to reference the 'minus_square' icon. */
    MINUS_SQUARE("\uf146"),

    /** A constant used to reference the 'mobile' icon. */
    MOBILE("\uf10b"),

    /** A constant used to reference the 'mobile_alt' icon. */
    MOBILE_ALT("\uf3cd"),

    /** A constant used to reference the 'money_bill' icon. */
    MONEY_BILL("\uf0d6"),

    /** A constant used to reference the 'money_bill_alt' icon. */
    MONEY_BILL_ALT("\uf3d1"),

    /** A constant used to reference the 'money_bill_wave' icon. */
    MONEY_BILL_WAVE("\uf53a"),

    /** A constant used to reference the 'money_bill_wave_alt' icon. */
    MONEY_BILL_WAVE_ALT("\uf53b"),

    /** A constant used to reference the 'money_check' icon. */
    MONEY_CHECK("\uf53c"),

    /** A constant used to reference the 'money_check_alt' icon. */
    MONEY_CHECK_ALT("\uf53d"),

    /** A constant used to reference the 'moon' icon. */
    MOON("\uf186"),

    /** A constant used to reference the 'motorcycle' icon. */
    MOTORCYCLE("\uf21c"),

    /** A constant used to reference the 'mouse_pointer' icon. */
    MOUSE_POINTER("\uf245"),

    /** A constant used to reference the 'music' icon. */
    MUSIC("\uf001"),

    /** A constant used to reference the 'neuter' icon. */
    NEUTER("\uf22c"),

    /** A constant used to reference the 'newspaper' icon. */
    NEWSPAPER("\uf1ea"),

    /** A constant used to reference the 'not_equal' icon. */
    NOT_EQUAL("\uf53e"),

    /** A constant used to reference the 'notes_medical' icon. */
    NOTES_MEDICAL("\uf481"),

    /** A constant used to reference the 'object_group' icon. */
    OBJECT_GROUP("\uf247"),

    /** A constant used to reference the 'object_ungroup' icon. */
    OBJECT_UNGROUP("\uf248"),

    /** A constant used to reference the 'outdent' icon. */
    OUTDENT("\uf03b"),

    /** A constant used to reference the 'paint_brush' icon. */
    PAINT_BRUSH("\uf1fc"),

    /** A constant used to reference the 'palette' icon. */
    PALETTE("\uf53f"),

    /** A constant used to reference the 'pallet' icon. */
    PALLET("\uf482"),

    /** A constant used to reference the 'paper_plane' icon. */
    PAPER_PLANE("\uf1d8"),

    /** A constant used to reference the 'paperclip' icon. */
    PAPERCLIP("\uf0c6"),

    /** A constant used to reference the 'parachute_box' icon. */
    PARACHUTE_BOX("\uf4cd"),

    /** A constant used to reference the 'paragraph' icon. */
    PARAGRAPH("\uf1dd"),

    /** A constant used to reference the 'parking' icon. */
    PARKING("\uf540"),

    /** A constant used to reference the 'paste' icon. */
    PASTE("\uf0ea"),

    /** A constant used to reference the 'pause' icon. */
    PAUSE("\uf04c"),

    /** A constant used to reference the 'pause_circle' icon. */
    PAUSE_CIRCLE("\uf28b"),

    /** A constant used to reference the 'paw' icon. */
    PAW("\uf1b0"),

    /** A constant used to reference the 'pen_square' icon. */
    PEN_SQUARE("\uf14b"),

    /** A constant used to reference the 'pencil_alt' icon. */
    PENCIL_ALT("\uf303"),

    /** A constant used to reference the 'people_carry' icon. */
    PEOPLE_CARRY("\uf4ce"),

    /** A constant used to reference the 'percent' icon. */
    PERCENT("\uf295"),

    /** A constant used to reference the 'percentage' icon. */
    PERCENTAGE("\uf541"),

    /** A constant used to reference the 'phone' icon. */
    PHONE("\uf095"),

    /** A constant used to reference the 'phone_slash' icon. */
    PHONE_SLASH("\uf3dd"),

    /** A constant used to reference the 'phone_square' icon. */
    PHONE_SQUARE("\uf098"),

    /** A constant used to reference the 'phone_volume' icon. */
    PHONE_VOLUME("\uf2a0"),

    /** A constant used to reference the 'piggy_bank' icon. */
    PIGGY_BANK("\uf4d3"),

    /** A constant used to reference the 'pills' icon. */
    PILLS("\uf484"),

    /** A constant used to reference the 'plane' icon. */
    PLANE("\uf072"),

    /** A constant used to reference the 'play' icon. */
    PLAY("\uf04b"),

    /** A constant used to reference the 'play_circle' icon. */
    PLAY_CIRCLE("\uf144"),

    /** A constant used to reference the 'plug' icon. */
    PLUG("\uf1e6"),

    /** A constant used to reference the 'plus' icon. */
    PLUS("\uf067"),

    /** A constant used to reference the 'plus_circle' icon. */
    PLUS_CIRCLE("\uf055"),

    /** A constant used to reference the 'plus_square' icon. */
    PLUS_SQUARE("\uf0fe"),

    /** A constant used to reference the 'podcast' icon. */
    PODCAST("\uf2ce"),

    /** A constant used to reference the 'poo' icon. */
    POO("\uf2fe"),

    /** A constant used to reference the 'portrait' icon. */
    PORTRAIT("\uf3e0"),

    /** A constant used to reference the 'pound_sign' icon. */
    POUND_SIGN("\uf154"),

    /** A constant used to reference the 'power_off' icon. */
    POWER_OFF("\uf011"),

    /** A constant used to reference the 'prescription_bottle' icon. */
    PRESCRIPTION_BOTTLE("\uf485"),

    /** A constant used to reference the 'prescription_bottle_alt' icon. */
    PRESCRIPTION_BOTTLE_ALT("\uf486"),

    /** A constant used to reference the 'print' icon. */
    PRINT("\uf02f"),

    /** A constant used to reference the 'procedures' icon. */
    PROCEDURES("\uf487"),

    /** A constant used to reference the 'project_diagram' icon. */
    PROJECT_DIAGRAM("\uf542"),

    /** A constant used to reference the 'puzzle_piece' icon. */
    PUZZLE_PIECE("\uf12e"),

    /** A constant used to reference the 'qrcode' icon. */
    QRCODE("\uf029"),

    /** A constant used to reference the 'question' icon. */
    QUESTION("\uf128"),

    /** A constant used to reference the 'question_circle' icon. */
    QUESTION_CIRCLE("\uf059"),

    /** A constant used to reference the 'quidditch' icon. */
    QUIDDITCH("\uf458"),

    /** A constant used to reference the 'quote_left' icon. */
    QUOTE_LEFT("\uf10d"),

    /** A constant used to reference the 'quote_right' icon. */
    QUOTE_RIGHT("\uf10e"),

    /** A constant used to reference the 'random' icon. */
    RANDOM("\uf074"),

    /** A constant used to reference the 'receipt' icon. */
    RECEIPT("\uf543"),

    /** A constant used to reference the 'recycle' icon. */
    RECYCLE("\uf1b8"),

    /** A constant used to reference the 'redo' icon. */
    REDO("\uf01e"),

    /** A constant used to reference the 'redo_alt' icon. */
    REDO_ALT("\uf2f9"),

    /** A constant used to reference the 'registered' icon. */
    REGISTERED("\uf25d"),

    /** A constant used to reference the 'reply' icon. */
    REPLY("\uf3e5"),

    /** A constant used to reference the 'reply_all' icon. */
    REPLY_ALL("\uf122"),

    /** A constant used to reference the 'retweet' icon. */
    RETWEET("\uf079"),

    /** A constant used to reference the 'ribbon' icon. */
    RIBBON("\uf4d6"),

    /** A constant used to reference the 'road' icon. */
    ROAD("\uf018"),

    /** A constant used to reference the 'robot' icon. */
    ROBOT("\uf544"),

    /** A constant used to reference the 'rocket' icon. */
    ROCKET("\uf135"),

    /** A constant used to reference the 'rss' icon. */
    RSS("\uf09e"),

    /** A constant used to reference the 'rss_square' icon. */
    RSS_SQUARE("\uf143"),

    /** A constant used to reference the 'ruble_sign' icon. */
    RUBLE_SIGN("\uf158"),

    /** A constant used to reference the 'ruler' icon. */
    RULER("\uf545"),

    /** A constant used to reference the 'ruler_combined' icon. */
    RULER_COMBINED("\uf546"),

    /** A constant used to reference the 'ruler_horizontal' icon. */
    RULER_HORIZONTAL("\uf547"),

    /** A constant used to reference the 'ruler_vertical' icon. */
    RULER_VERTICAL("\uf548"),

    /** A constant used to reference the 'rupee_sign' icon. */
    RUPEE_SIGN("\uf156"),

    /** A constant used to reference the 'save' icon. */
    SAVE("\uf0c7"),

    /** A constant used to reference the 'school' icon. */
    SCHOOL("\uf549"),

    /** A constant used to reference the 'screwdriver' icon. */
    SCREWDRIVER("\uf54a"),

    /** A constant used to reference the 'search' icon. */
    SEARCH("\uf002"),

    /** A constant used to reference the 'search_minus' icon. */
    SEARCH_MINUS("\uf010"),

    /** A constant used to reference the 'search_plus' icon. */
    SEARCH_PLUS("\uf00e"),

    /** A constant used to reference the 'seedling' icon. */
    SEEDLING("\uf4d8"),

    /** A constant used to reference the 'server' icon. */
    SERVER("\uf233"),

    /** A constant used to reference the 'share' icon. */
    SHARE("\uf064"),

    /** A constant used to reference the 'share_alt' icon. */
    SHARE_ALT("\uf1e0"),

    /** A constant used to reference the 'share_alt_square' icon. */
    SHARE_ALT_SQUARE("\uf1e1"),

    /** A constant used to reference the 'share_square' icon. */
    SHARE_SQUARE("\uf14d"),

    /** A constant used to reference the 'shekel_sign' icon. */
    SHEKEL_SIGN("\uf20b"),

    /** A constant used to reference the 'shield_alt' icon. */
    SHIELD_ALT("\uf3ed"),

    /** A constant used to reference the 'ship' icon. */
    SHIP("\uf21a"),

    /** A constant used to reference the 'shipping_fast' icon. */
    SHIPPING_FAST("\uf48b"),

    /** A constant used to reference the 'shoe_prints' icon. */
    SHOE_PRINTS("\uf54b"),

    /** A constant used to reference the 'shopping_bag' icon. */
    SHOPPING_BAG("\uf290"),

    /** A constant used to reference the 'shopping_basket' icon. */
    SHOPPING_BASKET("\uf291"),

    /** A constant used to reference the 'shopping_cart' icon. */
    SHOPPING_CART("\uf07a"),

    /** A constant used to reference the 'shower' icon. */
    SHOWER("\uf2cc"),

    /** A constant used to reference the 'sign' icon. */
    SIGN("\uf4d9"),

    /** A constant used to reference the 'sign_in_alt' icon. */
    SIGN_IN_ALT("\uf2f6"),

    /** A constant used to reference the 'sign_language' icon. */
    SIGN_LANGUAGE("\uf2a7"),

    /** A constant used to reference the 'sign_out_alt' icon. */
    SIGN_OUT_ALT("\uf2f5"),

    /** A constant used to reference the 'signal' icon. */
    SIGNAL("\uf012"),

    /** A constant used to reference the 'sitemap' icon. */
    SITEMAP("\uf0e8"),

    /** A constant used to reference the 'skull' icon. */
    SKULL("\uf54c"),

    /** A constant used to reference the 'sliders_h' icon. */
    SLIDERS_H("\uf1de"),

    /** A constant used to reference the 'smile' icon. */
    SMILE("\uf118"),

    /** A constant used to reference the 'smoking' icon. */
    SMOKING("\uf48d"),

    /** A constant used to reference the 'smoking_ban' icon. */
    SMOKING_BAN("\uf54d"),

    /** A constant used to reference the 'snowflake' icon. */
    SNOWFLAKE("\uf2dc"),

    /** A constant used to reference the 'sort' icon. */
    SORT("\uf0dc"),

    /** A constant used to reference the 'sort_alpha_down' icon. */
    SORT_ALPHA_DOWN("\uf15d"),

    /** A constant used to reference the 'sort_alpha_up' icon. */
    SORT_ALPHA_UP("\uf15e"),

    /** A constant used to reference the 'sort_amount_down' icon. */
    SORT_AMOUNT_DOWN("\uf160"),

    /** A constant used to reference the 'sort_amount_up' icon. */
    SORT_AMOUNT_UP("\uf161"),

    /** A constant used to reference the 'sort_down' icon. */
    SORT_DOWN("\uf0dd"),

    /** A constant used to reference the 'sort_numeric_down' icon. */
    SORT_NUMERIC_DOWN("\uf162"),

    /** A constant used to reference the 'sort_numeric_up' icon. */
    SORT_NUMERIC_UP("\uf163"),

    /** A constant used to reference the 'sort_up' icon. */
    SORT_UP("\uf0de"),

    /** A constant used to reference the 'space_shuttle' icon. */
    SPACE_SHUTTLE("\uf197"),

    /** A constant used to reference the 'spinner' icon. */
    SPINNER("\uf110"),

    /** A constant used to reference the 'square' icon. */
    SQUARE("\uf0c8"),

    /** A constant used to reference the 'square_full' icon. */
    SQUARE_FULL("\uf45c"),

    /** A constant used to reference the 'star' icon. */
    STAR("\uf005"),

    /** A constant used to reference the 'star_half' icon. */
    STAR_HALF("\uf089"),

    /** A constant used to reference the 'step_backward' icon. */
    STEP_BACKWARD("\uf048"),

    /** A constant used to reference the 'step_forward' icon. */
    STEP_FORWARD("\uf051"),

    /** A constant used to reference the 'stethoscope' icon. */
    STETHOSCOPE("\uf0f1"),

    /** A constant used to reference the 'sticky_note' icon. */
    STICKY_NOTE("\uf249"),

    /** A constant used to reference the 'stop' icon. */
    STOP("\uf04d"),

    /** A constant used to reference the 'stop_circle' icon. */
    STOP_CIRCLE("\uf28d"),

    /** A constant used to reference the 'stopwatch' icon. */
    STOPWATCH("\uf2f2"),

    /** A constant used to reference the 'store' icon. */
    STORE("\uf54e"),

    /** A constant used to reference the 'store_alt' icon. */
    STORE_ALT("\uf54f"),

    /** A constant used to reference the 'stream' icon. */
    STREAM("\uf550"),

    /** A constant used to reference the 'street_view' icon. */
    STREET_VIEW("\uf21d"),

    /** A constant used to reference the 'strikethrough' icon. */
    STRIKETHROUGH("\uf0cc"),

    /** A constant used to reference the 'stroopwafel' icon. */
    STROOPWAFEL("\uf551"),

    /** A constant used to reference the 'subscript' icon. */
    SUBSCRIPT("\uf12c"),

    /** A constant used to reference the 'subway' icon. */
    SUBWAY("\uf239"),

    /** A constant used to reference the 'suitcase' icon. */
    SUITCASE("\uf0f2"),

    /** A constant used to reference the 'sun' icon. */
    SUN("\uf185"),

    /** A constant used to reference the 'superscript' icon. */
    SUPERSCRIPT("\uf12b"),

    /** A constant used to reference the 'sync' icon. */
    SYNC("\uf021"),

    /** A constant used to reference the 'sync_alt' icon. */
    SYNC_ALT("\uf2f1"),

    /** A constant used to reference the 'syringe' icon. */
    SYRINGE("\uf48e"),

    /** A constant used to reference the 'table' icon. */
    TABLE("\uf0ce"),

    /** A constant used to reference the 'table_tennis' icon. */
    TABLE_TENNIS("\uf45d"),

    /** A constant used to reference the 'tablet' icon. */
    TABLET("\uf10a"),

    /** A constant used to reference the 'tablet_alt' icon. */
    TABLET_ALT("\uf3fa"),

    /** A constant used to reference the 'tablets' icon. */
    TABLETS("\uf490"),

    /** A constant used to reference the 'tachometer_alt' icon. */
    TACHOMETER_ALT("\uf3fd"),

    /** A constant used to reference the 'tag' icon. */
    TAG("\uf02b"),

    /** A constant used to reference the 'tags' icon. */
    TAGS("\uf02c"),

    /** A constant used to reference the 'tape' icon. */
    TAPE("\uf4db"),

    /** A constant used to reference the 'tasks' icon. */
    TASKS("\uf0ae"),

    /** A constant used to reference the 'taxi' icon. */
    TAXI("\uf1ba"),

    /** A constant used to reference the 'terminal' icon. */
    TERMINAL("\uf120"),

    /** A constant used to reference the 'text_height' icon. */
    TEXT_HEIGHT("\uf034"),

    /** A constant used to reference the 'text_width' icon. */
    TEXT_WIDTH("\uf035"),

    /** A constant used to reference the 'th' icon. */
    TH("\uf00a"),

    /** A constant used to reference the 'th_large' icon. */
    TH_LARGE("\uf009"),

    /** A constant used to reference the 'th_list' icon. */
    TH_LIST("\uf00b"),

    /** A constant used to reference the 'thermometer' icon. */
    THERMOMETER("\uf491"),

    /** A constant used to reference the 'thermometer_empty' icon. */
    THERMOMETER_EMPTY("\uf2cb"),

    /** A constant used to reference the 'thermometer_full' icon. */
    THERMOMETER_FULL("\uf2c7"),

    /** A constant used to reference the 'thermometer_half' icon. */
    THERMOMETER_HALF("\uf2c9"),

    /** A constant used to reference the 'thermometer_quarter' icon. */
    THERMOMETER_QUARTER("\uf2ca"),

    /** A constant used to reference the 'thermometer_three_quarters' icon. */
    THERMOMETER_THREE_QUARTERS("\uf2c8"),

    /** A constant used to reference the 'thumbs_down' icon. */
    THUMBS_DOWN("\uf165"),

    /** A constant used to reference the 'thumbs_up' icon. */
    THUMBS_UP("\uf164"),

    /** A constant used to reference the 'thumbtack' icon. */
    THUMBTACK("\uf08d"),

    /** A constant used to reference the 'ticket_alt' icon. */
    TICKET_ALT("\uf3ff"),

    /** A constant used to reference the 'times' icon. */
    TIMES("\uf00d"),

    /** A constant used to reference the 'times_circle' icon. */
    TIMES_CIRCLE("\uf057"),

    /** A constant used to reference the 'tint' icon. */
    TINT("\uf043"),

    /** A constant used to reference the 'toggle_off' icon. */
    TOGGLE_OFF("\uf204"),

    /** A constant used to reference the 'toggle_on' icon. */
    TOGGLE_ON("\uf205"),

    /** A constant used to reference the 'toolbox' icon. */
    TOOLBOX("\uf552"),

    /** A constant used to reference the 'trademark' icon. */
    TRADEMARK("\uf25c"),

    /** A constant used to reference the 'train' icon. */
    TRAIN("\uf238"),

    /** A constant used to reference the 'transgender' icon. */
    TRANSGENDER("\uf224"),

    /** A constant used to reference the 'transgender_alt' icon. */
    TRANSGENDER_ALT("\uf225"),

    /** A constant used to reference the 'trash' icon. */
    TRASH("\uf1f8"),

    /** A constant used to reference the 'trash_alt' icon. */
    TRASH_ALT("\uf2ed"),

    /** A constant used to reference the 'tree' icon. */
    TREE("\uf1bb"),

    /** A constant used to reference the 'trophy' icon. */
    TROPHY("\uf091"),

    /** A constant used to reference the 'truck' icon. */
    TRUCK("\uf0d1"),

    /** A constant used to reference the 'truck_loading' icon. */
    TRUCK_LOADING("\uf4de"),

    /** A constant used to reference the 'truck_moving' icon. */
    TRUCK_MOVING("\uf4df"),

    /** A constant used to reference the 'tshirt' icon. */
    TSHIRT("\uf553"),

    /** A constant used to reference the 'tty' icon. */
    TTY("\uf1e4"),

    /** A constant used to reference the 'tv' icon. */
    TV("\uf26c"),

    /** A constant used to reference the 'umbrella' icon. */
    UMBRELLA("\uf0e9"),

    /** A constant used to reference the 'underline' icon. */
    UNDERLINE("\uf0cd"),

    /** A constant used to reference the 'undo' icon. */
    UNDO("\uf0e2"),

    /** A constant used to reference the 'undo_alt' icon. */
    UNDO_ALT("\uf2ea"),

    /** A constant used to reference the 'universal_access' icon. */
    UNIVERSAL_ACCESS("\uf29a"),

    /** A constant used to reference the 'university' icon. */
    UNIVERSITY("\uf19c"),

    /** A constant used to reference the 'unlink' icon. */
    UNLINK("\uf127"),

    /** A constant used to reference the 'unlock' icon. */
    UNLOCK("\uf09c"),

    /** A constant used to reference the 'unlock_alt' icon. */
    UNLOCK_ALT("\uf13e"),

    /** A constant used to reference the 'upload' icon. */
    UPLOAD("\uf093"),

    /** A constant used to reference the 'user' icon. */
    USER("\uf007"),

    /** A constant used to reference the 'user_alt' icon. */
    USER_ALT("\uf406"),

    /** A constant used to reference the 'user_alt_slash' icon. */
    USER_ALT_SLASH("\uf4fa"),

    /** A constant used to reference the 'user_astronaut' icon. */
    USER_ASTRONAUT("\uf4fb"),

    /** A constant used to reference the 'user_check' icon. */
    USER_CHECK("\uf4fc"),

    /** A constant used to reference the 'user_circle' icon. */
    USER_CIRCLE("\uf2bd"),

    /** A constant used to reference the 'user_clock' icon. */
    USER_CLOCK("\uf4fd"),

    /** A constant used to reference the 'user_cog' icon. */
    USER_COG("\uf4fe"),

    /** A constant used to reference the 'user_edit' icon. */
    USER_EDIT("\uf4ff"),

    /** A constant used to reference the 'user_friends' icon. */
    USER_FRIENDS("\uf500"),

    /** A constant used to reference the 'user_graduate' icon. */
    USER_GRADUATE("\uf501"),

    /** A constant used to reference the 'user_lock' icon. */
    USER_LOCK("\uf502"),

    /** A constant used to reference the 'user_md' icon. */
    USER_MD("\uf0f0"),

    /** A constant used to reference the 'user_minus' icon. */
    USER_MINUS("\uf503"),

    /** A constant used to reference the 'user_ninja' icon. */
    USER_NINJA("\uf504"),

    /** A constant used to reference the 'user_plus' icon. */
    USER_PLUS("\uf234"),

    /** A constant used to reference the 'user_secret' icon. */
    USER_SECRET("\uf21b"),

    /** A constant used to reference the 'user_shield' icon. */
    USER_SHIELD("\uf505"),

    /** A constant used to reference the 'user_slash' icon. */
    USER_SLASH("\uf506"),

    /** A constant used to reference the 'user_tag' icon. */
    USER_TAG("\uf507"),

    /** A constant used to reference the 'user_tie' icon. */
    USER_TIE("\uf508"),

    /** A constant used to reference the 'user_times' icon. */
    USER_TIMES("\uf235"),

    /** A constant used to reference the 'users' icon. */
    USERS("\uf0c0"),

    /** A constant used to reference the 'users_cog' icon. */
    USERS_COG("\uf509"),

    /** A constant used to reference the 'utensil_spoon' icon. */
    UTENSIL_SPOON("\uf2e5"),

    /** A constant used to reference the 'utensils' icon. */
    UTENSILS("\uf2e7"),

    /** A constant used to reference the 'venus' icon. */
    VENUS("\uf221"),

    /** A constant used to reference the 'venus_double' icon. */
    VENUS_DOUBLE("\uf226"),

    /** A constant used to reference the 'venus_mars' icon. */
    VENUS_MARS("\uf228"),

    /** A constant used to reference the 'vial' icon. */
    VIAL("\uf492"),

    /** A constant used to reference the 'vials' icon. */
    VIALS("\uf493"),

    /** A constant used to reference the 'video' icon. */
    VIDEO("\uf03d"),

    /** A constant used to reference the 'video_slash' icon. */
    VIDEO_SLASH("\uf4e2"),

    /** A constant used to reference the 'volleyball_ball' icon. */
    VOLLEYBALL_BALL("\uf45f"),

    /** A constant used to reference the 'volume_down' icon. */
    VOLUME_DOWN("\uf027"),

    /** A constant used to reference the 'volume_off' icon. */
    VOLUME_OFF("\uf026"),

    /** A constant used to reference the 'volume_up' icon. */
    VOLUME_UP("\uf028"),

    /** A constant used to reference the 'walking' icon. */
    WALKING("\uf554"),

    /** A constant used to reference the 'wallet' icon. */
    WALLET("\uf555"),

    /** A constant used to reference the 'warehouse' icon. */
    WAREHOUSE("\uf494"),

    /** A constant used to reference the 'weight' icon. */
    WEIGHT("\uf496"),

    /** A constant used to reference the 'wheelchair' icon. */
    WHEELCHAIR("\uf193"),

    /** A constant used to reference the 'wifi' icon. */
    WIFI("\uf1eb"),

    /** A constant used to reference the 'window_close' icon. */
    WINDOW_CLOSE("\uf410"),

    /** A constant used to reference the 'window_maximize' icon. */
    WINDOW_MAXIMIZE("\uf2d0"),

    /** A constant used to reference the 'window_minimize' icon. */
    WINDOW_MINIMIZE("\uf2d1"),

    /** A constant used to reference the 'window_restore' icon. */
    WINDOW_RESTORE("\uf2d2"),

    /** A constant used to reference the 'wine_glass' icon. */
    WINE_GLASS("\uf4e3"),

    /** A constant used to reference the 'won_sign' icon. */
    WON_SIGN("\uf159"),

    /** A constant used to reference the 'wrench' icon. */
    WRENCH("\uf0ad"),

    /** A constant used to reference the 'x_ray' icon. */
    X_RAY("\uf497"),

    /** A constant used to reference the 'yen_sign' icon. */
    YEN_SIGN("\uf157");

    static
    {
        GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(SwingUtilities.FONT_AWESOME_SOLID_FONT);
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
    private AwesomeIconSolid(String pFontCode)
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
        return SwingUtilities.FONT_AWESOME_SOLID_FONT;
    }
}
