package io.opensphere.wps.ui.detail.provider;

import io.opensphere.core.Toolbox;
import io.opensphere.core.util.javafx.input.IdentifiedControl;
import io.opensphere.core.util.javafx.input.ValidatedIdentifiedControl;
import jidefx.scene.control.validation.ValidationGroup;
import net.opengis.wps._100.InputDescriptionType;

/**
 * A contract for a factory-like class that creates input components from configuration parameters.
 */
public interface WpsInputControlProvider
{
    /**
     * Creates an input for the supplied parameters. The Default value is optional and may be ignored.
     *
     * @param pToolbox the toolbox through which additional application data may be gathered.
     * @param pTitle the title of the component to be displayed on the U/I.
     * @param pInputDescription the WPS input descriptor from which the component will be created.
     * @param pDefaultValue the optional default value with which to populate the component.
     * @param pValidationGroup the validation group to which any validators will be added.
     * @return an {@link IdentifiedControl} generated using the supplied parameters.
     */
    ValidatedIdentifiedControl<?> create(Toolbox pToolbox, String pTitle, InputDescriptionType pInputDescription,
            String pDefaultValue, ValidationGroup pValidationGroup);
}
