package io.opensphere.core.export;

import java.awt.Component;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

import edu.umd.cs.findbugs.annotations.Nullable;
import javax.swing.JComponent;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import io.opensphere.core.Toolbox;
import io.opensphere.core.control.action.MenuOption;
import io.opensphere.core.util.XMLUtilities;
import io.opensphere.core.util.collections.New;

/**
 * Partial implementation of {@link Exporter}.
 */
public abstract class AbstractExporter implements Exporter
{
    /** The objects to be exported. */
    private volatile Collection<?> myObjects;

    /** The toolbox. */
    protected volatile Toolbox myToolbox;

    @Override
    public File export(Object target) throws IOException, ExportException
    {
        File file = null;
        if (target instanceof File)
        {
            file = export((File)target);
        }
        else if (target instanceof Node)
        {
            export((Node)target);
        }
        else
        {
            throw new UnsupportedOperationException();
        }
        return file;
    }

    /**
     * Export to a file.
     *
     * @param file The file.
     * @throws IOException If a problem occurs writing to the target.
     * @throws ExportException If a problem occurs during export.
     * @return the file exported to.
     */
    public File export(File file) throws IOException, ExportException
    {
        Document doc;
        try
        {
            doc = XMLUtilities.newDocument();
        }
        catch (ParserConfigurationException e)
        {
            throw new ExportException("Failed to create parent document: " + e, e);
        }

        export(doc);

        File outputFile = getExportFiles(file).iterator().next();

        if (outputFile.getParentFile() != null && !outputFile.getParentFile().exists() && !outputFile.getParentFile().mkdirs())
        {
            throw new ExportException("Unable to create directory: " + outputFile.getParentFile().getAbsolutePath());
        }

        XMLUtilities.format(doc, new FileOutputStream(outputFile), null);
        return outputFile;
    }

    /**
     * Export to a DOM node.
     *
     * @param node The node.
     * @throws ExportException If a problem occurs during export.
     */
    public void export(Node node) throws ExportException
    {
        throw new UnsupportedOperationException();
    }

    /**
     * Called before export happens. Default behavior returns true.
     */
    @Override
    public boolean preExport()
    {
        return true;
    }

    @Override
    public Collection<? extends File> getExportFiles(File file)
    {
        // Validate file extension (add if missing)
        String path = file.getAbsolutePath().toLowerCase();
        boolean found = false;
        String[] extensions = getMimeType().getFileExtensions();
        for (String extension : extensions)
        {
            if (path.endsWith("." + extension))
            {
                found = true;
                break;
            }
        }

        File result = found ? file : new File(file.getAbsolutePath() + "." + extensions[0]);

        return Collections.singleton(result);
    }

    @Override
    @Nullable
    public JComponent getFileChooserAccessory()
    {
        return null;
    }

    @Override
    public MenuOption getMenuOption()
    {
        String mimeType = getMimeTypeString();
        return new MenuOption(mimeType, mimeType, mimeType);
    }

    @Override
    public Collection<? extends Component> getMenuItems(String contextId, Object key)
    {
        return null;
    }

    @Override
    public String getMimeTypeString()
    {
        return getMimeType().toString();
    }

    /**
     * Get the objects to be exported.
     *
     * @return The objects.
     */
    public Collection<?> getObjects()
    {
        return myObjects;
    }

    /**
     * Get the toolbox.
     *
     * @return The toolbox.
     */
    public Toolbox getToolbox()
    {
        return myToolbox;
    }

    @Override
    public AbstractExporter setObjects(Collection<?> objects)
    {
        myObjects = New.unmodifiableCollection(objects);
        return this;
    }

    @Override
    public void setToolbox(Toolbox toolbox)
    {
        myToolbox = toolbox;
    }
}
