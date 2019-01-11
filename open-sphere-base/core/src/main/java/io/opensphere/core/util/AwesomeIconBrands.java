package io.opensphere.core.util;

import java.awt.Font;
import java.awt.GraphicsEnvironment;

import io.opensphere.core.util.swing.SwingUtilities;

/**
 * An enumeration over the set of available FontAwesome Brand icons.
 */
public enum AwesomeIconBrands implements FontIconEnum
{
    /** A constant used to reference the '500px' icon. */
    ICON_500PX("\uf26e"),

    /** A constant used to reference the 'accessible_icon' icon. */
    ACCESSIBLE_ICON("\uf368"),

    /** A constant used to reference the 'accusoft' icon. */
    ACCUSOFT("\uf369"),

    /** A constant used to reference the 'adn' icon. */
    ADN("\uf170"),

    /** A constant used to reference the 'adversal' icon. */
    ADVERSAL("\uf36a"),

    /** A constant used to reference the 'affiliatetheme' icon. */
    AFFILIATETHEME("\uf36b"),

    /** A constant used to reference the 'algolia' icon. */
    ALGOLIA("\uf36c"),

    /** A constant used to reference the 'amazon' icon. */
    AMAZON("\uf270"),

    /** A constant used to reference the 'amazon_pay' icon. */
    AMAZON_PAY("\uf42c"),

    /** A constant used to reference the 'amilia' icon. */
    AMILIA("\uf36d"),

    /** A constant used to reference the 'android' icon. */
    ANDROID("\uf17b"),

    /** A constant used to reference the 'angellist' icon. */
    ANGELLIST("\uf209"),

    /** A constant used to reference the 'angrycreative' icon. */
    ANGRYCREATIVE("\uf36e"),

    /** A constant used to reference the 'angular' icon. */
    ANGULAR("\uf420"),

    /** A constant used to reference the 'app_store' icon. */
    APP_STORE("\uf36f"),

    /** A constant used to reference the 'app_store_ios' icon. */
    APP_STORE_IOS("\uf370"),

    /** A constant used to reference the 'apper' icon. */
    APPER("\uf371"),

    /** A constant used to reference the 'apple' icon. */
    APPLE("\uf179"),

    /** A constant used to reference the 'apple_pay' icon. */
    APPLE_PAY("\uf415"),

    /** A constant used to reference the 'asymmetrik' icon. */
    ASYMMETRIK("\uf372"),

    /** A constant used to reference the 'audible' icon. */
    AUDIBLE("\uf373"),

    /** A constant used to reference the 'autoprefixer' icon. */
    AUTOPREFIXER("\uf41c"),

    /** A constant used to reference the 'avianex' icon. */
    AVIANEX("\uf374"),

    /** A constant used to reference the 'aviato' icon. */
    AVIATO("\uf421"),

    /** A constant used to reference the 'aws' icon. */
    AWS("\uf375"),

    /** A constant used to reference the 'bandcamp' icon. */
    BANDCAMP("\uf2d5"),

    /** A constant used to reference the 'behance' icon. */
    BEHANCE("\uf1b4"),

    /** A constant used to reference the 'behance_square' icon. */
    BEHANCE_SQUARE("\uf1b5"),

    /** A constant used to reference the 'bimobject' icon. */
    BIMOBJECT("\uf378"),

    /** A constant used to reference the 'bitbucket' icon. */
    BITBUCKET("\uf171"),

    /** A constant used to reference the 'bitcoin' icon. */
    BITCOIN("\uf379"),

    /** A constant used to reference the 'bity' icon. */
    BITY("\uf37a"),

    /** A constant used to reference the 'black_tie' icon. */
    BLACK_TIE("\uf27e"),

    /** A constant used to reference the 'blackberry' icon. */
    BLACKBERRY("\uf37b"),

    /** A constant used to reference the 'blogger' icon. */
    BLOGGER("\uf37c"),

    /** A constant used to reference the 'blogger_b' icon. */
    BLOGGER_B("\uf37d"),

    /** A constant used to reference the 'bluetooth' icon. */
    BLUETOOTH("\uf293"),

    /** A constant used to reference the 'bluetooth_b' icon. */
    BLUETOOTH_B("\uf294"),

    /** A constant used to reference the 'btc' icon. */
    BTC("\uf15a"),

    /** A constant used to reference the 'buromobelexperte' icon. */
    BUROMOBELEXPERTE("\uf37f"),

    /** A constant used to reference the 'cc_amazon_pay' icon. */
    CC_AMAZON_PAY("\uf42d"),

    /** A constant used to reference the 'cc_amex' icon. */
    CC_AMEX("\uf1f3"),

    /** A constant used to reference the 'cc_apple_pay' icon. */
    CC_APPLE_PAY("\uf416"),

