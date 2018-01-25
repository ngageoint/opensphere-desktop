package io.opensphere.core.util.swing.pie;

import java.util.List;

/**
 * Model for MultiLevelPieChart.
 *
 * @param <T> The type of the data
 */
public interface MultiLevelPieChartModel<T>
{
    /**
     * Returns the number of rings in the model. A
     * <code>MultiLevelPieChart</code> uses this method to determine how many
     * rings it should display. This method should be quick, as it is called
     * frequently during rendering.
     *
     * @return the number of rings in the model
     * @see #getSliceCount
     */
    int getRingCount();

    /**
     * Returns the name of the ring at <code>ringIndex</code>. This is used to
     * initialize the table's ring header name. Note: this name does not need to
     * be unique; two rings in a table can have the same name.
     *
     * @param ringIndex the index of the ring
     * @return the name of the ring
     */
    String getRingName(int ringIndex);

    /**
     * Returns the number of slices in the model. A
     * <code>MultiLevelPieChart</code> uses this method to determine how many
     * slices it should create and display by default.
     *
     * @return the number of slices in the model
     * @see #getRingCount
     */
    int getSliceCount();

    /**
     * Returns the name of the slice at <code>sliceIndex</code>. This is used to
     * initialize the table's slice header name. Note: this name does not need
     * to be unique; two slices in a table can have the same name.
     *
     * @param sliceIndex the index of the slice
     * @return the name of the slice
     */
    String getSliceName(int sliceIndex);

    /**
     * Returns the value for the cell at <code>sliceIndex</code> and
     * <code>ringIndex</code>.
     *
     * @param sliceIndex the slice whose value is to be queried
     * @param ringIndex the ring whose value is to be queried
     * @return the value at the specified cell
     */
    T getValueAt(int sliceIndex, int ringIndex);

    /**
     * Sets the ring names.
     *
     * @param ringNames the new ring names
     */
    void setRingNames(List<? extends String> ringNames);

    /**
     * Sets the slilce names.
     *
     * @param sliceNames the new slilce names
     */
    void setSlilceNames(List<? extends String> sliceNames);

    /**
     * Allows the values to be reset in the model without having to create a new
     * model.
     *
     * @param values the new values
     */
    void setValues(TwoDimensionArrayList<T> values);
}
