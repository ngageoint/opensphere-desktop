package io.opensphere.analysis.table.model;

import io.opensphere.mantle.data.MetaDataInfo;
import io.opensphere.mantle.data.element.DataElement;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

/**
 * Meta column for the list tool table model.
 *
 * @param <T> the type of the column
 */
public abstract class MetaColumn<T>
{
    /** The index meta column identifier. */
    public static final String INDEX = "Index";

    /** The color meta column identifier. */
    public static final String COLOR = "Color";

    /** The visible meta column identifier. */
    public static final String VISIBLE = "Visible";

    /** The hilight meta column identifier. */
    public static final String HILIGHT = "Hilight";

    /** The selected meta column identifier. */
    public static final String SELECTED = "Selected";

    /** The LOB visible meta column identifier. */
    public static final String LOB_VISIBLE = "LOB Visible";

    /** The MGRS derived meta column identifier. */
    public static final String MGRS_DERIVED = MetaDataInfo.MGRS_DERIVED;

    /** The column identifier. */
    private final String myColumnIdentifier;

    /** The column class. */
    private final Class<T> myColumnClass;

    /** Whether the column should be visible by default. */
    private final boolean myVisibleByDefault;

    /** The observable for changes to the meta column. */
    private final ObjectProperty<Object> myObservable = new SimpleObjectProperty<>();

    /**
     * Constructor.
     *
     * @param columnIdentifier The column identifier
     * @param columnClass The column class
     * @param visibleByDefault Whether the column should be visible by default
     */
    public MetaColumn(String columnIdentifier, Class<T> columnClass, boolean visibleByDefault)
    {
        myColumnIdentifier = columnIdentifier;
        myColumnClass = columnClass;
        myVisibleByDefault = visibleByDefault;
    }

    /**
     * Gets the columnIdentifier.
     *
     * @return the columnIdentifier
     */
    public String getColumnIdentifier()
    {
        return myColumnIdentifier;
    }

    /**
     * Gets the columnClass.
     *
     * @return the columnClass
     */
    public Class<T> getColumnClass()
    {
        return myColumnClass;
    }

    /**
     * Gets whether the column should be visible by default.
     *
     * @return whether the column should be visible by default
     */
    public boolean isVisibleByDefault()
    {
        return myVisibleByDefault;
    }

    /**
     * Gets the observable.
     *
     * @return the observable
     */
    public ObjectProperty<Object> getObservable()
    {
        return myObservable;
    }

    /**
     * Converts a row index and data element to a cell value.
     *
     * @param rowIndex the row index
     * @param dataElement the data element
     * @return the cell value
     */
    public abstract T getValue(int rowIndex, DataElement dataElement);
}
