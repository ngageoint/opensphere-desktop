package io.opensphere.develop.util.launch;

import java.util.Comparator;

import org.apache.maven.model.Dependency;

/**
 * A comparator implementation used to compare two Maven dependencies, based on
 * groupId, artifactId, and version.
 */
public class DependencyComparator implements Comparator<Dependency>
{
    /**
     * {@inheritDoc}
     *
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compare(Dependency o1, Dependency o2)
    {
        int groupId = o1.getGroupId().compareTo(o2.getGroupId());
        int artifactId = o1.getArtifactId().compareTo(o2.getArtifactId());
        int version = o1.getVersion().compareTo(o2.getVersion());

        if (groupId != 0)
        {
            return groupId;
        }
        if (artifactId != 0)
        {
            return artifactId;
        }
        if (version != 0)
        {
            return version;
        }

        return 0;
    }
}
