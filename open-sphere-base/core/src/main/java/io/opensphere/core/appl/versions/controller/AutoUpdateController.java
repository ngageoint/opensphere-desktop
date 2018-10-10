package io.opensphere.core.appl.versions.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.stream.Collectors;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import io.opensphere.core.Toolbox;
import io.opensphere.core.appl.versions.AutoUpdateToolbox;
import io.opensphere.core.appl.versions.AutoUpdateToolboxUtils;
import io.opensphere.core.appl.versions.DescriptorUtils;
import io.opensphere.core.appl.versions.FileDescriptor;
import io.opensphere.core.appl.versions.InstallDescriptor;
import io.opensphere.core.appl.versions.VersionComparator;
import io.opensphere.core.appl.versions.model.AutoUpdatePreferences;
import io.opensphere.core.appl.versions.view.AutoUpdateDialog;
import io.opensphere.core.util.Service;
import io.opensphere.core.util.io.StreamReader;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.core.util.lang.ThreadUtilities;
import io.opensphere.core.util.net.UrlUtilities;
import io.opensphere.core.util.swing.EventQueueUtilities;
import io.opensphere.core.util.taskactivity.CancellableTaskActivity;
import io.opensphere.core.util.taskactivity.TaskActivity;

/**
 * The controller used to manage state in auto-update transactions.
 */
public class AutoUpdateController implements Service
{
    /** The {@link Logger} instance used to capture output. */
    private static final Logger LOG = Logger.getLogger(AutoUpdateController.class);

    /** Number of times a file should try to be downloaded. */
    private static final int DEFAULT_DOWNLOAD_ATTEMPTS = 2;

    /** A comparator used to examine two versions. */
    private static final VersionComparator VERSION_COMPARATOR = new VersionComparator();

    /** The model in which preferences are stored. */
    private AutoUpdatePreferences myModel;

    /** The toolbox through which auto-update state is accessed. */
    private AutoUpdateToolbox myAutoUpdateToolbox;

    /** The toolbox through which auto-update state is accessed. */
    private final Toolbox myToolbox;

