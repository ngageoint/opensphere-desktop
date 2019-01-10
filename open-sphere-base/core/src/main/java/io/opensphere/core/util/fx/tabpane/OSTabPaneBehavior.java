package io.opensphere.core.util.fx.tabpane;

import static javafx.scene.input.KeyCode.DOWN;
import static javafx.scene.input.KeyCode.END;
import static javafx.scene.input.KeyCode.HOME;
import static javafx.scene.input.KeyCode.LEFT;
import static javafx.scene.input.KeyCode.PAGE_DOWN;
import static javafx.scene.input.KeyCode.PAGE_UP;
import static javafx.scene.input.KeyCode.RIGHT;
import static javafx.scene.input.KeyCode.TAB;
import static javafx.scene.input.KeyCode.UP;

import java.util.List;

import io.opensphere.core.util.fx.tabpane.inputmap.KeyMapping;
import io.opensphere.core.util.fx.tabpane.inputmap.MouseMapping;
import io.opensphere.core.util.fx.tabpane.inputmap.OSInputMap;
import io.opensphere.core.util.fx.tabpane.inputmap.OSKeyBinding;
import javafx.event.Event;
import javafx.scene.control.SelectionModel;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.input.MouseEvent;

/**
 * An implementation of the {@link OSBehaviorBase} for tab panes.
 */
public class OSTabPaneBehavior extends OSBehaviorBase<TabPane>
{
    /** The map for which this instance is configured. */
    private final OSInputMap<TabPane> myTabPaneInputMap;

    /**
     * Creates a new behavior for the supplied tab pane.
     *
     * @param tabPane the pane for which to create a new behavior.
     */
    public OSTabPaneBehavior(final TabPane tabPane)
    {
        super(tabPane);

        // create a map for TabPane-specific mappings (this reuses the default
        // OSInputMap installed on the control, if it is non-null, allowing us
        // to pick up any user-specified mappings)
        myTabPaneInputMap = createInputMap();

        // TabPane-specific mappings for key and mouse input
        addDefaultMapping(myTabPaneInputMap, new KeyMapping(UP, e -> selectPreviousTab()),
                new KeyMapping(DOWN, e -> selectNextTab()),
                new KeyMapping(LEFT, e -> rtl(tabPane, this::selectNextTab, this::selectPreviousTab)),
                new KeyMapping(RIGHT, e -> rtl(tabPane, this::selectPreviousTab, this::selectNextTab)), new KeyMapping(HOME, e ->
                {
                    if (getNode().isFocused())
                    {
                        moveSelection(-1, 1);
                    }
                }), new KeyMapping(END, e ->
                {
                    if (getNode().isFocused())
                    {
                        moveSelection(getNode().getTabs().size(), -1);
                    }
                }), new KeyMapping(new OSKeyBinding(PAGE_UP).control(), e -> selectPreviousTab()),
                new KeyMapping(new OSKeyBinding(PAGE_DOWN).control(), e -> selectNextTab()),
                new KeyMapping(new OSKeyBinding(TAB).control(), e -> selectNextTab()),
                new KeyMapping(new OSKeyBinding(TAB).control().shift(), e -> selectPreviousTab()),
                new MouseMapping(MouseEvent.MOUSE_PRESSED, e -> getNode().requestFocus()));
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.util.fx.tabpane.OSBehaviorBase#getInputMap()
     */
    @Override
    public OSInputMap<TabPane> getInputMap()
    {
        return myTabPaneInputMap;
    }

    /**
     * Selects the supplied tab in the tabbed pane.
     *
     * @param tab the tab to select in the selection model.
     */
    public void selectTab(final Tab tab)
    {
        getNode().getSelectionModel().select(tab);
    }

    /**
     * Tests to determine if the supplied tab can be closed.
     *
     * @param tab the tab to test.
     * @return <code>true</code> if the tab can be closed, <code>false</code>
     *         otherwise.
     */
    public boolean canCloseTab(final Tab tab)
    {
        final Event event = new Event(tab, tab, Tab.TAB_CLOSE_REQUEST_EVENT);
        Event.fireEvent(tab, event);
        return !event.isConsumed();
    }

    /**
     * Closes the supplied tab.
     *
     * @param tab the tab to close.
     */
    public void closeTab(final Tab tab)
    {
        final TabPane tabPane = getNode();
        // only switch to another tab if the selected tab is the one we're
        // closing
        final int index = tabPane.getTabs().indexOf(tab);
        if (index != -1)
        {
            tabPane.getTabs().remove(index);
        }
        if (tab.getOnClosed() != null)
        {
            Event.fireEvent(tab, new Event(Tab.CLOSED_EVENT));
        }
    }

    /**
     * Find a tab after the currently selected that is not disabled. Loop around
     * if no tabs are found after currently selected tab.
     */
    public void selectNextTab()
    {
        moveSelection(1);
    }

    /** Find a tab before the currently selected that is not disabled. */
    public void selectPreviousTab()
    {
        moveSelection(-1);
    }

    /**
     * Find a tab some number of items away from the currently selected that is
     * not disabled.
     *
     * @param delta the number of items to move.
     */
    private void moveSelection(final int delta)
    {
        moveSelection(getNode().getSelectionModel().getSelectedIndex(), delta);
    }

    /**
     * Moves the selection from the start index by the delta number of items.
     *
     * @param startIndex the initial index from which to start searching.
     * @param delta the number of items to move the selection.
     */
    private void moveSelection(final int startIndex, final int delta)
    {
        final TabPane tabPane = getNode();
        if (tabPane.getTabs().isEmpty())
        {
            return;
        }

        final int tabIndex = findValidTab(startIndex, delta);
        if (tabIndex > -1)
        {
            final SelectionModel<Tab> selectionModel = tabPane.getSelectionModel();
            selectionModel.select(tabIndex);
        }
        tabPane.requestFocus();
    }

    /**
     * Finds the next valid tab from the start index, jumping the delta number
     * of positions before beginning the examination.
     *
     * @param startIndex the initial examination location.
     * @param delta the initial number of positions to move before searching.
     * @return the index of the selected tab.
     */
    private int findValidTab(final int startIndex, final int delta)
    {
        final TabPane tabPane = getNode();
        final List<Tab> tabs = tabPane.getTabs();
        final int max = tabs.size();

        int index = startIndex;
        do
        {
            index = nextIndex(index + delta, max);
            final Tab tab = tabs.get(index);
            if (tab != null && !tab.isDisable())
            {
                return index;
            }
        }
        while (index != startIndex);

        return -1;
    }

    /**
     * Gets the next index of the item at the given value, with a maximum number
     * of hops.
     *
     * @param value the starting point of the search.
     * @param max the maximum number of movements to be performed.
     * @return the next available index.
     */
    private int nextIndex(final int value, final int max)
    {
        final int min = 0;
        int r = value % max;
        if (r > min && max < min)
        {
            r = r + max - min;
        }
        else if (r < min && max > min)
        {
            r = r + max - min;
        }
        return r;
    }
}
