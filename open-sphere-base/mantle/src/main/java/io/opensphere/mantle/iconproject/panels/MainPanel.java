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
import io.opensphere.mantle.iconproject.view.TreePopupMenu;

/**
 * The Main Panel in the Icon Manager UI comprised of the tree hierarchy and
 * icon display window.
 *
 */
public class MainPanel extends SplitPane
{
    /** The Model. */
    private PanelModel myPanelModel = new PanelModel();

    /** The Icon Display Grid. */
    private GridBuilder myIconGrid;

    /** The Customize Icon button. */
    private final ButtonBuilder myCustIconButton = new ButtonBuilder("Customize Icon", false);

    /** The button to add the icon. */
    private final MenuButton myAddIconButton = new MenuButton("Add Icon From");

    /** The button to generate a new icon. */
    private final ButtonBuilder myGenIconButton = new ButtonBuilder("Generate New Icon", false);

    /** The tree view. */
    private TreeView<String> myTreeView;

    /** The left Panel. */
    private AnchorPane myLeftView = new AnchorPane();

    /** The treeBuilder object. */
    private TreeBuilder treeBuilder;

    /** The map of collection name keys and icon record list values. */
    Map<String, List<IconRecord>> recordMap = new HashMap<>();

    /** The main panel's scroll pane which contains the grid of icons. */
    ScrollPane myScrollPane;

    /** The owner window of the main panel. */
    private final Window myOwner;

    /**
     * The MainPanel constructor.
     *
     * @param thePanelModel the model for the main panel
     */
    public MainPanel(PanelModel thePanelModel)
    {
        myPanelModel = thePanelModel;
        myOwner = myPanelModel.getOwner();

        myPanelModel.getTileWidth().addListener((o, v, m) -> refresh());

        createTreeView(null);
        recordMap = new HashMap<>(treeBuilder.getRecordMap());
        myPanelModel.setIconRecordList(recordMap.get("Default"));
        myIconGrid = new GridBuilder(myPanelModel);

        AnchorPane.setLeftAnchor(myAddIconButton, 0.);
        AnchorPane.setRightAnchor(myAddIconButton, 0.);
        AnchorPane.setBottomAnchor(myAddIconButton, 52.0);

        MenuItem fileOption = new MenuItem("File");
        MenuItem folderOption = new MenuItem("Folder");
        myAddIconButton.getItems().addAll(fileOption, folderOption);
        myAddIconButton.setAlignment(Pos.CENTER);
        fileOption.setOnAction(event ->
        {
            EventQueue.invokeLater(() ->
            {
                loadFromFile(myPanelModel.getImportProps().getCollectionName().get(),
                        myPanelModel.getImportProps().getSubCollectionName().get());
            });
        });

        folderOption.setOnAction(event ->
        {
            EventQueue.invokeLater(() ->
            {
                addIconsFromFolder();
            });
        });

        AnchorPane.setBottomAnchor(myCustIconButton, 26.0);
        myCustIconButton.lockButton(myCustIconButton);
        myCustIconButton.setOnAction(event ->
        {
            EventQueue.invokeLater(() ->
            {
                myIconGrid.showIconCustomizer(myOwner);
            });
        });

        AnchorPane.setBottomAnchor(myGenIconButton, 0.0);
        myGenIconButton.lockButton(myGenIconButton);
        myGenIconButton.setOnAction(event ->
        {
            refresh();
        });

        myScrollPane = new ScrollPane(myIconGrid);
        myScrollPane.setPannable(true);
        AnchorPane.setLeftAnchor(myScrollPane, 0.);
        AnchorPane.setRightAnchor(myScrollPane, 0.);
        AnchorPane.setTopAnchor(myScrollPane, 0.);
        AnchorPane.setBottomAnchor(myScrollPane, 0.);
        myScrollPane.setFitToHeight(true);
        myScrollPane.setFitToWidth(true);

        myLeftView.getChildren().addAll(myTreeView, myAddIconButton, myCustIconButton, myGenIconButton);
        getItems().addAll(myLeftView, myScrollPane);
    }

