package io.opensphere.merge.algorithm;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.crust.SimpleMetaDataProvider;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.MetaDataInfo;
import io.opensphere.mantle.data.SpecialKey;
import io.opensphere.mantle.data.element.DataElement;
import io.opensphere.mantle.data.element.impl.DefaultDataElement;
import io.opensphere.mantle.data.impl.DefaultMetaDataInfo;

/**
 * Utility class.
 */
public final class Util
{
    /** ID counter. */
    private static long ourIdCounter = 1L;

    /**
     * Private constructor, preventing use.
     */
    private Util()
    {
        throw new UnsupportedOperationException("Instantation of utility classes is not permitted.");
    }

    /**
     * Creates an element, using the supplied value map and columns to populate
     * the element. The element is returned after creation.
     *
     * @param valueMap The map from which values are pulled when creating the
     *            data element.
     * @param columns The columns with which to initialize the element.
     * @return The element generated using the supplied parameters.
     */
    public static DataElement createElement(Map<String, Object> valueMap, Set<String> columns)
    {
        DefaultDataElement dataElement = new DefaultDataElement(ourIdCounter++);
        dataElement.setMetaDataProvider(new SimpleMetaDataProvider(valueMap, columns));
        return dataElement;
    }

    /**
     * Gets the column from the supplied layer. If no column is defined using
     * the supplied key, a null value is returned.
     *
     * @param type The layer from which to get the named column.
     * @param key The name of the column to retrieve.
     * @return The column.
     */
    public static Col getColumn(DataTypeInfo type, String key)
    {
        MetaDataInfo layerMetadata = type.getMetaDataInfo();
        if (!layerMetadata.hasKey(key))
        {
            return null;
        }
        Col column = new Col();
        column.owner = type;
        column.name = key;
        column.type = layerMetadata.getKeyClassType(key);
        column.special = layerMetadata.getSpecialKeyToTypeMap().get(key);
        return column;
    }

    /**
     * Gets the metadata info for the List of supplied columns.
     *
     * @param columns The columns for which to get metadata.
     * @return The metadata info.
     */
    public static MetaDataInfo getMeta(List<Col> columns)
    {
        DefaultMetaDataInfo meta = new DefaultMetaDataInfo();
        for (Col column : columns)
        {
            meta.addKey(column.name, column.type, null);
            if (column.special != null)
            {
                meta.setSpecialKey(column.name, column.special, null);
            }
        }
        return meta;
    }

    /**
     * Null-tolerant put operation. This operation will populate the supplied
     * map with the key and value if and only if both are not null.
     *
     * @param map the map into which the key / value pair will be placed.
     * @param key the key to place into the map.
     * @param value the value to place into the map.
     * @param <KEYTYPE> the data type of the key.
     * @param <VALUETYPE> the data type of the value.
     */
    public static <KEYTYPE, VALUETYPE> void putNonNull(Map<KEYTYPE, VALUETYPE> map, KEYTYPE key, VALUETYPE value)
    {
        if (key != null && value != null)
        {
            map.put(key, value);
        }
    }

    /**
     * Validates the supplied collection of equivalence classes.
     *
     * @param columnsToValidate The columns on which validation is performed.
     * @return an error message, if an error is found, or null.
     */
    private static String validate(List<Col> columnsToValidate)
    {
        Set<String> owners = new TreeSet<>();
        SpecialKey sp = columnsToValidate.get(0).special;
        for (Col columnToValidate : columnsToValidate)
        {
            // secondly, if any one is special, then they all must be
            if (!Objects.equals(sp, columnToValidate.special))
            {
                return "Special columns do not align.";
            }
            owners.add(columnToValidate.owner.getTypeKey());
        }

        // lastly, no two of them can come from the same DataTypeInfo
        if (owners.size() < columnsToValidate.size())
        {
            return "Identified columns are from the same layer.";
        }

        // it passes all tests ... must be okay
        return null;
    }

    /**
     * Validates all of the supplied equivalence classes. If one or more
     * failures occur, an error message is returned to the caller indicating the
     * cause. If no failures occur, a null value is returned.
     *
     * @param columnEquivalenceClasses The column equivalence classes on which
     *            validation is performed.
     * @return an error message, if an error is found, or null
     */
    public static String validateAll(List<List<Col>> columnEquivalenceClasses)
    {
        // keep track of equivalence class specialties
        Map<SpecialKey, Set<Col>> specialColumns = New.map();
        for (List<Col> equivalenceCases : columnEquivalenceClasses)
        {
            // check for internal consistency in each equivalence class
            String validationFailureMessage = validate(equivalenceCases);
            if (validationFailureMessage != null)
            {
                return validationFailureMessage;
            }
            // check for another equivalence class with the same specialty
            Col column = equivalenceCases.get(0);
            SpecialKey specialtyKey = column.special;
            if (specialtyKey != null)
            {
                if (!specialColumns.containsKey(specialtyKey))
                {
                    specialColumns.put(specialtyKey, New.set());
                }

                // claim the specialty:
                specialColumns.get(specialtyKey).add(column);
            }

            boolean errorFound = false;
            StringBuilder errorMessage = new StringBuilder("Multiple columns with the same special type were found. ");
            errorMessage.append("Create column mappings for the following:");

            for (Map.Entry<SpecialKey, Set<Col>> specialColumnEntry : specialColumns.entrySet())
            {
                // if there are cases in which a special key type occurs more
                // than once without a mapping, it's an error that the user must
                // correct:
                if (specialColumnEntry.getValue().size() > 1)
                {
                    errorFound = true;
                    appendError(errorMessage, specialColumnEntry.getKey(), specialColumnEntry.getValue());
                }
            }

            if (errorFound)
            {
                return errorMessage.toString();
            }
        }
        return null;
    }

    /**
     * Appends a useful error to the supplied string builder, based on the
     * column entry.
     *
     * @param errorMessage the {@link StringBuilder} to which to append the
     *            error message.
     * @param key the special key type for which the error message will be
     *            created.
     * @param value the set of columns with the supplied special key type.
     */
    protected static void appendError(StringBuilder errorMessage, SpecialKey key, Set<Col> value)
    {
        boolean firstEntry = true;
        errorMessage.append("\n    ");
        errorMessage.append(key.getKeyName());
        errorMessage.append(": ");
        for (Col offendingColumn : value)
        {
            if (!firstEntry)
            {
                errorMessage.append(", ");
            }
            firstEntry = false;
            errorMessage.append(offendingColumn.owner.getDisplayName());
            errorMessage.append(".");
            errorMessage.append(offendingColumn.name);
        }
    }
}
