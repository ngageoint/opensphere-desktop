package io.opensphere.core.util.io;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

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
import org.apache.commons.compress.utils.SeekableInMemoryByteChannel;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import io.opensphere.core.util.net.UrlUtilities;

/**
 * Utilities used to read data from {@link InputStream}s, specifically handling
 * archives and inner files.
 */
public final class IOUtilities
{
    /** The logger used to capture output from instances of this class. */
    private static final Logger LOG = Logger.getLogger(IOUtilities.class);

    /**
     * A mime type resolver instance used to help find known MIME types for
     * files.
     */
    private static final MimetypesFileTypeMap MIME_TYPE_RESOLVER = new MimetypesFileTypeMap();

    /** Private constructor to prevent instantiation. */
    private IOUtilities()
    {
        throw new UnsupportedOperationException("Instantiation of utility classes is not permitted.");
    }

    /**
     * Gets the input stream for the supplied URL. This method allows for
     * callers to handle a URL referencing an item stored within an archive
     * (e.g.: a file stored within a ZIP archive), by setting the path to the
     * archive, and the fragment to the sub-path within the archive. For
     * example, if the name of the archive is foo.zip stored in /opt, and the
     * file within the archive is blah/bar.png, then the URL would be
     * file:///opt/foo.zip#blah/bar.png. If no fragment is specified, then a
     * standard input stream is returned.
     *
     * @param url The URL for which to get the input stream.
     * @return an input stream through which the requested resource may be read.
     * @throws IOException if the URL stream could not be opened
     */
    public static InputStream getInputStream(URL url) throws IOException
    {
        InputStream stream = null;

        if (UrlUtilities.isFragmentPresent(url))
        {
            // a fragment on a file URL indicates that the URL is probably
            // pointing to a file within a local archive:
            URLConnection connection = url.openConnection();
            connection.connect();
            String path = url.getPath();
            String contentType = MIME_TYPE_RESOLVER.getContentType(path);
            String fragment = UrlUtilities.getFragment(url);

            switch (contentType)
            {
                case "application/zip":
                    try (ZipFile zipFile = new ZipFile(
                            new SeekableInMemoryByteChannel(IOUtils.toByteArray(connection.getInputStream()))))
                    {
                        ZipArchiveEntry entry = zipFile.getEntry(fragment);
                        if (entry != null)
                        {
                            // copy the stream to a new byte array input
                            // stream, to avoid holding open the zip file:
                            stream = new ByteArrayInputStream(IOUtils.toByteArray(zipFile.getInputStream(entry)));
                        }
                    }
                    break;
                case "application/gzip":
                    try (InputStream source = new GzipCompressorInputStream(connection.getInputStream()))
                    {
                        String innerContentType = MIME_TYPE_RESOLVER.getContentType(path.substring(0, path.lastIndexOf('.')));
                        stream = getArchiveComponentInputStream(innerContentType, source, fragment);
                    }
                    break;
                case "application/x-bzip2":
                    try (InputStream source = new BZip2CompressorInputStream(connection.getInputStream()))
                    {
                        String innerContentType = MIME_TYPE_RESOLVER.getContentType(path.substring(0, path.lastIndexOf('.')));
                        stream = getArchiveComponentInputStream(innerContentType, source, fragment);
                    }
                    break;
                case "application/x-compress":
                    try (InputStream source = new ZCompressorInputStream(connection.getInputStream()))
                    {
                        String innerContentType = MIME_TYPE_RESOLVER.getContentType(path.substring(0, path.lastIndexOf('.')));
                        stream = getArchiveComponentInputStream(innerContentType, source, fragment);
                    }
                    break;
                case "application/x-lzma":
                    try (InputStream source = new LZMACompressorInputStream(connection.getInputStream()))
                    {
                        String innerContentType = MIME_TYPE_RESOLVER.getContentType(path.substring(0, path.lastIndexOf('.')));
                        stream = getArchiveComponentInputStream(innerContentType, source, fragment);
                    }
                    break;
                case "application/x-xz":
                    try (InputStream source = new XZCompressorInputStream(connection.getInputStream()))
                    {
                        String innerContentType = MIME_TYPE_RESOLVER.getContentType(path.substring(0, path.lastIndexOf('.')));
                        stream = getArchiveComponentInputStream(innerContentType, source, fragment);
                    }
                    break;
                default:
            }
        }
        else
        {
            stream = url.openStream();
        }

        return stream;
    }

    /**
     * Gets an input stream for the named file within the supplied input stream.
     * The supplied input stream is read according to the supplied content type.
     * Note that this method does not support compressed types, the compression
     * should be resolved before calling this method (e.g. in case of a .tar.gz
     * file, the original file input stream should be wrapped in a
     * {@link GzipCompressorInputStream}, and the content type supplied as
     * application/x-tar.
     *
     * @param archiveContentType the content type of the supplied stream (only
     *            "application/x-tar" and "application/zip" are supported at
     *            this time).
     * @param inputStream the stream with which the archive is read.
     * @param innerFile the name of the entry within the archive stream to
     *            extract.
     * @return an input stream containing the data from the inner file,
     *         extracted from the supplied input stream.
     * @throws IOException if data cannot be read from the supplied input
     *             stream.
     */
    private static InputStream getArchiveComponentInputStream(String archiveContentType, InputStream inputStream,
            String innerFile)
        throws IOException
    {
        InputStream stream = null;
        switch (archiveContentType)
        {
            case "application/x-tar":
                TarArchiveInputStream tarIn = new TarArchiveInputStream(inputStream);
                TarArchiveEntry tarEntry;
                while ((tarEntry = tarIn.getNextTarEntry()) != null)
                {
                    if (!tarIn.canReadEntryData(tarEntry))
                    {
                        continue;
                    }
                    if (!tarEntry.isDirectory() && tarEntry.getName().equals(innerFile))
                    {
                        stream = new ByteArrayInputStream(IOUtils.toByteArray(tarIn));
                        break;
                    }
                }
                break;
            case "application/zip":
                try (ZipFile zipFile = new ZipFile(new SeekableInMemoryByteChannel(IOUtils.toByteArray(inputStream))))
                {
                    ZipArchiveEntry entry = zipFile.getEntry(innerFile);
                    if (entry != null)
                    {
                        // copy the stream to a new byte array input
                        // stream, to avoid holding open the zip file:
                        stream = new ByteArrayInputStream(IOUtils.toByteArray(zipFile.getInputStream(entry)));
                    }
                }
                break;
            default:
                break;
        }

        return stream;
    }
}
