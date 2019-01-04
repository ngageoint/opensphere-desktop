package io.opensphere.core.util;

import java.awt.Font;
import java.awt.GraphicsEnvironment;

import io.opensphere.core.util.swing.SwingUtilities;

/**
 * An enumeration over the set of available WebHostingHub Glyph icons.
 */
public enum WebHostingHubGlyphs implements FontIconEnum
{
    /** A constant used to reference the 'aaabattery' icon. */
    AAABATTERY("\uf413"),
    /** A constant used to reference the 'abacus' icon. */
    ABACUS("\uf261"),
    /** A constant used to reference the 'accountfilter' icon. */
    ACCOUNTFILTER("\uf05e"),
    /** A constant used to reference the 'acsource' icon. */
    ACSOURCE("\uf3ea"),
    /** A constant used to reference the 'addfriend' icon. */
    ADDFRIEND("\uf3da"),
    /** A constant used to reference the 'address' icon. */
    ADDRESS("\uf08f"),
    /** A constant used to reference the 'addshape' icon. */
    ADDSHAPE("\uf1fd"),
    /** A constant used to reference the 'addtocart' icon. */
    ADDTOCART("\uf394"),
    /** A constant used to reference the 'addtolist' icon. */
    ADDTOLIST("\uf2ac"),
    /** A constant used to reference the 'adjust' icon. */
    ADJUST("\uf484"),
    /** A constant used to reference the 'adobe' icon. */
    ADOBE("\uf1c9"),
    /** A constant used to reference the 'ads_bilboard' icon. */
    ADS_BILBOARD("\uf082"),
    /** A constant used to reference the 'affiliate' icon. */
    AFFILIATE("\uf01e"),
    /** A constant used to reference the 'ajax' icon. */
    AJAX("\uf06f"),
    /** A constant used to reference the 'alarm' icon. */
    ALARM("\uf233"),
    /** A constant used to reference the 'alarmalt' icon. */
    ALARMALT("\uf23d"),
    /** A constant used to reference the 'album_cover' icon. */
    ALBUM_COVER("\uf19f"),
    /** A constant used to reference the 'alertalt' icon. */
    ALERTALT("\uf2b4"),
    /** A constant used to reference the 'alertpay' icon. */
    ALERTPAY("\uf269"),
    /** A constant used to reference the 'algorhythm' icon. */
    ALGORHYTHM("\uf0b8"),
    /** A constant used to reference the 'alienship' icon. */
    ALIENSHIP("\uf41f"),
    /** A constant used to reference the 'alienware' icon. */
    ALIENWARE("\uf3be"),
    /** A constant used to reference the 'align_center' icon. */
    ALIGN_CENTER("\uf1d9"),
    /** A constant used to reference the 'align_justify' icon. */
    ALIGN_JUSTIFY("\uf1da"),
    /** A constant used to reference the 'align_left' icon. */
    ALIGN_LEFT("\uf1d7"),
    /** A constant used to reference the 'align_right' icon. */
    ALIGN_RIGHT("\uf1d8"),
    /** A constant used to reference the 'alignbottomedge' icon. */
    ALIGNBOTTOMEDGE("\uf1d3"),
    /** A constant used to reference the 'alignhorizontalcenter' icon. */
    ALIGNHORIZONTALCENTER("\uf1d2"),
    /** A constant used to reference the 'alignleftedge' icon. */
    ALIGNLEFTEDGE("\uf1d6"),
    /** A constant used to reference the 'alignrightedge' icon. */
    ALIGNRIGHTEDGE("\uf1d5"),
    /** A constant used to reference the 'aligntopedge' icon. */
    ALIGNTOPEDGE("\uf1d4"),
    /** A constant used to reference the 'alignverticalcenter' icon. */
    ALIGNVERTICALCENTER("\uf1d1"),
    /** A constant used to reference the 'amd' icon. */
    AMD("\uf020"),
    /** A constant used to reference the 'analogdown' icon. */
    ANALOGDOWN("\uf2cb"),
    /** A constant used to reference the 'analogleft' icon. */
    ANALOGLEFT("\uf2c8"),
    /** A constant used to reference the 'analogright' icon. */
    ANALOGRIGHT("\uf2c9"),
    /** A constant used to reference the 'analogup' icon. */
    ANALOGUP("\uf2ca"),
    /** A constant used to reference the 'analytics_piechart' icon. */
    ANALYTICS_PIECHART("\uf000"),
    /** A constant used to reference the 'analyticsalt_piechartalt' icon. */
    ANALYTICSALT_PIECHARTALT("\uf001"),
    /** A constant used to reference the 'anchor_port' icon. */
    ANCHOR_PORT("\uf21d"),
    /** A constant used to reference the 'android' icon. */
    ANDROID("\uf12a"),
    /** A constant used to reference the 'angrybirds' icon. */
    ANGRYBIRDS("\uf3c1"),
    /** A constant used to reference the 'antenna' icon. */
    ANTENNA("\uf3ec"),
    /** A constant used to reference the 'apache_feather' icon. */
    APACHE_FEATHER("\uf056"),
    /** A constant used to reference the 'aperture' icon. */
    APERTURE("\uf356"),
    /** A constant used to reference the 'appointment_agenda' icon. */
    APPOINTMENT_AGENDA("\uf26c"),
    /** A constant used to reference the 'archive' icon. */
    ARCHIVE("\uf171"),
    /** A constant used to reference the 'arrow_down' icon. */
    ARROW_DOWN("\uf2fe"),
    /** A constant used to reference the 'arrow_left' icon. */
    ARROW_LEFT("\uf305"),
    /** A constant used to reference the 'arrow_right' icon. */
    ARROW_RIGHT("\uf304"),
    /** A constant used to reference the 'arrow_up' icon. */
    ARROW_UP("\uf301"),
    /** A constant used to reference the 'asterisk' icon. */
    ASTERISK("\uf317"),
    /** A constant used to reference the 'asteriskalt' icon. */
    ASTERISKALT("\002a"),
    /** A constant used to reference the 'at' icon. */
    AT("\40"),
    /** A constant used to reference the 'atari' icon. */
    ATARI("\uf3b9"),
    /** A constant used to reference the 'authentication_keyalt' icon. */
    AUTHENTICATION_KEYALT("\uf051"),
    /** A constant used to reference the 'automobile_car' icon. */
    AUTOMOBILE_CAR("\uf239"),
    /** A constant used to reference the 'autorespond' icon. */
    AUTORESPOND("\uf08e"),
    /** A constant used to reference the 'avatar' icon. */
    AVATAR("\uf15a"),
    /** A constant used to reference the 'avataralt' icon. */
    AVATARALT("\uf161"),
    /** A constant used to reference the 'avengers' icon. */
    AVENGERS("\uf342"),
    /** A constant used to reference the 'awstats' icon. */
    AWSTATS("\uf04c"),
    /** A constant used to reference the 'axe' icon. */
    AXE("\uf2ef"),
    /** A constant used to reference the 'backup_vault' icon. */
    BACKUP_VAULT("\uf004"),
    /** A constant used to reference the 'backupalt_vaultalt' icon. */
    BACKUPALT_VAULTALT("\uf005"),
    /** A constant used to reference the 'backupwizard' icon. */
    BACKUPWIZARD("\uf05f"),
    /** A constant used to reference the 'backward' icon. */
    BACKWARD("\uf183"),
    /** A constant used to reference the 'bag' icon. */
    BAG("\uf234"),
    /** A constant used to reference the 'baloon' icon. */
    BALOON("\uf405"),
    /** A constant used to reference the 'ban_circle' icon. */
    BAN_CIRCLE("\uf313"),
    /** A constant used to reference the 'banana' icon. */
    BANANA("\uf3f4"),
    /** A constant used to reference the 'bandwidth' icon. */
    BANDWIDTH("\uf006"),
    /** A constant used to reference the 'bank' icon. */
    BANK("\uf262"),
    /** A constant used to reference the 'barchart' icon. */
    BARCHART("\uf02f"),
    /** A constant used to reference the 'barchartalt' icon. */
    BARCHARTALT("\uf07d"),
    /** A constant used to reference the 'barcode' icon. */
    BARCODE("\uf276"),
    /** A constant used to reference the 'basecamp' icon. */
    BASECAMP("\uf160"),
    /** A constant used to reference the 'basketball' icon. */
    BASKETBALL("\uf2e9"),
    /** A constant used to reference the 'bat' icon. */
    BAT("\uf3d3"),
    /** A constant used to reference the 'batman' icon. */
    BATMAN("\uf348"),
    /** A constant used to reference the 'batteryaltcharging' icon. */
    BATTERYALTCHARGING("\uf104"),
    /** A constant used to reference the 'batteryaltfull' icon. */
    BATTERYALTFULL("\uf101"),
    /** A constant used to reference the 'batteryaltsixty' icon. */
    BATTERYALTSIXTY("\uf102"),
    /** A constant used to reference the 'batteryaltthird' icon. */
    BATTERYALTTHIRD("\uf103"),
    /** A constant used to reference the 'batterycharged' icon. */
    BATTERYCHARGED("\uf0f4"),
    /** A constant used to reference the 'batterycharging' icon. */
    BATTERYCHARGING("\uf0f3"),
    /** A constant used to reference the 'batteryeighty' icon. */
    BATTERYEIGHTY("\uf0f9"),
    /** A constant used to reference the 'batteryempty' icon. */
    BATTERYEMPTY("\uf0f5"),
    /** A constant used to reference the 'batteryforty' icon. */
    BATTERYFORTY("\uf0f7"),
    /** A constant used to reference the 'batteryfull' icon. */
    BATTERYFULL("\uf0fa"),
    /** A constant used to reference the 'batterysixty' icon. */
    BATTERYSIXTY("\uf0f8"),
    /** A constant used to reference the 'batterytwenty' icon. */
    BATTERYTWENTY("\uf0f6"),
    /** A constant used to reference the 'bed' icon. */
    BED("\uf2b9"),
    /** A constant used to reference the 'beer' icon. */
    BEER("\uf244"),
    /** A constant used to reference the 'bell' icon. */
    BELL("\2407"),
    /** A constant used to reference the 'bigger' icon. */
    BIGGER("\uf30a"),
    /** A constant used to reference the 'bill' icon. */
    BILL("\uf278"),
    /** A constant used to reference the 'binary' icon. */
    BINARY("\uf087"),
    /** A constant used to reference the 'binoculars_searchalt' icon. */
    BINOCULARS_SEARCHALT("\uf2a0"),
    /** A constant used to reference the 'birdhouse' icon. */
    BIRDHOUSE("\uf390"),
    /** A constant used to reference the 'birthday' icon. */
    BIRTHDAY("\uf36b"),
    /** A constant used to reference the 'bishop' icon. */
    BISHOP("\uf2f9"),
    /** A constant used to reference the 'blackberry' icon. */
    BLACKBERRY("\uf421"),
    /** A constant used to reference the 'blankstare' icon. */
    BLANKSTARE("\uf13e"),
    /** A constant used to reference the 'blogger_blog' icon. */
    BLOGGER_BLOG("\uf167"),
    /** A constant used to reference the 'bluetooth' icon. */
    BLUETOOTH("\uf12b"),
    /** A constant used to reference the 'bluetoothconnected' icon. */
    BLUETOOTHCONNECTED("\uf386"),
    /** A constant used to reference the 'boardgame' icon. */
    BOARDGAME("\uf2d9"),
    /** A constant used to reference the 'boat' icon. */
    BOAT("\uf21a"),
    /** A constant used to reference the 'bold' icon. */
    BOLD("\uf1f4"),
    /** A constant used to reference the 'bomb' icon. */
    BOMB("\uf2dc"),
    /** A constant used to reference the 'bone' icon. */
    BONE("\uf35f"),
    /** A constant used to reference the 'book' icon. */
    BOOK("\uf1ba"),
    /** A constant used to reference the 'bookmark' icon. */
    BOOKMARK("\uf143"),
    /** A constant used to reference the 'boombox' icon. */
    BOOMBOX("\uf195"),
    /** A constant used to reference the 'bottle' icon. */
    BOTTLE("\uf361"),
    /** A constant used to reference the 'bow' icon. */
    BOW("\uf2ee"),
    /** A constant used to reference the 'bowling' icon. */
    BOWLING("\uf2f3"),
    /** A constant used to reference the 'bowlingpins' icon. */
    BOWLINGPINS("\uf3d2"),
    /** A constant used to reference the 'bowtie' icon. */
    BOWTIE("\uf37f"),
    /** A constant used to reference the 'boxtrapper_mousetrap' icon. */
    BOXTRAPPER_MOUSETRAP("\uf046"),
    /** A constant used to reference the 'braces' icon. */
    BRACES("\uf0b4"),
    /** A constant used to reference the 'braille0' icon. */
    BRAILLE0("\uf44b"),
    /** A constant used to reference the 'braille1' icon. */
    BRAILLE1("\uf44c"),
    /** A constant used to reference the 'braille2' icon. */
    BRAILLE2("\uf44d"),
    /** A constant used to reference the 'braille3' icon. */
    BRAILLE3("\uf44e"),
    /** A constant used to reference the 'braille4' icon. */
    BRAILLE4("\uf44f"),
    /** A constant used to reference the 'braille5' icon. */
    BRAILLE5("\uf450"),
    /** A constant used to reference the 'braille6' icon. */
    BRAILLE6("\uf451"),
    /** A constant used to reference the 'braille7' icon. */
    BRAILLE7("\uf452"),
    /** A constant used to reference the 'braille8' icon. */
    BRAILLE8("\uf453"),
    /** A constant used to reference the 'braille9' icon. */
    BRAILLE9("\uf454"),
    /** A constant used to reference the 'braillea' icon. */
    BRAILLEA("\uf431"),
    /** A constant used to reference the 'brailleb' icon. */
    BRAILLEB("\uf432"),
    /** A constant used to reference the 'braillec' icon. */
    BRAILLEC("\uf433"),
    /** A constant used to reference the 'brailled' icon. */
    BRAILLED("\uf434"),
    /** A constant used to reference the 'braillee' icon. */
    BRAILLEE("\uf435"),
    /** A constant used to reference the 'braillef' icon. */
    BRAILLEF("\uf436"),
    /** A constant used to reference the 'brailleg' icon. */
    BRAILLEG("\uf437"),
    /** A constant used to reference the 'brailleh' icon. */
    BRAILLEH("\uf438"),
    /** A constant used to reference the 'braillei' icon. */
    BRAILLEI("\uf439"),
    /** A constant used to reference the 'braillej' icon. */
    BRAILLEJ("\uf43a"),
    /** A constant used to reference the 'braillek' icon. */
    BRAILLEK("\uf43b"),
    /** A constant used to reference the 'braillel' icon. */
    BRAILLEL("\uf43c"),
    /** A constant used to reference the 'braillem' icon. */
    BRAILLEM("\uf43d"),
    /** A constant used to reference the 'braillen' icon. */
    BRAILLEN("\uf43e"),
    /** A constant used to reference the 'brailleo' icon. */
    BRAILLEO("\uf43f"),
    /** A constant used to reference the 'braillep' icon. */
    BRAILLEP("\uf440"),
    /** A constant used to reference the 'brailleq' icon. */
    BRAILLEQ("\uf441"),
    /** A constant used to reference the 'brailler' icon. */
    BRAILLER("\uf442"),
    /** A constant used to reference the 'brailles' icon. */
    BRAILLES("\uf443"),
    /** A constant used to reference the 'braillespace' icon. */
    BRAILLESPACE("\uf455"),
    /** A constant used to reference the 'braillet' icon. */
    BRAILLET("\uf444"),
    /** A constant used to reference the 'brailleu' icon. */
    BRAILLEU("\uf445"),
    /** A constant used to reference the 'braillev' icon. */
    BRAILLEV("\uf446"),
    /** A constant used to reference the 'braillew' icon. */
    BRAILLEW("\uf447"),
    /** A constant used to reference the 'braillex' icon. */
    BRAILLEX("\uf448"),
    /** A constant used to reference the 'brailley' icon. */
    BRAILLEY("\uf449"),
    /** A constant used to reference the 'braillez' icon. */
    BRAILLEZ("\uf44a"),
    /** A constant used to reference the 'brain' icon. */
    BRAIN("\uf3e3"),
    /** A constant used to reference the 'bread' icon. */
    BREAD("\uf42f"),
    /** A constant used to reference the 'breakable' icon. */
    BREAKABLE("\uf41c"),
    /** A constant used to reference the 'briefcase' icon. */
    BRIEFCASE("\uf25e"),
    /** A constant used to reference the 'briefcasethree' icon. */
    BRIEFCASETHREE("\uf25f"),
    /** A constant used to reference the 'briefcasetwo' icon. */
    BRIEFCASETWO("\uf0a2"),
    /** A constant used to reference the 'brightness' icon. */
    BRIGHTNESS("\uf10a"),
    /** A constant used to reference the 'brightnessfull' icon. */
    BRIGHTNESSFULL("\uf10b"),
    /** A constant used to reference the 'brightnesshalf' icon. */
    BRIGHTNESSHALF("\uf10c"),
    /** A constant used to reference the 'broom' icon. */
    BROOM("\uf40a"),
    /** A constant used to reference the 'browser' icon. */
    BROWSER("\uf159"),
    /** A constant used to reference the 'brush' icon. */
    BRUSH("\uf1b8"),
    /** A constant used to reference the 'bucket' icon. */
    BUCKET("\uf1b5"),
    /** A constant used to reference the 'bug' icon. */
    BUG("\uf0a7"),
    /** A constant used to reference the 'bullhorn' icon. */
    BULLHORN("\uf287"),
    /** A constant used to reference the 'bus' icon. */
    BUS("\uf241"),
    /** A constant used to reference the 'businesscardalt' icon. */
    BUSINESSCARDALT("\uf137"),
    /** A constant used to reference the 'buttona' icon. */
    BUTTONA("\uf2bf"),
    /** A constant used to reference the 'buttonb' icon. */
    BUTTONB("\uf2c0"),
    /** A constant used to reference the 'buttonx' icon. */
    BUTTONX("\uf2c1"),
    /** A constant used to reference the 'buttony' icon. */
    BUTTONY("\uf2c2"),
    /** A constant used to reference the 'cactus_desert' icon. */
    CACTUS_DESERT("\uf22c"),
    /** A constant used to reference the 'calculator' icon. */
    CALCULATOR("\uf258"),
    /** A constant used to reference the 'calculatoralt' icon. */
    CALCULATORALT("\uf265"),
    /** A constant used to reference the 'calendar' icon. */
    CALENDAR("\uf20f"),
    /** A constant used to reference the 'calendaralt_cronjobs' icon. */
    CALENDARALT_CRONJOBS("\uf0a1"),
    /** A constant used to reference the 'camera' icon. */
    CAMERA("\uf19b"),
    /** A constant used to reference the 'candle' icon. */
    CANDLE("\uf29a"),
    /** A constant used to reference the 'candy' icon. */
    CANDY("\uf42d"),
    /** A constant used to reference the 'candycane' icon. */
    CANDYCANE("\uf37d"),
    /** A constant used to reference the 'cannon' icon. */
    CANNON("\uf401"),
    /** A constant used to reference the 'canvas' icon. */
    CANVAS("\uf1c8"),
    /** A constant used to reference the 'canvasrulers' icon. */
    CANVASRULERS("\uf205"),
    /** A constant used to reference the 'capacitator' icon. */
    CAPACITATOR("\uf3e8"),
    /** A constant used to reference the 'capslock' icon. */
    CAPSLOCK("\21ea"),
    /** A constant used to reference the 'captainamerica' icon. */
    CAPTAINAMERICA("\uf341"),
    /** A constant used to reference the 'carrot' icon. */
    CARROT("\uf3f2"),
    /** A constant used to reference the 'cashregister' icon. */
    CASHREGISTER("\uf26e"),
    /** A constant used to reference the 'cassette' icon. */
    CASSETTE("\uf377"),
    /** A constant used to reference the 'cd_dvd' icon. */
    CD_DVD("\uf0cd"),
    /** A constant used to reference the 'certificate' icon. */
    CERTIFICATE("\uf277"),
    /** A constant used to reference the 'certificatealt' icon. */
    CERTIFICATEALT("\uf058"),
    /** A constant used to reference the 'certificatethree' icon. */
    CERTIFICATETHREE("\uf059"),
    /** A constant used to reference the 'cgi' icon. */
    CGI("\uf086"),
    /** A constant used to reference the 'cgicenter' icon. */
    CGICENTER("\uf079"),
    /** A constant used to reference the 'chair' icon. */
    CHAIR("\2441"),
    /** A constant used to reference the 'chat' icon. */
    CHAT("\uf162"),
    /** A constant used to reference the 'check' icon. */
    CHECK("\uf310"),
    /** A constant used to reference the 'checkboxalt' icon. */
    CHECKBOXALT("\uf311"),
    /** A constant used to reference the 'checkin' icon. */
    CHECKIN("\uf223"),
    /** A constant used to reference the 'checkinalt' icon. */
    CHECKINALT("\uf227"),
    /** A constant used to reference the 'chef' icon. */
    CHEF("\uf3ce"),
    /** A constant used to reference the 'cherry' icon. */
    CHERRY("\uf35d"),
    /** A constant used to reference the 'chevron_down' icon. */
    CHEVRON_DOWN("\uf48b"),
    /** A constant used to reference the 'chevron_left' icon. */
    CHEVRON_LEFT("\uf489"),
    /** A constant used to reference the 'chevron_right' icon. */
    CHEVRON_RIGHT("\uf488"),
    /** A constant used to reference the 'chevron_up' icon. */
    CHEVRON_UP("\uf48a"),
    /** A constant used to reference the 'chevrons' icon. */
    CHEVRONS("\uf0b5"),
    /** A constant used to reference the 'chicken' icon. */
    CHICKEN("\uf359"),
    /** A constant used to reference the 'chocolate' icon. */
    CHOCOLATE("\uf367"),
    /** A constant used to reference the 'christiancross' icon. */
    CHRISTIANCROSS("\uf40f"),
    /** A constant used to reference the 'christmastree' icon. */
    CHRISTMASTREE("\uf37b"),
    /** A constant used to reference the 'chrome' icon. */
    CHROME("\uf14e"),
    /** A constant used to reference the 'cigarette' icon. */
    CIGARETTE("\uf229"),
    /** A constant used to reference the 'circle_arrow_down' icon. */
    CIRCLE_ARROW_DOWN("\uf475"),
    /** A constant used to reference the 'circle_arrow_left' icon. */
    CIRCLE_ARROW_LEFT("\uf472"),
    /** A constant used to reference the 'circle_arrow_right' icon. */
    CIRCLE_ARROW_RIGHT("\uf473"),
    /** A constant used to reference the 'circle_arrow_up' icon. */
    CIRCLE_ARROW_UP("\uf474"),
    /** A constant used to reference the 'circleadd' icon. */
    CIRCLEADD("\uf0d1"),
    /** A constant used to reference the 'circledelete' icon. */
    CIRCLEDELETE("\uf0d2"),
    /** A constant used to reference the 'circledown' icon. */
    CIRCLEDOWN("\uf3c7"),
    /** A constant used to reference the 'circleleft' icon. */
    CIRCLELEFT("\uf3c6"),
    /** A constant used to reference the 'circleright' icon. */
    CIRCLERIGHT("\uf3c9"),
    /** A constant used to reference the 'circleselect' icon. */
    CIRCLESELECT("\uf0d3"),
    /** A constant used to reference the 'circleselection' icon. */
    CIRCLESELECTION("\uf1b1"),
    /** A constant used to reference the 'circleup' icon. */
    CIRCLEUP("\uf3c8"),
    /** A constant used to reference the 'clearformatting' icon. */
    CLEARFORMATTING("\uf1e7"),
    /** A constant used to reference the 'clipboard_paste' icon. */
    CLIPBOARD_PASTE("\uf0cb"),
    /** A constant used to reference the 'clockalt_timealt' icon. */
    CLOCKALT_TIMEALT("\uf22b"),
    /** A constant used to reference the 'closetab' icon. */
    CLOSETAB("\uf170"),
    /** A constant used to reference the 'closewindow' icon. */
    CLOSEWINDOW("\uf16e"),
    /** A constant used to reference the 'cloud' icon. */
    CLOUD("\uf0b9"),
    /** A constant used to reference the 'clouddownload' icon. */
    CLOUDDOWNLOAD("\uf0bb"),
    /** A constant used to reference the 'cloudhosting' icon. */
    CLOUDHOSTING("\uf007"),
    /** A constant used to reference the 'cloudsync' icon. */
    CLOUDSYNC("\uf0bc"),
    /** A constant used to reference the 'cloudupload' icon. */
    CLOUDUPLOAD("\uf0ba"),
    /** A constant used to reference the 'clubs' icon. */
    CLUBS("\uf2f6"),
    /** A constant used to reference the 'cmd' icon. */
    CMD("\uf33a"),
    /** A constant used to reference the 'cms' icon. */
    CMS("\uf036"),
    /** A constant used to reference the 'cmsmadesimple' icon. */
    CMSMADESIMPLE("\uf0b0"),
    /** A constant used to reference the 'codeigniter' icon. */
    CODEIGNITER("\uf077"),
    /** A constant used to reference the 'coffee' icon. */
    COFFEE("\uf235"),
    /** A constant used to reference the 'coffeebean' icon. */
    COFFEEBEAN("\uf366"),
    /** A constant used to reference the 'cog' icon. */
    COG("\uf00f"),
    /** A constant used to reference the 'colocation' icon. */
    COLOCATION("\uf024"),
    /** A constant used to reference the 'colocationalt' icon. */
    COLOCATIONALT("\uf023"),
    /** A constant used to reference the 'colors' icon. */
    COLORS("\uf1e6"),
    /** A constant used to reference the 'comment' icon. */
    COMMENT("\uf12c"),
    /** A constant used to reference the 'commentout' icon. */
    COMMENTOUT("\uf080"),
    /** A constant used to reference the 'commentround' icon. */
    COMMENTROUND("\uf155"),
    /** A constant used to reference the 'commentroundempty' icon. */
    COMMENTROUNDEMPTY("\uf156"),
    /** A constant used to reference the 'commentroundtyping' icon. */
    COMMENTROUNDTYPING("\uf157"),
    /** A constant used to reference the 'commentroundtypingempty' icon. */
    COMMENTROUNDTYPINGEMPTY("\uf158"),
    /** A constant used to reference the 'commenttyping' icon. */
    COMMENTTYPING("\uf12d"),
    /** A constant used to reference the 'compass' icon. */
    COMPASS("\263c"),
    /** A constant used to reference the 'concretefive' icon. */
    CONCRETEFIVE("\uf0af"),
    /** A constant used to reference the 'contact_businesscard' icon. */
    CONTACT_BUSINESSCARD("\uf040"),
    /** A constant used to reference the 'controllernes' icon. */
    CONTROLLERNES("\uf2d2"),
    /** A constant used to reference the 'controllerps' icon. */
    CONTROLLERPS("\uf2d1"),
    /** A constant used to reference the 'controllersnes' icon. */
    CONTROLLERSNES("\uf2d3"),
    /** A constant used to reference the 'controlpanel' icon. */
    CONTROLPANEL("\uf008"),
    /** A constant used to reference the 'controlpanelalt' icon. */
    CONTROLPANELALT("\uf009"),
    /** A constant used to reference the 'cooling' icon. */
    COOLING("\uf00a"),
    /** A constant used to reference the 'coppermine' icon. */
    COPPERMINE("\uf0a4"),
    /** A constant used to reference the 'copy' icon. */
    COPY("\uf0c9"),
    /** A constant used to reference the 'copyright' icon. */
    COPYRIGHT("\00a9"),
    /** A constant used to reference the 'coupon' icon. */
    COUPON("\uf254"),
    /** A constant used to reference the 'cpanel' icon. */
    CPANEL("\uf072"),
    /** A constant used to reference the 'cplusplus' icon. */
    CPLUSPLUS("\uf0b1"),
    /** A constant used to reference the 'cpu_processor' icon. */
    CPU_PROCESSOR("\uf002"),
    /** A constant used to reference the 'cpualt_processoralt' icon. */
    CPUALT_PROCESSORALT("\uf003"),
    /** A constant used to reference the 'crayon' icon. */
    CRAYON("\uf383"),
    /** A constant used to reference the 'createfile' icon. */
    CREATEFILE("\uf0c6"),
    /** A constant used to reference the 'createfolder' icon. */
    CREATEFOLDER("\uf0da"),
    /** A constant used to reference the 'creativecommons' icon. */
    CREATIVECOMMONS("\uf1fc"),
    /** A constant used to reference the 'creditcard' icon. */
    CREDITCARD("\uf279"),
    /** A constant used to reference the 'cricket' icon. */
    CRICKET("\uf418"),
    /** A constant used to reference the 'croisant' icon. */
    CROISANT("\uf29f"),
    /** A constant used to reference the 'crop' icon. */
    CROP("\uf1af"),
    /** A constant used to reference the 'crown' icon. */
    CROWN("\uf28f"),
    /** A constant used to reference the 'csharp' icon. */
    CSHARP("\uf0b2"),
    /** A constant used to reference the 'cssthree' icon. */
    CSSTHREE("\uf06a"),
    /** A constant used to reference the 'cup_coffeealt' icon. */
    CUP_COFFEEALT("\uf24b"),
    /** A constant used to reference the 'cupcake' icon. */
    CUPCAKE("\uf35b"),
    /** A constant used to reference the 'curling' icon. */
    CURLING("\uf3d7"),
    /** A constant used to reference the 'cursor' icon. */
    CURSOR("\uf0dc"),
    /** A constant used to reference the 'cut_scissors' icon. */
    CUT_SCISSORS("\uf0ca"),
    /** A constant used to reference the 'dagger' icon. */
    DAGGER("\2020"),
    /** A constant used to reference the 'danger' icon. */
    DANGER("\uf415"),
    /** A constant used to reference the 'dart' icon. */
    DART("\uf3d4"),
    /** A constant used to reference the 'darthvader' icon. */
    DARTHVADER("\uf34a"),
    /** A constant used to reference the 'database' icon. */
    DATABASE("\uf00b"),
    /** A constant used to reference the 'databaseadd' icon. */
    DATABASEADD("\uf00c"),
    /** A constant used to reference the 'databasedelete' icon. */
    DATABASEDELETE("\uf00d"),
    /** A constant used to reference the 'davidstar' icon. */
    DAVIDSTAR("\uf40e"),
    /** A constant used to reference the 'dcsource' icon. */
    DCSOURCE("\uf3e9"),
    /** A constant used to reference the 'dedicatedserver' icon. */
    DEDICATEDSERVER("\uf00e"),
    /** A constant used to reference the 'deletefile' icon. */
    DELETEFILE("\uf0c7"),
    /** A constant used to reference the 'deletefolder' icon. */
    DELETEFOLDER("\uf0db"),
    /** A constant used to reference the 'delicious' icon. */
    DELICIOUS("\uf152"),
    /** A constant used to reference the 'designcontest' icon. */
    DESIGNCONTEST("\uf351"),
    /** A constant used to reference the 'desklamp' icon. */
    DESKLAMP("\uf412"),
    /** A constant used to reference the 'dialpad' icon. */
    DIALPAD("\uf399"),
    /** A constant used to reference the 'diamond' icon. */
    DIAMOND("\2666"),
    /** A constant used to reference the 'diamonds' icon. */
    DIAMONDS("\uf2f7"),
    /** A constant used to reference the 'die_dice' icon. */
    DIE_DICE("\uf2d8"),
    /** A constant used to reference the 'diefive' icon. */
    DIEFIVE("\uf3fb"),
    /** A constant used to reference the 'diefour' icon. */
    DIEFOUR("\uf3fa"),
    /** A constant used to reference the 'dieone' icon. */
    DIEONE("\uf3f7"),
    /** A constant used to reference the 'diesix' icon. */
    DIESIX("\uf3fc"),
    /** A constant used to reference the 'diethree' icon. */
    DIETHREE("\uf3f9"),
    /** A constant used to reference the 'dietwo' icon. */
    DIETWO("\uf3f8"),
    /** A constant used to reference the 'diode' icon. */
    DIODE("\uf3e7"),
    /** A constant used to reference the 'director' icon. */
    DIRECTOR("\uf2ae"),
    /** A constant used to reference the 'diskspace' icon. */
    DISKSPACE("\uf096"),
    /** A constant used to reference the 'distributehorizontalcenters' icon. */
    DISTRIBUTEHORIZONTALCENTERS("\uf1dc"),
    /** A constant used to reference the 'distributeverticalcenters' icon. */
    DISTRIBUTEVERTICALCENTERS("\uf1db"),
    /** A constant used to reference the 'divide' icon. */
    DIVIDE("\00f7"),
    /** A constant used to reference the 'dna' icon. */
    DNA("\uf409"),
    /** A constant used to reference the 'dnszone' icon. */
    DNSZONE("\uf07f"),
    /** A constant used to reference the 'document' icon. */
    DOCUMENT("\uf0c2"),
    /** A constant used to reference the 'doghouse' icon. */
    DOGHOUSE("\uf38f"),
    /** A constant used to reference the 'dollar' icon. */
    DOLLAR("\24"),
    /** A constant used to reference the 'dollaralt' icon. */
    DOLLARALT("\uf259"),
    /** A constant used to reference the 'dolphinsoftware' icon. */
    DOLPHINSOFTWARE("\uf064"),
    /** A constant used to reference the 'domain' icon. */
    DOMAIN("\uf01d"),
    /** A constant used to reference the 'domainaddon' icon. */
    DOMAINADDON("\uf053"),
    /** A constant used to reference the 'domino' icon. */
    DOMINO("\uf3d5"),
    /** A constant used to reference the 'donut' icon. */
    DONUT("\uf3ca"),
    /** A constant used to reference the 'downleft' icon. */
    DOWNLEFT("\uf2ff"),
    /** A constant used to reference the 'download' icon. */
    DOWNLOAD("\uf47b"),
    /** A constant used to reference the 'download_alt' icon. */
    DOWNLOAD_ALT("\uf11a"),
    /** A constant used to reference the 'downright' icon. */
    DOWNRIGHT("\uf300"),
    /** A constant used to reference the 'draft' icon. */
    DRAFT("\uf172"),
    /** A constant used to reference the 'dreamweaver' icon. */
    DREAMWEAVER("\uf1d0"),
    /** A constant used to reference the 'dribbble' icon. */
    DRIBBBLE("\uf14c"),
    /** A constant used to reference the 'dropmenu' icon. */
    DROPMENU("\uf0a5"),
    /** A constant used to reference the 'drupal' icon. */
    DRUPAL("\uf075"),
    /** A constant used to reference the 'drwho' icon. */
    DRWHO("\uf3c0"),
    /** A constant used to reference the 'edit' icon. */
    EDIT("\uf47c"),
    /** A constant used to reference the 'editalt' icon. */
    EDITALT("\uf0f2"),
    /** A constant used to reference the 'egg' icon. */
    EGG("\uf407"),
    /** A constant used to reference the 'eightball' icon. */
    EIGHTBALL("\uf36e"),
    /** A constant used to reference the 'eject' icon. */
    EJECT("\uf199"),
    /** A constant used to reference the 'elipse' icon. */
    ELIPSE("\uf1bc"),
    /** A constant used to reference the 'emailalt' icon. */
    EMAILALT("\uf136"),
    /** A constant used to reference the 'emailexport' icon. */
    EMAILEXPORT("\uf176"),
    /** A constant used to reference the 'emailforward' icon. */
    EMAILFORWARD("\uf175"),
    /** A constant used to reference the 'emailforwarders' icon. */
    EMAILFORWARDERS("\uf049"),
    /** A constant used to reference the 'emailimport' icon. */
    EMAILIMPORT("\uf177"),
    /** A constant used to reference the 'emailrefresh' icon. */
    EMAILREFRESH("\uf174"),
    /** A constant used to reference the 'emailtrace' icon. */
    EMAILTRACE("\uf091"),
    /** A constant used to reference the 'emergency' icon. */
    EMERGENCY("\uf246"),
    /** A constant used to reference the 'emptycart' icon. */
    EMPTYCART("\uf395"),
    /** A constant used to reference the 'enter' icon. */
    ENTER("\uf323"),
    /** A constant used to reference the 'envelope' icon. */
    ENVELOPE("\uf028"),
    /** A constant used to reference the 'equalizer' icon. */
    EQUALIZER("\uf18e"),
    /** A constant used to reference the 'equalizeralt' icon. */
    EQUALIZERALT("\uf18f"),
    /** A constant used to reference the 'equals' icon. */
    EQUALS("\uf30c"),
    /** A constant used to reference the 'eraser' icon. */
    ERASER("\uf1f1"),
    /** A constant used to reference the 'erroralt' icon. */
    ERRORALT("\uf05a"),
    /** A constant used to reference the 'euro' icon. */
    EURO("\20ac"),
    /** A constant used to reference the 'euroalt' icon. */
    EUROALT("\uf25a"),
    /** A constant used to reference the 'evernote' icon. */
    EVERNOTE("\uf17c"),
    /** A constant used to reference the 'exchange_currency' icon. */
    EXCHANGE_CURRENCY("\uf26b"),
    /** A constant used to reference the 'exclamation_sign' icon. */
    EXCLAMATION_SIGN("\uf04a"),
    /** A constant used to reference the 'excludeshape' icon. */
    EXCLUDESHAPE("\uf200"),
    /** A constant used to reference the 'exit' icon. */
    EXIT("\uf324"),
    /** A constant used to reference the 'explorerwindow' icon. */
    EXPLORERWINDOW("\uf0d9"),
    /** A constant used to reference the 'exportfile' icon. */
    EXPORTFILE("\uf32f"),
    /** A constant used to reference the 'exposure' icon. */
    EXPOSURE("\uf1de"),
    /** A constant used to reference the 'extinguisher' icon. */
    EXTINGUISHER("\uf2b7"),
    /** A constant used to reference the 'eye_close' icon. */
    EYE_CLOSE("\uf481"),
    /** A constant used to reference the 'eye_open' icon. */
    EYE_OPEN("\uf2b5"),
    /** A constant used to reference the 'eye_view' icon. */
    EYE_VIEW("\uf280"),
    /** A constant used to reference the 'eyedropper' icon. */
    EYEDROPPER("\uf1ad"),
    /** A constant used to reference the 'facebook' icon. */
    FACEBOOK("\uf140"),
    /** A constant used to reference the 'facebookalt' icon. */
    FACEBOOKALT("\uf14b"),
    /** A constant used to reference the 'facetime_video' icon. */
    FACETIME_VIDEO("\uf19c"),
    /** A constant used to reference the 'factory' icon. */
    FACTORY("\uf27a"),
    /** A constant used to reference the 'fantastico' icon. */
    FANTASTICO("\uf0ae"),
    /** A constant used to reference the 'faq' icon. */
    FAQ("\uf099"),
    /** A constant used to reference the 'fast_backward' icon. */
    FAST_BACKWARD("\uf47e"),
    /** A constant used to reference the 'fast_forward' icon. */
    FAST_FORWARD("\uf47f"),
    /** A constant used to reference the 'fastdown' icon. */
    FASTDOWN("\uf31d"),
    /** A constant used to reference the 'fastleft' icon. */
    FASTLEFT("\uf31a"),
    /** A constant used to reference the 'fastright' icon. */
    FASTRIGHT("\uf31b"),
    /** A constant used to reference the 'fastup' icon. */
    FASTUP("\uf31c"),
    /** A constant used to reference the 'favoritefile' icon. */
    FAVORITEFILE("\uf381"),
    /** A constant used to reference the 'favoritefolder' icon. */
    FAVORITEFOLDER("\uf382"),
    /** A constant used to reference the 'featheralt_write' icon. */
    FEATHERALT_WRITE("\uf1c5"),
    /** A constant used to reference the 'fedora' icon. */
    FEDORA("\uf3f1"),
    /** A constant used to reference the 'fence' icon. */
    FENCE("\uf2af"),
    /** A constant used to reference the 'file' icon. */
    FILE("\uf0d6"),
    /** A constant used to reference the 'film' icon. */
    FILM("\uf19d"),
    /** A constant used to reference the 'filmstrip' icon. */
    FILMSTRIP("\uf3ed"),
    /** A constant used to reference the 'filter' icon. */
    FILTER("\uf05c"),
    /** A constant used to reference the 'finder' icon. */
    FINDER("\uf398"),
    /** A constant used to reference the 'fire' icon. */
    FIRE("\uf27f"),
    /** A constant used to reference the 'firefox' icon. */
    FIREFOX("\uf420"),
    /** A constant used to reference the 'firewall' icon. */
    FIREWALL("\uf021"),
    /** A constant used to reference the 'firewire' icon. */
    FIREWIRE("\uf0fc"),
    /** A constant used to reference the 'firstaid' icon. */
    FIRSTAID("\uf2ba"),
    /** A constant used to reference the 'fish' icon. */
    FISH("\uf35a"),
    /** A constant used to reference the 'fishbone' icon. */
    FISHBONE("\uf42b"),
    /** A constant used to reference the 'flag' icon. */
    FLAG("\uf487"),
    /** A constant used to reference the 'flagalt' icon. */
    FLAGALT("\uf232"),
    /** A constant used to reference the 'flagtriangle' icon. */
    FLAGTRIANGLE("\uf20b"),
    /** A constant used to reference the 'flash' icon. */
    FLASH("\uf1cf"),
    /** A constant used to reference the 'flashlight' icon. */
    FLASHLIGHT("\uf299"),
    /** A constant used to reference the 'flashplayer' icon. */
    FLASHPLAYER("\uf070"),
    /** A constant used to reference the 'flaskfull' icon. */
    FLASKFULL("\uf27e"),
    /** A constant used to reference the 'flickr' icon. */
    FLICKR("\uf146"),
    /** A constant used to reference the 'flower' icon. */
    FLOWER("\uf2a5"),
    /** A constant used to reference the 'flowernew' icon. */
    FLOWERNEW("\uf3a8"),
    /** A constant used to reference the 'folder_close' icon. */
    FOLDER_CLOSE("\uf094"),
    /** A constant used to reference the 'folder_open' icon. */
    FOLDER_OPEN("\uf483"),
    /** A constant used to reference the 'foldertree' icon. */
    FOLDERTREE("\uf0f0"),
    /** A constant used to reference the 'font' icon. */
    FONT("\uf1ae"),
    /** A constant used to reference the 'foodtray' icon. */
    FOODTRAY("\uf3d0"),
    /** A constant used to reference the 'football_soccer' icon. */
    FOOTBALL_SOCCER("\uf2eb"),
    /** A constant used to reference the 'forbiddenalt' icon. */
    FORBIDDENALT("\uf314"),
    /** A constant used to reference the 'forest_tree' icon. */
    FOREST_TREE("\uf217"),
    /** A constant used to reference the 'forestalt_treealt' icon. */
    FORESTALT_TREEALT("\uf21c"),
    /** A constant used to reference the 'fork' icon. */
    FORK("\22d4"),
    /** A constant used to reference the 'forklift' icon. */
    FORKLIFT("\uf29b"),
    /** A constant used to reference the 'form' icon. */
    FORM("\uf08c"),
    /** A constant used to reference the 'forrst' icon. */
    FORRST("\uf14d"),
    /** A constant used to reference the 'fort' icon. */
    FORT("\uf400"),
    /** A constant used to reference the 'forward' icon. */
    FORWARD("\uf182"),
    /** A constant used to reference the 'fourohfour' icon. */
    FOUROHFOUR("\uf09d"),
    /** A constant used to reference the 'foursquare' icon. */
    FOURSQUARE("\uf42a"),
    /** A constant used to reference the 'freeway' icon. */
    FREEWAY("\uf24a"),
    /** A constant used to reference the 'fridge' icon. */
    FRIDGE("\uf40d"),
    /** A constant used to reference the 'fries' icon. */
    FRIES("\uf36a"),
    /** A constant used to reference the 'ftp' icon. */
    FTP("\uf029"),
    /** A constant used to reference the 'ftpaccounts' icon. */
    FTPACCOUNTS("\uf07b"),
    /** A constant used to reference the 'ftpsession' icon. */
    FTPSESSION("\uf07c"),
    /** A constant used to reference the 'fullscreen' icon. */
    FULLSCREEN("\uf485"),
    /** A constant used to reference the 'gameboy' icon. */
    GAMEBOY("\uf403"),
    /** A constant used to reference the 'gamecursor' icon. */
    GAMECURSOR("\uf2d0"),
    /** A constant used to reference the 'gasstation' icon. */
    GASSTATION("\uf216"),
    /** A constant used to reference the 'gearfour' icon. */
    GEARFOUR("\uf3a7"),
    /** A constant used to reference the 'ghost' icon. */
    GHOST("\uf2da"),
    /** A constant used to reference the 'gift' icon. */
    GIFT("\uf260"),
    /** A constant used to reference the 'github' icon. */
    GITHUB("\uf081"),
    /** A constant used to reference the 'glass' icon. */
    GLASS("\uf236"),
    /** A constant used to reference the 'glasses' icon. */
    GLASSES("\uf295"),
    /** A constant used to reference the 'glassesalt' icon. */
    GLASSESALT("\uf39d"),
    /** A constant used to reference the 'globe' icon. */
    GLOBE("\uf01b"),
    /** A constant used to reference the 'globealt' icon. */
    GLOBEALT("\uf36c"),
    /** A constant used to reference the 'glue' icon. */
    GLUE("\uf36d"),
    /** A constant used to reference the 'gmail' icon. */
    GMAIL("\uf150"),
    /** A constant used to reference the 'golf' icon. */
    GOLF("\uf2f1"),
    /** A constant used to reference the 'googledrive' icon. */
    GOOGLEDRIVE("\uf163"),
    /** A constant used to reference the 'googleplus' icon. */
    GOOGLEPLUS("\uf165"),
    /** A constant used to reference the 'googlewallet' icon. */
    GOOGLEWALLET("\uf270"),
    /** A constant used to reference the 'gpsoff_gps' icon. */
    GPSOFF_GPS("\uf21e"),
    /** A constant used to reference the 'gpson' icon. */
    GPSON("\uf21f"),
    /** A constant used to reference the 'gpu_graphicscard' icon. */
    GPU_GRAPHICSCARD("\uf108"),
    /** A constant used to reference the 'gradient' icon. */
    GRADIENT("\2207"),
    /** A constant used to reference the 'grails' icon. */
    GRAILS("\uf085"),
    /** A constant used to reference the 'greenlantern' icon. */
    GREENLANTERN("\uf340"),
    /** A constant used to reference the 'greenlightbulb' icon. */
    GREENLIGHTBULB("\uf406"),
    /** A constant used to reference the 'grooveshark' icon. */
    GROOVESHARK("\uf3a2"),
    /** A constant used to reference the 'groups_friends' icon. */
    GROUPS_FRIENDS("\uf134"),
    /** A constant used to reference the 'guitar' icon. */
    GUITAR("\uf19a"),
    /** A constant used to reference the 'halflife' icon. */
    HALFLIFE("\uf3ba"),
    /** A constant used to reference the 'halo' icon. */
    HALO("\uf3bb"),
    /** A constant used to reference the 'hamburger' icon. */
    HAMBURGER("\uf2b3"),
    /** A constant used to reference the 'hammer' icon. */
    HAMMER("\uf291"),
    /** A constant used to reference the 'hand_down' icon. */
    HAND_DOWN("\uf387"),
    /** A constant used to reference the 'hand_left' icon. */
    HAND_LEFT("\uf389"),
    /** A constant used to reference the 'hand_right' icon. */
    HAND_RIGHT("\uf388"),
    /** A constant used to reference the 'hand_up' icon. */
    HAND_UP("\uf0dd"),
    /** A constant used to reference the 'handcuffs' icon. */
    HANDCUFFS("\uf393"),
    /** A constant used to reference the 'handdrag' icon. */
    HANDDRAG("\uf0de"),
    /** A constant used to reference the 'handtwofingers' icon. */
    HANDTWOFINGERS("\uf0df"),
    /** A constant used to reference the 'hanger' icon. */
    HANGER("\uf2ab"),
    /** A constant used to reference the 'happy' icon. */
    HAPPY("\uf13c"),
    /** A constant used to reference the 'harrypotter' icon. */
    HARRYPOTTER("\uf38b"),
    /** A constant used to reference the 'hdd' icon. */
    HDD("\uf02a"),
    /** A constant used to reference the 'hdtv' icon. */
    HDTV("\uf1a0"),
    /** A constant used to reference the 'headphones' icon. */
    HEADPHONES("\uf180"),
    /** A constant used to reference the 'headphonesalt' icon. */
    HEADPHONESALT("\uf1a3"),
    /** A constant used to reference the 'heart' icon. */
    HEART("\uf131"),
    /** A constant used to reference the 'heartempty_love' icon. */
    HEARTEMPTY_LOVE("\uf132"),
    /** A constant used to reference the 'hearts' icon. */
    HEARTS("\uf2f4"),
    /** A constant used to reference the 'helicopter' icon. */
    HELICOPTER("\uf3e4"),
    /** A constant used to reference the 'hexagon_polygon' icon. */
    HEXAGON_POLYGON("\uf1be"),
    /** A constant used to reference the 'hockey' icon. */
    HOCKEY("\uf3d9"),
    /** A constant used to reference the 'home' icon. */
    HOME("\21b8"),
    /** A constant used to reference the 'homealt' icon. */
    HOMEALT("\uf02b"),
    /** A constant used to reference the 'hospital' icon. */
    HOSPITAL("\uf247"),
    /** A constant used to reference the 'hotdog' icon. */
    HOTDOG("\uf3cc"),
    /** A constant used to reference the 'hotlinkprotection' icon. */
    HOTLINKPROTECTION("\uf050"),
    /** A constant used to reference the 'hourglassalt' icon. */
    HOURGLASSALT("\uf122"),
    /** A constant used to reference the 'html' icon. */
    HTML("\uf068"),
    /** A constant used to reference the 'htmlfive' icon. */
    HTMLFIVE("\uf069"),
    /** A constant used to reference the 'hydrant' icon. */
    HYDRANT("\uf3ff"),
    /** A constant used to reference the 'icecream' icon. */
    ICECREAM("\uf2a4"),
    /** A constant used to reference the 'icecreamalt' icon. */
    ICECREAMALT("\uf289"),
    /** A constant used to reference the 'illustrator' icon. */
    ILLUSTRATOR("\uf1ce"),
    /** A constant used to reference the 'imac' icon. */
    IMAC("\uf0fb"),
    /** A constant used to reference the 'images_gallery' icon. */
    IMAGES_GALLERY("\uf09f"),
    /** A constant used to reference the 'importcontacts' icon. */
    IMPORTCONTACTS("\uf092"),
    /** A constant used to reference the 'importfile' icon. */
    IMPORTFILE("\uf32e"),
    /** A constant used to reference the 'inbox' icon. */
    INBOX("\uf17a"),
    /** A constant used to reference the 'inboxalt' icon. */
    INBOXALT("\uf178"),
    /** A constant used to reference the 'incomingcall' icon. */
    INCOMINGCALL("\uf15d"),
    /** A constant used to reference the 'indent_left' icon. */
    INDENT_LEFT("\uf1f2"),
    /** A constant used to reference the 'indent_right' icon. */
    INDENT_RIGHT("\uf1f3"),
    /** A constant used to reference the 'indexmanager' icon. */
    INDEXMANAGER("\uf09e"),
    /** A constant used to reference the 'infinity' icon. */
    INFINITY("\221e"),
    /** A constant used to reference the 'info_sign' icon. */
    INFO_SIGN("\uf315"),
    /** A constant used to reference the 'infographic' icon. */
    INFOGRAPHIC("\uf336"),
    /** A constant used to reference the 'ink' icon. */
    INK("\uf3f6"),
    /** A constant used to reference the 'inkpen' icon. */
    INKPEN("\uf1ac"),
    /** A constant used to reference the 'insertbarchart' icon. */
    INSERTBARCHART("\uf1e5"),
    /** A constant used to reference the 'insertpicture' icon. */
    INSERTPICTURE("\uf1e0"),
    /** A constant used to reference the 'insertpicturecenter' icon. */
    INSERTPICTURECENTER("\uf1e3"),
    /** A constant used to reference the 'insertpictureleft' icon. */
    INSERTPICTURELEFT("\uf1e1"),
    /** A constant used to reference the 'insertpictureright' icon. */
    INSERTPICTURERIGHT("\uf1e2"),
    /** A constant used to reference the 'insertpiechart' icon. */
    INSERTPIECHART("\uf1e4"),
    /** A constant used to reference the 'instagram' icon. */
    INSTAGRAM("\uf14a"),
    /** A constant used to reference the 'install' icon. */
    INSTALL("\uf128"),
    /** A constant used to reference the 'intel' icon. */
    INTEL("\uf01f"),
    /** A constant used to reference the 'intersection' icon. */
    INTERSECTION("\2229"),
    /** A constant used to reference the 'intersectshape' icon. */
    INTERSECTSHAPE("\uf1ff"),
    /** A constant used to reference the 'invert' icon. */
    INVERT("\uf1df"),
    /** A constant used to reference the 'invoice' icon. */
    INVOICE("\uf3e5"),
    /** A constant used to reference the 'ipcontrol' icon. */
    IPCONTROL("\uf08b"),
    /** A constant used to reference the 'iphone' icon. */
    IPHONE("\uf0e6"),
    /** A constant used to reference the 'ipod' icon. */
    IPOD("\uf190"),
    /** A constant used to reference the 'ironman' icon. */
    IRONMAN("\uf349"),
    /** A constant used to reference the 'islam' icon. */
    ISLAM("\uf410"),
    /** A constant used to reference the 'island' icon. */
    ISLAND("\uf392"),
    /** A constant used to reference the 'italic' icon. */
    ITALIC("\uf1f5"),
    /** A constant used to reference the 'jar' icon. */
    JAR("\uf2b6"),
    /** A constant used to reference the 'jason' icon. */
    JASON("\uf38c"),
    /** A constant used to reference the 'java' icon. */
    JAVA("\uf083"),
    /** A constant used to reference the 'joomla' icon. */
    JOOMLA("\uf073"),
    /** A constant used to reference the 'joystickarcade' icon. */
    JOYSTICKARCADE("\uf2d4"),
    /** A constant used to reference the 'joystickatari' icon. */
    JOYSTICKATARI("\uf2d5"),
    /** A constant used to reference the 'jquery' icon. */
    JQUERY("\uf06b"),
    /** A constant used to reference the 'jqueryui' icon. */
    JQUERYUI("\uf06c"),
    /** A constant used to reference the 'kerning' icon. */
    KERNING("\uf1e9"),
    /** A constant used to reference the 'key' icon. */
    KEY("\uf093"),
    /** A constant used to reference the 'keyboard' icon. */
    KEYBOARD("\uf119"),
    /** A constant used to reference the 'keyboardalt' icon. */
    KEYBOARDALT("\uf105"),
    /** A constant used to reference the 'keyboarddelete' icon. */
    KEYBOARDDELETE("\uf3a6"),
    /** A constant used to reference the 'kidney' icon. */
    KIDNEY("\uf3e0"),
    /** A constant used to reference the 'king' icon. */
    KING("\uf2fc"),
    /** A constant used to reference the 'knife' icon. */
    KNIFE("\uf214"),
    /** A constant used to reference the 'knight' icon. */
    KNIGHT("\uf2fb"),
    /** A constant used to reference the 'knob' icon. */
    KNOB("\uf376"),
    /** A constant used to reference the 'lab_flask' icon. */
    LAB_FLASK("\uf27d"),
    /** A constant used to reference the 'lamp' icon. */
    LAMP("\uf2b1"),
    /** A constant used to reference the 'lan' icon. */
    LAN("\uf0ee"),
    /** A constant used to reference the 'language' icon. */
    LANGUAGE("\uf042"),
    /** A constant used to reference the 'laptop' icon. */
    LAPTOP("\uf0d8"),
    /** A constant used to reference the 'lasso' icon. */
    LASSO("\uf396"),
    /** A constant used to reference the 'lastfm' icon. */
    LASTFM("\uf3a3"),
    /** A constant used to reference the 'laugh' icon. */
    LAUGH("\uf13f"),
    /** A constant used to reference the 'law' icon. */
    LAW("\uf263"),
    /** A constant used to reference the 'layers' icon. */
    LAYERS("\uf1ca"),
    /** A constant used to reference the 'layersalt' icon. */
    LAYERSALT("\uf1cb"),
    /** A constant used to reference the 'leaf' icon. */
    LEAF("\uf039"),
    /** A constant used to reference the 'leechprotect' icon. */
    LEECHPROTECT("\uf07e"),
    /** A constant used to reference the 'legacyfilemanager' icon. */
    LEGACYFILEMANAGER("\uf095"),
    /** A constant used to reference the 'lego' icon. */
    LEGO("\uf370"),
    /** A constant used to reference the 'lifeempty' icon. */
    LIFEEMPTY("\uf2e1"),
    /** A constant used to reference the 'lifefull' icon. */
    LIFEFULL("\uf2e3"),
    /** A constant used to reference the 'lifehacker' icon. */
    LIFEHACKER("\uf380"),
    /** A constant used to reference the 'lifehalf' icon. */
    LIFEHALF("\uf2e2"),
    /** A constant used to reference the 'lifepreserver' icon. */
    LIFEPRESERVER("\uf015"),
    /** A constant used to reference the 'lightbulb_idea' icon. */
    LIGHTBULB_IDEA("\uf338"),
    /** A constant used to reference the 'lighthouse' icon. */
    LIGHTHOUSE("\uf3e6"),
    /** A constant used to reference the 'lightning' icon. */
    LIGHTNING("\uf231"),
    /** A constant used to reference the 'lightningalt' icon. */
    LIGHTNINGALT("\uf2a8"),
    /** A constant used to reference the 'line' icon. */
    LINE("\uf1bf"),
    /** A constant used to reference the 'lineheight' icon. */
    LINEHEIGHT("\uf1c0"),
    /** A constant used to reference the 'link' icon. */
    LINK("\uf022"),
    /** A constant used to reference the 'linkalt' icon. */
    LINKALT("\uf333"),
    /** A constant used to reference the 'linkedin' icon. */
    LINKEDIN("\uf166"),
    /** A constant used to reference the 'linux' icon. */
    LINUX("\uf01a"),
    /** A constant used to reference the 'list' icon. */
    LIST("\uf111"),
    /** A constant used to reference the 'list_alt' icon. */
    LIST_ALT("\uf480"),
    /** A constant used to reference the 'liver' icon. */
    LIVER("\uf3e2"),
    /** A constant used to reference the 'loading_hourglass' icon. */
    LOADING_HOURGLASS("\uf123"),
    /** A constant used to reference the 'loadingalt' icon. */
    LOADINGALT("\uf339"),
    /** A constant used to reference the 'lock' icon. */
    LOCK("\uf0be"),
    /** A constant used to reference the 'lockalt_keyhole' icon. */
    LOCKALT_KEYHOLE("\uf0eb"),
    /** A constant used to reference the 'lollypop' icon. */
    LOLLYPOP("\uf3ee"),
    /** A constant used to reference the 'lungs' icon. */
    LUNGS("\uf3df"),
    /** A constant used to reference the 'macpro' icon. */
    MACPRO("\uf3a5"),
    /** A constant used to reference the 'macro_plant' icon. */
    MACRO_PLANT("\uf1c6"),
    /** A constant used to reference the 'magazine' icon. */
    MAGAZINE("\uf1ec"),
    /** A constant used to reference the 'magento' icon. */
    MAGENTO("\uf06e"),
    /** A constant used to reference the 'magnet' icon. */
    MAGNET("\uf281"),
    /** A constant used to reference the 'mailbox' icon. */
    MAILBOX("\uf044"),
    /** A constant used to reference the 'mailinglists' icon. */
    MAILINGLISTS("\uf090"),
    /** A constant used to reference the 'man_male' icon. */
    MAN_MALE("\uf2a1"),
    /** A constant used to reference the 'managedhosting' icon. */
    MANAGEDHOSTING("\uf038"),
    /** A constant used to reference the 'map' icon. */
    MAP("\uf209"),
    /** A constant used to reference the 'map_marker' icon. */
    MAP_MARKER("\uf220"),
    /** A constant used to reference the 'marker' icon. */
    MARKER("\uf204"),
    /** A constant used to reference the 'marvin' icon. */
    MARVIN("\uf3dd"),
    /** A constant used to reference the 'mastercard' icon. */
    MASTERCARD("\uf266"),
    /** A constant used to reference the 'maximize' icon. */
    MAXIMIZE("\uf30f"),
    /** A constant used to reference the 'medal' icon. */
    MEDAL("\uf2e5"),
    /** A constant used to reference the 'medalbronze' icon. */
    MEDALBRONZE("\uf2e8"),
    /** A constant used to reference the 'medalgold' icon. */
    MEDALGOLD("\uf2e6"),
    /** A constant used to reference the 'medalsilver' icon. */
    MEDALSILVER("\uf2e7"),
    /** A constant used to reference the 'mediarepeat' icon. */
    MEDIAREPEAT("\uf187"),
    /** A constant used to reference the 'men' icon. */
    MEN("\uf24c"),
    /** A constant used to reference the 'menu' icon. */
    MENU("\uf127"),
    /** A constant used to reference the 'merge' icon. */
    MERGE("\uf334"),
    /** A constant used to reference the 'mergecells' icon. */
    MERGECELLS("\uf327"),
    /** A constant used to reference the 'mergeshapes' icon. */
    MERGESHAPES("\uf201"),
    /** A constant used to reference the 'metro_subway' icon. */
    METRO_SUBWAY("\uf24f"),
    /** A constant used to reference the 'metronome' icon. */
    METRONOME("\uf374"),
    /** A constant used to reference the 'mickeymouse' icon. */
    MICKEYMOUSE("\uf37a"),
    /** A constant used to reference the 'microphone' icon. */
    MICROPHONE("\uf191"),
    /** A constant used to reference the 'microscope' icon. */
    MICROSCOPE("\uf283"),
    /** A constant used to reference the 'microsd' icon. */
    MICROSD("\uf107"),
    /** A constant used to reference the 'microwave' icon. */
    MICROWAVE("\uf42e"),
    /** A constant used to reference the 'mimetype' icon. */
    MIMETYPE("\uf057"),
    /** A constant used to reference the 'minimize' icon. */
    MINIMIZE("\uf30e"),
    /** A constant used to reference the 'minus' icon. */
    MINUS("\2212"),
    /** A constant used to reference the 'minus_sign' icon. */
    MINUS_SIGN("\uf477"),
    /** A constant used to reference the 'missedcall' icon. */
    MISSEDCALL("\uf15c"),
    /** A constant used to reference the 'mobile' icon. */
    MOBILE("\uf0e8"),
    /** A constant used to reference the 'moleskine' icon. */
    MOLESKINE("\uf1f0"),
    /** A constant used to reference the 'money_cash' icon. */
    MONEY_CASH("\uf27b"),
    /** A constant used to reference the 'moneybag' icon. */
    MONEYBAG("\uf271"),
    /** A constant used to reference the 'monitor' icon. */
    MONITOR("\uf0d5"),
    /** A constant used to reference the 'monstersinc' icon. */
    MONSTERSINC("\uf3bd"),
    /** A constant used to reference the 'moon_night' icon. */
    MOON_NIGHT("\uf207"),
    /** A constant used to reference the 'mouse' icon. */
    MOUSE("\uf0d4"),
    /** A constant used to reference the 'mousealt' icon. */
    MOUSEALT("\uf126"),
    /** A constant used to reference the 'move' icon. */
    MOVE("\uf322"),
    /** A constant used to reference the 'movieclapper' icon. */
    MOVIECLAPPER("\uf193"),
    /** A constant used to reference the 'moviereel' icon. */
    MOVIEREEL("\uf17f"),
    /** A constant used to reference the 'muffin' icon. */
    MUFFIN("\uf363"),
    /** A constant used to reference the 'mug' icon. */
    MUG("\uf24e"),
    /** A constant used to reference the 'mushroom' icon. */
    MUSHROOM("\uf35e"),
    /** A constant used to reference the 'music' icon. */
    MUSIC("\uf181"),
    /** A constant used to reference the 'musicalt' icon. */
    MUSICALT("\uf18d"),
    /** A constant used to reference the 'mutealt' icon. */
    MUTEALT("\uf0e5"),
    /** A constant used to reference the 'mxentry' icon. */
    MXENTRY("\uf07a"),
    /** A constant used to reference the 'mybb' icon. */
    MYBB("\uf065"),
    /** A constant used to reference the 'myspace' icon. */
    MYSPACE("\uf153"),
    /** A constant used to reference the 'mysql_dolphin' icon. */
    MYSQL_DOLPHIN("\uf076"),
    /** A constant used to reference the 'nail' icon. */
    NAIL("\uf428"),
    /** A constant used to reference the 'navigation' icon. */
    NAVIGATION("\uf23a"),
    /** A constant used to reference the 'network' icon. */
    NETWORK("\uf0a6"),
    /** A constant used to reference the 'networksignal' icon. */
    NETWORKSIGNAL("\uf3a9"),
    /** A constant used to reference the 'news' icon. */
    NEWS("\uf256"),
    /** A constant used to reference the 'newtab' icon. */
    NEWTAB("\uf16f"),
    /** A constant used to reference the 'newwindow' icon. */
    NEWWINDOW("\uf16d"),
    /** A constant used to reference the 'next' icon. */
    NEXT("\uf18a"),
    /** A constant used to reference the 'nexus' icon. */
    NEXUS("\uf0e7"),
    /** A constant used to reference the 'nintendods' icon. */
    NINTENDODS("\uf404"),
    /** A constant used to reference the 'nodejs' icon. */
    NODEJS("\uf084"),
    /** A constant used to reference the 'notes' icon. */
    NOTES("\uf0d7"),
    /** A constant used to reference the 'notificationbottom' icon. */
    NOTIFICATIONBOTTOM("\uf144"),
    /** A constant used to reference the 'notificationtop' icon. */
    NOTIFICATIONTOP("\uf145"),
    /** A constant used to reference the 'nut' icon. */
    NUT("\uf427"),
    /** A constant used to reference the 'off' icon. */
    OFF("\uf11d"),
    /** A constant used to reference the 'office_building' icon. */
    OFFICE_BUILDING("\uf245"),
    /** A constant used to reference the 'officechair' icon. */
    OFFICECHAIR("\uf26d"),
    /** A constant used to reference the 'ok' icon. */
    OK("\2713"),
    /** A constant used to reference the 'ok_circle' icon. */
    OK_CIRCLE("\uf471"),
    /** A constant used to reference the 'ok_sign' icon. */
    OK_SIGN("\uf479"),
    /** A constant used to reference the 'oneup' icon. */
    ONEUP("\uf3b7"),
    /** A constant used to reference the 'oneupalt' icon. */
    ONEUPALT("\uf3b6"),
    /** A constant used to reference the 'opencart' icon. */
    OPENCART("\uf060"),
    /** A constant used to reference the 'opennewwindow' icon. */
    OPENNEWWINDOW("\uf332"),
    /** A constant used to reference the 'orange' icon. */
    ORANGE("\uf29e"),
    /** A constant used to reference the 'outbox' icon. */
    OUTBOX("\uf179"),
    /** A constant used to reference the 'outgoingcall' icon. */
    OUTGOINGCALL("\uf15e"),
    /** A constant used to reference the 'oxwall' icon. */
    OXWALL("\uf06d"),
    /** A constant used to reference the 'pacman' icon. */
    PACMAN("\uf2db"),
    /** A constant used to reference the 'pageback' icon. */
    PAGEBACK("\uf31e"),
    /** A constant used to reference the 'pagebreak' icon. */
    PAGEBREAK("\uf1cc"),
    /** A constant used to reference the 'pageforward' icon. */
    PAGEFORWARD("\uf31f"),
    /** A constant used to reference the 'pagesetup' icon. */
    PAGESETUP("\uf331"),
    /** A constant used to reference the 'paintbrush' icon. */
    PAINTBRUSH("\uf1e8"),
    /** A constant used to reference the 'paintroll' icon. */
    PAINTROLL("\uf1fa"),
    /** A constant used to reference the 'palette_painting' icon. */
    PALETTE_PAINTING("\uf1b9"),
    /** A constant used to reference the 'paperclip' icon. */
    PAPERCLIP("\uf284"),
    /** A constant used to reference the 'paperclipalt' icon. */
    PAPERCLIPALT("\uf285"),
    /** A constant used to reference the 'paperclipvertical' icon. */
    PAPERCLIPVERTICAL("\uf286"),
    /** A constant used to reference the 'paperplane' icon. */
    PAPERPLANE("\uf296"),
    /** A constant used to reference the 'parentheses' icon. */
    PARENTHESES("\uf3c4"),
    /** A constant used to reference the 'parkeddomain' icon. */
    PARKEDDOMAIN("\uf055"),
    /** A constant used to reference the 'password' icon. */
    PASSWORD("\uf03e"),
    /** A constant used to reference the 'passwordalt' icon. */
    PASSWORDALT("\uf03f"),
    /** A constant used to reference the 'pasta' icon. */
    PASTA("\uf408"),
    /** A constant used to reference the 'patch' icon. */
    PATCH("\uf2a3"),
    /** A constant used to reference the 'path' icon. */
    PATH("\uf169"),
    /** A constant used to reference the 'pause' icon. */
    PAUSE("\uf186"),
    /** A constant used to reference the 'paw_pet' icon. */
    PAW_PET("\uf29d"),
    /** A constant used to reference the 'pawn' icon. */
    PAWN("\uf2f8"),
    /** A constant used to reference the 'paypal' icon. */
    PAYPAL("\uf267"),
    /** A constant used to reference the 'peace' icon. */
    PEACE("\uf2a7"),
    /** A constant used to reference the 'pen' icon. */
    PEN("\uf1ee"),
    /** A constant used to reference the 'pencil' icon. */
    PENCIL("\uf1b7"),
    /** A constant used to reference the 'pepperoni' icon. */
    PEPPERONI("\uf364"),
    /** A constant used to reference the 'percent' icon. */
    PERCENT("\25"),
    /** A constant used to reference the 'perl_camel' icon. */
    PERL_CAMEL("\uf0b6"),
    /** A constant used to reference the 'perlalt' icon. */
    PERLALT("\uf0b7"),
    /** A constant used to reference the 'phone_call' icon. */
    PHONE_CALL("\uf14f"),
    /** A constant used to reference the 'phonealt' icon. */
    PHONEALT("\uf15b"),
    /** A constant used to reference the 'phonebook' icon. */
    PHONEBOOK("\uf149"),
    /** A constant used to reference the 'phonebookalt' icon. */
    PHONEBOOKALT("\uf135"),
    /** A constant used to reference the 'phonemic' icon. */
    PHONEMIC("\uf391"),
    /** A constant used to reference the 'phoneold' icon. */
    PHONEOLD("\uf148"),
    /** A constant used to reference the 'photoshop' icon. */
    PHOTOSHOP("\uf1cd"),
    /** A constant used to reference the 'php' icon. */
    PHP("\uf09c"),
    /** A constant used to reference the 'phpbb' icon. */
    PHPBB("\uf063"),
    /** A constant used to reference the 'phppear' icon. */
    PHPPEAR("\uf09b"),
    /** A constant used to reference the 'piano' icon. */
    PIANO("\uf19e"),
    /** A constant used to reference the 'picture' icon. */
    PICTURE("\22b7"),
    /** A constant used to reference the 'pictureframe' icon. */
    PICTUREFRAME("\uf41e"),
    /** A constant used to reference the 'piggybank' icon. */
    PIGGYBANK("\uf257"),
    /** A constant used to reference the 'pigpena' icon. */
    PIGPENA("\uf456"),
    /** A constant used to reference the 'pigpenb' icon. */
    PIGPENB("\uf457"),
    /** A constant used to reference the 'pigpenc' icon. */
    PIGPENC("\uf458"),
    /** A constant used to reference the 'pigpend' icon. */
    PIGPEND("\uf459"),
    /** A constant used to reference the 'pigpene' icon. */
    PIGPENE("\uf45a"),
    /** A constant used to reference the 'pigpenf' icon. */
    PIGPENF("\uf45b"),
    /** A constant used to reference the 'pigpeng' icon. */
    PIGPENG("\uf45c"),
    /** A constant used to reference the 'pigpenh' icon. */
    PIGPENH("\uf45d"),
    /** A constant used to reference the 'pigpeni' icon. */
    PIGPENI("\uf45e"),
    /** A constant used to reference the 'pigpenj' icon. */
    PIGPENJ("\uf45f"),
    /** A constant used to reference the 'pigpenk' icon. */
    PIGPENK("\uf460"),
    /** A constant used to reference the 'pigpenl' icon. */
    PIGPENL("\uf461"),
    /** A constant used to reference the 'pigpenm' icon. */
    PIGPENM("\uf462"),
    /** A constant used to reference the 'pigpenn' icon. */
    PIGPENN("\uf463"),
    /** A constant used to reference the 'pigpeno' icon. */
    PIGPENO("\uf464"),
    /** A constant used to reference the 'pigpenp' icon. */
    PIGPENP("\uf465"),
    /** A constant used to reference the 'pigpenq' icon. */
    PIGPENQ("\uf466"),
    /** A constant used to reference the 'pigpenr' icon. */
    PIGPENR("\uf467"),
    /** A constant used to reference the 'pigpens' icon. */
    PIGPENS("\uf468"),
    /** A constant used to reference the 'pigpent' icon. */
    PIGPENT("\uf469"),
    /** A constant used to reference the 'pigpenu' icon. */
    PIGPENU("\uf46a"),
    /** A constant used to reference the 'pigpenv' icon. */
    PIGPENV("\uf46b"),
    /** A constant used to reference the 'pigpenw' icon. */
    PIGPENW("\uf46c"),
    /** A constant used to reference the 'pigpenx' icon. */
    PIGPENX("\uf46d"),
    /** A constant used to reference the 'pigpeny' icon. */
    PIGPENY("\uf46e"),
    /** A constant used to reference the 'pigpenz' icon. */
    PIGPENZ("\uf46f"),
    /** A constant used to reference the 'pilcrow' icon. */
    PILCROW("\00b6"),
    /** A constant used to reference the 'pill_antivirusalt' icon. */
    PILL_ANTIVIRUSALT("\uf0aa"),
    /** A constant used to reference the 'pin' icon. */
    PIN("\uf20a"),
    /** A constant used to reference the 'pipe' icon. */
    PIPE("\01c0"),
    /** A constant used to reference the 'piwigo' icon. */
    PIWIGO("\uf0ad"),
    /** A constant used to reference the 'pizza' icon. */
    PIZZA("\uf35c"),
    /** A constant used to reference the 'placeadd' icon. */
    PLACEADD("\uf221"),
    /** A constant used to reference the 'placealt' icon. */
    PLACEALT("\uf224"),
    /** A constant used to reference the 'placealtadd' icon. */
    PLACEALTADD("\uf225"),
    /** A constant used to reference the 'placealtdelete' icon. */
    PLACEALTDELETE("\uf226"),
    /** A constant used to reference the 'placedelete' icon. */
    PLACEDELETE("\uf222"),
    /** A constant used to reference the 'placeios' icon. */
    PLACEIOS("\uf20c"),
    /** A constant used to reference the 'plane' icon. */
    PLANE("\uf23e"),
    /** A constant used to reference the 'plaque' icon. */
    PLAQUE("\uf2b8"),
    /** A constant used to reference the 'play' icon. */
    PLAY("\uf184"),
    /** A constant used to reference the 'play_circle' icon. */
    PLAY_CIRCLE("\uf17e"),
    /** A constant used to reference the 'playstore' icon. */
    PLAYSTORE("\uf255"),
    /** A constant used to reference the 'playvideo' icon. */
    PLAYVIDEO("\uf03d"),
    /** A constant used to reference the 'plug' icon. */
    PLUG("\uf0ea"),
    /** A constant used to reference the 'pluginalt' icon. */
    PLUGINALT("\uf098"),
    /** A constant used to reference the 'plus' icon. */
    PLUS("\002b"),
    /** A constant used to reference the 'plus_sign' icon. */
    PLUS_SIGN("\uf476"),
    /** A constant used to reference the 'pocket' icon. */
    POCKET("\uf16b"),
    /** A constant used to reference the 'podcast' icon. */
    PODCAST("\uf1a2"),
    /** A constant used to reference the 'podium_winner' icon. */
    PODIUM_WINNER("\uf2d6"),
    /** A constant used to reference the 'pokemon' icon. */
    POKEMON("\uf354"),
    /** A constant used to reference the 'police' icon. */
    POLICE("\uf2aa"),
    /** A constant used to reference the 'polygonlasso' icon. */
    POLYGONLASSO("\uf397"),
    /** A constant used to reference the 'post' icon. */
    POST("\uf12e"),
    /** A constant used to reference the 'postalt' icon. */
    POSTALT("\uf130"),
    /** A constant used to reference the 'pound' icon. */
    POUND("\uf25b"),
    /** A constant used to reference the 'poundalt' icon. */
    POUNDALT("\uf25c"),
    /** A constant used to reference the 'powerjack' icon. */
    POWERJACK("\uf0fd"),
    /** A constant used to reference the 'powerplug' icon. */
    POWERPLUG("\uf0ed"),
    /** A constant used to reference the 'powerplugeu' icon. */
    POWERPLUGEU("\uf28b"),
    /** A constant used to reference the 'powerplugus' icon. */
    POWERPLUGUS("\uf28c"),
    /** A constant used to reference the 'presentation' icon. */
    PRESENTATION("\uf0c4"),
    /** A constant used to reference the 'prestashop' icon. */
    PRESTASHOP("\uf061"),
    /** A constant used to reference the 'pretzel' icon. */
    PRETZEL("\uf3cf"),
    /** A constant used to reference the 'preview' icon. */
    PREVIEW("\uf330"),
    /** A constant used to reference the 'previous' icon. */
    PREVIOUS("\uf18b"),
    /** A constant used to reference the 'print' icon. */
    PRINT("\uf125"),
    /** A constant used to reference the 'protecteddirectory' icon. */
    PROTECTEDDIRECTORY("\uf04d"),
    /** A constant used to reference the 'pscircle' icon. */
    PSCIRCLE("\uf2bb"),
    /** A constant used to reference the 'pscursor' icon. */
    PSCURSOR("\uf2c3"),
    /** A constant used to reference the 'psdown' icon. */
    PSDOWN("\uf2c6"),
    /** A constant used to reference the 'psleft' icon. */
    PSLEFT("\uf2c7"),
    /** A constant used to reference the 'pslone' icon. */
    PSLONE("\uf2cc"),
    /** A constant used to reference the 'psltwo' icon. */
    PSLTWO("\uf2cd"),
    /** A constant used to reference the 'psright' icon. */
    PSRIGHT("\uf2c5"),
    /** A constant used to reference the 'psrone' icon. */
    PSRONE("\uf2ce"),
    /** A constant used to reference the 'psrtwo' icon. */
    PSRTWO("\uf2cf"),
    /** A constant used to reference the 'pssquare' icon. */
    PSSQUARE("\uf2bc"),
    /** A constant used to reference the 'pstriangle' icon. */
    PSTRIANGLE("\uf2bd"),
    /** A constant used to reference the 'psup' icon. */
    PSUP("\uf2c4"),
    /** A constant used to reference the 'psx' icon. */
    PSX("\uf2be"),
    /** A constant used to reference the 'pull' icon. */
    PULL("\uf089"),
    /** A constant used to reference the 'punisher' icon. */
    PUNISHER("\uf343"),
    /** A constant used to reference the 'push' icon. */
    PUSH("\uf088"),
    /** A constant used to reference the 'puzzle_plugin' icon. */
    PUZZLE_PLUGIN("\uf0a0"),
    /** A constant used to reference the 'python' icon. */
    PYTHON("\uf071"),
    /** A constant used to reference the 'qrcode' icon. */
    QRCODE("\uf275"),
    /** A constant used to reference the 'quake' icon. */
    QUAKE("\uf355"),
    /** A constant used to reference the 'queen' icon. */
    QUEEN("\uf2fd"),
    /** A constant used to reference the 'query' icon. */
    QUERY("\uf08a"),
    /** A constant used to reference the 'question_sign' icon. */
    QUESTION_SIGN("\uf0a3"),
    /** A constant used to reference the 'quote' icon. */
    QUOTE("\uf12f"),
    /** A constant used to reference the 'quotedown' icon. */
    QUOTEDOWN("\uf329"),
    /** A constant used to reference the 'quoteup' icon. */
    QUOTEUP("\uf328"),
    /** A constant used to reference the 'raceflag' icon. */
    RACEFLAG("\uf38e"),
    /** A constant used to reference the 'racquet' icon. */
    RACQUET("\uf2f2"),
    /** A constant used to reference the 'radio' icon. */
    RADIO("\uf1a1"),
    /** A constant used to reference the 'radioactive' icon. */
    RADIOACTIVE("\uf282"),
    /** A constant used to reference the 'radiobutton' icon. */
    RADIOBUTTON("\uf312"),
    /** A constant used to reference the 'railroad' icon. */
    RAILROAD("\uf248"),
    /** A constant used to reference the 'rain' icon. */
    RAIN("\uf22f"),
    /** A constant used to reference the 'ram' icon. */
    RAM("\uf02c"),
    /** A constant used to reference the 'random' icon. */
    RANDOM("\uf188"),
    /** A constant used to reference the 'rar' icon. */
    RAR("\uf117"),
    /** A constant used to reference the 'raspberry' icon. */
    RASPBERRY("\uf368"),
    /** A constant used to reference the 'raspberrypi' icon. */
    RASPBERRYPI("\uf369"),
    /** A constant used to reference the 'rawaccesslogs' icon. */
    RAWACCESSLOGS("\uf0c1"),
    /** A constant used to reference the 'razor' icon. */
    RAZOR("\uf416"),
    /** A constant used to reference the 'reademail' icon. */
    READEMAIL("\uf173"),
    /** A constant used to reference the 'record' icon. */
    RECORD("\uf189"),
    /** A constant used to reference the 'rectangle' icon. */
    RECTANGLE("\25ad"),
    /** A constant used to reference the 'recycle' icon. */
    RECYCLE("\uf297"),
    /** A constant used to reference the 'reddit' icon. */
    REDDIT("\uf154"),
    /** A constant used to reference the 'redirect' icon. */
    REDIRECT("\uf054"),
    /** A constant used to reference the 'refresh' icon. */
    REFRESH("\uf078"),
    /** A constant used to reference the 'reliability' icon. */
    RELIABILITY("\uf016"),
    /** A constant used to reference the 'remote' icon. */
    REMOTE("\uf298"),
    /** A constant used to reference the 'remove' icon. */
    REMOVE("\00d7"),
    /** A constant used to reference the 'remove_circle' icon. */
    REMOVE_CIRCLE("\uf470"),
    /** A constant used to reference the 'remove_sign' icon. */
    REMOVE_SIGN("\uf478"),
    /** A constant used to reference the 'removefriend' icon. */
    REMOVEFRIEND("\uf3db"),
    /** A constant used to reference the 'repeat' icon. */
    REPEAT("\uf32b"),
    /** A constant used to reference the 'repeatone' icon. */
    REPEATONE("\uf196"),
    /** A constant used to reference the 'resellerhosting' icon. */
    RESELLERHOSTING("\uf03a"),
    /** A constant used to reference the 'residentevil' icon. */
    RESIDENTEVIL("\uf350"),
    /** A constant used to reference the 'resistor' icon. */
    RESISTOR("\uf3eb"),
    /** A constant used to reference the 'resize' icon. */
    RESIZE("\uf1ed"),
    /** A constant used to reference the 'resize_full' icon. */
    RESIZE_FULL("\uf325"),
    /** A constant used to reference the 'resize_horizontal' icon. */
    RESIZE_HORIZONTAL("\uf318"),
    /** A constant used to reference the 'resize_small' icon. */
    RESIZE_SMALL("\uf326"),
    /** A constant used to reference the 'resize_vertical' icon. */
    RESIZE_VERTICAL("\uf319"),
    /** A constant used to reference the 'restart' icon. */
    RESTART("\uf11f"),
    /** A constant used to reference the 'restaurantmenu' icon. */
    RESTAURANTMENU("\uf362"),
    /** A constant used to reference the 'restore' icon. */
    RESTORE("\uf30d"),
    /** A constant used to reference the 'restricted' icon. */
    RESTRICTED("\uf0ab"),
    /** A constant used to reference the 'retweet' icon. */
    RETWEET("\uf486"),
    /** A constant used to reference the 'rim' icon. */
    RIM("\uf36f"),
    /** A constant used to reference the 'ring' icon. */
    RING("\02da"),
    /** A constant used to reference the 'road' icon. */
    ROAD("\uf249"),
    /** A constant used to reference the 'roadsign_roadsignright' icon. */
    ROADSIGN_ROADSIGNRIGHT("\uf21b"),
    /** A constant used to reference the 'roadsignleft' icon. */
    ROADSIGNLEFT("\uf240"),
    /** A constant used to reference the 'robocop' icon. */
    ROBOCOP("\uf357"),
    /** A constant used to reference the 'rocket_launch' icon. */
    ROCKET_LAUNCH("\uf29c"),
    /** A constant used to reference the 'rook' icon. */
    ROOK("\uf2fa"),
    /** A constant used to reference the 'root' icon. */
    ROOT("\uf33c"),
    /** A constant used to reference the 'rorschach' icon. */
    RORSCHACH("\uf358"),
    /** A constant used to reference the 'rotateclockwise' icon. */
    ROTATECLOCKWISE("\uf202"),
    /** A constant used to reference the 'rotatecounterclockwise' icon. */
    ROTATECOUNTERCLOCKWISE("\uf203"),
    /** A constant used to reference the 'roundrectangle' icon. */
    ROUNDRECTANGLE("\uf1bd"),
    /** A constant used to reference the 'route' icon. */
    ROUTE("\uf402"),
    /** A constant used to reference the 'router' icon. */
    ROUTER("\uf0e9"),
    /** A constant used to reference the 'rss' icon. */
    RSS("\uf17b"),
    /** A constant used to reference the 'rubberstamp' icon. */
    RUBBERSTAMP("\uf274"),
    /** A constant used to reference the 'ruby' icon. */
    RUBY("\uf067"),
    /** A constant used to reference the 'ruler' icon. */
    RULER("\uf1ef"),
    /** A constant used to reference the 'sad' icon. */
    SAD("\uf13d"),
    /** A constant used to reference the 'safetypin' icon. */
    SAFETYPIN("\uf417"),
    /** A constant used to reference the 'satellite' icon. */
    SATELLITE("\uf38a"),
    /** A constant used to reference the 'satellitedish_remotemysql' icon. */
    SATELLITEDISH_REMOTEMYSQL("\uf0c0"),
    /** A constant used to reference the 'save_floppy' icon. */
    SAVE_FLOPPY("\uf0c8"),
    /** A constant used to reference the 'scales' icon. */
    SCALES("\uf3fd"),
    /** A constant used to reference the 'science_atom' icon. */
    SCIENCE_ATOM("\uf2b0"),
    /** A constant used to reference the 'scope_scan' icon. */
    SCOPE_SCAN("\uf212"),
    /** A constant used to reference the 'scopealt' icon. */
    SCOPEALT("\uf237"),
    /** A constant used to reference the 'screenshot' icon. */
    SCREENSHOT("\uf109"),
    /** A constant used to reference the 'screw' icon. */
    SCREW("\uf426"),
    /** A constant used to reference the 'screwdriver' icon. */
    SCREWDRIVER("\uf292"),
    /** A constant used to reference the 'screwdriveralt' icon. */
    SCREWDRIVERALT("\uf293"),
    /** A constant used to reference the 'script' icon. */
    SCRIPT("\uf08d"),
    /** A constant used to reference the 'sd' icon. */
    SD("\uf106"),
    /** A constant used to reference the 'search' icon. */
    SEARCH("\uf0c5"),
    /** A constant used to reference the 'searchdocument' icon. */
    SEARCHDOCUMENT("\uf419"),
    /** A constant used to reference the 'searchfolder' icon. */
    SEARCHFOLDER("\uf41a"),
    /** A constant used to reference the 'security_shield' icon. */
    SECURITY_SHIELD("\uf02d"),
    /** A constant used to reference the 'securityalt_shieldalt' icon. */
    SECURITYALT_SHIELDALT("\uf02e"),
    /** A constant used to reference the 'selection_rectangleselection' icon. */
    SELECTION_RECTANGLESELECTION("\uf1b0"),
    /** A constant used to reference the 'selectionadd' icon. */
    SELECTIONADD("\uf1b2"),
    /** A constant used to reference the 'selectionintersect' icon. */
    SELECTIONINTERSECT("\uf1b4"),
    /** A constant used to reference the 'selectionremove' icon. */
    SELECTIONREMOVE("\uf1b3"),
    /** A constant used to reference the 'seo' icon. */
    SEO("\uf030"),
    /** A constant used to reference the 'server' icon. */
    SERVER("\uf026"),
    /** A constant used to reference the 'servers' icon. */
    SERVERS("\uf027"),
    /** A constant used to reference the 'settingsandroid' icon. */
    SETTINGSANDROID("\uf309"),
    /** A constant used to reference the 'settingsfour_gearsalt' icon. */
    SETTINGSFOUR_GEARSALT("\uf306"),
    /** A constant used to reference the 'settingsthree_gears' icon. */
    SETTINGSTHREE_GEARS("\uf307"),
    /** A constant used to reference the 'settingstwo_gearalt' icon. */
    SETTINGSTWO_GEARALT("\uf308"),
    /** A constant used to reference the 'shades_sunglasses' icon. */
    SHADES_SUNGLASSES("\uf294"),
    /** A constant used to reference the 'shapes' icon. */
    SHAPES("\uf1dd"),
    /** A constant used to reference the 'share' icon. */
    SHARE("\uf47d"),
    /** A constant used to reference the 'share_alt' icon. */
    SHARE_ALT("\uf16c"),
    /** A constant used to reference the 'sharealt' icon. */
    SHAREALT("\uf147"),
    /** A constant used to reference the 'sharedfile' icon. */
    SHAREDFILE("\uf0ef"),
    /** A constant used to reference the 'sharedhosting' icon. */
    SHAREDHOSTING("\uf037"),
    /** A constant used to reference the 'sharethree' icon. */
    SHARETHREE("\uf414"),
    /** A constant used to reference the 'sheriff' icon. */
    SHERIFF("\uf2a9"),
    /** A constant used to reference the 'shipping' icon. */
    SHIPPING("\uf23f"),
    /** A constant used to reference the 'shopping' icon. */
    SHOPPING("\uf010"),
    /** A constant used to reference the 'shopping_cart' icon. */
    SHOPPING_CART("\uf035"),
    /** A constant used to reference the 'shoppingbag' icon. */
    SHOPPINGBAG("\uf273"),
    /** A constant used to reference the 'shortcut' icon. */
    SHORTCUT("\uf043"),
    /** A constant used to reference the 'shovel' icon. */
    SHOVEL("\uf290"),
    /** A constant used to reference the 'shredder' icon. */
    SHREDDER("\uf27c"),
    /** A constant used to reference the 'shutdown' icon. */
    SHUTDOWN("\uf11e"),
    /** A constant used to reference the 'sidebar' icon. */
    SIDEBAR("\uf124"),
    /** A constant used to reference the 'signal' icon. */
    SIGNAL("\uf100"),
    /** A constant used to reference the 'sim' icon. */
    SIM("\uf0e1"),
    /** A constant used to reference the 'simalt' icon. */
    SIMALT("\uf121"),
    /** A constant used to reference the 'skrill' icon. */
    SKRILL("\uf268"),
    /** A constant used to reference the 'skull' icon. */
    SKULL("\uf38d"),
    /** A constant used to reference the 'skype' icon. */
    SKYPE("\uf141"),
    /** A constant used to reference the 'skypeaway' icon. */
    SKYPEAWAY("\uf39f"),
    /** A constant used to reference the 'skypebusy' icon. */
    SKYPEBUSY("\uf3a0"),
    /** A constant used to reference the 'skypeoffline' icon. */
    SKYPEOFFLINE("\uf3a1"),
    /** A constant used to reference the 'skypeonline' icon. */
    SKYPEONLINE("\uf39e"),
    /** A constant used to reference the 'smaller' icon. */
    SMALLER("\uf30b"),
    /** A constant used to reference the 'smf' icon. */
    SMF("\uf062"),
    /** A constant used to reference the 'smile' icon. */
    SMILE("\263a"),
    /** A constant used to reference the 'snow' icon. */
    SNOW("\uf22e"),
    /** A constant used to reference the 'snowman' icon. */
    SNOWMAN("\uf37c"),
    /** A constant used to reference the 'socialnetwork' icon. */
    SOCIALNETWORK("\uf03b"),
    /** A constant used to reference the 'software' icon. */
    SOFTWARE("\uf09a"),
    /** A constant used to reference the 'sortbynameascending_atoz' icon. */
    SORTBYNAMEASCENDING_ATOZ("\uf1c2"),
    /** A constant used to reference the 'sortbynamedescending_ztoa' icon. */
    SORTBYNAMEDESCENDING_ZTOA("\uf1c1"),
    /** A constant used to reference the 'sortbysizeascending' icon. */
    SORTBYSIZEASCENDING("\uf1c3"),
    /** A constant used to reference the 'sortbysizedescending' icon. */
    SORTBYSIZEDESCENDING("\uf1c4"),
    /** A constant used to reference the 'soundwave' icon. */
    SOUNDWAVE("\uf194"),
    /** A constant used to reference the 'soup' icon. */
    SOUP("\uf3d1"),
    /** A constant used to reference the 'spaceinvaders' icon. */
    SPACEINVADERS("\uf352"),
    /** A constant used to reference the 'spades' icon. */
    SPADES("\uf2f5"),
    /** A constant used to reference the 'spam' icon. */
    SPAM("\uf047"),
    /** A constant used to reference the 'spamalt' icon. */
    SPAMALT("\uf048"),
    /** A constant used to reference the 'spawn' icon. */
    SPAWN("\uf344"),
    /** A constant used to reference the 'speaker' icon. */
    SPEAKER("\uf372"),
    /** A constant used to reference the 'speed' icon. */
    SPEED("\uf40b"),
    /** A constant used to reference the 'spider' icon. */
    SPIDER("\uf346"),
    /** A constant used to reference the 'spiderman' icon. */
    SPIDERMAN("\uf347"),
    /** A constant used to reference the 'split' icon. */
    SPLIT("\uf335"),
    /** A constant used to reference the 'spoon' icon. */
    SPOON("\uf213"),
    /** A constant used to reference the 'spray' icon. */
    SPRAY("\uf1c7"),
    /** A constant used to reference the 'spreadsheet' icon. */
    SPREADSHEET("\uf0c3"),
    /** A constant used to reference the 'squareapp' icon. */
    SQUAREAPP("\uf26f"),
    /** A constant used to reference the 'squarebrackets' icon. */
    SQUAREBRACKETS("\uf0b3"),
    /** A constant used to reference the 'ssh' icon. */
    SSH("\uf04e"),
    /** A constant used to reference the 'sslmanager' icon. */
    SSLMANAGER("\uf04f"),
    /** A constant used to reference the 'stadium' icon. */
    STADIUM("\uf3d6"),
    /** A constant used to reference the 'stamp' icon. */
    STAMP("\uf242"),
    /** A constant used to reference the 'stampalt' icon. */
    STAMPALT("\uf243"),
    /** A constant used to reference the 'star' icon. */
    STAR("\uf13a"),
    /** A constant used to reference the 'star_empty' icon. */
    STAR_EMPTY("\uf13b"),
    /** A constant used to reference the 'starempty' icon. */
    STAREMPTY("\uf2de"),
    /** A constant used to reference the 'starfull' icon. */
    STARFULL("\uf2e0"),
    /** A constant used to reference the 'starhalf' icon. */
    STARHALF("\uf2df"),
    /** A constant used to reference the 'steak' icon. */
    STEAK("\uf360"),
    /** A constant used to reference the 'steam' icon. */
    STEAM("\uf2dd"),
    /** A constant used to reference the 'step_backward' icon. */
    STEP_BACKWARD("\uf198"),
    /** A constant used to reference the 'step_forward' icon. */
    STEP_FORWARD("\uf197"),
    /** A constant used to reference the 'sticker' icon. */
    STICKER("\uf3f5"),
    /** A constant used to reference the 'stiletto' icon. */
    STILETTO("\uf429"),
    /** A constant used to reference the 'stockdown' icon. */
    STOCKDOWN("\uf252"),
    /** A constant used to reference the 'stocks' icon. */
    STOCKS("\uf250"),
    /** A constant used to reference the 'stockup' icon. */
    STOCKUP("\uf251"),
    /** A constant used to reference the 'stomach' icon. */
    STOMACH("\uf3e1"),
    /** A constant used to reference the 'stop' icon. */
    STOP("\uf185"),
    /** A constant used to reference the 'stopwatch' icon. */
    STOPWATCH("\uf219"),
    /** A constant used to reference the 'storage_box' icon. */
    STORAGE_BOX("\uf011"),
    /** A constant used to reference the 'storagealt_drawer' icon. */
    STORAGEALT_DRAWER("\uf012"),
    /** A constant used to reference the 'store' icon. */
    STORE("\uf272"),
    /** A constant used to reference the 'storm' icon. */
    STORM("\uf230"),
    /** A constant used to reference the 'stove' icon. */
    STOVE("\uf371"),
    /** A constant used to reference the 'strawberry' icon. */
    STRAWBERRY("\uf3f3"),
    /** A constant used to reference the 'strikethrough' icon. */
    STRIKETHROUGH("\uf1f7"),
    /** A constant used to reference the 'student_school' icon. */
    STUDENT_SCHOOL("\uf288"),
    /** A constant used to reference the 'stumbleupon' icon. */
    STUMBLEUPON("\uf40c"),
    /** A constant used to reference the 'subdomain' icon. */
    SUBDOMAIN("\uf052"),
    /** A constant used to reference the 'submarine' icon. */
    SUBMARINE("\uf373"),
    /** A constant used to reference the 'subscript' icon. */
    SUBSCRIPT("\uf1ea"),
    /** A constant used to reference the 'subtractshape' icon. */
    SUBTRACTSHAPE("\uf1fe"),
    /** A constant used to reference the 'sum' icon. */
    SUM("\uf33b"),
    /** A constant used to reference the 'sun_day' icon. */
    SUN_DAY("\uf206"),
    /** A constant used to reference the 'sunnysideup' icon. */
    SUNNYSIDEUP("\uf365"),
    /** A constant used to reference the 'superman' icon. */
    SUPERMAN("\uf33f"),
    /** A constant used to reference the 'superscript' icon. */
    SUPERSCRIPT("\uf1eb"),
    /** A constant used to reference the 'support' icon. */
    SUPPORT("\uf013"),
    /** A constant used to reference the 'supportalt' icon. */
    SUPPORTALT("\uf014"),
    /** A constant used to reference the 'switch' icon. */
    SWITCH("\uf28a"),
    /** A constant used to reference the 'switchoff' icon. */
    SWITCHOFF("\uf32d"),
    /** A constant used to reference the 'switchoffalt' icon. */
    SWITCHOFFALT("\uf28e"),
    /** A constant used to reference the 'switchon' icon. */
    SWITCHON("\uf32c"),
    /** A constant used to reference the 'switchonalt' icon. */
    SWITCHONALT("\uf28d"),
    /** A constant used to reference the 'sword' icon. */
    SWORD("\uf2ed"),
    /** A constant used to reference the 'sync' icon. */
    SYNC("\uf0bd"),
    /** A constant used to reference the 'syncalt' icon. */
    SYNCALT("\uf11c"),
    /** A constant used to reference the 'synckeeplocal' icon. */
    SYNCKEEPLOCAL("\uf33e"),
    /** A constant used to reference the 'synckeepserver' icon. */
    SYNCKEEPSERVER("\uf33d"),
    /** A constant used to reference the 'syringe_antivirus' icon. */
    SYRINGE_ANTIVIRUS("\uf0a9"),
    /** A constant used to reference the 'tablet' icon. */
    TABLET("\uf118"),
    /** A constant used to reference the 'tabletennis_pingpong' icon. */
    TABLETENNIS_PINGPONG("\uf2f0"),
    /** A constant used to reference the 'taco' icon. */
    TACO("\uf3cd"),
    /** A constant used to reference the 'tag' icon. */
    TAG("\uf032"),
    /** A constant used to reference the 'tagalt_pricealt' icon. */
    TAGALT_PRICEALT("\uf264"),
    /** A constant used to reference the 'tags' icon. */
    TAGS("\uf482"),
    /** A constant used to reference the 'tagvertical' icon. */
    TAGVERTICAL("\uf15f"),
    /** A constant used to reference the 'tank' icon. */
    TANK("\uf423"),
    /** A constant used to reference the 'target' icon. */
    TARGET("\uf2a6"),
    /** A constant used to reference the 'taskmanager_logprograms' icon. */
    TASKMANAGER_LOGPROGRAMS("\uf04b"),
    /** A constant used to reference the 'tasks' icon. */
    TASKS("\uf0e0"),
    /** A constant used to reference the 'taxi' icon. */
    TAXI("\uf3a4"),
    /** A constant used to reference the 'tea' icon. */
    TEA("\uf3cb"),
    /** A constant used to reference the 'teapot' icon. */
    TEAPOT("\uf42c"),
    /** A constant used to reference the 'telescope' icon. */
    TELESCOPE("\uf3ef"),
    /** A constant used to reference the 'temperature_thermometer' icon. */
    TEMPERATURE_THERMOMETER("\uf20d"),
    /**
     * A constant used to reference the 'temperaturealt_thermometeralt' icon.
     */
    TEMPERATUREALT_THERMOMETERALT("\uf20e"),
    /** A constant used to reference the 'tennis' icon. */
    TENNIS("\uf2ea"),
    /** A constant used to reference the 'tent_camping' icon. */
    TENT_CAMPING("\uf215"),
    /** A constant used to reference the 'terminal' icon. */
    TERMINAL("\uf114"),
    /** A constant used to reference the 'tethering' icon. */
    TETHERING("\uf0f1"),
    /** A constant used to reference the 'tetrisone' icon. */
    TETRISONE("\uf34b"),
    /** A constant used to reference the 'tetristhree' icon. */
    TETRISTHREE("\uf34d"),
    /** A constant used to reference the 'tetristwo' icon. */
    TETRISTWO("\uf34c"),
    /** A constant used to reference the 'text_height' icon. */
    TEXT_HEIGHT("\uf1f8"),
    /** A constant used to reference the 'text_width' icon. */
    TEXT_WIDTH("\uf1f9"),
    /** A constant used to reference the 'th' icon. */
    TH("\uf110"),
    /** A constant used to reference the 'th_large' icon. */
    TH_LARGE("\uf112"),
    /** A constant used to reference the 'th_list' icon. */
    TH_LIST("\uf113"),
    /** A constant used to reference the 'theather' icon. */
    THEATHER("\uf39c"),
    /** A constant used to reference the 'theme_style' icon. */
    THEME_STYLE("\uf041"),
    /** A constant used to reference the 'thissideup' icon. */
    THISSIDEUP("\uf41d"),
    /** A constant used to reference the 'threecolumns' icon. */
    THREECOLUMNS("\uf1ab"),
    /** A constant used to reference the 'thumbs_down' icon. */
    THUMBS_DOWN("\uf139"),
    /** A constant used to reference the 'thumbs_up' icon. */
    THUMBS_UP("\uf138"),
    /** A constant used to reference the 'ticket' icon. */
    TICKET("\uf3dc"),
    /** A constant used to reference the 'tictactoe' icon. */
    TICTACTOE("\uf39a"),
    /** A constant used to reference the 'tie_business' icon. */
    TIE_BUSINESS("\2040"),
    /** A constant used to reference the 'time' icon. */
    TIME("\uf210"),
    /** A constant used to reference the 'timeline' icon. */
    TIMELINE("\uf253"),
    /** A constant used to reference the 'tint' icon. */
    TINT("\uf208"),
    /** A constant used to reference the 'toast' icon. */
    TOAST("\uf2ad"),
    /** A constant used to reference the 'toiletpaper' icon. */
    TOILETPAPER("\uf384"),
    /** A constant used to reference the 'tooth' icon. */
    TOOTH("\uf3de"),
    /** A constant used to reference the 'toothbrush' icon. */
    TOOTHBRUSH("\uf385"),
    /** A constant used to reference the 'tophat' icon. */
    TOPHAT("\uf3f0"),
    /** A constant used to reference the 'torigate' icon. */
    TORIGATE("\uf411"),
    /** A constant used to reference the 'touchpad' icon. */
    TOUCHPAD("\uf115"),
    /** A constant used to reference the 'trafficlight' icon. */
    TRAFFICLIGHT("\uf22a"),
    /** A constant used to reference the 'transform' icon. */
    TRANSFORM("\uf1a6"),
    /** A constant used to reference the 'trash' icon. */
    TRASH("\uf0ce"),
    /** A constant used to reference the 'trashempty' icon. */
    TRASHEMPTY("\uf0cf"),
    /** A constant used to reference the 'trashfull' icon. */
    TRASHFULL("\uf0d0"),
    /** A constant used to reference the 'travel' icon. */
    TRAVEL("\uf422"),
    /** A constant used to reference the 'treediagram' icon. */
    TREEDIAGRAM("\uf0ec"),
    /** A constant used to reference the 'treeornament' icon. */
    TREEORNAMENT("\uf37e"),
    /** A constant used to reference the 'triangle' icon. */
    TRIANGLE("\25b3"),
    /** A constant used to reference the 'tron' icon. */
    TRON("\uf34f"),
    /** A constant used to reference the 'trophy' icon. */
    TROPHY("\uf2d7"),
    /** A constant used to reference the 'truck' icon. */
    TRUCK("\uf211"),
    /** A constant used to reference the 'trumpet' icon. */
    TRUMPET("\uf375"),
    /** A constant used to reference the 'tumblr' icon. */
    TUMBLR("\uf164"),
    /** A constant used to reference the 'tv' icon. */
    TV("\uf1a4"),
    /** A constant used to reference the 'twitter' icon. */
    TWITTER("\uf16a"),
    /** A constant used to reference the 'twocolumnsleft' icon. */
    TWOCOLUMNSLEFT("\uf1a9"),
    /** A constant used to reference the 'twocolumnsleftalt' icon. */
    TWOCOLUMNSLEFTALT("\uf1aa"),
    /** A constant used to reference the 'twocolumnsright' icon. */
    TWOCOLUMNSRIGHT("\uf1a7"),
    /** A constant used to reference the 'twocolumnsrightalt' icon. */
    TWOCOLUMNSRIGHTALT("\uf1a8"),
    /** A constant used to reference the 'ubuntu' icon. */
    UBUNTU("\uf120"),
    /** A constant used to reference the 'umbrella' icon. */
    UMBRELLA("\uf218"),
    /** A constant used to reference the 'underline' icon. */
    UNDERLINE("\uf1f6"),
    /** A constant used to reference the 'undo' icon. */
    UNDO("\uf32a"),
    /** A constant used to reference the 'unlock' icon. */
    UNLOCK("\uf0bf"),
    /** A constant used to reference the 'upleft' icon. */
    UPLEFT("\uf302"),
    /** A constant used to reference the 'upload' icon. */
    UPLOAD("\uf47a"),
    /** A constant used to reference the 'uploadalt' icon. */
    UPLOADALT("\uf11b"),
    /** A constant used to reference the 'upright' icon. */
    UPRIGHT("\uf303"),
    /** A constant used to reference the 'uptime' icon. */
    UPTIME("\uf017"),
    /** A constant used to reference the 'usb' icon. */
    USB("\uf10d"),
    /** A constant used to reference the 'usbalt' icon. */
    USBALT("\uf10e"),
    /** A constant used to reference the 'usbplug' icon. */
    USBPLUG("\uf10f"),
    /** A constant used to reference the 'user' icon. */
    USER("\uf133"),
    /** A constant used to reference the 'userfilter' icon. */
    USERFILTER("\uf05d"),
    /** A constant used to reference the 'usfootball' icon. */
    USFOOTBALL("\uf2ec"),
    /** A constant used to reference the 'value_coins' icon. */
    VALUE_COINS("\uf018"),
    /** A constant used to reference the 'vector' icon. */
    VECTOR("\uf1b6"),
    /** A constant used to reference the 'vendetta' icon. */
    VENDETTA("\uf3c5"),
    /** A constant used to reference the 'video' icon. */
    VIDEO("\uf17d"),
    /** A constant used to reference the 'viking' icon. */
    VIKING("\uf379"),
    /** A constant used to reference the 'vimeo' icon. */
    VIMEO("\uf168"),
    /** A constant used to reference the 'vinyl' icon. */
    VINYL("\uf0cc"),
    /** A constant used to reference the 'violin' icon. */
    VIOLIN("\uf1a5"),
    /** A constant used to reference the 'virus' icon. */
    VIRUS("\uf0a8"),
    /** A constant used to reference the 'visa' icon. */
    VISA("\uf3c2"),
    /** A constant used to reference the 'visitor' icon. */
    VISITOR("\uf097"),
    /** A constant used to reference the 'vlc_cone' icon. */
    VLC_CONE("\uf192"),
    /** A constant used to reference the 'voice' icon. */
    VOICE("\uf18c"),
    /** A constant used to reference the 'volume_down' icon. */
    VOLUME_DOWN("\uf0e3"),
    /** A constant used to reference the 'volume_off' icon. */
    VOLUME_OFF("\uf0e4"),
    /** A constant used to reference the 'volume_up' icon. */
    VOLUME_UP("\uf0e2"),
    /** A constant used to reference the 'vps' icon. */
    VPS("\uf025"),
    /** A constant used to reference the 'wacom' icon. */
    WACOM("\uf1bb"),
    /** A constant used to reference the 'walle' icon. */
    WALLE("\uf3bc"),
    /** A constant used to reference the 'wallet' icon. */
    WALLET("\ue000"),
    /** A constant used to reference the 'warcraft' icon. */
    WARCRAFT("\uf3bf"),
    /** A constant used to reference the 'warmedal' icon. */
    WARMEDAL("\uf2e4"),
    /** A constant used to reference the 'warning_sign' icon. */
    WARNING_SIGN("\uf316"),
    /** A constant used to reference the 'washer' icon. */
    WASHER("\uf39b"),
    /** A constant used to reference the 'watch' icon. */
    WATCH("\uf378"),
    /** A constant used to reference the 'watertap_plumbing' icon. */
    WATERTAP_PLUMBING("\uf22d"),
    /** A constant used to reference the 'wave_sea' icon. */
    WAVE_SEA("\uf23c"),
    /** A constant used to reference the 'wavealt_seaalt' icon. */
    WAVEALT_SEAALT("\uf23b"),
    /** A constant used to reference the 'webcam' icon. */
    WEBCAM("\uf0fe"),
    /** A constant used to reference the 'webcamalt' icon. */
    WEBCAMALT("\uf129"),
    /** A constant used to reference the 'webhostinghub' icon. */
    WEBHOSTINGHUB("\uf031"),
    /** A constant used to reference the 'webmail' icon. */
    WEBMAIL("\uf045"),
    /** A constant used to reference the 'webpage' icon. */
    WEBPAGE("\uf033"),
    /** A constant used to reference the 'webplatform' icon. */
    WEBPLATFORM("\uf3c3"),
    /** A constant used to reference the 'websitealt' icon. */
    WEBSITEALT("\uf01c"),
    /** A constant used to reference the 'websitebuilder' icon. */
    WEBSITEBUILDER("\uf034"),
    /** A constant used to reference the 'weight' icon. */
    WEIGHT("\uf430"),
    /** A constant used to reference the 'westernunion' icon. */
    WESTERNUNION("\uf26a"),
    /** A constant used to reference the 'wheel' icon. */
    WHEEL("\uf228"),
    /** A constant used to reference the 'wheelchair' icon. */
    WHEELCHAIR("\uf3fe"),
    /** A constant used to reference the 'whistle' icon. */
    WHISTLE("\uf3d8"),
    /** A constant used to reference the 'whmcs' icon. */
    WHMCS("\uf066"),
    /** A constant used to reference the 'wifi' icon. */
    WIFI("\uf0ff"),
    /** A constant used to reference the 'wind' icon. */
    WIND("\uf41b"),
    /** A constant used to reference the 'windleft' icon. */
    WINDLEFT("\uf424"),
    /** A constant used to reference the 'windows' icon. */
    WINDOWS("\uf019"),
    /** A constant used to reference the 'windright' icon. */
    WINDRIGHT("\uf425"),
    /** A constant used to reference the 'wine' icon. */
    WINE("\uf238"),
    /** A constant used to reference the 'wizard' icon. */
    WIZARD("\uf03c"),
    /** A constant used to reference the 'wizardalt' icon. */
    WIZARDALT("\uf1fb"),
    /** A constant used to reference the 'wizardhat' icon. */
    WIZARDHAT("\uf337"),
    /** A constant used to reference the 'woman_female' icon. */
    WOMAN_FEMALE("\uf2a2"),
    /** A constant used to reference the 'women' icon. */
    WOMEN("\uf24d"),
    /** A constant used to reference the 'wordpress' icon. */
    WORDPRESS("\uf074"),
    /** A constant used to reference the 'wrench' icon. */
    WRENCH("\uf05b"),
    /** A constant used to reference the 'wrenchalt' icon. */
    WRENCHALT("\uf2b2"),
    /** A constant used to reference the 'xbox' icon. */
    XBOX("\uf353"),
    /** A constant used to reference the 'xmen' icon. */
    XMEN("\uf345"),
    /** A constant used to reference the 'yahoo' icon. */
    YAHOO("\uf151"),
    /** A constant used to reference the 'yen' icon. */
    YEN("\00a5"),
    /** A constant used to reference the 'yenalt' icon. */
    YENALT("\uf25d"),
    /** A constant used to reference the 'yinyang' icon. */
    YINYANG("\262f"),
    /** A constant used to reference the 'youtube' icon. */
    YOUTUBE("\uf142"),
    /** A constant used to reference the 'zelda' icon. */
    ZELDA("\uf3b8"),
    /** A constant used to reference the 'zikula' icon. */
    ZIKULA("\uf0ac"),
    /** A constant used to reference the 'zip' icon. */
    ZIP("\uf116"),
    /** A constant used to reference the 'zodiac_aquarius' icon. */
    ZODIAC_AQUARIUS("\uf3b4"),
    /** A constant used to reference the 'zodiac_aries' icon. */
    ZODIAC_ARIES("\uf3aa"),
    /** A constant used to reference the 'zodiac_cancer' icon. */
    ZODIAC_CANCER("\uf3ad"),
    /** A constant used to reference the 'zodiac_capricorn' icon. */
    ZODIAC_CAPRICORN("\uf3b3"),
    /** A constant used to reference the 'zodiac_gemini' icon. */
    ZODIAC_GEMINI("\uf3ac"),
    /** A constant used to reference the 'zodiac_leo' icon. */
    ZODIAC_LEO("\uf3ae"),
    /** A constant used to reference the 'zodiac_libra' icon. */
    ZODIAC_LIBRA("\uf3b0"),
    /** A constant used to reference the 'zodiac_pisces' icon. */
    ZODIAC_PISCES("\uf3b5"),
    /** A constant used to reference the 'zodiac_sagitarius' icon. */
    ZODIAC_SAGITARIUS("\uf3b2"),
    /** A constant used to reference the 'zodiac_scorpio' icon. */
    ZODIAC_SCORPIO("\uf3b1"),
    /** A constant used to reference the 'zodiac_taurus' icon. */
    ZODIAC_TAURUS("\uf3ab"),
    /** A constant used to reference the 'zodiac_virgo' icon. */
    ZODIAC_VIRGO("\uf3af"),
    /** A constant used to reference the 'zoom_in' icon. */
    ZOOM_IN("\uf320"),
    /** A constant used to reference the 'zoom_out' icon. */
    ZOOM_OUT("\uf321"),
    /** A constant used to reference the 'vk' icon. */
    VK("\uf34e"),
    /** A constant used to reference the 'bitcoin' icon. */
    BITCOIN("\uf584"),
    /** A constant used to reference the 'rouble' icon. */
    ROUBLE("\uf4ca"),
    /** A constant used to reference the 'phpnuke' icon. */
    PHPNUKE("\uf48c"),
    /** A constant used to reference the 'modx' icon. */
    MODX("\uf48d"),
    /** A constant used to reference the 'eoneohseven' icon. */
    EONEOHSEVEN("\uf48e"),
    /** A constant used to reference the 'subrion' icon. */
    SUBRION("\uf48f"),
    /** A constant used to reference the 'typothree' icon. */
    TYPOTHREE("\uf490"),
    /** A constant used to reference the 'tikiwiki' icon. */
    TIKIWIKI("\uf491"),
    /** A constant used to reference the 'pligg' icon. */
    PLIGG("\uf492"),
    /** A constant used to reference the 'pyrocms' icon. */
    PYROCMS("\uf493"),
    /** A constant used to reference the 'mambo' icon. */
    MAMBO("\uf494"),
    /** A constant used to reference the 'contao' icon. */
    CONTAO("\uf495"),
    /** A constant used to reference the 'crackedegg' icon. */
    CRACKEDEGG("\uf496"),
    /** A constant used to reference the 'coffeecupalt' icon. */
    COFFEECUPALT("\uf497"),
    /** A constant used to reference the 'reademailalt' icon. */
    READEMAILALT("\uf498"),
    /** A constant used to reference the 'train' icon. */
    TRAIN("\uf499"),
    /** A constant used to reference the 'shoebox' icon. */
    SHOEBOX("\uf49a"),
    /** A constant used to reference the 'bathtub' icon. */
    BATHTUB("\uf49b"),
    /** A constant used to reference the 'ninegag' icon. */
    NINEGAG("\uf49c"),
    /** A constant used to reference the 'pebble' icon. */
    PEBBLE("\uf49d"),
    /** A constant used to reference the 'musicthree' icon. */
    MUSICTHREE("\uf49e"),
    /** A constant used to reference the 'stairsup' icon. */
    STAIRSUP("\uf49f"),
    /** A constant used to reference the 'stairsdown' icon. */
    STAIRSDOWN("\uf4a0"),
    /** A constant used to reference the 'bookalt' icon. */
    BOOKALT("\uf4a1"),
    /** A constant used to reference the 'programclose' icon. */
    PROGRAMCLOSE("\uf4a2"),
    /** A constant used to reference the 'programok' icon. */
    PROGRAMOK("\uf4a3"),
    /** A constant used to reference the 'splitalt' icon. */
    SPLITALT("\uf4a4"),
    /** A constant used to reference the 'solarsystem' icon. */
    SOLARSYSTEM("\uf4a5"),
    /** A constant used to reference the 'honeycomb' icon. */
    HONEYCOMB("\uf4a6"),
    /** A constant used to reference the 'tools' icon. */
    TOOLS("\uf4a7"),
    /** A constant used to reference the 'xoops' icon. */
    XOOPS("\uf4a8"),
    /** A constant used to reference the 'pixie' icon. */
    PIXIE("\uf4a9"),
    /** A constant used to reference the 'dotclear' icon. */
    DOTCLEAR("\uf4aa"),
    /** A constant used to reference the 'impresscms' icon. */
    IMPRESSCMS("\uf4ab"),
    /** A constant used to reference the 'saurus' icon. */
    SAURUS("\uf4ac"),
    /** A constant used to reference the 'impresspages' icon. */
    IMPRESSPAGES("\uf4ad"),
    /** A constant used to reference the 'monstra' icon. */
    MONSTRA("\uf4ae"),
    /** A constant used to reference the 'snews' icon. */
    SNEWS("\uf4af"),
    /** A constant used to reference the 'jcore' icon. */
    JCORE("\uf4b0"),
    /** A constant used to reference the 'silverstripe' icon. */
    SILVERSTRIPE("\uf4b1"),
    /** A constant used to reference the 'btwoevolution' icon. */
    BTWOEVOLUTION("\uf4b2"),
    /** A constant used to reference the 'nucleus' icon. */
    NUCLEUS("\uf4b3"),
    /** A constant used to reference the 'symphony' icon. */
    SYMPHONY("\uf4b5"),
    /** A constant used to reference the 'vanillacms' icon. */
    VANILLACMS("\uf4b6"),
    /** A constant used to reference the 'bbpress' icon. */
    BBPRESS("\uf4b7"),
    /** A constant used to reference the 'phpbbalt' icon. */
    PHPBBALT("\uf4b8"),
    /** A constant used to reference the 'chyrp' icon. */
    CHYRP("\uf4b9"),
    /** A constant used to reference the 'pivotx' icon. */
    PIVOTX("\uf4ba"),
    /** A constant used to reference the 'pagecookery' icon. */
    PAGECOOKERY("\uf4bb"),
    /** A constant used to reference the 'moviereelalt' icon. */
    MOVIEREELALT("\uf4bc"),
    /** A constant used to reference the 'cassettealt' icon. */
    CASSETTEALT("\uf4bd"),
    /** A constant used to reference the 'photobucket' icon. */
    PHOTOBUCKET("\uf4be"),
    /** A constant used to reference the 'technorati' icon. */
    TECHNORATI("\uf4bf"),
    /** A constant used to reference the 'theverge' icon. */
    THEVERGE("\uf4c0"),
    /** A constant used to reference the 'stacks' icon. */
    STACKS("\uf4c1"),
    /** A constant used to reference the 'dotlist' icon. */
    DOTLIST("\uf4c2"),
    /** A constant used to reference the 'numberlist' icon. */
    NUMBERLIST("\uf4c3"),
    /** A constant used to reference the 'indentleft' icon. */
    INDENTLEFT("\uf4c4"),
    /** A constant used to reference the 'indentright' icon. */
    INDENTRIGHT("\uf4c5"),
    /** A constant used to reference the 'fblike' icon. */
    FBLIKE("\uf4c6"),
    /** A constant used to reference the 'fbdislike' icon. */
    FBDISLIKE("\uf4c7"),
    /** A constant used to reference the 'sale' icon. */
    SALE("\uf4c8"),
    /** A constant used to reference the 'sharetronix' icon. */
    SHARETRONIX("\uf4c9"),
    /** A constant used to reference the 'markerdown' icon. */
    MARKERDOWN("\uf4cb"),
    /** A constant used to reference the 'markerup' icon. */
    MARKERUP("\uf4cc"),
    /** A constant used to reference the 'markerleft' icon. */
    MARKERLEFT("\uf4cd"),
    /** A constant used to reference the 'markerright' icon. */
    MARKERRIGHT("\uf4ce"),
    /** A constant used to reference the 'bookmarkalt' icon. */
    BOOKMARKALT("\uf4cf"),
    /** A constant used to reference the 'calendarthree' icon. */
    CALENDARTHREE("\uf4d0"),
    /** A constant used to reference the 'wineglass' icon. */
    WINEGLASS("\uf4d1"),
    /** A constant used to reference the 'slidersoff' icon. */
    SLIDERSOFF("\uf4d2"),
    /** A constant used to reference the 'slidersmiddle' icon. */
    SLIDERSMIDDLE("\uf4d3"),
    /** A constant used to reference the 'slidersfull' icon. */
    SLIDERSFULL("\uf4d4"),
    /** A constant used to reference the 'slidersdesc' icon. */
    SLIDERSDESC("\uf4d5"),
    /** A constant used to reference the 'slidersasc' icon. */
    SLIDERSASC("\uf4d6"),
    /** A constant used to reference the 'slideronefull' icon. */
    SLIDERONEFULL("\uf4d7"),
    /** A constant used to reference the 'slidertwofull' icon. */
    SLIDERTWOFULL("\uf4d8"),
    /** A constant used to reference the 'sliderthreefull' icon. */
    SLIDERTHREEFULL("\uf4d9"),
    /** A constant used to reference the 'noborders' icon. */
    NOBORDERS("\uf4da"),
    /** A constant used to reference the 'bottomborder' icon. */
    BOTTOMBORDER("\uf4db"),
    /** A constant used to reference the 'topborder' icon. */
    TOPBORDER("\uf4dc"),
    /** A constant used to reference the 'leftborder' icon. */
    LEFTBORDER("\uf4dd"),
    /** A constant used to reference the 'rightborder' icon. */
    RIGHTBORDER("\uf4de"),
    /** A constant used to reference the 'horizontalborder' icon. */
    HORIZONTALBORDER("\uf4df"),
    /** A constant used to reference the 'verticalborder' icon. */
    VERTICALBORDER("\uf4e0"),
    /** A constant used to reference the 'outerborders' icon. */
    OUTERBORDERS("\uf4e1"),
    /** A constant used to reference the 'innerborders' icon. */
    INNERBORDERS("\uf4e2"),
    /** A constant used to reference the 'fullborders' icon. */
    FULLBORDERS("\uf4e3"),
    /** A constant used to reference the 'networksignalalt' icon. */
    NETWORKSIGNALALT("\uf4e4"),
    /** A constant used to reference the 'resizeverticalalt' icon. */
    RESIZEVERTICALALT("\uf4e5"),
    /** A constant used to reference the 'resizehorizontalalt' icon. */
    RESIZEHORIZONTALALT("\uf4e6"),
    /** A constant used to reference the 'moneyalt' icon. */
    MONEYALT("\uf4e7"),
    /** A constant used to reference the 'fontcase' icon. */
    FONTCASE("\uf4e8"),
    /** A constant used to reference the 'playstation' icon. */
    PLAYSTATION("\uf4e9"),
    /** A constant used to reference the 'cube' icon. */
    CUBE("\uf4ea"),
    /** A constant used to reference the 'sphere' icon. */
    SPHERE("\uf4eb"),
    /** A constant used to reference the 'ceilinglight' icon. */
    CEILINGLIGHT("\uf4ec"),
    /** A constant used to reference the 'chandelier' icon. */
    CHANDELIER("\uf4ed"),
    /** A constant used to reference the 'details' icon. */
    DETAILS("\uf4ee"),
    /** A constant used to reference the 'detailsalt' icon. */
    DETAILSALT("\uf4ef"),
    /** A constant used to reference the 'bullet' icon. */
    BULLET("\uf4f0"),
    /** A constant used to reference the 'gun' icon. */
    GUN("\uf4f1"),
    /** A constant used to reference the 'processorthree' icon. */
    PROCESSORTHREE("\uf4f2"),
    /** A constant used to reference the 'world' icon. */
    WORLD("\uf4f3"),
    /** A constant used to reference the 'statistics' icon. */
    STATISTICS("\uf4f4"),
    /** A constant used to reference the 'shoppingcartalt' icon. */
    SHOPPINGCARTALT("\uf4f5"),
    /** A constant used to reference the 'microphonealt' icon. */
    MICROPHONEALT("\uf4f6"),
    /** A constant used to reference the 'routeralt' icon. */
    ROUTERALT("\uf4f7"),
    /** A constant used to reference the 'shell' icon. */
    SHELL("\uf4f8"),
    /** A constant used to reference the 'squareplay' icon. */
    SQUAREPLAY("\uf4f9"),
    /** A constant used to reference the 'squarestop' icon. */
    SQUARESTOP("\uf4fa"),
    /** A constant used to reference the 'squarepause' icon. */
    SQUAREPAUSE("\uf4fb"),
    /** A constant used to reference the 'squarerecord' icon. */
    SQUARERECORD("\uf4fc"),
    /** A constant used to reference the 'squareforward' icon. */
    SQUAREFORWARD("\uf4fd"),
    /** A constant used to reference the 'squareback' icon. */
    SQUAREBACK("\uf4fe"),
    /** A constant used to reference the 'squarenext' icon. */
    SQUARENEXT("\uf4ff"),
    /** A constant used to reference the 'squareprevious' icon. */
    SQUAREPREVIOUS("\uf500"),
    /** A constant used to reference the 'mega' icon. */
    MEGA("\uf501"),
    /** A constant used to reference the 'charliechaplin' icon. */
    CHARLIECHAPLIN("\uf502"),
    /** A constant used to reference the 'popcorn' icon. */
    POPCORN("\uf503"),
    /** A constant used to reference the 'fatarrowright' icon. */
    FATARROWRIGHT("\uf504"),
    /** A constant used to reference the 'fatarrowleft' icon. */
    FATARROWLEFT("\uf505"),
    /** A constant used to reference the 'fatarrowdown' icon. */
    FATARROWDOWN("\uf506"),
    /** A constant used to reference the 'fatarrowup' icon. */
    FATARROWUP("\uf507"),
    /** A constant used to reference the 'shirtbutton' icon. */
    SHIRTBUTTON("\uf508"),
    /** A constant used to reference the 'shirtbuttonalt' icon. */
    SHIRTBUTTONALT("\uf509"),
    /** A constant used to reference the 'cuckooclock' icon. */
    CUCKOOCLOCK("\uf50a"),
    /** A constant used to reference the 'lens' icon. */
    LENS("\uf50b"),
    /** A constant used to reference the 'voltage' icon. */
    VOLTAGE("\uf50c"),
    /** A constant used to reference the 'planealt' icon. */
    PLANEALT("\uf50d"),
    /** A constant used to reference the 'busalt' icon. */
    BUSALT("\uf50e"),
    /** A constant used to reference the 'lipstick' icon. */
    LIPSTICK("\uf50f"),
    /** A constant used to reference the 'plantalt' icon. */
    PLANTALT("\uf510"),
    /** A constant used to reference the 'paperboat' icon. */
    PAPERBOAT("\uf511"),
    /** A constant used to reference the 'texture' icon. */
    TEXTURE("\uf512"),
    /** A constant used to reference the 'dominoone' icon. */
    DOMINOONE("\uf513"),
    /** A constant used to reference the 'dominotwo' icon. */
    DOMINOTWO("\uf514"),
    /** A constant used to reference the 'dominothree' icon. */
    DOMINOTHREE("\uf515"),
    /** A constant used to reference the 'dominofour' icon. */
    DOMINOFOUR("\uf516"),
    /** A constant used to reference the 'dominofive' icon. */
    DOMINOFIVE("\uf517"),
    /** A constant used to reference the 'dominosix' icon. */
    DOMINOSIX("\uf518"),
    /** A constant used to reference the 'dominoseven' icon. */
    DOMINOSEVEN("\uf519"),
    /** A constant used to reference the 'dominoeight' icon. */
    DOMINOEIGHT("\uf51a"),
    /** A constant used to reference the 'dominonine' icon. */
    DOMINONINE("\uf51b"),
    /** A constant used to reference the 'connected' icon. */
    CONNECTED("\uf51c"),
    /** A constant used to reference the 'connectedpc' icon. */
    CONNECTEDPC("\uf51d"),
    /** A constant used to reference the 'musicsheet' icon. */
    MUSICSHEET("\uf51e"),
    /** A constant used to reference the 'rdio' icon. */
    RDIO("\uf51f"),
    /** A constant used to reference the 'spotify' icon. */
    SPOTIFY("\uf520"),
    /** A constant used to reference the 'deviantart' icon. */
    DEVIANTART("\uf521"),
    /** A constant used to reference the 'yelp' icon. */
    YELP("\uf522"),
    /** A constant used to reference the 'behance' icon. */
    BEHANCE("\uf523"),
    /** A constant used to reference the 'nfc' icon. */
    NFC("\uf524"),
    /** A constant used to reference the 'earbudsalt' icon. */
    EARBUDSALT("\uf525"),
    /** A constant used to reference the 'earbuds' icon. */
    EARBUDS("\uf526"),
    /** A constant used to reference the 'amazon' icon. */
    AMAZON("\uf527"),
    /** A constant used to reference the 'openid' icon. */
    OPENID("\uf528"),
    /** A constant used to reference the 'digg' icon. */
    DIGG("\uf529"),
    /** A constant used to reference the 'moonnew' icon. */
    MOONNEW("\uf52b"),
    /** A constant used to reference the 'moonwaxingcrescent' icon. */
    MOONWAXINGCRESCENT("\uf52c"),
    /** A constant used to reference the 'moonfirstquarter' icon. */
    MOONFIRSTQUARTER("\uf52d"),
    /** A constant used to reference the 'moonwaxinggibbous' icon. */
    MOONWAXINGGIBBOUS("\uf52e"),
    /** A constant used to reference the 'moonfull' icon. */
    MOONFULL("\uf52f"),
    /** A constant used to reference the 'moonwaninggibbous' icon. */
    MOONWANINGGIBBOUS("\uf530"),
    /** A constant used to reference the 'moonthirdquarter' icon. */
    MOONTHIRDQUARTER("\uf531"),
    /** A constant used to reference the 'moonwaningcrescent' icon. */
    MOONWANINGCRESCENT("\uf532"),
    /** A constant used to reference the 'planet' icon. */
    PLANET("\uf533"),
    /** A constant used to reference the 'sodacup' icon. */
    SODACUP("\uf534"),
    /** A constant used to reference the 'cocktail' icon. */
    COCKTAIL("\uf535"),
    /** A constant used to reference the 'church' icon. */
    CHURCH("\uf536"),
    /** A constant used to reference the 'mosque' icon. */
    MOSQUE("\uf537"),
    /** A constant used to reference the 'comedy' icon. */
    COMEDY("\uf538"),
    /** A constant used to reference the 'tragedy' icon. */
    TRAGEDY("\uf539"),
    /** A constant used to reference the 'bacon' icon. */
    BACON("\uf53a"),
    /** A constant used to reference the 'trailor' icon. */
    TRAILOR("\uf53b"),
    /** A constant used to reference the 'tshirt' icon. */
    TSHIRT("\uf53c"),
    /** A constant used to reference the 'design' icon. */
    DESIGN("\uf53d"),
    /** A constant used to reference the 'spiderweb' icon. */
    SPIDERWEB("\uf53e"),
    /** A constant used to reference the 'fireplace' icon. */
    FIREPLACE("\uf53f"),
    /** A constant used to reference the 'tallglass' icon. */
    TALLGLASS("\uf540"),
    /** A constant used to reference the 'grapes' icon. */
    GRAPES("\uf541"),
    /** A constant used to reference the 'biohazard' icon. */
    BIOHAZARD("\uf542"),
    /** A constant used to reference the 'directions' icon. */
    DIRECTIONS("\uf543"),
    /** A constant used to reference the 'equalizerthree' icon. */
    EQUALIZERTHREE("\uf544"),
    /** A constant used to reference the 'mountains' icon. */
    MOUNTAINS("\uf545"),
    /** A constant used to reference the 'bing' icon. */
    BING("\uf546"),
    /** A constant used to reference the 'windowseight' icon. */
    WINDOWSEIGHT("\uf547"),
    /** A constant used to reference the 'microsoftoffice' icon. */
    MICROSOFTOFFICE("\uf548"),
    /** A constant used to reference the 'salealt' icon. */
    SALEALT("\uf549"),
    /** A constant used to reference the 'purse' icon. */
    PURSE("\uf54a"),
    /** A constant used to reference the 'chickenalt' icon. */
    CHICKENALT("\uf54b"),
    /** A constant used to reference the 'podium' icon. */
    PODIUM("\uf54c"),
    /** A constant used to reference the 'findfriends' icon. */
    FINDFRIENDS("\uf54d"),
    /** A constant used to reference the 'microphonethree' icon. */
    MICROPHONETHREE("\uf54e"),
    /** A constant used to reference the 'workshirt' icon. */
    WORKSHIRT("\uf54f"),
    /** A constant used to reference the 'donotdisturb' icon. */
    DONOTDISTURB("\uf550"),
    /** A constant used to reference the 'addtags' icon. */
    ADDTAGS("\uf551"),
    /** A constant used to reference the 'removetags' icon. */
    REMOVETAGS("\uf556"),
    /** A constant used to reference the 'carbattery' icon. */
    CARBATTERY("\uf553"),
    /** A constant used to reference the 'debug' icon. */
    DEBUG("\uf554"),
    /** A constant used to reference the 'trojan' icon. */
    TROJAN("\uf555"),
    /** A constant used to reference the 'molecule' icon. */
    MOLECULE("\uf556"),
    /** A constant used to reference the 'safetygoggles' icon. */
    SAFETYGOGGLES("\uf557"),
    /** A constant used to reference the 'leather' icon. */
    LEATHER("\uf558"),
    /** A constant used to reference the 'teddybear' icon. */
    TEDDYBEAR("\uf559"),
    /** A constant used to reference the 'stroller' icon. */
    STROLLER("\uf55a"),
    /** A constant used to reference the 'circleplay' icon. */
    CIRCLEPLAY("\uf55b"),
    /** A constant used to reference the 'circlestop' icon. */
    CIRCLESTOP("\uf55c"),
    /** A constant used to reference the 'circlepause' icon. */
    CIRCLEPAUSE("\uf55d"),
    /** A constant used to reference the 'circlerecord' icon. */
    CIRCLERECORD("\uf55e"),
    /** A constant used to reference the 'circleforward' icon. */
    CIRCLEFORWARD("\uf55f"),
    /** A constant used to reference the 'circlebackward' icon. */
    CIRCLEBACKWARD("\uf560"),
    /** A constant used to reference the 'circlenext' icon. */
    CIRCLENEXT("\uf561"),
    /** A constant used to reference the 'circleprevious' icon. */
    CIRCLEPREVIOUS("\uf562"),
    /** A constant used to reference the 'circleplayempty' icon. */
    CIRCLEPLAYEMPTY("\uf563"),
    /** A constant used to reference the 'circlestopempty' icon. */
    CIRCLESTOPEMPTY("\uf564"),
    /** A constant used to reference the 'circlepauseempty' icon. */
    CIRCLEPAUSEEMPTY("\uf565"),
    /** A constant used to reference the 'circlerecordempty' icon. */
    CIRCLERECORDEMPTY("\uf566"),
    /** A constant used to reference the 'circleforwardempty' icon. */
    CIRCLEFORWARDEMPTY("\uf567"),
    /** A constant used to reference the 'circlebackwardempty' icon. */
    CIRCLEBACKWARDEMPTY("\uf568"),
    /** A constant used to reference the 'circlenextempty' icon. */
    CIRCLENEXTEMPTY("\uf569"),
    /** A constant used to reference the 'circlepreviousempty' icon. */
    CIRCLEPREVIOUSEMPTY("\uf56a"),
    /** A constant used to reference the 'belt' icon. */
    BELT("\uf56b"),
    /** A constant used to reference the 'bait' icon. */
    BAIT("\uf56c"),
    /** A constant used to reference the 'manalt' icon. */
    MANALT("\uf56d"),
    /** A constant used to reference the 'womanalt' icon. */
    WOMANALT("\uf56e"),
    /** A constant used to reference the 'clover' icon. */
    CLOVER("\uf56f"),
    /** A constant used to reference the 'pacifier' icon. */
    PACIFIER("\uf570"),
    /** A constant used to reference the 'calcplus' icon. */
    CALCPLUS("\uf571"),
    /** A constant used to reference the 'calcminus' icon. */
    CALCMINUS("\uf572"),
    /** A constant used to reference the 'calcmultiply' icon. */
    CALCMULTIPLY("\uf573"),
    /** A constant used to reference the 'calcdivide' icon. */
    CALCDIVIDE("\uf574"),
    /** A constant used to reference the 'calcequals' icon. */
    CALCEQUALS("\uf575"),
    /** A constant used to reference the 'city' icon. */
    CITY("\uf576"),
    /** A constant used to reference the 'hdvideo' icon. */
    HDVIDEO("\uf577"),
    /** A constant used to reference the 'horizontalexpand' icon. */
    HORIZONTALEXPAND("\uf578"),
    /** A constant used to reference the 'horizontalcontract' icon. */
    HORIZONTALCONTRACT("\uf579"),
    /** A constant used to reference the 'radar' icon. */
    RADAR("\uf57a"),
    /** A constant used to reference the 'threed' icon. */
    THREED("\uf57b"),
    /** A constant used to reference the 'flickralt' icon. */
    FLICKRALT("\uf57c"),
    /** A constant used to reference the 'pattern' icon. */
    PATTERN("\uf57d"),
    /** A constant used to reference the 'elevator' icon. */
    ELEVATOR("\uf57e"),
    /** A constant used to reference the 'escalator' icon. */
    ESCALATOR("\uf57f"),
    /** A constant used to reference the 'portrait' icon. */
    PORTRAIT("\uf580"),
    /** A constant used to reference the 'cigar' icon. */
    CIGAR("\uf581"),
    /** A constant used to reference the 'dropbox' icon. */
    DROPBOX("\uf582"),
    /** A constant used to reference the 'origami' icon. */
    ORIGAMI("\uf583"),
    /** A constant used to reference the 'opensource' icon. */
    OPENSOURCE("\uf585"),
    /** A constant used to reference the 'redaxscript' icon. */
    REDAXSCRIPT("\uf586"),
    /** A constant used to reference the 'mahara' icon. */
    MAHARA("\uf587"),
    /** A constant used to reference the 'forkcms' icon. */
    FORKCMS("\uf588"),
    /** A constant used to reference the 'pimcore' icon. */
    PIMCORE("\uf589"),
    /** A constant used to reference the 'bigace' icon. */
    BIGACE("\uf58a"),
    /** A constant used to reference the 'aef' icon. */
    AEF("\uf58b"),
    /** A constant used to reference the 'punbb' icon. */
    PUNBB("\uf58c"),
    /** A constant used to reference the 'phorum' icon. */
    PHORUM("\uf58d"),
    /** A constant used to reference the 'fluxbb' icon. */
    FLUXBB("\uf58e"),
    /** A constant used to reference the 'minibb' icon. */
    MINIBB("\uf58f"),
    /** A constant used to reference the 'zenphoto' icon. */
    ZENPHOTO("\uf590"),
    /** A constant used to reference the 'fourimages' icon. */
    FOURIMAGES("\uf591"),
    /** A constant used to reference the 'plogger' icon. */
    PLOGGER("\uf592"),
    /** A constant used to reference the 'jcow' icon. */
    JCOW("\uf593"),
    /** A constant used to reference the 'elgg' icon. */
    ELGG("\uf594"),
    /** A constant used to reference the 'etano' icon. */
    ETANO("\uf595"),
    /** A constant used to reference the 'openclassifieds' icon. */
    OPENCLASSIFIEDS("\uf596"),
    /** A constant used to reference the 'osclass' icon. */
    OSCLASS("\uf597"),
    /** A constant used to reference the 'openx' icon. */
    OPENX("\uf598"),
    /** A constant used to reference the 'phplist' icon. */
    PHPLIST("\uf599"),
    /** A constant used to reference the 'roundcube' icon. */
    ROUNDCUBE("\uf59a"),
    /** A constant used to reference the 'pommo' icon. */
    POMMO("\uf59b"),
    /** A constant used to reference the 'webinsta' icon. */
    WEBINSTA("\uf59c"),
    /** A constant used to reference the 'limesurvey' icon. */
    LIMESURVEY("\uf59d"),
    /** A constant used to reference the 'fengoffice' icon. */
    FENGOFFICE("\uf59e"),
    /** A constant used to reference the 'eyeos' icon. */
    EYEOS("\uf59f"),
    /** A constant used to reference the 'dotproject' icon. */
    DOTPROJECT("\uf5a0"),
    /** A constant used to reference the 'collabtive' icon. */
    COLLABTIVE("\uf5a1"),
    /** A constant used to reference the 'projectpier' icon. */
    PROJECTPIER("\uf5a2"),
    /** A constant used to reference the 'taskfreak' icon. */
    TASKFREAK("\uf5a3"),
    /** A constant used to reference the 'eventum' icon. */
    EVENTUM("\uf5a4"),
    /** A constant used to reference the 'traq' icon. */
    TRAQ("\uf5a5"),
    /** A constant used to reference the 'mantisbugtracker' icon. */
    MANTISBUGTRACKER("\uf5a6"),
    /** A constant used to reference the 'oscommerce' icon. */
    OSCOMMERCE("\uf5a7"),
    /** A constant used to reference the 'zencart' icon. */
    ZENCART("\uf5a8"),
    /** A constant used to reference the 'tomatocart' icon. */
    TOMATOCART("\uf5a9"),
    /** A constant used to reference the 'boxbilling' icon. */
    BOXBILLING("\uf5aa"),
    /** A constant used to reference the 'zurmo' icon. */
    ZURMO("\uf5ab"),
    /** A constant used to reference the 'orangehrm' icon. */
    ORANGEHRM("\uf5ac"),
    /** A constant used to reference the 'vtiger' icon. */
    VTIGER("\uf5ad"),
    /** A constant used to reference the 'mibew' icon. */
    MIBEW("\uf5ae"),
    /** A constant used to reference the 'phpmyfaq' icon. */
    PHPMYFAQ("\uf5af"),
    /** A constant used to reference the 'yiiframework' icon. */
    YIIFRAMEWORK("\uf5b0"),
    /** A constant used to reference the 'zendframework' icon. */
    ZENDFRAMEWORK("\uf5b1"),
    /** A constant used to reference the 'fuelphp' icon. */
    FUELPHP("\uf5b2"),
    /** A constant used to reference the 'kohana' icon. */
    KOHANA("\uf5b3"),
    /** A constant used to reference the 'smarty' icon. */
    SMARTY("\uf5b4"),
    /** A constant used to reference the 'sidu' icon. */
    SIDU("\uf5b5"),
    /** A constant used to reference the 'simplepie' icon. */
    SIMPLEPIE("\uf5b6"),
    /** A constant used to reference the 'projectsend' icon. */
    PROJECTSEND("\uf5b7"),
    /** A constant used to reference the 'extjs' icon. */
    EXTJS("\uf5b8"),
    /** A constant used to reference the 'raphael' icon. */
    RAPHAEL("\uf5b9"),
    /** A constant used to reference the 'sizzle' icon. */
    SIZZLE("\uf5ba"),
    /** A constant used to reference the 'yui' icon. */
    YUI("\uf5bb"),
    /** A constant used to reference the 'scissorsalt' icon. */
    SCISSORSALT("\uf5bc"),
    /** A constant used to reference the 'cuthere' icon. */
    CUTHERE("\uf5bd"),
    /** A constant used to reference the 'coinsalt' icon. */
    COINSALT("\uf5be"),
    /** A constant used to reference the 'parkingmeter' icon. */
    PARKINGMETER("\uf5bf"),
    /** A constant used to reference the 'treethree' icon. */
    TREETHREE("\uf5c0"),
    /** A constant used to reference the 'packarchive' icon. */
    PACKARCHIVE("\uf5c1"),
    /** A constant used to reference the 'unpackarchive' icon. */
    UNPACKARCHIVE("\uf5c2"),
    /** A constant used to reference the 'terminalalt' icon. */
    TERMINALALT("\uf5c3"),
    /** A constant used to reference the 'jersey' icon. */
    JERSEY("\uf5c4"),
    /** A constant used to reference the 'vial' icon. */
    VIAL("\uf5c5"),
    /** A constant used to reference the 'noteslist' icon. */
    NOTESLIST("\uf5c6"),
    /** A constant used to reference the 'notestasks' icon. */
    NOTESTASKS("\uf5c7"),
    /** A constant used to reference the 'notesdate' icon. */
    NOTESDATE("\uf5c8"),
    /** A constant used to reference the 'noteslocation' icon. */
    NOTESLOCATION("\uf5c9"),
    /** A constant used to reference the 'noteslistalt' icon. */
    NOTESLISTALT("\uf5ca"),
    /** A constant used to reference the 'notestasksalt' icon. */
    NOTESTASKSALT("\uf5cb"),
    /** A constant used to reference the 'notesdatealt' icon. */
    NOTESDATEALT("\uf5cc"),
    /** A constant used to reference the 'noteslocationalt' icon. */
    NOTESLOCATIONALT("\uf5cd"),
    /** A constant used to reference the 'useralt' icon. */
    USERALT("\uf5ce"),
    /** A constant used to reference the 'adduseralt' icon. */
    ADDUSERALT("\uf5cf"),
    /** A constant used to reference the 'removeuseralt' icon. */
    REMOVEUSERALT("\uf5d0"),
    /** A constant used to reference the 'banuseralt' icon. */
    BANUSERALT("\uf5d1"),
    /** A constant used to reference the 'banuser' icon. */
    BANUSER("\uf5d2"),
    /** A constant used to reference the 'paintrollalt' icon. */
    PAINTROLLALT("\uf5d3"),
    /** A constant used to reference the 'textcursor' icon. */
    TEXTCURSOR("\uf5d4"),
    /** A constant used to reference the 'textfield' icon. */
    TEXTFIELD("\uf5d5"),
    /** A constant used to reference the 'precisecursor' icon. */
    PRECISECURSOR("\uf5d6"),
    /** A constant used to reference the 'brokenlink' icon. */
    BROKENLINK("\uf5d7"),
    /** A constant used to reference the 'bookmarkthree' icon. */
    BOOKMARKTHREE("\uf5d8"),
    /** A constant used to reference the 'bookmarkfour' icon. */
    BOOKMARKFOUR("\uf5d9"),
    /** A constant used to reference the 'warmedalalt' icon. */
    WARMEDALALT("\uf5da"),
    /** A constant used to reference the 'thinking' icon. */
    THINKING("\uf5db"),
    /** A constant used to reference the 'commentlove' icon. */
    COMMENTLOVE("\uf5dc"),
    /** A constant used to reference the 'commentsmiley' icon. */
    COMMENTSMILEY("\uf5dd"),
    /** A constant used to reference the 'sharetwo' icon. */
    SHARETWO("\uf147"),
    /** A constant used to reference the 'emptystar' icon. */
    EMPTYSTAR("\uf2de"),
    /** A constant used to reference the 'halfstar' icon. */
    HALFSTAR("\uf2df"),
    /** A constant used to reference the 'fullstar' icon. */
    FULLSTAR("\uf2e0"),
    /** A constant used to reference the 'forbidden' icon. */
    FORBIDDEN("\uf314"),
    /** A constant used to reference the 'indentleftalt' icon. */
    INDENTLEFTALT("\uf4c4"),
    /** A constant used to reference the 'indentrightalt' icon. */
    INDENTRIGHTALT("\uf4c5"),
    /** A constant used to reference the 'modxalt' icon. */
    MODXALT("\uf5de"),
    /** A constant used to reference the 'apple' icon. */
    APPLE("\uf5df"),
    /** A constant used to reference the 'greekcolumn' icon. */
    GREEKCOLUMN("\uf5e0"),
    /** A constant used to reference the 'walletalt' icon. */
    WALLETALT("\uf5e1"),
    /** A constant used to reference the 'dollarsquare' icon. */
    DOLLARSQUARE("\uf5e2"),
    /** A constant used to reference the 'poundsquare' icon. */
    POUNDSQUARE("\uf5e3"),
    /** A constant used to reference the 'yensquare' icon. */
    YENSQUARE("\uf5e4"),
    /** A constant used to reference the 'eurosquare' icon. */
    EUROSQUARE("\uf5e5"),
    /** A constant used to reference the 'bitcoinsquare' icon. */
    BITCOINSQUARE("\uf5e6"),
    /** A constant used to reference the 'roublesquare' icon. */
    ROUBLESQUARE("\uf5e7"),
    /** A constant used to reference the 'roublealt' icon. */
    ROUBLEALT("\uf5e8"),
    /** A constant used to reference the 'bitcoinalt' icon. */
    BITCOINALT("\uf5e9"),
    /** A constant used to reference the 'gavel' icon. */
    GAVEL("\uf5ea"),
    /** A constant used to reference the 'barchartasc' icon. */
    BARCHARTASC("\uf5eb"),
    /** A constant used to reference the 'barchartdesc' icon. */
    BARCHARTDESC("\uf5ec"),
    /** A constant used to reference the 'house' icon. */
    HOUSE("\uf5ed"),
    /** A constant used to reference the 'garage' icon. */
    GARAGE("\uf5ee"),
    /** A constant used to reference the 'milk' icon. */
    MILK("\uf5ef"),
    /** A constant used to reference the 'hryvnia' icon. */
    HRYVNIA("\uf5f0"),
    /** A constant used to reference the 'hryvniasquare' icon. */
    HRYVNIASQUARE("\uf5f1"),
    /** A constant used to reference the 'hryvniaalt' icon. */
    HRYVNIAALT("\uf5f2"),
    /** A constant used to reference the 'beeralt' icon. */
    BEERALT("\uf5f3"),
    /** A constant used to reference the 'trolleyfull' icon. */
    TROLLEYFULL("\uf5f4"),
    /** A constant used to reference the 'trolleyload' icon. */
    TROLLEYLOAD("\uf5f5"),
    /** A constant used to reference the 'trolleyunload' icon. */
    TROLLEYUNLOAD("\uf5f6"),
    /** A constant used to reference the 'trolleyempty' icon. */
    TROLLEYEMPTY("\uf5f7"),
    /** A constant used to reference the 'mootools' icon. */
    MOOTOOLS("\uf5f8"),
    /** A constant used to reference the 'mootoolstwo' icon. */
    MOOTOOLSTWO("\uf5f9"),
    /** A constant used to reference the 'mootoolsthree' icon. */
    MOOTOOLSTHREE("\uf5fa"),
    /** A constant used to reference the 'mysqlthree' icon. */
    MYSQLTHREE("\uf5fb"),
    /** A constant used to reference the 'mysqlalt' icon. */
    MYSQLALT("\uf5fc"),
    /** A constant used to reference the 'pgsql' icon. */
    PGSQL("\uf5fd"),
    /** A constant used to reference the 'mongodb' icon. */
    MONGODB("\uf5fe"),
    /** A constant used to reference the 'neofourj' icon. */
    NEOFOURJ("\uf5ff"),
    /** A constant used to reference the 'nosql' icon. */
    NOSQL("\uf600"),
    /** A constant used to reference the 'catface' icon. */
    CATFACE("\uf601"),
    /** A constant used to reference the 'polaroid' icon. */
    POLAROID("\uf602"),
    /** A constant used to reference the 'clouderror' icon. */
    CLOUDERROR("\uf603"),
    /** A constant used to reference the 'camcorder' icon. */
    CAMCORDER("\uf604"),
    /** A constant used to reference the 'projector' icon. */
    PROJECTOR("\uf605"),
    /** A constant used to reference the 'sdvideo' icon. */
    SDVIDEO("\uf606"),
    /** A constant used to reference the 'fx' icon. */
    FX("\uf607"),
    /** A constant used to reference the 'gramophone' icon. */
    GRAMOPHONE("\uf608"),
    /** A constant used to reference the 'speakeralt' icon. */
    SPEAKERALT("\uf609"),
    /** A constant used to reference the 'hddalt' icon. */
    HDDALT("\uf60a"),
    /** A constant used to reference the 'usbflash' icon. */
    USBFLASH("\uf60b"),
    /** A constant used to reference the 'manillaenvelope' icon. */
    MANILLAENVELOPE("\uf60c"),
    /** A constant used to reference the 'stickynote' icon. */
    STICKYNOTE("\uf60d"),
    /** A constant used to reference the 'stickynotealt' icon. */
    STICKYNOTEALT("\uf60e"),
    /** A constant used to reference the 'torch' icon. */
    TORCH("\uf60f"),
    /** A constant used to reference the 'flashlightalt' icon. */
    FLASHLIGHTALT("\uf610"),
    /** A constant used to reference the 'campfire' icon. */
    CAMPFIRE("\uf611"),
    /** A constant used to reference the 'cctv' icon. */
    CCTV("\uf612"),
    /** A constant used to reference the 'drill' icon. */
    DRILL("\uf613"),
    /** A constant used to reference the 'lampalt' icon. */
    LAMPALT("\uf614"),
    /** A constant used to reference the 'flowerpot' icon. */
    FLOWERPOT("\uf615"),
    /** A constant used to reference the 'defragment' icon. */
    DEFRAGMENT("\uf616"),
    /** A constant used to reference the 'panoramio' icon. */
    PANORAMIO("\uf617"),
    /** A constant used to reference the 'panorama' icon. */
    PANORAMA("\uf618"),
    /** A constant used to reference the 'photosphere' icon. */
    PHOTOSPHERE("\uf619"),
    /** A constant used to reference the 'panoramaalt' icon. */
    PANORAMAALT("\uf61a"),
    /** A constant used to reference the 'timer' icon. */
    TIMER("\uf61b"),
    /** A constant used to reference the 'burstmode' icon. */
    BURSTMODE("\uf61c"),
    /** A constant used to reference the 'cameraflash' icon. */
    CAMERAFLASH("\uf61d"),
    /** A constant used to reference the 'autoflash' icon. */
    AUTOFLASH("\uf61e"),
    /** A constant used to reference the 'noflash' icon. */
    NOFLASH("\uf61f"),
    /** A constant used to reference the 'threetofour' icon. */
    THREETOFOUR("\uf620"),
    /** A constant used to reference the 'sixteentonine' icon. */
    SIXTEENTONINE("\uf621"),
    /** A constant used to reference the 'cat' icon. */
    CAT("\uf622"),
    /** A constant used to reference the 'dog' icon. */
    DOG("\uf623"),
    /** A constant used to reference the 'rabbit' icon. */
    RABBIT("\uf624"),
    /** A constant used to reference the 'koala' icon. */
    KOALA("\uf625"),
    /** A constant used to reference the 'butterflyalt' icon. */
    BUTTERFLYALT("\uf626"),
    /** A constant used to reference the 'butterfly' icon. */
    BUTTERFLY("\uf627"),
    /** A constant used to reference the 'wwf' icon. */
    WWF("\uf628"),
    /** A constant used to reference the 'poop' icon. */
    POOP("\uf629"),
    /** A constant used to reference the 'poopalt' icon. */
    POOPALT("\uf62a"),
    /** A constant used to reference the 'kiwi' icon. */
    KIWI("\uf62b"),
    /** A constant used to reference the 'kiwifruit' icon. */
    KIWIFRUIT("\uf62c"),
    /** A constant used to reference the 'lemon' icon. */
    LEMON("\uf62d"),
    /** A constant used to reference the 'pear' icon. */
    PEAR("\uf62e"),
    /** A constant used to reference the 'watermelon' icon. */
    WATERMELON("\uf62f"),
    /** A constant used to reference the 'onion' icon. */
    ONION("\uf630"),
    /** A constant used to reference the 'turnip' icon. */
    TURNIP("\uf631"),
    /** A constant used to reference the 'eggplant' icon. */
    EGGPLANT("\uf632"),
    /** A constant used to reference the 'avocado' icon. */
    AVOCADO("\uf633"),
    /** A constant used to reference the 'perfume' icon. */
    PERFUME("\uf634"),
    /** A constant used to reference the 'arch' icon. */
    ARCH("\uf635"),
    /** A constant used to reference the 'pluspages' icon. */
    PLUSPAGES("\uf636"),
    /** A constant used to reference the 'community' icon. */
    COMMUNITY("\uf637"),
    /** A constant used to reference the 'pluscircles' icon. */
    PLUSCIRCLES("\uf638"),
    /** A constant used to reference the 'googleplusold' icon. */
    GOOGLEPLUSOLD("\uf639"),
    /** A constant used to reference the 'plusgames' icon. */
    PLUSGAMES("\uf63a"),
    /** A constant used to reference the 'event' icon. */
    EVENT("\uf63b"),
    /** A constant used to reference the 'miui' icon. */
    MIUI("\uf63c"),
    /** A constant used to reference the 'hot' icon. */
    HOT("\uf63d"),
    /** A constant used to reference the 'flowup' icon. */
    FLOWUP("\uf63e"),
    /** A constant used to reference the 'flowdown' icon. */
    FLOWDOWN("\uf63f"),
    /** A constant used to reference the 'moustache' icon. */
    MOUSTACHE("\uf640"),
    /** A constant used to reference the 'angle' icon. */
    ANGLE("\uf641"),
    /** A constant used to reference the 'sleep' icon. */
    SLEEP("\uf642"),
    /** A constant used to reference the 'acorn' icon. */
    ACORN("\uf643"),
    /** A constant used to reference the 'steamalt' icon. */
    STEAMALT("\uf644"),
    /** A constant used to reference the 'resizeupleft' icon. */
    RESIZEUPLEFT("\uf645"),
    /** A constant used to reference the 'resizeupright' icon. */
    RESIZEUPRIGHT("\uf646"),
    /** A constant used to reference the 'resizedownright' icon. */
    RESIZEDOWNRIGHT("\uf647"),
    /** A constant used to reference the 'resizedownleft' icon. */
    RESIZEDOWNLEFT("\uf648"),
    /** A constant used to reference the 'hammeralt' icon. */
    HAMMERALT("\uf649"),
    /** A constant used to reference the 'bamboo' icon. */
    BAMBOO("\uf64a"),
    /** A constant used to reference the 'mypictures' icon. */
    MYPICTURES("\uf64b"),
    /** A constant used to reference the 'mymusic' icon. */
    MYMUSIC("\uf64c"),
    /** A constant used to reference the 'myvideos' icon. */
    MYVIDEOS("\uf64d"),
    /** A constant used to reference the 'systemfolder' icon. */
    SYSTEMFOLDER("\uf64e"),
    /** A constant used to reference the 'bookthree' icon. */
    BOOKTHREE("\uf64f"),
    /** A constant used to reference the 'compile' icon. */
    COMPILE("\uf650"),
    /** A constant used to reference the 'report' icon. */
    REPORT("\uf651"),
    /** A constant used to reference the 'fliphorizontal' icon. */
    FLIPHORIZONTAL("\uf652"),
    /** A constant used to reference the 'flipvertical' icon. */
    FLIPVERTICAL("\uf653"),
    /** A constant used to reference the 'construction' icon. */
    CONSTRUCTION("\uf654"),
    /** A constant used to reference the 'counteralt' icon. */
    COUNTERALT("\uf655"),
    /** A constant used to reference the 'counter' icon. */
    COUNTER("\uf656"),
    /** A constant used to reference the 'papercutter' icon. */
    PAPERCUTTER("\uf657"),
    /** A constant used to reference the 'snaptodot' icon. */
    SNAPTODOT("\uf658"),
    /** A constant used to reference the 'snaptogrid' icon. */
    SNAPTOGRID("\uf659"),
    /** A constant used to reference the 'caligraphy' icon. */
    CALIGRAPHY("\uf65a"),
    /** A constant used to reference the 'icecreamthree' icon. */
    ICECREAMTHREE("\uf65b"),
    /** A constant used to reference the 'skitch' icon. */
    SKITCH("\uf65c"),
    /** A constant used to reference the 'archlinux' icon. */
    ARCHLINUX("\uf65d"),
    /** A constant used to reference the 'elementaryos' icon. */
    ELEMENTARYOS("\uf65e"),
    /** A constant used to reference the 'loadingone' icon. */
    LOADINGONE("\uf65f"),
    /** A constant used to reference the 'loadingtwo' icon. */
    LOADINGTWO("\uf660"),
    /** A constant used to reference the 'loadingthree' icon. */
    LOADINGTHREE("\uf661"),
    /** A constant used to reference the 'loadingfour' icon. */
    LOADINGFOUR("\uf662"),
    /** A constant used to reference the 'loadingfive' icon. */
    LOADINGFIVE("\uf663"),
    /** A constant used to reference the 'loadingsix' icon. */
    LOADINGSIX("\uf664"),
    /** A constant used to reference the 'loadingseven' icon. */
    LOADINGSEVEN("\uf665"),
    /** A constant used to reference the 'loadingeight' icon. */
    LOADINGEIGHT("\uf666"),
    /** A constant used to reference the 'brokenheart' icon. */
    BROKENHEART("\uf667"),
    /** A constant used to reference the 'heartarrow' icon. */
    HEARTARROW("\uf668"),
    /** A constant used to reference the 'heartsparkle' icon. */
    HEARTSPARKLE("\uf669"),
    /** A constant used to reference the 'cell' icon. */
    CELL("\uf66a"),
    /** A constant used to reference the 'panda' icon. */
    PANDA("\uf66b"),
    /** A constant used to reference the 'refreshalt' icon. */
    REFRESHALT("\uf66c"),
    /** A constant used to reference the 'mirror' icon. */
    MIRROR("\uf66d"),
    /** A constant used to reference the 'headphonesthree' icon. */
    HEADPHONESTHREE("\uf66e"),
    /** A constant used to reference the 'fan' icon. */
    FAN("\uf66f"),
    /** A constant used to reference the 'tornado' icon. */
    TORNADO("\uf670"),
    /** A constant used to reference the 'hangout' icon. */
    HANGOUT("\uf671"),
    /** A constant used to reference the 'beaker' icon. */
    BEAKER("\uf672"),
    /** A constant used to reference the 'beakeralt' icon. */
    BEAKERALT("\uf673"),
    /** A constant used to reference the 'phonescreensize' icon. */
    PHONESCREENSIZE("\uf674"),
    /** A constant used to reference the 'tabletscreensize' icon. */
    TABLETSCREENSIZE("\uf675"),
    /** A constant used to reference the 'notification' icon. */
    NOTIFICATION("\uf676"),
    /** A constant used to reference the 'googleglass' icon. */
    GOOGLEGLASS("\uf677"),
    /** A constant used to reference the 'pinterest' icon. */
    PINTEREST("\uf678"),
    /** A constant used to reference the 'soundcloud' icon. */
    SOUNDCLOUD("\uf679"),
    /** A constant used to reference the 'alarmclock' icon. */
    ALARMCLOCK("\uf67a"),
    /** A constant used to reference the 'addalarm' icon. */
    ADDALARM("\uf67b"),
    /** A constant used to reference the 'deletealarm' icon. */
    DELETEALARM("\uf67c"),
    /** A constant used to reference the 'turnoffalarm' icon. */
    TURNOFFALARM("\uf67d"),
    /** A constant used to reference the 'snooze' icon. */
    SNOOZE("\uf67e"),
    /** A constant used to reference the 'bringforward' icon. */
    BRINGFORWARD("\uf67f"),
    /** A constant used to reference the 'sendbackward' icon. */
    SENDBACKWARD("\uf680"),
    /** A constant used to reference the 'bringtofront' icon. */
    BRINGTOFRONT("\uf681"),
    /** A constant used to reference the 'sendtoback' icon. */
    SENDTOBACK("\uf682"),
    /** A constant used to reference the 'tectile' icon. */
    TECTILE("\uf683"),
    /** A constant used to reference the 'grave' icon. */
    GRAVE("\uf684"),
    /** A constant used to reference the 'gravetwo' icon. */
    GRAVETWO("\uf685"),
    /** A constant used to reference the 'gravethree' icon. */
    GRAVETHREE("\uf686"),
    /** A constant used to reference the 'gravefour' icon. */
    GRAVEFOUR("\uf687"),
    /** A constant used to reference the 'textlayer' icon. */
    TEXTLAYER("\uf688"),
    /** A constant used to reference the 'vectoralt' icon. */
    VECTORALT("\uf689"),
    /** A constant used to reference the 'drmanhattan' icon. */
    DRMANHATTAN("\uf68a"),
    /** A constant used to reference the 'foursquarealt' icon. */
    FOURSQUAREALT("\uf68b"),
    /** A constant used to reference the 'hashtag' icon. */
    HASHTAG("\uf68c"),
    /** A constant used to reference the 'enteralt' icon. */
    ENTERALT("\uf68d"),
    /** A constant used to reference the 'exitalt' icon. */
    EXITALT("\uf68e"),
    /** A constant used to reference the 'cartalt' icon. */
    CARTALT("\uf68f"),
    /** A constant used to reference the 'vaultthree' icon. */
    VAULTTHREE("\uf690"),
    /** A constant used to reference the 'fatundo' icon. */
    FATUNDO("\uf691"),
    /** A constant used to reference the 'fatredo' icon. */
    FATREDO("\uf692"),
    /** A constant used to reference the 'feedly' icon. */
    FEEDLY("\uf693"),
    /** A constant used to reference the 'feedlyalt' icon. */
    FEEDLYALT("\uf694"),
    /** A constant used to reference the 'squareheart' icon. */
    SQUAREHEART("\uf695"),
    /** A constant used to reference the 'squarestar' icon. */
    SQUARESTAR("\uf696"),
    /** A constant used to reference the 'squarecomment' icon. */
    SQUARECOMMENT("\uf697"),
    /** A constant used to reference the 'squarelike' icon. */
    SQUARELIKE("\uf698"),
    /** A constant used to reference the 'squarebookmark' icon. */
    SQUAREBOOKMARK("\uf699"),
    /** A constant used to reference the 'squaresearch' icon. */
    SQUARESEARCH("\uf69a"),
    /** A constant used to reference the 'squaresettings' icon. */
    SQUARESETTINGS("\uf69b"),
    /** A constant used to reference the 'squarevoice' icon. */
    SQUAREVOICE("\uf69c"),
    /** A constant used to reference the 'google' icon. */
    GOOGLE("\uf69d"),
    /** A constant used to reference the 'emojigrinalt' icon. */
    EMOJIGRINALT("\uf69e"),
    /** A constant used to reference the 'emojigrin' icon. */
    EMOJIGRIN("\uf69f"),
    /** A constant used to reference the 'constellation' icon. */
    CONSTELLATION("\uf6a0"),
    /** A constant used to reference the 'emojisurprise' icon. */
    EMOJISURPRISE("\uf6a1"),
    /** A constant used to reference the 'emojidead' icon. */
    EMOJIDEAD("\uf6a2"),
    /** A constant used to reference the 'emojiangry' icon. */
    EMOJIANGRY("\uf6a3"),
    /** A constant used to reference the 'emojidevil' icon. */
    EMOJIDEVIL("\uf6a4"),
    /** A constant used to reference the 'emojiwink' icon. */
    EMOJIWINK("\uf6a5"),
    /** A constant used to reference the 'moonorbit' icon. */
    MOONORBIT("\uf6a6"),
    /** A constant used to reference the 'emojismile' icon. */
    EMOJISMILE("\uf6a7"),
    /** A constant used to reference the 'emojisorry' icon. */
    EMOJISORRY("\uf6a8"),
    /** A constant used to reference the 'emojiconfused' icon. */
    EMOJICONFUSED("\uf6a9"),
    /** A constant used to reference the 'emojisleep' icon. */
    EMOJISLEEP("\uf6aa"),
    /** A constant used to reference the 'emojicry' icon. */
    EMOJICRY("\uf6ab"),
    /** A constant used to reference the 'circlefork' icon. */
    CIRCLEFORK("\uf6ac"),
    /** A constant used to reference the 'circlespoon' icon. */
    CIRCLESPOON("\uf6ad"),
    /** A constant used to reference the 'circleknife' icon. */
    CIRCLEKNIFE("\uf6ae"),
    /** A constant used to reference the 'circlepencil' icon. */
    CIRCLEPENCIL("\uf6af"),
    /** A constant used to reference the 'circlehammer' icon. */
    CIRCLEHAMMER("\uf6b0"),
    /** A constant used to reference the 'circlescrewdriver' icon. */
    CIRCLESCREWDRIVER("\uf6b1"),
    /** A constant used to reference the 'middlefinger' icon. */
    MIDDLEFINGER("\uf6b2"),
    /** A constant used to reference the 'heavymetal' icon. */
    HEAVYMETAL("\uf6b3"),
    /** A constant used to reference the 'turnright' icon. */
    TURNRIGHT("\uf6b4"),
    /** A constant used to reference the 'turnleft' icon. */
    TURNLEFT("\uf6b5"),
    /** A constant used to reference the 'vineapp' icon. */
    VINEAPP("\uf6b6"),
    /** A constant used to reference the 'vineappalt' icon. */
    VINEAPPALT("\uf6b7"),
    /** A constant used to reference the 'finance' icon. */
    FINANCE("\uf6b8"),
    /** A constant used to reference the 'survey' icon. */
    SURVEY("\uf6b9"),
    /** A constant used to reference the 'hangouts' icon. */
    HANGOUTS("\uf6ba"),
    /** A constant used to reference the 'square0' icon. */
    SQUARE0("\uf6bb"),
    /** A constant used to reference the 'square1' icon. */
    SQUARE1("\uf6bc"),
    /** A constant used to reference the 'square2' icon. */
    SQUARE2("\uf6bd"),
    /** A constant used to reference the 'square3' icon. */
    SQUARE3("\uf6be"),
    /** A constant used to reference the 'square4' icon. */
    SQUARE4("\uf6bf"),
    /** A constant used to reference the 'square5' icon. */
    SQUARE5("\uf6c0"),
    /** A constant used to reference the 'square6' icon. */
    SQUARE6("\uf6c1"),
    /** A constant used to reference the 'square7' icon. */
    SQUARE7("\uf6c2"),
    /** A constant used to reference the 'square8' icon. */
    SQUARE8("\uf6c3"),
    /** A constant used to reference the 'square9' icon. */
    SQUARE9("\uf6c4"),
    /** A constant used to reference the 'squarea' icon. */
    SQUAREA("\uf6c5"),
    /** A constant used to reference the 'squareb' icon. */
    SQUAREB("\uf6c6"),
    /** A constant used to reference the 'squarec' icon. */
    SQUAREC("\uf6c7"),
    /** A constant used to reference the 'squared' icon. */
    SQUARED("\uf6c8"),
    /** A constant used to reference the 'squaree' icon. */
    SQUAREE("\uf6c9"),
    /** A constant used to reference the 'squaref' icon. */
    SQUAREF("\uf6ca"),
    /** A constant used to reference the 'squareg' icon. */
    SQUAREG("\uf6cb"),
    /** A constant used to reference the 'squareh' icon. */
    SQUAREH("\uf6cc"),
    /** A constant used to reference the 'squarei' icon. */
    SQUAREI("\uf6cd"),
    /** A constant used to reference the 'squarej' icon. */
    SQUAREJ("\uf6ce"),
    /** A constant used to reference the 'squarek' icon. */
    SQUAREK("\uf6cf"),
    /** A constant used to reference the 'squarel' icon. */
    SQUAREL("\uf6d0"),
    /** A constant used to reference the 'squarem' icon. */
    SQUAREM("\uf6d1"),
    /** A constant used to reference the 'squaren' icon. */
    SQUAREN("\uf6d2"),
    /** A constant used to reference the 'squareo' icon. */
    SQUAREO("\uf6d3"),
    /** A constant used to reference the 'squarep' icon. */
    SQUAREP("\uf6d4"),
    /** A constant used to reference the 'squareq' icon. */
    SQUAREQ("\uf6d5"),
    /** A constant used to reference the 'squarer' icon. */
    SQUARER("\uf6d6"),
    /** A constant used to reference the 'squares' icon. */
    SQUARES("\uf6d7"),
    /** A constant used to reference the 'squaret' icon. */
    SQUARET("\uf6d8"),
    /** A constant used to reference the 'squareu' icon. */
    SQUAREU("\uf6d9"),
    /** A constant used to reference the 'squarev' icon. */
    SQUAREV("\uf6da"),
    /** A constant used to reference the 'squarew' icon. */
    SQUAREW("\uf6db"),
    /** A constant used to reference the 'squarex' icon. */
    SQUAREX("\uf6dc"),
    /** A constant used to reference the 'squarey' icon. */
    SQUAREY("\uf6dd"),
    /** A constant used to reference the 'squarez' icon. */
    SQUAREZ("\uf6de"),
    /** A constant used to reference the 'shuttle' icon. */
    SHUTTLE("\uf6df"),
    /** A constant used to reference the 'meteor' icon. */
    METEOR("\uf6e0"),
    /** A constant used to reference the 'galaxy' icon. */
    GALAXY("\uf6e1"),
    /** A constant used to reference the 'observatory' icon. */
    OBSERVATORY("\uf6e2"),
    /** A constant used to reference the 'astronaut' icon. */
    ASTRONAUT("\uf6e3"),
    /** A constant used to reference the 'asteroid' icon. */
    ASTEROID("\uf6e4"),
    /** A constant used to reference the 'sunrise' icon. */
    SUNRISE("\uf6e5"),
    /** A constant used to reference the 'sunset' icon. */
    SUNSET("\uf6e6"),
    /** A constant used to reference the 'tiderise' icon. */
    TIDERISE("\uf6e7"),
    /** A constant used to reference the 'tidefall' icon. */
    TIDEFALL("\uf6e8"),
    /** A constant used to reference the 'mushroomcloud' icon. */
    MUSHROOMCLOUD("\uf6e9"),
    /** A constant used to reference the 'galaxyalt' icon. */
    GALAXYALT("\uf6ea"),
    /** A constant used to reference the 'sputnik' icon. */
    SPUTNIK("\uf6eb"),
    /** A constant used to reference the 'sextant' icon. */
    SEXTANT("\uf6ec"),
    /** A constant used to reference the 'spock' icon. */
    SPOCK("\uf6ed"),
    /** A constant used to reference the 'meteorite' icon. */
    METEORITE("\uf6ee"),
    /** A constant used to reference the 'deathstar' icon. */
    DEATHSTAR("\uf6ef"),
    /** A constant used to reference the 'deathstarbulding' icon. */
    DEATHSTARBULDING("\uf6f0"),
    /** A constant used to reference the 'fallingstar' icon. */
    FALLINGSTAR("\uf6f1"),
    /** A constant used to reference the 'windmill' icon. */
    WINDMILL("\uf6f2"),
    /** A constant used to reference the 'windmillalt' icon. */
    WINDMILLALT("\uf6f3"),
    /** A constant used to reference the 'pumpjack' icon. */
    PUMPJACK("\uf6f4"),
    /** A constant used to reference the 'nuclearplant' icon. */
    NUCLEARPLANT("\uf6f5"),
    /** A constant used to reference the 'solarpanel' icon. */
    SOLARPANEL("\uf6f6"),
    /** A constant used to reference the 'barrel' icon. */
    BARREL("\uf6f7"),
    /** A constant used to reference the 'canister' icon. */
    CANISTER("\uf6f8"),
    /** A constant used to reference the 'railtunnel' icon. */
    RAILTUNNEL("\uf6f9"),
    /** A constant used to reference the 'roadtunnel' icon. */
    ROADTUNNEL("\uf6fa"),
    /** A constant used to reference the 'pickaxe' icon. */
    PICKAXE("\uf6fb"),
    /** A constant used to reference the 'cow' icon. */
    COW("\uf6fc"),
    /** A constant used to reference the 'sheep' icon. */
    SHEEP("\uf6fd"),
    /** A constant used to reference the 'fountain' icon. */
    FOUNTAIN("\uf6fe"),
    /** A constant used to reference the 'circlezero' icon. */
    CIRCLEZERO("\uf6ff"),
    /** A constant used to reference the 'circleone' icon. */
    CIRCLEONE("\uf700"),
    /** A constant used to reference the 'circletwo' icon. */
    CIRCLETWO("\uf701"),
    /** A constant used to reference the 'circlethree' icon. */
    CIRCLETHREE("\uf702"),
    /** A constant used to reference the 'circlefour' icon. */
    CIRCLEFOUR("\uf703"),
    /** A constant used to reference the 'circlefive' icon. */
    CIRCLEFIVE("\uf704"),
    /** A constant used to reference the 'circlesix' icon. */
    CIRCLESIX("\uf705"),
    /** A constant used to reference the 'circleseven' icon. */
    CIRCLESEVEN("\uf706"),
    /** A constant used to reference the 'circleeight' icon. */
    CIRCLEEIGHT("\uf707"),
    /** A constant used to reference the 'circlenine' icon. */
    CIRCLENINE("\uf708"),
    /** A constant used to reference the 'circlea' icon. */
    CIRCLEA("\uf709"),
    /** A constant used to reference the 'circleb' icon. */
    CIRCLEB("\uf70a"),
    /** A constant used to reference the 'circlec' icon. */
    CIRCLEC("\uf70b"),
    /** A constant used to reference the 'circled' icon. */
    CIRCLED("\uf70c"),
    /** A constant used to reference the 'circlee' icon. */
    CIRCLEE("\uf70d"),
    /** A constant used to reference the 'circlef' icon. */
    CIRCLEF("\uf70e"),
    /** A constant used to reference the 'circleg' icon. */
    CIRCLEG("\uf70f"),
    /** A constant used to reference the 'circleh' icon. */
    CIRCLEH("\uf710"),
    /** A constant used to reference the 'circlei' icon. */
    CIRCLEI("\uf711"),
    /** A constant used to reference the 'circlej' icon. */
    CIRCLEJ("\uf712"),
    /** A constant used to reference the 'circlek' icon. */
    CIRCLEK("\uf713"),
    /** A constant used to reference the 'circlel' icon. */
    CIRCLEL("\uf714"),
    /** A constant used to reference the 'circlem' icon. */
    CIRCLEM("\uf715"),
    /** A constant used to reference the 'circlen' icon. */
    CIRCLEN("\uf716"),
    /** A constant used to reference the 'circleo' icon. */
    CIRCLEO("\uf717"),
    /** A constant used to reference the 'circlep' icon. */
    CIRCLEP("\uf718"),
    /** A constant used to reference the 'circleq' icon. */
    CIRCLEQ("\uf719"),
    /** A constant used to reference the 'circler' icon. */
    CIRCLER("\uf71a"),
    /** A constant used to reference the 'circles' icon. */
    CIRCLES("\uf71b"),
    /** A constant used to reference the 'circlet' icon. */
    CIRCLET("\uf71c"),
    /** A constant used to reference the 'circleu' icon. */
    CIRCLEU("\uf71d"),
    /** A constant used to reference the 'circlev' icon. */
    CIRCLEV("\uf71e"),
    /** A constant used to reference the 'circlew' icon. */
    CIRCLEW("\uf71f"),
    /** A constant used to reference the 'circlex' icon. */
    CIRCLEX("\uf720"),
    /** A constant used to reference the 'circley' icon. */
    CIRCLEY("\uf721"),
    /** A constant used to reference the 'circlez' icon. */
    CIRCLEZ("\uf722"),
    /** A constant used to reference the 'creeper' icon. */
    CREEPER("\uf723"),
    /** A constant used to reference the 'minecraft' icon. */
    MINECRAFT("\uf724"),
    /** A constant used to reference the 'minecraftalt' icon. */
    MINECRAFTALT("\uf725"),
    /** A constant used to reference the 'pixelsword' icon. */
    PIXELSWORD("\uf726"),
    /** A constant used to reference the 'pixelbroadsword' icon. */
    PIXELBROADSWORD("\uf727"),
    /** A constant used to reference the 'pixelwand' icon. */
    PIXELWAND("\uf728"),
    /** A constant used to reference the 'pixelpotion' icon. */
    PIXELPOTION("\uf729"),
    /** A constant used to reference the 'pixelpotionalt' icon. */
    PIXELPOTIONALT("\uf72a"),
    /** A constant used to reference the 'pixelpickaxe' icon. */
    PIXELPICKAXE("\uf72b"),
    /** A constant used to reference the 'pixelbow' icon. */
    PIXELBOW("\uf72c"),
    /** A constant used to reference the 'pixelarrow' icon. */
    PIXELARROW("\uf72d"),
    /** A constant used to reference the 'pixelaxe' icon. */
    PIXELAXE("\uf72e"),
    /** A constant used to reference the 'pixeldagger' icon. */
    PIXELDAGGER("\uf72f"),
    /** A constant used to reference the 'pixelbastardsword' icon. */
    PIXELBASTARDSWORD("\uf730"),
    /** A constant used to reference the 'pixellance' icon. */
    PIXELLANCE("\uf731"),
    /** A constant used to reference the 'pixelbattleaxe' icon. */
    PIXELBATTLEAXE("\uf732"),
    /** A constant used to reference the 'pixelshovel' icon. */
    PIXELSHOVEL("\uf733"),
    /** A constant used to reference the 'pixelsphere' icon. */
    PIXELSPHERE("\uf734"),
    /** A constant used to reference the 'pixelelixir' icon. */
    PIXELELIXIR("\uf735"),
    /** A constant used to reference the 'pixelchest' icon. */
    PIXELCHEST("\uf736"),
    /** A constant used to reference the 'pixelshield' icon. */
    PIXELSHIELD("\uf737"),
    /** A constant used to reference the 'pixelheart' icon. */
    PIXELHEART("\uf738"),
    /** A constant used to reference the 'rudder' icon. */
    RUDDER("\uf739"),
    /** A constant used to reference the 'folderalt' icon. */
    FOLDERALT("\uf73a"),
    /** A constant used to reference the 'removefolderalt' icon. */
    REMOVEFOLDERALT("\uf73b"),
    /** A constant used to reference the 'addfolderalt' icon. */
    ADDFOLDERALT("\uf73c"),
    /** A constant used to reference the 'deletefolderalt' icon. */
    DELETEFOLDERALT("\uf73d"),
    /** A constant used to reference the 'openfolderalt' icon. */
    OPENFOLDERALT("\uf73e"),
    /** A constant used to reference the 'clipboardalt' icon. */
    CLIPBOARDALT("\uf73f"),
    /** A constant used to reference the 'pastealt' icon. */
    PASTEALT("\uf740"),
    /** A constant used to reference the 'loadingflowccw' icon. */
    LOADINGFLOWCCW("\uf741"),
    /** A constant used to reference the 'loadingflowcw' icon. */
    LOADINGFLOWCW("\uf742"),
    /** A constant used to reference the 'code' icon. */
    CODE("\uf743"),
    /** A constant used to reference the 'cloveralt' icon. */
    CLOVERALT("\uf744"),
    /** A constant used to reference the 'lips' icon. */
    LIPS("\uf745"),
    /** A constant used to reference the 'kiss' icon. */
    KISS("\uf746"),
    /** A constant used to reference the 'manualshift' icon. */
    MANUALSHIFT("\uf747"),
    /** A constant used to reference the 'simcardthree' icon. */
    SIMCARDTHREE("\uf748"),
    /** A constant used to reference the 'parthenon' icon. */
    PARTHENON("\uf749"),
    /** A constant used to reference the 'addcomment' icon. */
    ADDCOMMENT("\uf74a"),
    /** A constant used to reference the 'deletecomment' icon. */
    DELETECOMMENT("\uf74b"),
    /** A constant used to reference the 'gender' icon. */
    GENDER("\uf74c"),
    /** A constant used to reference the 'callalt' icon. */
    CALLALT("\uf74d"),
    /** A constant used to reference the 'outgoingcallalt' icon. */
    OUTGOINGCALLALT("\uf74e"),
    /** A constant used to reference the 'incomingcallalt' icon. */
    INCOMINGCALLALT("\uf74f"),
    /** A constant used to reference the 'missedcallalt' icon. */
    MISSEDCALLALT("\uf750"),
    /** A constant used to reference the 'export' icon. */
    EXPORT("\uf751"),
    /** A constant used to reference the 'import' icon. */
    IMPORT("\uf752"),
    /** A constant used to reference the 'cherryalt' icon. */
    CHERRYALT("\uf753"),
    /** A constant used to reference the 'panties' icon. */
    PANTIES("\uf754"),
    /** A constant used to reference the 'kimai' icon. */
    KIMAI("\uf755"),
    /** A constant used to reference the 'livejournal' icon. */
    LIVEJOURNAL("\uf756"),
    /** A constant used to reference the 'livejournalalt' icon. */
    LIVEJOURNALALT("\uf757"),
    /** A constant used to reference the 'tagged' icon. */
    TAGGED("\uf758"),
    /** A constant used to reference the 'temple' icon. */
    TEMPLE("\uf759"),
    /** A constant used to reference the 'mayanpyramid' icon. */
    MAYANPYRAMID("\uf75a"),
    /** A constant used to reference the 'egyptpyramid' icon. */
    EGYPTPYRAMID("\uf75b"),
    /** A constant used to reference the 'tampermonkey' icon. */
    TAMPERMONKEY("\uf75c"),
    /** A constant used to reference the 'pushbullet' icon. */
    PUSHBULLET("\uf75d"),
    /** A constant used to reference the 'currents' icon. */
    CURRENTS("\uf75e"),
    /** A constant used to reference the 'communitysmall' icon. */
    COMMUNITYSMALL("\uf75f"),
    /** A constant used to reference the 'squaregithub' icon. */
    SQUAREGITHUB("\uf760"),
    /** A constant used to reference the 'projectfork' icon. */
    PROJECTFORK("\uf761"),
    /** A constant used to reference the 'projectmerge' icon. */
    PROJECTMERGE("\uf762"),
    /** A constant used to reference the 'projectcompare' icon. */
    PROJECTCOMPARE("\uf763"),
    /** A constant used to reference the 'history' icon. */
    HISTORY("\uf764"),
    /** A constant used to reference the 'notebook' icon. */
    NOTEBOOK("\uf765"),
    /** A constant used to reference the 'issue' icon. */
    ISSUE("\uf766"),
    /** A constant used to reference the 'issueclosed' icon. */
    ISSUECLOSED("\uf767"),
    /** A constant used to reference the 'issuereopened' icon. */
    ISSUEREOPENED("\uf768"),
    /** A constant used to reference the 'rubyalt' icon. */
    RUBYALT("\uf769"),
    /** A constant used to reference the 'lighton' icon. */
    LIGHTON("\uf76a"),
    /** A constant used to reference the 'lightoff' icon. */
    LIGHTOFF("\uf76b"),
    /** A constant used to reference the 'bellalt' icon. */
    BELLALT("\uf76c"),
    /** A constant used to reference the 'versions' icon. */
    VERSIONS("\uf777"),
    /** A constant used to reference the 'twog' icon. */
    TWOG("\uf76e"),
    /** A constant used to reference the 'threeg' icon. */
    THREEG("\uf76f"),
    /** A constant used to reference the 'fourg' icon. */
    FOURG("\uf770"),
    /** A constant used to reference the 'gpsalt' icon. */
    GPSALT("\uf771"),
    /** A constant used to reference the 'circleloaderfull' icon. */
    CIRCLELOADERFULL("\uf772"),
    /** A constant used to reference the 'circleloaderseven' icon. */
    CIRCLELOADERSEVEN("\uf773"),
    /** A constant used to reference the 'circleloadersix' icon. */
    CIRCLELOADERSIX("\uf774"),
    /** A constant used to reference the 'circleloaderfive' icon. */
    CIRCLELOADERFIVE("\uf775"),
    /** A constant used to reference the 'circleloaderfour' icon. */
    CIRCLELOADERFOUR("\uf776"),
    /** A constant used to reference the 'circleloaderthree' icon. */
    CIRCLELOADERTHREE("\uf777"),
    /** A constant used to reference the 'circleloadertwo' icon. */
    CIRCLELOADERTWO("\uf778"),
    /** A constant used to reference the 'circleloaderone' icon. */
    CIRCLELOADERONE("\uf779"),
    /** A constant used to reference the 'circleloaderempty' icon. */
    CIRCLELOADEREMPTY("\uf77a"),
    /** A constant used to reference the 'whatsapp' icon. */
    WHATSAPP("\uf77b"),
    /** A constant used to reference the 'whatsappalt' icon. */
    WHATSAPPALT("\uf77c"),
    /** A constant used to reference the 'viber' icon. */
    VIBER("\uf77d"),
    /** A constant used to reference the 'squareviber' icon. */
    SQUAREVIBER("\uf77e"),
    /** A constant used to reference the 'teamviewer' icon. */
    TEAMVIEWER("\uf77f"),
    /** A constant used to reference the 'tunein' icon. */
    TUNEIN("\uf780"),
    /** A constant used to reference the 'tuneinalt' icon. */
    TUNEINALT("\uf781"),
    /** A constant used to reference the 'weightscale' icon. */
    WEIGHTSCALE("\uf782"),
    /** A constant used to reference the 'boxing' icon. */
    BOXING("\uf783"),
    /** A constant used to reference the 'speedalt' icon. */
    SPEEDALT("\uf784"),
    /** A constant used to reference the 'scriptalt' icon. */
    SCRIPTALT("\uf785"),
    /** A constant used to reference the 'splitthree' icon. */
    SPLITTHREE("\uf786"),
    /** A constant used to reference the 'mergethree' icon. */
    MERGETHREE("\uf787"),
    /** A constant used to reference the 'layersthree' icon. */
    LAYERSTHREE("\uf788"),
    /** A constant used to reference the 'mutemic' icon. */
    MUTEMIC("\uf789"),
    /** A constant used to reference the 'zerply' icon. */
    ZERPLY("\uf78a"),
    /** A constant used to reference the 'circlegoogleplus' icon. */
    CIRCLEGOOGLEPLUS("\uf78b"),
    /** A constant used to reference the 'circletwitter' icon. */
    CIRCLETWITTER("\uf78c"),
    /** A constant used to reference the 'circlefacebook' icon. */
    CIRCLEFACEBOOK("\uf78d"),
    /** A constant used to reference the 'circleyahoo' icon. */
    CIRCLEYAHOO("\uf78e"),
    /** A constant used to reference the 'circlegithub' icon. */
    CIRCLEGITHUB("\uf78f"),
    /** A constant used to reference the 'forumsalt' icon. */
    FORUMSALT("\uf790"),
    /** A constant used to reference the 'circlepath' icon. */
    CIRCLEPATH("\uf791"),
    /** A constant used to reference the 'circlevimeo' icon. */
    CIRCLEVIMEO("\uf792"),
    /** A constant used to reference the 'circlevine' icon. */
    CIRCLEVINE("\uf793"),
    /** A constant used to reference the 'instagramtwo' icon. */
    INSTAGRAMTWO("\uf794"),
    /** A constant used to reference the 'instagramthree' icon. */
    INSTAGRAMTHREE("\uf795"),
    /** A constant used to reference the 'flickrthree' icon. */
    FLICKRTHREE("\uf796"),
    /** A constant used to reference the 'quora' icon. */
    QUORA("\uf797"),
    /** A constant used to reference the 'squarequora' icon. */
    SQUAREQUORA("\uf798"),
    /** A constant used to reference the 'circlequora' icon. */
    CIRCLEQUORA("\uf799"),
    /** A constant used to reference the 'picasa' icon. */
    PICASA("\uf79a"),
    /** A constant used to reference the 'branch' icon. */
    BRANCH("\uf79b"),
    /** A constant used to reference the 'ingress' icon. */
    INGRESS("\uf79c"),
    /** A constant used to reference the 'squarezerply' icon. */
    SQUAREZERPLY("\uf79d"),
    /** A constant used to reference the 'circlezerply' icon. */
    CIRCLEZERPLY("\uf79e"),
    /** A constant used to reference the 'squarevimeo' icon. */
    SQUAREVIMEO("\uf79f"),
    /** A constant used to reference the 'squaretwitter' icon. */
    SQUARETWITTER("\uf7a0"),
    /** A constant used to reference the 'brightnessalt' icon. */
    BRIGHTNESSALT("\uf7a1"),
    /** A constant used to reference the 'brightnessalthalf' icon. */
    BRIGHTNESSALTHALF("\uf7a2"),
    /** A constant used to reference the 'brightnessaltfull' icon. */
    BRIGHTNESSALTFULL("\uf7a3"),
    /** A constant used to reference the 'brightnessaltauto' icon. */
    BRIGHTNESSALTAUTO("\uf7a4"),
    /** A constant used to reference the 'shirtbuttonthree' icon. */
    SHIRTBUTTONTHREE("\uf7a5"),
    /** A constant used to reference the 'openshare' icon. */
    OPENSHARE("\uf7a6"),
    /** A constant used to reference the 'copyapp' icon. */
    COPYAPP("\uf7a7"),
    /** A constant used to reference the 'bowl' icon. */
    BOWL("\uf7a8"),
    /** A constant used to reference the 'cloudalt' icon. */
    CLOUDALT("\uf7a9"),
    /** A constant used to reference the 'cloudaltdownload' icon. */
    CLOUDALTDOWNLOAD("\uf7aa"),
    /** A constant used to reference the 'cloudaltupload' icon. */
    CLOUDALTUPLOAD("\uf7ab"),
    /** A constant used to reference the 'cloudaltsync' icon. */
    CLOUDALTSYNC("\uf7ac"),
    /** A constant used to reference the 'cloudaltprivate' icon. */
    CLOUDALTPRIVATE("\uf7ad"),
    /** A constant used to reference the 'flipboard' icon. */
    FLIPBOARD("\uf7ae"),
    /** A constant used to reference the 'octoloaderempty' icon. */
    OCTOLOADEREMPTY("\uf7af"),
    /** A constant used to reference the 'octoloaderone' icon. */
    OCTOLOADERONE("\uf7b0"),
    /** A constant used to reference the 'octoloadertwo' icon. */
    OCTOLOADERTWO("\uf7b1"),
    /** A constant used to reference the 'octoloaderthree' icon. */
    OCTOLOADERTHREE("\uf7b2"),
    /** A constant used to reference the 'octoloaderfour' icon. */
    OCTOLOADERFOUR("\uf7b3"),
    /** A constant used to reference the 'octoloaderfive' icon. */
    OCTOLOADERFIVE("\uf7b4"),
    /** A constant used to reference the 'octoloadersix' icon. */
    OCTOLOADERSIX("\uf7b5"),
    /** A constant used to reference the 'octoloaderseven' icon. */
    OCTOLOADERSEVEN("\uf7b6"),
    /** A constant used to reference the 'octoloaderfull' icon. */
    OCTOLOADERFULL("\uf7b7"),
    /** A constant used to reference the 'selectionsymbol' icon. */
    SELECTIONSYMBOL("\uf7b8"),
    /** A constant used to reference the 'infinityalt' icon. */
    INFINITYALT("\uf7b9"),
    /** A constant used to reference the 'pullrequest' icon. */
    PULLREQUEST("\uf7ba"),
    /** A constant used to reference the 'projectforkdelete' icon. */
    PROJECTFORKDELETE("\uf7bb"),
    /** A constant used to reference the 'projectforkprivate' icon. */
    PROJECTFORKPRIVATE("\uf7bc"),
    /** A constant used to reference the 'commit' icon. */
    COMMIT("\uf7bd"),
    /** A constant used to reference the 'htmlfile' icon. */
    HTMLFILE("\uf7be"),
    /** A constant used to reference the 'pushalt' icon. */
    PUSHALT("\uf7bf"),
    /** A constant used to reference the 'pullalt' icon. */
    PULLALT("\uf7c0"),
    /** A constant used to reference the 'photonineframes' icon. */
    PHOTONINEFRAMES("\uf7c1"),
    /** A constant used to reference the 'wetfloor' icon. */
    WETFLOOR("\uf7c2"),
    /** A constant used to reference the 'instagramfour' icon. */
    INSTAGRAMFOUR("\uf7c3"),
    /** A constant used to reference the 'circleinstagram' icon. */
    CIRCLEINSTAGRAM("\uf7c4"),
    /** A constant used to reference the 'videocamerathree' icon. */
    VIDEOCAMERATHREE("\uf7c5"),
    /** A constant used to reference the 'subtitles' icon. */
    SUBTITLES("\uf7c6"),
    /** A constant used to reference the 'subtitlesoff' icon. */
    SUBTITLESOFF("\uf7c7"),
    /** A constant used to reference the 'compress' icon. */
    COMPRESS("\uf7c8"),
    /** A constant used to reference the 'baby' icon. */
    BABY("\uf7c9"),
    /** A constant used to reference the 'ducky' icon. */
    DUCKY("\uf7ca"),
    /** A constant used to reference the 'handswipe' icon. */
    HANDSWIPE("\uf7cb"),
    /** A constant used to reference the 'swipeup' icon. */
    SWIPEUP("\uf7cc"),
    /** A constant used to reference the 'swipedown' icon. */
    SWIPEDOWN("\uf7cd"),
    /** A constant used to reference the 'twofingerswipedown' icon. */
    TWOFINGERSWIPEDOWN("\uf7ce"),
    /** A constant used to reference the 'twofingerswipeup' icon. */
    TWOFINGERSWIPEUP("\uf7cf"),
    /** A constant used to reference the 'doubletap' icon. */
    DOUBLETAP("\uf7d0"),
    /** A constant used to reference the 'dribbblealt' icon. */
    DRIBBBLEALT("\uf7d1"),
    /** A constant used to reference the 'circlecallmissed' icon. */
    CIRCLECALLMISSED("\uf7d2"),
    /** A constant used to reference the 'circlecallincoming' icon. */
    CIRCLECALLINCOMING("\uf7d3"),
    /** A constant used to reference the 'circlecalloutgoing' icon. */
    CIRCLECALLOUTGOING("\uf7d4"),
    /** A constant used to reference the 'circledownload' icon. */
    CIRCLEDOWNLOAD("\uf7d5"),
    /** A constant used to reference the 'circleupload' icon. */
    CIRCLEUPLOAD("\uf7d6"),
    /** A constant used to reference the 'minismile' icon. */
    MINISMILE("\uf7d7"),
    /** A constant used to reference the 'minisad' icon. */
    MINISAD("\uf7d8"),
    /** A constant used to reference the 'minilaugh' icon. */
    MINILAUGH("\uf7d9"),
    /** A constant used to reference the 'minigrin' icon. */
    MINIGRIN("\uf7da"),
    /** A constant used to reference the 'miniangry' icon. */
    MINIANGRY("\uf7db"),
    /** A constant used to reference the 'minitongue' icon. */
    MINITONGUE("\uf7dc"),
    /** A constant used to reference the 'minitonguealt' icon. */
    MINITONGUEALT("\uf7dd"),
    /** A constant used to reference the 'miniwink' icon. */
    MINIWINK("\uf7de"),
    /** A constant used to reference the 'minitonguewink' icon. */
    MINITONGUEWINK("\uf7df"),
    /** A constant used to reference the 'miniconfused' icon. */
    MINICONFUSED("\uf7e0"),
    /** A constant used to reference the 'soundright' icon. */
    SOUNDRIGHT("\uf7e1"),
    /** A constant used to reference the 'soundleft' icon. */
    SOUNDLEFT("\uf7e2"),
    /** A constant used to reference the 'savetodrive' icon. */
    SAVETODRIVE("\uf7e3"),
    /** A constant used to reference the 'layerorderup' icon. */
    LAYERORDERUP("\uf7e4"),
    /** A constant used to reference the 'layerorderdown' icon. */
    LAYERORDERDOWN("\uf7e5"),
    /** A constant used to reference the 'layerorder' icon. */
    LAYERORDER("\uf7e6"),
    /** A constant used to reference the 'circledribbble' icon. */
    CIRCLEDRIBBBLE("\uf7e7"),
    /** A constant used to reference the 'squaredribbble' icon. */
    SQUAREDRIBBBLE("\uf7e8"),
    /** A constant used to reference the 'handexpand' icon. */
    HANDEXPAND("\uf7e9"),
    /** A constant used to reference the 'handpinch' icon. */
    HANDPINCH("\uf7ea"),
    /** A constant used to reference the 'fontserif' icon. */
    FONTSERIF("\uf7eb"),
    /** A constant used to reference the 'fontsansserif' icon. */
    FONTSANSSERIF("\uf7ec"),
    /** A constant used to reference the 'fontrounded' icon. */
    FONTROUNDED("\uf7ed"),
    /** A constant used to reference the 'fonthandwriting' icon. */
    FONTHANDWRITING("\uf7ee"),
    /** A constant used to reference the 'fonttypewriter' icon. */
    FONTTYPEWRITER("\uf7ef"),
    /** A constant used to reference the 'fontcomic' icon. */
    FONTCOMIC("\uf7f0"),
    /** A constant used to reference the 'fontcaligraphy' icon. */
    FONTCALIGRAPHY("\uf7f1"),
    /** A constant used to reference the 'fontgothic' icon. */
    FONTGOTHIC("\uf7f2"),
    /** A constant used to reference the 'fontstencil' icon. */
    FONTSTENCIL("\uf7f3");

    static
    {
        GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(SwingUtilities.WEB_HOSTING_HUB_GLYPHS_FONT);
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
    private WebHostingHubGlyphs(String pFontCode)
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
        return SwingUtilities.WEB_HOSTING_HUB_GLYPHS_FONT;
    }
}
