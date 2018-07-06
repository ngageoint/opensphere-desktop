package io.opensphere.mantle.iconproject.view;

import org.apache.log4j.Logger;

import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class TopMenuBar extends AnchorPane
{

    private ButtonBar mySizeMenu;

    private AnchorPane myTopMenuBar;

    private Text mySizeLabel;

    private Button myShrinkButton;

    private Button myEnlargeButton;

    private HBox myViewToggle;

    private Text myViewLabel;

    private RadioButton myListView;

    private RadioButton myGridView;

    private ButtonBar mySearchBar;

    private Text mySearchLabel;

    private ToggleGroup mytoggleGroup;

    private TextField myTextField;

    public TopMenuBar()
    {

        mySizeMenu = new ButtonBar();
        mySizeLabel = new Text();
        myShrinkButton = new Button();
        myEnlargeButton = new Button();
        myViewToggle = new HBox();
        myViewLabel = new Text();

        myListView = new RadioButton();
        mytoggleGroup = new ToggleGroup();
        myGridView = new RadioButton();
        myTextField = new TextField();
        mySearchBar = new ButtonBar();
        mySearchLabel = new Text();

        setLeftAnchor(mySizeMenu, 10.0);

        mySizeMenu.setLayoutY(8.0);

        mySizeLabel.setStrokeType(javafx.scene.shape.StrokeType.OUTSIDE);
        mySizeLabel.setStrokeWidth(0.0);
        mySizeLabel.setText("Icon Size:");

        myShrinkButton.setMinWidth(5.0);
        myShrinkButton.setMnemonicParsing(false);

        myShrinkButton.setPrefHeight(25.0);
        myShrinkButton.setText("-");

        myEnlargeButton.setMinWidth(5.0);
        myEnlargeButton.setMnemonicParsing(false);

        myEnlargeButton.setPrefHeight(25.0);
        myEnlargeButton.setText("+");

        setBottomAnchor(myViewToggle, -6.0);
        setLeftAnchor(myViewToggle, 397.0);
        setRightAnchor(myViewToggle, 383.0);
        setTopAnchor(myViewToggle, 14.0);

        myViewToggle.setAlignment(javafx.geometry.Pos.TOP_CENTER);
        myViewToggle.setCache(true);
        myViewToggle.setCacheHint(javafx.scene.CacheHint.SPEED);

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
        myListView.setToggleGroup(mytoggleGroup);

        myGridView.setMnemonicParsing(false);

        myGridView.setText("Grid");
        myGridView.setToggleGroup(mytoggleGroup);
        myGridView.setFont(new Font(14.0));

        setRightAnchor(mySearchBar, 10.0);

        mySearchBar.setLayoutY(8.0);
        mySearchBar.setPrefHeight(40.0);
        mySearchBar.setPrefWidth(247.0);

        mySearchLabel.setStrokeType(javafx.scene.shape.StrokeType.OUTSIDE);
        mySearchLabel.setStrokeWidth(0.0);
        mySearchLabel.setText("Filter:");
        mySearchLabel.setFont(new Font(14.0));

        myTextField.setPrefHeight(25.0);
        myTextField.setPrefWidth(141.0);

        mySizeMenu.getButtons().add(mySizeLabel);
        mySizeMenu.getButtons().add(myShrinkButton);
        mySizeMenu.getButtons().add(myEnlargeButton);

        myViewToggle.getChildren().add(myViewLabel);
        myViewToggle.getChildren().add(myListView);
        myViewToggle.getChildren().add(myGridView);

        mySearchBar.getButtons().add(mySearchLabel);
        mySearchBar.getButtons().add(myTextField);

        getChildren().addAll(mySizeMenu, myViewToggle, mySearchBar);
    }

}
