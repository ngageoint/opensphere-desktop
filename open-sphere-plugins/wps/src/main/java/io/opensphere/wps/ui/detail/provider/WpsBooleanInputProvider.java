package io.opensphere.wps.ui.detail.provider;

import java.util.function.Supplier;

import javafx.scene.control.CheckBox;

import javax.inject.Named;

import org.apache.commons.lang.StringUtils;

import com.google.inject.Singleton;

import io.opensphere.core.Toolbox;
import io.opensphere.core.util.javafx.input.ValidatedIdentifiedControl;
import jidefx.scene.control.validation.ValidationGroup;
import net.opengis.wps._100.InputDescriptionType;

/**
 * A boolean input provider in which a checkbox is generated.
 */
@Singleton
@Named("boolean")
public class WpsBooleanInputProvider implements WpsInputControlProvider
{
    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.wps.ui.detail.provider.WpsInputControlProvider#create(io.opensphere.core.Toolbox, java.lang.String,
     *      net.opengis.wps._100.InputDescriptionType, java.lang.String, ValidationGroup)
     */
    @Override
    public ValidatedIdentifiedControl<?> create(Toolbox pToolbox, String pTitle, InputDescriptionType pInputDescription,
            String pDefaultValue, ValidationGroup pValidationGroup)
    {
        ValidatedIdentifiedControl<?> returnValue;
        CheckBox checkBox = new CheckBox();
        if (StringUtils.isNotBlank(pDefaultValue))
        {
            checkBox.setSelected(Boolean.parseBoolean(pDefaultValue));
        }
        Supplier<String> resultAccessorFunction = () -> Boolean.toString(checkBox.isSelected());
        returnValue = new ValidatedIdentifiedControl<>(pInputDescription.getIdentifier().getValue(), pTitle,
                resultAccessorFunction, checkBox);
        returnValue.setValidationGroup(pValidationGroup);

        return returnValue;
    }
}
