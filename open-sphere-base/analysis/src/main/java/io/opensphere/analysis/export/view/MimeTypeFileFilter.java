package io.opensphere.analysis.export.view;

import java.io.File;

import javax.swing.filechooser.FileFilter;

import io.opensphere.core.util.MimeType;

/**
 * A file filter based on {@link MimeType}.
 */
public class MimeTypeFileFilter extends FileFilter
{
    /**
     * The {@link MimeType}.
     */
    private final MimeType myMimeType;

    /**
     * Constructs a new {@link MimeTypeFileFilter}.
     *
     * @param mimeType The {@link MimeType} to filter on.
     */
    public MimeTypeFileFilter(MimeType mimeType)
    {
        myMimeType = mimeType;
    }

    @Override
    public boolean accept(File f)
    {
        boolean accept = f.isDirectory();
        if (!accept)
        {
            for (String extension : myMimeType.getFileExtensions())
            {
                if (f.getName().endsWith("." + extension))
                {
                    accept = true;
                    break;
                }
            }
        }

        return accept;
    }

    @Override
    public String getDescription()
    {
        return myMimeType.getDescription();
    }
}
