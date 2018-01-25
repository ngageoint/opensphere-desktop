package io.opensphere.mantle.data.merge;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.MetaDataInfo;
import io.opensphere.mantle.data.SpecialKey;
import io.opensphere.mantle.data.element.MetaDataProvider;
import io.opensphere.mantle.data.element.impl.MDILinkedMetaDataProvider;
import io.opensphere.mantle.data.impl.DefaultMetaDataInfo;
import io.opensphere.mantle.data.merge.MergeKeySpecification.ConversionHint;

/**
 * The Class DataTypeMergeAssistant.
 */
@SuppressWarnings("PMD.GodClass")
public final class DataTypeMergeAssistant
{
    /** The SOURCE_TYPE. */
    public static final String SOURCE_TYPE = "SOURCE_TYPE";

    /** The Data type key to data type display name map. */
    private final Map<String, String> myDataTypeKeyToDataTypeDisplayNameMap;

    /** The Data type key to merge component map. */
    private final Map<String, DataTypeMergeComponentAssistant> myDataTypeKeyToMergeComponentMap;

    /** The Merge key names. */
    private final List<MergeKeySpecification> myMergeKeySpecs;

    /**
     * Creates a DataTypeMergeAssistant from a DataTypeMergeMap.
     *
     * @param map the {@link DataTypeMergeMap}
     * @return the {@link DataTypeMergeAssistant}
     */
    public static DataTypeMergeAssistant createFromMergeMap(DataTypeMergeMap map)
    {
        DataTypeMergeAssistant assistant = new DataTypeMergeAssistant();
        assistant.configureFromMergeMap(map);
        return assistant;
    }

    /**
     * Creates the meta data info.
     *
     * @param ksList the ks list
     * @param includeSourceTypeKey the include source type key
     * @param columnFilter the column filter
     * @return the meta data info
     */
    public static DefaultMetaDataInfo createMetaDataInfo(List<? extends KeySpecification> ksList, boolean includeSourceTypeKey,
            Set<String> columnFilter)
    {
        List<String> allKeyList = New.list();
        if (includeSourceTypeKey)
        {
            allKeyList.add(SOURCE_TYPE);
        }
        for (KeySpecification mks : ksList)
        {
            allKeyList.add(mks.getKeyName());
        }

        DefaultMetaDataInfo dmdi = new DefaultMetaDataInfo(allKeyList, Collections.<String>emptyList());

        if (includeSourceTypeKey)
        {
            dmdi.addKey(SOURCE_TYPE, String.class, null);
        }
        for (KeySpecification mks : ksList)
        {
            Class<?> colClass = KeySpecification.getKeyClassForClassName(mks.getClassName(), String.class);
            String specialKeyClassName = mks.getSpecialKeyClassName();

            SpecialKey sk = KeySpecification.getSpecialKeyForSpecialKeyClassName(specialKeyClassName);

            if (columnFilter == null || columnFilter.isEmpty() || !columnFilter.contains(mks.getKeyName()))
            {
                dmdi.addKey(mks.getKeyName(), colClass, null);
                if (sk != null)
                {
                    dmdi.setSpecialKey(mks.getKeyName(), sk, null);
                }

                if (Number.class.isAssignableFrom(colClass))
                {
                    dmdi.setKeyNumeric(mks.getKeyName());
                }
            }
        }

        return dmdi;
    }

