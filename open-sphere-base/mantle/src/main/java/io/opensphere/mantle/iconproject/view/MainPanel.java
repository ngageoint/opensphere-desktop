package io.opensphere.mantle.iconproject.view;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
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

    private final GridPane gridPane;

    private final Button myGenIconButton;

    private final Button myCustIconButton;

    private final Button myAddIconButton;

    private final TreeView myTreeList;

    private final AnchorPane myTreeView;

    private final ScrollBar myScrollBar;

    public MainPanel()
    {

        myTreeView = new AnchorPane();
        myTreeList = new TreeView();
        myAddIconButton = new Button();
        myCustIconButton = new Button();
        myGenIconButton = new Button();
        gridPane = new GridPane();
        myScrollBar = new ScrollBar();

        setDividerPositions(0.2406679764243615, 0.9833005893909627);
        setLayoutY(48.0);

        AnchorPane.setBottomAnchor(myTreeList, 78.0);
        AnchorPane.setLeftAnchor(myTreeList, 0.0);
        AnchorPane.setRightAnchor(myTreeList, 0.0);
        AnchorPane.setTopAnchor(myTreeList, 0.0);
        myTreeList.setLayoutY(8.0);
        myTreeList.setPadding(new Insets(2.0));

        AnchorPane.setBottomAnchor(myAddIconButton, 52.0);
        AnchorPane.setLeftAnchor(myAddIconButton, -2.0);
        AnchorPane.setRightAnchor(myAddIconButton, 0.0);
        myAddIconButton.setAlignment(javafx.geometry.Pos.CENTER);
        myAddIconButton.setLayoutX(-2.0);
        myAddIconButton.setLayoutY(394.0);
        myAddIconButton.setMnemonicParsing(false);

        myAddIconButton.setText("Add Icon from File");

        AnchorPane.setBottomAnchor(myCustIconButton, 26.0);
        AnchorPane.setLeftAnchor(myCustIconButton, -2.0);
        AnchorPane.setRightAnchor(myCustIconButton, 0.0);
        myCustIconButton.setLayoutY(419.0);
        myCustIconButton.setMnemonicParsing(false);

        myCustIconButton.setText("Customize Icon");

        AnchorPane.setBottomAnchor(myGenIconButton, 0.0);
        AnchorPane.setLeftAnchor(myGenIconButton, -2.0);
        AnchorPane.setRightAnchor(myGenIconButton, 0.0);
        myGenIconButton.setLayoutX(-2.0);
        myGenIconButton.setLayoutY(444.0);
        myGenIconButton.setMnemonicParsing(false);

        myGenIconButton.setText("Generate New Icon");

        myScrollBar.setOrientation(javafx.geometry.Orientation.VERTICAL);

        myTreeView.getChildren().add(myTreeList);
        myTreeView.getChildren().add(myAddIconButton);
        myTreeView.getChildren().add(myCustIconButton);
        myTreeView.getChildren().add(myGenIconButton);

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
