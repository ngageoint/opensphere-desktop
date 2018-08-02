package io.opensphere.mantle.iconproject.panels;

import io.opensphere.mantle.iconproject.impl.ButtonBuilder;
import io.opensphere.mantle.iconproject.impl.LabelMaker;
import io.opensphere.mantle.iconproject.model.PanelModel;
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
    /** The label for the icon display size. */
    private Text mySizeLabel = new LabelMaker("Icon Size");

    /** The button to decrease icon display size. */
    private ButtonBuilder myShrinkButton = new ButtonBuilder("-", false);

    /** The button to increase icon display size. */
    private ButtonBuilder myEnlargeButton = new ButtonBuilder("+", false);

    /** The bar containing the sizing options. */
    private ButtonBar mySizeMenu = new ButtonBar();

    /** The bar containing the icon display toggles. */
    private ButtonBar myViewToggle = new ButtonBar();

    /** The label for the icon display view. */
    private Text myViewLabel = new LabelMaker("View Style");

    /** The two way radio button for selecting list style. */
    private RadioButton myListView = new RadioButton();

    /** The two way radio button for selecting grid style. */
    private RadioButton myGridView = new RadioButton();

    /** The toggle group comprised of the two view styles. */
    private ToggleGroup mytoggleGroup = new ToggleGroup();

    /** The bar containing the filter functionality. */
    private ButtonBar mySearchBar = new ButtonBar();

    /** The label for the filter text input. */
    private Text mySearchLabel = new LabelMaker("Filter");

    /** The bar to enter text to filter icon results. */
    private TextField myTextField = new TextField();

    private PanelModel myPanelModel;

    /** Creates the top menu bar of the icon manager UI. */
    public TopMenuBar(PanelModel thePanelModel)
    {
        mySearchBar = createFilterBar();
        myViewToggle = createViewToggle();
        mySizeMenu = createSizeMenu();
        myPanelModel = thePanelModel;

        Region region1 = new Region();
        HBox.setHgrow(region1, Priority.ALWAYS);

        Region region2 = new Region();
        HBox.setHgrow(region2, Priority.ALWAYS);

        getChildren().addAll(mySizeMenu, region1, myViewToggle, region2, mySearchBar);
        setAlignment(javafx.geometry.Pos.TOP_CENTER);
    }

    /**
     * Creates the search bar containing the label and ext entry field for
     * filtering icons.
     *
     * @return a JavaFX ButtonBar containing filter control elements.
     */
    private ButtonBar createFilterBar()
    {
        ButtonBar theSearchBar = new ButtonBar();
        theSearchBar.getButtons().addAll(mySearchLabel, myTextField);
        return theSearchBar;
    }

    /**
     * Creates the sliders to adjust the icon display size. Currently not in use
     * but may be used instead of the Plus/ Minus style buttons.
     *
     * @return A JavaFX HBox containing icon display control elements.
     */
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
     * Creates the size menu options.
     *
     * @return A JavaFX ButtonBar with the size control elements.
     */
    public ButtonBar createSizeMenu()
    {
        ButtonBar theSizeMenu = new ButtonBar();
        myEnlargeButton.setOnAction(event ->
        {
            int origTile = myPanelModel.getTileWidth().get();
            myPanelModel.getTileWidth().set(origTile + 10);
            myPanelModel.getViewModel().getMainPanel().myScrollPane.setContent(new GridBuilder(myPanelModel));
        });
        
        myShrinkButton.setOnAction(event ->
        {
            int origTile = myPanelModel.getTileWidth().get();
            myPanelModel.getTileWidth().set(origTile - 10);
        });
        theSizeMenu.getButtons().addAll(mySizeLabel, myShrinkButton, myEnlargeButton);
        return theSizeMenu;
    }

    /**
     * Creates the bar containing list and grid view selection buttons.
     *
     * @return A JavaFX ButtonBar containing the view control elements.
     */
    public ButtonBar createViewToggle()
    {
        ButtonBar theViewToggle = new ButtonBar();
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

        theViewToggle.getButtons().addAll(myViewLabel, myListView, myGridView);

        return theViewToggle;
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
