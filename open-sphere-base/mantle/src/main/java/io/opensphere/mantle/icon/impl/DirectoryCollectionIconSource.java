package io.opensphere.mantle.icon.impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import javax.activation.MimetypesFileTypeMap;

import org.apache.log4j.Logger;

import io.opensphere.core.Toolbox;
import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.icon.IconProvider;
import io.opensphere.mantle.icon.IconRecord;
import io.opensphere.mantle.icon.IconSource;
import javafx.scene.Node;
import javafx.stage.DirectoryChooser;

/** An icon source used to generate icons from a directory. */
public class DirectoryCollectionIconSource implements IconSource<FileIconSourceModel>
{
    /** The logger used to capture output from instances of this class. */
    private static final Logger LOG = Logger.getLogger(DirectoryCollectionIconSource.class);

    /** The model in which the icon source maintains state. */
    private final FileIconSourceModel myModel = new FileIconSourceModel();

    /** A resolver used to find specific content types. */
    private static final MimetypesFileTypeMap MIME_TYPE_RESOLVER = new MimetypesFileTypeMap();

    /** THe set of image types handled by this source. */
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
     * @see io.opensphere.mantle.icon.IconSource#getIconProviders(Toolbox)
     */
    @Override
    public List<IconProvider> getIconProviders(Toolbox toolbox)
    {
        return getIconProviders(myModel.fileProperty().get().toPath());
    }

    /**
     * Gets the set of icon providers from the path. This method recursively
     * examines all sub-directories of the supplied path.
     *
     * @param path the path to examine.
     * @return a list of icon providers generated from the recursive examination
     *         of the supplied path.
     */
    private List<IconProvider> getIconProviders(Path path)
    {
        List<IconProvider> results = New.list();

        if (Files.isDirectory(path))
        {
            try (Stream<Path> list = Files.list(path))
            {
                list.forEach(f -> results.addAll(getIconProviders(f)));
            }
            catch (IOException e)
            {
                LOG.error("Unable to read '" + path.toString() + "' while searching for icons.", e);
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
                    LOG.error("Unable to read '" + file.toString() + "' while searching for icons.", e);
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

    /**
     * Gets the icon provider for the supplied file. It is assumed that this
     * method is only called with a valid image file reference (which is why it
     * is private).
     *
     * @param file the file for which to get the icon provider.
     * @return an IconProvider to reference the supplied file.
     * @throws IOException if the file cannot be read.
     */
    private IconProvider getIconProvider(File file) throws IOException
    {
        // assumption: this is only called with valid image files
        return new DefaultIconProvider(file.toURI().toURL(), IconRecord.USER_ADDED_COLLECTION, null);
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
    public boolean getUserInput(Node parent)
    {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Select an Directory containing Icons");
        chooser.setInitialDirectory(new File(System.getProperty("user.home")));
        File file = chooser.showDialog(parent.getScene().getWindow());
        if (file != null)
        {
            myModel.fileProperty().set(file);
            return true;
        }
        return false;
    }
}
