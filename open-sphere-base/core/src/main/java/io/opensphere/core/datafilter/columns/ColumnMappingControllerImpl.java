package io.opensphere.core.datafilter.columns;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;

import org.apache.commons.lang3.StringUtils;

import io.opensphere.core.preferences.Preferences;
import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.core.util.ChangeSupport;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.WeakChangeSupport;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.Pair;
import io.opensphere.state.v2.ColumnMappingType;
import io.opensphere.state.v2.ColumnMappings;
import io.opensphere.state.v2.ColumnType;

/**
 * Controller for column mappings.
 *
 *
 * ColumnMappingController(Impl) is a wrapper for a ColumnMappings
 * ColumnMappings is a glorified list of ColumnMappingType ColumnMappingType is
 * an equivalence class of fields; it has:
 * <ul>
 * <li>name: useless</li>
 * <li>description: useless</li>
 * <li>type: fields in this eq. class contain values of this type</li>
 * <li>a list of ColumnType ColumnType identifies a single field; it contains:
 * <ul>
 * <li>layer: the typeKey of the containing datatype</li>
 * <li>value: the name of the field in the containing datatype</li>
 * </ul>
 * </li>
 * </ul>
 */
@SuppressWarnings("PMD.GodClass")
@ThreadSafe
public class ColumnMappingControllerImpl implements MutableColumnMappingController
{
    /** The preference key. */
    private static final String PREF_KEY = "columnMappings";

    /** The preferences. */
    private final Preferences myPreferences;

    /** The model. */
    @GuardedBy("this")
    private ColumnMappings myColumnMappings;

    /** The change support. */
    private final ChangeSupport<Consumer<Void>> myChangeSupport = new WeakChangeSupport<>();

    /**
     * Constructor.
     *
     * @param preferencesRegistry The preferences registry
     */
    public ColumnMappingControllerImpl(PreferencesRegistry preferencesRegistry)
    {
        myPreferences = preferencesRegistry != null ? preferencesRegistry.getPreferences(ColumnMappingController.class) : null;
    }

    /** Initializes the controller. */
    public synchronized void initialize()
    {
        myColumnMappings = loadColumnMappings();
    }

    /**
     * {@inheritDoc}
     *
     * @see MutableColumnMappingController#supportsTypes(String, Set, List)
     */
    @Override
    public synchronized boolean supportsTypes(String fromType, Set<String> cols, List<String> toTypes)
    {
        // check the trivial cases first
        if (toTypes == null || toTypes.isEmpty())
        {
            return true;
        }
        if (toTypes.size() == 1 && toTypes.contains(fromType))
        {
            return true;
        }

        // find the equivalence classes for the specified columns; there must
        // be an equivalence class for each column or else we are done
        List<ColumnMappingType> equiv = getEquiv(fromType, cols);
        if (equiv.size() < cols.size())
        {
            return false;
        }

        // each target type should have a field in each equivalence class
        for (ColumnMappingType e : equiv)
        {
            if (!hasAllTypes(e, toTypes))
            {
                return false;
            }
        }
        return true;
    }

    @Override
    public Map<String, Map<String, String>> getDefinedColumns(List<Pair<String, List<String>>> layers)
    {
        Map<String, Map<String, String>> definedColumns = New.map();

        for (Pair<String, List<String>> layer1 : layers)
        {
            List<String> layer1Columns = layer1.getSecondObject();
            for (Pair<String, List<String>> layer2 : layers)
            {
                if (!layer2.equals(layer1))
                {
                    List<String> layer2Columns = layer2.getSecondObject();
                    for (String layer1Column : layer1Columns)
                    {
                        String definedColumn1 = getDefinedColumn(layer1.getFirstObject(), layer1Column);
                        if (StringUtils.isNotEmpty(definedColumn1))
                        {
                            for (String layer2Column : layer2Columns)
                            {
                                String definedColumn2 = getDefinedColumn(layer2.getFirstObject(), layer2Column);
                                if (definedColumn1.equals(definedColumn2))
                                {
                                    if (!definedColumns.containsKey(layer1.getFirstObject()))
                                    {
                                        definedColumns.put(layer1.getFirstObject(), New.map());
                                    }

                                    if(!definedColumns.containsKey(layer2.getFirstObject()))
                                    {
                                        definedColumns.put(layer2.getFirstObject(), New.map());
                                    }

                                    definedColumns.get(layer1.getFirstObject()).put(layer1Column, definedColumn1);
                                    definedColumns.get(layer2.getFirstObject()).put(layer2Column, definedColumn2);
                                }
                            }
                        }
                    }
                }
            }
        }

        return definedColumns;
    }