    /** A constant used to reference the 'cc_diners_club' icon. */
    CC_DINERS_CLUB("\uf24c"),

    /** A constant used to reference the 'cc_discover' icon. */
    CC_DISCOVER("\uf1f2"),

    /** A constant used to reference the 'cc_jcb' icon. */
    CC_JCB("\uf24b"),

    /** A constant used to reference the 'cc_mastercard' icon. */
    CC_MASTERCARD("\uf1f1"),

    /** A constant used to reference the 'cc_paypal' icon. */
    CC_PAYPAL("\uf1f4"),

    /** A constant used to reference the 'cc_stripe' icon. */
    CC_STRIPE("\uf1f5"),

    /** A constant used to reference the 'cc_visa' icon. */
    CC_VISA("\uf1f0"),

    /** A constant used to reference the 'centercode' icon. */
    CENTERCODE("\uf380"),

    /** A constant used to reference the 'chrome' icon. */
    CHROME("\uf268"),

    /** A constant used to reference the 'cloudscale' icon. */
    CLOUDSCALE("\uf383"),

    /** A constant used to reference the 'cloudsmith' icon. */
    CLOUDSMITH("\uf384"),

    /** A constant used to reference the 'cloudversify' icon. */
    CLOUDVERSIFY("\uf385"),

    /** A constant used to reference the 'codepen' icon. */
    CODEPEN("\uf1cb"),

    /** A constant used to reference the 'codiepie' icon. */
    CODIEPIE("\uf284"),

    /** A constant used to reference the 'connectdevelop' icon. */
    CONNECTDEVELOP("\uf20e"),

    /** A constant used to reference the 'contao' icon. */
    CONTAO("\uf26d"),

    /** A constant used to reference the 'cpanel' icon. */
    CPANEL("\uf388"),

    /** A constant used to reference the 'creative_commons' icon. */
    CREATIVE_COMMONS("\uf25e"),

    /** A constant used to reference the 'creative_commons_by' icon. */
    CREATIVE_COMMONS_BY("\uf4e7"),

    /** A constant used to reference the 'creative_commons_nc' icon. */
    CREATIVE_COMMONS_NC("\uf4e8"),

    /** A constant used to reference the 'creative_commons_nc_eu' icon. */
    CREATIVE_COMMONS_NC_EU("\uf4e9"),

    /** A constant used to reference the 'creative_commons_nc_jp' icon. */
    CREATIVE_COMMONS_NC_JP("\uf4ea"),

    /** A constant used to reference the 'creative_commons_nd' icon. */
    CREATIVE_COMMONS_ND("\uf4eb"),

    /** A constant used to reference the 'creative_commons_pd' icon. */
    CREATIVE_COMMONS_PD("\uf4ec"),

    /** A constant used to reference the 'creative_commons_pd_alt' icon. */
    CREATIVE_COMMONS_PD_ALT("\uf4ed"),

    /** A constant used to reference the 'creative_commons_remix' icon. */
    CREATIVE_COMMONS_REMIX("\uf4ee"),

    /** A constant used to reference the 'creative_commons_sa' icon. */
    CREATIVE_COMMONS_SA("\uf4ef"),

    /** A constant used to reference the 'creative_commons_sampling' icon. */
    CREATIVE_COMMONS_SAMPLING("\uf4f0"),

    /**
     * A constant used to reference the 'creative_commons_sampling_plus' icon.
     */
    CREATIVE_COMMONS_SAMPLING_PLUS("\uf4f1"),

    /** A constant used to reference the 'creative_commons_share' icon. */
    CREATIVE_COMMONS_SHARE("\uf4f2"),

    /** A constant used to reference the 'css3' icon. */
    CSS3("\uf13c"),

    /** A constant used to reference the 'css3_alt' icon. */
    CSS3_ALT("\uf38b"),

    /** A constant used to reference the 'cuttlefish' icon. */
    CUTTLEFISH("\uf38c"),

    /** A constant used to reference the 'd_and_d' icon. */
    D_AND_D("\uf38d"),

    /** A constant used to reference the 'dashcube' icon. */
    DASHCUBE("\uf210"),

    /** A constant used to reference the 'delicious' icon. */
    DELICIOUS("\uf1a5"),

    /** A constant used to reference the 'deploydog' icon. */
    DEPLOYDOG("\uf38e"),

    /** A constant used to reference the 'deskpro' icon. */
    DESKPRO("\uf38f"),

    /** A constant used to reference the 'deviantart' icon. */
    DEVIANTART("\uf1bd"),

    /** A constant used to reference the 'digg' icon. */
    DIGG("\uf1a6"),

    /** A constant used to reference the 'digital_ocean' icon. */
    DIGITAL_OCEAN("\uf391"),

