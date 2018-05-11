package io.opensphere.wps.ui.detail.provider;

import java.util.function.Supplier;

import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;

import com.google.inject.Singleton;

import io.opensphere.core.Toolbox;
import io.opensphere.core.util.javafx.input.ValidatedIdentifiedControl;
import io.opensphere.wps.ui.detail.validator.BasicValidator;
import io.opensphere.wps.ui.detail.validator.RequiredTextExaminer;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import jidefx.scene.control.validation.ValidationEvent;
import jidefx.scene.control.validation.ValidationGroup;
import net.opengis.wps._100.InputDescriptionType;

/**
 * An input provider in which an editor for integers is generated.
 */
@Singleton
@Named("double")
public class WpsDecimalInputProvider implements WpsInputControlProvider
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
        Spinner<Double> spinner = new Spinner<>();
        if (StringUtils.isNotBlank(pDefaultValue))
        {
            SpinnerValueFactory<Double> valueFactory = spinner.getValueFactory();
            if (valueFactory == null)
            {
                valueFactory = new SpinnerValueFactory.DoubleSpinnerValueFactory(0, 1000);
            }
            Double doubleValue = Double.valueOf(pDefaultValue);
            valueFactory.setValue(doubleValue);
        }

        Supplier<String> resultAccessorFunction = () -> spinner.valueProperty().get().toString();
        returnValue = new ValidatedIdentifiedControl<>(pInputDescription.getIdentifier().getValue(), pTitle,
                resultAccessorFunction, spinner);
        returnValue.setValidationGroup(pValidationGroup);

        if (pInputDescription.getMinOccurs() != null && pInputDescription.getMinOccurs().intValue() > 0)
        {
            // this is a required field, put a validator on it:
            returnValue.setValidator(new BasicValidator(new RequiredTextExaminer()));
            spinner.addEventHandler(ValidationEvent.ANY, event -> DecorationUtils.update(spinner, event.getEventType()));
        }

        return returnValue;
    }
}
