package io.opensphere.core.util.fx.tabpane;

import io.opensphere.core.util.fx.tabpane.skin.OSTabPaneSkin;
import javafx.beans.InvalidationListener;
import javafx.beans.WeakInvalidationListener;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.Tab;

/** A menu item for a single tab. */
public class OSTabMenuItem extends RadioMenuItem
{
    /** The tab for which the menu items are active. */
    private final Tab myTab;

    /** The listener used to disable the tab. */
    private final InvalidationListener myDisableListener = o -> setDisable(myTab.isDisable());

    /** The weak handle to the invalidation listener. */
    private final WeakInvalidationListener myWeakDisableListener = new WeakInvalidationListener(myDisableListener);

    /**
     * Creates a new menu item for the supplied tab.
     *
     * @param tab the tab for which to create the menu item.
     */
    public OSTabMenuItem(final Tab tab)
    {
        super(tab.getText(), OSTabPaneSkin.clone(tab.getGraphic()));
        myTab = tab;
        setDisable(tab.isDisable());
        tab.disableProperty().addListener(myWeakDisableListener);
        textProperty().bind(tab.textProperty());
    }

    /**
     * Gets the value of the {@link #myTab} field.
     *
     * @return the value stored in the {@link #myTab} field.
     */
    public Tab getTab()
    {
        return myTab;
    }

    /** Disposes of the tab and any associated listeners. */
    public void dispose()
    {
        myTab.disableProperty().removeListener(myWeakDisableListener);
    }
}
