package io.opensphere.core.util.lang;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import io.opensphere.core.util.Utilities;

/**
 * A helper for tokenizing a string based on a string delimiter. This supports
 * the use of text delimiters like quotation marks, parenthesis and brackets.
 * For ease of processing some characters or groups of characters are
 * substituted with unused characters. Unicode characters in the range of E000
 * to F8FF are reserved for private use, so using these will not interfere with
 * any unicode character set.
 */
public class TextDelimitedStringTokenizer implements StringTokenizer
{
    /**
     * Substitute for a close text delimiter which has be identified as a
     * delimiter and not plain text.
     */
    private static final char ourFinalClose = '\uE021';

    /**
     * Substitute for a open text delimiter which has be identified as a
     * delimiter and not plain text.
     */
    private static final char ourFinalOpen = '\uE020';

    /**
     * Substitute for plain text when it matches the close text delimiter, but
     * should not interfere with tokenizing.
     */
    private static final char ourPlainTextClose = '\uE031';

    /**
     * Substitute for plain text when it matches the open text delimiter, but
     * should not interfere with tokenizing.
     */
    private static final char ourPlainTextOpen = '\uE030';

    /**
     * Substitute for the close text delimiter to ensure that it is a single
     * character.
     */
    private static final char ourSingleCharClose = '\uE011';

    /**
     * Substitute for the open text delimiter to ensure that it is a single
     * character.
     */
    private static final char ourSingleCharOpen = '\uE010';

    /**
     * This character is used as a replacement for the actual token delimiter to
     * make processing easier.
     */
    private static final char ourSingleCharTokenDelimiter = '\uE000';

    /**
     * The end of a text delimiter (the position at which normal tokenizing
     * resumes).
     */
    private final String myTextDelimiterClose;

    /**
     * The start of a text delimiter (the position after which tokenizing is
     * suspended). If the start and end match, this will be used for both.
     */
    private final String myTextDelimiterOpen;

    /** The delimiter for tokenizing strings. */
    private final String myTokenDelimiter;

    /** When true the open and close text delimiters match. */
    private final boolean myUsesSingleTextDelimiter;

    /**
     * Constructor.
     *
     * @param tokenDelimiter The delimiter which divides tokens within a string.
     */
    public TextDelimitedStringTokenizer(String tokenDelimiter)
    {
        checkDelimiters(tokenDelimiter, null, null);
        myTokenDelimiter = tokenDelimiter;
        myTextDelimiterOpen = null;
        myTextDelimiterClose = null;
        myUsesSingleTextDelimiter = false;
    }

    /**
     * Constructor.
     *
     * @param tokenDelimiter The delimiter which divides tokens within a string.
     * @param textDelimiter A single text delimiter (like a quotation mark)
     *            causes token delimiters to be considered regular text until
     *            another matching delimiter is reached.
     */
    public TextDelimitedStringTokenizer(String tokenDelimiter, String textDelimiter)
    {
        checkDelimiters(tokenDelimiter, textDelimiter, textDelimiter);
        myTokenDelimiter = tokenDelimiter;
        myTextDelimiterOpen = textDelimiter;
        myTextDelimiterClose = null;
        myUsesSingleTextDelimiter = true;
    }

    /**
     * Constructor.
     *
     * @param tokenDelimiter The delimiter which divides tokens within a string.
     * @param textDelimiterOpen The open text delimiter causes token delimiters
     *            to be considered regular text until a close text delimiter is
     *            reached.
     * @param textDelimiterClose The close text delimiter causes regular
     *            tokenizing to resume. When a close is used, a matching open
     *            must precede it.
     */
    public TextDelimitedStringTokenizer(String tokenDelimiter, String textDelimiterOpen, String textDelimiterClose)
    {
        checkDelimiters(tokenDelimiter, textDelimiterOpen, textDelimiterClose);
        myTokenDelimiter = tokenDelimiter;
        myTextDelimiterOpen = textDelimiterOpen;
        if (textDelimiterOpen.equals(textDelimiterClose))
        {
            myTextDelimiterClose = null;
            myUsesSingleTextDelimiter = true;
        }
        else
        {
            myTextDelimiterClose = textDelimiterClose;
            myUsesSingleTextDelimiter = false;
        }
    }