    /**
     * Convert value using hint.
     *
     * @param valToConvert the val to convert
     * @param conversionHint the conversion hint
     * @return the object
     */
    private static Object convertValueUsingHint(Object valToConvert, ConversionHint conversionHint)
    {
        ConversionHint hint = conversionHint == null ? ConversionHint.NONE : conversionHint;
        Object val = valToConvert;
        if (val != null && hint != ConversionHint.NONE)
        {
            switch (hint)
            {
                case CONVERT_TO_DOUBLE:
                    if (val instanceof Number)
                    {
                        val = Double.valueOf(((Number)val).doubleValue());
                    }
                    else
                    {
                        val = null;
                    }
                    break;
                case CONVERT_TO_INTEGER:
                    if (val instanceof Number)
                    {
                        val = Integer.valueOf(((Number)val).intValue());
                    }
                    else
                    {
                        val = null;
                    }
                    break;
                case CONVERT_TO_LONG:
                    if (val instanceof Number)
                    {
                        val = Long.valueOf(((Number)val).longValue());
                    }
                    else
                    {
                        val = null;
                    }
                    break;
                case CONVERT_TO_SHORT:
                    // Can only happen when it is Byte and Short
                    if (val instanceof Number)
                    {
                        val = Short.valueOf(((Number)val).shortValue());
                    }
                    else
                    {
                        val = null;
                    }
                    break;
                default: // Covers CONVERT_TO_STRING and all others.
                    val = val.toString();
                    break;
            }
        }
        return val;
    }

    /**
     * Instantiates a new data type merge assistant.
     */
    private DataTypeMergeAssistant()
    {
        myDataTypeKeyToMergeComponentMap = new HashMap<>();
        myDataTypeKeyToDataTypeDisplayNameMap = new HashMap<>();
        myMergeKeySpecs = new ArrayList<>();
    }

    /**
     * Creates a list of merged metadata arrays from a list of component meta
     * data providers.
     *
     * @param dtiKey the dti key
     * @param mdProviderList the md provider list
     * @param includeDataType the include data type
     * @return the list
     */
    public List<Object[]> createMergedMetaDataAsArray(String dtiKey, List<MetaDataProvider> mdProviderList,
            boolean includeDataType)
    {
        return createMergedMetaDataAsArray(dtiKey, mdProviderList, includeDataType, null);
    }

    /**
     * Creates a list of merged metadata arrays from a list of component meta
     * data providers.
     *
     * @param dtiKey the dti key
     * @param mdProviderList the md provider list
     * @param includeDataType the include data type
     * @param columnFilter the set of columns not to include in the resulting
     *            meta data array
     * @return the list
     */
    public List<Object[]> createMergedMetaDataAsArray(String dtiKey, List<MetaDataProvider> mdProviderList,
            boolean includeDataType, Set<String> columnFilter)
    {
        Utilities.checkNull(dtiKey, "dtiKey");
        Utilities.checkNull(mdProviderList, "mdProviderList");

        DataTypeMergeComponentAssistant mergeCompAssistant = myDataTypeKeyToMergeComponentMap.get(dtiKey);
        if (mergeCompAssistant == null)
        {
            throw new IllegalArgumentException("Data type \"" + dtiKey + " is not a type listed in the mapping.");
        }

        if (mdProviderList.isEmpty())
        {
            return Collections.<Object[]>emptyList();
        }

        int valIndex = 0;
        List<Object[]> resultList = new ArrayList<>(mdProviderList.size());
        for (MetaDataProvider mdProvider : mdProviderList)
        {
            valIndex = 0;
            int filteredColumnCount = determineNumberOfFilteredColumns(mdProvider, columnFilter);
            int numKeys = myMergeKeySpecs.size() + (includeDataType ? 1 : 0) - filteredColumnCount;

            Object[] values = new Object[numKeys];
            if (includeDataType)
            {
                values[valIndex] = myDataTypeKeyToDataTypeDisplayNameMap.get(dtiKey);
                valIndex++;
            }

            for (MergeKeySpecification mergeKeySpec : myMergeKeySpecs)
            {
                String mergeKey = mergeKeySpec.getKeyName();
                if (columnFilter == null || columnFilter.isEmpty() || !columnFilter.contains(mergeKey))
                {
                    String compKey = mergeCompAssistant.getCompKey(mergeKey);
                    if (compKey == null)
                    {
                        values[valIndex] = null;
                    }
                    else
                    {
                        Object val = mdProvider.getValue(compKey);
                        if (val != null)
                        {
                            val = convertValueUsingHint(val, mergeKeySpec.getConversionHint());
                        }
                        values[valIndex] = val;
                    }
                    valIndex++;
                }
            }
            resultList.add(values);
        }
        return resultList;
    }

