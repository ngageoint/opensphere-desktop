package io.opensphere.mantle.iconproject.view;

import java.awt.EventQueue;
import java.awt.Window;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.TreeView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.RowConstraints;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import io.opensphere.core.Toolbox;

public class IconProjPanel extends AnchorPane
{
    public IconProjPanel(Toolbox tb)
    {
        /* Button test = new Button("click me"); setBottomAnchor(test, 10.);
         * getChildren().add(test); */

        /** The Toolbox. */
        // final Toolbox myToolbox = IconProjDialog.getMyToolbox();
        Window owner = tb.getUIRegistry().getMainFrameProvider().get();

        final AnchorPane myTopMenuBar;
        final ButtonBar mySizeMenu;
        final Text mySizeLabel;
        final Button myShrinkButton;
        final Button myEnlargeButton;
        final HBox myViewToggle;
        final Text myViewLabel;
        final RadioButton myListView;
        final ToggleGroup ViewStyle;
        final RadioButton myGridView;
        final ButtonBar mySearchBar;
        final Text mySearchLabel;
        final TextField textField;
        final SplitPane splitPane;
        final AnchorPane myTreeView;
        final TreeView myTreeList;
        final Button myAddIconButton;
        final Button myCustIconButton;
        final Button myGenIconButton;
        final GridPane gridPane;
        final ColumnConstraints columnConstraints;
        final ColumnConstraints columnConstraints0;
        final RowConstraints rowConstraints;
        final RowConstraints rowConstraints0;
        final RowConstraints rowConstraints1;
        final ScrollBar scrollBar;
        final AnchorPane myBottomMenuBar;
        final AnchorPane anchorPane;
        final AnchorPane anchorPane0;

        final TextField myDataBar;
        final Text myNotifyText;

        myTopMenuBar = new AnchorPane();
        mySizeMenu = new ButtonBar();
        mySizeLabel = new Text();
        myShrinkButton = new Button();
        myEnlargeButton = new Button();
        myViewToggle = new HBox();
        myViewLabel = new Text();
        myListView = new RadioButton();
        ViewStyle = new ToggleGroup();
        myGridView = new RadioButton();
        mySearchBar = new ButtonBar();
        mySearchLabel = new Text();
        textField = new TextField();
        splitPane = new SplitPane();
        myTreeView = new AnchorPane();
        myTreeList = new TreeView();
        myAddIconButton = new Button();
        myCustIconButton = new Button();
        myGenIconButton = new Button();
        gridPane = new GridPane();
        columnConstraints = new ColumnConstraints();
        columnConstraints0 = new ColumnConstraints();
        rowConstraints = new RowConstraints();
        rowConstraints0 = new RowConstraints();
        rowConstraints1 = new RowConstraints();
        scrollBar = new ScrollBar();
        myBottomMenuBar = new AnchorPane();
        anchorPane = new AnchorPane();
        anchorPane0 = new AnchorPane();
        // myDataBar = new TextField();
        // myNotifyText = new Text();

        AnchorPane.setLeftAnchor(myTopMenuBar, 0.0);
        AnchorPane.setRightAnchor(myTopMenuBar, 1.0);
        AnchorPane.setTopAnchor(myTopMenuBar, 0.0);

        AnchorPane.setLeftAnchor(mySizeMenu, 10.0);
        mySizeMenu.setLayoutX(36.0);
        mySizeMenu.setLayoutY(8.0);

        mySizeLabel.setStrokeType(javafx.scene.shape.StrokeType.OUTSIDE);
        mySizeLabel.setStrokeWidth(0.0);
        mySizeLabel.setText("Icon Size:");

        myShrinkButton.setMinWidth(0.0);
        myShrinkButton.setMnemonicParsing(false);

        myShrinkButton.setPrefHeight(25.0);
        myShrinkButton.setText("-");

        myEnlargeButton.setMinWidth(0.0);
        myEnlargeButton.setMnemonicParsing(false);

        myEnlargeButton.setPrefHeight(25.0);
        myEnlargeButton.setText("+");

        AnchorPane.setBottomAnchor(myViewToggle, -6.0);
        AnchorPane.setLeftAnchor(myViewToggle, 397.0);
        AnchorPane.setRightAnchor(myViewToggle, 383.0);
        AnchorPane.setTopAnchor(myViewToggle, 14.0);
        myViewToggle.setAlignment(javafx.geometry.Pos.TOP_CENTER);
        myViewToggle.setCache(true);
        myViewToggle.setCacheHint(javafx.scene.CacheHint.SPEED);
        myViewToggle.setLayoutX(397.0);
        myViewToggle.setLayoutY(14.0);
        myViewToggle.setPrefHeight(40.0);
        myViewToggle.setPrefWidth(240.0);
        myViewToggle.setSpacing(20.0);

        myViewLabel.setStrokeType(javafx.scene.shape.StrokeType.OUTSIDE);
        myViewLabel.setStrokeWidth(0.0);
        myViewLabel.setText("View Style:");
        myViewLabel.setTextOrigin(javafx.geometry.VPos.TOP);
        myViewLabel.setFont(new Font(14.0));

        myListView.setMnemonicParsing(false);

        myListView.setText("List");
        myListView.setTextOverrun(javafx.scene.control.OverrunStyle.CLIP);
        myListView.setFont(new Font(14.0));

        myListView.setToggleGroup(ViewStyle);

        myGridView.setMnemonicParsing(false);

        myGridView.setSelected(true);
        myGridView.setText("Grid");
        myGridView.setToggleGroup(ViewStyle);
        myGridView.setFont(new Font(14.0));

        AnchorPane.setRightAnchor(mySearchBar, 10.0);
        mySearchBar.setLayoutX(773.0);
        mySearchBar.setLayoutY(8.0);
        mySearchBar.setPrefHeight(40.0);
        mySearchBar.setPrefWidth(247.0);

        mySearchLabel.setStrokeType(javafx.scene.shape.StrokeType.OUTSIDE);
        mySearchLabel.setStrokeWidth(0.0);
        mySearchLabel.setText("Filter:");
        mySearchLabel.setFont(new Font(14.0));

        textField.setPrefHeight(25.0);
        textField.setPrefWidth(141.0);

        AnchorPane.setBottomAnchor(splitPane, 43.0);
        AnchorPane.setLeftAnchor(splitPane, 0.0);
        AnchorPane.setRightAnchor(splitPane, 1.0);
        AnchorPane.setTopAnchor(splitPane, 48.0);
        splitPane.setBlendMode(javafx.scene.effect.BlendMode.MULTIPLY);
        splitPane.setDividerPositions(0.2406679764243615, 0.9833005893909627);
        splitPane.setLayoutY(48.0);

        AnchorPane.setBottomAnchor(myTreeList, 78.0);
        AnchorPane.setLeftAnchor(myTreeList, 0.0);
        AnchorPane.setRightAnchor(myTreeList, 0.0);
        AnchorPane.setTopAnchor(myTreeList, 0.0);
        myTreeList.setLayoutY(8.0);
        myTreeList.setPadding(new Insets(2.0));

        AnchorPane.setBottomAnchor(myAddIconButton, 52.0);
        AnchorPane.setLeftAnchor(myAddIconButton, -2.0);
        AnchorPane.setRightAnchor(myAddIconButton, 0.0);
        // myAddIconButton.setAlignment(javafx.geometry.Pos.CENTER);
        myAddIconButton.setLayoutX(-2.0);
        myAddIconButton.setLayoutY(394.0);
        myAddIconButton.setMnemonicParsing(false);

        // myAddIconButton.getStylesheets().add("/icon/manager/model/IconManagerFrameStyle.css");


        myAddIconButton.setText("Add Icon from File");
        //myAddIconButton.setOnAction(event -> loadFromFile(IconRecord.USER_ADDED_COLLECTION, null));


        AnchorPane.setBottomAnchor(myCustIconButton, 26.0);
        AnchorPane.setLeftAnchor(myCustIconButton, -2.0);
        AnchorPane.setRightAnchor(myCustIconButton, 0.0);
        myCustIconButton.setLayoutY(419.0);
        myCustIconButton.setMnemonicParsing(false);

        myCustIconButton.setText("Customize Icon");
        myCustIconButton.setOnAction(event ->
        {
            EventQueue.invokeLater(() ->
            {
                @SuppressWarnings("unused")
                IconProjBuilderDialog builderPane = new IconProjBuilderDialog(owner, tb);
            });
        });

        AnchorPane.setBottomAnchor(myGenIconButton, 0.0);
        AnchorPane.setLeftAnchor(myGenIconButton, -2.0);
        AnchorPane.setRightAnchor(myGenIconButton, 0.0);
        myGenIconButton.setLayoutX(-2.0);
        myGenIconButton.setLayoutY(444.0);
        myGenIconButton.setMnemonicParsing(false);

        myGenIconButton.setText("Generate New Icon");

        columnConstraints.setHgrow(javafx.scene.layout.Priority.SOMETIMES);

        columnConstraints0.setHgrow(javafx.scene.layout.Priority.SOMETIMES);
        columnConstraints0.setMinWidth(10.0);
        columnConstraints0.setPrefWidth(100.0);

        rowConstraints.setMinHeight(10.0);
        rowConstraints.setPrefHeight(30.0);
        rowConstraints.setVgrow(javafx.scene.layout.Priority.SOMETIMES);

        rowConstraints0.setMinHeight(10.0);
        rowConstraints0.setPrefHeight(30.0);
        rowConstraints0.setVgrow(javafx.scene.layout.Priority.SOMETIMES);

        rowConstraints1.setMinHeight(10.0);
        rowConstraints1.setPrefHeight(30.0);
        rowConstraints1.setVgrow(javafx.scene.layout.Priority.SOMETIMES);

        scrollBar.setOrientation(javafx.geometry.Orientation.VERTICAL);

        AnchorPane.setBottomAnchor(myBottomMenuBar, 0.0);
        AnchorPane.setLeftAnchor(myBottomMenuBar, 0.0);
        AnchorPane.setRightAnchor(myBottomMenuBar, 1.0);
        myBottomMenuBar.setLayoutY(445.0);
        myBottomMenuBar.setPrefHeight(25.0);
        myBottomMenuBar.setPrefWidth(807.0);

        AnchorPane.setBottomAnchor(anchorPane, 0.0);
        AnchorPane.setLeftAnchor(anchorPane, 0.0);
        AnchorPane.setRightAnchor(anchorPane, 126.0);
        anchorPane.setLayoutY(152.0);
        anchorPane.setPrefHeight(48.0);
        anchorPane.setPrefWidth(900.0);

        AnchorPane.setBottomAnchor(anchorPane0, 11.0);
        AnchorPane.setLeftAnchor(anchorPane0, 239.0);
        AnchorPane.setRightAnchor(anchorPane0, -99.0);
        anchorPane0.setLayoutX(239.0);
        anchorPane0.setLayoutY(12.0);

        mySizeMenu.getButtons().add(mySizeLabel);
        mySizeMenu.getButtons().add(myShrinkButton);
        mySizeMenu.getButtons().add(myEnlargeButton);
        myTopMenuBar.getChildren().add(mySizeMenu);
        myViewToggle.getChildren().add(myViewLabel);
        myViewToggle.getChildren().add(myListView);
        myViewToggle.getChildren().add(myGridView);
        myTopMenuBar.getChildren().add(myViewToggle);
        mySearchBar.getButtons().add(mySearchLabel);
        mySearchBar.getButtons().add(textField);
        myTopMenuBar.getChildren().add(mySearchBar);
        getChildren().add(myTopMenuBar);
        myTreeView.getChildren().add(myTreeList);
        myTreeView.getChildren().add(myAddIconButton);
        myTreeView.getChildren().add(myCustIconButton);
        myTreeView.getChildren().add(myGenIconButton);
        splitPane.getItems().add(myTreeView);
        gridPane.getColumnConstraints().add(columnConstraints);
        gridPane.getColumnConstraints().add(columnConstraints0);
        gridPane.getRowConstraints().add(rowConstraints);
        gridPane.getRowConstraints().add(rowConstraints0);
        gridPane.getRowConstraints().add(rowConstraints1);
        splitPane.getItems().add(gridPane);
        splitPane.getItems().add(scrollBar);
        getChildren().add(splitPane);
    }
}
