package io.opensphere.server.permalink;

import java.awt.Component;
import java.awt.EventQueue;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.swing.JFileChooser;

import org.apache.log4j.Logger;

import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.core.util.filesystem.MnemonicFileChooser;
import io.opensphere.core.util.lang.HappyCallable;
import io.opensphere.core.util.swing.EventQueueUtilities;
import io.opensphere.server.toolbox.FilePayload;
import io.opensphere.server.toolbox.SimpleFilePayload;

/**
 * Uploads a user-chosen file to the server and provides the URL to access the
 * file on the server.
 */
public class FileSelectorUploader
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(FileSelectorUploader.class);

    /** The preferences, used to remember the file chooser directory. */
    private final PreferencesRegistry myPrefsRegistry;

    /** The server to use for uploading the file. */
    private final PermalinkHttpServer myServer;

    /**
     * Constructor.
     *
     * @param server The server to use for uploading the file.
     * @param prefsRegistry The preferences, used to remember the file chooser
     *            directory.
     */
    public FileSelectorUploader(PermalinkHttpServer server, PreferencesRegistry prefsRegistry)
    {
        myServer = server;
        myPrefsRegistry = prefsRegistry;
    }

    /**
     * Prompt the user for a file, upload the file to the server, and send the
     * URL to the consumer.
     *
     * @param parentSupplier Supplier for the parent component to be used for
     *            dialogs.
     * @param urlConsumer The consumer for the URL.
     */
    public void selectAndUpload(Supplier<? extends Component> parentSupplier, Consumer<String> urlConsumer)
    {
        EventQueueUtilities.runOnEDT(() ->
        {
            assert EventQueue.isDispatchThread();

            MnemonicFileChooser chooser = new MnemonicFileChooser(myPrefsRegistry, null);
            if (chooser.showOpenDialog(parentSupplier.get()) != JFileChooser.APPROVE_OPTION)
            {
                return;
            }

            upload(chooser.getSelectedFile(), parentSupplier, urlConsumer);
        });
    }

    /**
     * Upload the file to the server, and send the URL to the consumer.
     *
     * @param file The file to be uploaded.
     * @param parentSupplier Supplier for the parent component to be used for
     *            dialogs.
     * @param urlConsumer The consumer for the URL.
     */
    public void upload(File file, Supplier<? extends Component> parentSupplier, Consumer<String> urlConsumer)
    {
        EventQueueUtilities.runOnEDT(() ->
        {
            EventQueueUtilities.runInBackgroundAndReturnResult(parentSupplier.get(),
                    (HappyCallable<String>)() -> uploadFile(new SimpleFilePayload(file)), urlConsumer);
        });
    }

    /**
     * Upload the file to the server.
     *
     * @param payload The file data to upload to the server.
     * @return The URL pointing to the uploaded file.
     */
    protected String uploadFile(FilePayload payload)
    {
        try
        {
            return myServer.uploadFile(payload);
        }
        catch (IOException | URISyntaxException e)
        {
            LOGGER.error("Failed to upload file [" + payload.getFile() + "]: " + e, e);
            return null;
        }
    }
}
