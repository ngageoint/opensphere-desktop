package io.opensphere.mantle.iconproject.view;

import java.awt.EventQueue;
import java.awt.Window;
import java.net.MalformedURLException;
import java.net.URL;

import io.opensphere.core.Toolbox;

import io.opensphere.mantle.iconproject.impl.ButtonBuilder;
import io.opensphere.mantle.iconproject.model.PanelModel;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import io.opensphere.mantle.icon.IconRecord;
import io.opensphere.mantle.icon.IconRecordTreeNodeUserObject;
import io.opensphere.mantle.icon.IconRegistry;
import io.opensphere.mantle.util.MantleToolboxUtils;

import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
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

    // these are in swing vvvvv
    /** The Tree. */
    private final JTree myTree;

    /** The Root tree node. */
    @SuppressWarnings("PMD.SingularField")
    private TreeNode myRootTreeNode;

    /** The Tree model. */
    private DefaultTreeModel myTreeModel;
    // ^^^^^ need to convert swing to fx

    RowConstraints rowConstraints = new RowConstraints();

    ColumnConstraints columnConstraints = new ColumnConstraints();

    /** Portion containing the icons */
    private final GridPane gridPane;

    /** The Customize Icon button. */
    private final ButtonBuilder myCustIconButton = new ButtonBuilder("Customize Icon", false);

    /** The button to add the icon. */
    private final ButtonBuilder myAddIconButton = new ButtonBuilder("Add Icon from File", false);

    /** The button to generate a new icon */
    private final ButtonBuilder myGenIconButton = new ButtonBuilder("Generate New Icon", false);

    /** The scrollbar for the icons in view. */
    private final ScrollBar myScrollBar;

    /** Temporary for experimentation. */
    private static StackPane stackPane;

    /** The record of the currently selected icon. */
    private IconRecord mySelectedIcon;

    private TreeView myTreeList;
    private final PanelModel myPanel = new PanelModel();

    private AnchorPane myTreeView;

    public MainPanel(Toolbox tb,Window owner)
    {
        myTree = new JTree();
        myTree.setRootVisible(false);
        myTree.setExpandsSelectedPaths(true);

        myIconRegistry = MantleToolboxUtils.getMantleToolbox(tb).getIconRegistry();
  //      Window owner = tb.getUIRegistry().getMainFrameProvider().get();

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
            // loadFromFile(IconRecord.USER_ADDED_COLLECTION, null, tb);
            //loadFromFile(IconRecord.USER_ADDED_COLLECTION, null, tb);
            //FileAddBuilder fileAdd = new FileAddBuilder(tb);
        });

        AnchorPane.setBottomAnchor(myCustIconButton, 26.0);
        myCustIconButton.lockButton(myCustIconButton);
        AnchorPane.setBottomAnchor(myGenIconButton, 0.0);
        
        
        URL test = null;
        try
        {
            test = new URL("file:/C:/Users/Kelly/mist/vortex/iconCache/"
                    + "Location_Pin_32.png_0.0_0.6000000238418579-0.0-0.0.png"
);
        }
        catch (MalformedURLException e)
        {
            e.printStackTrace();
        }
        IconRecord iconrecord = MantleToolboxUtils.getMantleToolbox(tb).getIconRegistry().getIconRecord(test);
        
        myCustIconButton.setOnAction(event ->
        {
            EventQueue.invokeLater(() ->
            {
                IconProjBuilderNewDialog builderPane = new IconProjBuilderNewDialog(owner, myIconRegistry, iconrecord);
                builderPane.setVisible(true);
            });
        });

        myGenIconButton.lockButton(myGenIconButton);
        GridBuilder CustomGrid = new GridBuilder(110,myIconRegistry);
        
        myScrollBar.setOrientation(javafx.geometry.Orientation.VERTICAL);
        ScrollPane myScrollPane = new ScrollPane(CustomGrid);
        myScrollPane.setPannable(true);
        
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

        myTreeView.getChildren().addAll(myTreeList, myAddIconButton, myCustIconButton, myGenIconButton);
        getItems().addAll(myTreeView, myScrollPane);
        
    }

    static void changeTop(boolean choice)
    {
        ObservableList<Node> childs = stackPane.getChildren();

        Node grid = childs.get(1);
        Node list = childs.get(0);
        if (choice)
        {
            grid.setVisible(false);
            list.setVisible(true);
        }
        else
        {
            list.setVisible(false);
            grid.setVisible(true);
        }
    }

}
