package io.opensphere.core.util;

import java.util.function.Predicate;

/**
 * Implementation of {@linkplain PredicateWithMessage} that takes a
 * {@link Predicate} and two messages and returns one message when the predicate
 * returns {@code true} and the other message when the predicate returns
 * {@code false}.
 *
 * @param <T> The input for the predicate.
 */
public class DefaultPredicateWithMessage<T> extends WrappedPredicateWithMessage<T>
{
    /** The message when the test returns false. */
    private final String myFalseMessage;

    /** The last message. */
    private volatile String myMessage;

    /** The message when the test returns true. */
    private final String myTrueMessage;

    /**
     * Constructor.
     *
     * @param predicate The wrapped predicate.
     * @param trueMessage The message when the test is true.
     * @param falseMessage The message when the test is false.
     */
    public DefaultPredicateWithMessage(Predicate<? super T> predicate, String trueMessage, String falseMessage)
    {
        super(predicate);
        myTrueMessage = trueMessage;
        myFalseMessage = falseMessage;
    }

    @Override
    public String getMessage()
    {
        return myMessage;
    }

    @Override
    public boolean test(T input)
    {
        boolean result = super.test(input);
        myMessage = result ? myTrueMessage : myFalseMessage;
        return result;
    }
}
