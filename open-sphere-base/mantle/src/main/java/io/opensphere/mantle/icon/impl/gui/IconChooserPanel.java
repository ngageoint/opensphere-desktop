package io.opensphere.mantle.icon.impl.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.jdesktop.swingx.JXBusyLabel;

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import io.opensphere.core.Toolbox;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.WeakChangeSupport;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.concurrent.EventQueueExecutor;
import io.opensphere.core.util.image.ImageUtil;
import io.opensphere.core.util.swing.EventQueueUtilities;
import io.opensphere.core.util.swing.JTreeUtilities;
import io.opensphere.mantle.icon.IconRecord;
import io.opensphere.mantle.icon.IconRecordTreeNodeUserObject;
import io.opensphere.mantle.icon.IconRegistry;
import io.opensphere.mantle.icon.impl.DefaultIconProvider;
import io.opensphere.mantle.icon.impl.gui.IconChooserPanel.BuildIconGridWorker;
import io.opensphere.mantle.util.MantleToolboxUtils;

/**
 * The Class IconChooserPanel.
 */
@SuppressWarnings("PMD.GodClass")
public class IconChooserPanel extends JPanel implements TreeSelectionListener
{
    /** The Constant ICON_SELECTED. */
    private static final String ICON_SELECTED = "ICON_SELECTED";

    /** The Constant SELECTION_CHANGED. */
    private static final String SELECTION_CHANGED = "SELECTION_CHANGED";

    /**
     * serialVersionUID.
     */
    private static final long serialVersionUID = 1L;

    /** The Busy label. */
    private JXBusyLabel myBusyLabel;

    /** The Change support. */
    private final WeakChangeSupport<ActionListener> myChangeSupport;

    /** The Grid panel. */
    private JPanel myGridPanel;

    /** The Popup menu. */
    private final JPopupMenu myIconPopupMenu;

    /** The Icon registry. */
    private final IconRegistry myIconRegistry;

    /** The Popup record. */
    private IconRecord myLastFocusRecord;

    /** The Last selected tree node user object. */
    private IconRecordTreeNodeUserObject myLastSelectedTreeNodeUserObject;

    /** The Loader. */
    private Thread myLoader;

    /** The Loading panel. */
    private JPanel myLoadingPanel;
    
    /** The Multi select. */
    private final boolean myMultiSelect;

    /** The Resize timer. */
    private final Timer myResizeTimer;

    /** The Root tree node. */
    @SuppressWarnings("PMD.SingularField")
    private TreeNode myRootTreeNode;

    /** The Selected buttons. */
    private final Set<RecButton> mySelectedButtons;

    /** The Last selected record. */
    private IconRecord mySelectedRecord;

    /** The Selected records. */
    private final Set<IconRecord> mySelectedRecords;

    /** The Show add icon button. */
    private final boolean myShowAddIconButton;

    /** The Toolbox. */
    private final Toolbox myToolbox;

    /** The Tree. */
    private JTree myTree;

    /** The Tree model. */
    private DefaultTreeModel myTreeModel;

    /** The Tree panel. */
    @SuppressWarnings("PMD.SingularField")
    private JPanel myTreePanel;

    /** The Popup menu. */
    private final JPopupMenu myTreePopupMenu;

    /** Button to open the Icon Builder. */
    private final JButton myIconBuilderButton;

    /** The optional selected icon URL. */
    private String mySelectedUrl;

    /** The icon display width */
	public int myTileWidth;
	
	 /** The list of icons being resized */
	public List<IconRecord> ResizeRec = New.list();

    
    /**
     * Instantiates a new icon chooser panel.
     *
     * @param tb the {@link Toolbox}
     * @param isMultiSelect the is multi select
     * @param showAddIconButton the show add icon button
     * @param iconPopupMenu the icon popup menu
     * @param treePopupMenu the tree popup menu ( optional, null means none)
     * @param iconBuilderButton button
     */
    
    public IconChooserPanel(Toolbox tb, boolean isMultiSelect, boolean showAddIconButton, JPopupMenu iconPopupMenu,
            JPopupMenu treePopupMenu, JButton iconBuilderButton)
    {
        myToolbox = tb;
        myIconRegistry = MantleToolboxUtils.getMantleToolbox(myToolbox).getIconRegistry();
        myMultiSelect = isMultiSelect;
        myIconPopupMenu = iconPopupMenu;
        myTreePopupMenu = treePopupMenu;
        myIconBuilderButton = iconBuilderButton;
        myShowAddIconButton = showAddIconButton;
        myChangeSupport = new WeakChangeSupport<>();
        mySelectedRecords = New.set();
        mySelectedButtons = New.set();

        myResizeTimer = new Timer(200, e -> valueChanged(null));
        myResizeTimer.setRepeats(false);
    }
   

