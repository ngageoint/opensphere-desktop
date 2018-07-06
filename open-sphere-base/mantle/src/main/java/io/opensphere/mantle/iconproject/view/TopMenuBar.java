package io.opensphere.mantle.iconproject.view;

import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class TopMenuBar extends HBox
{
    private Text mySizeLabel = new LabelMaker("Icon Size");

    private ButtonBuilder myShrinkButton = new ButtonBuilder("-", false);

    private ButtonBuilder myEnlargeButton = new ButtonBuilder("+", false);

    private ButtonBar myViewToggle;

    private Text myViewLabel = new LabelMaker("View Style");

    private RadioButton myListView;

    private RadioButton myGridView;

    private ButtonBar mySearchBar;

    private Text mySearchLabel = new LabelMaker("Filter");

    private ToggleGroup mytoggleGroup;

    private TextField myTextField;

    private ButtonBar mySizeMenu;

    public TopMenuBar()
    {

        mySizeMenu = new ButtonBar();
        myViewToggle = new ButtonBar();
        myListView = new RadioButton();
        mytoggleGroup = new ToggleGroup();
        myGridView = new RadioButton();
        myTextField = new TextField();
        mySearchBar = new ButtonBar();

        myListView.setText("List");
        myListView.setToggleGroup(mytoggleGroup);

        myGridView.setText("Grid");
        myGridView.setToggleGroup(mytoggleGroup);

        mySizeMenu.getButtons().addAll(mySizeLabel, myShrinkButton, myEnlargeButton);

        myViewToggle.getButtons().addAll(myViewLabel, myListView, myGridView);

        mySearchBar.getButtons().addAll(mySearchLabel, myTextField);

        Region region1 = new Region();
        HBox.setHgrow(region1, Priority.ALWAYS);

        Region region2 = new Region();
        HBox.setHgrow(region2, Priority.ALWAYS);

        getChildren().addAll(mySizeMenu, region1, myViewToggle, region2, mySearchBar);
        setAlignment(javafx.geometry.Pos.TOP_CENTER);
    }

}