    /** A constant used to reference the 'discord' icon. */
    DISCORD("\uf392"),

    /** A constant used to reference the 'discourse' icon. */
    DISCOURSE("\uf393"),

    /** A constant used to reference the 'dochub' icon. */
    DOCHUB("\uf394"),

    /** A constant used to reference the 'docker' icon. */
    DOCKER("\uf395"),

    /** A constant used to reference the 'draft2digital' icon. */
    DRAFT2DIGITAL("\uf396"),

    /** A constant used to reference the 'dribbble' icon. */
    DRIBBBLE("\uf17d"),

    /** A constant used to reference the 'dribbble_square' icon. */
    DRIBBBLE_SQUARE("\uf397"),

    /** A constant used to reference the 'dropbox' icon. */
    DROPBOX("\uf16b"),

    /** A constant used to reference the 'drupal' icon. */
    DRUPAL("\uf1a9"),

    /** A constant used to reference the 'dyalog' icon. */
    DYALOG("\uf399"),

    /** A constant used to reference the 'earlybirds' icon. */
    EARLYBIRDS("\uf39a"),

    /** A constant used to reference the 'ebay' icon. */
    EBAY("\uf4f4"),

    /** A constant used to reference the 'edge' icon. */
    EDGE("\uf282"),

    /** A constant used to reference the 'elementor' icon. */
    ELEMENTOR("\uf430"),

    /** A constant used to reference the 'ember' icon. */
    EMBER("\uf423"),

    /** A constant used to reference the 'empire' icon. */
    EMPIRE("\uf1d1"),

    /** A constant used to reference the 'envira' icon. */
    ENVIRA("\uf299"),

    /** A constant used to reference the 'erlang' icon. */
    ERLANG("\uf39d"),

    /** A constant used to reference the 'ethereum' icon. */
    ETHEREUM("\uf42e"),

    /** A constant used to reference the 'etsy' icon. */
    ETSY("\uf2d7"),

    /** A constant used to reference the 'expeditedssl' icon. */
    EXPEDITEDSSL("\uf23e"),

    /** A constant used to reference the 'facebook' icon. */
    FACEBOOK("\uf09a"),

    /** A constant used to reference the 'facebook_f' icon. */
    FACEBOOK_F("\uf39e"),

    /** A constant used to reference the 'facebook_messenger' icon. */
    FACEBOOK_MESSENGER("\uf39f"),

    /** A constant used to reference the 'facebook_square' icon. */
    FACEBOOK_SQUARE("\uf082"),

    /** A constant used to reference the 'firefox' icon. */
    FIREFOX("\uf269"),

    /** A constant used to reference the 'first_order' icon. */
    FIRST_ORDER("\uf2b0"),

    /** A constant used to reference the 'first_order_alt' icon. */
    FIRST_ORDER_ALT("\uf50a"),

    /** A constant used to reference the 'firstdraft' icon. */
    FIRSTDRAFT("\uf3a1"),

    /** A constant used to reference the 'flickr' icon. */
    FLICKR("\uf16e"),

    /** A constant used to reference the 'flipboard' icon. */
    FLIPBOARD("\uf44d"),

    /** A constant used to reference the 'fly' icon. */
    FLY("\uf417"),

    /** A constant used to reference the 'font_awesome' icon. */
    FONT_AWESOME("\uf2b4"),

    /** A constant used to reference the 'font_awesome_alt' icon. */
    FONT_AWESOME_ALT("\uf35c"),

    /** A constant used to reference the 'font_awesome_flag' icon. */
    FONT_AWESOME_FLAG("\uf425"),

    /** A constant used to reference the 'font_awesome_logo_full' icon. */
    FONT_AWESOME_LOGO_FULL("\uf4e6"),

    /** A constant used to reference the 'fonticons' icon. */
    FONTICONS("\uf280"),

    /** A constant used to reference the 'fonticons_fi' icon. */
    FONTICONS_FI("\uf3a2"),

    /** A constant used to reference the 'fort_awesome' icon. */
    FORT_AWESOME("\uf286"),

    /** A constant used to reference the 'fort_awesome_alt' icon. */
    FORT_AWESOME_ALT("\uf3a3"),

    /** A constant used to reference the 'forumbee' icon. */
    FORUMBEE("\uf211"),

    /** A constant used to reference the 'foursquare' icon. */
    FOURSQUARE("\uf180"),

    /** A constant used to reference the 'free_code_camp' icon. */
    FREE_CODE_CAMP("\uf2c5"),

    /** A constant used to reference the 'freebsd' icon. */
    FREEBSD("\uf3a4"),

    /** A constant used to reference the 'fulcrum' icon. */
    FULCRUM("\uf50b"),

    /** A constant used to reference the 'galactic_republic' icon. */
    GALACTIC_REPUBLIC("\uf50c"),

