package io.opensphere.mantle.icon.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import javax.activation.MimetypesFileTypeMap;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.compressors.lzma.LZMACompressorInputStream;
import org.apache.commons.compress.compressors.xz.XZCompressorInputStream;
import org.apache.commons.compress.compressors.z.ZCompressorInputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.compress.utils.SeekableInMemoryByteChannel;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import io.opensphere.core.Toolbox;
import io.opensphere.core.dialog.alertviewer.event.UserMessageEvent;
import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.icon.IconProvider;
import io.opensphere.mantle.icon.IconRecord;
import io.opensphere.mantle.icon.IconSource;
import javafx.scene.Node;
import javafx.stage.FileChooser;

/**
 * An icon source used to read icons from a compressed source (ZIP, GZ, etc.).
 */
public class CompressedCollectionIconSource implements IconSource<FileIconSourceModel>
{
    /** The logger used to capture output from instances of this class. */
    private static final Logger LOG = Logger.getLogger(CompressedCollectionIconSource.class);

    /** The model in which the icon source maintains state. */
    private final FileIconSourceModel myModel = new FileIconSourceModel();

    /** A resolver used to find specific content types. */
    private static final MimetypesFileTypeMap MIME_TYPE_RESOLVER = new MimetypesFileTypeMap();

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.icon.IconSource#getName()
     */
    @Override
    public String getName()
    {
        return "icons from an archive file";
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.icon.IconSource#getIconProviders(Toolbox)
     */
    @Override
    public List<IconProvider> getIconProviders(Toolbox toolbox)
    {
        File file = myModel.fileProperty().get();
        List<IconProvider> results;
        try
        {
            results = processStream(file.getName(), file.toURI(), file.toURI().toURL().openStream());
        }
        catch (IOException e)
        {
            LOG.error("Unable to process archive.", e);
            UserMessageEvent.error(toolbox.getEventManager(),
                    "Unable to process archive from file '" + file.getAbsolutePath() + "'");
            results = Collections.emptyList();
        }

        return results;
    }

    /**
     * Processes the supplied stream, reading data from the compressed source.
     *
     * @param name the name of the source.
     * @param rootUri the root URIL of the source.
     * @param stream the stream from which to read data.
     * @return a list of icon providers generated from the supplied parameters.
     * @throws IOException if the icons cannot be read from the supplied stream.
     */
    private List<IconProvider> processStream(String name, URI rootUri, InputStream stream) throws IOException
    {
        List<IconProvider> results = New.list();
        String entryContentType = MIME_TYPE_RESOLVER.getContentType(name);
        switch (entryContentType)
        {
            case "application/zip":
            case "application/x-compressed":
            case "application/x-zip-compressed":
            case "multipart/x-zip":
                results.addAll(processZipArchive(name, rootUri, stream));
                break;
            case "application/gzip":
            case "application/x-bzip2":
            case "application/x-compress":
            case "application/x-lzma":
            case "application/x-xz":
                results.addAll(processCompressedArchive(entryContentType, name, rootUri, stream));
                break;
            case "application/x-tar":
            case "application/x-tar+gzip":
            case "application/x-tar+x-bzip2":
            case "application/x-tar+x-compress":
            case "application/x-tar+x-lzma":
            case "application/x-tar+x-xz":
                results.addAll(processTarArchive(entryContentType, name, rootUri, stream));
                break;
            case "image/jpeg":
            case "image/png":
            case "image/gif":
            case "image/x-icon":
            case "image/bmp":
                results.add(processImageStream(name, rootUri, stream));
                break;
            default:
                LOG.warn("Unrecognized file content type: '" + entryContentType + "'");
                break;
        }

        return results;
    }

    /**
     * Reads data from the supplied compressed archive stream, de-compressing it
     * and then reading all matching files from within.
     *
     * @param mimeType the MIME type of the content within the stream.
     * @param name the name of the source.
     * @param rootUri the root URI of the source.
     * @param stream the stream to read.
     * @return a {@link List} of {@link IconProvider}s read from the supplied
     *         stream
     * @throws IOException if the stream cannot be read.
     */
    private List<IconProvider> processCompressedArchive(String mimeType, String name, URI rootUri, InputStream stream)
        throws IOException
    {
        try (InputStream in = getCompressionStream(mimeType, stream))
        {
            if (in == null)
            {
                return Collections.emptyList();
            }
            String subname = name.substring(0, name.lastIndexOf('.'));
            return processStream(subname, rootUri, in);
        }
    }

    /**
     * Creates a compression stream using the supplied mime type and input
     * stream.
     *
     * @param mimeType the mime type of the stream.
     * @param stream the stream to wrap.
     * @return an input stream wrapping the supplied input stream to read
     *         compressed data.
     * @throws IOException if the stream cannot be read.
     */
    private InputStream getCompressionStream(String mimeType, InputStream stream) throws IOException
    {
        InputStream in;
        if (StringUtils.equals(mimeType, "application/gzip"))
        {
            in = new GzipCompressorInputStream(stream);
        }
        else if (StringUtils.equals(mimeType, "application/x-bzip2"))
        {
            in = new BZip2CompressorInputStream(stream);
        }
        else if (StringUtils.equals(mimeType, "application/x-compress"))
        {
            in = new ZCompressorInputStream(stream);
        }
        else if (StringUtils.equals(mimeType, "application/x-lzma"))
        {
            in = new LZMACompressorInputStream(stream);
        }
        else if (StringUtils.equals(mimeType, "application/x-xz"))
        {
            in = new XZCompressorInputStream(stream);
        }
        else
        {
            LOG.warn("Unrecognized compression type: '" + mimeType + "'");
            in = null;
        }
        return in;
    }

    /**
     * Creates a combined tar + compression stream using the supplied mime type
     * and input stream.
     *
     * @param mimeType the mime type of the stream.
     * @param stream the stream to wrap.
     * @return an input stream wrapping the supplied input stream to read
     *         compressed data.
     * @throws IOException if the stream cannot be read.
     */
    private InputStream getTarCompressionStream(String mimeType, InputStream stream) throws IOException
    {
        InputStream in;
        if (StringUtils.equals(mimeType, "application/x-tar"))
        {
            in = stream;
        }
        else if (StringUtils.equals(mimeType, "application/x-tar+gzip"))
        {
            in = new GzipCompressorInputStream(stream);
        }
        else if (StringUtils.equals(mimeType, "application/x-tar+x-bzip2"))
        {
            in = new BZip2CompressorInputStream(stream);
        }
        else if (StringUtils.equals(mimeType, "application/x-tar+x-compress"))
        {
            in = new ZCompressorInputStream(stream);
        }
        else if (StringUtils.equals(mimeType, "application/x-tar+x-lzma"))
        {
            in = new LZMACompressorInputStream(stream);
        }
        else if (StringUtils.equals(mimeType, "application/x-tar+x-xz"))
        {
            in = new XZCompressorInputStream(stream);
        }
        else
        {
            LOG.warn("Unrecognized tar compression type: '" + mimeType + "'");
            in = null;
        }
        return in;
    }

    /**
     * Reads the supplied input stream as a (potentially compressed) tar
     * archive.
     *
     * @param mimeType the MIME type of the content of the stream.
     * @param name the name of the data source.
     * @param rootUri the root URI of the data source.
     * @param stream the stream to read.
     * @return a {@link List} of {@link IconProvider} instances generated from
     *         the supplied stream.
     * @throws IOException if the stream cannot be read.
     */
    private List<IconProvider> processTarArchive(String mimeType, String name, URI rootUri, InputStream stream) throws IOException
    {
        try (InputStream in = getTarCompressionStream(mimeType, stream))
        {
            if (in == null)
            {
                return Collections.emptyList();
            }
            List<IconProvider> results = New.list();
            try (TarArchiveInputStream tarIn = new TarArchiveInputStream(in))
            {
                TarArchiveEntry entry;
                while ((entry = tarIn.getNextTarEntry()) != null)
                {
                    if (!tarIn.canReadEntryData(entry))
                    {
                        continue;
                    }
                    if (!entry.isDirectory())
                    {
                        results.addAll(processStream(entry.getName(), rootUri, tarIn));
                    }
                }
            }
            return results;
        }
    }

    /**
     * Processes the the supplied stream to read an image from it, and generates
     * an {@link IconProvider} using the contents.
     *
     * @param name the name of the source from which to generate the provider.
     * @param rootUri the root URI with which to create the provider.
     * @param stream the stream to read for the icon image data.
     * @return an {@link IconProvider} generated from the contents of the
     *         supplied stream.
     * @throws IOException if the data from the stream cannot be read, or if a
     *             URI cannot be created.
     */
    private IconProvider processImageStream(String name, URI rootUri, InputStream stream) throws IOException
    {
        String currentFragment = rootUri.getFragment();
        if (StringUtils.isBlank(currentFragment))
        {
            currentFragment = "";
        }
        byte[] contents = new byte[stream.available()];
        IOUtils.readFully(stream, contents);

        currentFragment += name;
        URI path;
        try
        {
            path = new URI(rootUri.getScheme(), rootUri.getUserInfo(), rootUri.getHost(), rootUri.getPort(), rootUri.getPath(),
                    rootUri.getQuery(), currentFragment);
        }
        catch (URISyntaxException e)
        {
            throw new IOException("Unable to construct path", e);
        }
        IconProvider provider = new MemoryCachedIconProvider(path.toURL(), contents, IconRecord.USER_ADDED_COLLECTION, null);

        return provider;
    }

    /**
     * Processes the supplied input stream as a ZIP archive.
     *
     * @param name the name of the data source to process.
     * @param rootUri the root URI of the data source to process.
     * @param stream the stream from which to read data.
     * @return a {@link List} of {@link IconProvider}s generated from the
     *         supplied input stream.
     * @throws IOException if the stream cannot be read.
     */
    private List<IconProvider> processZipArchive(String name, URI rootUri, InputStream stream) throws IOException
    {
        String currentFragment = rootUri.getFragment();
        if (StringUtils.isBlank(currentFragment))
        {
            currentFragment = "";
        }

        List<IconProvider> results = New.list();
        try (ZipFile zipFile = new ZipFile(new SeekableInMemoryByteChannel(IOUtils.toByteArray(stream))))
        {
            Enumeration<ZipArchiveEntry> entries = zipFile.getEntries();
            while (entries.hasMoreElements())
            {
                ZipArchiveEntry element = entries.nextElement();
                if (!element.isDirectory())
                {
                    results.addAll(processStream(element.getName(), rootUri, zipFile.getInputStream(element)));
                }
            }
        }
        return results;
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
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters()
                .addAll(new FileChooser.ExtensionFilter("All Archives", "*.zip", "*.ZIP", "*.tar", "*.tar.gz", "*.tar.Z",
                        "*.tar.bz2", "*.tgz", "*.tar.lzma", "*.tar.xz", "*.TAR", "*.TAR.GZ", "*.TAR.Z", "*.TAR.BZ2", "*.TGZ",
                        "*.jar", "*.JAR", "*.TAR.LZMA", "*.TAR.XZ"), new FileChooser.ExtensionFilter("All Files", "*.*"),
                        new FileChooser.ExtensionFilter("ZIP", "*.zip", "*.ZIP"),
                        new FileChooser.ExtensionFilter("TAR", "*.tar", "*.tar.gz", "*.tar.Z", "*.tar.bz2", "*.tgz", "*.TAR",
                                "*.TAR.GZ", "*.TAR.Z", "*.TAR.BZ2", "*.TGZ", "*.TAR.LZMA", "*.TAR.XZ"),
                        new FileChooser.ExtensionFilter("JAR", "*.jar", "*.JAR"));
        chooser.setTitle("Select an Archive containing Icons");
        chooser.setInitialDirectory(new File(System.getProperty("user.home")));
        File file = chooser.showOpenDialog(parent.getScene().getWindow());
        if (file != null)
        {
            myModel.fileProperty().set(file);
            return true;
        }
        return false;
    }
}
