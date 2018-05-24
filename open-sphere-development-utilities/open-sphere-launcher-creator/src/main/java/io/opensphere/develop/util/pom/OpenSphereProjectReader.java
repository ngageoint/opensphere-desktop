package io.opensphere.develop.util.pom;

import java.util.Set;

/**
 *
 */
public class OpenSphereProjectReader extends AbstractCompositeProjectReader
{
    /**
     * Creates a new project reader to read open sphere projects.
     *
     * @param compositeProjectModel The composite model in which projects are
     *            referenced.
     * @param activeProfiles The maven profiles active during project
     *            resolution.
     */
    public OpenSphereProjectReader(CompositeProjectModel compositeProjectModel, Set<String> activeProfiles)
    {
        super(compositeProjectModel, activeProfiles);
    }

    /**
     * {@inheritDoc}
     *
     * @param projectName the name of the project folder
     * @see io.opensphere.develop.util.pom.AbstractCompositeProjectReader#readProject()
     */
    @Override
    public Project readProject(String projectName)
    {
        return readProjectImpl(projectName, " -Dmionline.login.disabled=true", null,
            "open-sphere-config/default-config/override-jar");
    }
}
