package io.opensphere.geopackage.export;

import java.awt.Component;
import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nullable;
import javax.swing.JComponent;

import io.opensphere.core.Toolbox;
import io.opensphere.core.control.action.context.GeometryContextKey;
import io.opensphere.core.export.AbstractExporter;
import io.opensphere.core.export.ExportException;
import io.opensphere.core.export.ExportUtilities;
import io.opensphere.core.geometry.PolygonGeometry;
import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.util.MimeType;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.swing.SwingUtilities;
import io.opensphere.core.util.taskactivity.CancellableTaskActivity;
import io.opensphere.geopackage.export.feature.FeatureExporter;
import io.opensphere.geopackage.export.model.ExportModel;
import io.opensphere.geopackage.export.model.GeoPackageSubExporter;
import io.opensphere.geopackage.export.tile.TileExporter;
import io.opensphere.geopackage.export.ui.ExportOptionsPanel;
import io.opensphere.geopackage.export.ui.UserAskerImpl;
import io.opensphere.geopackage.model.ProgressModel;
import io.opensphere.geopackage.progress.ProgressReporter;
import io.opensphere.mantle.MantleToolbox;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.util.MantleToolboxUtils;
import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.GeoPackageConstants;
import mil.nga.geopackage.manager.GeoPackageManager;
import mil.nga.geopackage.validate.GeoPackageValidate;

/**
 * An exporter that takes {@link DataTypeInfo} as the export objects and exports
 * their data, features or tiles, to a user selected geopackage file.
 */
public class GeoPackageExporter extends AbstractExporter
{
    /** The bounding box for the export. */
    @Nullable
    private GeographicBoundingBox myBbox;

    /** The export options panel. */
    private volatile ExportOptionsPanel myExportOptionsPanel;

    @Override
    public boolean canExport(Class<?> target)
    {
        return target != null && File.class.isAssignableFrom(target)
                && getObjects().stream().allMatch(o -> o instanceof DataTypeInfo) && getObjects().stream().anyMatch(
                    o -> FeatureExporter.isExportable((DataTypeInfo)o) || TileExporter.isExportable((DataTypeInfo)o));
    }

    @Override
    public File export(File file) throws ExportException
    {
        File theFile = file;
        if (!GeoPackageValidate.hasGeoPackageExtension(theFile))
        {
            theFile = new File(theFile.toString() + "." + GeoPackageConstants.GEOPACKAGE_EXTENSION);
        }

        CancellableTaskActivity ta = CancellableTaskActivity.createActive("Exporting to geopackage file " + theFile);
        getToolbox().getUIRegistry().getMenuBarRegistry().addTaskActivity(ta);

        boolean exists = theFile.exists();
        if (!exists)
        {
            exists = GeoPackageManager.create(theFile);
        }

        if (exists)
        {
            ExportModel model = getExportModel(theFile);
            Collection<GeoPackageSubExporter> subExporters = getSubExporters(model);
            Collection<DataTypeInfo> typesWithoutData = updateModel(model, subExporters);
            if (proceed(typesWithoutData, model))
            {
                doExport(model, subExporters, ta);
            }
        }

        return theFile;
    }

    @Override
    public MimeType getMimeType()
    {
        return MimeType.GPKG;
    }

    @Override
    public Collection<? extends Component> getMenuItems(String contextId, Object key)
    {
        if (key instanceof GeometryContextKey)
        {
            GeometryContextKey geomKey = (GeometryContextKey)key;
            if (geomKey.getGeometry() instanceof PolygonGeometry)
            {
                PolygonGeometry polygon = (PolygonGeometry)geomKey.getGeometry();
                GeographicBoundingBox bbox = GeographicBoundingBox.getMinimumBoundingBox(
                        CollectionUtilities.filterDowncast(polygon.getVertices(), GeographicPosition.class));
                return Collections.singleton(SwingUtilities.newMenuItem(getMimeTypeString(), e -> export(bbox)));
            }
        }
        return null;
    }

    @Override
    public JComponent getFileChooserAccessory()
    {
        if (myBbox != null)
        {
            Collection<DataTypeInfo> dataTypes = getToolbox().getPluginToolboxRegistry().getPluginToolbox(MantleToolbox.class)
                    .getDataGroupController().findActiveMembers(TileExporter::isExportable);
            myExportOptionsPanel = new ExportOptionsPanel(dataTypes);
            return myExportOptionsPanel;
        }
        return null;
    }

