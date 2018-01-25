package io.opensphere.core.util.predicate;

import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * A predicate that accepts valid URLs.
 */
public class ValidURLPredicate implements Predicate<String>
{
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
    private static boolean isValidAbsoluteUrl(String pUrl)
    {
        return ABSOLUTE_PATTERN.matcher(pUrl).matches();
    }

    @Override
    public boolean test(String input)
    {
        return input != null && isValidAbsoluteUrl(input);
    }
}
