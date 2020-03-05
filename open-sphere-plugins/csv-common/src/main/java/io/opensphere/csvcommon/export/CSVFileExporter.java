package io.opensphere.csvcommon.export;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import javax.swing.JOptionPane;

import org.apache.commons.text.StringEscapeUtils;
import org.apache.log4j.Logger;

import io.opensphere.core.export.AbstractExporter;
import io.opensphere.core.preferences.ListToolPreferences;
import io.opensphere.core.util.MimeType;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.collections.StreamUtilities;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.core.util.swing.EventQueueUtilities;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.element.DataElement;
import io.opensphere.mantle.util.MantleToolboxUtils;

/**
 * An exporter that exports a layer to a CSV file.
 */
public class CSVFileExporter extends AbstractExporter
{
    /** The logger. */
    private static final Logger LOGGER = Logger.getLogger(CSVFileExporter.class);

    /** Name of the UTF-8 Charset. */
    private static final String UTF8_NAME = "UTF-8";

    /** Function to format a cell. */
    private static final Function<Object, String> FORMAT_CELL = new Function<Object, String>()
    {
        @Override
        public String apply(Object cell)
        {
            String translate = "";
            if (cell != null)
            {
                try
                {
                    translate = StringEscapeUtils.escapeCsv(cell.toString());
                }
                catch (StringIndexOutOfBoundsException e)
                {
                    LOGGER.error(e, e);
                }
            }

            return translate;
        }
    };

    @Override
    public boolean canExport(Class<?> target)
    {
        return target != null && File.class.isAssignableFrom(target)
                && getObjects().stream().allMatch(o -> o instanceof DataTypeInfo);
    }

    @Override
    public File export(File file) throws IOException
    {
        try (FileOutputStream out = new FileOutputStream(getExportFiles(file).iterator().next()))
        {
            // Note: On Windows, files encoded in UTF-8 that do not have the
            // "Byte Order Mark" may not be interpreted correctly by native
            // applications (Excel and LibreOffice are known examples).
            // Therefore, we include the BOM even though standard practice in
            // Java is to omit it.
            if (StringUtilities.DEFAULT_CHARSET.name().equals(UTF8_NAME))
            {
                // "Byte Order Mark" for UTF-8: EF BB BF
                out.write(0xef);
                out.write(0xbb);
                out.write(0xbf);
            }
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(out, StringUtilities.DEFAULT_CHARSET.newEncoder()));
            DataTypeInfo dataType = getElements().iterator().next();
            List<DataElement> dataElements = MantleToolboxUtils.getDataElementLookupUtils(getToolbox())
                    .getDataElements(dataType);
            List<String> keyList = dataElements.iterator().next().getMetaData().getKeys();
            int timeIndex = dataType.getMetaDataInfo().getKeyIndex(dataType.getMetaDataInfo().getTimeKey());
            int timePrecision = getToolbox().getPreferencesRegistry().getPreferences(ListToolPreferences.class)
                    .getInt(ListToolPreferences.LIST_TOOL_TIME_PRECISION_DIGITS, 0);
            SimpleDateFormat dateFormatter = ListToolPreferences.getSimpleDateFormatForPrecision(timePrecision);

            writer.write(StringUtilities.join(",",
                    StreamUtilities.map(keyList, FORMAT_CELL)));
            writer.newLine();
            for (DataElement element : dataElements)
            {
                List<Object> values = New.list(element.getMetaData().getValues());
                if (timeIndex != -1)
                {
                    values.set(timeIndex, dateFormatter.format(values.get(timeIndex)));
                }

                writer.write(StringUtilities.join(",", StreamUtilities.map(values, FORMAT_CELL)));
                writer.newLine();
            }

            writer.flush();
        }

		EventQueueUtilities.runOnEDT(() -> JOptionPane.showMessageDialog(getToolbox().getUIRegistry().getMainFrameProvider().get(),
                "Successfully saved file:\n" + file.getAbsolutePath(), "Saved CSV File", JOptionPane.INFORMATION_MESSAGE));

        return file;
    }

    @Override
    public MimeType getMimeType()
    {
        return MimeType.CSV;
    }

    /**
     * Gets the stored DataTypeInfo for the layer.
     *
     * @return the DataTypeInfo
     */
    private Collection<DataTypeInfo> getElements()
    {
        return CollectionUtilities.filterDowncast(getObjects(), DataTypeInfo.class);
    }
}
