package io.opensphere.mantle.iconproject.panels;

import java.awt.EventQueue;
import java.awt.Window;
import java.io.File;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.AnchorPane;
import io.opensphere.core.util.fx.JFXDialog;
import io.opensphere.core.util.image.ImageUtil;
import io.opensphere.mantle.icon.IconProvider;
import io.opensphere.mantle.icon.IconRecord;
import io.opensphere.mantle.icon.impl.DefaultIconProvider;
import io.opensphere.mantle.iconproject.impl.ButtonBuilder;
import io.opensphere.mantle.iconproject.model.PanelModel;
import io.opensphere.mantle.iconproject.view.AddIconDialog;
import io.opensphere.mantle.iconproject.view.IconProjGenDialog;
import io.opensphere.mantle.iconproject.view.TreePopupMenu;

/**
 * The Main Panel in the Icon Manager UI comprised of the tree hierarchy and
 * icon display window.
 */
public class MainPanel extends SplitPane
{
    /** The Model. */
    private PanelModel myPanelModel;

    /** The Icon Display Grid. */
    private GridBuilder myIconGrid;

    /** The Customize Icon button. */
    private final ButtonBuilder myCustomizeIconButton = new ButtonBuilder("Customize Icon", false);

    /** The button to add the icon. */
    private final MenuButton myAddIconButton = new MenuButton("Add Icon From");

    /** The button to generate a new icon. */
    private final ButtonBuilder myGenerateIconButton = new ButtonBuilder("Generate New Icon", false);

    /** The tree view. */
    private TreeView<String> myTreeView;

    /** The left Panel. */
    private AnchorPane myLeftView = new AnchorPane();

    /** The treeBuilder object. */
    private TreeBuilder myTreeBuilder;

    /** The map of collection name keys and icon record list values. */
    private Map<String, List<IconRecord>> myRecordMap = new HashMap<>();

    /** The main panel's scroll pane which contains the grid of icons. */
    private ScrollPane myScrollPane;

    /** The owner window of the main panel. */
    private final Window myOwner;

    /**
     * The MainPanel constructor.
     *
     * @param panelModel the model for the main panel
     */
    public MainPanel(PanelModel panelModel)
    {
        myPanelModel = panelModel;
        myOwner = myPanelModel.getOwner();

        myPanelModel.getCurrentTileWidth().addListener((o, v, m) -> refresh());
        createTreeView(myPanelModel.getIconRegistry().getManagerPrefs().getTreeSelection());
        myRecordMap = new HashMap<>(myTreeBuilder.getRecordMap());
        myPanelModel.setRecordList(myRecordMap.get("Default"));
        myIconGrid = new GridBuilder(myPanelModel);

        AnchorPane.setLeftAnchor(myAddIconButton, 0.);
        AnchorPane.setRightAnchor(myAddIconButton, 0.);
        AnchorPane.setBottomAnchor(myAddIconButton, 52.0);

        MenuItem fileOption = new MenuItem("File");
        MenuItem folderOption = new MenuItem("Folder");
        myAddIconButton.getItems().addAll(fileOption, folderOption);
        myAddIconButton.setAlignment(Pos.CENTER);
        fileOption.setOnAction(event -> EventQueue.invokeLater(() -> loadFromFile(myPanelModel.getImportProps().getCollectionName().get(),
                    myPanelModel.getImportProps().getSubCollectionName().get())));

        folderOption.setOnAction(event -> EventQueue.invokeLater(() -> addIconsFromFolder()));

        AnchorPane.setBottomAnchor(myCustomizeIconButton, 26.0);
        myCustomizeIconButton.lockButton(myCustomizeIconButton);
        myCustomizeIconButton.setOnAction(event -> EventQueue.invokeLater(() -> myIconGrid.showIconCustomizer(myOwner)));

        AnchorPane.setBottomAnchor(myGenerateIconButton, 0.0);
        myGenerateIconButton.lockButton(myGenerateIconButton);
        myGenerateIconButton.setOnAction(event -> EventQueue.invokeLater(() ->
            {
                IconProjGenDialog dialog = new IconProjGenDialog(myOwner, myPanelModel.getIconRegistry(), this);
                dialog.setVisible(true);
            }));

        myScrollPane = new ScrollPane(myIconGrid);
        myScrollPane.setPannable(true);
        AnchorPane.setLeftAnchor(myScrollPane, 0.);
        AnchorPane.setRightAnchor(myScrollPane, 0.);
        AnchorPane.setTopAnchor(myScrollPane, 0.);
        AnchorPane.setBottomAnchor(myScrollPane, 0.);
        myScrollPane.setFitToHeight(true);
        myScrollPane.setFitToWidth(true);

        myLeftView.getChildren().addAll(myAddIconButton, myCustomizeIconButton, myGenerateIconButton, myTreeView);
        setResizableWithParent(myLeftView, false);
        getItems().addAll(myLeftView, myScrollPane);
    }

