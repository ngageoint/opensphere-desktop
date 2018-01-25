package io.opensphere.mantle.data.filter;

import java.util.function.Predicate;

import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.mantle.data.DataTypeInfo;

/**
 * A predicate that only accepts {@link DataTypeInfo}s that have timed data
 * elements.
 */
public class TimedDataElementDataTypeInfoPredicate implements Predicate<DataTypeInfo>
{
    @Override
    public boolean test(DataTypeInfo info)
    {
        boolean isTimeLess = false;

        if (info.getTimeExtents() != null && !info.getTimeExtents().getTimespans().isEmpty())
        {
            for (TimeSpan span : info.getTimeExtents().getTimespans())
            {
                // Data types may have spans with the same start and end date.
                if (span.isTimeless() || span.isInstantaneous())
                {
                    isTimeLess = true;
                    break;
                }
            }
        }
        else
        {
            isTimeLess = true;
        }

        boolean isTimelineOrStaticMapElement = info.getBasicVisualizationInfo() != null
                && info.getBasicVisualizationInfo().getLoadsTo().isAnalysisEnabled() && info.getMapVisualizationInfo() != null
                && info.getMapVisualizationInfo().getVisualizationType().isMapDataElementType();
        boolean usesDataElements = info.getBasicVisualizationInfo() != null
                && info.getBasicVisualizationInfo().usesDataElements();

        return info.getMetaDataInfo() != null && !isTimeLess && (isTimelineOrStaticMapElement || usesDataElements);
    }
}