    /**
     * Creates the merged meta data as an object array.
     *
     * @param dtiKey the {@link DataTypeInfo} key for the component type to be
     *            merged/converted
     * @param mdProvider the {@link MetaDataProvider}
     * @param includeDataType the include data type
     * @return the result merged object[] containing the mapped meta data.
     */
    public Object[] createMergedMetaDataAsArray(String dtiKey, MetaDataProvider mdProvider, boolean includeDataType)
    {
        return createMergedMetaDataAsArray(dtiKey, mdProvider, includeDataType, null);
    }

    /**
     * Creates the merged meta data as an object array.
     *
     * @param dtiKey the {@link DataTypeInfo} key for the component type to be
     *            merged/converted
     * @param mdProvider the {@link MetaDataProvider}
     * @param includeDataType the include data type
     * @param columnFilter the set of columns not to include in the resulting
     *            meta data array
     * @return the result merged object[] containing the mapped meta data.
     */
    public Object[] createMergedMetaDataAsArray(String dtiKey, MetaDataProvider mdProvider, boolean includeDataType,
            Set<String> columnFilter)
    {
        Utilities.checkNull(dtiKey, "dtiKey");
        Utilities.checkNull(mdProvider, "mdProvider");
        List<Object[]> resultList = createMergedMetaDataAsArray(dtiKey, Collections.singletonList(mdProvider), includeDataType,
                columnFilter);
        return resultList.isEmpty() ? null : resultList.get(0);
    }

    /**
     * Creates a list of merged MetaDataProviders that are the mapped values
     * from the provided MetaDataProviders of the component type.
     *
     * @param mergedDataTypeInfo the merged data type info
     * @param compDTIKey the component type {@link DataTypeInfo} key.
     * @param compMDProviderList the list of component {@link MetaDataProvider}
     * @param includeDataType the include data type
     * @return the list of merged {@link MetaDataProvider}
     */
    public List<MetaDataProvider> createMergedMetaDataAsMetaDataProvider(DataTypeInfo mergedDataTypeInfo, String compDTIKey,
            List<MetaDataProvider> compMDProviderList, boolean includeDataType)
    {
        return createMergedMetaDataAsMetaDataProvider(mergedDataTypeInfo, compDTIKey, compMDProviderList, includeDataType, null);
    }

    /**
     * Creates a list of merged MetaDataProviders that are the mapped values
     * from the provided MetaDataProviders of the component type.
     *
     * @param mergedDataTypeInfo the merged data type info
     * @param compDTIKey the component type {@link DataTypeInfo} key.
     * @param compMDProviderList the list of component {@link MetaDataProvider}
     * @param includeDataType the include data type
     * @param columnFilter the set of columns not to include in the resulting
     *            meta data array ( note that it will not alter the
     *            {@link MetaDataInfo} in the mergedDataType info, it presumes
     *            the keys have already been removed for filtered columns, but
     *            any values matching the keys in the column filter will not be
     *            included in the result.
     * @return the list of merged {@link MetaDataProvider}
     */
    public List<MetaDataProvider> createMergedMetaDataAsMetaDataProvider(DataTypeInfo mergedDataTypeInfo, String compDTIKey,
            List<MetaDataProvider> compMDProviderList, boolean includeDataType, Set<String> columnFilter)
    {
        Utilities.checkNull(mergedDataTypeInfo, "mergedDataTypeInfo");
        Utilities.checkNull(compMDProviderList, "compMDProviderList");

        if (compMDProviderList.isEmpty())
        {
            return Collections.<MetaDataProvider>emptyList();
        }

        if (mergedDataTypeInfo.getMetaDataInfo() == null)
        {
            throw new IllegalArgumentException("Provided merged DataTypeInfo does not have a MetaDataInfo");
        }

        DataTypeMergeComponentAssistant mergeCompAssistant = myDataTypeKeyToMergeComponentMap.get(compDTIKey);
        if (mergeCompAssistant == null)
        {
            throw new IllegalArgumentException("Data type \"" + compDTIKey + " is not a type listed in the mapping.");
        }

        List<MetaDataProvider> resultProviderList = new ArrayList<>(compMDProviderList.size());

        for (MetaDataProvider compMDProvider : compMDProviderList)
        {
            MDILinkedMetaDataProvider provider = new MDILinkedMetaDataProvider(mergedDataTypeInfo.getMetaDataInfo());
            if (includeDataType)
            {
                provider.setValue(SOURCE_TYPE, myDataTypeKeyToDataTypeDisplayNameMap.get(compDTIKey));
            }
            for (MergeKeySpecification mergeKeySpec : myMergeKeySpecs)
            {
                String mergeKey = mergeKeySpec.getKeyName();
                if (columnFilter == null || columnFilter.isEmpty() || !columnFilter.contains(mergeKey))
                {
                    String compKey = mergeCompAssistant.getCompKey(mergeKey);
                    Object val = compKey == null ? null : compMDProvider.getValue(compKey);
                    provider.setValue(mergeKeySpec.getKeyName(),
                            (Serializable)convertValueUsingHint(val, mergeKeySpec.getConversionHint()));
                }
            }
            resultProviderList.add(provider);
        }

        return resultProviderList;
    }