    /**
     * Creates the icon tree hierarchy.
     *
     * @param treeItem the name of an item to add to the tree.
     */
    private void createTreeView(TreeItem<String> treeItem)
    {
        treeBuilder = new TreeBuilder(myPanelModel, null);
        myTreeView = new TreeView<>(treeBuilder);
        myTreeView.setShowRoot(false);
        myTreeView.setContextMenu(new TreePopupMenu(myPanelModel));
        
        myPanelModel.getTreeObj().getMyObsTree().set(myTreeView);
        myPanelModel.getTreeObj().getMyObsTree()
        .addListener((o, v, n) -> refreshTree());
        
        setDividerPositions(0.25);
        setLayoutY(48.0);

        AnchorPane.setBottomAnchor(myTreeView, 78.0);
        AnchorPane.setLeftAnchor(myTreeView, 0.0);
        AnchorPane.setRightAnchor(myTreeView, 0.0);
        AnchorPane.setTopAnchor(myTreeView, 0.0);

        if (treeItem == null)
        {
            for (int i = 0; i <= myTreeView.getExpandedItemCount(); i++)
            {
                if ((myTreeView.getTreeItem(i).getValue()) == "Default")
                {
                    myTreeView.getSelectionModel().select(myTreeView.getRow((myTreeView.getTreeItem(i))));
                    break;
                }
            }
        }
        else
        {
            myTreeView.getSelectionModel().select(treeItem);
        }

        myTreeView.getSelectionModel().selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> treeHandle(newValue));
    }

    /**
     * Creates a new Tree Hierarchy and Icon Display Grid to reflect changes.
     */
    public void refresh()
    {
        Platform.runLater(new Runnable()
        {
            @Override
            public void run()
            {
                refreshTree();
                recordMap = new HashMap<>(treeBuilder.getRecordMap());
                myPanelModel.setIconRecordList(recordMap.get(myTreeView.getSelectionModel().getSelectedItem().getValue()));

                myScrollPane.setContent(myIconGrid = new GridBuilder(myPanelModel));
                myPanelModel.getSelectedIcons().clear();
                System.out.println("Icon Grid has been refreshed!!!!!!!");
            }
        });
    }
    /** Refreshes the Tree Hierarchy.*/
    public void refreshTree()
    {
        myLeftView.getChildren().removeAll(myTreeView);
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
        String colName = newValue.getValue();
        if (!myPanelModel.getRecordList().equals(recordMap.get(colName)))
        {
            myPanelModel.getSelectedIcons().clear();
        }
        myPanelModel.setIconRecordList(recordMap.get(colName));
        myScrollPane.setContent(myIconGrid = new GridBuilder(myPanelModel));
        if (myPanelModel.getIconRegistry().getCollectionNames().contains(colName))
        {
            myPanelModel.getImportProps().getCollectionName().set(colName);
        }
        else
        {
            myPanelModel.getImportProps().getCollectionName().set(recordMap.get(colName).get(0).getCollectionName());
            myPanelModel.getImportProps().getSubCollectionName().set(colName);
        }
        // if (obj.getNameType() == NameType.COLLECTION)
        // {
        // iconIdList = myIconRegistry
        // .getIconIds(value -> EqualsHelper.equals(value.getCollectionName(),
        // obj.getLabel()));
        // }
        // else
        // {
        // // Subcategory.
        // iconIdList = myIconRegistry.getIconIds(value ->
        // EqualsHelper.equals(value.getSubCategory(), obj.getLabel()));
        // }
    }

    /**
     * Changes the Icon Display Grid from Grid to List.
     *
     * @param choice the selected toggle.
     */
    static void changeTop(boolean choice)
    {
        // StackPane stackPane = new StackPane();
        // ObservableList<Node> childs = stackPane.getChildren();
        //
        // Node grid = childs.get(1);
        // Node list = childs.get(0);
        // if (choice)
        // {
        // grid.setVisible(false);
        // list.setVisible(true);
        // }
        // else
        // {
        // list.setVisible(false);
        // grid.setVisible(true);
        // }
    }

    /**
     * Loads a single icon from a file.
     *
     * @param collectionName the collection name
     * @param subCatName the sub cat name
     */
    public void loadFromFile(String collectionName, String subCatName)
    {
        File result = ImageUtil.showImageFileChooser("Choose Icon File", myOwner,
                myPanelModel.getToolBox().getPreferencesRegistry());
        if (result != null)
        {
            try
            {
                IconProvider provider = new DefaultIconProvider(result.toURI().toURL(), collectionName, subCatName, "User");
                myPanelModel.getIconRegistry().addIcon(provider, this);
            }
            catch (MalformedURLException e)
            {
                JFXDialog test = new JFXDialog(myOwner, "Failed to load Icons");
                test.setFxNode(myScrollPane);
                test.setVisible(true);
            }
            refresh();
        }
    }

    /**
     * Adds the icons from folder.
     */
    private void addIconsFromFolder()
    {
        System.out.println(myPanelModel.getIconRegistry().getAllAssignedElementIds());
        AddIconDialog iconImporter = new AddIconDialog(myOwner, myPanelModel);
        iconImporter.setVisible(true);
    }

    /**
     * Gets the current icon display grid.
     * 
     * @return myIconGrid the current icon display grid.
     */
    public GridBuilder getIconGrid()
    {
        return myIconGrid;
    }

    public TreeItem<String> getTree()
    {
        return null;
    }
}
