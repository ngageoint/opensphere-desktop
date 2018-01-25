package io.opensphere.mantle.data.dynmeta;

/**
 * The Class DynamicColumnManager.
 */
@FunctionalInterface
public interface DynamicDataElementMetadataManager
{
    /**
     * Gets the controller for the data type.
     *
     * @param dtiKey the dti key
     * @return the controller
     */
    DynamicMetadataDataTypeController getController(String dtiKey);
}
