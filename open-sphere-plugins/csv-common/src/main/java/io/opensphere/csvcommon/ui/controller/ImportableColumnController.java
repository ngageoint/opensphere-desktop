package io.opensphere.csvcommon.ui.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import io.opensphere.analysis.table.model.MetaColumn;
import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.core.util.collections.New;
import io.opensphere.csvcommon.config.v2.CSVParseParameters;
import io.opensphere.csvcommon.detect.util.CSVColumnPrefsUtil;

/**
 * Looks at the set column names, and sets them as do not import if applicable.
 */
public class ImportableColumnController
{
    /**
     * The prefix of the keys for non importable column configurations.
     */
    private static final String ourKeyPrefix = "NonImportable_";

    /**
     * Looks at the detected column names and determines which one should not be
     * imported, if any.
     *
     * @param parameters Contains the detected column names and the list of
     *            columns to not import.
     * @param registry The preferences registry to get non importable
     *            configurations.
     */
    public void detectNonImportableColumns(CSVParseParameters parameters, PreferencesRegistry registry)
    {
        List<String> metaColumns = new ArrayList<String>(Arrays.asList(MetaColumn.COLOR, MetaColumn.HILIGHT, MetaColumn.INDEX,
                MetaColumn.LOB_VISIBLE, MetaColumn.MGRS_DERIVED, MetaColumn.SELECTED, MetaColumn.VISIBLE));
        if (parameters.getColumnNames() != null)
        {
            Map<String, Integer> upperCasedColumnNames = New.map();

            int index = 0;
            for (String columnName : parameters.getColumnNames())
            {
                upperCasedColumnNames.put(columnName.toUpperCase(), Integer.valueOf(index));
                index++;
            }

            for (String columnName : parameters.getColumnNames())
            {
                String key = ourKeyPrefix + columnName.toLowerCase();
                List<String> columnsToNotImport = CSVColumnPrefsUtil.getCustomKeys(registry, key);
                List<String> currentParams = new ArrayList<String>(parameters.getColumnNames());
                List<String> newParams = new ArrayList<String>();
                currentParams.forEach(e ->
                {
                    if (metaColumns.contains(e))
                    {
                        e = e + "_user";
                    }
                    newParams.add(e);
                });
                parameters.setColumnNames(newParams);

                if (columnsToNotImport != null)
                {
                    for (String columns : columnsToNotImport)
                    {
                        String upperCaseColumn = columns.toUpperCase();
                        if (upperCasedColumnNames.containsKey(upperCaseColumn))
                        {
                            parameters.getColumnsToIgnore().add(upperCasedColumnNames.get(upperCaseColumn));
                        }
                    }
                }
            }
        }
    }
}
