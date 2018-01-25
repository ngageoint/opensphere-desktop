package io.opensphere.analysis.baseball;

import java.util.Collections;
import java.util.List;

import io.opensphere.analysis.table.model.MGRSMetaColumn;
import io.opensphere.analysis.table.model.MetaColumn;
import io.opensphere.core.preferences.Preferences;
import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.collections.StreamUtilities;
import io.opensphere.core.util.swing.table.AbstractColumnTableModel;
import io.opensphere.mantle.data.SpecialKey;
import io.opensphere.mantle.data.element.DataElement;

/**
 * The baseball card table model.
 */
class BaseballTableModel extends AbstractColumnTableModel
{
    /** The serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The key order preference topic. */
    private static final String KEY_ORDER_TOPIC = "io.opensphere.externaltools.model.DotTableModel";

    /** The data element. */
    private final transient DataElement myElement;

    /** The keys. */
    private final List<String> myKeys;

    /** The values. */
    private final List<Object> myValues;

    /**
     * Constructor.
     *
     * @param element the data element
     * @param prefsRegistry The preferences registry
     */
    public BaseballTableModel(DataElement element, PreferencesRegistry prefsRegistry)
    {
        super();
        myElement = element;
        myKeys = New.list();
        myValues = New.list();
        setColumnIdentifiers("Field", "Value");
        setColumnClasses(String.class, Object.class);
        addMetaColumnData();
        addNormalData(prefsRegistry);
        setTimeField();
    }

    @Override
    public int getRowCount()
    {
        return myKeys.size();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex)
    {
        return columnIndex == 0 ? myKeys.get(rowIndex) : myValues.get(rowIndex);
    }

    /**
     * Gets the data element.
     *
     * @return the data element
     */
    public DataElement getDataElement()
    {
        return myElement;
    }

    /**
     * Gets the special key for the given row.
     *
     * @param rowIndex the row index
     * @return the specialKey
     */
    public SpecialKey getSpecialKey(int rowIndex)
    {
        String key = myKeys.get(rowIndex);
        return myElement.getDataTypeInfo().getMetaDataInfo().getSpecialTypeForKey(key);
    }

    /**
     * Adds meta column data.
     */
    private void addMetaColumnData()
    {
        List<MetaColumn<?>> metaColumns = Collections.<MetaColumn<?>>singletonList(new MGRSMetaColumn());
        for (MetaColumn<?> metaColumn : metaColumns)
        {
            myKeys.add(metaColumn.getColumnIdentifier());
            myValues.add(metaColumn.getValue(-1, myElement));
        }
    }

    /**
     * Adds normal data.
     *
     * @param prefsRegistry The preferences registry
     */
    private void addNormalData(PreferencesRegistry prefsRegistry)
    {
        Preferences prefs = prefsRegistry.getPreferences(KEY_ORDER_TOPIC);
        List<String> alternateKeyOrder = prefs.getStringList(myElement.getDataTypeInfo().getTypeKey() + ".AKO", null);
        if (alternateKeyOrder != null)
        {
            alternateKeyOrder = New.list(alternateKeyOrder);
            alternateKeyOrder.retainAll(myElement.getMetaData().getKeys());
            myKeys.addAll(alternateKeyOrder);
            myValues.addAll(StreamUtilities.map(alternateKeyOrder, key -> myElement.getMetaData().getValue(key)));
        }
        else
        {
            myKeys.addAll(myElement.getMetaData().getKeys());
            myValues.addAll(myElement.getMetaData().getValues());
        }
    }

    /**
     * Set the time field to the TimeSpan.
     */
    private void setTimeField()
    {
        String timeKey = myElement.getDataTypeInfo().getMetaDataInfo().getTimeKey();
        int timeIndex = timeKey == null ? -1 : myKeys.indexOf(timeKey);
        if (timeIndex != -1)
        {
            myValues.set(timeIndex, myElement.getTimeSpan());
        }
    }
}