    /**
     * Tokenize the string. Token delimiters will be excluded from returned
     * tokens, as will text delimiters.
     *
     * @param input The string which is to be tokenized.
     * @return A list of strings divided by the delimiter.
     */
    @Override
    public List<String> tokenize(String input)
    {
        Utilities.checkNull(input, "input");

        // Substitute a single non-printable character for multi-character
        // delimiters to simplify processing
        String replaced = substituteDelimiters(input);

        // Reduce double open and close delimiters
        String reduced = reduceTextDelimiters(replaced);

        char open = ourFinalOpen;
        char close = myUsesSingleTextDelimiter ? ourFinalOpen : ourFinalClose;
        List<String> tokens = new ArrayList<>();
        int tokenStart = 0;
        boolean inTextDelim = false;
        for (int i = 0; i < reduced.length(); ++i)
        {
            char current = reduced.charAt(i);
            if (inTextDelim)
            {
                if (current == close)
                {
                    inTextDelim = false;
                }
            }
            else if (current == open)
            {
                inTextDelim = true;
            }
            else if (current == ourSingleCharTokenDelimiter)
            {
                if (tokenStart != -1)
                {
                    tokens.add(postProcessToken(reduced.substring(tokenStart, i)));
                }
                tokenStart = i + 1;
            }
        }

        if (tokenStart != -1)
        {
            tokens.add(postProcessToken(reduced.substring(tokenStart, reduced.length())));
        }

        return tokens;
    }

    /**
     * Check to make sure that the set of delimiters is valid.
     *
     * @param tokenDelimiter The delimiter which divides tokens.
     * @param textDelimiterOpen The delimiter which begins grouping of text
     *            regardless of token delimiters.
     * @param textDelimiterClose The delimiter which ends grouping of text
     *            regardless of token delimiters.
     */
    private void checkDelimiters(String tokenDelimiter, String textDelimiterOpen, String textDelimiterClose)
    {
        if (StringUtils.isEmpty(tokenDelimiter))
        {
            throw new IllegalArgumentException("A non-empty delimiter must be supplied for tokenizing.");
        }

        if (StringUtils.isEmpty(textDelimiterOpen) ^ StringUtils.isEmpty(textDelimiterClose))
        {
            throw new IllegalArgumentException(
                    "When a text delimiter is used for tokenizing, the open and close must both be supplied.");
        }
    }

    /**
     * For characters which were substituted for processing purposes, restore
     * the original character set.
     *
     * @param input The string to reconstitute.
     * @return The reconstituted string.
     */
    private String postProcessToken(String input)
    {
        String temp1 = input.replace(String.valueOf(ourSingleCharTokenDelimiter), myTokenDelimiter);
        if (myTextDelimiterOpen != null)
        {
            temp1 = temp1.replace(String.valueOf(ourPlainTextOpen), myTextDelimiterOpen);
            temp1 = temp1.replace(new String(new char[] { ourFinalOpen }), "");
            if (!myUsesSingleTextDelimiter)
            {
                temp1 = temp1.replace(String.valueOf(ourPlainTextClose), myTextDelimiterClose);
                temp1 = temp1.replace(new String(new char[] { ourFinalClose }), "");
            }
        }
        return temp1.trim();
    }

    /**
     * Determine which occurrences of the text delimiters are plain text and
     * which will be used to delimit the text. Consecutive occurrences will be
     * paired and considered a single plain text character (for example """"
     * will resolve to "" where both "'s are plain text. """ will also result in
     * "", but the first " will be a delimiter and the second will be plain
     * text).
     *
     * @param input The string on which to perform reduction.
     * @return The reduced string.
     */
    private String reduceTextDelimiters(String input)
    {
        // When we find a token delimiter followed by an open, reduce from the
        // right backwards, making doubles plain text.
        // When we find a token delimiter preceded by a close, reduce from the
        // left forwards, making doubles plain text.
        char[] current = input.toCharArray();

        // Open/close needs to occur next to the token delimiter so reduce those
        // first looking for consecutive ones next to the delimiter.
        for (int i = 0; i < current.length; ++i)
        {
            if (i == 0)
            {
                searchForwards(current, -1);
            }
            else if (i == current.length - 1)
            {
                searchBackwards(current, current.length);
            }
            else if (current[i] == ourSingleCharTokenDelimiter)
            {
                searchBackwards(current, i);
                searchForwards(current, i);
            }
        }

        // replace plain text doubles with singles.
        String replaced = new String(current);
        String reduced = replaced.replace(new String(new char[] { ourPlainTextOpen, ourPlainTextOpen }),
                String.valueOf(ourPlainTextOpen));
        reduced = reduced.replace(new String(new char[] { ourPlainTextClose, ourPlainTextClose }),
                String.valueOf(ourPlainTextClose));

        // Remaining doubles are escaped delimiters, so replace them with plain
        // text singles.
        reduced = reduced.replace(new String(new char[] { ourSingleCharOpen, ourSingleCharOpen }),
                String.valueOf(ourPlainTextOpen));
        reduced = reduced.replace(new String(new char[] { ourSingleCharClose, ourSingleCharClose }),
                String.valueOf(ourPlainTextClose));

        // Finalize any text delimiters which remain.
        reduced = reduced.replace(ourSingleCharOpen, ourFinalOpen);
        return reduced.replace(ourSingleCharClose, ourFinalClose);
    }

