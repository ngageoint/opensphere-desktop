package io.opensphere.mantle.iconproject.view;

import java.awt.EventQueue;
import java.awt.Window;
import io.opensphere.core.Toolbox;
import io.opensphere.mantle.icon.impl.gui.IconChooserPanel;

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

    private final ButtonBuilder myCustIconButton = new ButtonBuilder("Customize Icon", false);

    private final ButtonBuilder myAddIconButton = new ButtonBuilder("Add Icon from File", false);

    private final ButtonBuilder myGenIconButton = new ButtonBuilder("Generate New Icon", false);

    private final TreeView myTreeList;

    private final AnchorPane myTreeView;

    private final ScrollBar myScrollBar;

    @SuppressWarnings("unused")
    public MainPanel(Toolbox tb)
    {

        Window owner = tb.getUIRegistry().getMainFrameProvider().get();

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

        myAddIconButton.setOnAction(event ->
        {
            IconChooserPanel chooseIcon = new IconChooserPanel(tb);
            // loadFromFile(IconRecord.USER_ADDED_COLLECTION, null);
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

        myTreeView.getChildren().addAll(myTreeList, myAddIconButton, myCustIconButton, myGenIconButton);

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
