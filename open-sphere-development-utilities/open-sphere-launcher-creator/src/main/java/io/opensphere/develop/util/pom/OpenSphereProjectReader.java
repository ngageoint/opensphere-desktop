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
     * @param projectNames the name of the project folder. This should be an
     *            array of >=1.
     * @see io.opensphere.develop.util.pom.AbstractCompositeProjectReader#readProject(String...)
     */
    @Override
    public Project readProject(String... projectNames)
    {
        return readProjectImpl(projectNames[0], " -Dmionline.login.disabled=true", null,
                "open-sphere-config/default-config/override-jar");
    }
}
