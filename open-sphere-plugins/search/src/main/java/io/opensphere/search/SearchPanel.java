package io.opensphere.search;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.MenuElement;
import javax.swing.MenuSelectionManager;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.jidesoft.swing.JidePopupMenu;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import io.opensphere.core.control.action.ContextMenuProvider;
import io.opensphere.core.control.action.context.ContextIdentifiers;
import io.opensphere.core.event.ApplicationLifecycleEvent;
import io.opensphere.core.event.DataRemovalEvent;
import io.opensphere.core.event.EventListener;
import io.opensphere.core.hud.awt.HUDJInternalFrame;
import io.opensphere.core.preferences.Preferences;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.image.IconUtil;
import io.opensphere.core.util.image.IconUtil.IconStyle;
import io.opensphere.core.util.image.IconUtil.IconType;
import io.opensphere.core.util.javafx.WebDialog;
import io.opensphere.core.util.swing.EventQueueUtilities;
import io.opensphere.core.util.swing.GhostTextField;
import io.opensphere.core.util.swing.OptionsMenu;
import io.opensphere.mantle.MantleToolbox;
import io.opensphere.mantle.controller.DataGroupController;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.search.model.SearchModel;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;

/**
 * Panel which contains the "Goto" functionality.
 */
@SuppressWarnings("PMD.GodClass")
public final class SearchPanel extends JPanel implements ActionListener
{
    /** "Clear search history" text. */
    private static final String CLEAR_SEARCH_HISTORY = "Clear Search History";

    /** "Enable search history" text. */
    private static final String ENABLE_SEARCH_HISTORY = "Enable Search History";

    /** "Force text search" text. */
    private static final String FORCE_TEXT_SEARCH = "Force Text Search";

    /** "Remove goto points" text. */
    private static final String REMOVE_GOTO_POINTS = "Remove Goto Points";

    /** "Search help" text. */
    private static final String SEARCH_HELP = "Search Help";

    /** "Search Options View" text. */
    private static final String SEARCH_OPTIONS_PANEL = "Search Options";

    /** Serial id. */
    private static final long serialVersionUID = 1L;

    /** The Clear all goto points event listener. */
    private final transient EventListener<DataRemovalEvent> myClearAllGotoPointsEventListener;

