package io.opensphere.core.util.fx;

import io.opensphere.core.util.fx.tabpane.skin.TabEditPhase;
import io.opensphere.core.util.javafx.ConcurrentObjectProperty;
import javafx.beans.property.ObjectProperty;
import javafx.scene.Node;
import javafx.scene.control.Tab;

/**
 * A simple extension of the Tab class to bind the tab edit phase.
 */
public class OSTab extends Tab
{
    /** The property in which TabEditPhase values are stored. */
    private final ObjectProperty<TabEditPhase> myTabEditPhaseProperty = new ConcurrentObjectProperty<>();

    /**
     * Creates a tab with no title.
     */
    public OSTab()
    {
        super();
    }

    /**
     * Creates a tab with a text title.
     *
     * @param text The title of the tab.
     */
    public OSTab(String text)
    {
        super(text);
    }

    /**
     * Creates a tab with a text title and the specified content node.
     *
     * @param text The title of the tab.
     * @param content The content of the tab.
     */
    public OSTab(String text, Node content)
    {
        super(text, content);
    }

    /**
     * Gets the value stored in the TabEditPhase property. Logically equivalent
     * to calling <code>TabEditPhaseProperty().get();</code>.
     *
     * @return the value stored in the {@link #myTabEditPhaseProperty}.
     */
    public TabEditPhase getTabEditPhase()
    {
        return myTabEditPhaseProperty.get();
    }

    /**
     * Stores the supplied value in the {@link #myTabEditPhaseProperty}.
     * Logically equivalent to calling
     * <code>TabEditPhaseProperty().set(TabEditPhase);</code>.
     *
     * @param TabEditPhase the value to store in the
     *            {@link #myTabEditPhaseProperty}.
     */
    public void setTabEditPhase(TabEditPhase TabEditPhase)
    {
        myTabEditPhaseProperty.set(TabEditPhase);
    }

    /**
     * Gets the property defined in the {@link #myTabEditPhaseProperty} field.
     *
     * @return the property defined in the {@link #myTabEditPhaseProperty}
     *         field.
     */
    public ObjectProperty<TabEditPhase> tabEditPhaseProperty()
    {
        return myTabEditPhaseProperty;
    }
}
