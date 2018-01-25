package io.opensphere.wps.ui.detail.provider;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.function.Supplier;

import javax.inject.Named;

import com.google.inject.Singleton;

import io.opensphere.core.Toolbox;
import io.opensphere.core.util.DateTimeUtilities;
import io.opensphere.core.util.javafx.input.ValidatedIdentifiedControl;
import io.opensphere.core.util.javafx.input.view.CombinedDateTimePicker;
import io.opensphere.wps.ui.detail.validator.BasicValidator;
import io.opensphere.wps.ui.detail.validator.RequiredTextExaminer;
import jidefx.scene.control.validation.ValidationEvent;
import jidefx.scene.control.validation.ValidationGroup;
import net.opengis.wps._100.InputDescriptionType;

/**
 * An input provider in which an editor for date / times is generated.
 */
@Singleton
@Named("dateTime")
public class WpsDateTimeInputProvider implements WpsInputControlProvider
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
        CombinedDateTimePicker dtPicker = new CombinedDateTimePicker();
        dtPicker.setValue(LocalTime.MIDNIGHT.atDate(LocalDate.now()));
        Supplier<String> resultAccessorFunction = () -> DateTimeUtilities
                .generateISO8601DateString(dtPicker.valueProperty().get());
        returnValue = new ValidatedIdentifiedControl<>(pInputDescription.getIdentifier().getValue(), pTitle,
                resultAccessorFunction, dtPicker);
        returnValue.setValidationGroup(pValidationGroup);

        if (pInputDescription.getMinOccurs() != null && pInputDescription.getMinOccurs().intValue() > 0)
        {
            // this is a required field, put a validator on it:
            returnValue.setValidator(new BasicValidator(new RequiredTextExaminer()));
            dtPicker.addEventHandler(ValidationEvent.ANY, event -> DecorationUtils.update(dtPicker, event.getEventType()));
        }

        return returnValue;
    }
}
