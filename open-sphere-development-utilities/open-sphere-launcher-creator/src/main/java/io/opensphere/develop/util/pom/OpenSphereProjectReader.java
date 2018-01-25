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
     * @see io.opensphere.develop.util.pom.AbstractCompositeProjectReader#readProject()
     */
    @Override
    public Project readProject()
    {
        return readProjectImpl("OpenSphereDesktop", " -Dmionline.login.disabled=true",
                "open-sphere-config/default-config/override-jar", null);
    }
}
