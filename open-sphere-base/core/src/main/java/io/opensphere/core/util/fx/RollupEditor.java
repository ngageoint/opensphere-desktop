package io.opensphere.core.util.fx;

import java.util.ArrayList;
import java.util.Collection;

import io.opensphere.core.util.RollupValidator;
import io.opensphere.core.util.ValidatorSupport;
import io.opensphere.core.util.collections.New;

/**
 * Roll-up editor.
 *
 * @param <T> the type of the editor
 */
public class RollupEditor<T extends Editor> implements Editor
{
    /** The child editors. */
    private final Collection<T> myChildren = new ArrayList<>();

    /** The roll-up validator. */
    private final RollupValidator myValidator = new RollupValidator(this);

    @Override
    public ValidatorSupport getValidatorSupport()
    {
        return myValidator;
    }

    @Override
    public void accept()
    {
        for (Editor editor : myChildren)
        {
            editor.accept();
        }
    }

    /**
     * Add a child editor.
     *
     * @param child The child editor.
     */
    public void addChildEditor(T child)
    {
        myChildren.add(child);
        myValidator.addChildValidator(child.getValidatorSupport());
    }

    /**
     * Removes a child editor.
     *
     * @param child The child editor.
     */
    public void removeChildEditor(T child)
    {
        myChildren.remove(child);
        myValidator.removeChildValidator(child.getValidatorSupport());
    }

    /**
     * Removes all a child editors.
     */
    public void clear()
    {
        myChildren.clear();
        myValidator.clear();
    }

    /**
     * Gets the children.
     *
     * @return the children
     */
    public Collection<T> getChildren()
    {
        return New.list(myChildren);
    }
}
