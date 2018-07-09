package io.opensphere.mantle.iconproject.view;

import java.awt.EventQueue;
import java.awt.Window;
import io.opensphere.core.Toolbox;

import io.opensphere.mantle.iconproject.impl.ButtonBuilder;
import io.opensphere.mantle.iconproject.model.PanelModel;
import javafx.beans.property.Property;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;

import io.opensphere.mantle.icon.IconRecordTreeNodeUserObject;
import io.opensphere.mantle.icon.IconRegistry;
import io.opensphere.mantle.util.MantleToolboxUtils;

import javafx.scene.control.ScrollBar;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TreeView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

public class MainPanel extends SplitPane
{
    /** The Icon registry. */
    private final IconRegistry myIconRegistry;

    /** The optional selected icon URL. */
    private String mySelectedUrl;

    /** The Last selected tree node user object. */
    private IconRecordTreeNodeUserObject myLastSelectedTreeNodeUserObject;
    //these are in swing vvvvv
    /** The Tree. */
    private final JTree myTree;
    /** The Root tree node. */
    @SuppressWarnings("PMD.SingularField")
    private TreeNode myRootTreeNode;
    /** The Tree model. */
    private DefaultTreeModel myTreeModel;
    //^^^^^ need to convert swing to fx

    RowConstraints rowConstraints = new RowConstraints();

    ColumnConstraints columnConstraints = new ColumnConstraints();

    private final GridPane gridPane;

    private final ButtonBuilder myCustIconButton = new ButtonBuilder("Customize Icon", false);

    private final ButtonBuilder myAddIconButton = new ButtonBuilder("Add Icon from File", false);

    private final ButtonBuilder myGenIconButton = new ButtonBuilder("Generate New Icon", false);

    private final TreeView myTreeList;

    private final AnchorPane myTreeView;

    private final ScrollBar myScrollBar;

    private static StackPane stackPane;

    private PanelModel myPanel = new PanelModel();


    @SuppressWarnings("unused")
    
    public MainPanel(Toolbox tb)
    {
        myTree = new JTree();
        myTree.setRootVisible(false);
        myTree.setExpandsSelectedPaths(true);



        myIconRegistry = MantleToolboxUtils.getMantleToolbox(tb).getIconRegistry();
        Window owner = tb.getUIRegistry().getMainFrameProvider().get();


        myTreeView = new AnchorPane();
        TreeBuilder myTreeBuilder = new TreeBuilder();
        myTreeList = new TreeView(myTreeBuilder);

        gridPane = new GridPane();
        myScrollBar = new ScrollBar();

        setDividerPositions(0.25, 0.98);
        setLayoutY(48.0);

        AnchorPane.setBottomAnchor(myTreeList, 78.0);
        AnchorPane.setLeftAnchor(myTreeList, 0.0);
        AnchorPane.setRightAnchor(myTreeList, 0.0);
        AnchorPane.setTopAnchor(myTreeList, 0.0);
        myTreeList.setLayoutY(8.0);

        myAddIconButton.lockButton(myAddIconButton);
        AnchorPane.setBottomAnchor(myAddIconButton, 52.0);

        myAddIconButton.setOnAction(event ->
        {
            // IconChooserPanel chooseIcon = new IconChooserPanel(tb);
            //loadFromFile(IconRecord.USER_ADDED_COLLECTION, null, tb);
        });

        AnchorPane.setBottomAnchor(myCustIconButton, 26.0);
        myCustIconButton.lockButton(myCustIconButton);
        myCustIconButton.setOnAction(event ->
        {
            EventQueue.invokeLater(() ->
            {
                IconProjBuilderDialog builderPane = new IconProjBuilderDialog(owner, tb);
            });
        });

        AnchorPane.setBottomAnchor(myGenIconButton, 0.0);
        myGenIconButton.lockButton(myGenIconButton);

        myScrollBar.setOrientation(javafx.geometry.Orientation.VERTICAL);

        columnConstraints.setMinWidth(10.0);
        columnConstraints.setPrefWidth(100.0);
        columnConstraints.setHgrow(javafx.scene.layout.Priority.SOMETIMES);

        rowConstraints.setMinHeight(10.0);
        rowConstraints.setPrefHeight(30.0);
        rowConstraints.setVgrow(javafx.scene.layout.Priority.SOMETIMES);

        // StackPane
        stackPane = new StackPane();

        // Add Label to StackPane
        Label label = new Label("I'm a Label");
        label.setStyle("-fx-background-color:red");
        label.setPadding(new Insets(5, 5, 5, 5));
        stackPane.getChildren().add(label);

        // Add Button to StackPane
        Button button = new Button("I'm a Button");
        button.setStyle("-fx-background-color: blue");
        button.setPadding(new Insets(5, 5, 5, 5));
        stackPane.getChildren().add(button);
        // stackPane.rotateProperty().bind(mysizerrrrrr);
 
        //

        gridPane.getColumnConstraints().addAll(columnConstraints);
        gridPane.getRowConstraints().addAll(rowConstraints);
        myTreeView.getChildren().addAll(myTreeList, myAddIconButton, myCustIconButton, myGenIconButton);
        getItems().addAll(myTreeView, stackPane, myScrollBar);
    }

    static void changeTop(boolean choice)
    {
        ObservableList<Node> childs = stackPane.getChildren();

        Node grid = childs.get(1);
        Node list = childs.get(0);
        if (choice)
        {
            grid.setVisible(false);
            // grid.toBack();
            list.setVisible(true);
        }
        else
        {
            list.setVisible(false);
            // list.toBack();
            grid.setVisible(true);
        }

    }

    /**
     * Load from file.
     *
     * @param collectionName the collection name
     * @param subCatName the sub cat name
     */
    /*public void loadFromFile(String collectionName, String subCatName, Toolbox myToolbox)
    {
        Component parent = myToolbox.getUIRegistry().getMainFrameProvider().get();
        File result = ImageUtil.showImageFileChooser("Choose Icon File", parent, myToolbox.getPreferencesRegistry());

        if (result != null)
        {
            try
            {
                myIconRegistry.addIcon(new DefaultIconProvider(result.toURI().toURL(), collectionName, subCatName, "User"), this);
                refreshFromRegistry(collectionName);
            }
            catch (MalformedURLException e)
            {
                //JOptionPane.showMessageDialog(this, "Failed to load image: " + result.getAbsolutePath(), "Image Load Error",
                        //JOptionPane.ERROR_MESSAGE);
                System.out.println("Failed to load image");
            }
        }
    }*/

    /**
     * Refresh from registry.
     *
     * @param collectionToShow the name of the collection to show, or null
     */
    /*public final void refreshFromRegistry(String collectionToShow)
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
    }*/
    /**
     * Gets the node to select.
     *
     * @param collectionToShow the requested collection to show
     * @return the node to select, or null
     */
    /*private TreeNode getNodeToSelect(String collectionToShow)
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
            //vvvvvvvvvv mySelectedUrl is set in IconChooserDialog and needs to be ported over somewhere
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

    }*/

    /**
     * Sets the selected icon URL.
     *
     * @param selectedUrl the icon URL
     */
    /*public void setSelectedUrl(String selectedUrl) //currently not in use?
    {
        mySelectedUrl = selectedUrl;
    }*/
}
