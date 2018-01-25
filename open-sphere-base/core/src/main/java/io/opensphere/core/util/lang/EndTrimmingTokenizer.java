package io.opensphere.core.util.lang;

import java.util.List;
import java.util.function.Predicate;

import io.opensphere.core.util.Utilities;

/**
 * A tokenizer that trims the strings at the end of the tokenized result.
 */
public class EndTrimmingTokenizer implements StringTokenizer
{
    /**
     * The predicate used to trim elements from the end of the result lists.
     */
    private final Predicate<String> myPredicate;

    /** The wrapped tokenizer. */
    private final StringTokenizer myTokenizer;

    /**
     * Constructor.
     *
     * @param predicate The predicate used to trim elements from the end of the
     *            result lists.
     * @param tokenizer The wrapped tokenizer.
     */
    public EndTrimmingTokenizer(Predicate<String> predicate, StringTokenizer tokenizer)
    {
        myTokenizer = Utilities.checkNull(tokenizer, "tokenizer");
        myPredicate = Utilities.checkNull(predicate, "predicate");
    }

    @Override
    public List<String> tokenize(String input)
    {
        List<String> result = myTokenizer.tokenize(input);
        while (!result.isEmpty() && myPredicate.test(result.get(result.size() - 1)))
        {
            result.remove(result.size() - 1);
        }
        return result;
    }
}
