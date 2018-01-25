package io.opensphere.analysis.binning.editor.controller;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import io.opensphere.analysis.binning.criteria.BinCriteria;
import io.opensphere.analysis.binning.criteria.BinCriteriaElement;
import io.opensphere.analysis.binning.criteria.UniqueCriteria;
import io.opensphere.analysis.binning.editor.model.BinCriteriaModel;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.Pair;
import io.opensphere.mantle.controller.DataGroupController;
import io.opensphere.mantle.data.DataTypeInfo;

/**
 * Controller used for {@link BinCriteria} editing.
 */
public class BinCriteriaController implements Comparator<Pair<String, Class<?>>>
{
    /** The model the UI can use. */
    private final BinCriteriaModel myModel;

    /** Used to get layer information. */
    private final DataGroupController groupCtrl;

    /**
     * Constructs a new {@link BinCriteriaController}. Constructs a new
     * {@link BinCriteriaModel} to be used by the ui.
     *
     * @param ctrl Used to get layer information.
     * @param dataTypeId The layer to create/edit criteria for.
     * @param existingCriteria The existing criteria to edit, or null if we are
     *            creating new criteria.
     */
    public BinCriteriaController(DataGroupController ctrl, String dataTypeId, BinCriteria existingCriteria)
    {
        groupCtrl = ctrl;

        BinCriteria criteria = existingCriteria;

        List<Pair<String, Class<?>>> layersFields = getTypeFields(dataTypeId);
        if (existingCriteria == null)
        {
            criteria = createDefault(dataTypeId, layersFields);
        }
        else if (existingCriteria.getCriterias().isEmpty())
        {
            existingCriteria.getCriterias().add(createDefaultElement(layersFields));
        }

        if (!dataTypeId.equals(criteria.getDataTypeKey()))
        {
            criteria.setDataTypeKey(dataTypeId);
        }

        myModel = new BinCriteriaModel(dataTypeId, layersFields, criteria);
    }

    @Override
    public int compare(Pair<String, Class<?>> o1, Pair<String, Class<?>> o2)
    {
        return o1.getFirstObject().compareTo(o2.getFirstObject());
    }

    /**
     * Gets the model.
     *
     * @return The model.
     */
    public BinCriteriaModel getModel()
    {
        return myModel;
    }

    /**
     * Creates a default {@link BinCriteria} to start editing with.
     *
     * @param dataTypeId The layer id.
     * @param layersFields The fields and their respective types contained in
     *            the layer.
     * @return The default {@link BinCriteria} to start editing.
     */
    private BinCriteria createDefault(String dataTypeId, List<Pair<String, Class<?>>> layersFields)
    {
        BinCriteria criteria = new BinCriteria();
        criteria.setDataTypeKey(dataTypeId);

        criteria.getCriterias().add(createDefaultElement(layersFields));

        return criteria;
    }

    /**
     * Creates a default {@link BinCriteria} to start editing with.
     *
     * @param layersFields The fields and their respective types contained in
     *            the layer.
     * @return The default {@link BinCriteria} to start editing.
     */
    private BinCriteriaElement createDefaultElement(List<Pair<String, Class<?>>> layersFields)
    {
        BinCriteriaElement element = new BinCriteriaElement();
        element.setField(layersFields.get(0).getFirstObject());
        element.setCriteriaType(new UniqueCriteria());

        return element;
    }

    /**
     * Gets the layer's column names and types.
     *
     * @param dataTypeId The layer id.
     * @return The layer's column names and types sorted alphabetically.
     */
    private List<Pair<String, Class<?>>> getTypeFields(String dataTypeId)
    {
        DataTypeInfo dataType = groupCtrl.findMemberById(dataTypeId);
        Map<String, Class<?>> columns = dataType.getMetaDataInfo().getKeyClassTypeMap();
        List<Pair<String, Class<?>>> fields = New.list();
        for (Entry<String, Class<?>> entry : columns.entrySet())
        {
            fields.add(new Pair<>(entry.getKey(), entry.getValue()));
        }

        fields.sort(this);

        return fields;
    }
}
