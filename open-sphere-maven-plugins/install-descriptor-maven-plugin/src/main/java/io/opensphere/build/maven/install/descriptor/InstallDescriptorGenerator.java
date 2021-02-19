package io.opensphere.build.maven.install.descriptor;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.output.FileWriterWithEncoding;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.apache.maven.shared.model.fileset.util.FileSetManager;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import io.opensphere.core.appl.versions.DescriptorUtils;
import io.opensphere.core.appl.versions.FileDescriptor;
import io.opensphere.core.appl.versions.InstallDescriptor;

/**
 * @goal generate-install-descriptor
 * @phase package
 * @threadSafe true
 */
public class InstallDescriptorGenerator extends AbstractMojo
{
    /**
     * The Maven Session Object
     * @parameter property="session" default-value="${session}"
     * @required true
     * @readonly true
     */
    private MavenSession mySession;

    /**
     * The Maven Project Object
     * @parameter property="project" default-value="${project}"
     * @required true
     * @readonly true
     */
    private MavenProject myProject;

    /**
     * Maven ProjectHelper.
     * @component
     */
    private MavenProjectHelper myProjectHelper;

    /**
     * The version number of the build (optional, extracted from the maven
     * project if not specified).
     * @required false
     * @parameter alias="version"
     */
    private String myVersion;

    /**
     * The build number to include in the generated install descriptor.
     * @required true
     * @parameter alias="buildnumber"
     */
    private String buildnumber;

    /**
     * The environment for which the build was generated, will be included in
     * the generated install descriptor.
     * @required true
     * @parameter alias="environment"
     */
    private String environment;

    /**
     * The target operating system for which the build was generated, will be
     * included in the generated install descriptor.
     * @required true
     * @parameter alias="targetos"
     */
    private String targetos;

    /**
     * The name of the file to be created by the install descriptor generator.
     * @required true
     * @parameter alias="destination"
     */
    private String destination;

    /**
     * The group of directed file sets to include in the install descriptor.
     * @parameter
     */
    private DirectedFileSet[] filesets;

    /**
     * The set of files to include in the install descriptor.
     * @parameter
     */
    private DirectedFileSet fileset;

    /**
     * {@inheritDoc}
     *
     * @see org.apache.maven.plugin.Mojo#execute()
     */
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException
    {
        getLog().info("Generating install descriptor");
        if (StringUtils.isBlank(myVersion))
        {
            myVersion = myProject.getVersion();
            getLog().info("Version not specified, using " + myVersion + " from the build.");
        }

        InstallDescriptor installDescriptor = new InstallDescriptor();
        installDescriptor.setVersion(myVersion);
        installDescriptor.setBuild(buildnumber);
        installDescriptor.setEnvironment(environment);
        installDescriptor.setTargetOperatingSystem(targetos);

        List<FileDescriptor> fileDescriptors = new ArrayList<>();
        FileSetManager manager = new FileSetManager(getLog());
        if (filesets != null)
        {
            for (DirectedFileSet directedFileSet : filesets)
            {
                fileDescriptors.addAll(processFileSet(directedFileSet, manager));
            }
        }

        if (fileset != null)
        {
            fileDescriptors.addAll(processFileSet(fileset, manager));
        }

        installDescriptor.setFiles(fileDescriptors);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);

        File jsonFile = Paths.get(destination).toFile();
        getLog().info("Writing install descriptor to " + jsonFile.getAbsolutePath());
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
            getLog().error("Could not create installdescriptor.json at " + jsonFile.getAbsolutePath(), e);
        }
    }

    /**
     * Examines the supplied fileset (using the supplied manager), processing
     * and returning a collection of file descriptors.
     *
     * @param fileset the fileset to examine.
     * @param manager the manager with which to examine the fileset.
     * @return a collection of file descriptors generated from the supplied
     *         fileset (which may be empty, but will never be null).
     */
    public Collection<FileDescriptor> processFileSet(DirectedFileSet fileset, FileSetManager manager)
    {
        List<FileDescriptor> fileDescriptors = new ArrayList<>();
        getLog().info("Extracting files from " + fileset.getDirectory());

        String[] includedFiles = manager.getIncludedFiles(fileset);
        String[] includedDirectories = manager.getIncludedDirectories(fileset);

        getLog().debug("Found " + includedFiles.length + " included files.");
        getLog().debug("Found " + includedDirectories.length + " included directories.");

        for (String includedFile : includedFiles)
        {
            getLog().debug("Processing " + includedFile);
            getLog().debug("Fileset target: " + fileset.getTarget());
            Path path = Paths.get(fileset.getTarget(), includedFile).normalize();
            getLog().debug("Path: " + path.toString());

            FileDescriptor descriptor = new FileDescriptor();
            descriptor.setFileName(path.getFileName().toString());
            if (path.getParent() != null)
            {
                descriptor.setTargetPath(path.getParent().toString());
            }
            else
            {
                descriptor.setTargetPath(".");
            }

            descriptor.setExecutable(isExecutable(path));
            descriptor.setChecksum(DescriptorUtils.createChecksum(Paths.get(fileset.getDirectory(), includedFile).toFile()));
            fileDescriptors.add(descriptor);
        }
        return fileDescriptors;
    }

    /**
     * Tests to determine if the supplied path points to file that should be
     * executable.
     *
     * @param path the path to test.
     * @return true if the path should be executable, false otherwise.
     */
    protected boolean isExecutable(Path path)
    {
        if (path.getParent() != null)
        {
            Path parent = path.getParent();
            if (parent.endsWith("jre/bin"))
            {
                return true;
            }
        }

        String fullPath = path.toString();
        if (fullPath.endsWith(".sh") || fullPath.endsWith(".exe") || fullPath.endsWith(".bat") || fullPath.endsWith(".vbs"))
        {
            return true;
        }
        return false;
    }

}
