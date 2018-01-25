package io.opensphere.develop.util.pom;

import java.util.Comparator;

import org.apache.commons.lang3.StringUtils;

/**
 * A comparator implementation designed to examine two {@link Coordinate}
 * instances, and determine if they are equivalent.
 */
public class CoordinateComparator implements Comparator<Coordinate>
{
    /**
     * {@inheritDoc}
     *
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compare(Coordinate o1, Coordinate o2)
    {
        int groupIdResult = StringUtils.compare(o1.getGroupId(), o2.getGroupId());
        int artifactIdResult = StringUtils.compare(o1.getArtifactId(), o2.getArtifactId());
        int versionResult = StringUtils.compare(o1.getVersion(), o2.getVersion());
        int packagingResult = StringUtils.compare(o1.getPackaging(), o2.getPackaging());
        int classifierResult = StringUtils.compare(o1.getClassifier(), o2.getClassifier());

        if (groupIdResult != 0)
        {
            return groupIdResult;
        }
        if (artifactIdResult != 0)
        {
            return artifactIdResult;
        }
        if (versionResult != 0)
        {
            return versionResult;
        }
        if (packagingResult != 0)
        {
            return packagingResult;
        }
        if (classifierResult != 0)
        {
            return classifierResult;
        }

        return 0;
    }

}
