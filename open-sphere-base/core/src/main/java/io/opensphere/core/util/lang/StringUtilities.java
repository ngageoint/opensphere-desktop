package io.opensphere.core.util.lang;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import io.opensphere.core.util.Constants;
import io.opensphere.core.util.TimingMessageProvider;
import io.opensphere.core.util.Utilities;

/**
 * Generic string utilities.
 */
@SuppressWarnings("PMD.GodClass")
public final class StringUtilities
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(StringUtilities.class);

    /** The default charset. */
    public static final Charset DEFAULT_CHARSET = Charset.forName(System.getProperty("opensphere.charset", "UTF-8"));

    /** The degree symbol. */
    public static final char DEGREE_SYMBOL = '\u00b0';

    /** An empty string. */
    public static final String EMPTY = "";

    /** A constant for the local file separator. */
    public static final String FILE_SEP;

    /** A constant for the local line separator. */
    public static final String LINE_SEP;

    /** Array of characters corresponding with hexadecimal notation. */
    private static final char[] HEX_CHARS = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

    /** The pattern to match any sgml tag. */
    private static final Pattern SGML_PATTERN = Pattern.compile("<[^\\s].*?>");

    static
    {
        String lineSep;
        try
        {
            lineSep = AccessController.doPrivileged((PrivilegedAction<String>)() -> System.getProperty("line.separator"));
        }
        catch (final SecurityException e)
        {
            LOGGER.debug("Security exception encountered while looking up line separator. Defaulting to newline.", e);
            lineSep = "\n";
        }
        LINE_SEP = lineSep;

        String fileSep;
        try
        {
            fileSep = AccessController.doPrivileged((PrivilegedAction<String>)() -> System.getProperty("file.separator"));
        }
        catch (final SecurityException e)
        {
            LOGGER.debug("Security exception encountered while looking up file separator. Defaulting to forward slash.", e);
            fileSep = "/";
        }
        FILE_SEP = fileSep;
    }

    /** Disallow instantiation. */
    private StringUtilities()
    {
        throw new UnsupportedOperationException("Instantiation of utility classes is not permitted.");
    }

    /**
     * Examine the given String and if it is longer than the given number of
     * characters, insert HTML line breaks to produce and return a multi-line
     * version. Does not wrap returned String in HTML tags.
     *
     * @param text The text to format.
     * @param length The approximate number of characters long each line should
     *            be.
     * @return Reformatted String with HTML line breaks added. If length is less
     *         than 1 the original string is returned.
     */
    public static String addHTMLLineBreaks(String text, int length)
    {
        if (text == null || length > text.length() || length <= 0)
        {
            return text;
        }

        final String[] words = text.split(" ");
        final StringBuilder newText = new StringBuilder();

        int currentLength = 0;
        for (int i = 0; i < words.length; i++)
        {
            if (currentLength >= length)
            {
                newText.append("<p>");
                currentLength = 0;
            }
            newText.append(words[i]);
            currentLength += words[i].length();

            // Add space back in after each word (try to prevent beginning of
            // new line being a space. But if there are multiple
            // spaces, then just divide).

            // Check that we don't append a space to the end of original
            // sentence.
            if (i != words.length - 1)
            {
                newText.append(' ');
                currentLength += 1;
            }
        }

        return newText.toString();
    }

    /**
     * Breaks the message into lines with the given length of characters. This
     * method eats only one space between words at the line boundaries.
     *
     * @param msg The string message to format with multiple lines.
     * @param lineLength The approximate length of each line.
     * @return The formatted string with added line breaks.
     */
    public static String addLineBreaks(String msg, int lineLength)
    {
        if (msg == null)
        {
            return null;
        }

        if (lineLength < 1)
        {
            return msg;
        }

        String msgCopy = msg;
        final StringBuilder sb = new StringBuilder();
        while (true)
        {
            int idx = msgCopy.indexOf(' ', lineLength);
            if (msgCopy.length() <= lineLength || idx <= 0)
            {
                sb.append(msgCopy);
                break;
            }
            final String tmp = msgCopy.substring(0, idx);
            sb.append(tmp).append(LINE_SEP);
            msgCopy = msgCopy.substring(++idx, msgCopy.length());
        }
        return sb.toString();
    }

    /**
     * Make the first character in the given string upper-case.
     *
     * @param string The string.
     * @return The capitalized string.
     */
    public static String capitalize(String string)
    {
        final char[] chars = string.toCharArray();
        if (chars.length > 0)
        {
            chars[0] = Character.toTitleCase(chars[0]);
        }
        return new String(chars);
    }

    /**
     * Concatenate an array of strings when the total length is known.
     *
     * @param totalLength The length of the result.
     * @param strings The array of strings.
     *
     * @return The concatenated string.
     */
    private static String concat(int totalLength, String... strings)
    {
        final char[] arr = new char[totalLength];
        int pos = 0;
        for (int index = 0; index < strings.length; ++index)
        {
            if (strings[index] != null)
            {
                strings[index].getChars(0, strings[index].length(), arr, pos);
                pos += strings[index].length();
            }
        }
        return new String(arr);
    }

    /**
     * Convenient and fast method for concatenating the string representations
     * of objects.
     *
     * @param objects Arbitrary number of objects.
     * @return The concatenation of the arguments.
     */
    public static String concat(Object... objects)
    {
        final String[] strings = new String[objects.length];

        // Calculate the needed length
        int totalLength = 0;
        for (int index = 0; index < objects.length; ++index)
        {
            if (objects[index] != null)
            {
                strings[index] = objects[index].toString();
                totalLength += strings[index].length();
            }
        }

        return concat(totalLength, strings);
    }

    /**
     * Convenient and fast method for concatenating strings.
     *
     * @param strings Arbitrary number of strings.
     * @return The concatenation of the arguments.
     */
    public static String concat(String... strings)
    {
        // Calculate the needed length
        int totalLength = 0;
        for (int index = 0; index < strings.length; ++index)
        {
            if (strings[index] != null)
            {
                totalLength += strings[index].length();
            }
        }

        return concat(totalLength, strings);
    }

    /**
     * Convenient and fast method for concatenating the string representations
     * of objects to a string builder.
     *
     * @param sb Input string builder.
     * @param objects Arbitrary number of objects.
     * @return The input string builder.
     */
    public static StringBuilder concat(StringBuilder sb, String... objects)
    {
        for (int index = 0; index < objects.length; ++index)
        {
            if (objects[index] != null)
            {
                sb.append(objects[index]);
            }
        }
        return sb;
    }

    /**
     * Return true if the text contains some sgml tags.
     *
     * @param text The text string to evaluate.
     * @return true if the string contains sgml or html tags
     */
    public static boolean containsHTML(String text)
    {
        return text != null && SGML_PATTERN.matcher(text).find();
    }

    /**
     * Convert control characters (currently just tabs and newlines) to the
     * equivalent HTML tags, and enclose the text in &lt;html&gt; tags.
     *
     * @param text The text to format.
     * @return The HTML formatted string.
     */
    public static String convertToHTML(String text)
    {
        return convertToHTML(text, false);
    }

    /**
     * Convert control characters (currently just tabs and newlines) to the
     * equivalent HTML tags, and enclose the text in &lt;html&gt; tags.
     *
     * @param text The text to format.
     * @param pre Indicates if a &lt;pre&gt; tag should be used.
     * @return The HTML formatted string.
     */
    public static String convertToHTML(String text, boolean pre)
    {
        if (text == null)
        {
            return text;
        }

        final StringBuilder newText = new StringBuilder(text.length());

        newText.append("<html>");
        if (pre)
        {
            newText.append("<pre>");
        }
        newText.append(text.replace("\t", "<t/>").replace("\n", "<br/>").replace("\r", ""));
        if (pre)
        {
            newText.append("</pre>");
        }
        newText.append("</html>");

        return newText.toString();
    }

    /**
     * Convert a string containing ASCII tabs and newlines into an HTML table.
     *
     * @param text The text to format.
     * @return The HTML formatted string.
     */
    public static String convertToHTMLTable(String text)
    {
        if (text == null)
        {
            return text;
        }

        final StringBuilder newText = new StringBuilder(text.length());

        newText.append("<html><table>");
        newText.append(text.replaceAll("([^\t\n\r]+)\t*", "<td>$1</td>").replaceAll("([^\n\r]+)[\n\r]*", "<tr>$1</tr>"));
        newText.append("</table></html>");

        return newText.toString();
    }

    /**
     * Gets the count of the given character in the given String.
     *
     * @param ch the character
     * @param s the String
     * @return the count
     */
    public static int count(char ch, String s)
    {
        int count = 0;
        if (s != null)
        {
            for (int index = 0; index < s.length(); index++)
            {
                if (s.charAt(index) == ch)
                {
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * Gets the number of times a regular expression is matched in a string.
     *
     * @param str The string.
     * @param regex The regular expression.
     * @return The count.
     */
    public static int count(CharSequence str, String regex)
    {
        return count(Pattern.compile(regex).matcher(str));
    }

    /**
     * Gets the number of times a matcher matches its pattern.
     *
     * @param matcher The matcher.
     * @return The count.
     */
    public static int count(Matcher matcher)
    {
        int count = 0;
        while (matcher.find())
        {
            ++count;
        }
        return count;
    }

    /**
     * Cuts a string to the given size, adding ellipses if cut.
     *
     * @param s the string
     * @param length the maximum desired length
     * @return the cut string
     */
    public static String cut(String s, int length)
    {
        if (s == null)
        {
            return null;
        }
        final String end = "...";
        return s.length() <= length ? s : s.substring(0, Math.max(length - end.length(), 0)) + end;
    }

    /**
     * Determine if a String ends with a particular character. This is much
     * faster than calling {@link String#endsWith(String)}.
     *
     * @param str The string to test.
     * @param c The character.
     * @return The result.
     */
    public static boolean endsWith(String str, char c)
    {
        return !StringUtils.isEmpty(str) && str.charAt(str.length() - 1) == c;
    }

    /**
     * Determine if a String ends with any of the given suffixes.
     *
     * @param str The string to test.
     * @param list The list of possible suffixes.
     * @return {@code true} if {@code str} ends with any of the strings in
     *         {@code list}.
     */
    public static boolean endsWith(String str, List<? extends String> list)
    {
        if (str != null && list != null)
        {
            for (int index = 0; index < list.size();)
            {
                if (str.endsWith(list.get(index++)))
                {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Replace properties of the form <code>${name}</code> in the given string
     * with their values.
     *
     * @param input The input string.
     * @param properties The set of properties.
     * @return The expanded string.
     */
    public static String expandProperties(String input, Properties properties)
    {
        if (input == null)
        {
            return null;
        }
        final String beginToken = "${";
        final String endToken = "}";
        final StringBuilder sb = new StringBuilder(input);
        boolean moreTokens = true;
        int position = sb.length();
        while (moreTokens)
        {
            final int beginIndex = sb.lastIndexOf(beginToken, position);
            if (beginIndex == -1)
            {
                moreTokens = false;
            }
            else
            {
                final int endIndex = sb.indexOf(endToken, beginIndex);
                if (endIndex == -1)
                {
                    position = beginIndex - 1;
                }
                else
                {
                    final String key = sb.substring(beginIndex + 2, endIndex);
                    final String value = properties.getProperty(key);
                    if (value != null && !value.equals(sb.substring(beginIndex, endIndex + 1)))
                    {
                        sb.replace(beginIndex, endIndex + 1, value);
                        position = beginIndex + value.length();
                    }
                    else
                    {
                        position = beginIndex - 1;
                    }
                }
            }
        }

        return sb.toString();
    }

    /**
     * Tokenizes the line into parts by splitting on commas, but not commas
     * inside quotation sets.
     *
     * @param line - the line to explode
     * @return the String[] of the tokens
     */
    public static String[] explodeLineOnUnquotedCommas(String line)
    {
        if (line == null)
        {
            return null;
        }
        final List<String> valueList = new ArrayList<>();
        int lastIndex = 0;
        boolean inQuote = false;

        for (int i = 0; i < line.length(); i++)
        {
            if (line.charAt(i) == '"')
            {
                inQuote = !inQuote;
            }

            if (!inQuote)
            {
                if (line.charAt(i) == ',')
                {
                    String sub = line.substring(lastIndex, i);

                    sub = sub.trim();

                    // Remove lead and trail quotes
                    if (sub.length() > 1 && sub.charAt(0) == '"' && sub.charAt(sub.length() - 1) == '"')
                    {
                        sub = sub.substring(1, sub.length() - 1);
                    }

                    valueList.add(sub.trim());
                    lastIndex = i + 1;
                }
                if (i == line.length() - 1 && lastIndex < line.length())
                {
                    final String sub = line.substring(lastIndex);
                    valueList.add(sub.trim());
                }
            }
        }

        return !valueList.isEmpty() ? valueList.toArray(new String[valueList.size()]) : null;
    }

    /**
     * Formats the Object as a String.
     *
     * @param o the object
     * @return the formatted String
     */
    public static String format(Object o)
    {
        String s;
        if (o instanceof Number)
        {
            NumberFormat format = NumberFormat.getInstance();
            format.setGroupingUsed(false);
            format.setMaximumFractionDigits(9);
            s = format.format(o);
        }
        else
        {
            s = o.toString();
        }
        return s;
    }

    /**
     * Format a message that reports a timing outcome.
     *
     * @param msg The beginning of the message.
     * @param nanoseconds The number of nanoseconds to report.
     * @return The formatted message.
     */
    public static String formatTimingMessage(String msg, long nanoseconds)
    {
        final StringBuilder sb = new StringBuilder(128).append(msg);
        final double log = Math.log10(nanoseconds);
        if (log < 3)
        {
            sb.append(nanoseconds).append(" ns");
        }
        else if (log < 6)
        {
            final String val = String.format("%.2f",
                    Double.valueOf((double)nanoseconds * Constants.MICRO_PER_UNIT / Constants.NANO_PER_UNIT));
            sb.append(val).append(" μs");
        }
        else if (log < 7)
        {
            final String val = String.format("%.2f",
                    Double.valueOf((double)nanoseconds * Constants.MILLI_PER_UNIT / Constants.NANO_PER_UNIT));
            sb.append(val).append(" ms");
        }
        else
        {
            final String val = String.format("%.4f", Double.valueOf((double)nanoseconds / Constants.NANO_PER_UNIT));
            sb.append(val).append(" s");
        }
        return sb.toString();
    }

    /**
     * Format a message that reports a timing outcome.
     *
     * @param provider The timing message provider.
     * @param nanoseconds The number of nanoseconds to report.
     * @return The formatted message.
     */
    public static String formatTimingMessage(TimingMessageProvider provider, long nanoseconds)
    {
        return formatTimingMessage(provider.getTimingMessage(), nanoseconds);
    }

    /**
     * Generate a string representation of the thread's stack.
     *
     * @param threadName The name of the thread for which the stack is desired.
     * @return a string representation of the thread's stack.
     */
    public static String generateStackString(String threadName)
    {
        final List<Thread> activeThreads = ThreadUtilities.getActiveThreads();
        for (final Thread thread : activeThreads)
        {
            if (thread.getName().equals(threadName))
            {
                return generateStackString(thread);
            }
        }

        return "";
    }

    /**
     * Generate a string representation of a stack trace.
     *
     * @param threadName The name of the thread.
     * @param trace The stack trace.
     *
     * @return a string representation of the thread's stack.
     */
    public static String generateStackString(String threadName, StackTraceElement[] trace)
    {
        final int capacity = trace.length * 64;
        final StringBuilder sb = new StringBuilder(capacity);
        sb.append("Generated stack trace for thread \"").append(threadName).append("\":\n");
        for (int i = 0; i < trace.length;)
        {
            sb.append("\tat ").append(trace[i++]).append('\n');
        }
        return sb.toString();
    }

    /**
     * Generate a string representation of the thread's stack.
     *
     * @param thread The thread for which the stack is desired.
     * @return a string representation of the thread's stack.
     */
    public static String generateStackString(Thread thread)
    {
        return generateStackString(thread.getName(), thread.getStackTrace());
    }

    /**
     * Get the properties from the input set whose keys start with a prefix. The
     * keys are returned with the prefix removed.
     *
     * @param input The input properties.
     * @param prefix The prefix.
     * @return The subset.
     */
    public static Map<String, String> getSubProperties(Properties input, String prefix)
    {
        final Map<String, String> result = new HashMap<>();
        for (final Entry<Object, Object> entry : input.entrySet())
        {
            final String key = (String)entry.getKey();
            if (key.length() > prefix.length() && key.startsWith(prefix))
            {
                result.put(key.substring(prefix.length()), (String)entry.getValue());
            }
        }
        return result;
    }

    /**
     * Get the properties from the input set whose keys start with a prefix. The
     * keys are returned with the prefix removed. The values are converted to
     * the specified type. The type must have a static <code>valueOf</code>
     * method that takes a string.
     *
     * @param <T> The type of the values expected in the return map.
     * @param input The input properties.
     * @param prefix The prefix.
     * @param type The type of the values expected in the return map.
     * @return The subset.
     */
    public static <T> Map<String, T> getSubProperties(Properties input, String prefix, Class<T> type)
    {
        Method valueOf;
        try
        {
            valueOf = type.getMethod("valueOf", String.class);
            final Map<String, String> props = getSubProperties(input, prefix);
            final Map<String, T> result = new HashMap<>();
            for (final Entry<String, String> entry : props.entrySet())
            {
                @SuppressWarnings("unchecked")
                final
                T value = (T)valueOf.invoke(null, entry.getValue());
                result.put(entry.getKey(), value);
            }
            return result;
        }
        catch (final SecurityException e)
        {
            throw new IllegalArgumentException("Cannot access valueOf method for type [" + type + "]: " + e, e);
        }
        catch (final NoSuchMethodException e)
        {
            throw new IllegalArgumentException("Cannot find valueOf method for type [" + type + "]: " + e, e);
        }
        catch (final IllegalArgumentException e)
        {
            throw new IllegalArgumentException("Cannot invoke valueOf method for type [" + type + "]:" + e, e);
        }
        catch (final IllegalAccessException e)
        {
            throw new IllegalArgumentException("Cannot invoke valueOf method for type [" + type + "]:" + e, e);
        }
        catch (final InvocationTargetException e)
        {
            LOGGER.debug("Invocation target exception encountered, rethrowing nested cause.", e);
            throw (RuntimeException)e.getCause();
        }
    }

    /**
     * Get a name that starts with the given prefix but does not already exist
     * in the given collection of names.
     *
     * @param prefix The prefix.
     * @param disallowedNames The disallowed names.
     *
     * @return The unique name.
     */
    public static String getUniqueName(String prefix, Collection<? extends String> disallowedNames)
    {
        return getUniqueName(prefix, disallowedNames, EMPTY);
    }

    /**
     * Get a name that starts with the given prefix but does not already exist
     * in the given collection of names. To get a unique name, a number is added
     * to the end of the prefix, followed by {@code suffix}.
     *
     * @param prefix The prefix.
     * @param disallowedNames The disallowed names.
     * @param suffix A suffix to put after the index.
     * @return The unique name.
     */
    public static String getUniqueName(String prefix, Collection<? extends String> disallowedNames, String suffix)
    {
        int resultIndex = 1;
        final Pattern pattern = Pattern.compile(concat("\\Q", prefix, "\\E(\\d+)\\Q", suffix, "\\E"));
        for (final String name : disallowedNames)
        {
            final Matcher matcher = pattern.matcher(name);
            if (matcher.matches())
            {
                resultIndex = Math.max(resultIndex, Integer.parseInt(matcher.group(1)) + 1);
            }
        }

        return concat(prefix, Integer.toString(resultIndex), suffix);
    }

    /**
     * Tests if a String is an integer. Based off
     * {@link java.lang.Integer#parseInt(String, int)}.
     *
     * @param s The string
     * @return true if the String is an integer
     */
    public static boolean isInteger(String s)
    {
        if (s == null)
        {
            return false;
        }

        final int radix = 10;
        int result = 0;
        int i = 0;
        final int len = s.length();
        int limit = -Integer.MAX_VALUE;
        int multmin;
        int digit;

        if (len > 0)
        {
            final char firstChar = s.charAt(0);
            if (firstChar < '0')
            {
                // Possible leading "+" or "-"
                if (firstChar == '-')
                {
                    limit = Integer.MIN_VALUE;
                }
                else if (firstChar != '+')
                {
                    return false;
                }

                if (len == 1)
                {
                    return false;
                }
                i++;
            }
            multmin = limit / radix;
            while (i < len)
            {
                // Accumulating negatively avoids surprises near MAX_VALUE
                digit = Character.digit(s.charAt(i++), radix);
                if (digit < 0)
                {
                    return false;
                }
                if (result < multmin)
                {
                    return false;
                }
                result *= radix;
                if (result < limit + digit)
                {
                    return false;
                }
                result -= digit;
            }
        }
        else
        {
            return false;
        }
        return true;
    }

    /**
     * Joins each value of the object collection ( using
     * {@link Object#toString()} ) together separated by a separator.
     *
     * @param separator The separator.
     * @param objects The objects to join.
     *
     * @return The joined string.
     */
    public static String join(String separator, Collection<? extends Object> objects)
    {
        String s;
        if (objects == null || objects.isEmpty())
        {
            s = EMPTY;
        }
        else if (objects.size() == 1)
        {
            s = objects.iterator().next().toString();
        }
        else
        {
            s = join(new StringBuilder(), separator, objects).toString();
        }
        return s;
    }

    /**
     * Joins each value of the int array ( using {@link Object#toString()} )
     * together separated by a separator.
     *
     * @param separator The separator.
     * @param objects The ints to join.
     *
     * @return The joined string.
     */
    public static String join(String separator, int... objects)
    {
        if (objects == null)
        {
            return EMPTY;
        }
        final StringBuilder sb = new StringBuilder();
        return join(sb, separator, objects).toString();
    }

    /**
     * Joins each value of the object array ( using {@link Object#toString()} )
     * together separated by a separator.
     *
     * @param separator The separator.
     * @param objects The objects to join.
     *
     * @return The joined string.
     */
    public static String join(String separator, Object... objects)
    {
        return objects == null ? EMPTY : join(new StringBuilder(), separator, objects).toString();
    }

    /**
     * Joins each value of the object array ( using {@link Object#toString()} )
     * together separated by a separator.
     *
     * @param separator The separator.
     * @param objects The objects to join.
     *
     * @return The joined string.
     */
    public static String join(String separator, String... objects)
    {
        return join(separator, (Object[])objects);
    }

    /**
     * Joins each value of the object collection ( using
     * {@link Object#toString()} ) together separated by a separator. Use the
     * provided string builder.
     *
     * @param sb The string builder.
     * @param separator The separator.
     * @param objects The objects to join.
     *
     * @return The string builder.
     */
    public static StringBuilder join(StringBuilder sb, String separator, Collection<? extends Object> objects)
    {
        if (objects != null && !objects.isEmpty())
        {
            for (final Object obj : objects)
            {
                if (obj != null)
                {
                    sb.append(obj);
                }
                sb.append(separator);
            }
            sb.setLength(sb.length() - separator.length());
        }
        return sb;
    }

    /**
     * Joins each int together separated by a separator. Use the provided string
     * builder.
     *
     * @param sb The string builder.
     * @param separator The separator.
     * @param objects The ints to join.
     *
     * @return The string builder.
     */
    public static StringBuilder join(StringBuilder sb, String separator, int... objects)
    {
        if (objects != null && objects.length > 0)
        {
            for (final int obj : objects)
            {
                sb.append(obj).append(separator);
            }
            sb.setLength(sb.length() - separator.length());
        }
        return sb;
    }

    /**
     * Joins each value of the object collection ( using
     * {@link Object#toString()} ) together separated by a separator. Use the
     * provided string builder.
     *
     * @param sb The string builder.
     * @param separator The separator.
     * @param objects The objects to join.
     *
     * @return The string builder.
     */
    public static StringBuilder join(StringBuilder sb, String separator, Object... objects)
    {
        if (objects != null && objects.length > 0)
        {
            for (int i = 0; i < objects.length;)
            {
                final Object obj = objects[i++];
                if (obj != null)
                {
                    sb.append(obj);
                }
                sb.append(separator);
            }
            sb.setLength(sb.length() - separator.length());
        }
        return sb;
    }

    /**
     * Joins each value of the object collection ( using
     * {@link Object#toString()} ) together separated by a separator. Use the
     * provided string builder.
     *
     * @param sb The string builder.
     * @param separator The separator.
     * @param objects The objects to join.
     *
     * @return The string builder.
     */
    public static StringBuilder join(StringBuilder sb, String separator, String... objects)
    {
        return join(sb, separator, (Object[])objects);
    }

    /**
     * Returns an empty string if the argument is null, otherwise returns the
     * argument as is.
     *
     * @param str The input string
     * @return The non-null string
     */
    public static String nonNull(String str)
    {
        return str == null ? EMPTY : str;
    }

    /**
     * Pads the end of the string with spaces to make it the desired size.
     *
     * @param str The input string
     * @param desiredLength The desired length
     * @return the padded string
     */
    public static String pad(String str, int desiredLength)
    {
        final StringBuilder builder = new StringBuilder(desiredLength);
        if (str != null)
        {
            builder.append(str);
        }
        final int padCount = str == null ? desiredLength : desiredLength - str.length();
        for (int i = 0; i < padCount; i++)
        {
            builder.append(' ');
        }
        return builder.toString();
    }

    /**
     * Returns a String with all sgml or html tags removed.
     *
     * @param text The text string to evaluate.
     * @return The String with all sgml or html tags removed
     */
    public static String removeHTML(String text)
    {
        return text != null ? SGML_PATTERN.matcher(text).replaceAll("").replace("&nbsp;", " ") : null;
    }

//    /**
//     * Returns a String with all sgml or html tags removed.
//     *
//     * @param htmlText The text string to evaluate.
//     * @return The String with all sgml or html tags removed
//     */
//    public static String removeHTML(String htmlText)
//    {
//        if (htmlText == null)
//        {
//            return null;
//        }
//
//        String text;
//        Html2Text parser = new Html2Text();
//        try
//        {
//            parser.parse(htmlText);
//            text = parser.getText();
//        }
//        catch (IOException e)
//        {
//            text = htmlText;
//        }
//        return text;
//    }

    /**
     * Repeat a string some number of times.
     *
     * @param str The string to repeat.
     * @param times The number of times.
     * @return The result string.
     */
    public static String repeat(String str, int times)
    {
        final StringBuilder sb = new StringBuilder(str.length() * times);
        return repeat(str, times, sb).toString();
    }

    /**
     * Append a string to a string builder some number of times.
     *
     * @param str The string to repeat.
     * @param times The number of times.
     * @param sb The string builder.
     * @return The string builder.
     */
    public static StringBuilder repeat(String str, int times, StringBuilder sb)
    {
        for (int i = 0; i < times; ++i)
        {
            sb.append(str);
        }
        return sb;
    }

    /**
     * Removes the prefix from the string if it starts with the prefix.
     *
     * @param s the string
     * @param prefix the prefix
     * @return the new string
     */
    public static String removePrefix(String s, String prefix)
    {
        if (s.startsWith(prefix))
        {
            return s.substring(prefix.length());
        }
        return s;
    }

    /**
     * Removes the suffix from the string if it ends with the prefix.
     *
     * @param s the string
     * @param suffix the suffix
     * @return the new string
     */
    public static String removeSuffix(String s, String suffix)
    {
        if (s.endsWith(suffix))
        {
            return s.substring(0, s.length() - suffix.length());
        }
        return s;
    }

    /**
     * Gets the last substring when the string is split with a given delimiting
     * character.
     *
     * @param string the string
     * @param delimiter the delimiter character
     * @return the substring
     */
    public static String getLastSubstring(String string, String delimiter)
    {
        String last = string;
        final int lastChar = string.lastIndexOf(delimiter);
        if (lastChar != -1)
        {
            last = string.substring(lastChar + 1, string.length());
        }
        return last;
    }

    /**
     * Replaces the character at the given index with the replacement.
     *
     * @param string The string.
     * @param index The index to replace
     * @param replacement The replacement character
     * @return The string with replacement.
     */
    public static String replace(String string, int index, char replacement)
    {
        final char[] chars = string.toCharArray();
        if (0 <= index && index < chars.length)
        {
            chars[index] = replacement;
        }
        return new String(chars);
    }

    /**
     * Replaces non alpha-numeric characters with underscores.
     *
     * @param string The string.
     * @return The string with replacement.
     */
    public static String replaceSpecialCharacters(String string)
    {
        return string != null ? trim(string.replaceAll("[^A-z0-9\\-]+", "_"), '_') : null;
    }

    /**
     * Returns an empty string if the argument is null, otherwise returns the
     * trimmed argument.
     *
     * @param str The input string
     * @return The non-null trimmed string
     */
    public static String safeTrim(String str)
    {
        return str == null ? EMPTY : str.trim();
    }

    /**
     * Join some strings together, ensuring that they are separated by slashes
     * but without adding extra slashes.
     *
     * @param strings The strings.
     * @return The joined string.
     */
    public static String slashJoin(String... strings)
    {
        final StringBuilder sb = new StringBuilder();
        for (final String str : strings)
        {
            if (!str.isEmpty())
            {
                if (sb.length() > 0 && sb.charAt(sb.length() - 1) != '/' && str.charAt(0) != '/')
                {
                    sb.append('/');
                }
                sb.append(str);
            }
        }
        return sb.toString();
    }

    /**
     * Convert the stack trace from an exception to a string.
     *
     * @param e the e
     * @return the string
     */
    public static String stackTraceToString(Exception e)
    {
        final StringWriter writer = new StringWriter();
        final PrintWriter pw = new PrintWriter(writer);
        e.printStackTrace(pw);
        return writer.toString();
    }

    /**
     * Determine if a String starts with a particular character. This is much
     * faster than calling {@link String#startsWith(String)}.
     *
     * @param str The string to test.
     * @param c The character.
     * @return The result.
     */
    public static boolean startsWith(String str, char c)
    {
        return !StringUtils.isEmpty(str) && str.charAt(0) == c;
    }

    /**
     * Create a hexadecimal string representation of a byte array.
     *
     * @param arr The byte array.
     * @param byteSeparator Optional separator to put between bytes.
     * @return The hexadecimal string.
     */
    public static String toHexString(byte[] arr, String byteSeparator)
    {
        final int len = Utilities.checkNull(arr, "arr").length * 2
                + (byteSeparator == null || arr.length <= 1 ? 0 : byteSeparator.length() * (arr.length - 1));
        final StringBuilder sb = new StringBuilder(len);
        if (byteSeparator == null || arr.length <= 1)
        {
            for (int index = 0; index < arr.length; ++index)
            {
                final byte b = arr[index];
                sb.append(HEX_CHARS[(b & 0xF0) >> 4]);
                sb.append(HEX_CHARS[b & 0xF]);
            }
        }
        else
        {
            for (int index = 0; index < arr.length - 1; ++index)
            {
                final byte b = arr[index];
                sb.append(HEX_CHARS[(b & 0xF0) >> 4]);
                sb.append(HEX_CHARS[b & 0xF]);
                sb.append(byteSeparator);
            }
            final byte b = arr[arr.length - 1];
            sb.append(HEX_CHARS[(b & 0xF0) >> 4]);
            sb.append(HEX_CHARS[b & 0xF]);
        }

        return sb.toString();
    }

    /**
     * Converts an object to string.
     *
     * @param value the object
     * @return the string
     */
    public static String toString(Object value)
    {
        return toString(value, null);
    }

    /**
     * Converts an object to string.
     *
     * @param value the object
     * @param defaultValue the default value
     * @return the string
     */
    public static String toString(Object value, String defaultValue)
    {
        return value != null ? value.toString() : defaultValue;
    }

    /**
     * Null-safe version of String.trim().
     *
     * @param input The string to be trimmed.
     * @return The trimmed string.
     */
    public static String trim(String input)
    {
        return input == null ? null : input.trim();
    }

    /**
     * Trim a string, removing characters from the end of the string.
     *
     * @param input The string to be trimmed.
     * @param trimmableChars The trimmable characters.
     *
     * @return The trimmed string.
     */
    public static String trim(String input, char... trimmableChars)
    {
        int index = input.length() - 1;
        while (index >= 0)
        {
            final char inputChar = input.charAt(index);

            boolean found = false;
            for (final char trimmable : trimmableChars)
            {
                if (inputChar == trimmable)
                {
                    found = true;
                    break;
                }
            }
            if (!found)
            {
                break;
            }

            --index;
        }

        if (index + 1 < input.length())
        {
            return input.substring(0, index + 1);
        }
        return input;
    }

    /**
     * Trim a string, removing characters from the beginning and end of the
     * string.
     *
     * @param input The string to be trimmed.
     * @param trimmableChar The trimmable character.
     * @return The trimmed string.
     */
    public static String trimBoth(String input, char trimmableChar)
    {
        if (input == null)
        {
            return null;
        }

        int len = input.length();
        int st = 0;
        char[] val = input.toCharArray();

        while (st < len && val[st] == trimmableChar)
        {
            st++;
        }
        while (st < len && val[len - 1] == trimmableChar)
        {
            len--;
        }
        return st > 0 || len < input.length() ? input.substring(st, len) : input;
    }

    /**
     * Replace escape characters in string with human readable originals.
     *
     * @param str The string to convert.
     * @return The updated string.
     */
    public static String unEscapeString(String str)
    {
        final StringBuilder result = new StringBuilder(str.length());
        int index = str.indexOf('&');
        int lastIndex = 0;
        while (index++ >= 0)
        {
            if (str.regionMatches(true, index, "lt;", 0, 3))
            {
                result.append('<');
                lastIndex = index + 3;
            }
            else if (str.regionMatches(true, index, "gt;", 0, 3))
            {
                result.append('>');
                lastIndex = index + 3;
            }
            else if (str.regionMatches(true, index, "quot;", 0, 5))
            {
                result.append('"');
                lastIndex = index + 5;
            }
            else if (str.regionMatches(true, index, "amp;", 0, 4))
            {
                result.append('&');
                lastIndex = index + 4;
            }
            else if (str.regionMatches(true, index, "apos;", 0, 5))
            {
                result.append('\'');
                lastIndex = index + 5;
            }
            else
            {
                result.append('&');
                lastIndex = index;
            }
            index = str.indexOf('&', lastIndex);

            if (index > lastIndex)
            {
                result.append(str, lastIndex, index);
            }
        }
        if (lastIndex < str.length())
        {
            result.append(str, lastIndex, str.length());
        }

        return result.toString();
    }
}
