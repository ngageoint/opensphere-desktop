package io.opensphere.wps.ui.detail.provider;

import java.util.function.Supplier;

import javafx.event.EventType;
import javafx.scene.Node;

import javax.inject.Named;

import io.opensphere.core.Toolbox;
import io.opensphere.core.util.javafx.input.ValidatedIdentifiedControl;
import io.opensphere.core.util.lang.NumberUtilities;
import io.opensphere.wps.ui.detail.bbpicker.BoundingBoxPicker;
import io.opensphere.wps.ui.detail.validator.BasicValidator;
import jidefx.scene.control.validation.ValidationEvent;
import jidefx.scene.control.validation.ValidationGroup;
import net.opengis.wps._100.InputDescriptionType;

/**
 * An input provider in which an editor for bounding boxes is generated.
 */
@Named("BBOX")
public class WpsBoundingBoxInputProvider implements WpsInputControlProvider
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
        BoundingBoxPicker picker = new BoundingBoxPicker(pToolbox, NumberUtilities.toInt(pInputDescription.getMinOccurs(), 1),
                NumberUtilities.toInt(pInputDescription.getMaxOccurs(), 1));

        Supplier<String> resultAccessorFunction = () -> picker.getValue().get();
        returnValue = new ValidatedIdentifiedControl<>(pInputDescription.getIdentifier().getValue(), pTitle,
                resultAccessorFunction, picker);

        returnValue.setValidator(new BasicValidator(v -> picker.isValid()), picker.getValue());
        picker.addEventHandler(ValidationEvent.ANY, event -> update(picker, event.getEventType()));

        returnValue.setValidationGroup(pValidationGroup);

        picker.fireValidationEvent();

        return returnValue;
    }

    /**
     * Updates the border of the supplied node, changing it based on the
     * contents of the supplied event.
     *
     * @param pNode the node for which the border will be updated.
     * @param pEvent the event on which the update operation is based.
     */
    public static void update(Node pNode, EventType<ValidationEvent> pEvent)
    {
        boolean isError = pEvent.equals(ValidationEvent.VALIDATION_ERROR);
        pNode.setStyle(isError ? DecorationUtils.ERROR_STYLE : BoundingBoxPicker.DEFAULT_STYLE);
    }
}