    /** A constant used to reference the 'galactic_senate' icon. */
    GALACTIC_SENATE("\uf50d"),

    /** A constant used to reference the 'get_pocket' icon. */
    GET_POCKET("\uf265"),

    /** A constant used to reference the 'gg' icon. */
    GG("\uf260"),

    /** A constant used to reference the 'gg_circle' icon. */
    GG_CIRCLE("\uf261"),

    /** A constant used to reference the 'git' icon. */
    GIT("\uf1d3"),

    /** A constant used to reference the 'git_square' icon. */
    GIT_SQUARE("\uf1d2"),

    /** A constant used to reference the 'github' icon. */
    GITHUB("\uf09b"),

    /** A constant used to reference the 'github_alt' icon. */
    GITHUB_ALT("\uf113"),

    /** A constant used to reference the 'github_square' icon. */
    GITHUB_SQUARE("\uf092"),

    /** A constant used to reference the 'gitkraken' icon. */
    GITKRAKEN("\uf3a6"),

    /** A constant used to reference the 'gitlab' icon. */
    GITLAB("\uf296"),

    /** A constant used to reference the 'gitter' icon. */
    GITTER("\uf426"),

    /** A constant used to reference the 'glide' icon. */
    GLIDE("\uf2a5"),

    /** A constant used to reference the 'glide_g' icon. */
    GLIDE_G("\uf2a6"),

    /** A constant used to reference the 'gofore' icon. */
    GOFORE("\uf3a7"),

    /** A constant used to reference the 'goodreads' icon. */
    GOODREADS("\uf3a8"),

    /** A constant used to reference the 'goodreads_g' icon. */
    GOODREADS_G("\uf3a9"),

    /** A constant used to reference the 'google' icon. */
    GOOGLE("\uf1a0"),

    /** A constant used to reference the 'google_drive' icon. */
    GOOGLE_DRIVE("\uf3aa"),

    /** A constant used to reference the 'google_play' icon. */
    GOOGLE_PLAY("\uf3ab"),

    /** A constant used to reference the 'google_plus' icon. */
    GOOGLE_PLUS("\uf2b3"),

    /** A constant used to reference the 'google_plus_g' icon. */
    GOOGLE_PLUS_G("\uf0d5"),

    /** A constant used to reference the 'google_plus_square' icon. */
    GOOGLE_PLUS_SQUARE("\uf0d4"),

    /** A constant used to reference the 'google_wallet' icon. */
    GOOGLE_WALLET("\uf1ee"),

    /** A constant used to reference the 'gratipay' icon. */
    GRATIPAY("\uf184"),

    /** A constant used to reference the 'grav' icon. */
    GRAV("\uf2d6"),

    /** A constant used to reference the 'gripfire' icon. */
    GRIPFIRE("\uf3ac"),

    /** A constant used to reference the 'grunt' icon. */
    GRUNT("\uf3ad"),

    /** A constant used to reference the 'gulp' icon. */
    GULP("\uf3ae"),

    /** A constant used to reference the 'hacker_news' icon. */
    HACKER_NEWS("\uf1d4"),

    /** A constant used to reference the 'hacker_news_square' icon. */
    HACKER_NEWS_SQUARE("\uf3af"),

    /** A constant used to reference the 'hips' icon. */
    HIPS("\uf452"),

    /** A constant used to reference the 'hire_a_helper' icon. */
    HIRE_A_HELPER("\uf3b0"),

    /** A constant used to reference the 'hooli' icon. */
    HOOLI("\uf427"),

    /** A constant used to reference the 'hotjar' icon. */
    HOTJAR("\uf3b1"),

    /** A constant used to reference the 'houzz' icon. */
    HOUZZ("\uf27c"),

    /** A constant used to reference the 'html5' icon. */
    HTML5("\uf13b"),

    /** A constant used to reference the 'hubspot' icon. */
    HUBSPOT("\uf3b2"),

    /** A constant used to reference the 'imdb' icon. */
    IMDB("\uf2d8"),

    /** A constant used to reference the 'instagram' icon. */
    INSTAGRAM("\uf16d"),

    /** A constant used to reference the 'internet_explorer' icon. */
    INTERNET_EXPLORER("\uf26b"),

    /** A constant used to reference the 'ioxhost' icon. */
    IOXHOST("\uf208"),

    /** A constant used to reference the 'itunes' icon. */
    ITUNES("\uf3b4"),

    /** A constant used to reference the 'itunes_note' icon. */
    ITUNES_NOTE("\uf3b5"),

    /** A constant used to reference the 'java' icon. */
    JAVA("\uf4e4"),

    /** A constant used to reference the 'jedi_order' icon. */
    JEDI_ORDER("\uf50e"),

