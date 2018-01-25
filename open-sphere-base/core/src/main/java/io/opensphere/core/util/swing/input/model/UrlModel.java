package io.opensphere.core.util.swing.input.model;

import java.util.Collection;
import java.util.Collections;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

import org.apache.commons.lang3.StringUtils;

import io.opensphere.core.util.CompoundPredicateWithMessage;
import io.opensphere.core.util.ObservableValueValidatorSupport;
import io.opensphere.core.util.PredicateWithMessage;
import io.opensphere.core.util.WrappedPredicateWithMessage;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.core.util.predicate.NotInPredicate;
import io.opensphere.core.util.predicate.ValidURLPredicate;

/**
 * Model for a URL.
 */
public class UrlModel extends TextModel
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /** The disallowed URLs. */
    private Collection<String> myDisallowedUrls;

    /**
     * Constructor.
     */
    public UrlModel()
    {
        setDisallowedUrls(Collections.<String>emptyList());
    }

    @Override
    public DocumentFilter getDocumentFilter()
    {
        return new DocumentFilter()
        {
            @Override
            public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException
            {
                if (StringUtils.isEmpty(string))
                {
                    super.insertString(fb, offset, string, attr);
                }
                else
                {
                    fb.insertString(offset, trimFront(string).replaceAll(" ", "+"), attr);
                }
            }

            @Override
            public void remove(FilterBypass fb, int offset, int length) throws BadLocationException
            {
                String existing = fb.getDocument().getText(0, fb.getDocument().getLength());
                if (offset == 0 && StringUtils.isNotEmpty(existing))
                {
                    String candidate = existing.substring(0, offset) + existing.substring(offset + length, existing.length());
                    String trim = trimFront(candidate);

                    // Any difference in the length of the candidate string and
                    // the trimmed string should also be removed from the front
                    // of the string. For example, attempting to remove the
                    // first 3 characters of "foo bar" should result in removing
                    // the first 4 characters. However, typically there should
                    // not be
                    // spaces in the URL since we substitute "+" instead.
                    super.remove(fb, offset, length + candidate.length() - trim.length());
                }
                else
                {
                    super.remove(fb, offset, length);
                }
            }

            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
                throws BadLocationException
            {
                if (StringUtils.isEmpty(text))
                {
                    super.replace(fb, offset, length, text, attrs);
                }
                else if (offset == 0)
                {
                    // Trim the front before replacing spaces. The length given
                    // to the method is the length being removed, so it does not
                    // need to be adjusted when we trim.
                    fb.replace(offset, length, trimFront(text).replaceAll(" ", "+"), attrs);
                }
                else
                {
                    // just replace space with "+", but do the regular document
                    // replacement.
                    fb.replace(offset, length, text.replaceAll(" ", "+"), attrs);
                }
            }

            /**
             * Trim only the front of the given string. This functions exactly
             * like the trim function of java.lang.String, but the end of the
             * string is not trimmed.
             *
             * @param orig The string to trim.
             * @return The trimmed string if trimming was necessary or the
             *         original (same reference) if no trimming was required.
             */
            private String trimFront(String orig)
            {
                int start = 0;
                while (start < orig.length() && orig.charAt(start) <= ' ')
                {
                    ++start;
                }
                return start > 0 ? orig.substring(start, orig.length()) : orig;
            }
        };
    }

    /**
     * Sets the disallowed URLs.
     *
     * @param disallowedUrls The disallowed URLs
     */
    public final void setDisallowedUrls(Collection<String> disallowedUrls)
    {
        myDisallowedUrls = disallowedUrls;
        setValidator();
    }

    @Override
    public void setRequired(boolean isRequired)
    {
        super.setRequired(isRequired);
        setValidator();
    }

    /**
     * Set the validator based on the current disallowed URLs and required
     * properties.
     */
    private void setValidator()
    {
        Collection<PredicateWithMessage<? super String>> predicates = New.collection(2);
        predicates.add(new WrappedPredicateWithMessage<String>(new ValidURLPredicate())
        {
            @Override
            public String getMessage()
            {
                return StringUtilities.concat(getName(), " is not a valid URL.");
            }

            @Override
            public boolean test(String input)
            {
                return !isRequired() && StringUtils.isEmpty(input) || super.test(input);
            }
        });
        predicates.add(new WrappedPredicateWithMessage<String>(new NotInPredicate(myDisallowedUrls))
        {
            @Override
            public String getMessage()
            {
                return StringUtilities.concat(getName(), " is already in use.");
            }
        });
        PredicateWithMessage<String> predicate = new CompoundPredicateWithMessage<>(predicates);
        setValidatorSupport(new ObservableValueValidatorSupport<String>(this, predicate));
    }
}