    /**
     * Creates the merged meta data as meta data provider.
     *
     * @param mergedDataTypeInfo the merged {@link DataTypeInfo}
     * @param compDTIKey the component type {@link DataTypeInfo} key.
     * @param compMDProvider the component type {@link MetaDataProvider}
     * @param includeDataType the include data type
     * @return the meta data provider
     */
    public MetaDataProvider createMergedMetaDataAsMetaDataProvider(DataTypeInfo mergedDataTypeInfo, String compDTIKey,
            MetaDataProvider compMDProvider, boolean includeDataType)
    {
        return createMergedMetaDataAsMetaDataProvider(mergedDataTypeInfo, compDTIKey, compMDProvider, includeDataType, null);
    }

    /**
     * Creates the merged meta data as meta data provider.
     *
     * @param mergedDataTypeInfo the merged {@link DataTypeInfo}
     * @param compDTIKey the component type {@link DataTypeInfo} key.
     * @param compMDProvider the component type {@link MetaDataProvider}
     * @param includeDataType the include data type
     * @param columnFilter the set of columns not to include in the resulting
     *            meta data array
     * @return the meta data provider
     */
    public MetaDataProvider createMergedMetaDataAsMetaDataProvider(DataTypeInfo mergedDataTypeInfo, String compDTIKey,
            MetaDataProvider compMDProvider, boolean includeDataType, Set<String> columnFilter)
    {
        Utilities.checkNull(mergedDataTypeInfo, "mergedDataTypeInfo");
        if (mergedDataTypeInfo.getMetaDataInfo() == null)
        {
            throw new IllegalArgumentException("Provided merged DataTypeInfo does not have a MetaDataInfo");
        }

        List<MetaDataProvider> resultList = createMergedMetaDataAsMetaDataProvider(mergedDataTypeInfo, compDTIKey,
                Collections.singletonList(compMDProvider), includeDataType, columnFilter);

        return resultList.isEmpty() ? null : resultList.get(0);
    }

    /**
     * Creates the a DefaultMetaDataInfo that represents the merged keyset.
     *
     * @param includeSourceTypeKey the include source type key
     * @param columnFilter the set of columns not to include in the resulting
     *            {@link MetaDataInfo}
     * @return the {@link MetaDataInfo}
     */
    public DefaultMetaDataInfo createMergedMetaDataInfo(boolean includeSourceTypeKey, Set<String> columnFilter)
    {
        return createMetaDataInfo(myMergeKeySpecs, includeSourceTypeKey, columnFilter);
    }

