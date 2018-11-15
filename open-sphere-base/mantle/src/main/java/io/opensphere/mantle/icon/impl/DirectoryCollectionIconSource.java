package io.opensphere.mantle.icon.impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import javax.activation.MimetypesFileTypeMap;

import org.apache.log4j.Logger;

import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.icon.IconProvider;
import io.opensphere.mantle.icon.IconRecord;
import io.opensphere.mantle.icon.IconSource;
import javafx.scene.Node;
import javafx.stage.DirectoryChooser;

/**
 *
 */
public class DirectoryCollectionIconSource implements IconSource<FileIconSourceModel>
{
    /** The logger used to capture output from instances of this class. */
    private static final Logger LOG = Logger.getLogger(DirectoryCollectionIconSource.class);

    /** The model in which the icon source maintains state. */
    private final FileIconSourceModel myModel = new FileIconSourceModel();

    private static final MimetypesFileTypeMap MIME_TYPE_RESOLVER = new MimetypesFileTypeMap();

    private static final Set<String> IMAGE_MIME_TYPES = Set.of("image/jpeg", "image/png", "image/gif", "image/x-icon",
            "image/bmp");

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.icon.IconSource#getName()
     */
    @Override
    public String getName()
    {
        return "icons from a directory";
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.icon.IconSource#getIconProviders()
     */
    @Override
    public List<IconProvider> getIconProviders()
    {
        File directory = myModel.fileProperty().get();

        List<IconProvider> results = getIconProviders(directory.toPath());

        return results;
    }

    private List<IconProvider> getIconProviders(Path path)
    {
        List<IconProvider> results = New.list();

        if (Files.isDirectory(path))
        {
            try
            {
                Files.list(path).forEach(f -> results.addAll(getIconProviders(f)));
            }
            catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        else
        {
            File file = path.toFile();
            if (IMAGE_MIME_TYPES.contains(MIME_TYPE_RESOLVER.getContentType(file)))
            {
                try
                {
                    results.add(getIconProvider(file));
                }
                catch (IOException e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            else if (LOG.isDebugEnabled())
            {
                LOG.debug(file.getAbsolutePath() + " skipped due to non-matching MIME type ("
                        + MIME_TYPE_RESOLVER.getContentType(file) + ")");
            }
        }

        return results;
    }

    private IconProvider getIconProvider(File file) throws IOException
    {
        // assumption: this is only called with valid image files
        IconProvider provider = new DefaultIconProvider(file.toURI().toURL(), IconRecord.USER_ADDED_COLLECTION, null, null);

        return provider;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.icon.IconSource#getModel()
     */
    @Override
    public FileIconSourceModel getModel()
    {
        return myModel;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.icon.IconSource#getUserInput(Node)
     */
    @Override
    public void getUserInput(Node parent)
    {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Select an Directory containing Icons");
        chooser.setInitialDirectory(new File(System.getProperty("user.home")));
        File file = chooser.showDialog(parent.getScene().getWindow());
        if (file != null)
        {
            myModel.fileProperty().set(file);
        }
    }
}
