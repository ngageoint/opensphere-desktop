package io.opensphere.wps.ui.detail.provider;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.function.Supplier;

import javax.inject.Named;

import com.google.inject.Singleton;

import io.opensphere.core.Toolbox;
import io.opensphere.core.util.DateTimeUtilities;
import io.opensphere.core.util.javafx.input.ValidatedIdentifiedControl;
import io.opensphere.core.util.javafx.input.view.TimePicker;
import io.opensphere.wps.ui.detail.validator.BasicValidator;
import io.opensphere.wps.ui.detail.validator.RequiredTextExaminer;
import jidefx.scene.control.validation.ValidationEvent;
import jidefx.scene.control.validation.ValidationGroup;
import net.opengis.wps._100.InputDescriptionType;

/**
 * An input provider in which an editor for time is generated.
 */
@Singleton
@Named("time")
public class WpsTimeInputProvider implements WpsInputControlProvider
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
        TimePicker tPicker = new TimePicker();
        tPicker.setValue(LocalTime.MIDNIGHT);
        Supplier<String> resultAccessorFunction = () -> DateTimeUtilities.generateISO8601DateString(
                Date.from(tPicker.valueProperty().get().atDate(LocalDate.now()).toInstant(ZoneOffset.UTC)));
        returnValue = new ValidatedIdentifiedControl<>(pInputDescription.getIdentifier().getValue(), pTitle,
                resultAccessorFunction, tPicker);
        returnValue.setValidationGroup(pValidationGroup);

        if (pInputDescription.getMinOccurs() != null && pInputDescription.getMinOccurs().intValue() > 0)
        {
            // this is a required field, put a validator on it:
            returnValue.setValidator(new BasicValidator(new RequiredTextExaminer()));
            tPicker.addEventHandler(ValidationEvent.ANY, event -> DecorationUtils.update(tPicker, event.getEventType()));
        }

        return returnValue;
    }
}
