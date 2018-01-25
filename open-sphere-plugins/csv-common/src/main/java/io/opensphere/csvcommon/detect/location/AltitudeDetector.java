package io.opensphere.csvcommon.detect.location;

import io.opensphere.csvcommon.common.CellSampler;
import io.opensphere.csvcommon.detect.CellDetector;
import io.opensphere.csvcommon.detect.ValuesWithConfidence;
import io.opensphere.importer.config.ColumnType;
import io.opensphere.importer.config.SpecialColumn;

/** The AltitudeDetector determines if a header has an altitude column. */
public class AltitudeDetector implements CellDetector<SpecialColumn>
{
    @Override
    public ValuesWithConfidence<SpecialColumn> detect(CellSampler sampler)
    {
        SpecialColumn column = new SpecialColumn();
        float confidence = 0f;

        int index = 0;
        for (String colName : sampler.getHeaderCells())
        {
            if (colName.toLowerCase().startsWith("alt"))
            {
                column.setColumnIndex(index);
                column.setColumnType(ColumnType.ALT);
                confidence = 1f;
                break;
            }
            index++;
        }

        return new ValuesWithConfidence<>(column, confidence);
    }
}
