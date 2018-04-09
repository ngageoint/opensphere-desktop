package io.opensphere.core.util.javafx.input.view;

import javafx.scene.Node;
import javafx.scene.control.Skin;
import javafx.scene.control.Skinnable;

/**
 * A generic skin used for popup content.
 *
 * @param <T> The skinnable type of the item from which the popup was launched.
 */
public class PopupSkin<T extends Skinnable> implements Skin<Skinnable>
{
    /**
     * the {@link Skinnable} item from which the popup was created.
     */
    private final T mySkinnable;

    /**
     * the content to use as the node of the skin.
     */
    private final Node myContent;

    /**
     * Creates a new generic popup skin.
     *
     * @param skinnable the {@link Skinnable} item from which the popup was
     *            created.
     * @param content the content to use as the node of the skin.
     */
    public PopupSkin(T skinnable, Node content)
    {
        mySkinnable = skinnable;
        myContent = content;
    }

    /**
     * {@inheritDoc}
     *
     * @see javafx.scene.control.Skin#getSkinnable()
     */
    @Override
    public Skinnable getSkinnable()
    {
        return mySkinnable;
    }

    /**
     * {@inheritDoc}
     *
     * @see javafx.scene.control.Skin#getNode()
     */
    @Override
    public Node getNode()
    {
        return myContent;
    }

    /**
     * {@inheritDoc}
     *
     * @see javafx.scene.control.Skin#dispose()
     */
    @Override
    public void dispose()
    {
        /* intentionally blank */
    }
}
