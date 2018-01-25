package io.opensphere.core.appl.versions;

import java.util.Comparator;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.artifact.versioning.ComparableVersion;

/**
 * Compares two version numbers to each other and indicates which one is less
 * than the other, or if they are equal.
 */
public class VersionComparator implements Comparator<String>
{
    /**
     * {@inheritDoc}
     *
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compare(String version1, String version2)
    {
        int compare = 0;

        if (StringUtils.isEmpty(version2))
        {
            compare = 1;
        }
        else if (StringUtils.isEmpty(version1))
        {
            compare = -1;
        }
        else
        {
            ComparableVersion comparableVersion1 = new ComparableVersion(version1);
            ComparableVersion comparableVersion2 = new ComparableVersion(version2);

            compare = comparableVersion1.compareTo(comparableVersion2);
        }

        return compare;
    }
}
