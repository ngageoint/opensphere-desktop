package io.opensphere.wps.ui.detail;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import javafx.scene.control.Control;
import javafx.scene.control.Tooltip;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.apache.log4j.Logger;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;

import io.opensphere.core.Toolbox;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.javafx.input.DateFieldType;
import io.opensphere.core.util.javafx.input.DateTimeRangeInput;
import io.opensphere.core.util.javafx.input.IdentifiedControl;
import io.opensphere.core.util.javafx.input.ValidatedIdentifiedControl;
import io.opensphere.core.util.javafx.input.view.CombinedDateTimePicker;
import io.opensphere.core.util.lang.Pair;
import io.opensphere.mantle.MantleToolbox;
import io.opensphere.wfs.layer.WFSMetaDataInfo;
import io.opensphere.wps.config.v2.ProcessConfig;
import io.opensphere.wps.config.v2.ProcessSetting;
import io.opensphere.wps.config.v2.UiElement;
import io.opensphere.wps.layer.WpsDataTypeInfo;
import io.opensphere.wps.ui.detail.provider.WpsInputControlProvider;
import io.opensphere.wps.ui.detail.provider.WpsInputControlProviderModule;
import jidefx.scene.control.validation.ValidationGroup;
import net.opengis.ows._110.CodeType;
import net.opengis.ows._110.DomainMetadataType;
import net.opengis.ows._110.LanguageStringType;
import net.opengis.wps._100.InputDescriptionType;
import net.opengis.wps._100.LiteralInputType;
import net.opengis.wps._100.ProcessDescriptionType;

/**
 * A factory responsible for creating U/I components in response to a WPS
 * describe process request.
 */
public class WpsProcessEditorFactory
{
    /**
     * The <code>Log</code> instance used for logging.
     */
    private static final Logger LOG = Logger.getLogger(WpsProcessEditorFactory.class);

    /**
     * The toolbox passed to subcomponents for application interaction.
     */
    private final Toolbox myToolbox;

    /**
     * The IoC injector through which input providers are instantiated.
     */
    private final Injector myInjector;

    /**
     * Creates a new editor factory, initialized with the supplied toolbox.
     *
     * @param pToolbox The toolbox passed to subcomponents for application
     *            interaction.
     */
    public WpsProcessEditorFactory(Toolbox pToolbox)
    {
        myToolbox = pToolbox;
        myInjector = Guice.createInjector(new WpsInputControlProviderModule());
    }

    /**
     * Creates a process form in which the WPS process instance can be
     * configured.
     *
     * @param pServerId the unique ID of the server to which the form is bound.
     * @param pProcessDescription the description from which to create the form.
     * @param config The process configuration
     * @param processSetting The user process settings
     * @return a {@link WpsProcessForm} generated from the supplied
     *         configuration
     */
    public WpsProcessForm createForm(String pServerId, ProcessDescriptionType pProcessDescription, ProcessConfig config,
            ProcessSetting processSetting)
    {
        WpsProcessForm form = new WpsProcessForm(pServerId, pProcessDescription, getNamesInUse());

        ValidationGroup validationGroup = form.getValidationGroup();
        if (pProcessDescription.getDataInputs() != null && pProcessDescription.getDataInputs().getInput() != null)
        {
            Collection<IdentifiedControl<? extends Control>> controls = createInputControls(
                    pProcessDescription.getDataInputs().getInput(), validationGroup, config, processSetting);
            form.addComponents(controls);
            form.doLayout();
        }

        return form;
    }

    /**
     * Gets the names in use by other processes.
     *
     * @return the names
     */
    private Collection<String> getNamesInUse()
    {
        MantleToolbox mantleToolbox = myToolbox.getPluginToolboxRegistry().getPluginToolbox(MantleToolbox.class);
        Set<String> names = mantleToolbox.getDataGroupController()
                .findMembers(t -> t instanceof WpsDataTypeInfo && t.getMetaDataInfo() instanceof WFSMetaDataInfo, false).stream()
                .map(t -> t.getDisplayName()).collect(Collectors.toSet());
        return names;
    }

