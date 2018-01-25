package io.opensphere.core.common.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A utility class in which a set of validation and extraction methods are
 * provided to aid in the processing of URIs. The rules implemented in this
 * class are defined in RFC-3986 - Uniform Resource Identifier (URI): Generic
 * Syntax.
 */
public class UrlUtil
{

    /**
     * The regular expression pattern used to match the 'scheme' portion of a
     * URI. Lifted directly from RFC-3986, page 51 (Appendix B). The scheme
     * portion contains the protocol specification (typically "http", "https",
     * "ftp", etc).
     */
    private static final Pattern SCHEME_PATTERN = Pattern.compile("([A-Za-z0-9+-.]+)");

    /**
     * The regular expression pattern used to match the 'authority' portion of a
     * URI. Lifted directly from RFC-3986, page 51 (Appendix B). The authority
     * portion consists of the hostname or IP address, and optionally a username
     * and password.
     */
    private static final Pattern AUTHORITY_PATTERN = Pattern.compile("([^/?#]*)");

    /**
     * The regular expression pattern used to match the 'path' portion of the
     * URI. Lifted directly from RFC-3986, page 51 (Appendix B). The path
     * portion consists of the section of the URI occurring after the authority,
     * but before the query.
     */
    private static final Pattern PATH_PATTERN = Pattern.compile("([^?#]*)");

    /**
     * The regular expression pattern used to match the 'query' portion of the
     * URI. Lifted directly from RFC-3986, page 51 (Appendix B). The query
     * portion consists of the section of the URI occurring after the path, but
     * before the fragment.
     */
    private static final Pattern QUERY_PATTERN = Pattern.compile("([^#]*)");

    /**
     * The regular expression pattern used to match the 'fragment' portion of
     * the URI. Lifted directly from RFC-3986, page 51 (Appendix B). The
     * fragment portion consists of the section of the URI occurring after the
     * query.
     */
    private static final Pattern FRAGMENT_PATTERN = Pattern.compile("(.*)");

    /**
     * The regular expression pattern used to match a complete URL. Lifted
     * directly from RFC-3986, page 51 (Appendix B).
     */
    private static final Pattern FULL_PATTERN = Pattern
            .compile("^(([A-Za-z0-9+-.]+):)?(//([^/?#]*))?([^?#]*)(\\?([^#]*))?(#(.*))?");

    /**
     * The regular expression pattern used to match an absolute URL. Generated
     * using the following assumptions:
     * <ul>
     * <li>The following items are required:
     * <ol>
     * <li>scheme</li>
     * <li>authority (within authority, only the hostname or IP address is
     * required, the username, password and port are optional)</li>
     * </ol>
     * </li>
     * <li>The following items are optional:
     * <ol>
     * <li>path</li>
     * <li>query string</li>
     * <li>fragment_id</li>
     * </ol>
     * </li>
     * </ul>
     */
    private static final Pattern ABSOLUTE_PATTERN = Pattern
            .compile("^([A-Za-z0-9+-.]+)://([^/?# ]+)((:\\d{1,5})?)(/[^?#]*)?(\\?[^#]*)?(#(.*))?$");

    /**
     * Attempts to verify that the supplied String contains a full URI, as
     * defined within RFC-3986 - Uniform Resource Identifier (URI): Generic
     * Syntax.
     *
     * @param pUri the Uniform Resource Identifier to verify.
     * @return true if the supplied URI matches the specified format, false
     *         otherwise.
     */
    public static boolean isValidUri(String pUri)
    {
        if (pUri == null)
        {
            return false;
        }
        return FULL_PATTERN.matcher(pUri).matches();
    }