    /** A constant used to reference the 'jenkins' icon. */
    JENKINS("\uf3b6"),

    /** A constant used to reference the 'joget' icon. */
    JOGET("\uf3b7"),

    /** A constant used to reference the 'joomla' icon. */
    JOOMLA("\uf1aa"),

    /** A constant used to reference the 'js' icon. */
    JS("\uf3b8"),

    /** A constant used to reference the 'js_square' icon. */
    JS_SQUARE("\uf3b9"),

    /** A constant used to reference the 'jsfiddle' icon. */
    JSFIDDLE("\uf1cc"),

    /** A constant used to reference the 'keybase' icon. */
    KEYBASE("\uf4f5"),

    /** A constant used to reference the 'keycdn' icon. */
    KEYCDN("\uf3ba"),

    /** A constant used to reference the 'kickstarter' icon. */
    KICKSTARTER("\uf3bb"),

    /** A constant used to reference the 'kickstarter_k' icon. */
    KICKSTARTER_K("\uf3bc"),

    /** A constant used to reference the 'korvue' icon. */
    KORVUE("\uf42f"),

    /** A constant used to reference the 'laravel' icon. */
    LARAVEL("\uf3bd"),

    /** A constant used to reference the 'lastfm' icon. */
    LASTFM("\uf202"),

    /** A constant used to reference the 'lastfm_square' icon. */
    LASTFM_SQUARE("\uf203"),

    /** A constant used to reference the 'leanpub' icon. */
    LEANPUB("\uf212"),

    /** A constant used to reference the 'less' icon. */
    LESS("\uf41d"),

    /** A constant used to reference the 'line' icon. */
    LINE("\uf3c0"),

    /** A constant used to reference the 'linkedin' icon. */
    LINKEDIN("\uf08c"),

    /** A constant used to reference the 'linkedin_in' icon. */
    LINKEDIN_IN("\uf0e1"),

    /** A constant used to reference the 'linode' icon. */
    LINODE("\uf2b8"),

    /** A constant used to reference the 'linux' icon. */
    LINUX("\uf17c"),

    /** A constant used to reference the 'lyft' icon. */
    LYFT("\uf3c3"),

    /** A constant used to reference the 'magento' icon. */
    MAGENTO("\uf3c4"),

    /** A constant used to reference the 'mandalorian' icon. */
    MANDALORIAN("\uf50f"),

    /** A constant used to reference the 'mastodon' icon. */
    MASTODON("\uf4f6"),

    /** A constant used to reference the 'maxcdn' icon. */
    MAXCDN("\uf136"),

    /** A constant used to reference the 'medapps' icon. */
    MEDAPPS("\uf3c6"),

    /** A constant used to reference the 'medium' icon. */
    MEDIUM("\uf23a"),

    /** A constant used to reference the 'medium_m' icon. */
    MEDIUM_M("\uf3c7"),

    /** A constant used to reference the 'medrt' icon. */
    MEDRT("\uf3c8"),

    /** A constant used to reference the 'meetup' icon. */
    MEETUP("\uf2e0"),

    /** A constant used to reference the 'microsoft' icon. */
    MICROSOFT("\uf3ca"),

    /** A constant used to reference the 'mix' icon. */
    MIX("\uf3cb"),

    /** A constant used to reference the 'mixcloud' icon. */
    MIXCLOUD("\uf289"),

    /** A constant used to reference the 'mizuni' icon. */
    MIZUNI("\uf3cc"),

    /** A constant used to reference the 'modx' icon. */
    MODX("\uf285"),

    /** A constant used to reference the 'monero' icon. */
    MONERO("\uf3d0"),

    /** A constant used to reference the 'napster' icon. */
    NAPSTER("\uf3d2"),

    /** A constant used to reference the 'nintendo_switch' icon. */
    NINTENDO_SWITCH("\uf418"),

    /** A constant used to reference the 'node' icon. */
    NODE("\uf419"),

    /** A constant used to reference the 'node_js' icon. */
    NODE_JS("\uf3d3"),

    /** A constant used to reference the 'npm' icon. */
    NPM("\uf3d4"),

    /** A constant used to reference the 'ns8' icon. */
    NS8("\uf3d5"),

    /** A constant used to reference the 'nutritionix' icon. */
    NUTRITIONIX("\uf3d6"),

    /** A constant used to reference the 'odnoklassniki' icon. */
    ODNOKLASSNIKI("\uf263"),

    /** A constant used to reference the 'odnoklassniki_square' icon. */
    ODNOKLASSNIKI_SQUARE("\uf264"),

    /** A constant used to reference the 'old_republic' icon. */
    OLD_REPUBLIC("\uf510"),

    /** A constant used to reference the 'opencart' icon. */
    OPENCART("\uf23d"),

