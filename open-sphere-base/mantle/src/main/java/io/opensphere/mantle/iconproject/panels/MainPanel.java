package io.opensphere.mantle.iconproject.panels;

import java.awt.EventQueue;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.geometry.Pos;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.AnchorPane;

import io.opensphere.core.util.image.ImageUtil;
import io.opensphere.mantle.icon.IconProvider;
import io.opensphere.mantle.icon.IconRecord;
import io.opensphere.mantle.icon.IconRegistry;
import io.opensphere.mantle.icon.impl.DefaultIconProvider;
import io.opensphere.mantle.iconproject.impl.ButtonBuilder;
import io.opensphere.mantle.iconproject.model.PanelModel;
import io.opensphere.mantle.iconproject.view.AddIconDialog;

/**
 * The Class Main Panel.
 *
 */
public class MainPanel extends SplitPane
{
    /** The Icon registry. */
    private final IconRegistry myIconRegistry;

    /** The Model. */
    private PanelModel myPanelModel = new PanelModel();

    /** The Icon Display Grid. */
    private final GridBuilder myIconGrid;

    /** The Customize Icon button. */
    private final ButtonBuilder myCustIconButton = new ButtonBuilder("Customize Icon", false);

    /** The button to add the icon. */
    // private final ButtonBuilder myAddIconButton = new ButtonBuilder("Add Icon
    // from File", false);
    private final MenuButton myAddIconButton = new MenuButton("Add Icon From");

    /** The button to generate a new icon. */
    private final ButtonBuilder myGenIconButton = new ButtonBuilder("Generate New Icon", false);

    /** The tree view. */
    private final TreeView<String> myTreeView;

    /** The left Panel. */
    private final AnchorPane myLeftView;

    /** The treeBuilder object. */
    private final TreeBuilder treeBuilder;

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
        myIconRegistry = myPanelModel.getMyIconRegistry();
        myLeftView = new AnchorPane();
        myPanelModel.getTileWidth().addListener((o,v,m) -> {
            refresh();
        });

        treeBuilder = new TreeBuilder(myPanelModel, null);
        myTreeView = new TreeView<>(treeBuilder);
        myTreeView.setShowRoot(false);

        recordMap = new HashMap<>(treeBuilder.getRecordMap());
        myPanelModel.setIconRecordList(recordMap.get("Default"));

        myIconGrid = new GridBuilder(myPanelModel);

        setDividerPositions(0.25);
        setResizableWithParent(myLeftView, false);
        setLayoutY(48.0);

        AnchorPane.setBottomAnchor(myTreeView, 78.0);
        AnchorPane.setLeftAnchor(myTreeView, 0.0);
        AnchorPane.setRightAnchor(myTreeView, 0.0);
        AnchorPane.setTopAnchor(myTreeView, 0.0);
        myTreeView.setLayoutY(8.0);

        AnchorPane.setLeftAnchor(myAddIconButton, 0.);
        AnchorPane.setRightAnchor(myAddIconButton, 0.);
        AnchorPane.setBottomAnchor(myAddIconButton, 52.0);

        MenuItem File = new MenuItem("File");
        MenuItem Folder = new MenuItem("Folder");
        myAddIconButton.getItems().addAll(File, Folder);
        myAddIconButton.setAlignment(Pos.CENTER);
        File.setOnAction(event ->
        {
            EventQueue.invokeLater(() ->
            {
                System.out.println("File has been Selected");
                loadFromFile(myPanelModel.getImportProps().getCollectionName().get(),
                        myPanelModel.getImportProps().getSubCollectionName().get());
            });
        });

        Folder.setOnAction(event ->
        {
            EventQueue.invokeLater(() ->
            {
                System.out.println("Folder has been Selected");
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
            EventQueue.invokeLater(() ->
            {
            });
        });

        myScrollPane = new ScrollPane(myIconGrid);
        myScrollPane.setPannable(true);
        AnchorPane.setLeftAnchor(myScrollPane, 0.);
        AnchorPane.setRightAnchor(myScrollPane, 0.);
        AnchorPane.setTopAnchor(myScrollPane, 0.);
        AnchorPane.setBottomAnchor(myScrollPane, 0.);
        myScrollPane.setFitToHeight(true);
        myScrollPane.setFitToWidth(true);

        myTreeView.getSelectionModel().selectedItemProperty()
        .addListener((observable, oldValue, newValue) -> treeHandle(newValue));

        myLeftView.getChildren().addAll(myTreeView, myAddIconButton, myCustIconButton, myGenIconButton);
        getItems().addAll(myLeftView, myScrollPane);
    }

    public void refresh()
    {
        System.out.println("Icon Grid starting to Refreshed!!!!!!!");
        myScrollPane.setContent(new GridBuilder(myPanelModel));
        System.out.println("Icon Grid has been refreshed!!!!!!!");
    }

    /**
     * The tree event handler.
     *
     * @param newValue the new clicked-on Value
     */
    private void treeHandle(TreeItem<String> newValue)
    {
        String colName = newValue.getValue();
        myPanelModel.setIconRecordList(recordMap.get(colName));
        myScrollPane.setContent(new GridBuilder(myPanelModel));

        if (myPanelModel.getMyIconRegistry().getCollectionNames().contains(colName))
        {
            myPanelModel.getImportProps().getCollectionName().set(colName);
        }
        else
        {
            myPanelModel.getImportProps().getCollectionName().set(recordMap.get(colName).get(0).getCollectionName());
            myPanelModel.getImportProps().getSubCollectionName().set(colName);
        }

        //        if (obj.getNameType() == NameType.COLLECTION)
        //        {
        //            iconIdList = myIconRegistry
        //                    .getIconIds(value -> EqualsHelper.equals(value.getCollectionName(), obj.getLabel()));
        //        }
        //        else
        //        {
        //            // Subcategory.
        //            iconIdList = myIconRegistry.getIconIds(value -> EqualsHelper.equals(value.getSubCategory(), obj.getLabel()));
        //        }
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
     * Load from file.
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
                System.out.println("the Icon Url is : " + result.toURI().toURL());
                System.out.println("loading from file under the name:  " + collectionName);
                IconProvider provider = new DefaultIconProvider(result.toURI().toURL(), collectionName, subCatName, "User");
                myPanelModel.getMyIconRegistry().addIcon(provider, this);
            }
            catch (MalformedURLException e)
            {
            }
        }
        myPanelModel.getViewModel().getMainPanel().refresh();
    }

    /**
     * Adds the icons from folder.
     */
    private void addIconsFromFolder()
    {
        AddIconDialog iconImporter = new AddIconDialog(myOwner, myPanelModel);
        iconImporter.setVisible(true);
    }
}
