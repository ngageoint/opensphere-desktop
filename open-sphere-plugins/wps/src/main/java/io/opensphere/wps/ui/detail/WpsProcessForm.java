package io.opensphere.wps.ui.detail;

import java.util.Collection;
import java.util.List;

import javafx.beans.property.StringProperty;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

import io.opensphere.core.util.Visitable;
import io.opensphere.core.util.Visitor;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.fx.FXUtilities;
import io.opensphere.core.util.javafx.input.IdentifiedControl;
import io.opensphere.core.util.javafx.input.TitledControl;
import io.opensphere.core.util.javafx.input.ValidatedIdentifiedControl;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.wps.ui.detail.provider.DecorationUtils;
import io.opensphere.wps.ui.detail.validator.BasicValidator;
import io.opensphere.wps.ui.detail.validator.RequiredTextExaminer;
import io.opensphere.wps.util.WPSConstants;
import jidefx.scene.control.validation.DecorationFreeValidationUtils;
import jidefx.scene.control.validation.ValidationEvent;
import jidefx.scene.control.validation.ValidationGroup;
import jidefx.scene.control.validation.ValidationMode;
import net.opengis.wps._100.ProcessDescriptionType;

/**
 * A simple form onto which {@link TitledControl} components are rendered.
 */
public class WpsProcessForm extends GridPane implements ValidatableForm, Visitable
{
    /**
     * The field into which the name of the configuration is entered.
     */
    private final TextField myNameField;

    /**
     * A dictionary of controls, in which each control is mapped to its corresponding WPS process variable name.
     */
    private final List<IdentifiedControl<? extends Control>> myControls;

    /**
     * The validation group associated with the form.
     */
    private final ValidationGroup myValidationGroup;

    /**
     * The process description from which the form was generated.
     */
    private final ProcessDescriptionType myProcessDescription;

    /**
     * The unique identifier of the server to which the form is bound.
     */
    private final String myServerId;

    /**
     * Creates a new WPS form in which components are rendered.
     *
     * @param pServerId the unique identifier of the server to which the form is bound.
     * @param pProcessDescription the process description for which the form is generated.
     * @param namesInUse the other process names in use
     */
    @SuppressWarnings("PMD.ConstructorCallsOverridableMethod")
    public WpsProcessForm(String pServerId, ProcessDescriptionType pProcessDescription, Collection<String> namesInUse)
    {
        setPadding(new Insets(5));
        setHgap(5);
        setVgap(5);

        myServerId = pServerId;
        myProcessDescription = pProcessDescription;
        myControls = New.list();
        myValidationGroup = new ValidationGroup();

        String title = StringUtilities.getUniqueName("New " + pProcessDescription.getTitle().getValue() + " ", namesInUse);
        myNameField = new TextField(title);
        ValidatedIdentifiedControl<TextField> nameControl = new ValidatedIdentifiedControl<>("WPS_CONFIGURATION_NAME", "Name",
                myNameField);
        myControls.add(nameControl);
        nameControl.setValidationGroup(myValidationGroup);
        nameControl.setValidator(new BasicValidator(new RequiredTextExaminer()));
        myNameField.addEventHandler(ValidationEvent.ANY, event -> DecorationUtils.update(myNameField, event.getEventType()));

        // when the parent is added, the form has been rendered, which means it needs to be validated.
        parentProperty().addListener((pParent, pOld, pNew) -> performValidation());
    }

    /**
     * Gets the value of the {@link #myProcessDescription} field.
     *
     * @return the value stored in the {@link #myProcessDescription} field.
     */
    public ProcessDescriptionType getProcessDescription()
    {
        return myProcessDescription;
    }

    /**
     * Adds the supplied components to the form.
     *
     * @param pControls the componens to add to the form.
     */
    public void addComponents(Collection<? extends IdentifiedControl<? extends Control>> pControls)
    {
        myControls.addAll(pControls);
    }

    /**
     * Performs layout of the form.
     */
    public void doLayout()
    {
        getChildren().clear();
        int row = 0;
        for (IdentifiedControl<? extends Control> control : myControls)
        {
            Label label = new Label(control.getTitle() + ": ");
            GridPane.setHalignment(label, HPos.RIGHT);
            Node component = control.getUnits() == null ? control : FXUtilities.newHBox(control, new Label(control.getUnits()));
            addRow(row++, label, component);
        }
    }

    /**
     * Gets the property associated with the name of the configuration.
     *
     * @return the property associated with the name of the configuration.
     */
    public StringProperty nameProperty()
    {
        return myNameField.textProperty();
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.wps.ui.detail.ValidatableForm#getValidationGroup()
     */
    @Override
    public ValidationGroup getValidationGroup()
    {
        return myValidationGroup;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.wps.ui.detail.ValidatableForm#performValidation()
     */
    @Override
    public void performValidation()
    {
        if (myValidationGroup != null)
        {
            for (Node control : myValidationGroup.getValidationNodes())
            {
                DecorationFreeValidationUtils.forceValidate(control, ValidationMode.ON_FLY);
            }
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.util.Visitable#visit(io.opensphere.core.util.Visitor)
     */
    @Override
    public void visit(Visitor<?> pVisitor)
    {
        pVisitor.setValue(WPSConstants.PROCESS_INSTANCE_NAME, myNameField.getText());

        for (IdentifiedControl<? extends Control> control : myControls)
        {
            control.visit(pVisitor);
        }
    }

    /**
     * Gets the value of the {@link #myServerId} field.
     *
     * @return the value stored in the {@link #myServerId} field.
     */
    public String getServerId()
    {
        return myServerId;
    }
}
