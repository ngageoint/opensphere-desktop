package io.opensphere.analysis.heatmap;

import java.io.File;

import io.opensphere.analysis.heatmap.DataRegistryHelper.HeatmapImageInfo;
import io.opensphere.core.export.AbstractExporter;
import io.opensphere.core.export.ExportException;
import io.opensphere.core.util.MimeType;
import io.opensphere.core.util.filesystem.FileUtilities;
import io.opensphere.core.util.gdal.GdalIOUtilities;
import io.opensphere.core.util.taskactivity.TaskActivity;
import io.opensphere.mantle.data.DataTypeInfo;

/** An exporter that exports heat map layers. */
public class HeatmapExporter extends AbstractExporter
{
    /** The optional image info if known. */
    private volatile HeatmapImageInfo myImageInfo;

    @Override
    public boolean canExport(Class<?> target)
    {
        return target != null && File.class.isAssignableFrom(target) && getObjects().size() == 1 && getObjects().stream()
                .allMatch(o -> o instanceof DataTypeInfo && "Heatmap".equals(((DataTypeInfo)o).getSourcePrefix()));
    }

    @Override
    public File export(File file) throws ExportException
    {
        File theFile = file;
        if (!"tiff".equalsIgnoreCase(FileUtilities.getSuffix(theFile)))
        {
            theFile = new File(theFile + ".tiff");
        }

        try (TaskActivity ta = TaskActivity.createActive("Exporting heatmap to " + theFile))
        {
            getToolbox().getUIRegistry().getMenuBarRegistry().addTaskActivity(ta);

            HeatmapImageInfo imageInfo = myImageInfo;
            if (imageInfo == null)
            {
                String typeKey = getObjects().stream().map(o -> ((DataTypeInfo)o).getTypeKey()).findAny().orElse(null);
                if (typeKey != null)
                {
                    imageInfo = new DataRegistryHelper(getToolbox().getDataRegistry()).queryImage(typeKey);
                }
            }

            if (imageInfo != null)
            {
                GdalIOUtilities.exportTiff(imageInfo.getImage(), imageInfo.getBbox(), theFile);
            }
        }

        return theFile;
    }

    @Override
    public MimeType getMimeType()
    {
        return MimeType.GEOTIFF;
    }

    /**
     * Sets the imageInfo.
     *
     * @param imageInfo the imageInfo
     */
    public void setImageInfo(HeatmapImageInfo imageInfo)
    {
        myImageInfo = imageInfo;
    }
}