    /**
     * Creates the icon tree hierarchy.
     *
     * @param treeItem the name of an item to add to the tree.
     */
    private void createTreeView(TreeItem<String> treeItem)
    {
        myTreeBuilder = new TreeBuilder(myPanelModel, null);
        myTreeView = new TreeView<>(myTreeBuilder);
        myTreeView.setShowRoot(false);
        myTreeView.setContextMenu(new TreePopupMenu(myPanelModel));
        myPanelModel.getTreeObject().getMyObsTree().set(myTreeView);
        myPanelModel.getTreeObject().getMyObsTree().addListener((o, v, n) -> refreshTree());

        AnchorPane.setBottomAnchor(myTreeView, 78.0);
        AnchorPane.setLeftAnchor(myTreeView, 0.0);
        AnchorPane.setRightAnchor(myTreeView, 0.0);
        AnchorPane.setTopAnchor(myTreeView, 0.0);

        if (treeItem == null)
        {
            myTreeView.getSelectionModel().select(myTreeView.getRow(myTreeView.getTreeItem(0)));
        }
        else if (treeItem.getValue().equals("temp"))
        {
            for (int i = 0; i < myTreeView.getExpandedItemCount(); i++)
            {
                if ((myTreeView.getTreeItem(i).getValue()).equals("Default"))
                {
                    myTreeView.getSelectionModel().select(myTreeView.getRow((myTreeView.getTreeItem(i))));
                    break;
                }
            }
        }
        else
        {
            for (int i = 0; i < myTreeView.getExpandedItemCount(); i++)
            {
                if ((myTreeView.getTreeItem(i).getValue()).equals(treeItem.getValue()))
                {
                    myTreeView.getSelectionModel().select(myTreeView.getRow((myTreeView.getTreeItem(i))));
                    break;
                }
            }
        }
        myTreeView.getSelectionModel().selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> treeHandle(newValue));
    }

    /**
     * Creates a new Tree Hierarchy and Icon Display Grid to reflect changes.
     */
    public void refresh()
    {
        Platform.runLater(() ->
        {
            refreshTree();
            myRecordMap = new HashMap<>(myTreeBuilder.getRecordMap());
            String item = myTreeView.getSelectionModel().getSelectedItem() == null ? myTreeView.getTreeItem(0).getValue() : 
                    myTreeView.getSelectionModel().getSelectedItem().getValue();
            myPanelModel.setRecordList(myRecordMap.get(item));
            myScrollPane.setContent(myIconGrid = new GridBuilder(myPanelModel));
            myPanelModel.getAllSelectedIcons().clear();
            myPanelModel.getSingleSelectedIcon().clear();
        });
    }

    /** Refreshes the Tree Hierarchy. */
    public void refreshTree()
    {
        myLeftView.getChildren().remove(myTreeView);
        createTreeView(myTreeView.getSelectionModel().getSelectedItem());
        myLeftView.getChildren().addAll(myTreeView);
    }

    /**
     * The tree event handler.
     *
     * @param newValue the new clicked-on Value
     */
    private void treeHandle(TreeItem<String> newValue)
    {
        String collectionName = newValue.getValue();
        if (!myPanelModel.getRecordList().equals(myRecordMap.get(collectionName)))
        {
            myPanelModel.getAllSelectedIcons().clear();
        }
        myPanelModel.setRecordList(myRecordMap.get(collectionName));
        myPanelModel.setUseFilteredList(false);
        myScrollPane.setContent(myIconGrid = new GridBuilder(myPanelModel));
        if (myPanelModel.getIconRegistry().getCollectionNames().contains(collectionName))
        {
            myPanelModel.getImportProps().getCollectionName().set(collectionName);
        }
        else
        {
            myPanelModel.getImportProps().getCollectionName().set(myRecordMap.get(collectionName).get(0).getCollectionName());
            myPanelModel.getImportProps().getSubCollectionName().set(collectionName);
        }
    }

    /**
     * Loads a single icon from a file.
     *
     * @param collectionName the collection name
     * @param subCategoryName the sub category name
     */
    public void loadFromFile(String collectionName, String subCategoryName)
    {
        File result = ImageUtil.showImageFileChooser("Choose Icon File", myOwner,
                myPanelModel.getToolbox().getPreferencesRegistry());
        if (result != null)
        {
            try
            {
                IconProvider provider = new DefaultIconProvider(result.toURI().toURL(), collectionName, subCategoryName, "User");
                myPanelModel.getIconRegistry().addIcon(provider, this);
            }
            catch (MalformedURLException e)
            {
                JFXDialog failedDialog = new JFXDialog(myOwner, "Failed to load Icons");
                failedDialog.setFxNode(myScrollPane);
                failedDialog.setVisible(true);
            }
            refresh();
        }
    }

    /**
     * Adds the icons from folder.
     */
    private void addIconsFromFolder()
    {
        AddIconDialog iconImporter = new AddIconDialog(myOwner, myPanelModel);
        iconImporter.setVisible(true);
    }

    /**
     * Gets the current icon display grid.
     *
     * @return the current icon display grid
     */
    public GridBuilder getIconGrid()
    {
        return myIconGrid;
    }

    /**
     * Gets the scroll pane.
     *
     * @return the scroll pane
     */
    public ScrollPane getScrollPane()
    {
        return myScrollPane;
    }
}