    /**
     * Creates a new auto update controller, initialized with the supplied
     * toolbox.
     *
     * @param toolbox the toolbox through which application state is accessed.
     */
    public AutoUpdateController(Toolbox toolbox)
    {
        super();
        myToolbox = toolbox;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.util.Service#open()
     */
    @Override
    public void open()
    {
        myAutoUpdateToolbox = AutoUpdateToolboxUtils.getAutoUpdateToolboxToolbox(myToolbox);
        myModel = myAutoUpdateToolbox.getPreferences();
        Properties properties = loadLaunchConfiguration();
        myModel.setNewestLocalVersion(properties.getProperty("newest.version"));
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.util.Service#close()
     */
    @Override
    public void close()
    {
        /* intentionally blank */
    }

    /**
     * Checks the configured remote system for updates, and if a newer version
     * is available, prompts the user to download and install the latest
     * version.
     *
     * @param notifyUpToDate a flag used to allow the check to notify the user
     *            that the application is already up to date (true will notify,
     *            false will complete silently).
     */
    public void checkForUpdates(boolean notifyUpToDate)
    {
        ThreadUtilities.runBackground(() ->
        {
            if (myModel.isAutoUpdateEnabled())
            {
                if (isNewVersionAvailable())
                {
                    EventQueueUtilities.runOnEDT(() -> promptAndUpdate());
                }
                else if (notifyUpToDate)
                {
                    EventQueueUtilities.runOnEDT(() ->
                    {
                        final JFrame parent = myToolbox.getUIRegistry().getMainFrameProvider().get();
                        JOptionPane.showMessageDialog(parent,
                                "The installed version of the application is the most current available!",
                                "Application up to date", JOptionPane.INFORMATION_MESSAGE);
                    });
                }
            }
        });
    }

    /**
     * Tests to determine if a newer version is available on the remote system.
     *
     * @return true if the remote system contains a version newer than the
     *         latest local version, false otherwise.
     */
    private boolean isNewVersionAvailable()
    {
        String localNewestVersion = myModel.getNewestLocalVersion();
        if (StringUtils.isNotBlank(localNewestVersion))
        {
            LOG.info("Newest installed version: " + localNewestVersion);
            updateNewestRemoteVersion();

            String remoteNewestVersion = myModel.getNewestRemoteVersion();
            return VERSION_COMPARATOR.compare(localNewestVersion, remoteNewestVersion) < 0;
        }
        LOG.warn("Unable to determine local version, skipping auto-update.");
        return false;
    }

    /**
     * Shows the update dialog.
     */
    private void promptAndUpdate()
    {
        final JFrame parent = myToolbox.getUIRegistry().getMainFrameProvider().get();

        LOG.info("Prompting the user to determine if a new version should be downloaded.");
        String newestRemoteVersion = myModel.getNewestRemoteVersion();
        if (AutoUpdateDialog.showConfirmDialog(myToolbox.getPreferencesRegistry(), parent, newestRemoteVersion) < 2)
        {
            LOG.info("Downloading new version.");

            ThreadUtilities.runBackground(() ->
            {
                String updateUrlString = AutoUpdateUtils.getUrlString(myModel.getAutoUpdateProtocol(),
                        myModel.getAutoUpdateHostname(), myModel.getUpdateUrl() + "install_descriptor.json");

                updateUrlString = AutoUpdateUtils.substituteUrl(updateUrlString, newestRemoteVersion);

                URL updateURL = UrlUtilities.toURL(updateUrlString);
                String installConfirmation = installUpdateFiles(updateURL)
                        ? "Successfully installed version " + newestRemoteVersion
                                : "Failed to install version " + newestRemoteVersion;
                LOG.info(installConfirmation);
            });
        }
    }

    /** Gets the new version from the server. */
    private void updateNewestRemoteVersion()
    {
        String urlString = AutoUpdateUtils.getUrlString(myModel.getAutoUpdateProtocol(), myModel.getAutoUpdateHostname(),
                myModel.getLatestVersionUrl());
        LOG.info("Checking remote endpoint '" + urlString + "' for new version.");
        URL newVersionURL = UrlUtilities.toURL(urlString);

        try (InputStream result = AutoUpdateUtils.performRequest(newVersionURL, myToolbox))
        {
            if (result != null)
            {
                String version = new StreamReader(result).readStreamIntoString(StringUtilities.DEFAULT_CHARSET).trim();
                LOG.info("Remote endpoint's newest version is '" + version + "'");
                myModel.setNewestRemoteVersion(version);
            }
            else
            {
                LOG.error("Unable to perform request for remote version.");
            }
        }
        catch (IOException e)
        {
            LOG.error("Unable to perform request for remote version.", e);
        }
    }

    /**
     * Copies or downloads all files listed in the update's install descriptor.
     *
     * @param installDescriptorURL the URL of the install descriptor for the
     *            update
     * @return boolean value if all files were installed properly
     */
    public boolean installUpdateFiles(URL installDescriptorURL)
    {
        InstallDescriptor installDescriptor = AutoUpdateUtils.getUpdateInstallDescriptor(installDescriptorURL);
        try (CancellableTaskActivity ta = CancellableTaskActivity.createActive("Downloading update"))
        {
            String installVersion = myModel.getNewestRemoteVersion();
            String installDescriptorPath = Paths
                    .get(myModel.getInstallDirectory().getAbsolutePath(), installVersion, "install_descriptor.json").toString();

            downloadUpdateFile(installDescriptorURL, installDescriptorPath);
            InstallProgressTracker installTracker = new InstallProgressTracker();

            LOG.info("Downloading / installing new version from remote server: " + installVersion);
            myToolbox.getUIRegistry().getMenuBarRegistry().addTaskActivity(ta);
            // get the newest version files on the local system to copy files
            // from
            File previousVersionDirectory = new File(myModel.getInstallDirectory(), myModel.getNewestLocalVersion());
            Collection<File> localFiles = FileUtils.listFiles(previousVersionDirectory, TrueFileFilter.TRUE, TrueFileFilter.TRUE);

            double fileCount = installDescriptor.getFiles().size();

            String updateUrlPrefix = AutoUpdateUtils.substituteUrl(AutoUpdateUtils.getUrlString(myModel.getAutoUpdateProtocol(),
                    myModel.getAutoUpdateHostname(), myModel.getUpdateUrl()), installVersion);
            for (FileDescriptor fileDescriptor : installDescriptor.getFiles())
            {
                if (ta.isCancelled())
                {
                    LOG.info("User cancelled download. Cleaning up.");

                    Path versionDirectory = Paths.get(myModel.getInstallDirectory().getAbsolutePath(), installVersion)
                            .normalize();

                    try
                    {
                        Files.walk(versionDirectory).sorted(Comparator.reverseOrder()).map(Path::toFile).peek(LOG::debug)
                        .forEach(File::delete);
                    }
                    catch (IOException e)
                    {
                        LOG.error("Unable to cleanup '" + versionDirectory.toString() + "' after requested cancel", e);
                    }

                    return false;
                }
                String newFilePath = createNewFilePath(fileDescriptor, installVersion);
                File previousVersionFile = DescriptorUtils.getMatchForFile(fileDescriptor, localFiles);
                if (previousVersionFile != null)
                {
                    copyFile(installTracker, previousVersionFile, fileDescriptor, newFilePath);
                }
                else
                {
                    downloadFile(installTracker, updateUrlPrefix, fileDescriptor, newFilePath);
                }

                FileUtils.getFile(newFilePath).setExecutable(fileDescriptor.getExecutable(), false);
                ta.setProgress(installTracker.getFilesRetrieved() / fileCount);
            }
            EventQueueUtilities.invokeLater(() ->
            {
                LOG.info("Updating newest version property to " + installVersion);
                updateProperty("newest.version", installVersion);
                showChooseVersionDialog();
            });
            ta.setComplete(true);
            LOG.info("Downloaded " + installTracker.getBytesDownloaded() + " bytes");
            LOG.info("Copied " + installTracker.getBytesCopied() + " bytes");
            LOG.info("Retrieved " + installTracker.getFilesRetrieved() + " files");
        }
        return true;
    }

    /**
     * Copies the named file from a previous installation to avoid downloading a
     * new copy.
     *
     * @param installTracker the tracker in which progress is maintained.
     * @param sourceFile the file to copy.
     * @param fileDescriptor the file descriptor describing the target file.
     * @param newFilePath the location to which the file will be copied.
     */
    private void copyFile(InstallProgressTracker installTracker, File sourceFile, FileDescriptor fileDescriptor,
            String newFilePath)
    {
        LOG.info("Copying " + fileDescriptor.getFileName());
        try
        {
            installTracker.addToBytesCopied(FileUtils.sizeOf(sourceFile));
            installTracker.incrementRetrievedFiles();
            Files.copy(sourceFile.toPath(), Paths.get(newFilePath), StandardCopyOption.REPLACE_EXISTING);
        }
        catch (IOException e)
        {
            LOG.error("Failed to copy " + fileDescriptor.getFileName() + " from " + sourceFile.getParent(), e);
        }
    }

    /**
     * Copies the named file from a URL to the supplied path.
     *
     * @param installTracker the tracker in which progress is maintained.
     * @param urlPrefix the prefix to use to construct file URLs for retrieval.
     * @param fileDescriptor the file descriptor describing the target file.
     * @param newFilePath the location to which the file will be copied.
     */
    private void downloadFile(InstallProgressTracker installTracker, String urlPrefix, FileDescriptor fileDescriptor,
            String newFilePath)
    {
        LOG.info("Downloading " + fileDescriptor.getFileName());
        int downloadAttempts = 0;
        String downloadFileChecksum = null;
        do
        {
            try (TaskActivity ta = TaskActivity.createActive("Downloading " + fileDescriptor.getFileName()))
            {
                myToolbox.getUIRegistry().getMenuBarRegistry().addTaskActivity(ta);

                URL downloadUrl = getDownloadUrl(urlPrefix, fileDescriptor);
                downloadUpdateFile(downloadUrl, newFilePath);
                downloadAttempts++;
                installTracker.incrementRetrievedFiles();
                File file = new File(newFilePath);
                installTracker.addToBytesDownloaded(FileUtils.sizeOf(file));
                downloadFileChecksum = DescriptorUtils.createChecksum(file);
                ta.setComplete(true);
            }
        }
        while (!fileDescriptor.getChecksum().equals(downloadFileChecksum) && downloadAttempts <= DEFAULT_DOWNLOAD_ATTEMPTS);
    }

    /**
     * Retrieves the file from the supplied URL, and saves it to the supplied
     * path.
     *
     * @param fileUrl the URL of the file to retrieve.
     * @param fileTargetPath the path to which to save the file.
     */
    public void downloadUpdateFile(URL fileUrl, String fileTargetPath)
    {
        try (InputStream responseStream = AutoUpdateUtils.performRequest(fileUrl, myToolbox))
        {
            if (responseStream != null)
            {
                FileUtils.copyInputStreamToFile(responseStream, new File(fileTargetPath));
            }
            else
            {
                LOG.warn("Failed to get file from " + fileUrl.toString());
            }
        }
        catch (IOException e)
        {
            LOG.error("Failed to get file from " + fileUrl.toString(), e);
        }
    }

    /**
     * Creates the target file path string for an update file.
     *
     * @param fileDescriptor the descriptor for an update file
     * @param version the version in which the file is / will be located.
     * @return the target file path string
     */
    private String createNewFilePath(FileDescriptor fileDescriptor, String version)
    {
        // create the new version directory in the OpenSphere folder
        String newVersionPath = new File(myModel.getInstallDirectory(), version).toString();
        FileUtils.getFile(newVersionPath, "plugins").mkdirs();

        String newFilePath = Paths.get(newVersionPath, fileDescriptor.getTargetPath(), fileDescriptor.getFileName()).toString();
        if (!fileDescriptor.getTargetPath().equals("."))
        {
            // make any necessary folders in the new version folder
            FileUtils.getFile(newVersionPath, fileDescriptor.getTargetPath()).mkdirs();
        }
        return newFilePath;
    }

    /**
     * Shows the dialog for the user to choose a preferred version of the
     * application.
     */
    private void showChooseVersionDialog()
    {
        String newestRemoteVersion = myModel.getNewestRemoteVersion();
        String chooseVersionMessage = "Version " + newestRemoteVersion + " has finished downloading." + System.lineSeparator()
        + "The new version will be used the next time the application is launched." + System.lineSeparator()
        + "Would you like to restart with the new version now?";
        updateProperty("preferred.version", newestRemoteVersion);

        int response = JOptionPane.showConfirmDialog(myToolbox.getUIRegistry().getMainFrameProvider().get(), chooseVersionMessage,
                "Would you like to restart?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (response == JOptionPane.YES_OPTION)
        {
            myToolbox.getSystemToolbox().requestRestart();
        }
    }

    /**
     * Updates a configuration property.
     *
     * @param propertyKey the property key
     * @param propertyValue the property value
     */
    private void updateProperty(String propertyKey, String propertyValue)
    {
        if (!propertyValue.isEmpty())
        {
            Properties loadLaunchConfiguration = loadLaunchConfiguration();
            loadLaunchConfiguration.setProperty(propertyKey, propertyValue);
            storeLaunchConfiguration(loadLaunchConfiguration);
        }
    }

    /**
     * Constructs a download URL to access the file described by the supplied
     * descriptor.
     *
     * @param updateUrlPrefix the URL prefix to apply to the file for generating
     *            the download URL.
     * @param fileDescriptor the descriptor of the file to download.
     * @return a URL to access the described file.
     */
    private URL getDownloadUrl(String updateUrlPrefix, FileDescriptor fileDescriptor)
    {
        String remoteFilePath = fileDescriptor.getTargetPath() + "/" + fileDescriptor.getFileName();
        if (remoteFilePath.startsWith("."))
        {
            remoteFilePath = remoteFilePath.substring(1);
        }
        if (remoteFilePath.startsWith("/"))
        {
            remoteFilePath = remoteFilePath.substring(1);
        }
        if (!myModel.getUpdateUrl().endsWith("/"))
        {
            remoteFilePath = "/" + remoteFilePath;
        }

        return UrlUtilities.toURL(updateUrlPrefix + remoteFilePath);
    }

    /**
     * Gets the path of the launch configuration file.
     *
     * @return the absolute path of the launch configuration file.
     */
    private String getLaunchConfigurationFilePath()
    {
        return Paths.get(myModel.getInstallDirectory().getAbsolutePath(), myModel.getLaunchConfigurationFilename())
                .toAbsolutePath().toString();

    }

    /**
     * Loads the launch configuration properties from the configured file. If
     * the file cannot be found, an empty container is returned.
     *
     * @return the {@link Properties} object containing the launch configuration
     *         read from the filesystem.
     */
    public Properties loadLaunchConfiguration()
    {
        Properties properties = new Properties();
        String launchConfigurationFilePath = getLaunchConfigurationFilePath();
        try (InputStream input = new FileInputStream(launchConfigurationFilePath))
        {
            properties.load(input);
        }
        catch (IOException e)
        {
            LOG.error("There was a problem loading properties from '" + launchConfigurationFilePath + "'", e);
        }

        return properties;
    }

    /**
     * Stores the current launch configuration to the configured file.
     *
     * @param properties the properties to store into the launch configuration.
     */
    public void storeLaunchConfiguration(Properties properties)
    {
        String launchConfigurationFilePath = getLaunchConfigurationFilePath();
        try (OutputStream output = new FileOutputStream(launchConfigurationFilePath))
        {
            properties.store(output, "config settings");
        }
        catch (IOException e)
        {
            LOG.error("There was a problem storing properties to '" + launchConfigurationFilePath + "'", e);
        }
    }

    /**
     * Gets the versions which are available to the user in their install
     * directory.
     *
     * @return the version options
     */
    public List<String> getVersionOptions()
    {
        LOG.info("Searching " + myModel.getInstallDirectory() + " for versions of the application");

        Path installDirectory = myModel.getInstallDirectory().toPath();
        try
        {
            return Files.walk(installDirectory).filter(t -> t.getFileName().toString().equals("install_descriptor.json"))
                    .map(Path::toUri).map(AutoUpdateUtils::toDescriptor).filter(Objects::nonNull).map(AutoUpdateUtils::toVersion)
                    .sorted(Comparator.reverseOrder()).collect(Collectors.toList());
        }
        catch (IOException e)
        {
            LOG.error("Unable to search directory '" + myModel.getInstallDirectory().toString() + "'", e);
            return Collections.emptyList();
        }
    }

    /**
     * Deletes the version from the file system.
     *
     * @param versionDirectory the path of the version within the filesystem.
     * @throws IOException if the folder cannot be deleted.
     */
    public void deleteVersionFromFilesystem(Path versionDirectory) throws IOException
    {
        Files.walk(versionDirectory).sorted(Comparator.reverseOrder()).map(Path::toFile).peek(LOG::debug).forEach(File::delete);
    }
}
