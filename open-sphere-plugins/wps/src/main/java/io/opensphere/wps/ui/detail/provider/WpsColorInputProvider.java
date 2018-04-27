package io.opensphere.wps.ui.detail.provider;

import java.util.function.Supplier;

import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;

import com.google.inject.Singleton;

import io.opensphere.core.Toolbox;
import io.opensphere.core.util.javafx.input.ValidatedIdentifiedControl;
import javafx.scene.control.ColorPicker;
import javafx.scene.paint.Color;
import jidefx.scene.control.validation.ValidationGroup;
import net.opengis.wps._100.InputDescriptionType;

/**
 * A color input provider in which a color picker is generated.
 */
@Singleton
@Named("color")
public class WpsColorInputProvider implements WpsInputControlProvider
{
    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.wps.ui.detail.provider.WpsInputControlProvider#create(io.opensphere.core.Toolbox,
     *      java.lang.String, net.opengis.wps._100.InputDescriptionType,
     *      java.lang.String, ValidationGroup)
     */
    @Override
    public ValidatedIdentifiedControl<?> create(Toolbox pToolbox, String pTitle, InputDescriptionType pInputDescription,
            String pDefaultValue, ValidationGroup pValidationGroup)
    {
        ValidatedIdentifiedControl<?> returnValue;
        ColorPicker colorPicker = new ColorPicker();
        if (StringUtils.isNotBlank(pDefaultValue))
        {
            colorPicker.setValue(Color.web(pDefaultValue));
        }
        Supplier<String> resultAccessorFunction = () -> colorPicker.getValue().toString();
        returnValue = new ValidatedIdentifiedControl<>(pInputDescription.getIdentifier().getValue(), pTitle,
                resultAccessorFunction, colorPicker);
        returnValue.setValidationGroup(pValidationGroup);

        return returnValue;
    }
}