	/**
     * Sets the selected icon URL.
     *
     * @param selectedUrl the icon URL
     */
    public void setSelectedUrl(String selectedUrl)
    {
        mySelectedUrl = selectedUrl;
    }

    /**
     * Adds the action listener.
     *
     * Note: The {@link ActionListener} is held as a weak reference.
     *
     * @param al the al
     */
    public void addActionListener(ActionListener al)
    {
        myChangeSupport.addListener(al);
    }

    @Override
    public void addNotify()
    {
        super.addNotify();

        setLayout(new BorderLayout());

        myTreePanel = new JPanel(new BorderLayout());
        myTreePanel.setMinimumSize(new Dimension(200, 500));
        myTreeModel = new DefaultTreeModel(new DefaultMutableTreeNode());
        myTree = new JTree();
        myTree.setRootVisible(false);
        myTree.setExpandsSelectedPaths(true);

        JScrollPane treeSP = new JScrollPane(myTree);
        myTreePanel.add(treeSP, BorderLayout.CENTER);

        JSplitPane jsp = new JSplitPane();
        jsp.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
        jsp.setLeftComponent(myTreePanel);

        myGridPanel = new JPanel(new BorderLayout());
        JScrollPane gridSP = new JScrollPane(myGridPanel);
        jsp.setRightComponent(gridSP);

        add(jsp, BorderLayout.CENTER);

        myTree.addTreeSelectionListener(this);
        myTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

        if (myTreePopupMenu != null)
        {
            myTree.setComponentPopupMenu(myTreePopupMenu);
        }

        createLoadingPanel();

        if (myShowAddIconButton)
        {
            JButton addFromFile = new JButton("Add Icon From File");
            addFromFile.addActionListener(e -> loadFromFile(IconRecord.USER_ADDED_COLLECTION, null));

            JPanel buttonPanel = new JPanel(new BorderLayout());
            if (myIconBuilderButton != null)
            {
                buttonPanel.add(myIconBuilderButton, BorderLayout.NORTH);
            }
            buttonPanel.add(addFromFile, BorderLayout.SOUTH);
            myTreePanel.add(buttonPanel, BorderLayout.SOUTH);
        }

        refreshFromRegistry(null);

        addComponentListener(new ComponentAdapter()
        {
            @Override
            public void componentResized(ComponentEvent e)
            {
                myResizeTimer.restart();
            }
        });
    }

    /**
     * Clear selection.
     */
    public void clearSelection()
    {
        if (!mySelectedRecords.isEmpty())
        {
            final List<RecButton> btList = New.list(mySelectedButtons);
            EventQueueUtilities.runOnEDT(() ->
            {
                for (RecButton bt : btList)
                {
                    bt.setSelectionUIState(false);
                }
            });
            mySelectedButtons.clear();
            mySelectedRecords.clear();
            fireActionPerformed(new ActionEvent(this, 0, SELECTION_CHANGED));
        }
    }

    /**
     * Gets the last focus record.
     *
     * @return the last focus record
     */
    public IconRecord getLastPopupTriggerIconRecord()
    {
        return myLastFocusRecord;
    }

    /**
     * Gets the last selected tree node user object.
     *
     * @return the last selected tree node user object
     */
    public IconRecordTreeNodeUserObject getLastSelectedTreeNodeUserObject()
    {
        return myLastSelectedTreeNodeUserObject;
    }

    /**
     * Gets the selected icon.
     *
     * @return the selected icon
     */
    public IconRecord getSelectedIcon()
    {
        return mySelectedRecord;
    }

    /**
     * Gets the selected icons.
     *
     * @return the selected icons
     */
    public Set<IconRecord> getSelectedIcons()
    {
        if (myMultiSelect)
        {
            return Collections.unmodifiableSet(New.set(mySelectedRecords));
        }
        return Collections.singleton(mySelectedRecord);
    }

    /**
     * Checks for selected icons.
     *
     * @return true, if successful
     */
    public boolean hasSelectedIcons()
    {
        if (myMultiSelect)
        {
            return !mySelectedRecords.isEmpty();
        }
        return mySelectedRecord != null;
    }