    /**
     * Attempts to verify that the supplied String contains a full absolute URL.
     * A valid URL is defined using the following:
     * <ul>
     * <li>The following items are required:
     * <ol>
     * <li>scheme</li>
     * <li>authority (within authority, only the hostname or IP address is
     * required, the username, password and port are optional)</li>
     * </ol>
     * </li>
     * <li>The following items are optional:
     * <ol>
     * <li>path</li>
     * <li>query string</li>
     * <li>fragment_id</li>
     * </ol>
     * </li>
     * </ul>
     *
     * @param pUrl the Uniform Resource Identifier to verify.
     * @return true if the supplied URI matches the specified format, false
     *         otherwise.
     */
    public static boolean isValidAbsoluteUrl(String pUrl)
    {
        if (pUrl == null)
        {
            return false;
        }
        return ABSOLUTE_PATTERN.matcher(pUrl).matches();
    }

    /**
     * Attempts to verify that the supplied String consists of or contains a
     * valid scheme, as defined within RFC-3986 - Uniform Resource Identifier
     * (URI): Generic Syntax. The scheme portion contains the protocol
     * specification (typically "http", "https", "ftp", etc). This method will
     * validate if the supplied string consists solely of a scheme, or if the
     * supplied string contains a full URI, in which a scheme is specified.
     *
     * @param pUriScheme the string against which the validation will occur.
     * @return true if the supplied string contains or consists a valid scheme.
     */
    public static boolean isValidScheme(String pUriScheme)
    {
        if (pUriScheme == null)
        {
            return false;
        }
        // split always returns at least one token, therefore the following will
        // work even if no colon is present in the
        // supplied string:
        return SCHEME_PATTERN.matcher(pUriScheme.split(":")[0]).matches();
    }

    /**
     * Attempts to verify that the supplied String consists of or contains a
     * valid authority, as defined within RFC-3986 - Uniform Resource Identifier
     * (URI): Generic Syntax. The authority portion consists of the hostname or
     * IP address, and optionally a username and password. This method will
     * validate if the supplied string consists solely of an authority, or if
     * the supplied string contains a full URI in which case the authority
     * portion will be extracted, and the the extracted portion validated.
     *
     * @param pUriAuthority the String to be validated.
     * @return true if the supplied string contains or consists entirely of a
     *         valid URI authority.
     */
    public static boolean isValidAuthority(String pUriAuthority)
    {
        if (pUriAuthority == null)
        {
            return false;
        }
        String tokenToCompare = pUriAuthority;
        if (tokenToCompare.contains("://"))
        {
            // if the authority is a complete URI and contains a scheme, extract
            // it:
            tokenToCompare = tokenToCompare.split("://")[1];
            // if the remaining portion contains a path, remove it (the split
            // routine will always return at least one
            // token, therefore the following will work even if no delimiter is
            // present in the string):
            tokenToCompare = tokenToCompare.split("/")[0];
        }

        return AUTHORITY_PATTERN.matcher(tokenToCompare).matches();
    }

    /**
     * Attempts to verify that the supplied String consists of or contains a
     * valid path, as defined within RFC-3986 - Uniform Resource Identifier
     * (URI): Generic Syntax. The path portion consists of the section of the
     * URI occurring after the authority, but before the query.
     *
     * @param pUriPath the path to validate.
     * @return true if the supplied String consists of or contains a valid path,
     *         false otherwise.
     */
    public static boolean isValidPath(String pUriPath)
    {
        if (pUriPath == null)
        {
            return false;
        }
        String tokenToCompare = pUriPath;

        // check to determine if the supplied String contains a full URI, and if
        // it does, extract the path portion for
        // comparison.
        if (tokenToCompare.contains("://"))
        {
            tokenToCompare = tokenToCompare.split("://")[1];

            if (tokenToCompare.contains("/"))
            {
                tokenToCompare = tokenToCompare.split("/", 2)[1];
            }
            else
            {
                tokenToCompare = "";
            }

            tokenToCompare = tokenToCompare.split("?")[0];
            tokenToCompare = tokenToCompare.split("#")[0];
        }

        return PATH_PATTERN.matcher(tokenToCompare).matches();
    }