    /**
     * Creates a {@link Collection} of input controls for the supplied input
     * descriptors.
     *
     * @param inputDescriptions the input descriptors for which to create the
     *            input controls.
     * @param pValidationGroup the validation group to which any generated
     *            validators will be added.
     * @param config The process configuration
     * @param processSetting The user process settings
     * @return a {@link Collection} of input controls, which may be empty, but
     *         never null.
     */
    protected Collection<IdentifiedControl<? extends Control>> createInputControls(
            Collection<? extends InputDescriptionType> inputDescriptions, ValidationGroup pValidationGroup, ProcessConfig config,
            ProcessSetting processSetting)
    {
        Collection<IdentifiedControl<? extends Control>> controls = New.list();

        Map<String, InputDescriptionType> nameToInputDescription = CollectionUtilities.map(inputDescriptions,
            i -> i.getIdentifier().getValue());

        LayerColumnLinker linker = new LayerColumnLinker(myToolbox);
        for (UiElement uiElement : config.getUiElements())
        {
            if (uiElement.getIdentifiers().size() > 1)
            {
                /* Currently time ranges are the only case of two combined
                 * inputs. If this changes this code block will need to be a bit
                 * smarter. */
                List<InputDescriptionType> groupDescriptions = uiElement.getIdentifiers().stream()
                        .map(nameToInputDescription::get).collect(Collectors.toList());
                controls.addAll(createDateTimeControls(groupDescriptions, pValidationGroup, uiElement));
            }
            else
            {
                InputDescriptionType inputDescription = uiElement.getIdentifiers().isEmpty() ? createInputDescription(uiElement)
                        : nameToInputDescription.get(uiElement.getIdentifier());
                IdentifiedControl<?> control = createInput(inputDescription, pValidationGroup, uiElement, processSetting);
                controls.add(control);

                linker.linkLayerAndColumn(inputDescription, control, controls, processSetting);
            }
        }

        return controls;
    }

    /**
     * Creates a "fake" InputDescriptionType from a UiElement.
     *
     * @param uiElement the UiElement
     * @return the InputDescriptionType
     */
    private InputDescriptionType createInputDescription(UiElement uiElement)
    {
        InputDescriptionType inputDescription = new InputDescriptionType();

        LiteralInputType literalData = new LiteralInputType();
        DomainMetadataType dataType = new DomainMetadataType();
        dataType.setValue(uiElement.getUiComponent());
        literalData.setDataType(dataType);
        inputDescription.setLiteralData(literalData);

        CodeType identifier = new CodeType();
        String uiIdentifier = uiElement.getIdentifiers().isEmpty() ? uiElement.getTitle() : uiElement.getIdentifier();
        identifier.setValue(uiIdentifier);
        inputDescription.setIdentifier(identifier);

        LanguageStringType abstract1 = new LanguageStringType();
        abstract1.setValue(uiElement.getAbstract());
        inputDescription.setAbstract(abstract1);

        return inputDescription;
    }

    /**
     * Creates input controls for the supplied descriptors. Before generating
     * the controls, the supplied inputs are organized into groups based on the
     * root variable names. If two or more inputs share the same root variable
     * name, then the input is generated as a compound input to allow ranged
     * date / time input controls. For example, if two input descriptors contain
     * variables named 'START_TIME' and 'END_TIME', the root variable name is
     * 'TIME', which allows a single compound {@link DateTimeRangeInput} to be
     * generated encapsulating both inputs.
     *
     * @param pInputs the set of date / time input descriptors for which to
     *            generate controls.
     * @param pValidationGroup the validation group to which any generated
     *            validators will be added.
     * @param uiConfig the configuration for a single UI element
     * @return a {@link Collection} of controls generated from the supplied
     *         inputs.
     */
    protected Collection<IdentifiedControl<? extends Control>> createDateTimeControls(Collection<InputDescriptionType> pInputs,
            ValidationGroup pValidationGroup, UiElement uiConfig)
    {
        Map<String, List<Pair<DateFieldType, InputDescriptionType>>> groups = extractInputGroups(pInputs);

        Collection<IdentifiedControl<? extends Control>> returnValue = New.list();
        for (Entry<String, List<Pair<DateFieldType, InputDescriptionType>>> groupEntry : groups.entrySet())
        {
            List<Pair<DateFieldType, InputDescriptionType>> groupMembers = groupEntry.getValue();
            if (groupMembers.size() == 2)
            {
                returnValue.add(createCompoundTemporalControl(groupEntry.getKey(), groupMembers.get(0), groupMembers.get(1),
                        pValidationGroup, uiConfig));
            }
            else
            {
                for (Pair<DateFieldType, InputDescriptionType> member : groupMembers)
                {
                    returnValue.add(createInput(member.getSecondObject(), pValidationGroup, uiConfig, null));
                }
            }
        }

        return returnValue;
    }