    /**
     * Checks if is multi select.
     *
     * @return true, if is multi select
     */
    public boolean isMultiSelect()
    {
        return myMultiSelect;
    }

    /**
     * Load from file.
     *
     * @param collectionName the collection name
     * @param subCatName the sub cat name
     */
    public void loadFromFile(String collectionName, String subCatName)
    {
        File result = ImageUtil.showImageFileChooser("Choose Icon File", this, myToolbox.getPreferencesRegistry());

        if (result != null)
        {
            try
            {
                myIconRegistry.addIcon(new DefaultIconProvider(result.toURI().toURL(), collectionName, subCatName, "User"), this);
                refreshFromRegistry(collectionName);
            }
            catch (MalformedURLException e)
            {
                JOptionPane.showMessageDialog(this, "Failed to load image: " + result.getAbsolutePath(), "Image Load Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Refresh from registry.
     *
     * @param collectionToShow the name of the collection to show, or null
     */
    public final void refreshFromRegistry(String collectionToShow)
    {
        EventQueueUtilities.runOnEDT(new Runnable()
        {
            @Override
            public void run()
            {
                if (myTree != null)
                {
                    myLastSelectedTreeNodeUserObject = null;
                    myRootTreeNode = new IconTreeBuilder(myIconRegistry).getIconRecordTree(null);
                    if (myTreeModel == null)
                    {
                        myTreeModel = new DefaultTreeModel(myRootTreeNode);
                    }
                    else
                    {
                        myTreeModel.setRoot(myRootTreeNode);
                    }
                    myTree.setModel(myTreeModel);
                    myTree.revalidate();
                    JTreeUtilities.expandOrCollapseAll(myTree, true);

                    TreeNode nodeToSelect = getNodeToSelect(collectionToShow);
                    if (nodeToSelect != null)
                    {
                        TreeNode[] nodeArray = myTreeModel.getPathToRoot(nodeToSelect);
                        TreePath path = new TreePath(nodeArray);
                        myTree.getSelectionModel().setSelectionPath(path);
                    }
                }
            }
        });
    }

    /**
     * Gets the node to select.
     *
     * @param collectionToShow the requested collection to show
     * @return the node to select, or null
     */
    private TreeNode getNodeToSelect(String collectionToShow)
    {
        TreeNode nodeToSelect = null;

        // If there is a requested collection to show, find its node
        if (collectionToShow != null)
        {
            for (int i = 0; i < myRootTreeNode.getChildCount(); i++)
            {
                if (collectionToShow.equals(myRootTreeNode.getChildAt(i).toString()))
                {
                    nodeToSelect = myRootTreeNode.getChildAt(i);
                    break;
                }
            }
        }

        if (nodeToSelect == null && myRootTreeNode.getChildCount() > 0)
        {
            // If there is a selected icon, find its node
            if (mySelectedUrl != null)
            {
                for (int i = 0; i < myRootTreeNode.getChildCount(); i++)
                {
                    TreeNode child = myRootTreeNode.getChildAt(i);
                    if (child instanceof DefaultMutableTreeNode)
                    {
                        DefaultMutableTreeNode mtn = (DefaultMutableTreeNode)child;
                        Object userObj = mtn.getUserObject();
                        if (userObj instanceof IconRecordTreeNodeUserObject)
                        {
                            IconRecordTreeNodeUserObject irNode = (IconRecordTreeNodeUserObject)userObj;
                            boolean hasIcon = irNode.getRecords(true).stream()
                                    .anyMatch(r -> mySelectedUrl.equals(r.getImageURL().toString()));
                            if (hasIcon)
                            {
                                nodeToSelect = mtn;
                                break;
                            }
                        }
                    }
                }
            }
            // Default to the first one
            else
            {
                nodeToSelect = myRootTreeNode.getChildAt(0);
            }
        }
        return nodeToSelect;
    }

    /**
     * Removes the action listener.
     *
     * @param al the al
     */
    public void removeActionListener(ActionListener al)
    {
        myChangeSupport.removeListener(al);
    }

    @Override
    public void valueChanged(TreeSelectionEvent e)
    {
        if (!mySelectedRecords.isEmpty())
        {
            mySelectedRecords.clear();
            mySelectedButtons.clear();
            fireActionPerformed(new ActionEvent(this, 0, SELECTION_CHANGED));
        }
        TreePath path = myTree.getSelectionPath();
        List<IconRecord> recList = null;
        myLastSelectedTreeNodeUserObject = null;
        if (path != null)
        {
            Object lastPathComp = path.getLastPathComponent();
            if (lastPathComp instanceof DefaultMutableTreeNode)
            {
                DefaultMutableTreeNode mtn = (DefaultMutableTreeNode)lastPathComp;
                Object userObj = mtn.getUserObject();
                if (userObj instanceof IconRecordTreeNodeUserObject)
                {
                    IconRecordTreeNodeUserObject irNode = (IconRecordTreeNodeUserObject)userObj;
                    myLastSelectedTreeNodeUserObject = irNode;
                    recList = irNode.getRecords(true);
                }
            }
        }
        recList = recList == null ? Collections.<IconRecord>emptyList() : recList;;
        displayIconRecords(recList, true);
        ResizeRec = recList;
    }

    /**
     * Fire action performed.
     *
     * @param event the event
     */
    protected void fireActionPerformed(final ActionEvent event)
    {
        myChangeSupport.notifyListeners(listener -> listener.actionPerformed(event), new EventQueueExecutor());
    }

    /**
     * Adds the to selection.
     *
     * @param button the button
     */
    private void addToSelection(RecButton button)
    {
        if (mySelectedRecords.add(button.getRecord()))
        {
            mySelectedButtons.add(button);
            fireActionPerformed(new ActionEvent(this, 0, SELECTION_CHANGED));
        }
    }

    /**
     * Button selected.
     *
     * @param bt the bt
     */
    private void buttonSelected(RecButton bt)
    {
        mySelectedRecord = bt.getRecord();
        fireActionPerformed(new ActionEvent(this, 0, ICON_SELECTED));
    }

    /**
     * Creates the loading panel.
     */
    private void createLoadingPanel()
    {
        myLoadingPanel = new JPanel(new BorderLayout());
        JPanel subPanel = new JPanel();
        subPanel.setLayout(new BoxLayout(subPanel, BoxLayout.X_AXIS));
        subPanel.add(Box.createHorizontalGlue());
        Dimension dim = new Dimension(100, 100);
        myBusyLabel = new JXBusyLabel(dim);
        myBusyLabel.setMinimumSize(dim);
        myBusyLabel.setMaximumSize(dim);
        myBusyLabel.setPreferredSize(dim);
        subPanel.add(myBusyLabel);
        subPanel.add(Box.createHorizontalGlue());

        JPanel subPanel2 = new JPanel();
        subPanel2.setLayout(new BoxLayout(subPanel2, BoxLayout.Y_AXIS));
        subPanel2.add(Box.createVerticalGlue());
        subPanel2.add(subPanel);
        JPanel labelPanel = new JPanel(new BorderLayout());
        labelPanel.setMaximumSize(new Dimension(10000, 30));
        JLabel loadLabel = new JLabel("Loading Images");
        loadLabel.setHorizontalAlignment(SwingConstants.CENTER);
        loadLabel.setFont(loadLabel.getFont().deriveFont(Font.BOLD, loadLabel.getFont().getSize() + 8));
        labelPanel.add(loadLabel, BorderLayout.CENTER);
        subPanel2.add(labelPanel);
        subPanel2.add(Box.createVerticalGlue());
        myLoadingPanel.add(subPanel2, BorderLayout.CENTER);   
    }

    /**
     * Display icon records.
     *
     * @param recList the rec list
     * @param canCancel the can cancel
     */
    public void displayIconRecords(final List<IconRecord> recList, boolean canCancel)
    {
        EventQueueUtilities.runOnEDT(() ->
        {
            myGridPanel.removeAll();
            myBusyLabel.setBusy(true);
            myGridPanel.add(myLoadingPanel, BorderLayout.CENTER);
            myGridPanel.revalidate();
            myGridPanel.repaint();
        });

        if (myLoader != null)
        {
            myLoader.interrupt();
        }
        myLoader = new Thread(new BuildIconGridWorker(recList, canCancel));
        myLoader.start();
    }
    
	/**
     * Shows resizes icons and displays loading screen until done.
     */
	public void IconResizer(List<IconRecord> recList,int tileWidth,boolean canCancel) {
	
		myTileWidth = tileWidth; 
        myLoader = new Thread(new BuildIconGridWorker(recList, canCancel));
        myLoader.start();
        EventQueueUtilities.runOnEDT(() ->
        {
            myGridPanel.removeAll();
            myBusyLabel.setBusy(true);
            myGridPanel.add(myLoadingPanel, BorderLayout.CENTER);
            myGridPanel.revalidate();
            myGridPanel.repaint();
        });
 
	}

    /**
     * Removes the from selection.
     *
     * @param button the button
     */
    private void removeFromSelection(RecButton button)
    {
        if (mySelectedRecords.remove(button.getRecord()))
        {
            mySelectedButtons.remove(button);
            fireActionPerformed(new ActionEvent(this, 0, SELECTION_CHANGED));
        }
    }

    /**
     * Sets the last focus record.
     *
     * @param rec the new last focus record
     */
    private void setLastFocusRecord(IconRecord rec)
    {
        if (!Utilities.sameInstance(myLastFocusRecord, rec))
        {
            myLastFocusRecord = rec;
        }
    }

    /**
     * The Class BuildIconGridWorker.
     */
    public class BuildIconGridWorker implements Runnable
    {
        /** The Can interrupt. */
        private final boolean myCanInterrupt;

        /** The Interrupted. */
        private boolean myInterrupted;

        /** The Rec list. */
        public final List<IconRecord> myRecList;

        /**
         * Instantiates a new builds the icon grid worker.
         *
         * @param recList the rec list
         * @param canCancel the can cancel
         */
        public BuildIconGridWorker(List<IconRecord> recList, boolean canCancel)
        {
            myRecList = recList;
            myCanInterrupt = canCancel;
            ResizeRec = recList;
        }

		@Override
        public void run()
        {
            int borderSize = 6;
            if (myTileWidth == 0) {
            		myTileWidth +=100;
            }
            int iconWidth =  (int) (myTileWidth - borderSize);
            int width = myGridPanel.getWidth();
            if (width < 0 || width > 5000)
            {
                width = 400;
            }

            int height = myGridPanel.getHeight();
            if (height < 0 || height > 5000)
            {
                height = 400;
            }
            int numIconRowsInView = (int)Math.ceil((double)height / (double)myTileWidth);
            JPanel grid = new JPanel();
            if (!isInterrupted() && !myRecList.isEmpty())
            {
                List<RecordImageIcon> imIcList = buildImageList(iconWidth);

                if (!isInterrupted())
                {
                    int numIconsPerRow = myTileWidth > width ? 1 : (int)Math.floor((double)width / (double)myTileWidth);
                    int numRows = (int)Math.ceil((double)imIcList.size() / (double)numIconsPerRow);
                    grid = new JPanel(new GridLayout(numRows < numIconRowsInView ? numIconRowsInView : numRows, numIconsPerRow,
                            borderSize, borderSize));
                    Dimension size = new Dimension(iconWidth, iconWidth);

                    for (int i = 0; i < imIcList.size() && !isInterrupted(); i++)
                    {
                        RecordImageIcon rec = imIcList.get(i);
                        JPanel imageBT = buildRecButton(size, rec);
                        grid.add(imageBT);
                    }
                    int blankGridLements = numIconsPerRow * numIconRowsInView - imIcList.size();
                    for (int i = 0; i < blankGridLements; i++)
                    {
                        grid.add(new JPanel());
                    }
                }
            }
            if (!isInterrupted())
            {
                final JPanel fGrid = grid;
                EventQueueUtilities.runOnEDT(() ->
                {
                    myBusyLabel.setBusy(false);
                    myGridPanel.removeAll();
                    myGridPanel.add(fGrid, BorderLayout.CENTER);
                    myGridPanel.revalidate();
                    myGridPanel.repaint();
                });
            }
        }

        /**
         * Checks if is interrupted.
         *
         * @return true, if is interrupted
         */
        boolean isInterrupted()
        {
            if (!myInterrupted && myCanInterrupt)
            {
                myInterrupted = Thread.interrupted();
            }
            return myInterrupted;
        }

        /**
         * Builds the image list.
         *
         * @param iconWidth the icon width
         * @return the list
         */
        private List<RecordImageIcon> buildImageList(int iconWidth)
        {
            List<RecordImageIcon> icons = New.list(myRecList.size());
            TIntList brokenIconIds = new TIntArrayList();
            for (IconRecord record : myRecList)
            {
                RecordImageIcon icon = loadImage(record, iconWidth);
                if (icon != null)
                {
                    icons.add(icon);
                }
                else
                {
                    brokenIconIds.add(record.getId());
                }

                if (isInterrupted())
                {
                    break;
                }
            }

            if (!brokenIconIds.isEmpty())
            {
                myIconRegistry.removeIcons(brokenIconIds, this);
            }

            return icons;
        }

        /**
         * Builds the rec button.
         *
         * @param size the size
         * @param rec the rec
         * @return the rec button
         */
        private JPanel buildRecButton(Dimension size, RecordImageIcon rec)
        {
            JPanel recBTPanel = new JPanel(new BorderLayout());
            RecButton imageBT = new RecButton(rec, myIconPopupMenu);
            recBTPanel.setMinimumSize(size);
            recBTPanel.setMaximumSize(size);
            recBTPanel.setPreferredSize(size);

            JLabel nameLB = new JLabel(rec.getRecord().getName());
            recBTPanel.add(nameLB, BorderLayout.SOUTH);

            String urlStr = rec.getRecord().getImageURL().toString();
            imageBT.setToolTipText(urlStr);
            recBTPanel.add(imageBT, BorderLayout.CENTER);
            return recBTPanel;
        }

        /**
         * Loads an image.
         *
         * @param record the icon record
         * @param iconWidth the icon width
         * @return the record image icon, or null if it couldn't be loaded
         */
        private RecordImageIcon loadImage(IconRecord record, int iconWidth)
        {
            BufferedImage image;
            try
            {
                image = ImageIO.read(record.getImageURL());
            }
            catch (IOException e)
            {
                image = null;
            }

            RecordImageIcon icon = null;
            if (image != null)
            {
                Image scaledImage = ImageUtil.scaleDownImage(image, iconWidth, iconWidth - 20);
                icon = new RecordImageIcon(scaledImage, record);
            }
            return icon;
        }
    }

    /**
     * The Class RecButton.
     */
    private class RecButton extends JButton
    {
        /**
         * serialVersionUID.
         */
        private static final long serialVersionUID = 1L;

        /** The Rec image icon. */
        private final RecordImageIcon myRecImageIcon;

        /** The Selected. */
        private boolean mySelected;

        /**
         * Instantiates a new rec button.
         *
         * @param recII the rec ii
         * @param popupMu the popup mu
         */
        public RecButton(RecordImageIcon recII, JPopupMenu popupMu)
        {
            super(recII);
            setFocusable(false);

            myRecImageIcon = recII;
            addActionListener(e ->
            {
                if (!myMultiSelect)
                {
                    buttonSelected(RecButton.this);
                }
            });
            addMouseListener(new MouseAdapter()
            {
                @Override
                public void mouseClicked(MouseEvent e)
                {
                    if (e.getButton() == MouseEvent.BUTTON1 && myMultiSelect)
                    {
                        mySelected = !mySelected;
                        if (mySelected)
                        {
                            ((JPanel)getParent()).setBorder(BorderFactory.createLineBorder(Color.white));
                            addToSelection(RecButton.this);
                        }
                        else
                        {
                            ((JPanel)getParent()).setBorder(BorderFactory.createEmptyBorder());
                            removeFromSelection(RecButton.this);
                        }
                    }
                }

                @Override
                public void mouseEntered(MouseEvent e)
                {
                    setLastFocusRecord(getRecord());
                }

                @Override
                public void mousePressed(MouseEvent e)
                {
                    if (e.getButton() == MouseEvent.BUTTON3 && myMultiSelect && !mySelected)
                    {
                        mySelected = true;
                        ((JPanel)getParent()).setBorder(BorderFactory.createLineBorder(Color.white));
                        addToSelection(RecButton.this);
                    }
                }
            });
            if (popupMu != null)
            {
                setComponentPopupMenu(popupMu);
            }
        }

        /**
         * Gets the record.
         *
         * @return the record
         */
        public IconRecord getRecord()
        {
            return myRecImageIcon.getRecord();
        }

        /**
         * Sets the selection ui state.
         *
         * @param selected the new selection ui state
         */
        public void setSelectionUIState(boolean selected)
        {
            mySelected = selected;
            ((JPanel)getParent())
                    .setBorder(selected ? BorderFactory.createLineBorder(Color.white) : BorderFactory.createEmptyBorder());
        }
    }

    /** An ImageIcon with associated IconRecord. */
    private static class RecordImageIcon extends ImageIcon
    {
        /** Serial version UID. */
        private static final long serialVersionUID = 1L;

        /** The record. */
        private final IconRecord myRecord;

        /**
         * Constructor.
         *
         * @param image the image
         * @param record the icon record
         */
        public RecordImageIcon(Image image, IconRecord record)
        {
            super(image);
            myRecord = record;
        }

        /**
         * Gets the record.
         *
         * @return the record
         */
        public IconRecord getRecord()
        {
            return myRecord;
        }
    }


}
