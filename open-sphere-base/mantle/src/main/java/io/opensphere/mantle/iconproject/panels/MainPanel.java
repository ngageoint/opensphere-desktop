package io.opensphere.mantle.iconproject.panels;

import java.awt.EventQueue;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.apache.commons.lang3.StringUtils;

import javafx.geometry.Pos;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.AnchorPane;
import io.opensphere.core.util.filesystem.MnemonicFileChooser;
import io.opensphere.core.util.image.ImageUtil;
import io.opensphere.mantle.icon.IconProvider;
import io.opensphere.mantle.icon.IconRecord;
import io.opensphere.mantle.icon.IconRegistry;
import io.opensphere.mantle.icon.impl.DefaultIconProvider;
import io.opensphere.mantle.icon.impl.IconProviderFactory;
import io.opensphere.mantle.icon.impl.gui.SubCategoryPanel;
import io.opensphere.mantle.iconproject.impl.ButtonBuilder;
import io.opensphere.mantle.iconproject.model.ImportProp;
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

    /** The Model */
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

    private final TreeBuilder treeBuilder;

    Map<String, List<IconRecord>> recordMap = new HashMap<>();

    ScrollPane myScrollPane;

    private Window myOwner;

    // private final Thread myGridLoader;

    /**
     * The MainPanel constructor.
     *
     * @param tb the toolbox
     * @param owner the window owner
     */
    public MainPanel(PanelModel thePanelModel)
    {
        myPanelModel = thePanelModel;
        myOwner = myPanelModel.getOwner();
        myIconRegistry = myPanelModel.getMyIconRegistry();

        myLeftView = new AnchorPane();

        treeBuilder = new TreeBuilder(myPanelModel, null);
        myTreeView = new TreeView<>(treeBuilder);
        myTreeView.setShowRoot(false);

        recordMap = new HashMap<>(treeBuilder.getRecordMap());
        List<IconRecord> recordList = recordMap.get("User Added");
        System.out.println(recordList);

        myIconGrid = new GridBuilder(90, recordList, myPanelModel);
        // myGridLoader = new Thread(myIconGrid);
        // startThread();

        setDividerPositions(0.25);
        // maxWidthProperty().multiply(0.25);
        // setResizableWithParent(myLeftView, false);
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
                loadFromFile(myPanelModel.getImportProps().getCollectionName().get(), null);
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

    /**
     * The tree event handler.
     *
     * @param newValue the new clicked-on Value
     */
    private void treeHandle(TreeItem<String> newValue)
    {
        String colName = newValue.getValue();
        myPanelModel.getImportProps().getCollectionName().set(colName);
        if (recordMap.get(colName) == null)
        {
            for (Entry<String, List<IconRecord>> entry : recordMap.entrySet())
            {
                String key = entry.getKey();
                List<IconRecord> value = entry.getValue();

                if (value.get(0).getSubCategory() != null && value.get(0).getSubCategory().toString().equals(colName))
                {
                    colName = key;
                    break;
                }
            }
        }
        System.out.println("fixed choice: " + colName);
        myScrollPane.setContent(new GridBuilder(90, recordMap.get(colName), myPanelModel));
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
                // myIconRegistry.addIcon(new
                // DefaultIconProvider(result.toURI().toURL(), collectionName,
                // subCatName, "User"), this);
                System.out.println("loading from file under the name:  " + collectionName);
                IconProvider provider = new DefaultIconProvider(result.toURI().toURL(), collectionName, null,
                        "User");
                myIconRegistry.addIcon(provider, this);
            }
            catch (MalformedURLException e)
            {
            }
        }
    }

    /**
     * Adds the icons from folder.
     */
    private void addIconsFromFolder()
    {

        AddIconDialog iconImporter = new AddIconDialog(myOwner, myPanelModel);
        iconImporter.setVisible(true);
        iconImporter.addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent e)
            {
                System.out.println("WindowClosingDemo.windowClosing");
                System.exit(0);
            }
        });

    }
}