    /**
     * Exports the given bounding box.
     *
     * @param bbox the bounding box
     */
    private void export(GeographicBoundingBox bbox)
    {
        myBbox = bbox;
        Toolbox toolbox = getToolbox();
        ExportUtilities.export(toolbox.getUIRegistry().getMainFrameProvider().get(), toolbox.getPreferencesRegistry(), this);
    }

    /**
     * Creates the export model from the file.
     *
     * @param file the file
     * @return the export model
     */
    private ExportModel getExportModel(File file)
    {
        ExportModel model = new ExportModel(file);
        if (myExportOptionsPanel != null)
        {
            setObjects(myExportOptionsPanel.getSelections());
            model.setMaxZoomLevel(myExportOptionsPanel.getMaxZoomLevel());
        }
        for (Object object : getObjects())
        {
            if (object instanceof DataTypeInfo)
            {
                model.getDataTypesToExport().add((DataTypeInfo)object);
            }
        }
        return model;
    }

    /**
     * Gets the sub exporters.
     *
     * @param model the export model
     * @return the sub exporters
     */
    private Collection<GeoPackageSubExporter> getSubExporters(ExportModel model)
    {
        Collection<GeoPackageSubExporter> subExporters = New.list(1);
        if (myBbox != null)
        {
            subExporters.add(new TileExporter(model, getToolbox().getGeometryRegistry(), myBbox));
        }
        else
        {
            MantleToolbox mantlebox = MantleToolboxUtils.getMantleToolbox(getToolbox());
            subExporters.add(new FeatureExporter(model.getDataTypesToExport(), mantlebox.getDataElementLookupUtils(),
                    mantlebox.getDataElementCache()));
        }
        return subExporters;
    }

    /**
     * Updates the model with data from the sub exporters.
     *
     * @param model the export model
     * @param subExporters the sub exporters
     * @return the types with out data
     */
    private Collection<DataTypeInfo> updateModel(ExportModel model, Collection<? extends GeoPackageSubExporter> subExporters)
    {
        int totalCount = subExporters.stream().mapToInt(e -> e.getRecordCount()).sum();
        Set<DataTypeInfo> dataTypesToExport = subExporters.stream().flatMap(e -> e.getExportableTypes().stream())
                .collect(Collectors.toSet());

        Collection<DataTypeInfo> typesWithoutData = CollectionUtilities.difference(model.getDataTypesToExport(),
                dataTypesToExport);

        model.setTotalCountToExport(totalCount);
        model.getDataTypesToExport().clear();
        model.getDataTypesToExport().addAll(dataTypesToExport);

        return typesWithoutData;
    }

    /**
     * Asks the user whether to proceed if some data types have no data.
     *
     * @param typesWithoutData the data types without data
     * @param model the export model
     * @return whether to proceed
     */
    private boolean proceed(Collection<? extends DataTypeInfo> typesWithoutData, ExportModel model)
    {
        boolean proceed = true;
        if (!typesWithoutData.isEmpty())
        {
            String title;
            String question;
            if (model.getDataTypesToExport().isEmpty())
            {
                title = "No Data To Export";
                question = "No data found for any layers. Proceed with export?";
            }
            else
            {
                title = "Confirm Export";
                question = new StringBuilder().append("Only ").append(model.getDataTypesToExport().size()).append(" of ")
                        .append(model.getDataTypesToExport().size() + typesWithoutData.size())
                        .append(" layers have data. Proceed with export?").toString();
            }
            proceed = new UserAskerImpl(getToolbox().getUIRegistry()).askYesNo(question, title);
        }
        return proceed;
    }

    /**
     * Does the export.
     *
     * @param model the export model
     * @param subExporters the sub exporters
     * @param ta the task activity
     * @throws ExportException if a problem occurs while exporting
     */
    private void doExport(ExportModel model, Collection<? extends GeoPackageSubExporter> subExporters, CancellableTaskActivity ta)
        throws ExportException
    {
        ProgressReporter progressReporter = new ProgressReporter(new ProgressModel(), model.getExportFile().toString(),
                model.getTotalCountToExport(), ta);
        model.setProgressReporter(progressReporter);

        GeoPackage geoPackage = GeoPackageManager.open(model.getExportFile());
        model.setGeoPackage(geoPackage);

        for (GeoPackageSubExporter subExporter : subExporters)
        {
            subExporter.export(model);
        }

        // Tile exports happen on background threads, so closing the reporter
        // happens in that code. If we just have features we can close it here.
        if (model.getDataTypesToExport().stream().noneMatch(TileExporter::isExportable))
        {
            progressReporter.close();
        }
    }
}
