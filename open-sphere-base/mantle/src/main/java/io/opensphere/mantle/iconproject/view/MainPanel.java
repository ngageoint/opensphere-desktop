package io.opensphere.mantle.iconproject.view;

import javafx.scene.control.ScrollBar;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TreeView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;

public class MainPanel extends SplitPane
{

    RowConstraints rowConstraints = new RowConstraints();

    ColumnConstraints columnConstraints = new ColumnConstraints();

    private GridPane gridPane;

    private ButtonBuilder myCustIconButton = new ButtonBuilder("Customize Icon", false);

    private ButtonBuilder myAddIconButton = new ButtonBuilder("Add Icon from File", false);

    private ButtonBuilder myGenIconButton = new ButtonBuilder("Generate New Icon", false);

    private TreeView myTreeList;

    private AnchorPane myTreeView;

    private ScrollBar myScrollBar;

    public MainPanel()
    {

        myTreeView = new AnchorPane();
        myTreeList = new TreeView();

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

        AnchorPane.setBottomAnchor(myCustIconButton, 26.0);
        myCustIconButton.lockButton(myCustIconButton);

        AnchorPane.setBottomAnchor(myGenIconButton, 0.0);
        myGenIconButton.lockButton(myGenIconButton);

        myScrollBar.setOrientation(javafx.geometry.Orientation.VERTICAL);

        myTreeView.getChildren().addAll(myTreeList,myAddIconButton,myCustIconButton,myGenIconButton);

        columnConstraints.setMinWidth(10.0);
        columnConstraints.setPrefWidth(100.0);
        columnConstraints.setHgrow(javafx.scene.layout.Priority.SOMETIMES);

        rowConstraints.setMinHeight(10.0);
        rowConstraints.setPrefHeight(30.0);
        rowConstraints.setVgrow(javafx.scene.layout.Priority.SOMETIMES);

        gridPane.getColumnConstraints().addAll(columnConstraints);
        gridPane.getRowConstraints().addAll(rowConstraints);

        getItems().addAll(myTreeView, gridPane, myScrollBar);
    }
}