    /** A constant used to reference the 'openid' icon. */
    OPENID("\uf19b"),

    /** A constant used to reference the 'opera' icon. */
    OPERA("\uf26a"),

    /** A constant used to reference the 'optin_monster' icon. */
    OPTIN_MONSTER("\uf23c"),

    /** A constant used to reference the 'osi' icon. */
    OSI("\uf41a"),

    /** A constant used to reference the 'page4' icon. */
    PAGE4("\uf3d7"),

    /** A constant used to reference the 'pagelines' icon. */
    PAGELINES("\uf18c"),

    /** A constant used to reference the 'palfed' icon. */
    PALFED("\uf3d8"),

    /** A constant used to reference the 'patreon' icon. */
    PATREON("\uf3d9"),

    /** A constant used to reference the 'paypal' icon. */
    PAYPAL("\uf1ed"),

    /** A constant used to reference the 'periscope' icon. */
    PERISCOPE("\uf3da"),

    /** A constant used to reference the 'phabricator' icon. */
    PHABRICATOR("\uf3db"),

    /** A constant used to reference the 'phoenix_framework' icon. */
    PHOENIX_FRAMEWORK("\uf3dc"),

    /** A constant used to reference the 'phoenix_squadron' icon. */
    PHOENIX_SQUADRON("\uf511"),

    /** A constant used to reference the 'php' icon. */
    PHP("\uf457"),

    /** A constant used to reference the 'pied_piper' icon. */
    PIED_PIPER("\uf2ae"),

    /** A constant used to reference the 'pied_piper_alt' icon. */
    PIED_PIPER_ALT("\uf1a8"),

    /** A constant used to reference the 'pied_piper_hat' icon. */
    PIED_PIPER_HAT("\uf4e5"),

    /** A constant used to reference the 'pied_piper_pp' icon. */
    PIED_PIPER_PP("\uf1a7"),

    /** A constant used to reference the 'pinterest' icon. */
    PINTEREST("\uf0d2"),

    /** A constant used to reference the 'pinterest_p' icon. */
    PINTEREST_P("\uf231"),

    /** A constant used to reference the 'pinterest_square' icon. */
    PINTEREST_SQUARE("\uf0d3"),

    /** A constant used to reference the 'playstation' icon. */
    PLAYSTATION("\uf3df"),

    /** A constant used to reference the 'product_hunt' icon. */
    PRODUCT_HUNT("\uf288"),

    /** A constant used to reference the 'pushed' icon. */
    PUSHED("\uf3e1"),

    /** A constant used to reference the 'python' icon. */
    PYTHON("\uf3e2"),

    /** A constant used to reference the 'qq' icon. */
    QQ("\uf1d6"),

    /** A constant used to reference the 'quinscape' icon. */
    QUINSCAPE("\uf459"),

    /** A constant used to reference the 'quora' icon. */
    QUORA("\uf2c4"),

    /** A constant used to reference the 'r_project' icon. */
    R_PROJECT("\uf4f7"),

    /** A constant used to reference the 'ravelry' icon. */
    RAVELRY("\uf2d9"),

    /** A constant used to reference the 'react' icon. */
    REACT("\uf41b"),

    /** A constant used to reference the 'readme' icon. */
    README("\uf4d5"),

    /** A constant used to reference the 'rebel' icon. */
    REBEL("\uf1d0"),

    /** A constant used to reference the 'red_river' icon. */
    RED_RIVER("\uf3e3"),

    /** A constant used to reference the 'reddit' icon. */
    REDDIT("\uf1a1"),

    /** A constant used to reference the 'reddit_alien' icon. */
    REDDIT_ALIEN("\uf281"),

    /** A constant used to reference the 'reddit_square' icon. */
    REDDIT_SQUARE("\uf1a2"),

    /** A constant used to reference the 'rendact' icon. */
    RENDACT("\uf3e4"),

    /** A constant used to reference the 'renren' icon. */
    RENREN("\uf18b"),

    /** A constant used to reference the 'replyd' icon. */
    REPLYD("\uf3e6"),

    /** A constant used to reference the 'researchgate' icon. */
    RESEARCHGATE("\uf4f8"),

    /** A constant used to reference the 'resolving' icon. */
    RESOLVING("\uf3e7"),

    /** A constant used to reference the 'rocketchat' icon. */
    ROCKETCHAT("\uf3e8"),

    /** A constant used to reference the 'rockrms' icon. */
    ROCKRMS("\uf3e9"),

    /** A constant used to reference the 'safari' icon. */
    SAFARI("\uf267"),

    /** A constant used to reference the 'sass' icon. */
    SASS("\uf41e"),

    /** A constant used to reference the 'schlix' icon. */
    SCHLIX("\uf3ea"),

    /** A constant used to reference the 'scribd' icon. */
    SCRIBD("\uf28a"),

