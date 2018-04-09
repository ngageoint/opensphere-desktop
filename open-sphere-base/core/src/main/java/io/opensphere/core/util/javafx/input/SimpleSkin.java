package io.opensphere.core.util.javafx.input;

import javafx.scene.Node;
import javafx.scene.control.Skin;
import javafx.scene.control.Skinnable;

/**
 * A simple skin implementation that wraps a {@link Skinnable} instance and a
 * {@link Node}.
 */
public class SimpleSkin implements Skin<Skinnable>
{
    /**
     * The skinnable instance wrapped by the instance.
     */
    private final Skinnable mySkinnable;

    /**
     * The node instance wrapped by the instance.
     */
    private final Node myNode;

    /**
     * Creates a new skin.
     *
     * @param pSkinnable The skinnable instance wrapped by the instance.
     * @param pNode The node instance wrapped by the instance.
     */
    public SimpleSkin(Skinnable pSkinnable, Node pNode)
    {
        mySkinnable = pSkinnable;
        myNode = pNode;
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
        return myNode;
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