    /**
     * Organizes the supplied inputs into groups based on their root variable
     * names. If two or more input descriptors share the same root variable
     * name, then the group is generated to allow multiple members. For example,
     * if two input descriptors contain variables named 'START_TIME' and
     * 'END_TIME', the root variable name is 'TIME', which allows a single group
     * to be created containing both inputs.
     *
     * @param pInputs the input descriptors to organize into groups.
     * @return a {@link Map} of groups, using the name of the group as the key,
     *         and the members of the group as the value. Each member is
     *         represented as a {@link Pair} object, in which the member type
     *         (expressed as a {@link DateFieldType}) is associated with its
     *         corresponding member).
     */
    protected Map<String, List<Pair<DateFieldType, InputDescriptionType>>> extractInputGroups(
            Collection<InputDescriptionType> pInputs)
    {
        Map<String, List<Pair<DateFieldType, InputDescriptionType>>> groups = New.map(pInputs.size());
        for (InputDescriptionType input : pInputs)
        {
            String variableName = input.getIdentifier().getValue();
            String groupName = getGroupName(variableName);
            if (StringUtils.isNotBlank(groupName))
            {
                String typeName = variableName.replace(groupName, "");
                groups.computeIfAbsent(groupName, k -> New.list()).add(new Pair<>(DateFieldType.fromString(typeName), input));
            }
            else
            {
                LOG.info("Null group name encountered for variable '" + variableName + "'");
            }
        }
        return groups;
    }

    /**
     * Creates a compound temporal control for the supplied group and its two
     * members. The order of the members does not matter, as the method will
     * sort them such that the start group is listed first, and the other group
     * is second.
     *
     * @param group the group for which to create the input control.
     * @param pMemberOne one member of the group.
     * @param pMemberTwo another member of the group.
     * @param pValidationGroup the group to which the control will be bound for
     *            validation operations.
     * @param uiConfig the configuration for a single UI element
     * @return a control containing inputs for the supplied members.
     */
    protected IdentifiedControl<? extends Control> createCompoundTemporalControl(String group,
            Pair<DateFieldType, InputDescriptionType> pMemberOne, Pair<DateFieldType, InputDescriptionType> pMemberTwo,
            ValidationGroup pValidationGroup, UiElement uiConfig)
    {
        InputDescriptionType start;
        InputDescriptionType end;

        if (pMemberOne.getFirstObject() == DateFieldType.START)
        {
            start = pMemberOne.getSecondObject();
            end = pMemberTwo.getSecondObject();
        }
        else
        {
            start = pMemberTwo.getSecondObject();
            end = pMemberOne.getSecondObject();
        }

        WpsInputControlProvider endProvider = myInjector
                .getInstance(Key.get(WpsInputControlProvider.class, Names.named(end.getLiteralData().getDataType().getValue())));
        WpsInputControlProvider startProvider = myInjector.getInstance(
                Key.get(WpsInputControlProvider.class, Names.named(start.getLiteralData().getDataType().getValue())));

        @SuppressWarnings("unchecked")
        ValidatedIdentifiedControl<CombinedDateTimePicker> startControl = (ValidatedIdentifiedControl<CombinedDateTimePicker>)startProvider
                .create(myToolbox, "Start Time", start, null, pValidationGroup);
        @SuppressWarnings("unchecked")
        ValidatedIdentifiedControl<CombinedDateTimePicker> endControl = (ValidatedIdentifiedControl<CombinedDateTimePicker>)endProvider
                .create(myToolbox, "End Time", end, null, pValidationGroup);

        String title = uiConfig.getTitle() != null ? uiConfig.getTitle() : WordUtils.capitalizeFully(group);
        DateTimeRangeInput control = new DateTimeRangeInput(WordUtils.capitalizeFully(group), startControl, endControl,
                myToolbox);
        ValidatedIdentifiedControl<DateTimeRangeInput> returnValue = new ValidatedIdentifiedControl<>(group, title, control);

        return returnValue;
    }

    /**
     * Extracts the name of the group, removing any reference to start, stop,
     * begin, end or any other temporal qualifier. If the supplied text does not
     * contain a temporal qualifier, it is returned unmodified.
     *
     * @param pVariableName the name of the variable from which to extract the
     *            group name.
     * @return the name of the group.
     */
    protected String getGroupName(String pVariableName)
    {
        return pVariableName.replaceAll("START|BEGIN|END|STOP", "");
    }

