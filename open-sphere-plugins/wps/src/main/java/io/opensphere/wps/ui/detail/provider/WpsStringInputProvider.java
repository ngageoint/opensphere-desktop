package io.opensphere.wps.ui.detail.provider;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.util.Callback;

import javax.inject.Named;

import org.apache.commons.lang.StringUtils;

import com.google.inject.Singleton;

import io.opensphere.core.Toolbox;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.fx.AutoCompleteComboBoxListener;
import io.opensphere.core.util.javafx.input.IdentifiedControl;
import io.opensphere.core.util.javafx.input.ValidatedIdentifiedControl;
import io.opensphere.mantle.MantleToolbox;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.util.MantleToolboxUtils;
import io.opensphere.wps.ui.detail.validator.BasicValidator;
import io.opensphere.wps.ui.detail.validator.RequiredTextExaminer;
import io.opensphere.wps.util.WpsUtilities;
import jidefx.scene.control.validation.ValidationEvent;
import jidefx.scene.control.validation.ValidationGroup;
import net.opengis.wps._100.InputDescriptionType;

/**
 * An input provider in which an editor for strings is generated.
 */
@Singleton
@Named("string")
public class WpsStringInputProvider implements WpsInputControlProvider
{
    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.wps.ui.detail.provider.WpsInputControlProvider#create(Toolbox,
     *      String, InputDescriptionType, String, ValidationGroup)
     */
    @Override
    public ValidatedIdentifiedControl<?> create(Toolbox pToolbox, String pTitle, InputDescriptionType pInputDescription,
            String pDefaultValue, ValidationGroup pValidationGroup)
    {
        ValidatedIdentifiedControl<?> returnValue;
        if (pInputDescription.getLiteralData().getAllowedValues() != null
                && !pInputDescription.getLiteralData().getAllowedValues().getValueOrRange().isEmpty()
                || WpsUtilities.isColumnField(pInputDescription))
        {
            returnValue = createChoiceInput(pToolbox, pTitle, pInputDescription, pDefaultValue, pValidationGroup);
        }
        else
        {
            returnValue = createTextInput(pTitle, pInputDescription, pDefaultValue, pValidationGroup);
        }
        return returnValue;
    }

    /**
     * Creates a simple text field input for the supplied parameters. The
     * Default value is optional and may be ignored.
     *
     * @param pTitle the title of the component to be displayed on the U/I.
     * @param pInputDescription the WPS input descriptor from which the
     *            component will be created.
     * @param pDefaultValue the optional default value with which to populate
     *            the component.
     * @param pValidationGroup the group to which generated validators are
     *            added.
     * @return an {@link IdentifiedControl} generated using the supplied
     *         parameters.
     */
    protected ValidatedIdentifiedControl<?> createTextInput(String pTitle, InputDescriptionType pInputDescription,
            String pDefaultValue, ValidationGroup pValidationGroup)
    {
        ValidatedIdentifiedControl<TextField> returnValue;
        TextField textField = new TextField();
        if (StringUtils.isNotBlank(pDefaultValue))
        {
            textField.setText(pDefaultValue);
        }
        Supplier<String> resultAccessorFunction = () -> textField.getText();
        returnValue = new ValidatedIdentifiedControl<>(pInputDescription.getIdentifier().getValue(), pTitle,
                resultAccessorFunction, textField);
        returnValue.setValidationGroup(pValidationGroup);

        if (pInputDescription.getMinOccurs() != null && pInputDescription.getMinOccurs().intValue() > 0)
        {
            // this is a required field, put a validator on it:
            returnValue.setValidator(new BasicValidator(new RequiredTextExaminer()));
            textField.addEventHandler(ValidationEvent.ANY, event -> DecorationUtils.update(textField, event.getEventType()));
        }

        return returnValue;
    }

    /**
     * Creates a choice field input for the supplied parameters. The Default
     * value is optional and may be ignored.
     *
     * @param pToolbox the toolbox through which additional application data may
     *            be gathered.
     * @param pTitle the title of the component to be displayed on the U/I.
     * @param pInputDescription the WPS input descriptor from which the
     *            component will be created.
     * @param pDefaultValue the optional default value with which to populate
     *            the component.
     * @param pValidationGroup the group to which generated validators are
     *            added.
     * @return an {@link IdentifiedControl} generated using the supplied
     *         parameters.
     */
    protected ValidatedIdentifiedControl<?> createChoiceInput(Toolbox pToolbox, String pTitle,
            InputDescriptionType pInputDescription, String pDefaultValue, ValidationGroup pValidationGroup)
    {
        ValidatedIdentifiedControl<?> returnValue;

        List<String> allowedValues = WpsUtilities.getAllowedValues(pInputDescription);
        boolean required = pInputDescription.getMinOccurs() != null && pInputDescription.getMinOccurs().intValue() > 0;
        if (!required)
        {
            allowedValues.add(0, "");
        }
        ObservableList<String> values = FXCollections.observableList(allowedValues);

        ComboBox<String> choiceBox = new ComboBox<>(values);
        DecorationUtils.setValue(choiceBox, values, pDefaultValue);

        decorateLayerCombo(pToolbox, pInputDescription, choiceBox);

        Supplier<String> resultAccessorFunction = () -> choiceBox.getValue();
        returnValue = new ValidatedIdentifiedControl<>(pInputDescription.getIdentifier().getValue(), pTitle,
                resultAccessorFunction, choiceBox);
        returnValue.setValidationGroup(pValidationGroup);

        returnValue.setValidator(new BasicValidator(choiceBox.getItems()::contains));
        choiceBox.addEventHandler(ValidationEvent.ANY, event -> DecorationUtils.update(choiceBox, event.getEventType()));

        new AutoCompleteComboBoxListener<>(choiceBox);

        return returnValue;
    }

    /**
     * Renders layer choices with user-friendly layer names.
     *
     * @param pToolbox the toolbox
     * @param pInputDescription the input description
     * @param comboBox the combo box
     */
    private void decorateLayerCombo(Toolbox pToolbox, InputDescriptionType pInputDescription, ComboBox<String> comboBox)
    {
        if ("TYPENAME".equals(pInputDescription.getIdentifier().getValue()))
        {
            MantleToolbox mantleToolbox = MantleToolboxUtils.getMantleToolbox(pToolbox);
            // Only get the wfs layers because we need to filter out wms and wps
            Set<DataTypeInfo> dataTypes = mantleToolbox.getDataGroupController()
                    .findMembers(t -> t.getTypeKey().contains("wfsServer"), false);
            Map<String, DataTypeInfo> typeMap = CollectionUtilities.map(dataTypes, t -> t.getTypeName());

            Callback<ListView<String>, ListCell<String>> cellFactory = p -> new ListCell<String>()
            {
                @Override
                protected void updateItem(String item, boolean empty)
                {
                    super.updateItem(item, empty);

                    DataTypeInfo dataType = typeMap.get(item);
                    String displayName = dataType != null ? dataType.getDisplayName() : item;
                    setText(displayName);
                }
            };
            comboBox.setCellFactory(cellFactory);
            comboBox.setButtonCell(cellFactory.call(null));
        }
    }
}
