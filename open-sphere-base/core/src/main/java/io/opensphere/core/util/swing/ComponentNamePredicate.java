package io.opensphere.core.util.swing;

import java.awt.Component;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * A predicate that matches components with a given name.
 *
 * @param <T> The component type.
 */
public class ComponentNamePredicate<T extends Component> implements Predicate<T>
{
    /** The name to match. */
    private final String myName;

    /**
     * Constructor.
     *
     * @param name The name to match.
     */
    public ComponentNamePredicate(String name)
    {
        myName = name;
    }

    @Override
    public boolean test(T t)
    {
        return Objects.equals(myName, t.getName());
    }
}
