package io.opensphere.mantle.iconproject.view;

import io.opensphere.mantle.iconproject.impl.ButtonBuilder;
import io.opensphere.mantle.iconproject.impl.LabelMaker;
import javafx.beans.property.Property;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
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

    private Property<Number> mySize;

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
        myListView.setOnMouseClicked(event ->
        {
            MainPanel.changeTop(true);
        });
        myGridView.setText("Grid");
        myGridView.setToggleGroup(mytoggleGroup);
        myGridView.setSelected(true);
        myGridView.setOnMouseClicked(event ->
        {
            MainPanel.changeTop(false);
        });

        mySizeMenu.getButtons().addAll(mySizeLabel, myShrinkButton, myEnlargeButton);
        myViewToggle.getButtons().addAll(myViewLabel, myListView, myGridView);
        mySearchBar.getButtons().addAll(mySearchLabel, myTextField);

        Region region1 = new Region();
        HBox.setHgrow(region1, Priority.ALWAYS);

        Region region2 = new Region();
        HBox.setHgrow(region2, Priority.ALWAYS);

        HBox myHbox = createControlPanel();

        getChildren().addAll(mySizeMenu, region1, myViewToggle, region2, mySearchBar);
        setAlignment(javafx.geometry.Pos.TOP_CENTER);
    }

    public HBox createControlPanel()
    {
        HBox box = new HBox(8);
        box.setAlignment(Pos.CENTER_LEFT);
        Slider slider = createSlider();
        HBox.setHgrow(slider, Priority.ALWAYS);
        box.getChildren().addAll(slider);
        return box;
    }

    /**
     * Creates the rotation slider.
     *
     * @return the slider
     */
    private Slider createSlider()
    {
        Slider slider = new Slider(-180, 180, 0);
        slider.setShowTickMarks(false);
        slider.setShowTickLabels(false);
        return slider;

    }
}