    /**
     * Creates a new {@link IdentifiedControl} for the supplied input
     * description.
     *
     * @param pInputDescription the input description for which to create the
     *            named control.
     * @param pValidationGroup the validation group to which any generated
     *            validators will be added.
     * @param uiConfig the configuration for a single UI element
     * @param processSetting The user process settings
     * @return a named control generated for the supplied input description.
     */
    protected IdentifiedControl<?> createInput(InputDescriptionType pInputDescription, ValidationGroup pValidationGroup,
            UiElement uiConfig, ProcessSetting processSetting)
    {
        IdentifiedControl<?> returnValue = null;

        String typeName = getTypeName(pInputDescription, uiConfig);
        String defaultValue = getDefaultValue(pInputDescription, uiConfig, processSetting);
        String title = getTitle(pInputDescription, uiConfig);
        String tooltip = getTooltip(pInputDescription, uiConfig);

        WpsInputControlProvider provider = myInjector.getInstance(Key.get(WpsInputControlProvider.class, Names.named(typeName)));
        returnValue = provider.create(myToolbox, title, pInputDescription, defaultValue, pValidationGroup);

        if (tooltip != null)
        {
            returnValue.getControl().setTooltip(new Tooltip(tooltip));
        }
        returnValue.setUnits(uiConfig.getUnits());

        return returnValue;
    }

    /**
     * Gets the type name.
     *
     * @param pInputDescription the input description
     * @param uiConfig the configuration for a single UI element
     * @return the type name
     */
    protected String getTypeName(InputDescriptionType pInputDescription, UiElement uiConfig)
    {
        String typeName = null;
        if (uiConfig.getUiComponent() != null)
        {
            typeName = uiConfig.getUiComponent();
        }
        else if (pInputDescription.getLiteralData() != null)
        {
            if (pInputDescription.getLiteralData().getDataType() != null)
            {
                typeName = pInputDescription.getLiteralData().getDataType().getValue();
            }
        }
        else if (pInputDescription.getBoundingBoxData() != null)
        {
            typeName = "BBOX";
        }

        if (StringUtils.isBlank(typeName))
        {
            typeName = "string";
        }
        return typeName;
    }

    /**
     * Gets the default value.
     *
     * @param pInputDescription the input description
     * @param uiConfig the configuration for a single UI element
     * @param processSetting The user process settings
     * @return the type name
     */
    protected String getDefaultValue(InputDescriptionType pInputDescription, UiElement uiConfig, ProcessSetting processSetting)
    {
        String defaultValue = null;
        String lastUsedValue = processSetting != null
                ? processSetting.getLastUsedValues().get(pInputDescription.getIdentifier().getValue()) : null;
        if (lastUsedValue != null)
        {
            defaultValue = lastUsedValue;
        }
        else if (uiConfig.getDefaultValue() != null)
        {
            defaultValue = uiConfig.getDefaultValue();
        }
        else if (pInputDescription.getLiteralData() != null)
        {
            defaultValue = pInputDescription.getLiteralData().getDefaultValue();
        }
        return defaultValue;
    }

    /**
     * Gets the title of the supplied input descriptor.
     *
     * @param pInputDescriptor the descriptor for which to get the input title.
     * @param uiConfig the configuration for a single UI element
     * @return the title of the supplied input descriptor.
     */
    protected String getTitle(InputDescriptionType pInputDescriptor, UiElement uiConfig)
    {
        return uiConfig.getTitle() != null ? uiConfig.getTitle() : pInputDescriptor.getTitle().getValue(); // getInputTitle(pInputDescriptor);
    }

    /**
     * Gets the tooltip text.
     *
     * @param pInputDescription the input description
     * @param uiConfig the configuration for a single UI element
     * @return the tooltip text
     */
    protected String getTooltip(InputDescriptionType pInputDescription, UiElement uiConfig)
    {
        String defaultValue = null;
        if (uiConfig.getAbstract() != null)
        {
            defaultValue = uiConfig.getAbstract();
        }
        else if (pInputDescription.getAbstract() != null)
        {
            defaultValue = pInputDescription.getAbstract().getValue();
        }
        return defaultValue;
    }

//    /**
//     * Gets the title of the supplied input descriptor.
//     *
//     * @param pInputDescriptor the descriptor for which to get the input title.
//     * @return the title of the supplied input descriptor.
//     */
//    protected String getInputTitle(InputDescriptionType pInputDescriptor)
//    {
//        String variableName = pInputDescriptor.getIdentifier().getValue();
//        String inputName;
//        if (StringUtils.equalsIgnoreCase(variableName, "TYPENAME"))
//        {
//            inputName = "Layer";
//        }
//        else if (StringUtils.equalsIgnoreCase(variableName, "IDENTIFIER"))
//        {
//            inputName = "Column Value";
//        }
//        else if (StringUtils.equalsIgnoreCase(variableName, "BBOX"))
//        {
//            inputName = "Area";
//        }
//        else
//        {
//            inputName = pInputDescriptor.getTitle().getValue();
//        }
//        return inputName;
//    }
}
