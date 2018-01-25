package io.opensphere.core.util.javafx.input.view;

import javafx.scene.Node;
import javafx.scene.control.Skin;
import javafx.scene.control.Skinnable;

/**
 * A skin in which a combined picker's content is rendered.
 */
public class CombinedDateTimePickerSkin implements Skin<Skinnable>
{
    /**
     * The date picker to be skinned.
     */
    private final CombinedDateTimePicker mySkinnable;

    /**
     * The node in which components are rendered.
     */
    private final Node myNode;

    /**
     * Creates a new skin, wrapping the supplied date picker and component.
     *
     * @param pSkinnable the date picker to be skinned.
     * @param pNode the node in which components are rendered.
     */
    public CombinedDateTimePickerSkin(CombinedDateTimePicker pSkinnable, Node pNode)
    {
        super();
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
        // intentionally blank
    }
}
