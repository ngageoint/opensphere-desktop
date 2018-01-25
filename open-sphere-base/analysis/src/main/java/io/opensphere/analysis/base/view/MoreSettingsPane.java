package io.opensphere.analysis.base.view;

import java.awt.Window;
import java.util.EnumSet;

import javafx.collections.FXCollections;
import javafx.embed.swing.JFXPanel;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

import javax.swing.JDialog;
import javax.swing.SwingUtilities;

import io.opensphere.analysis.base.model.BinType;
import io.opensphere.analysis.base.model.Orientation;
import io.opensphere.analysis.base.model.SettingsModel;
import io.opensphere.analysis.base.model.SortMethod;
import io.opensphere.analysis.binning.criteria.TimeBinType;
import io.opensphere.core.util.fx.FXUtilities;

/** Dialog for more tool settings. */
public class MoreSettingsPane extends JDialog
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /** The settings model. */
    private final SettingsModel mySettingsModel;

    /** The parent frame. */
    private final Window myParentWindow;

    /** The panel. */
    private JFXPanel myPanel;

    /**
     * Constructor.
     *
     * @param settingsModel The settings model
     * @param parentWindow The parent window
     */
    public MoreSettingsPane(SettingsModel settingsModel, Window parentWindow)
    {
        super(parentWindow, "Histogram Settings");
        mySettingsModel = settingsModel;
        myParentWindow = parentWindow;
        myPanel = new JFXPanel();
        FXUtilities.runOnFXThread(this::startUI);
    }

    /** Starts building the UI. */
    private void startUI()
    {
        Accordion accordion = new Accordion();
        accordion.getPanes().addAll(createBinControlPane(), createChartSettingsPane(), createTitleSettingsPane(),
                createAxisSettingsPane());
        accordion.setExpandedPane(accordion.getPanes().get(0));
        myPanel.setScene(FXUtilities.addDesktopStyle(new Scene(accordion)));
        SwingUtilities.invokeLater(this::finishUI);
    }

    /** Finishes building the UI. */
    private void finishUI()
    {
        add(myPanel);
        pack();
        setSize(360, 350);
        setLocationRelativeTo(myParentWindow);
    }

    /**
     * Creates the bin control pane.
     *
     * @return the pane
     */
    private TitledPane createBinControlPane()
    {
        VBox box = new VBox(10);
        box.setAlignment(Pos.TOP_LEFT);
        CheckBox naCheckBox = SettingsUtilities.createCheckBox("Show N/A bin", mySettingsModel.showNABinProperty());
        box.getChildren().addAll(naCheckBox, createNumericBinPane(), createTimeBinPane());
        return new TitledPane("Bin Controls", box);
    }

    /**
     * Creates the chart settings pane.
     *
     * @return the pane
     */
    private TitledPane createChartSettingsPane()
    {
        VBox box = new VBox(10);
        box.setAlignment(Pos.TOP_LEFT);
        box.getChildren().addAll(createSortMethodPane(), createOrientationPane(), createBackgroundColorPicker(),
                createForegroundColorPicker());
        return new TitledPane("Chart Settings", box);
    }

    /**
     * Creates the title settings pane.
     *
     * @return the title settings pane
     */
    private TitledPane createTitleSettingsPane()
    {
        VBox box = new VBox(10);
        box.setAlignment(Pos.TOP_LEFT);
        CheckBox showTitleCheckBox = SettingsUtilities.createCheckBox("Show Title", mySettingsModel.showTitleProperty());
        box.getChildren().addAll(showTitleCheckBox, createTitlePane());
        TitledPane labelPane = new TitledPane("Title", box);
        return labelPane;
    }

    /**
     * Creates the axis settings pane.
     *
     * @return the axis settings pane
     */
    private TitledPane createAxisSettingsPane()
    {
        VBox box = new VBox(10);
        box.setAlignment(Pos.TOP_LEFT);
        box.getChildren().add(createAxisPane());
        TitledPane labelPane = new TitledPane("Axis Settings", box);
        return labelPane;
    }

    /**
     * Creates the numeric bin type combo box.
     *
     * @return the combo box
     */
    private Node createNumericBinPane()
    {
        ComboBox<BinType> combo = new ComboBox<>(FXCollections.observableArrayList(EnumSet.of(BinType.RANGE, BinType.UNIQUE)));
        combo.valueProperty().bindBidirectional(mySettingsModel.numericBinTypeProperty());
        return FXUtilities.newHBox(new Label("Numeric Bin Type:"), combo);
    }

    /**
     * Creates the time bin type combo box.
     *
     * @return the combo box
     */
    private Node createTimeBinPane()
    {
        ComboBox<TimeBinType> combo = new ComboBox<>(FXCollections.observableArrayList(TimeBinType.values()));
        combo.valueProperty().bindBidirectional(mySettingsModel.timeBinTypeProperty());
        return FXUtilities.newHBox(new Label("Time Bin Type:"), combo);
    }

    /**
     * Creates the sort method combo box.
     *
     * @return the combo box
     */
    private Node createSortMethodPane()
    {
        ComboBox<SortMethod> combo = new ComboBox<>(FXCollections.observableArrayList(SortMethod.values()));
        combo.valueProperty().bindBidirectional(mySettingsModel.sortMethodProperty());
        return FXUtilities.newHBox(new Label("Sort Method:"), combo);
    }

    /**
     * Creates the chart orientation combo box.
     *
     * @return the combo box
     */
    private Node createOrientationPane()
    {
        ComboBox<Orientation> combo = new ComboBox<>(FXCollections.observableArrayList(Orientation.values()));
        combo.valueProperty().bindBidirectional(mySettingsModel.orientationProperty());
        return FXUtilities.newHBox(new Label("Orientation:"), combo);
    }

    /**
     * Creates the background color picker.
     *
     * @return the picker
     */
    private Node createBackgroundColorPicker()
    {
        ColorPicker colorPicker = new ColorPicker(mySettingsModel.backgroundColorProperty().get());
        colorPicker.valueProperty().addListener((obs, old, color) -> mySettingsModel.backgroundColorProperty().set(color));
        mySettingsModel.backgroundColorProperty().addListener((obs, old, color) -> colorPicker.setValue(color));
        Button resetButton = new Button("Reset");
        resetButton.setOnAction(e -> mySettingsModel.backgroundColorProperty().set(null));
        return FXUtilities.newHBox(new Label("Background:"), colorPicker, resetButton);
    }

    /**
     * Creates the foreground color picker.
     *
     * @return the picker
     */
    private Node createForegroundColorPicker()
    {
        ColorPicker colorPicker = new ColorPicker(mySettingsModel.foregroundColorProperty().get());
        colorPicker.valueProperty().addListener((obs, old, color) -> mySettingsModel.foregroundColorProperty().set(color));
        mySettingsModel.foregroundColorProperty().addListener((obs, old, color) -> colorPicker.setValue(color));
        Button resetButton = new Button("Reset");
        resetButton.setOnAction(e -> mySettingsModel.foregroundColorProperty().set(null));
        return FXUtilities.newHBox(new Label("Foreground:"), colorPicker, resetButton);
    }

    /**
     * Creates the title settings pane fields.
     *
     * @return the title settings pane fields
     */
    private Node createTitlePane()
    {
        TextField textField = new TextField(mySettingsModel.titleTextProperty().get());
        textField.textProperty().bindBidirectional(mySettingsModel.titleTextProperty());
        HBox textBox = FXUtilities.newHBox(new Label("Text: ", textField));

        ColorPicker colorPicker = new ColorPicker(mySettingsModel.getTitleLabelModel().colorProperty().get());
        colorPicker.valueProperty()
                .addListener((obs, old, color) -> mySettingsModel.getTitleLabelModel().colorProperty().set(color));
        mySettingsModel.getTitleLabelModel().colorProperty().addListener((obs, old, color) -> colorPicker.setValue(color));
        HBox colorBox = FXUtilities.newHBox(new Label("Color:"), colorPicker);

        ComboBox<String> fontPicker = new ComboBox<>(FXCollections.observableArrayList(Font.getFamilies()));
        fontPicker.valueProperty()
                .addListener((obs, old, fontFamily) -> mySettingsModel.getTitleLabelModel().fontProperty().set(fontFamily));
        fontPicker.getSelectionModel().select(mySettingsModel.getTitleLabelModel().fontProperty().get());
        HBox fontBox = FXUtilities.newHBox(new Label("Font:"), fontPicker);

        Spinner<Integer> sizePicker = new Spinner<>(2, 50, mySettingsModel.getTitleLabelModel().sizeProperty().get(), 2);
        sizePicker.valueProperty()
                .addListener((obs, old, size) -> mySettingsModel.getTitleLabelModel().sizeProperty().set(size.intValue()));
        HBox sizeBox = FXUtilities.newHBox(new Label("Size:"), sizePicker);

        return FXUtilities.newVBox(textBox, colorBox, fontBox, sizeBox);
    }

    /**
     * Creates the axis label settings pane fields.
     *
     * @return the axis label settings pane fields
     */
    private Node createAxisPane()
    {
        TextField xTextField = new TextField(mySettingsModel.categoryAxisTextProperty().get());
        xTextField.textProperty().bindBidirectional(mySettingsModel.categoryAxisTextProperty());
        HBox xAxisTextBox = FXUtilities.newHBox(new Label("Category Axis Text:"), xTextField);

        TextField yTextField = new TextField(mySettingsModel.countAxisTextProperty().get());
        yTextField.textProperty().bindBidirectional(mySettingsModel.countAxisTextProperty());
        HBox yAxisTextBox = FXUtilities.newHBox(new Label("Count Axis Text:"), yTextField);

        ColorPicker colorPicker = new ColorPicker(mySettingsModel.getAxisLabelModel().colorProperty().get());
        colorPicker.valueProperty()
                .addListener((obs, old, color) -> mySettingsModel.getAxisLabelModel().colorProperty().set(color));
        mySettingsModel.getAxisLabelModel().colorProperty().addListener((obs, old, color) -> colorPicker.setValue(color));
        HBox colorBox = FXUtilities.newHBox(new Label("Color:"), colorPicker);

        ComboBox<String> fontPicker = new ComboBox<>(FXCollections.observableArrayList(Font.getFamilies()));
        fontPicker.valueProperty()
                .addListener((obs, old, fontFamily) -> mySettingsModel.getAxisLabelModel().fontProperty().set(fontFamily));
        fontPicker.getSelectionModel().select(mySettingsModel.getAxisLabelModel().fontProperty().get());
        HBox fontBox = FXUtilities.newHBox(new Label("Font:"), fontPicker);

        Spinner<Integer> sizePicker = new Spinner<>(2, 50, mySettingsModel.getAxisLabelModel().sizeProperty().get(), 2);
        sizePicker.valueProperty()
                .addListener((obs, old, size) -> mySettingsModel.getAxisLabelModel().sizeProperty().set(size.intValue()));
        HBox sizeBox = FXUtilities.newHBox(new Label("Size:"), sizePicker);

        return FXUtilities.newVBox(xAxisTextBox, yAxisTextBox, colorBox, fontBox, sizeBox);
    }
}
