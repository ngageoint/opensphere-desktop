package io.opensphere.core.geometry.debug.ellipsoid;

import java.awt.Dimension;
import java.text.NumberFormat;

import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.util.converter.NumberStringConverter;

import javax.swing.JMenuItem;

import io.opensphere.core.Toolbox;
import io.opensphere.core.util.fx.FXUtilities;
import io.opensphere.core.util.fx.JFXDialog;

/**
 * The UI that allows a user to draw ellipsoids on globe in debug mode.
 */
public class EllipsoidDebugUI extends GridPane
{
    /**
     * The model containing the parameters.
     */
    private final EllipsoidDebugModel myModel;

    /**
     * The debug menu item to draw an ellipsoid on globe.
     *
     * @param toolbox The sytem toolbox.
     * @return The menu item.
     */
    public static JMenuItem getDebugMenu(Toolbox toolbox)
    {
        JMenuItem menu = new JMenuItem("Ellipsoid");
        menu.addActionListener((a) -> launchDialog(toolbox));
        return menu;
    }

    /**
     * Launches the dialog.
     *
     * @param toolbox The system toolbox.
     */
    private static void launchDialog(Toolbox toolbox)
    {
        EllipsoidDebugController controller = new EllipsoidDebugController(toolbox.getMapManager(), toolbox.getGeometryRegistry(),
                toolbox.getPreferencesRegistry());
        //@formatter:off
        JFXDialog dialog = new JFXDialog(toolbox.getUIRegistry().getMainFrameProvider().get(),
                "Ellipsoid", () -> new EllipsoidDebugUI(toolbox, controller.getModel()));
        //@formatter:on
        dialog.setSize(new Dimension(390, 550));
        dialog.setResizable(true);
        dialog.setLocationRelativeTo(toolbox.getUIRegistry().getMainFrameProvider().get());
        dialog.setModal(false);
        dialog.setAcceptEar(controller);
        dialog.setVisible(true);
    }

    /**
     * Constructs a new UI must be called on fx thread.
     *
     * @param toolbox The system toolbox.
     * @param model The model.
     */
    public EllipsoidDebugUI(Toolbox toolbox, EllipsoidDebugModel model)
    {
        myModel = model;
        createUI();
    }

    /**
     * Sets the color on the model.
     *
     * @param observable The color.
     * @param oldValue The old value.
     * @param newValue The new value.
     */
    private void colorToModel(ObservableValue<? extends Color> observable, Color oldValue, Color newValue)
    {
        java.awt.Color color = FXUtilities.toAwtColor(newValue);
        myModel.setColor(color);
    }

    /**
     * Creates the fields that edit the ellipsoids axis.
     *
     * @param nf The number format.
     */
    private void createAxisFields(NumberFormat nf)
    {
        Label label = new Label("Axis A (Meters):");
        TextField field = new TextField();
        Bindings.bindBidirectional(field.textProperty(), myModel.getAxisA(), new NumberStringConverter(nf));
        this.add(label, 0, 3);
        this.add(field, 1, 3);

        label = new Label("Axis B (Meters):");
        field = new TextField();
        Bindings.bindBidirectional(field.textProperty(), myModel.getAxisB(), new NumberStringConverter(nf));
        this.add(label, 0, 4);
        this.add(field, 1, 4);

        label = new Label("Axis C (Meters):");
        field = new TextField();
        Bindings.bindBidirectional(field.textProperty(), myModel.getAxisC(), new NumberStringConverter(nf));
        this.add(label, 0, 5);
        this.add(field, 1, 5);
    }

    /**
     * Creates the location fields.
     *
     * @param nf The number format.
     */
    private void createLocationFields(NumberFormat nf)
    {
        Label label = new Label("Latitude:");
        TextField field = new TextField();
        Bindings.bindBidirectional(field.textProperty(), myModel.getLatitude(), new NumberStringConverter(nf));
        this.add(label, 0, 0);
        this.add(field, 1, 0);

        label = new Label("Longitude:");
        field = new TextField();
        Bindings.bindBidirectional(field.textProperty(), myModel.getLongitude(), new NumberStringConverter(nf));
        this.add(label, 0, 1);
        this.add(field, 1, 1);

        label = new Label("Altitude (Meters):");
        field = new TextField();
        Bindings.bindBidirectional(field.textProperty(), myModel.getAltitude(), new NumberStringConverter(nf));
        this.add(label, 0, 2);
        this.add(field, 1, 2);
    }

    /**
     * Creates the UI.
     */
    private void createUI()
    {
        NumberFormat nf = NumberFormat.getNumberInstance();
        nf.setMaximumFractionDigits(10);
        NumberFormat intNf = NumberFormat.getNumberInstance();
        intNf.setMaximumFractionDigits(0);

        setVgap(5);
        setHgap(5);

        createLocationFields(nf);
        createAxisFields(nf);

        Label label = new Label("Orientaion °:");
        TextField field = new TextField();
        Bindings.bindBidirectional(field.textProperty(), myModel.getHeading(), new NumberStringConverter(nf));
        this.add(label, 0, 6);
        this.add(field, 1, 6);

        label = new Label("Pitch °:");
        field = new TextField();
        Bindings.bindBidirectional(field.textProperty(), myModel.getPitch(), new NumberStringConverter(nf));
        this.add(label, 0, 7);
        this.add(field, 1, 7);

        label = new Label("Roll °:");
        field = new TextField();
        Bindings.bindBidirectional(field.textProperty(), myModel.getRoll(), new NumberStringConverter(nf));
        this.add(label, 0, 8);
        this.add(field, 1, 8);

        label = new Label("Color:");
        ColorPicker colorPicker = new ColorPicker(FXUtilities.fromAwtColor(myModel.getColor()));
        colorPicker.valueProperty().addListener(this::colorToModel);
        this.add(label, 0, 9);
        this.add(colorPicker, 1, 9);

        label = new Label("Opacity (0 - 255):");
        field = new TextField();
        Bindings.bindBidirectional(field.textProperty(), myModel.getOpacity(), new NumberStringConverter(intNf));
        this.add(label, 0, 10);
        this.add(field, 1, 10);

        label = new Label("Quality:");
        field = new TextField();
        Bindings.bindBidirectional(field.textProperty(), myModel.getQuality(), new NumberStringConverter(intNf));
        this.add(label, 0, 11);
        this.add(field, 1, 11);

        CheckBox checkBox = new CheckBox("Use Lighting");
        Bindings.bindBidirectional(checkBox.selectedProperty(), myModel.isUseLighting());
        this.add(checkBox, 0, 12);

        checkBox = new CheckBox("Remove Previous Ellipsoid");
        Bindings.bindBidirectional(checkBox.selectedProperty(), myModel.isRemovePrevious());
        this.add(checkBox, 0, 13);
    }
}
