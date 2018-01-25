package io.opensphere.kml.export;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collection;

import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.element.DataElement;

/**
 * The Interface KMLExporter.
 */
public interface KMLExporter
{
    /**
     * Generate kml document.
     *
     * @param dti the dti
     * @param points the points
     * @param columnNames the column names
     * @param dateFormat the meta data date format
     * @param preExportModel the pre export options model
     */
    void generateKMLDocument(DataTypeInfo dti, Collection<? extends DataElement> points, Collection<String> columnNames,
            SimpleDateFormat dateFormat, KMLExportOptionsModel preExportModel);

    /**
     * Write kml to file.
     *
     * @param outputFile the output file
     * @return the actual file written to
     * @throws IOException Signals that an I/O exception has occurred.
     */
    File writeKMLToFile(File outputFile) throws IOException;
}
