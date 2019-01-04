package io.opensphere.core.util.fx.tabpane;

import javafx.beans.InvalidationListener;
import javafx.beans.WeakInvalidationListener;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.layout.StackPane;

/** A region for each tab to contain the tab's content node */
public class OSTabContentRegion extends StackPane
{
    /** The tab for which the content region is defined. */
    private Tab myTab;

    /** The listener for invalidation of the tab content. */
    private InvalidationListener tabContentListener = v -> updateContent();

    /** The listener for invalidation of the selection of the tab. */
    private InvalidationListener tabSelectedListener = v -> setVisible(myTab.isSelected());

    /** Weak reference for the content listener. */
    private WeakInvalidationListener weakTabContentListener = new WeakInvalidationListener(tabContentListener);

    /** Weak reference for the selection listener. */
    private WeakInvalidationListener weakTabSelectedListener = new WeakInvalidationListener(tabSelectedListener);

    /**
     * Creates a new tab content region for the supplied tab.
     *
     * @param tab The tab for which the content region is defined.
     */
    public OSTabContentRegion(Tab tab)
    {
        getStyleClass().setAll("tab-content-area");
        setManaged(false);
        this.myTab = tab;
        updateContent();
        setVisible(tab.isSelected());

        tab.selectedProperty().addListener(weakTabSelectedListener);
        tab.contentProperty().addListener(weakTabContentListener);
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

    /** Updates the content for the tab's region. */
    private void updateContent()
    {
        Node newContent = getTab().getContent();
        if (newContent == null)
        {
            getChildren().clear();
        }
        else
        {
            getChildren().setAll(newContent);
        }
    }

    /**
     * Invalidates all listeners for the supplied tab.
     *
     * @param tab the tab for which to invalidate the listeners.
     */
    public void removeListeners(Tab tab)
    {
        tab.selectedProperty().removeListener(weakTabSelectedListener);
        tab.contentProperty().removeListener(weakTabContentListener);
    }

}
