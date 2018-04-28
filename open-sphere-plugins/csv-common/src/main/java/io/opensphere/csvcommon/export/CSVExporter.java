package io.opensphere.csvcommon.export;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.function.Function;

import org.apache.commons.text.StringEscapeUtils;
import org.apache.log4j.Logger;

import io.opensphere.core.export.AbstractExporter;
import io.opensphere.core.util.MimeType;
import io.opensphere.core.util.collections.StreamUtilities;
import io.opensphere.core.util.lang.StringUtilities;

/** A generic CSV exporter that exports a collection of lists. */
public class CSVExporter extends AbstractExporter
{
    /** Used to log messages. */
    private static final Logger LOGGER = Logger.getLogger(CSVExporter.class);

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
        return target != null && File.class.isAssignableFrom(target) && getObjects().stream().allMatch(o -> o instanceof List);
    }

    @Override
    public File export(File file) throws IOException
    {
        try (FileOutputStream out = new FileOutputStream(getExportFiles(file).iterator().next()))
        {
            // Note:  On Windows, files encoded in UTF-8 that do not have the
            // "Byte Order Mark" may not be interpreted correctly by native
            // applications (Excel and LibreOffice are known examples).
            // Therefore, we include the BOM even though standard pratice in
            // Java is to omit it.
            if (StringUtilities.DEFAULT_CHARSET.name().equals(UTF8_NAME))
            {
                // "Byte Order Mark" for UTF-8:  EF BB BF
                out.write(0xef);
                out.write(0xbb);
                out.write(0xbf);
            }
            BufferedWriter w = new BufferedWriter(new OutputStreamWriter(out, StringUtilities.DEFAULT_CHARSET.newEncoder()));
            for (Object obj : getObjects())
            {
                w.write(StringUtilities.join(",", StreamUtilities.map((List<?>)obj, FORMAT_CELL)));
                w.newLine();
            }
            w.flush();
        }

        return file;
    }

    @Override
    public MimeType getMimeType()
    {
        return MimeType.CSV;
    }
}
