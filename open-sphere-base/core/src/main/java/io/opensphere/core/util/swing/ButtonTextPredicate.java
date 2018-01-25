package io.opensphere.core.util.swing;

import java.util.Objects;
import java.util.function.Predicate;

import javax.swing.AbstractButton;

/**
 * A predicate that matches buttons with given text.
 *
 * @param <T> The component type.
 */
public class ButtonTextPredicate<T extends AbstractButton> implements Predicate<T>
{
    /** The text to match. */
    private final String myText;

    /**
     * Constructor.
     *
     * @param text The text to match.
     */
    public ButtonTextPredicate(String text)
    {
        myText = text;
    }

    @Override
    public boolean test(T t)
    {
        return Objects.equals(myText, t.getText());
    }
}
