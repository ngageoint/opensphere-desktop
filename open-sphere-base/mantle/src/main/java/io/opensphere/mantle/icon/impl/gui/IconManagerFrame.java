package io.opensphere.mantle.icon.impl.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import io.opensphere.core.Toolbox;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.filesystem.MnemonicFileChooser;
import io.opensphere.core.util.lang.EqualsHelper;
import io.opensphere.mantle.icon.IconProvider;
import io.opensphere.mantle.icon.IconRecord;
import io.opensphere.mantle.icon.IconRecordTreeNodeUserObject;
import io.opensphere.mantle.icon.IconRecordTreeNodeUserObject.NameType;
import io.opensphere.mantle.icon.IconRegistry;
import io.opensphere.mantle.icon.IconRegistryListener;
import io.opensphere.mantle.icon.impl.DefaultIconProvider;
import io.opensphere.mantle.icon.impl.IconProviderFactory;
import io.opensphere.mantle.iconproject.model.PanelModel;
import io.opensphere.mantle.iconproject.view.IconCustomizerDialog;
import io.opensphere.mantle.util.MantleToolboxUtils;

/**
 * A icon manager user interface.
 */
public class IconManagerFrame extends JFrame implements IconRegistryListener
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(IconManagerFrame.class);

    /**
     * serialVersionUID.
     */
    private static final long serialVersionUID = 1L;

    /** The Chooser panel. */
    private final IconChooserPanel myChooserPanel;

    /** The Edit menu. */
    private final JMenu myEditMenu;

    /** The File menu. */
    private final JMenu myFileMenu;

    /** The Icon registry. */
    private final IconRegistry myIconRegistry;

    /** The Menu bar. */
    private final JMenuBar myMenuBar;

    /** The Toolbox. */
    private final Toolbox myToolbox;

    /** The initial width of display icon tiles. */
    private int tileWidth = 100;

    /**
     * Instantiates a new icon chooser dialog.
     *
     * @param tb the {@link Toolbox}
     */
    public IconManagerFrame(Toolbox tb)
    {
        super();
        myToolbox = tb;
        myIconRegistry = MantleToolboxUtils.getMantleToolbox(myToolbox).getIconRegistry();
        setTitle("Icon Manager");
        setIconImage(myToolbox.getUIRegistry().getMainFrameProvider().get().getIconImage());
        setSize(new Dimension(800, 600));
        setMinimumSize(new Dimension(600, 400));
        JPopupMenu iconPopupMenu = new JPopupMenu();
        JPopupMenu treePopupMenu = new JPopupMenu();

        JButton buildIcon = new JButton("Build New Icon");
        buildIcon.addActionListener(e -> showBuilderDialog());

        Font font = new Font("arial", Font.BOLD, 22);
        JButton enlargeButton = new JButton("+");
        enlargeButton.addActionListener(e -> setIconSize(true));
        enlargeButton.setFocusPainted(false);
        enlargeButton.setFont(font);
        enlargeButton.setToolTipText("Increase Icon Size");

        JButton reduceButton = new JButton("-");
        reduceButton.addActionListener(e -> setIconSize(false));
        reduceButton.setFocusPainted(false);
        reduceButton.setFont(font);
        reduceButton.setToolTipText("Reduce Icon Size");

        myChooserPanel = new IconChooserPanel(tb, true, true, iconPopupMenu, treePopupMenu, buildIcon);

        myMenuBar = new JMenuBar();
        myFileMenu = new JMenu("File");
        myMenuBar.add(myFileMenu);
        myEditMenu = new JMenu("Edit");
        myMenuBar.add(myEditMenu);
        myMenuBar.add(Box.createHorizontalGlue());
        myMenuBar.add(reduceButton);
        myMenuBar.add(enlargeButton);
        myMenuBar.add(Box.createHorizontalGlue());

        JPanel mainPanel = new JPanel(new BorderLayout());
        JPanel closePanel = new JPanel();
        closePanel.setLayout(new BoxLayout(closePanel, BoxLayout.X_AXIS));
        JButton closeButton = new JButton("Close");
        closeButton.setMaximumSize(new Dimension(100, 30));
        closeButton.addActionListener(e -> setVisible(false));
        closePanel.add(Box.createHorizontalGlue());
        closePanel.add(closeButton);
        closePanel.add(Box.createHorizontalStrut(-5));
        closePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        mainPanel.add(myMenuBar, BorderLayout.NORTH);
        mainPanel.add(myChooserPanel, BorderLayout.CENTER);
        mainPanel.add(closePanel, BorderLayout.SOUTH);
        setContentPane(mainPanel);
        setLocationRelativeTo(tb.getUIRegistry().getMainFrameProvider().get());

        createMenuBarFileMenuItems();
        createMenuBarEditMenuItems();
        createIconPopupMenuItems(iconPopupMenu);
        createTreePopupMenuItems(treePopupMenu);
        myIconRegistry.addListener(this);
    }

    /**
     * Initiates icon resizing.
     *
     * @param enlarge the {@link #JButton reduceButton/increaseButton}
     */
    private void setIconSize(boolean enlarge)
    {
        if (enlarge)
        {
            tileWidth += 20;
        }
        else
        {
            tileWidth -= 20;
        }
        myChooserPanel.setIconSize(myChooserPanel.getResizeRecords(), false, tileWidth);
    }

    /**
     * Shows the icon builder dialog.
     */
    private void showBuilderDialog()
    {
        PanelModel thePanelModel = new PanelModel(myToolbox);
        thePanelModel.setIconRegistry(MantleToolboxUtils.getMantleToolbox(myToolbox).getIconRegistry());
        thePanelModel.getSelectedRecord().set((myChooserPanel.getLastPopupTriggerIconRecord()));
        IconCustomizerDialog dialog = new IconCustomizerDialog(this, thePanelModel);
        dialog.setVisible(true);
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.icon.IconRegistryListener#iconAssigned(long,
     *      java.util.List, java.lang.Object)
     */
    @Override
    public void iconAssigned(long iconId, List<Long> deIds, Object source)
    {
        // Do nothing.
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.icon.IconRegistryListener#iconsAdded(java.util.List,
     *      java.lang.Object)
     */
    @Override
    public void iconsAdded(List<IconRecord> added, Object source)
    {
        String collection = !added.isEmpty() ? added.get(0).getCollectionName() : null;
        myChooserPanel.refreshFromRegistry(collection);
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.icon.IconRegistryListener#iconsRemoved(java.util.List,
     *      java.lang.Object)
     */
    @Override
    public void iconsRemoved(List<IconRecord> removed, Object source)
    {
        String collection = !removed.isEmpty() ? removed.get(0).getCollectionName() : null;
        myChooserPanel.refreshFromRegistry(collection);
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.icon.IconRegistryListener#iconsUnassigned(java.util.List,
     *      java.lang.Object)
     */
    @Override
    public void iconsUnassigned(List<Long> deIds, Object source)
    {
        // Do nothing.
    }

    /**
     * Adds the icon from file.
     */
    private void addIconFromFile()
    {
        String colName = getCollectionNameFromUser();
        if (!StringUtils.isBlank(colName))
        {
            SubCategoryPanel pnl = getSubCategoryFromUser(colName, false);
            if (pnl != null)
            {
                myChooserPanel.loadFromFile(colName, pnl.getCategory());
            }
        }
    }

    /**
     * Adds the icons from folder.
     */
    private void addIconsFromFolder()
    {
        String colName = getCollectionNameFromUser();
        if (!StringUtils.isBlank(colName))
        {
            SubCategoryPanel pnl = getSubCategoryFromUser(colName, true);
            if (pnl != null)
            {
                MnemonicFileChooser chooser = new MnemonicFileChooser(myToolbox.getPreferencesRegistry(), "IconManagerFrame");

                chooser.setDialogTitle("Choose Folder");
                chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

                int result = chooser.showOpenDialog(this);

                if (result == JFileChooser.APPROVE_OPTION)
                {
                    File resultFile = chooser.getSelectedFile();
                    try
                    {
                        List<DefaultIconProvider> providerList = IconProviderFactory.createFromDirectory(resultFile, colName,
                                "IconManager", pnl.isSubCatsFromDirNames(), pnl.getCategory());
                        myIconRegistry.addIcons(providerList, this);
                    }
                    catch (IOException e)
                    {
                        JOptionPane.showMessageDialog(this, "Failed to load one or more icons: " + resultFile.getAbsolutePath(),
                                "Image Load Error", JOptionPane.ERROR_MESSAGE);
                        LOGGER.error("Failed to load icon.", e);
                    }
                }
            }
        }
    }

    /**
     * Adds the selected to favorites.
     */
    private void addSelectedToFavorites()
    {
        Set<IconRecord> recordSet = myChooserPanel.getSelectedIcons();
        if (recordSet.isEmpty())
        {
            JOptionPane.showMessageDialog(this, "There are currently no icons selected.\nSelect at least one icon and try again.",
                    "No Icons Selected Warning", JOptionPane.WARNING_MESSAGE);
        }
        else
        {
            List<IconProvider> providerList = New.list(recordSet.size());
            for (IconRecord rec : recordSet)
            {
                DefaultIconProvider provider = new DefaultIconProvider(rec.getImageURL(), IconRecord.FAVORITES_COLLECTION, null,
                        "User");
                providerList.add(provider);
            }
            MantleToolboxUtils.getMantleToolbox(myToolbox).getIconRegistry().addIcons(providerList, this);
        }
    }

    /**
     * Creates the icon popup menu items, and adds the new items to the supplied
     * menu.
     *
     * @param puMenu the popup menu in which the items will be added.
     */
    private void createIconPopupMenuItems(JPopupMenu puMenu)
    {
        getMenuItems().forEach(button -> puMenu.add(button));

        JMenuItem deleteSelectedMI = new JMenuItem("Delete Selected");
        deleteSelectedMI.addActionListener(e -> deleteSelected());
        puMenu.add(deleteSelectedMI);
    }

    /**
     * Creates the menu bar menu items, adding the newly created items to the
     * {@link #myEditMenu}.
     */
    private void createMenuBarEditMenuItems()
    {
        getMenuItems().forEach(button -> myEditMenu.add(button));

        JMenuItem deleteSelectedTreeNodeMI = new JMenuItem("Delete Selected Icon Set");
        deleteSelectedTreeNodeMI.addActionListener(e -> deleteSelectedTreeNode());
        myEditMenu.add(deleteSelectedTreeNodeMI);
    }

    /**
     * Creates the menu bar and popup buttons for
     * {@link #createMenuBarFileMenuItems()}
     * {@link #createIconPopupMenuItems(JPopupMenu)}.
     *
     * @return buttonList
     */
    private List<JMenuItem> getMenuItems()
    {
        JMenuItem addToFavoritesMI = new JMenuItem("Add Selected To Favorites");
        addToFavoritesMI.addActionListener(e -> addSelectedToFavorites());

        JMenuItem rotateSelectedMI = new JMenuItem("Rotate Selected");
        rotateSelectedMI.addActionListener(e -> rotateSelected());

        JMenuItem deSelectAllMI = new JMenuItem("De-Select All");
        deSelectAllMI.addActionListener(e -> deselectAll());

        return New.list(addToFavoritesMI, rotateSelectedMI, deSelectAllMI);
    }

    /**
     * Creates the menu bar file menu items, adding the newly created items to
     * the {@link #myFileMenu}.
     */
    private void createMenuBarFileMenuItems()
    {
        JMenuItem addIconFromFile = new JMenuItem("Add Icon From File");
        addIconFromFile.addActionListener(e -> addIconFromFile());
        myFileMenu.add(addIconFromFile);

        JMenuItem addIconsFromFolder = new JMenuItem("Add Icons From Folder");
        addIconsFromFolder.addActionListener(e -> addIconsFromFolder());
        myFileMenu.add(addIconsFromFolder);
    }

    /**
     * Creates the tree popup menu items, adding each to the supplied menu.
     *
     * @param puMenu the popup menu to which the items will be added.
     */
    private void createTreePopupMenuItems(JPopupMenu puMenu)
    {
        JMenuItem deleteSelectedTreeNodeMI = new JMenuItem("Delete Selected");
        deleteSelectedTreeNodeMI.addActionListener(e -> deleteSelectedTreeNode());
        puMenu.add(deleteSelectedTreeNodeMI);
    }

    /**
     * Delete selected.
     */
    private void deleteSelected()
    {
        Set<IconRecord> recordSet = myChooserPanel.getSelectedIcons();
        if (recordSet.isEmpty())
        {
            JOptionPane.showMessageDialog(this, "There are currently no icons selected.\nSelect at least one icon and try again.",
                    "No Icons Selected Warning", JOptionPane.WARNING_MESSAGE);
        }
        else
        {
            int result = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to delete all the selected icons?\n"
                            + "\nThe icon files on your computer will not be deleted \n"
                            + "but the application will no longer remember them.",
                    "Delete Icon Confirmation", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (result == JOptionPane.YES_OPTION)
            {
                TIntList idList = new TIntArrayList(recordSet.size());
                for (IconRecord rec : recordSet)
                {
                    idList.add(rec.getId());
                }
                MantleToolboxUtils.getMantleToolbox(myToolbox).getIconRegistry().removeIcons(idList, this);
            }
        }
    }

    /**
     * Shows the icon rotation dialog.
     */
    private void rotateSelected()
    {
        Set<IconRecord> recordSet = myChooserPanel.getSelectedIcons();
        if (recordSet.size() == 1)
        {
            IconRecord record = recordSet.iterator().next();
            IconRotationDialog dialog = new IconRotationDialog(this, record, myIconRegistry, myChooserPanel);
            dialog.setVisible(true);
        }
        else if (recordSet.isEmpty())
        {
            JOptionPane.showMessageDialog(this, "There are currently no icons selected.\nSelect at least one icon and try again.",
                    "No Icons Selected Warning", JOptionPane.WARNING_MESSAGE);
        }
        else
        {
            JOptionPane.showMessageDialog(this, "Please select only one icon and try again.", "Multiple Icons Selected Warning",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    /**
     * Delete selected tree node.
     */
    private void deleteSelectedTreeNode()
    {
        final IconRecordTreeNodeUserObject obj = myChooserPanel.getLastSelectedTreeNodeUserObject();
        if (obj == null)
        {
            JOptionPane.showMessageDialog(this,
                    "There is currently no tree node selected.\nSelect at least one node and try again.",
                    "No Tree Node Selected Warning", JOptionPane.PLAIN_MESSAGE);
        }
        else
        {
            int result = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to delete the selected icon set \"" + obj.getLabel()
                            + "\"?\n\nThe icon files on your computer will not be deleted but the application \n"
                            + "will no longer remember them.",
                    "Delete Icon Set Confirmation", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (result == JOptionPane.YES_OPTION)
            {
                TIntList iconIdList = null;
                if (obj.getNameType() == NameType.COLLECTION)
                {
                    iconIdList = myIconRegistry
                            .getIconIds(value -> EqualsHelper.equals(value.getCollectionName(), obj.getLabel()));
                }
                else
                {
                    // Subcategory.
                    iconIdList = myIconRegistry.getIconIds(value -> EqualsHelper.equals(value.getSubCategory(), obj.getLabel()));
                }
                if (!iconIdList.isEmpty())
                {
                    myIconRegistry.removeIcons(iconIdList, this);
                }
            }
        }
    }

    /**
     * Deselect all.
     */
    private void deselectAll()
    {
        myChooserPanel.clearSelection();
    }

    /**
     * Gets the collection name from user.
     *
     * @return the collection name from user
     */
    private String getCollectionNameFromUser()
    {
        String resultCollectionName = null;
        IconCollectionNamePanel pnl = new IconCollectionNamePanel(myIconRegistry.getCollectionNames());

        boolean done = false;
        while (!done)
        {
            int result = JOptionPane.showConfirmDialog(this, pnl, "Collection Name Selection", JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE);
            if (result == JOptionPane.OK_OPTION)
            {
                String colName = pnl.getCollectionName();
                if (StringUtils.isBlank(colName))
                {
                    JOptionPane.showMessageDialog(this, "The collection name can not be blank.", "Collection Name Error",
                            JOptionPane.PLAIN_MESSAGE);
                }
                else
                {
                    resultCollectionName = colName;
                    done = true;
                }
            }
            else
            {
                done = true;
            }
        }
        return resultCollectionName;
    }

    /**
     * Gets the sub category from user.
     *
     * @param collectionName the collection name
     * @param subCatsFromDirName the sub cats from dir name
     * @return the sub category from user ( null if cancelled )
     */
    private SubCategoryPanel getSubCategoryFromUser(String collectionName, boolean subCatsFromDirName)
    {
        SubCategoryPanel pnl = new SubCategoryPanel(myIconRegistry.getSubCategoiresForCollection(collectionName),
                subCatsFromDirName);

        boolean done = false;
        boolean cancelled = false;
        while (!done)
        {
            int result = JOptionPane.showConfirmDialog(this, pnl, "Sub-Category Selection", JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE);
            if (result == JOptionPane.OK_OPTION)
            {
                String colName = pnl.getCategory();
                if (!pnl.isSubCatsFromDirNames() && !pnl.isNoCategory() && StringUtils.isBlank(colName))
                {
                    JOptionPane.showMessageDialog(this, "The sub-category name can not be blank.", "Sub-category Name Error",
                            JOptionPane.PLAIN_MESSAGE);
                }
                else
                {
                    done = true;
                }
            }
            else
            {
                done = true;
                cancelled = true;
            }
        }
        return cancelled ? null : pnl;
    }
}
