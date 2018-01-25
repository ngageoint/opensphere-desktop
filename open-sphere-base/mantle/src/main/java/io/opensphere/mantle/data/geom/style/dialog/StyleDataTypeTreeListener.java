package io.opensphere.mantle.data.geom.style.dialog;

/**
 * Listener for changes. in the dual (tile/feature) tree sets.
 */
public interface StyleDataTypeTreeListener
{
    /**
     * Data type selected.
     *
     * @param type the DataTypeNodeUserObject that was selected.
     */
    void dataTypeSelected(DataTypeNodeUserObject type);

    /**
     * Force rebuild.
     */
    void forceRebuild();

    /**
     * No data type selected.
     */
    void noDataTypeSelected();
}
