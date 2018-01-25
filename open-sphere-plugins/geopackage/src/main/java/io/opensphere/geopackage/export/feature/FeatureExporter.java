package io.opensphere.geopackage.export.feature;

import java.util.Collection;
import java.util.stream.Collectors;

import io.opensphere.core.util.cache.SimpleCache;
import io.opensphere.core.util.collections.New;
import io.opensphere.geopackage.export.model.ExportModel;
import io.opensphere.geopackage.export.model.GeoPackageSubExporter;
import io.opensphere.mantle.crust.DataTypeChecker;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.cache.DataElementCache;
import io.opensphere.mantle.data.element.DataElement;
import io.opensphere.mantle.data.util.DataElementLookupUtils;

/**
 * Given a list of {@link DataTypeInfo} this class will export all with feature
 * data to geopackage tables.
 */
public class FeatureExporter implements GeoPackageSubExporter
{
    /** The unfiltered data types to export. */
    private final Collection<? extends DataTypeInfo> myDataTypes;

    /**
     * Exports all features for a given {@link DataTypeInfo}.
     */
    private final FeatureLayerExporter myLayerExporter;

    /** Map from data type to element count. */
    private final SimpleCache<DataTypeInfo, Integer> myTypeToElementCount;

    /**
     * Determines if the data type if supported by this exporter type.
     *
     * @param dataType the data type
     * @return whether the data type is exportable
     */
    public static boolean isExportable(DataTypeInfo dataType)
    {
        return DataTypeChecker.isFeatureType(dataType);
    }

    /**
     * Constructs a new feature exporter.
     *
     * @param dataTypes The unfiltered data types to export
     * @param lookupUtils Used to get {@link DataTypeInfo}s' {@link DataElement}
     *            .
     * @param elementCache Used to get the data element counts of feature
     *            layers.
     */
    public FeatureExporter(Collection<? extends DataTypeInfo> dataTypes, DataElementLookupUtils lookupUtils,
            DataElementCache elementCache)
    {
        myDataTypes = dataTypes;
        myLayerExporter = new FeatureLayerExporter(lookupUtils);
        myTypeToElementCount = new SimpleCache<>(New.map(), elementCache::getElementCountForType);
    }

    @Override
    public Collection<DataTypeInfo> getExportableTypes()
    {
        return myDataTypes.stream().filter(t -> isExportable(t) && myTypeToElementCount.apply(t).intValue() > 0)
                .collect(Collectors.toList());
    }

    @Override
    public int getRecordCount()
    {
        return myDataTypes.stream().filter(FeatureExporter::isExportable).mapToInt(myTypeToElementCount::apply).sum();
    }

    @Override
    public void export(ExportModel model)
    {
        for (DataTypeInfo dataType : myDataTypes)
        {
            if (isExportable(dataType))
            {
                myLayerExporter.exportFeatures(dataType, model.getGeoPackage(), model.getProgressReporter().getModel(),
                        model.getProgressReporter().getTaskActivity());
            }

            if (model.getProgressReporter().getTaskActivity().isCancelled())
            {
                break;
            }
        }
    }
}
