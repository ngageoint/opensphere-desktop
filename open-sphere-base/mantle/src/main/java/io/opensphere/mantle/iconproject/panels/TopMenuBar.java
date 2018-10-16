package io.opensphere.mantle.iconproject.panels;

import javafx.geometry.Pos;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.text.Text;

import org.apache.commons.lang.StringUtils;

import io.opensphere.mantle.iconproject.impl.ButtonBuilder;
import io.opensphere.mantle.iconproject.impl.LabelMaker;
import io.opensphere.mantle.iconproject.model.PanelModel;
import io.opensphere.mantle.iconproject.model.ViewStyle;

/** An HBox containing display size controls, view style, and filter options. */
public class TopMenuBar extends HBox
{
    /** The label for the icon display size. */
    private final Text mySizeLabel = new LabelMaker("Icon Size");

    /** The button to decrease icon display size. */
    private final ButtonBuilder myShrinkButton = new ButtonBuilder("-", false);

    /** The button to increase icon display size. */
    private final ButtonBuilder myEnlargeButton = new ButtonBuilder("+", false);

    /** The bar containing the sizing options. */
    private final ButtonBar mySizeMenu;

    /** The bar containing the icon display toggles. */
    private final ButtonBar myViewToggle;

    /** The label for the icon display view. */
    private final Text myViewLabel = new LabelMaker("View Style");

    /** The two way radio button for selecting list style. */
    private final RadioButton myListView = new RadioButton();

    /** The two way radio button for selecting grid style. */
    private final RadioButton myGridView = new RadioButton();

    /** The toggle group comprised of the two view styles. */
    private final ToggleGroup myToggleGroup = new ToggleGroup();

    /** The bar containing the filter functionality. */
    private final ButtonBar mySearchBar;

    /** The label for the filter text input. */
    private final Text mySearchLabel = new LabelMaker("Filter");

    /** The bar to enter text to filter icon results. */
    private final TextField myTextField = new TextField();

    /** The model used for the UI. */
    private final PanelModel myPanelModel;

    /**
     * Creates the top menu bar of the icon manager UI.
     *
     * @param panelModel the current UI model.
     */
    public TopMenuBar(PanelModel panelModel)
    {
        mySearchBar = createFilterBar();
        myViewToggle = createViewToggle();
        mySizeMenu = createSizeMenu();
        myPanelModel = panelModel;

        Region region1 = new Region();
        HBox.setHgrow(region1, Priority.ALWAYS);

        Region region2 = new Region();
        HBox.setHgrow(region2, Priority.ALWAYS);

        getChildren().addAll(mySizeMenu, region1, myViewToggle, region2, mySearchBar);
        setAlignment(javafx.geometry.Pos.TOP_CENTER);
    }

    /**
     * Creates the search bar containing the label and text entry field for
     * filtering icons.
     *
     * @return a JavaFX ButtonBar containing filter control elements.
     */
    private ButtonBar createFilterBar()
    {
        ButtonBar searchBar = new ButtonBar();
        searchBar.getButtons().addAll(mySearchLabel, myTextField);
        myTextField.setOnKeyTyped(event -> filterIcons());
        return searchBar;
    }

    /**
     * Filters icons shown by matching the entered text.
     */
    private void filterIcons()
    {
        if (StringUtils.isEmpty(myTextField.getText()))
        {
            myPanelModel.setUseFilteredList(false);
            myPanelModel.getViewModel().getMainPanel().refresh();
            return;
        }
        myPanelModel.getFilteredRecordList().clear();
        myPanelModel.getRecordList().stream().filter(r -> StringUtils.containsIgnoreCase(r.getName(), myTextField.getText()))
                .forEach(r -> myPanelModel.getFilteredRecordList().add(r));
        myPanelModel.setUseFilteredList(true);
        myPanelModel.getViewModel().getMainPanel().refresh();
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
        ButtonBar sizeMenu = new ButtonBar();
        myEnlargeButton.setOnAction(event -> myPanelModel.getCurrentTileWidth().set(myPanelModel.getCurrentTileWidth().get() + 10));
        myEnlargeButton.setTooltip(new Tooltip("Increase Icon Size"));

        myShrinkButton.setOnAction(event -> myPanelModel.getCurrentTileWidth().set(myPanelModel.getCurrentTileWidth().get() - 10));
        myShrinkButton.setTooltip(new Tooltip("Decrease Icon Size"));
        sizeMenu.getButtons().addAll(mySizeLabel, myShrinkButton, myEnlargeButton);
        return sizeMenu;
    }

    /**
     * Creates the bar containing list and grid view selection buttons.
     *
     * @return A JavaFX ButtonBar containing the view control elements.
     */
    public ButtonBar createViewToggle()
    {
        ButtonBar viewToggle = new ButtonBar();
        myListView.setText("List");
        myListView.setToggleGroup(myToggleGroup);
        myGridView.setText("Grid");
        myGridView.setToggleGroup(myToggleGroup);
        myGridView.setSelected(true);
        myListView.setOnAction(event ->
        {
            myPanelModel.getViewStyle().set(ViewStyle.LIST);
            myShrinkButton.setDisable(true);
            myEnlargeButton.setDisable(true);
            myPanelModel.getViewModel().getMainPanel().refresh();
        });
        myGridView.setOnAction(event ->
        {
            myPanelModel.getViewStyle().set(ViewStyle.GRID);
            myShrinkButton.setDisable(false);
            myEnlargeButton.setDisable(false);
            myPanelModel.getViewModel().getMainPanel().refresh();
        });

        viewToggle.getButtons().addAll(myViewLabel, myGridView, myListView);

        return viewToggle;
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

