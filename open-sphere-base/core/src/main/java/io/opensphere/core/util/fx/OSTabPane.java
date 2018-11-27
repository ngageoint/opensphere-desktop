package io.opensphere.core.util.fx;

import io.opensphere.core.util.fx.tabpane.skin.OSTabPaneSkin;
import io.opensphere.core.util.javafx.ConcurrentObjectProperty;
import javafx.beans.property.ObjectProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Skin;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

/**
 *
 */
public class OSTabPane extends TabPane
{
    /**
     * The action handler associated with this text field, or {@code null} if no
     * action handler is assigned.
     *
     * The action handler is normally called when the user types the ENTER key.
     */
    private ObjectProperty<EventHandler<ActionEvent>> myNewTabAction = new ConcurrentObjectProperty<>();

    /**
     *
     */
    public OSTabPane()
    {
        super();
    }

    /**
     * @param tabs
     */
    public OSTabPane(Tab... tabs)
    {
        super(tabs);
    }

    /**
     * {@inheritDoc}
     *
     * @see javafx.scene.control.TabPane#createDefaultSkin()
     */
    @Override
    protected Skin<?> createDefaultSkin()
    {
        return new OSTabPaneSkin(this);
    }

    /**
     * Gets the value of the {@link #myNewTabAction} field.
     *
     * @return the value stored in the {@link #myNewTabAction} field.
     */
    public ObjectProperty<EventHandler<ActionEvent>> newTabAction()
    {
        return myNewTabAction;
    }
}
