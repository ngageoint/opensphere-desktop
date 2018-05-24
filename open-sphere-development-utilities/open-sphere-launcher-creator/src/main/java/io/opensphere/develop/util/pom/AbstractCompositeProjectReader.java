package io.opensphere.develop.util.pom;

import java.nio.file.Paths;
import java.util.Collections;
import java.util.Set;

/**
 * A basic project reader, configured with common items needed to read
 * OpenSphere projects.
 */
public abstract class AbstractCompositeProjectReader
{
    /** The composite model in which projects are referenced. */
    private final CompositeProjectModel myCompositeProjectModel;

    /** The maven profiles active during project resolution. */
    private final Set<String> myActiveProfiles;

    /**
     * Creates a new project reader.
     *
     * @param compositeProjectModel The composite model in which projects are
     *            referenced.
     * @param activeProfiles The maven profiles active during project
     *            resolution.
     */
    public AbstractCompositeProjectReader(CompositeProjectModel compositeProjectModel, Set<String> activeProfiles)
    {
        myActiveProfiles = activeProfiles;
        myCompositeProjectModel = compositeProjectModel;
        myCompositeProjectModel.setProfiles(myActiveProfiles);
    }

    /**
     * Reads the project specified by the concrete reader implementation.
     *
     * @param projectName the name of the project folder
     * @return the project read from the POM.
     */
    public abstract Project readProject(String projectName);

    /**
     * Gets the value of the {@link #myActiveProfiles} field.
     *
     * @return the value stored in the {@link #myActiveProfiles} field.
     */
    public Set<String> getActiveProfiles()
    {
        return myActiveProfiles;
    }

    /**
     * Reads and creates a {@link Project} object using the supplied
     * information. The generated {@link Project} object is stored in the
     * {@link #myCompositeProjectModel} before being returned.
     *
     * @param relativePath the path of the project to read, relative to the
     *            {@link CompositeProjectModel#getRootPath()}.
     * @param additionalVmArgs the additional VM Arguments to supply to the
     *            generated launcher (these will be used in the output).
     * @param suffix the launcher suffix to apply to the generated output.
     * @param additionalClasspath the additional classpath items to supply to
     *            the generated launcher (these will be used in the output).
     * @return a {@link Project} object generated using the supplied
     *         information.
     */
    protected Project readProjectImpl(String relativePath, String additionalVmArgs, String suffix, String... additionalClasspath)
    {
        Project project = new Project(Paths.get(myCompositeProjectModel.getRootPath().toString(), relativePath),
                myCompositeProjectModel.getModuleRegistry(), getExcludedArtifactIds());
        project.setDirPrefix(project.getArtifactId());
        project.setTitle(project.getModel().getName());

        project.setLauncherPrefix(project.getTitle().replaceAll(" ", "_") + "_");
        project.setLauncherSuffix(suffix);

        project.setAdditionalVmArgs(additionalVmArgs);
        project.setAdditionalClasspathItems(additionalClasspath);

        myCompositeProjectModel.storeProject(project);

        return project;
    }

    /**
     * Gets the value of the {@link #myCompositeProjectModel} field.
     *
     * @return the value stored in the {@link #myCompositeProjectModel} field.
     */
    public CompositeProjectModel getCompositeProjectModel()
    {
        return myCompositeProjectModel;
    }

    /**
     * Gets the set of artifact IDs that should be excluded from this operation.
     *
     * @return the set of artifact IDs that should not be resolved.
     */
    public Set<String> getExcludedArtifactIds()
    {
        return Collections.emptySet();
    }
}
