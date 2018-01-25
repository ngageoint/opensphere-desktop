package io.opensphere.controlpanels.styles.ui;

import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;

import io.opensphere.controlpanels.styles.controller.EllipseController;
import io.opensphere.controlpanels.styles.model.EllipseModel;
import io.opensphere.core.UnitsRegistry;
import io.opensphere.core.util.fx.FXUtilities;

/**
 * Allows the use to edit the ellipse style.
 */
public class EllipsePanel extends GridPane implements EllipseView
{
    /**
     * Keeps the UI and model synchronized.
     */
    private final EllipseBinder myBinder;

    /**
     * The orientation text input.
     */
    private TextField myOrientationField;

    /**
     * The semi major text input.
     */
    private TextField mySemiMajorField;

    /**
     * The semi major units.
     */
    private ComboBox<String> mySemiMajorUnitsPicker;

    /**
     * The semi minor text input.
     */
    private TextField mySemiMinorField;

    /**
     * The semi minor units.
     */
    private ComboBox<String> mySemiMinorUnitsPicker;

    /**
     * The ellipse controller.
     */
    private final EllipseController myController;

    /**
     * Constructs a new ellipse panel.
     *
     * @param unitsRegistry The units registry.
     * @param model The model to edit.
     */
    public EllipsePanel(UnitsRegistry unitsRegistry, EllipseModel model)
    {
        createUI();
        myController = new EllipseController(unitsRegistry, model);
        myController.applyUnits();
        myBinder = new EllipseBinder(this, model);
    }

    /**
     * Stops editing the model.
     */
    public void close()
    {
        myBinder.close();
    }

    @Override
    public TextField getOrientationField()
    {
        return myOrientationField;
    }

    @Override
    public TextField getSemiMajorField()
    {
        return mySemiMajorField;
    }

    @Override
    public ComboBox<String> getSemiMajorUnitsPicker()
    {
        return mySemiMajorUnitsPicker;
    }

    @Override
    public TextField getSemiMinorField()
    {
        return mySemiMinorField;
    }

    @Override
    public ComboBox<String> getSemiMinorUnitsPicker()
    {
        return mySemiMinorUnitsPicker;
    }

    /**
     * Creates the UI components.
     */
    private void createUI()
    {
        setVgap(5);
        setHgap(5);

        Label semiMajorLabel = new Label("Semi Major:");
        mySemiMajorField = FXUtilities.newNumericText(2);
        mySemiMajorField.setTooltip(new Tooltip("Semi-major axis of the ellipse in the specified units."));
        mySemiMajorUnitsPicker = new ComboBox<>();
        mySemiMajorUnitsPicker.setTooltip(new Tooltip("Semi-major units"));

        add(semiMajorLabel, 0, 0);
        add(mySemiMajorField, 1, 0);
        add(mySemiMajorUnitsPicker, 2, 0);

        Label semiMinorLabel = new Label("Semi Minor:");
        mySemiMinorField = FXUtilities.newNumericText(2);
        mySemiMinorField.setTooltip(new Tooltip("Semi-minor axis of the ellipse in the specified units."));
        mySemiMinorUnitsPicker = new ComboBox<>();
        mySemiMinorUnitsPicker.setTooltip(new Tooltip("Semi-minor units"));

        add(semiMinorLabel, 0, 1);
        add(mySemiMinorField, 1, 1);
        add(mySemiMinorUnitsPicker, 2, 1);

        Label orientationLabel = new Label("Orientation:");
        myOrientationField = FXUtilities.newNumericText(2);
        myOrientationField.setTooltip(new Tooltip("Orientation of the ellipse in degrees from north"));

        add(orientationLabel, 0, 2);
        add(myOrientationField, 1, 2);
    }
}