    /** A constant used to reference the 'searchengin' icon. */
    SEARCHENGIN("\uf3eb"),

    /** A constant used to reference the 'sellcast' icon. */
    SELLCAST("\uf2da"),

    /** A constant used to reference the 'sellsy' icon. */
    SELLSY("\uf213"),

    /** A constant used to reference the 'servicestack' icon. */
    SERVICESTACK("\uf3ec"),

    /** A constant used to reference the 'shirtsinbulk' icon. */
    SHIRTSINBULK("\uf214"),

    /** A constant used to reference the 'simplybuilt' icon. */
    SIMPLYBUILT("\uf215"),

    /** A constant used to reference the 'sistrix' icon. */
    SISTRIX("\uf3ee"),

    /** A constant used to reference the 'sith' icon. */
    SITH("\uf512"),

    /** A constant used to reference the 'skyatlas' icon. */
    SKYATLAS("\uf216"),

    /** A constant used to reference the 'skype' icon. */
    SKYPE("\uf17e"),

    /** A constant used to reference the 'slack' icon. */
    SLACK("\uf198"),

    /** A constant used to reference the 'slack_hash' icon. */
    SLACK_HASH("\uf3ef"),

    /** A constant used to reference the 'slideshare' icon. */
    SLIDESHARE("\uf1e7"),

    /** A constant used to reference the 'snapchat' icon. */
    SNAPCHAT("\uf2ab"),

    /** A constant used to reference the 'snapchat_ghost' icon. */
    SNAPCHAT_GHOST("\uf2ac"),

    /** A constant used to reference the 'snapchat_square' icon. */
    SNAPCHAT_SQUARE("\uf2ad"),

    /** A constant used to reference the 'soundcloud' icon. */
    SOUNDCLOUD("\uf1be"),

    /** A constant used to reference the 'speakap' icon. */
    SPEAKAP("\uf3f3"),

    /** A constant used to reference the 'spotify' icon. */
    SPOTIFY("\uf1bc"),

    /** A constant used to reference the 'stack_exchange' icon. */
    STACK_EXCHANGE("\uf18d"),

    /** A constant used to reference the 'stack_overflow' icon. */
    STACK_OVERFLOW("\uf16c"),

    /** A constant used to reference the 'staylinked' icon. */
    STAYLINKED("\uf3f5"),

    /** A constant used to reference the 'steam' icon. */
    STEAM("\uf1b6"),

    /** A constant used to reference the 'steam_square' icon. */
    STEAM_SQUARE("\uf1b7"),

    /** A constant used to reference the 'steam_symbol' icon. */
    STEAM_SYMBOL("\uf3f6"),

    /** A constant used to reference the 'sticker_mule' icon. */
    STICKER_MULE("\uf3f7"),

    /** A constant used to reference the 'strava' icon. */
    STRAVA("\uf428"),

    /** A constant used to reference the 'stripe' icon. */
    STRIPE("\uf429"),

    /** A constant used to reference the 'stripe_s' icon. */
    STRIPE_S("\uf42a"),

    /** A constant used to reference the 'studiovinari' icon. */
    STUDIOVINARI("\uf3f8"),

    /** A constant used to reference the 'stumbleupon' icon. */
    STUMBLEUPON("\uf1a4"),

    /** A constant used to reference the 'stumbleupon_circle' icon. */
    STUMBLEUPON_CIRCLE("\uf1a3"),

    /** A constant used to reference the 'superpowers' icon. */
    SUPERPOWERS("\uf2dd"),

    /** A constant used to reference the 'supple' icon. */
    SUPPLE("\uf3f9"),

    /** A constant used to reference the 'teamspeak' icon. */
    TEAMSPEAK("\uf4f9"),

    /** A constant used to reference the 'telegram' icon. */
    TELEGRAM("\uf2c6"),

    /** A constant used to reference the 'telegram_plane' icon. */
    TELEGRAM_PLANE("\uf3fe"),

    /** A constant used to reference the 'tencent_weibo' icon. */
    TENCENT_WEIBO("\uf1d5"),

    /** A constant used to reference the 'themeisle' icon. */
    THEMEISLE("\uf2b2"),

    /** A constant used to reference the 'trade_federation' icon. */
    TRADE_FEDERATION("\uf513"),

    /** A constant used to reference the 'trello' icon. */
    TRELLO("\uf181"),

    /** A constant used to reference the 'tripadvisor' icon. */
    TRIPADVISOR("\uf262"),

    /** A constant used to reference the 'tumblr' icon. */
    TUMBLR("\uf173"),

    /** A constant used to reference the 'tumblr_square' icon. */
    TUMBLR_SQUARE("\uf174"),

    /** A constant used to reference the 'twitch' icon. */
    TWITCH("\uf1e8"),