    /**
     * A helper method used by
     * {@link TextDelimitedStringTokenizer#reduceTextDelimiters(String)} to
     * search backwards from the index looking for close text delimiters.
     *
     * @param input The string to search.
     * @param index The index at which to begin the search.
     */
    private void searchBackwards(char[] input, int index)
    {
        char delim = myUsesSingleTextDelimiter ? ourSingleCharOpen : ourSingleCharClose;
        char replace = myUsesSingleTextDelimiter ? ourPlainTextOpen : ourPlainTextClose;
        char finalDelim = myUsesSingleTextDelimiter ? ourFinalOpen : ourFinalClose;
        int lastMatch = index;
        while (lastMatch > 1 && input[lastMatch - 1] == delim)
        {
            --lastMatch;
        }

        // This is actually always positive, but just to make sure...
        int span = Math.abs(index - lastMatch);

        // If there are only 2 and they are the only things in the field, then
        // this is an empty field and not an escaped delimiter.
        boolean isEmptyField = myUsesSingleTextDelimiter && span == 2
                && (lastMatch == 0 || input[lastMatch - 1] == ourSingleCharTokenDelimiter);
        if (span > 1)
        {
            if (isEmptyField)
            {
                input[index - 1] = finalDelim;
                input[index - 2] = finalDelim;
            }
            else
            {
                int firstIndex = index - 1;
                if (span % 2 == 1)
                {
                    input[firstIndex--] = finalDelim;
                }
                for (int i = firstIndex; i >= lastMatch; --i)
                {
                    input[i] = replace;
                }
            }
        }
    }

    /**
     * A helper method used by
     * {@link TextDelimitedStringTokenizer#reduceTextDelimiters(String)} to
     * search forwards from the index looking for open text delimiters.
     *
     * @param input The string to search.
     * @param index The index at which to begin the search.
     */
    private void searchForwards(char[] input, int index)
    {
        char delim = ourSingleCharOpen;
        char replace = ourPlainTextOpen;
        char finalDelim = ourFinalOpen;
        int lastMatch = index;
        while (lastMatch < input.length - 2 && input[lastMatch + 1] == delim)
        {
            ++lastMatch;
        }

        // This is actually always positive, but just to make sure...
        int span = Math.abs(lastMatch - index);

        // If there are only 2 and they are the only things in the field, then
        // this is an empty field and not an escaped delimiter.
        boolean isEmptyField = myUsesSingleTextDelimiter && span == 2
                && (lastMatch == input.length - 1 || input[lastMatch + 1] == ourSingleCharTokenDelimiter);
        if (span > 1)
        {
            if (isEmptyField)
            {
                input[index + 1] = finalDelim;
                input[index + 2] = finalDelim;
            }
            else
            {
                int firstIndex = index + 1;
                if (span % 2 == 1)
                {
                    input[firstIndex++] = finalDelim;
                }
                for (int i = firstIndex; i <= lastMatch; ++i)
                {
                    input[i] = replace;
                }
            }
        }
    }

    /**
     * Substitute the delimiters with privately used single characters to make
     * processing easier.
     *
     * @param input The string on which to perform substitutions.
     * @return The substituted string.
     */
    private String substituteDelimiters(String input)
    {
        String temp1 = input.replace(myTokenDelimiter, String.valueOf(ourSingleCharTokenDelimiter));
        // Since we replace the open delimiter first, if the open and the close
        // are the same this replacement will result in only opens.
        if (myTextDelimiterOpen != null)
        {
            String temp2 = temp1.replace(myTextDelimiterOpen, String.valueOf(ourSingleCharOpen));
            if (!myUsesSingleTextDelimiter)
            {
                return temp2.replace(myTextDelimiterClose, String.valueOf(ourSingleCharClose));
            }
            return temp2;
        }
        return temp1;
    }
}
