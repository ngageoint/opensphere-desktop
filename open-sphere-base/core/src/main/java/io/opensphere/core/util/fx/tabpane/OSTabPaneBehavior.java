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
 *
 */
public class OSTabPaneBehavior extends OSBehaviorBase<TabPane>
{

    private final OSInputMap<TabPane> tabPaneInputMap;

    public OSTabPaneBehavior(TabPane tabPane)
    {
        super(tabPane);

        // create a map for TabPane-specific mappings (this reuses the default
        // OSInputMap installed on the control, if it is non-null, allowing us to pick up any user-specified mappings)
        tabPaneInputMap = createInputMap();

        // TabPane-specific mappings for key and mouse input
        addDefaultMapping(tabPaneInputMap,
            new KeyMapping(UP, e -> selectPreviousTab()),
            new KeyMapping(DOWN, e -> selectNextTab()),
            new KeyMapping(LEFT, e -> rtl(tabPane, this::selectNextTab, this::selectPreviousTab)),
            new KeyMapping(RIGHT, e -> rtl(tabPane, this::selectPreviousTab, this::selectNextTab)),
            new KeyMapping(HOME, e -> {
                if (getNode().isFocused()) {
                    moveSelection(-1, 1);
                }
            }),
            new KeyMapping(END, e -> {
                if (getNode().isFocused()) {
                    moveSelection(getNode().getTabs().size(), -1);
                }
            }),
            new KeyMapping(new OSKeyBinding(PAGE_UP).control(), e -> selectPreviousTab()),
            new KeyMapping(new OSKeyBinding(PAGE_DOWN).control(), e -> selectNextTab()),
            new KeyMapping(new OSKeyBinding(TAB).control(), e -> selectNextTab()),
            new KeyMapping(new OSKeyBinding(TAB).control().shift(), e -> selectPreviousTab()),
            new MouseMapping(MouseEvent.MOUSE_PRESSED, e -> getNode().requestFocus())
        );
    }

    @Override
    public OSInputMap<TabPane> getInputMap()
    {
        return tabPaneInputMap;
    }

    public void selectTab(Tab tab)
    {
        getNode().getSelectionModel().select(tab);
    }

    public boolean canCloseTab(Tab tab)
    {
        Event event = new Event(tab, tab, Tab.TAB_CLOSE_REQUEST_EVENT);
        Event.fireEvent(tab, event);
        return !event.isConsumed();
    }

    public void closeTab(Tab tab)
    {
        TabPane tabPane = getNode();
        // only switch to another tab if the selected tab is the one we're
        // closing
        int index = tabPane.getTabs().indexOf(tab);
        if (index != -1)
        {
            tabPane.getTabs().remove(index);
        }
        if (tab.getOnClosed() != null)
        {
            Event.fireEvent(tab, new Event(Tab.CLOSED_EVENT));
        }
    }

    // Find a tab after the currently selected that is not disabled. Loop around
    // if no tabs are found after currently selected tab.
    public void selectNextTab()
    {
        moveSelection(1);
    }

    // Find a tab before the currently selected that is not disabled.
    public void selectPreviousTab()
    {
        moveSelection(-1);
    }

    private void moveSelection(int delta)
    {
        moveSelection(getNode().getSelectionModel().getSelectedIndex(), delta);
    }

    private void moveSelection(int startIndex, int delta)
    {
        final TabPane tabPane = getNode();
        if (tabPane.getTabs().isEmpty())
        {
            return;
        }

        int tabIndex = findValidTab(startIndex, delta);
        if (tabIndex > -1)
        {
            final SelectionModel<Tab> selectionModel = tabPane.getSelectionModel();
            selectionModel.select(tabIndex);
        }
        tabPane.requestFocus();
    }

    private int findValidTab(int startIndex, int delta)
    {
        final TabPane tabPane = getNode();
        final List<Tab> tabs = tabPane.getTabs();
        final int max = tabs.size();

        int index = startIndex;
        do
        {
            index = nextIndex(index + delta, max);
            Tab tab = tabs.get(index);
            if (tab != null && !tab.isDisable())
            {
                return index;
            }
        }
        while (index != startIndex);

        return -1;
    }

    private int nextIndex(int value, int max)
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
