package io.opensphere.analysis.base.view;

import java.text.DecimalFormat;
import java.text.ParseException;

import javafx.embed.swing.JFXPanel;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import javax.swing.JDialog;
import javax.swing.SwingUtilities;

import io.opensphere.analysis.base.model.BinType;
import io.opensphere.analysis.base.model.SettingsModel;
import io.opensphere.core.util.fx.FXUtilities;
import io.opensphere.core.util.image.IconUtil.IconType;

/** Pane for tool settings. */
public class SettingsPane extends VBox
{
    /** The parent panel. */
    private final JFXPanel myParentPanel;

    /** The settings model. */
    private final SettingsModel mySettingsModel;

    /** The more settings dialog. */
    private JDialog myMoreSettingsDialog;

    /**
     * Constructor.
     *
     * @param parentPanel the parent panel
     * @param settingsModel The settings model
     */
    public SettingsPane(JFXPanel parentPanel, SettingsModel settingsModel)
    {
        super(5);
        mySettingsModel = settingsModel;
        myParentPanel = parentPanel;
        buildUI();
    }

    /** Builds the UI. */
    private void buildUI()
    {
        setPadding(new Insets(5));
        setAlignment(Pos.TOP_LEFT);
        getChildren().addAll(createTopPane(), createBottomPane());
    }

    /**
     * Creates the top pane.
     *
     * @return the top pane
     */
    private Pane createTopPane()
    {
        return SettingsUtilities.createRow(new LayerPane(mySettingsModel), createColumnPane(), FXUtilities.newHSpacer());
    }

    /**
     * Creates the bottom pane.
     *
     * @return the bottom pane
     */
    private Pane createBottomPane()
    {
        CheckBox allTime = SettingsUtilities.createCheckBox("All Time", mySettingsModel.allTimeProperty());
        return SettingsUtilities.createRow(allTime, FXUtilities.newHSpacer(0), createBinControlPane(), FXUtilities.newHSpacer(),
                createMoreSettingsButton());
    }

    /**
     * Creates the column selection pane.
     *
     * @return the pane
     */
    private Pane createColumnPane()
    {
        ComboBox<String> combo = new ComboBox<>(mySettingsModel.availableColumnsProperty());
        combo.valueProperty().bindBidirectional(mySettingsModel.selectedColumnProperty());
        return FXUtilities.newHBox(new Label("Column:"), combo);
    }

    /**
     * Creates the bin control pane.
     *
     * @return the pane
     */
    private Pane createBinControlPane()
    {
        Label binWidthLabel = new Label("Bin Width:");
        TextField binWidth = new TextField();
        binWidth.setMaxWidth(60);
        final DecimalFormat decimalFormat = new DecimalFormat("#.###");
        binWidth.textProperty().bindBidirectional(mySettingsModel.binWidthProperty(), new StringConverter<Number>()
        {
            @Override
            public String toString(Number number)
            {
                return decimalFormat.format(number);
            }

            @Override
            public Number fromString(String string)
            {
                Number number = null;
                try
                {
                    number = (Number)decimalFormat.parseObject(string);
                    binWidth.setStyle(null);
                }
                catch (ParseException e)
                {
                    binWidth.setStyle("-fx-text-fill: red;");
                }
                return number;
            }
        });

        CheckBox showEmpty = SettingsUtilities.createCheckBox("Show empty bins", mySettingsModel.showEmptyBinsProperty());

        FXUtilities.addListenerAndInit(mySettingsModel.binTypeProperty(), binType ->
        {
            binWidthLabel.setDisable(binType != BinType.RANGE);
            binWidth.setDisable(binType != BinType.RANGE);
            showEmpty.setDisable(binType == BinType.UNIQUE);
        });

        return FXUtilities.newHBox(binWidthLabel, binWidth, FXUtilities.newHSpacer(0), showEmpty);
    }

    /**
     * Creates the more settings button.
     *
     * @return the button
     */
    private Button createMoreSettingsButton()
    {
        Button button = FXUtilities.newIconButton("Settings", IconType.COG, null);
        button.setOnAction(e ->
        {
            SwingUtilities.invokeLater(this::showMoreSettingsDialog);
        });
        return button;
    }

    /** Shows the more settings dialog and creates it first if needed. */
    private void showMoreSettingsDialog()
    {
        if (myMoreSettingsDialog == null)
        {
            myMoreSettingsDialog = new MoreSettingsPane(mySettingsModel, SwingUtilities.getWindowAncestor(myParentPanel));
        }
        myMoreSettingsDialog.setVisible(true);
    }

    /** Closes the more settings dialog. */
    public void closeDialog()
    {
        SwingUtilities.invokeLater(() ->
        {
            if (myMoreSettingsDialog != null)
            {
                myMoreSettingsDialog.dispose();
            }
        });
    }
}