    /**
     * Attempts to verify that the supplied String consists of or contains a
     * valid query, as defined within RFC-3986 - Uniform Resource Identifier
     * (URI): Generic Syntax. The query portion consists of the section of the
     * URI occurring after the path, but before the fragment.
     *
     * @param pUriQuery the string to validate.
     * @return true if the supplied string consists of or contains a valid
     *         query, false otherwise.
     */
    public static boolean isValidQuery(String pUriQuery)
    {
        if (pUriQuery == null)
        {
            return false;
        }

        String tokenToCompare = pUriQuery;

        // check to determine if the supplied String contains a full URI, and if
        // it does, extract the query portion for
        // comparison.
        if (tokenToCompare.contains("://"))
        {
            tokenToCompare = tokenToCompare.split("://")[1];

            if (tokenToCompare.contains("?"))
            {
                tokenToCompare = tokenToCompare.split("?")[1];
                tokenToCompare = tokenToCompare.split("#")[0];
            }
            else
            {
                tokenToCompare = "";
            }
        }

        return QUERY_PATTERN.matcher(tokenToCompare).matches();
    }

    /**
     * Attempts to verify that the supplied String consists of or contains a
     * valid fragment, as defined within RFC-3986 - Uniform Resource Identifier
     * (URI): Generic Syntax. The fragment portion consists of the section of
     * the URI occurring as the last of the 5 defined URI sections.
     *
     * @param pUriFragment the string to validate.
     * @return true if the supplied string consists of or contains a valid
     *         fragment, false otherwise.
     */
    public static boolean isValidFragment(String pUriFragment)
    {
        if (pUriFragment == null)
        {
            return false;
        }
        String tokenToCompare = pUriFragment;

        // check to determine if the supplied String contains a full URI, and if
        // it does, extract the fragment portion
        // for comparison.
        if (tokenToCompare.contains("://"))
        {
            tokenToCompare = tokenToCompare.split("://")[1];

            if (tokenToCompare.contains("#"))
            {
                tokenToCompare = tokenToCompare.split("#")[1];
            }
            else
            {
                tokenToCompare = "";
            }
        }

        return FRAGMENT_PATTERN.matcher(tokenToCompare).matches();
    }

    /**
     * Extracts the scheme section from the supplied full URI.
     *
     * @param pUri the string from which to extract the scheme.
     * @return the scheme extracted from the supplied URI, null if the supplied
     *         string is null, or blank if none could be found.
     */
    public static String extractScheme(String pUri)
    {
        if (pUri == null)
        {
            return null;
        }

        Matcher matcher = FULL_PATTERN.matcher(pUri);

        return matcher.group(2);
    }

    /**
     * Extracts the authority section from the supplied full URI.
     *
     * @param pUri the string from which to extract the authority.
     * @return the authority extracted from the supplied URI, null if the
     *         supplied string is null, or blank if none could be found.
     */
    public static String extractAuthority(String pUri)
    {
        if (pUri == null)
        {
            return null;
        }

        Matcher matcher = FULL_PATTERN.matcher(pUri);

        return matcher.group(4);
    }

    /**
     * Extracts the path section from the supplied full URI.
     *
     * @param pUri the string from which to extract the path.
     * @return the path extracted from the supplied URI, null if the supplied
     *         string is null, or blank if none could be found.
     */
    public static String extractPath(String pUri)
    {
        if (pUri == null)
        {
            return null;
        }

        Matcher matcher = FULL_PATTERN.matcher(pUri);

        return matcher.group(5);
    }

    /**
     * Extracts the query section from the supplied full URI.
     *
     * @param pUri the string from which to extract the query.
     * @return the query extracted from the supplied URI, null if the supplied
     *         string is null, or blank if none could be found.
     */
    public static String extractQuery(String pUri)
    {
        if (pUri == null)
        {
            return null;
        }

        Matcher matcher = FULL_PATTERN.matcher(pUri);

        return matcher.group(7);
    }

    /**
     * Extracts the fragment section from the supplied full URI.
     *
     * @param pUri the string from which to extract the fragment.
     * @return the fragment extracted from the supplied URI, null if the
     *         supplied string is null, or blank if none could be found.
     */
    public static String extractFragment(String pUri)
    {
        if (pUri == null)
        {
            return null;
        }

        Matcher matcher = FULL_PATTERN.matcher(pUri);

        return matcher.group(9);
    }
}
