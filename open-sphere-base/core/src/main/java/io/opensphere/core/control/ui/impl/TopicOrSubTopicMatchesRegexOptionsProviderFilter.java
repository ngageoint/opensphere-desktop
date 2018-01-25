package io.opensphere.core.control.ui.impl;

import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import io.opensphere.core.options.OptionsProvider;
import io.opensphere.core.util.Utilities;

/**
 * The Class TopicMatchesRegexOptionsProviderFilter.
 */
public class TopicOrSubTopicMatchesRegexOptionsProviderFilter implements Predicate<OptionsProvider>
{
    /** The Contains string. */
    private final String myRegex;

    /**
     * Instantiates a new topic or sub topic matches regex options provider
     * filter.
     *
     * @param regex the regex
     */
    public TopicOrSubTopicMatchesRegexOptionsProviderFilter(String regex)
    {
        Utilities.checkNull(regex, "regex");
        String tRegex = regex;
        try
        {
            Pattern.compile(tRegex);
        }
        catch (PatternSyntaxException e)
        {
            tRegex = Pattern.quote(tRegex);
        }
        myRegex = tRegex;
    }

    @Override
    public boolean test(OptionsProvider value)
    {
        boolean accept = false;

        if (value != null)
        {
            accept = value.getTopic().matches(myRegex);

            if (!accept)
            {
                Set<OptionsProvider> subTopics = value.getSubTopics();
                if (subTopics != null && !subTopics.isEmpty())
                {
                    for (OptionsProvider p : subTopics)
                    {
                        accept = test(p);
                        if (accept)
                        {
                            break;
                        }
                    }
                }
            }
        }
        return accept;
    }
}
