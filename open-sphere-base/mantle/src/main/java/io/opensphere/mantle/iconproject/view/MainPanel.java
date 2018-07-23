package io.opensphere.mantle.iconproject.view;

import java.awt.EventQueue;
import java.awt.Window;

import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.AnchorPane;

import io.opensphere.core.Toolbox;
import io.opensphere.mantle.icon.IconRegistry;
import io.opensphere.mantle.iconproject.impl.ButtonBuilder;
import io.opensphere.mantle.util.MantleToolboxUtils;

/**
 * The Class Main Panel.
 *
 */
public class MainPanel extends SplitPane
{
    /** The Icon registry. */
    private final IconRegistry myIconRegistry;

<<<<<<< HEAD
    /** The optional selected icon URL. */
    private String mySelectedUrl;

    /** The Last selected tree node user object. */
    private IconRecordTreeNodeUserObject myLastSelectedTreeNodeUserObject;

    /** The Tree. */
    private final JTree myTree;

    /** The Root tree node. */
    @SuppressWarnings("PMD.SingularField")
    private TreeNode myRootTreeNode;

    /** The Tree model. */
    private DefaultTreeModel myTreeModel;

    RowConstraints rowConstraints = new RowConstraints();

    ColumnConstraints columnConstraints = new ColumnConstraints();

    /** Portion containing the icons */
    private final GridPane gridPane;

=======
>>>>>>> 3f8d801363cd121397e3e212fd06efdf05e9a74f
    /** The Customize Icon button. */
    private final ButtonBuilder myCustIconButton = new ButtonBuilder("Customize Icon", false);

    /** The button to add the icon. */
    private final ButtonBuilder myAddIconButton = new ButtonBuilder("Add Icon from File", false);

    /** The button to generate a new icon. */
    private final ButtonBuilder myGenIconButton = new ButtonBuilder("Generate New Icon", false);

    /** The scroll-bar for the icons in view. */
    private final ScrollBar myScrollBar;

    /** The tree view. */
    private final TreeView<String> myTreeView;

    /** The left panel view. */
    private final AnchorPane myLeftView;

<<<<<<< HEAD
    private final TreeView myTreeList;

    private final PanelModel myPanel = new PanelModel();

    private final AnchorPane myTreeView;

    /** The Display Grid of Icons. */
    private GridBuilder myIconGrid;
=======
    /** The treeView choice. */
    private String theChoice = "";
>>>>>>> 3f8d801363cd121397e3e212fd06efdf05e9a74f