    /**
     * Gets the filtered column indices.
     *
     * @param columnFilter the column filter
     * @param includesSourceTypeKey the includes source type key
     * @return the filtered column indices
     */
    public TIntList getFilteredColumnIndices(Set<String> columnFilter, boolean includesSourceTypeKey)
    {
        TIntList result = new TIntArrayList();
        int index = includesSourceTypeKey ? 1 : 0;
        for (MergeKeySpecification mks : myMergeKeySpecs)
        {
            if (columnFilter != null && !columnFilter.isEmpty() && columnFilter.contains(mks.getKeyName()))
            {
                result.add(index);
            }
            index++;
        }
        return result;
    }

    /**
     * Configure the assistant from a merge map.
     *
     * @param map the map
     */
    private void configureFromMergeMap(DataTypeMergeMap map)
    {
        Utilities.checkNull(map, "map");
        myDataTypeKeyToMergeComponentMap.clear();
        myMergeKeySpecs.clear();
        myMergeKeySpecs.addAll(map.getMergedKeyNames());

        if (CollectionUtilities.hasContent(map.getMergeComponents()))
        {
            for (DataTypeMergeComponent comp : map.getMergeComponents())
            {
                myDataTypeKeyToMergeComponentMap.put(comp.getDataTypeKey(), new DataTypeMergeComponentAssistant(comp));
                myDataTypeKeyToDataTypeDisplayNameMap.put(comp.getDataTypeKey(), comp.getDataTypeDisplayName());
            }
        }
    }

    /**
     * Determines the number of keys in the column filter that occur in the
     * {@link MetaDataProvider} key set.
     *
     * @param mdProvider the md provider key set.
     * @param columnFilter the column filter
     * @return the int
     */
    private int determineNumberOfFilteredColumns(MetaDataProvider mdProvider, Set<String> columnFilter)
    {
        int intersectingCount = 0;
        if (columnFilter != null && !columnFilter.isEmpty())
        {
            for (String key : mdProvider.getKeys())
            {
                if (columnFilter.contains(key))
                {
                    intersectingCount++;
                }
            }
        }

        return intersectingCount;
    }

    /**
     * The Class DataTypeMergeComponentAssistant.
     */
    public static class DataTypeMergeComponentAssistant
    {
        /** The Component key toy merge key map. */
        private final Map<String, String> myComponentKeyToyMergeKeyMap;

        /** The Data type key. */
        private final String myDataTypeKey;

        /** The Merge key to component key map. */
        private final Map<String, String> myMergeKeyToComponentKeyMap;

        /**
         * Instantiates a new data type merge component assistant.
         *
         * @param comp the comp
         */
        public DataTypeMergeComponentAssistant(DataTypeMergeComponent comp)
        {
            myMergeKeyToComponentKeyMap = new HashMap<>();
            myComponentKeyToyMergeKeyMap = new HashMap<>();
            myDataTypeKey = comp.getDataTypeKey();
            if (comp.getMetaDataMergeKeyMapEntryList() != null)
            {
                for (MetaDataMergeKeyMapEntry entry : comp.getMetaDataMergeKeyMapEntryList())
                {
                    myMergeKeyToComponentKeyMap.put(entry.getMergeKeyName(), entry.getSourceKeyName());
                    myComponentKeyToyMergeKeyMap.put(entry.getSourceKeyName(), entry.getMergeKeyName());
                }
            }
        }

        /**
         * Gets the comp key.
         *
         * @param mergeKey the merge key
         * @return the comp key
         */
        public String getCompKey(String mergeKey)
        {
            return myMergeKeyToComponentKeyMap.get(mergeKey);
        }

        /**
         * Gets the data type key.
         *
         * @return the data type key
         */
        public String getDataTypeKey()
        {
            return myDataTypeKey;
        }

        /**
         * Gets the merge key.
         *
         * @param compKey the comp key
         * @return the merge key
         */
        public String getMergeKey(String compKey)
        {
            return myComponentKeyToyMergeKeyMap.get(compKey);
        }
    }
}
