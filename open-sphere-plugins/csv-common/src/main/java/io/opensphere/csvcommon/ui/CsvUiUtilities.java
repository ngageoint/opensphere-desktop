package io.opensphere.csvcommon.ui;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.collections.StreamUtilities;
import io.opensphere.core.util.lang.StringUtilities;

/**
 * CSV UI Utilities.
 */
public final class CsvUiUtilities
{
    /**
     * Combines the rows into one string that is padded with spaces in order to
     * play better in scroll panes. Not padding the rows can result in the
     * scroll pane moving around when the user clicks in far right areas of
     * short rows.
     *
     * @param rows the rows
     * @return the combined text
     */
    public static String combineText(Collection<? extends String> rows)
    {
        int maxLength = 0;
        for (String row : rows)
        {
            if (row.length() > maxLength)
            {
                maxLength = row.length();
            }
        }
        final int finalMaxLength = maxLength;

        Collection<String> paddedRows = StreamUtilities.map(rows, new Function<String, String>()
        {
            @Override
            public String apply(String row)
            {
                return StringUtilities.pad(row, finalMaxLength);
            }
        });

        return StringUtilities.join("\n", paddedRows);
    }

    /**
     * Generates a list of default column identifiers (for when there is no
     * header).
     *
     * @param columnCount the number of columns
     * @return the column identifiers
     */
    public static List<String> generateDefaultColumnIdentifiers(int columnCount)
    {
        List<String> columnIdentifiers = New.list(columnCount);
        for (int i = 1; i <= columnCount; i++)
        {
            columnIdentifiers.add("COLUMN_" + i);
        }
        return columnIdentifiers;
    }

    /** Disallow instantiation. */
    private CsvUiUtilities()
    {
    }
}
