package io.opensphere.controlpanels.styles.controller;

import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import io.opensphere.controlpanels.styles.model.EllipseModel;
import io.opensphere.core.UnitsRegistry;
import io.opensphere.core.units.length.Length;
import io.opensphere.core.util.collections.New;

/**
 * Provides the available units for the semi major and semi minor unit pickers.
 */
public class EllipseController
{
    /**
     * The ellipse model.
     */
    private final EllipseModel myModel;

    /**
     * The units registry.
     */
    private final UnitsRegistry myUnitsRegistry;

    /**
     * Constructs a new ellipse controller.
     *
     * @param unitsRegistry the units registry.
     * @param model The ellipse model.
     */
    public EllipseController(UnitsRegistry unitsRegistry, EllipseModel model)
    {
        myUnitsRegistry = unitsRegistry;
        myModel = model;
    }

    /**
     * Gets the available length units, populates the model, and sets default
     * units in the model.
     */
    public void applyUnits()
    {
        List<String> units = New.list();
        Collection<Class<? extends Length>> availableUnits = myUnitsRegistry.getAvailableUnits(Length.class, false);
        for (Class<? extends Length> lengthType : availableUnits)
        {
            String selectionLabel = Length.getSelectionLabel(lengthType);
            units.add(selectionLabel);
        }

        myModel.getAvailableUnits().addAll(units);

        if (StringUtils.isEmpty(myModel.getSemiMajorUnits()) || StringUtils.isEmpty(myModel.getSemiMinorUnits()))
        {
            String defaultUnit = Length.getSelectionLabel(myUnitsRegistry.getPreferredUnits(Length.class));
            if (!units.contains(defaultUnit))
            {
                defaultUnit = units.get(0);
            }

            if (StringUtils.isEmpty(myModel.getSemiMajorUnits()))
            {
                myModel.setSemiMajorUnits(defaultUnit);
            }

            if (StringUtils.isEmpty(myModel.getSemiMinorUnits()))
            {
                myModel.setSemiMinorUnits(defaultUnit);
            }
        }
    }
}