    /** A constant used to reference the 'twitter' icon. */
    TWITTER("\uf099"),

    /** A constant used to reference the 'twitter_square' icon. */
    TWITTER_SQUARE("\uf081"),

    /** A constant used to reference the 'typo3' icon. */
    TYPO3("\uf42b"),

    /** A constant used to reference the 'uber' icon. */
    UBER("\uf402"),

    /** A constant used to reference the 'uikit' icon. */
    UIKIT("\uf403"),

    /** A constant used to reference the 'uniregistry' icon. */
    UNIREGISTRY("\uf404"),

    /** A constant used to reference the 'untappd' icon. */
    UNTAPPD("\uf405"),

    /** A constant used to reference the 'usb' icon. */
    USB("\uf287"),

    /** A constant used to reference the 'ussunnah' icon. */
    USSUNNAH("\uf407"),

    /** A constant used to reference the 'vaadin' icon. */
    VAADIN("\uf408"),

    /** A constant used to reference the 'viacoin' icon. */
    VIACOIN("\uf237"),

    /** A constant used to reference the 'viadeo' icon. */
    VIADEO("\uf2a9"),

    /** A constant used to reference the 'viadeo_square' icon. */
    VIADEO_SQUARE("\uf2aa"),

    /** A constant used to reference the 'viber' icon. */
    VIBER("\uf409"),

    /** A constant used to reference the 'vimeo' icon. */
    VIMEO("\uf40a"),

    /** A constant used to reference the 'vimeo_square' icon. */
    VIMEO_SQUARE("\uf194"),

    /** A constant used to reference the 'vimeo_v' icon. */
    VIMEO_V("\uf27d"),

    /** A constant used to reference the 'vine' icon. */
    VINE("\uf1ca"),

    /** A constant used to reference the 'vk' icon. */
    VK("\uf189"),

    /** A constant used to reference the 'vnv' icon. */
    VNV("\uf40b"),

    /** A constant used to reference the 'vuejs' icon. */
    VUEJS("\uf41f"),

    /** A constant used to reference the 'weibo' icon. */
    WEIBO("\uf18a"),

    /** A constant used to reference the 'weixin' icon. */
    WEIXIN("\uf1d7"),

    /** A constant used to reference the 'whatsapp' icon. */
    WHATSAPP("\uf232"),

    /** A constant used to reference the 'whatsapp_square' icon. */
    WHATSAPP_SQUARE("\uf40c"),

    /** A constant used to reference the 'whmcs' icon. */
    WHMCS("\uf40d"),

    /** A constant used to reference the 'wikipedia_w' icon. */
    WIKIPEDIA_W("\uf266"),

    /** A constant used to reference the 'windows' icon. */
    WINDOWS("\uf17a"),

    /** A constant used to reference the 'wolf_pack_battalion' icon. */
    WOLF_PACK_BATTALION("\uf514"),

    /** A constant used to reference the 'wordpress' icon. */
    WORDPRESS("\uf19a"),

    /** A constant used to reference the 'wordpress_simple' icon. */
    WORDPRESS_SIMPLE("\uf411"),

    /** A constant used to reference the 'wpbeginner' icon. */
    WPBEGINNER("\uf297"),

    /** A constant used to reference the 'wpexplorer' icon. */
    WPEXPLORER("\uf2de"),

    /** A constant used to reference the 'wpforms' icon. */
    WPFORMS("\uf298"),

    /** A constant used to reference the 'xbox' icon. */
    XBOX("\uf412"),

    /** A constant used to reference the 'xing' icon. */
    XING("\uf168"),

    /** A constant used to reference the 'xing_square' icon. */
    XING_SQUARE("\uf169"),

    /** A constant used to reference the 'y_combinator' icon. */
    Y_COMBINATOR("\uf23b"),

    /** A constant used to reference the 'yahoo' icon. */
    YAHOO("\uf19e"),

    /** A constant used to reference the 'yandex' icon. */
    YANDEX("\uf413"),

    /** A constant used to reference the 'yandex_international' icon. */
    YANDEX_INTERNATIONAL("\uf414"),

    /** A constant used to reference the 'yelp' icon. */
    YELP("\uf1e9"),

    /** A constant used to reference the 'yoast' icon. */
    YOAST("\uf2b1"),

    /** A constant used to reference the 'youtube' icon. */
    YOUTUBE("\uf167"),

    /** A constant used to reference the 'youtube_square' icon. */
    YOUTUBE_SQUARE("\uf431");

    static
    {
        GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(SwingUtilities.FONT_AWESOME_BRANDS_FONT);
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
    private AwesomeIconBrands(String pFontCode)
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
        return SwingUtilities.FONT_AWESOME_BRANDS_FONT;
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