    /**
     * Gets the equivalence classes for the specified columns; there must be an
     * equivalence class for each column or else we are done.
     *
     * @param type the type for which to search.
     * @param cols The set of column on which the equivalence determination will
     *            be based.
     * @return a List of column mapping types for the equivalence classes.
     */
    private List<ColumnMappingType> getEquiv(String type, Set<String> cols)
    {
        List<ColumnMappingType> equiv = new LinkedList<>();
        for (ColumnMappingType cmt : myColumnMappings.getColumnMapping())
        {
            if (hasField(cmt, type, cols))
            {
                equiv.add(cmt);
            }
        }
        return equiv;
    }

    /**
     * Tests to determine if the supplied column mapping type contains a field
     * named t, and also tests to determine if that column mapping is present in
     * the supplied set.
     *
     * @param c the mapping type to test.
     * @param t the name of the field for which to search.
     * @param f the set of columns on which to base the test.
     * @return true if the column mapping type contains the field, false
     *         otherwise.
     */
    private static boolean hasField(ColumnMappingType c, String t, Set<String> f)
    {
        for (ColumnType ct : c.getColumn())
        {
            if (ct.getLayer().equals(t) && f.contains(ct.getValue()))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Tests to determine if the supplied column mapping type contains all of
     * the supplied fields.
     *
     * @param c the mapping type to test.
     * @param t the list of fields for which to search.
     * @return true if the supplied column mapping type contains all of the
     *         supplied fields.
     */
    private static boolean hasAllTypes(ColumnMappingType c, List<String> t)
    {
        Set<String> notFound = new TreeSet<>(t);
        c.getColumn().stream().forEach(ct -> notFound.remove(ct.getLayer()));
        return notFound.isEmpty();
    }

    @Override
    public synchronized Collection<String> getDefinedColumns()
    {
        return myColumnMappings.getColumnMapping().stream().map(cm -> cm.getName()).collect(Collectors.toList());
    }

    @Override
    public synchronized String getDefinedColumn(String layerKey, String layerColumn)
    {
        // This could be made more efficient
        String definedColumn = null;
        for (ColumnMappingType columnMapping : myColumnMappings.getColumnMapping())
        {
            for (ColumnType column : columnMapping.getColumn())
            {
                if (column.getLayer().equals(layerKey) && column.getValue().equals(layerColumn))
                {
                    definedColumn = columnMapping.getName();
                    break;
                }
            }
        }
        return definedColumn;
    }

    @Override
    public synchronized String getLayerColumn(String layerKey, String definedColumn)
    {
        // This could be made more efficient
        return myColumnMappings.getColumnMapping().stream().filter(cm -> cm.getName().equals(definedColumn))
                .flatMap(cm -> cm.getColumn().stream()).filter(c -> c.getLayer().equals(layerKey)).map(c -> c.getValue())
                .findAny().orElse(null);
    }

    @Override
    public synchronized List<ColumnMapping> getMappings(String definedColumn)
    {
        List<ColumnMapping> mappings = Collections.emptyList();
        ColumnMappingType columnMapping = getColumnMapping(definedColumn);
        if (columnMapping != null)
        {
            mappings = columnMapping.getColumn().stream().map(c -> new ColumnMapping(columnMapping.getName(),
                    c.getLayer() == null ? ALL_LAYERS : c.getLayer(), c.getValue())).collect(Collectors.toList());
        }
        return mappings;
    }

    @Override
    public String getMappedColumn(String sourceColumn, String sourceLayerKey, String targetLayerKey)
    {
        String definedColumn = Utilities.getValue(getDefinedColumn(sourceLayerKey, sourceColumn), sourceColumn);
        return getLayerColumn(targetLayerKey, definedColumn);
    }

    @Override
    public synchronized String getDescription(String definedColumn)
    {
        ColumnMappingType columnMapping = getColumnMapping(definedColumn);
        return columnMapping != null ? columnMapping.getDescription() : null;
    }

    @Override
    public synchronized String getType(String definedColumn)
    {
        ColumnMappingType columnMapping = getColumnMapping(definedColumn);
        return columnMapping != null ? columnMapping.getType() : null;
    }

    @Override
    public synchronized boolean canAssociate(String type1, String col1, String type2, String col2)
    {
        // trivial case: same type => succeed if and only if same column
        if (type1.equals(type2))
        {
            return col1.equals(col2);
        }

        // look up equivalence classes
        ColumnMappingType eq1 = findEquiv(type1, col1);
        ColumnMappingType eq2 = findEquiv(type2, col2);
        // if they are null or equal but not null, success is automatic
        if (Utilities.sameInstance(eq1, eq2))
        {
            return true;
        }
        // if only one exists, it can't already have a field of the other type
        if (eq1 == null)
        {
            return !eq2.getColumn().stream().anyMatch(c -> c.getLayer().equals(type1));
        }
        if (eq2 == null)
        {
            return !eq1.getColumn().stream().anyMatch(c -> c.getLayer().equals(type2));
        }
        // reaching this point implies that a merge would be necessary
        // types must match
        if (!eq1.getType().equals(eq2.getType()))
        {
            return false;
        }
        // they must not have columns from the same type
        Set<String> allTypes1 = new TreeSet<>();
        eq1.getColumn().forEach(c -> allTypes1.add(c.getLayer()));
        return !eq2.getColumn().stream().anyMatch(c -> allTypes1.contains(c.getLayer()));
    }

    @Override
    public synchronized void associate(String t1, String f1, String t2, String f2, String valType)
    {
        // trivial case--they are already the same thing
        if (t1.equals(t2) && f1.equals(f2))
        {
            return;
        }

        ColumnMappingType eq1 = findEquiv(t1, f1);
        ColumnMappingType eq2 = findEquiv(t2, f2);
        if (eq1 == null && eq2 == null)
        {
            // both are null => create a new equivalence class for them
            ColumnMappingType eq = makeEquiv(f1);
            eq.setType(valType);
            eq.getColumn().add(makeColType(t1, f1));
            eq.getColumn().add(makeColType(t2, f2));
            myColumnMappings.getColumnMapping().add(eq);
        }
        else if (Utilities.sameInstance(eq1, eq2))
        {
            // they are already in the same equivalence class--we are done.
            return;
        }
        else if (eq1 == null)
        {
            eq2.getColumn().add(makeColType(t1, f1));
        }
        else if (eq2 == null)
        {
            eq1.getColumn().add(makeColType(t2, f2));
        }
        else
        {
            // they are already in separate equivalence classes;
            // creating this association will either
            // - break some existing associations or
            // - join the two existing equivalence classes together into one;
            // the latter is preferable, but may not always be possible
            // because the two classes may have columns from the same type;
            // in practice, this occurrence may be rare, but possible.

            // for now, force the merge and hope for the best
            eq1.getColumn().addAll(eq1.getColumn());
            myColumnMappings.getColumnMapping().remove(eq2);
        }

        // reaching this point implies that at least one change was made
        finishMutation();
    }

    /**
     * Finds the column mapping type named with the supplied column name and of
     * the supplied datatype.
     *
     * @param type the datatype for which to search.
     * @param col the name of the column for which to search.
     * @return the {@link ColumnMappingType} extracted from the
     *         {@link #myColumnMappings} field matching the supplied type and
     *         column name.
     */
    private ColumnMappingType findEquiv(String type, String col)
    {
        return myColumnMappings.getColumnMapping().stream()
                .filter(c -> c.getColumn().stream().anyMatch(x -> fieldMatch(x, type, col))).findAny().orElse(null);
    }

    /**
     * Creates a new column mapping type with the supplied name, and returns it.
     *
     * @param name the name of the column mapping type to create.
     * @return a newly generated column mapping type, populated with the
     *         supplied name.
     */
    private ColumnMappingType makeEquiv(String name)
    {
        ColumnMappingType eq = new ColumnMappingType();
        eq.setName(name);
        eq.setDescription(null);
        eq.setType(null);
        return eq;
    }

    /**
     * Creates a new column type using the supplied data type and column value.
     *
     * @param type the layer to which the column is bound.
     * @param col the value of the column.
     * @return the {@link ColumnType} created from the supplied values.
     */
    private static ColumnType makeColType(String type, String col)
    {
        ColumnType c = new ColumnType();
        c.setLayer(type);
        c.setValue(col);
        return c;
    }

    /**
     * Tests to determine if the supplied column type's layer and value match
     * the supplied parameters.
     *
     * @param f the column type to test.
     * @param layer the name of the layer to use during the test.
     * @param col the value of the column to use during the test.
     * @return true if the supplied column type matches the supplied layer and
     *         value.
     */
    private static boolean fieldMatch(ColumnType f, String layer, String col)
    {
        return f.getLayer().equals(layer) && f.getValue().equals(col);
    }

    @Override
    public synchronized void addMapping(String definedColumn, String layerKey, String layerColumn, boolean overwrite)
    {
        ColumnMappingType columnMapping = getColumnMapping(definedColumn);
        if (columnMapping == null)
        {
            columnMapping = new ColumnMappingType();
            columnMapping.setName(definedColumn);
            columnMapping.setType(null);
            columnMapping.setDescription(null);
            myColumnMappings.getColumnMapping().add(columnMapping);
        }

        ColumnType column = columnMapping.getColumn().stream().filter(c -> c.getLayer().equals(layerKey)).findAny().orElse(null);
        if (column == null)
        {
            column = new ColumnType();
            column.setLayer(layerKey);
            columnMapping.getColumn().add(column);
        }

        if (overwrite || column.getValue() == null)
        {
            column.setValue(ALL_LAYERS.equals(layerColumn) ? null : layerColumn);
            finishMutation();
        }
    }

    @Override
    public synchronized void rename(String definedColumn, String newColumn)
    {
        ColumnMappingType columnMapping = getColumnMapping(definedColumn);
        if (columnMapping != null)
        {
            columnMapping.setName(newColumn);
            finishMutation();
        }
    }

    @Override
    public synchronized void setDescription(String definedColumn, String description, boolean overwrite)
    {
        ColumnMappingType columnMapping = getColumnMapping(definedColumn);
        if (columnMapping != null && (overwrite || columnMapping.getDescription() == null))
        {
            columnMapping.setDescription(description);
            finishMutation();
        }
    }

    @Override
    public synchronized void setType(String definedColumn, String type, boolean overwrite)
    {
        ColumnMappingType columnMapping = getColumnMapping(definedColumn);
        if (columnMapping != null && (overwrite || columnMapping.getType() == null))
        {
            columnMapping.setType(type);
            finishMutation();
        }
    }

    @Override
    public synchronized void clearMappings(String definedColumn)
    {
        ColumnMappingType columnMapping = getColumnMapping(definedColumn);
        if (columnMapping != null && !columnMapping.getColumn().isEmpty())
        {
            columnMapping.getColumn().clear();
            finishMutation();
        }
    }

    @Override
    public synchronized void remove(String definedColumn)
    {
        ColumnMappingType columnMapping = getColumnMapping(definedColumn);
        if (columnMapping != null)
        {
            myColumnMappings.getColumnMapping().remove(columnMapping);
            finishMutation();
        }
    }

    @Override
    public void addListener(Consumer<Void> listener)
    {
        myChangeSupport.addListener(listener);
    }

    @Override
    public void removeListener(Consumer<Void> listener)
    {
        myChangeSupport.removeListener(listener);
    }

    /**
     * Loads the column mappings from the preferences.
     *
     * @return the column mappings
     */
    ColumnMappings loadColumnMappings()
    {
        ColumnMappings mappings = myPreferences != null ? myPreferences.getJAXBObject(ColumnMappings.class, PREF_KEY, null)
                : null;
        if (mappings == null)
        {
            mappings = new ColumnMappings();
        }
        return mappings;
    }

    /**
     * Saves the column mappings to the preferences.
     *
     * @param columnMappings the column mappings
     */
    void saveColumnMappings(ColumnMappings columnMappings)
    {
        if (myPreferences != null)
        {
            myPreferences.putJAXBObject(PREF_KEY, columnMappings, false, this);
        }
    }

    /** Finishes a mutation of the controller. */
    private void finishMutation()
    {
        notifyListeners();
        saveColumnMappings();
    }

    /** Saves the column mappings to the preferences. */
    private void saveColumnMappings()
    {
        saveColumnMappings(ColumnMappingUtils.copy(myColumnMappings));
    }

    /** Notifies listeners that a change has occurred. */
    private void notifyListeners()
    {
        myChangeSupport.notifyListeners(listener -> listener.accept(null));
    }

    /**
     * Gets the column mapping for the defined column.
     *
     * @param definedColumn the defined column
     * @return the column mapping, or null
     */
    private ColumnMappingType getColumnMapping(final String definedColumn)
    {
        return myColumnMappings.getColumnMapping().stream().filter(cm -> cm.getName().equals(definedColumn)).findAny()
                .orElse(null);
    }
}
