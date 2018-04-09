package io.opensphere.core.appl.versions;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.output.FileWriterWithEncoding;
import org.apache.log4j.Logger;
import org.apache.maven.plugin.MojoFailureException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * Generates and places a catalog of all files in an update.
 */
public final class InstallDescriptorGenerator
{
    /** The {@link Logger} instance used to capture output. */
    private static final Logger LOG = Logger.getLogger(InstallDescriptorGenerator.class);

    /** The name of the application pack directory. */
    private static final String APPLICATION_PACK = "application_pack";

    /** The name of the resource pack directory. */
    private static final String RESOURCE_PACK = "resource_pack";

    /** The name of the native library pack directory. */
    private static final String NATIVE_LIBRARY_PACK = "native_library_pack";

    /** The name of the update pack directory. */
    private static final String UPDATE_PACK = "update_pack";

    /** The update version. */
    private static String ourVersion;

    /** The update build number. */
    private static String ourBuild;

    /** The target environment. */
    private static String ourEnvironment;

    /** The target operating system. */
    private static String ourTargetOperatingSystem;

    /** The directory that holds the update files. */
    private static String ourStagingDirectoryPath;

    /**
     * Main method called during the package phase of the build.
     *
     * @param args the properties passed in during the build
     * @throws MojoFailureException if the install descriptor is invalid
     */
    public static void main(String[] args) throws MojoFailureException
    {
        ourVersion = args[0];
        ourBuild = args[1];
        ourEnvironment = args[2];
        ourTargetOperatingSystem = args[3];
        ourStagingDirectoryPath = args[4];

        LOG.info("Version: " + ourVersion);
        LOG.info("Build: " + ourBuild);
        LOG.info("Environment: " + ourEnvironment);
        LOG.info("Target operating system: " + ourTargetOperatingSystem);
        LOG.info("Staging directory path: " + ourStagingDirectoryPath);

        try
        {
            generateInstallDescriptor();
        }
        catch (IOException e)
        {
            throw new MojoFailureException("Invalid install descriptor created", e);
        }
    }

    /**
     * Generates the install descriptor json file.
     *
     * @throws IOException if an  invalid file descriptor was created
     */
    private static void generateInstallDescriptor() throws IOException
    {
        Collection<File> applicationFiles = FileUtils
                .listFiles(new File(ourStagingDirectoryPath + File.separatorChar + APPLICATION_PACK), null, true);
        Collection<File> resourceFiles = FileUtils
                .listFiles(new File(ourStagingDirectoryPath + File.separatorChar + RESOURCE_PACK), null, true);
        Collection<File> nativeLibraryFiles = FileUtils
                .listFiles(new File(ourStagingDirectoryPath + File.separatorChar + NATIVE_LIBRARY_PACK), null, true);

        List<FileDescriptor> fileDescriptors = createFileDescriptors(applicationFiles, APPLICATION_PACK);
        fileDescriptors.addAll(createFileDescriptors(resourceFiles, RESOURCE_PACK));
        fileDescriptors.addAll(createFileDescriptors(nativeLibraryFiles, NATIVE_LIBRARY_PACK));

        createJsonFile(createInstallDescriptor(ourVersion, ourEnvironment, ourTargetOperatingSystem, fileDescriptors));
    }

    /**
     * Creates a json file from the install descriptor and places the file.
     *
     * @param installDescriptor the install descriptor
     */
    private static void createJsonFile(InstallDescriptor installDescriptor)
    {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);

        File jsonFile = Paths.get(ourStagingDirectoryPath, UPDATE_PACK, "installdescriptor.json").toFile();
        boolean fileExists = jsonFile.exists();

        try
        {
            String stringInstallDescriptor = objectMapper.writeValueAsString(installDescriptor);
            if (!fileExists)
            {
                fileExists = jsonFile.createNewFile();
            }
            if (fileExists)
            {
                FileWriterWithEncoding writer = new FileWriterWithEncoding(jsonFile.getAbsolutePath(), "UTF-8");
                BufferedWriter bw = new BufferedWriter(writer);

                bw.write(stringInstallDescriptor);
                bw.close();
            }
        }
        catch (IOException e)
        {
            LOG.error("Could not create installdescriptor.json at " + jsonFile.getAbsolutePath(), e);
        }
    }

    /**
     * Creates an install descriptor.
     *
     * @param version the update version
     * @param environment the target environment
     * @param operatingSystem the target operating system
     * @param fileDescriptors the update file descriptors
     * @return an install descriptor
     */
    private static InstallDescriptor createInstallDescriptor(String version, String environment, String operatingSystem,
            List<FileDescriptor> fileDescriptors)
    {
        InstallDescriptor installDescriptor = new InstallDescriptor();
        installDescriptor.setVersion(version);
        installDescriptor.setEnvironment(environment);
        installDescriptor.setTargetOperatingSystem(operatingSystem);
        installDescriptor.setFiles(fileDescriptors);
        return installDescriptor;
    }

    /**
     * Creates a list of file descriptors.
     *
     * @param files the update files
     * @param packType the type of pack that the files belong to
     * @return a list of file descriptors
     * @throws IOException if the checksum was set to null - making an invalid
     *             file descriptor
     */
    private static List<FileDescriptor> createFileDescriptors(Collection<File> files, String packType) throws IOException
    {
        List<FileDescriptor> fileDescriptors = new ArrayList<>();
        String targetPath = createTargetPath(files, packType);

        for (File file : files)
        {
            FileDescriptor fileDescriptor = new FileDescriptor();
            fileDescriptor.setFileName(file.getName());
            fileDescriptor.setTargetPath(targetPath);
            // check if the file is a shell script
            if (FilenameUtils.getExtension(file.getName()).equals("sh"))
            {
                fileDescriptor.setExecutable(true);
            }
            fileDescriptor.setChecksum(DescriptorUtils.createChecksum(file));
            fileDescriptors.add(fileDescriptor);
        }
        return fileDescriptors;
    }

    /**
     * Creates the target path for files in a given pack.
     *
     * @param files the update files
     * @param packType the type of pack that the files belong to
     * @return targetPath the target path for files in a pack
     */
    private static String createTargetPath(Collection<File> files, String packType)
    {
        String targetPath;
        switch (packType)
        {
            case APPLICATION_PACK:
                targetPath = ".";
                break;
            case RESOURCE_PACK:
                targetPath = ".";
                break;
            case NATIVE_LIBRARY_PACK:
                if (ourTargetOperatingSystem.equals("win64"))
                {
                    targetPath = "lib" + File.separator + "win32" + File.separator + "x86_64";
                }
                else
                {
                    targetPath = "lib" + File.separator + "linux" + File.separator + "x86_64";
                }
                break;
            default:
                targetPath = ".";
        }
        return targetPath;
    }

    /**
     * Disallows instantiation.
     */
    private InstallDescriptorGenerator()
    {
        throw new UnsupportedOperationException("Instantiating a utility class");
    }
}