    /** The Clear goto points. */
    private final transient ContextMenuProvider<Void> myClearGotoPoints = new ContextMenuProvider<Void>()
    {
        @Override
        public List<JMenuItem> getMenuItems(String contextId, Void key)
        {
            JMenuItem cancelAllQueriesMenuItem = new JMenuItem("Clear Goto Points");
            cancelAllQueriesMenuItem.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    clearGotoPoints();
                }
            });
            return Collections.singletonList(cancelAllQueriesMenuItem);
        }

        @Override
        public int getPriority()
        {
            return 100;
        }
    };

    /** The Container panel. */
    private JPanel myContainerPanel;

    /** The Force text search. */
    private boolean myForceTextSearch;

    /** The Help browser. */
    private WebDialog myHelpBrowser;

    /** The Is search history enabled. */
    private boolean myIsSearchHistoryEnabled = true;

    /** The Life cycle event listener. */
    private transient EventListener<ApplicationLifecycleEvent> myLifeCycleEventListener;

    /** The search/goto preferences. */
    private final transient Preferences myPrefs;

    /** The Search field. */
    private GhostTextField mySearchField;

    /** The button used to search in the current view. */
    private JButton myWhatsHereButton;

    /** The Search goto controller. */
    private final transient KeywordController mySearchGotoController;

    /** The History map. */
    private TIntObjectMap<List<String>> mySearchHistoryMap;

    /** The Search history. */
    private CustomPopup mySearchHistoryPopup;

    /** The search options menu. */
    private OptionsMenu mySearchOptionsMenu;

    /** The Data Group Controller. */
    private final transient DataGroupController myDataGroupController;

    /** The dialog used to visualize results. */
    private final SearchResultHudDialog myResultsDialog;

    /** The supplier used to supply the HUD frame. */
    private final transient Supplier<HUDJInternalFrame> myHudFrameSupplier;

    /** The main search model. */
    private final transient SearchModel mySearchModel;

    /** Map of search provider name to menu item. */
    private final Map<String, JCheckBoxMenuItem> myProviderMenuItems = New.map();

    /**
     * Default constructor.
     *
     * @param controller the controller
     * @param resultsDialog The dialog used to visualize results.
     * @param hudFrameSupplier The supplier used to supply the HUD frame.
     * @param searchModel The main search model.
     */
    public SearchPanel(KeywordController controller, SearchResultHudDialog resultsDialog,
            Supplier<HUDJInternalFrame> hudFrameSupplier, SearchModel searchModel)
    {
        super();
        mySearchGotoController = controller;
        myResultsDialog = resultsDialog;
        myHudFrameSupplier = hudFrameSupplier;
        mySearchModel = searchModel;
        myDataGroupController = mySearchGotoController.getToolbox().getPluginToolboxRegistry()
                .getPluginToolbox(MantleToolbox.class).getDataGroupController();
        myPrefs = mySearchGotoController.getToolbox().getPreferencesRegistry().getPreferences(SearchPanel.class);
        myClearAllGotoPointsEventListener = event -> clearGotoPoints();
        controller.getToolbox().getEventManager().subscribe(DataRemovalEvent.class, myClearAllGotoPointsEventListener);
        controller.getToolbox().getUIRegistry().getContextActionManager()
                .registerContextMenuItemProvider(ContextIdentifiers.DELETE_CONTEXT, Void.class, myClearGotoPoints);

        initialize();
    }

    @Override
    public void actionPerformed(ActionEvent evt)
    {
        if (evt.getSource() instanceof JCheckBoxMenuItem)
        {
            JCheckBoxMenuItem anItem = (JCheckBoxMenuItem)evt.getSource();
            if (anItem.getText().equals(FORCE_TEXT_SEARCH))
            {
                myPrefs.putBoolean(FORCE_TEXT_SEARCH, anItem.isSelected(), this);
                myForceTextSearch = anItem.isSelected();
            }
            else if (anItem.getText().equals(ENABLE_SEARCH_HISTORY))
            {
                myIsSearchHistoryEnabled = anItem.isSelected();
            }
        }
        else if (evt.getSource() instanceof JMenuItem)
        {
            JMenuItem anItem = (JMenuItem)evt.getSource();
            if (anItem.getText().equals(CLEAR_SEARCH_HISTORY))
            {
                mySearchHistoryPopup.setVisible(false);
                initHistoryMap();
            }
            else if (anItem.getName() != null && anItem.getName().equals("historyMenuItem"))
            {
                getSearchField().setText(anItem.getText());
                search(getSearchField().getText(), validateInput());
            }
            else if (anItem.getText().equals(REMOVE_GOTO_POINTS))
            {
                clearGotoPoints();
            }
            else if (anItem.getText().equals(SEARCH_HELP))
            {
                showHelpBrowser();
            }
            else if (anItem.getText().equals(SEARCH_OPTIONS_PANEL))
            {
                mySearchGotoController.getToolbox().getUIRegistry().getOptionsRegistry()
                        .requestShowTopic(SearchOptionsProvider.PROVIDER_NAME);
            }
        }
    }

    /**
     * Gets the search field.
     *
     * @return the search field
     */
    public GhostTextField getSearchField()
    {
        if (mySearchField == null)
        {
            mySearchField = new GhostTextField("Enter coordinates or search");
            mySearchField.setOpaque(false);
            mySearchField.setSize(new Dimension(230, 24));
            mySearchField.setMinimumSize(mySearchField.getSize());
            mySearchField.setPreferredSize(mySearchField.getSize());
            mySearchField.getDocument().addDocumentListener(createSearchFieldDocumentListener());
            mySearchField.addKeyListener(createSearchFieldKeyListener());
            mySearchField.addPropertyChangeListener(createSearchFieldPropertyChangeListener());
        }
        return mySearchField;
    }

    /**
     * Clear goto points.
     */
    private void clearGotoPoints()
    {
        mySearchGotoController.removeAllGotoGeometries();
    }

    /**
     * Clear search history.
     */
    private void clearSearchHistory()
    {
        Component[] comps = mySearchHistoryPopup.getComponents();
        for (int i = mySearchHistoryPopup.getComponentCount() - 1; i >= 0; i--)
        {
            if (comps[i] instanceof JMenuItem)
            {
                JMenuItem anItem = (JMenuItem)comps[i];
                anItem.removeActionListener(this);
                mySearchHistoryPopup.remove(comps[i]);
            }
            if (comps[i] instanceof JSeparator)
            {
                mySearchHistoryPopup.remove(comps[i]);
            }
        }
        mySearchHistoryPopup.setVisible(false);
    }

    /**
     * Creates the search field document listener.
     *
     * @return the document listener
     */
    private DocumentListener createSearchFieldDocumentListener()
    {
        return new DocumentListener()
        {
            @Override
            public void changedUpdate(DocumentEvent e)
            {
                validateInput();
            }

            @Override
            public void insertUpdate(DocumentEvent e)
            {
                validateInput();
                resetSearchHistoryList();
            }

            @Override
            public void removeUpdate(DocumentEvent e)
            {
                validateInput();
                if (getSearchField().getText().length() == 0)
                {
                    mySearchGotoController.clearSearchResults();
                    clearSearchHistory();
                }
                else
                {
                    resetSearchHistoryList();
                }
            }
        };
    }

    /**
     * Creates the search field key listener.
     *
     * @return the key listener
     */
    private KeyListener createSearchFieldKeyListener()
    {
        return new KeyAdapter()
        {
            @Override
            public void keyPressed(KeyEvent e)
            {
                int key = e.getKeyCode();
                if (key == KeyEvent.VK_ENTER || key == KeyEvent.VK_PASTE)
                {
                    // Check to see if we've pressed enter over a menu item in
                    // the popup menu
                    if (mySearchHistoryPopup.isVisible())
                    {
                        MenuElement[] path = MenuSelectionManager.defaultManager().getSelectedPath();
                        if (path.length >= 2 && path[0] instanceof CustomPopup && path[1] instanceof JMenuItem)
                        {
                            JMenuItem anItem = (JMenuItem)path[1];
                            getSearchField().setText(anItem.getText());
                            search(anItem.getText(), validateInput());
                            mySearchHistoryPopup.setVisible(false);
                        }
                        else
                        {
                            search(getSearchField().getText(), validateInput());
                        }
                    }
                    else
                    {
                        if (getSearchField().getText().length() > 0)
                        {
                            search(getSearchField().getText(), validateInput());
                        }
                    }
                }
            }
        };
    }

    /**
     * When searching for a term thats already in a active group, remove that
     * group.
     *
     * @param searchTerm the string being searched.
     *
     */
    private void replaceActiveData(String searchTerm)
    {
        List<DataGroupInfo> activeGroups = myDataGroupController.getActiveGroups();

        if (activeGroups != null && !activeGroups.isEmpty())
        {
            for (DataGroupInfo group : activeGroups)
            {
                if (group.getId().equals("MyPlaces"))
                {
                    for (DataGroupInfo child : group.getChildren())
                    {
                        if (child.getDisplayName().contains(searchTerm))
                        {
                            group.removeChild(child, this);
                        }
                    }
                }
            }
        }
    }

    /**
     * Creates the search field property change listener.
     *
     * @return the property change listener
     */
    private PropertyChangeListener createSearchFieldPropertyChangeListener()
    {
        return new PropertyChangeListener()
        {
            @Override
            public void propertyChange(PropertyChangeEvent evt)
            {
                if (evt.getPropertyName().equals(GhostTextField.CLEAR_PROPERTY))
                {
                    clearSearchHistory();
                }
            }
        };
    }

    /**
     * Gets the container panel.
     *
     * @return the container panel
     */
    private JPanel getContainerPanel()
    {
        if (myContainerPanel == null)
        {
            myContainerPanel = new JPanel();
            myContainerPanel.setOpaque(false);
            myContainerPanel.setBackground(new Color(100, 100, 100, 100));
            myContainerPanel.setLayout(new GridBagLayout());

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets.right = 5;
            gbc.gridx = 0;
            gbc.gridy = 0;

            setLayout(new GridBagLayout());
            setName("searchPanel");

            // removed to disable layer recommendation. Do not delete this code,
            // it will go back in when layer recommendation is re-enabled for
            // deployment:
//            myWhatsHereButton = new IconButton(
//                    IconUtil.getColorizedIcon("/images/whatshere.png", IconStyle.NORMAL, IconUtil.DEFAULT_ICON_FOREGROUND));
//            myWhatsHereButton.setToolTipText("Discover what data is available in the current view and time.");
//            myWhatsHereButton.addActionListener(e ->
//            {
//                myHudFrameSupplier.get().setVisible(true);
//                myResultsDialog.resizeAndPositionToDefault(true);
//            });
//            myContainerPanel.add(myWhatsHereButton, gbc);
//
//            gbc.gridx++;

            myContainerPanel.add(getSearchMenu(), gbc);

            gbc.gridx++;
            gbc.insets = new Insets(0, 0, 0, 3);
            myContainerPanel.add(getSearchField(), gbc);
        }
        return myContainerPanel;
    }

    /**
     * Gets the life cycle event listener. Wait until all plugins are loaded
     * before enabling the search tool.
     *
     * @return the life cycle event listener
     */
    private EventListener<ApplicationLifecycleEvent> getLifeCycleEventListener()
    {
        if (myLifeCycleEventListener == null)
        {
            myLifeCycleEventListener = new EventListener<ApplicationLifecycleEvent>()
            {
                @Override
                public void notify(ApplicationLifecycleEvent event)
                {
                    if (event.getStage() == ApplicationLifecycleEvent.Stage.PLUGINS_INITIALIZED)
                    {
                        EventQueueUtilities.invokeLater(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                getSearchField().setEnabled(true);
                                getSearchMenu().setEnabled(true);
                            }
                        });
                    }
                }
            };
        }
        return myLifeCycleEventListener;
    }

    /**
     * Gets the search menu.
     *
     * @return the search menu
     */
    private OptionsMenu getSearchMenu()
    {
        if (mySearchOptionsMenu == null)
        {
            mySearchOptionsMenu = new OptionsMenu((Icon)null, 0);
            mySearchOptionsMenu.setIcon(IconUtil.getColorizedIcon(IconType.MENU, IconStyle.FLAT, Color.WHITE, 13));

            JCheckBoxMenuItem forceText = new JCheckBoxMenuItem(FORCE_TEXT_SEARCH);
            myForceTextSearch = myPrefs.getBoolean(FORCE_TEXT_SEARCH, false);
            forceText.setSelected(myForceTextSearch);
            forceText.addActionListener(this);
            mySearchOptionsMenu.addItem(forceText);

            JMenuItem clearGotoPoints = new JMenuItem(REMOVE_GOTO_POINTS);
            clearGotoPoints.addActionListener(this);
            mySearchOptionsMenu.addItem(clearGotoPoints);

            mySearchOptionsMenu.add(new JSeparator(SwingConstants.HORIZONTAL));

            JCheckBoxMenuItem item3 = new JCheckBoxMenuItem(ENABLE_SEARCH_HISTORY);
            item3.setSelected(true);
            item3.addActionListener(this);
            mySearchOptionsMenu.addItem(item3);

            JMenuItem item4 = new JMenuItem(CLEAR_SEARCH_HISTORY);
            item4.addActionListener(this);
            mySearchOptionsMenu.addItem(item4);

            mySearchOptionsMenu.add(new JSeparator(SwingConstants.HORIZONTAL));

            JMenuItem searchHelpitem = new JMenuItem(SEARCH_HELP);
            searchHelpitem.addActionListener(this);

            mySearchOptionsMenu.addItem(searchHelpitem);

            JMenuItem searchOptionsMenu = new JMenuItem(SEARCH_OPTIONS_PANEL);
            searchOptionsMenu.addActionListener(this);

            mySearchOptionsMenu.addItem(searchOptionsMenu);
        }

        return mySearchOptionsMenu;
    }

    /**
     * Inits the history map.
     */
    private void initHistoryMap()
    {
        if (mySearchHistoryMap != null)
        {
            mySearchHistoryMap = null;
        }
        mySearchHistoryMap = new TIntObjectHashMap<>();

        for (int i = 0; i < 127; i++)
        {
            char ch = (char)i;
            if (Character.isLetterOrDigit(ch))
            {
                ArrayList<String> list = new ArrayList<>();
                mySearchHistoryMap.put(i, list);
            }
        }
    }

    /**
     * Initialize.
     */
    private void initialize()
    {
        setOpaque(false);
        add(getContainerPanel());
        mySearchHistoryPopup = new CustomPopup();
        initHistoryMap();

        listenForSearchProviders();
        listenForSelectedProviders();
    }

    /**
     * Listens for search providers being added, adding menu items for them.
     */
    private void listenForSearchProviders()
    {
        mySearchModel.getSearchTypes().addListener(new ListChangeListener<String>()
        {
            @Override
            public void onChanged(ListChangeListener.Change<? extends String> change)
            {
                while (change.next())
                {
                    if (change.wasAdded())
                    {
                        List<String> addedList = New.list(change.getAddedSubList());
                        Set<String> selectedTypes = New.set(mySearchModel.getSelectedSearchTypes());

                        EventQueue.invokeLater(() -> addProviderMenuItems(addedList, selectedTypes));
                    }
                }
            }
        });
    }

    /**
     * Listens for search providers being selected/deselected.
     */
    private void listenForSelectedProviders()
    {
        mySearchModel.getSelectedSearchTypes().addListener(new ListChangeListener<String>()
        {
            @Override
            public void onChanged(ListChangeListener.Change<? extends String> change)
            {
                while (change.next())
                {
                    List<String> added = New.list(change.getAddedSubList());
                    List<String> removed = New.list(change.getRemoved());
                    EventQueue.invokeLater(() ->
                    {
                        for (String type : added)
                        {
                            JCheckBoxMenuItem menuItem = myProviderMenuItems.get(type);
                            if (menuItem != null)
                            {
                                menuItem.setSelected(true);
                            }
                        }
                        for (String type : removed)
                        {
                            JCheckBoxMenuItem menuItem = myProviderMenuItems.get(type);
                            if (menuItem != null)
                            {
                                menuItem.setSelected(false);
                            }
                        }
                    });
                }
            }
        });
    }

    /**
     * Adds search provider menu items.
     *
     * @param searchTypes the search types
     * @param selectedTypes the selected types
     */
    private void addProviderMenuItems(Collection<? extends String> searchTypes, Collection<? extends String> selectedTypes)
    {
        mySearchOptionsMenu.add(new JSeparator(SwingConstants.HORIZONTAL));

        JLabel label = new JLabel("Search Types");
        label.setFont(label.getFont().deriveFont(Font.BOLD));
        label.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        mySearchOptionsMenu.add(label);

        for (String searchType : searchTypes)
        {
            JCheckBoxMenuItem typeMenu = new JCheckBoxMenuItem(searchType);
            typeMenu.setSelected(selectedTypes.contains(searchType));
            mySearchOptionsMenu.addItem(typeMenu);
            myProviderMenuItems.put(searchType, typeMenu);

            typeMenu.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    AbstractButton menuItem = (AbstractButton)e.getSource();
                    boolean selected = menuItem.isSelected();
                    String type = menuItem.getText();
                    Platform.runLater(() ->
                    {
                        if (selected)
                        {
                            CollectionUtilities.addIfNotContained(mySearchModel.getSelectedSearchTypes(), type);
                        }
                        else
                        {
                            mySearchModel.getSelectedSearchTypes().remove(type);
                        }
                    });
                }
            });
        }
    }

    /**
     * Sets the available menu items in the popup menu based on the currently
     * entered text.
     */
    private void resetSearchHistoryList()
    {
        String searchText = getSearchField().getText();
        int key = searchText.charAt(0);

        clearSearchHistory();

        if (mySearchHistoryMap.containsKey(key))
        {
            ArrayList<String> toAdd = new ArrayList<>();
            List<String> list = mySearchHistoryMap.get(key);
            if (!list.isEmpty())
            {
                // Go thru the list of saved strings
                for (String str : list)
                {
                    if (str.startsWith(searchText))
                    {
                        toAdd.add(str);
                    }
                }

                // If any matched, create the history menu
                if (!toAdd.isEmpty())
                {
                    // When there are 5 items in the list, the jidepopupmenu
                    // displays the space for the scroll button at the bottom of
                    // the screen. The +10 will draw the popup over this space
                    int height = 18 * toAdd.size();
                    if (toAdd.size() == 5)
                    {
                        height += 10;
                    }
                    for (int i = 0; i < toAdd.size(); i++)
                    {
                        JMenuItem item = new JMenuItem(toAdd.get(i));
                        item.setName("historyMenuItem");
                        item.addActionListener(this);
                        mySearchHistoryPopup.add(item);
                        if (i == 0)
                        {
                            mySearchHistoryPopup.add(new JSeparator(SwingConstants.HORIZONTAL));
                        }
                    }
                    mySearchHistoryPopup.setPreferredSize(height);
                    mySearchHistoryPopup.setScrollableViewportHeight(height);
                    if (myIsSearchHistoryEnabled)
                    {
                        mySearchHistoryPopup.show(this, getSearchField().getLocation().x, getSearchField().getSize().height);
                    }
                }
                getSearchField().requestFocus();
            }
        }
    }

    /**
     * Create a filter and apply it to the table which will display and
     * highlight only rows that contain the search text.
     *
     * @param text the text
     * @param gotoPoint the goto point
     */
    private void search(final String text, boolean gotoPoint)
    {
        // Check to see if term being searched is already in active group
        replaceActiveData(getSearchField().getText());
        // Add the search string to the map
        int key = -1;
        // Get the first digit or num and use that for the key
        // EX: -102 will live in the array for the index '1' in the map
        for (int i = 0; i < text.length(); i++)
        {
            char ch = text.charAt(i);
            if (Character.isLetterOrDigit(ch))
            {
                key = ch;
                break;
            }
        }

        if (mySearchHistoryMap.containsKey(key) && !mySearchHistoryMap.get(key).contains(text))
        {
            mySearchHistoryMap.get(key).add(text);
        }

        if (!gotoPoint || myForceTextSearch)
        {
            myHudFrameSupplier.get().setVisible(true);
            myResultsDialog.resizeAndPositionToDefault(false);
            mySearchGotoController.serviceSearch(text);
        }
        else
        {
            mySearchGotoController.centerOn(text);
        }
    }

    /**
     * Show help browser.
     */
    private void showHelpBrowser()
    {
        if (myHelpBrowser == null)
        {
            myHelpBrowser = new WebDialog(SwingUtilities.windowForComponent(this));
            myHelpBrowser.setTitle("Search / Goto Help");
            URL helpUrl = getClass().getResource("/help/SearchGotoHelp.html");
            myHelpBrowser.load(helpUrl.toExternalForm());
        }
        myHelpBrowser.setVisible(true);
        myHelpBrowser.toFront();
    }

    /**
     * Validate input.
     *
     * @return true, if successful
     */
    private boolean validateInput()
    {
        return mySearchGotoController.validate(getSearchField().getText()) && !myForceTextSearch;
    }

    /**
     * The Class CustomPopup.
     */
    private static class CustomPopup extends JidePopupMenu
    {
        /** The Constant DEFAULT_CUSTOM_POPUP_WIDTH. */
        private static final int DEFAULT_CUSTOM_POPUP_WIDTH = 210;

        /**
         * serialVersionUID.
         */
        private static final long serialVersionUID = 1L;

        /** The d. */
        private Dimension myDim = new Dimension(DEFAULT_CUSTOM_POPUP_WIDTH, 21);

        /** The max dim. */
        private final Dimension myMaxDim = new java.awt.Dimension(DEFAULT_CUSTOM_POPUP_WIDTH, 80);

        @Override
        public Dimension getPreferredScrollableViewportSize()
        {
            return myDim;
        }

        /**
         * Sets the preferred size.
         *
         * @param height the new preferred size
         */
        public void setPreferredSize(int height)
        {
            setPreferredSize(new Dimension(DEFAULT_CUSTOM_POPUP_WIDTH, height));
        }

        /**
         * Sets the scrollable viewport height.
         *
         * @param height the new scrollable viewport height
         */
        public void setScrollableViewportHeight(int height)
        {
            Dimension newDim = new Dimension(DEFAULT_CUSTOM_POPUP_WIDTH, height);
            if (height < myMaxDim.height)
            {
                myDim = newDim;
            }
            else
            {
                myDim = myMaxDim;
            }
        }
    }
}
