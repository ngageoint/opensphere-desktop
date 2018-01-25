package io.opensphere.analysis.binning.editor.model;

import java.util.List;

import io.opensphere.analysis.binning.criteria.BinCriteria;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.Pair;

/**
 * The model used by the bin criteria editor. Contains the bin criteria as well
 * as the layer id the criteria is used for.
 */
public class BinCriteriaModel
{
    /**
     * The bin criteria.
     */
    private final BinCriteria myCriteria;

    /**
     * The id of the layer the bin criteria is for.
     */
    private final String myDataTypeId;

    /**
     * The available fields for the layer.
     */
    private final List<Pair<String, Class<?>>> myLayersFields = New.list();

    /**
     * Constructor.
     *
     * @param dataTypeId The id of the layer the bin criteria is for.
     * @param layersFields The availbable fields for the layer.
     * @param criteria The bin criteria.
     */
    public BinCriteriaModel(String dataTypeId, List<Pair<String, Class<?>>> layersFields, BinCriteria criteria)
    {
        myDataTypeId = dataTypeId;
        myLayersFields.addAll(layersFields);
        myCriteria = criteria;
    }

    /**
     * Gets the bin criteria.
     *
     * @return The bin criteria.
     */
    public BinCriteria getCriteria()
    {
        return myCriteria;
    }

    /**
     * Gets the id of the layer we are building bin criteria for.
     *
     * @return The layer id.
     */
    public String getDataTypeId()
    {
        return myDataTypeId;
    }

    /**
     * Gets the layers fields.
     *
     * @return The columns of the layer and their associated data type.
     */
    public List<Pair<String, Class<?>>> getLayersFields()
    {
        return New.unmodifiableList(myLayersFields);
    }
}
