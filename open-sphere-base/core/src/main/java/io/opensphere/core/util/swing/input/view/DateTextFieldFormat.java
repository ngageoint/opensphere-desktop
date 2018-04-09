package io.opensphere.core.util.swing.input.view;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import io.opensphere.core.util.DateTimeFormats;
import io.opensphere.core.util.MathUtil;

/**
 * Format information for a date text field.
 */
public final class DateTextFieldFormat
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(DateTextFieldFormat.class);

    /** The field separators. */
    private static final char[] SEPARATORS = new char[] { '-', '-', ' ', ':', ':' };

    /** The format characters. */
    private static final char[] FORMAT_CHARS = new char[] { 'y', 'M', 'd', 'H', 'm', 's' };

    /** The date/time format. */
    public static final DateTextFieldFormat DATE_TIME = new DateTextFieldFormat(DateTimeFormats.DATE_TIME_FORMAT,
            new Predicate<Matcher>()
            {
                @Override
                public boolean test(Matcher matcher)
                {
                    try
                    {
                        LocalDate.of(Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2)),
                                Integer.parseInt(matcher.group(3)));
                        return MathUtil.between(Integer.parseInt(matcher.group(4)), 0, 23)
                                && MathUtil.between(Integer.parseInt(matcher.group(5)), 0, 59)
                                && MathUtil.between(Integer.parseInt(matcher.group(6)), 0, 59);
                    }
                    catch (RuntimeException e)
                    {
                        LOGGER.debug("Unable to parse format information", e);
                        return false;
                    }
                }
            }, 150);

    /** The date format. */
    public static final DateTextFieldFormat DATE = new DateTextFieldFormat(DateTimeFormats.DATE_FORMAT, new Predicate<Matcher>()
    {
        @Override
        public boolean test(Matcher matcher)
        {
            try
            {
                LocalDate.of(Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2)),
                        Integer.parseInt(matcher.group(3)));
                return true;
            }
            catch (RuntimeException e)
            {
                LOGGER.debug("Unable to parse format information", e);
                return false;
            }
        }
    }, 90);

    /** The month format. */
    public static final DateTextFieldFormat MONTH = new DateTextFieldFormat("yyyy-MM", new Predicate<Matcher>()
    {
        @Override
        public boolean test(Matcher matcher)
        {
            return MathUtil.between(Integer.parseInt(matcher.group(2)), 1, 12);
        }
    }, 67);

    /** The date format. */
    private final SimpleDateFormat myFormat;

    /** The date pattern. */
    private final Pattern myPattern;

    /** The date validator. */
    private final Predicate<Matcher> myValidator;

    /** The text field width. */
    private final int myWidth;

    /**
     * Constructor.
     *
     * @param format the date format
     * @param validator the date validator
     * @param width the width
     */
    private DateTextFieldFormat(String format, Predicate<Matcher> validator, int width)
    {
        myFormat = new SimpleDateFormat(format);
        myPattern = Pattern.compile(dateFormatToPattern(format));
        myValidator = validator;
        myWidth = width;
    }

    /**
     * Gets the format.
     *
     * @return the format
     */
    public SimpleDateFormat getFormat()
    {
        return myFormat;
    }

    /**
     * Gets the width.
     *
     * @return the width
     */
    public int getWidth()
    {
        return myWidth;
    }

    /**
     * Determines if the text is valid.
     *
     * @param text the text
     * @return whether the text is valid
     */
    public boolean isValid(String text)
    {
        Matcher matcher = myPattern.matcher(text);
        return matcher.matches() && myValidator.test(matcher);
    }

    /**
     * Parses the given string.
     *
     * @param text the string
     * @return the parsed Date
     * @throws ParseException if an  exception occurs while parsing
     */
    public Date parse(String text) throws ParseException
    {
        return myFormat.parse(convertToKnownFormat(text));
    }

    /**
     * Converts the given date text into a format that can be understood by
     * myFormat.
     *
     * @param text the date text
     * @return the date text in the known format
     */
    private String convertToKnownFormat(String text)
    {
        StringBuilder builder = new StringBuilder(myFormat.toPattern().length());
        Matcher matcher = myPattern.matcher(text);
        if (matcher.matches())
        {
            for (int group = 1; group <= matcher.groupCount(); ++group)
            {
                if (group != 1)
                {
                    builder.append(SEPARATORS[group - 2]);
                }
                builder.append(matcher.group(group));
            }
        }
        else
        {
            builder.append(text);
        }
        return builder.toString();
    }

    /**
     * Converts a date format into a regular expression pattern.
     *
     * @param format the date format
     * @return the regular expression pattern
     */
    private static String dateFormatToPattern(String format)
    {
        String pattern = format;

        pattern = pattern.replaceAll("\\W", ".*?");
        for (char specialChar : FORMAT_CHARS)
        {
            String regex = new StringBuilder().append(".*?(").append(specialChar).append("+).*").toString();
            Matcher matcher = Pattern.compile(regex).matcher(pattern);
            if (matcher.matches())
            {
                String group1 = matcher.group(1);
                String replacement = new StringBuilder(3).append('(').append(group1.length()).append(')').toString();
                pattern = pattern.replace(group1, replacement);
            }
        }
        pattern = pattern.replace("(", "(\\d{");
        pattern = pattern.replace(")", "})");

        return pattern;
    }
}
