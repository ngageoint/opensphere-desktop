package io.opensphere.csvcommon.detect.lob;

import java.util.Collections;
import java.util.List;

import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.csvcommon.common.CellSampler;
import io.opensphere.csvcommon.detect.CellDetector;
import io.opensphere.csvcommon.detect.ValuesWithConfidence;
import io.opensphere.csvcommon.detect.lob.model.LineOfBearingColumn;
import io.opensphere.csvcommon.detect.lob.model.LobColumnResults;
import io.opensphere.csvcommon.detect.util.CSVColumnPrefsUtil;
import io.opensphere.importer.config.ColumnType;

/**
 * The Class LineOfBearingDetector. Determines if the set of header columns
 * contains a line of bearing column.
 */
public class LineOfBearingDetector implements CellDetector<LobColumnResults>
{
    /** A list of well know line of bearing column names. */
    private final List<String> myLOBColumnNames;

    /**
     * Instantiates a new line of bearing detector.
     *
     * @param prefsRegistry the preferences registry
     */
    public LineOfBearingDetector(PreferencesRegistry prefsRegistry)
    {
        myLOBColumnNames = CSVColumnPrefsUtil.getSpecialKeys(prefsRegistry, ColumnType.LOB);
    }

    @Override
    public ValuesWithConfidence<LobColumnResults> detect(CellSampler sampler)
    {
        LobColumnResults ecr = new LobColumnResults();
        for (int headerIndex = 0; headerIndex < sampler.getHeaderCells().size(); headerIndex++)
        {
            String colName = sampler.getHeaderCells().get(headerIndex);
            for (String lob : myLOBColumnNames)
            {
                if (lob.equalsIgnoreCase(colName.toLowerCase()))
                {
                    LineOfBearingColumn lobCol = new LineOfBearingColumn(colName, headerIndex);
                    ecr.setLineOfBearingColumn(lobCol);
                    break;
                }
            }
        }
        return new ValuesWithConfidence<LobColumnResults>(ecr, Math.max(0f, ecr.getConfidence()));
    }

    /**
     * Gets the line of bearing column names.
     *
     * @return the line of bearing column names
     */
    public List<String> getLineOfBearingColumnNames()
    {
        return Collections.unmodifiableList(myLOBColumnNames);
    }
}