    /**
     * The MainPanel constructor.
     *
     * @param tb the toolbox
     * @param owner the window owner
     */
    public MainPanel(Toolbox tb, Window owner)
    {
        myIconRegistry = MantleToolboxUtils.getMantleToolbox(tb).getIconRegistry();

        myLeftView = new AnchorPane();

        TreeBuilder treeBuilder = new TreeBuilder(myIconRegistry, null);
        myTreeView = new TreeView<>(treeBuilder);

        myScrollBar = new ScrollBar();

        setDividerPositions(0.25, 0.98);
        setLayoutY(48.0);

        AnchorPane.setBottomAnchor(myTreeView, 78.0);
        AnchorPane.setLeftAnchor(myTreeView, 0.0);
        AnchorPane.setRightAnchor(myTreeView, 0.0);
        AnchorPane.setTopAnchor(myTreeView, 0.0);
        myTreeView.setLayoutY(8.0);

        myAddIconButton.lockButton(myAddIconButton);
        AnchorPane.setBottomAnchor(myAddIconButton, 52.0);
        myAddIconButton.setOnAction(event ->
        {
<<<<<<< HEAD
=======
            EventQueue.invokeLater(() ->
            {
                // TODO: add generation stuff
            });
>>>>>>> 3f8d801363cd121397e3e212fd06efdf05e9a74f
        });

        AnchorPane.setBottomAnchor(myCustIconButton, 26.0);
        myCustIconButton.lockButton(myCustIconButton);
        AnchorPane.setBottomAnchor(myGenIconButton, 0.0);

        myGenIconButton.lockButton(myGenIconButton);
        myGenIconButton.setOnAction(event ->
        {
            EventQueue.invokeLater(() ->
            {
                // TODO: add generation stuff
            });
        });

<<<<<<< HEAD
        // System.out.println("width: " + getWidth());

        GridBuilder myIconGrid = new GridBuilder(130, myIconRegistry);

        myScrollBar.setOrientation(javafx.geometry.Orientation.VERTICAL);
        ScrollPane myScrollPane = new ScrollPane(myIconGrid);
        // ScrollPane myScrollPane = new ScrollPane(grid);
        myScrollPane.setPannable(true);
        AnchorPane.setLeftAnchor(myScrollPane, 0.);
        AnchorPane.setRightAnchor(myScrollPane, 0.);
        AnchorPane.setTopAnchor(myScrollPane, 0.);
        AnchorPane.setBottomAnchor(myScrollPane, 0.);
        myScrollPane.setFitToHeight(true);
        myScrollPane.setFitToWidth(true);

        columnConstraints.setMinWidth(10.0);
        columnConstraints.setPrefWidth(100.0);
        columnConstraints.setHgrow(javafx.scene.layout.Priority.SOMETIMES);

        rowConstraints.setMinHeight(10.0);
        rowConstraints.setPrefHeight(30.0);
        rowConstraints.setVgrow(javafx.scene.layout.Priority.SOMETIMES);

        myTreeView.getChildren().addAll(myTreeList, myAddIconButton, myCustIconButton, myGenIconButton);
        getItems().addAll(myTreeView, myScrollPane);
=======
        myTreeView.getSelectionModel().selectedItemProperty()
        .addListener((observable, oldValue, newValue) -> treeHandle(newValue));

        GridBuilder customGrid = new GridBuilder(130, myIconRegistry, theChoice);
        System.out.println("choice is:   " + theChoice);

        myScrollBar.setOrientation(javafx.geometry.Orientation.VERTICAL);
        ScrollPane theScrollPane = new ScrollPane(customGrid);
        theScrollPane.setPannable(true);
        AnchorPane.setLeftAnchor(theScrollPane, 0.);
        AnchorPane.setRightAnchor(theScrollPane, 0.);
        AnchorPane.setTopAnchor(theScrollPane, 0.);
        AnchorPane.setBottomAnchor(theScrollPane, 0.);
        theScrollPane.setFitToHeight(true);
        theScrollPane.setFitToWidth(true);

        myLeftView.getChildren().addAll(myTreeView, myAddIconButton, myCustIconButton, myGenIconButton);
        getItems().addAll(myLeftView, theScrollPane);
>>>>>>> 3f8d801363cd121397e3e212fd06efdf05e9a74f

        myCustIconButton.setOnAction(event ->
        {
            EventQueue.invokeLater(() ->
            {
<<<<<<< HEAD
                myIconGrid.openBuilder(tb, owner);
=======
                customGrid.openBuilder(tb, owner);
>>>>>>> 3f8d801363cd121397e3e212fd06efdf05e9a74f
            });
        });

        System.out.println("choiceee is:   " + theChoice);

    }

    /**
     * The tree event handler.
     *
     * @param newValue the new clicked-on Value
     */
    private void treeHandle(TreeItem<String> newValue)
    {
        System.out.println(newValue.getValue());
        theChoice = newValue.getValue();
    }

    /**
     * I'm not sure.
     *
     * @param choice the choice?
     */
    static void changeTop(boolean choice)
    {
        /* ObservableList<Node> childs = stackPane.getChildren();
<<<<<<< HEAD
         * 
         * Node grid = childs.get(1); Node list = childs.get(0); if (choice) {
         * grid.setVisible(false); list.setVisible(true); } else {
         * list.setVisible(false); grid.setVisible(true); } */
=======

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
        }*/
>>>>>>> 3f8d801363cd121397e3e212fd06efdf05e9a74f
    }
}
