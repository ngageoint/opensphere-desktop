package io.opensphere.core.appl;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Set;

import org.junit.Test;

import io.opensphere.core.appl.versions.VersionComparator;
import io.opensphere.core.util.collections.New;

/**
 * Unit test for {@link VersionComparator}.
 */
public class NewVersionCheckerTest
{
    /**
     * Tests the version checker.
     */
    @Test
    public void test()
    {
        String version1 = "5.1.6_71620";
        String version2 = "5.1.7";
        String version3 = "5.1.7.104568";
        String version4 = "5.1.7-SNAPSHOT";
        String version5 = "5.2.0-SNAPSHOT_2017241_155447555";
        String version6 = "5.2.0-SNAPSHOT_2017241_156447555";
        String version7 = "5.2.0-SNAPSHOT_2017241_156457555";
        String version8 = "5.2.0-SNAPSHOT_2017241_157447555";
        String version9 = "5.2.0-SNAPSHOT_2017241_165447555";
        String version10 = "5.2.0-SNAPSHOT_2017241_255447555";
        String version11 = "5.2.0-SNAPSHOT_2017241_355447555";
        String version12 = "5.2.0.1-SNAPSHOT_2017241_355447555";
        String version13 = "5.2.0.2-SNAPSHOT_2017241_355447555";
        String version14 = "5.2.0.4-SNAPSHOT_2017241_355447555";
        String version15 = "5.2.1.4-SNAPSHOT_2017241_355447555";
        String version16 = "5.3.1.4-SNAPSHOT_2017241_355447555";
        String version17 = "5.3.1.4_2017241_355447555";
        String version18 = "6.0.0.0-SNAPSHOT_2017241_355447555";
        String version19 = null;

        Set<String> versions = New.set(version1, version2, version3, version4, version5, version6, version7, version8, version9,
                version10, version11, version12, version13, version14, version15, version16, version17, version18, version19);
        List<String> versionList = New.list(versions);

        versionList.sort(new VersionComparator());

        assertEquals(version19, versionList.get(0));
        assertEquals(version1, versionList.get(1));
        assertEquals(version4, versionList.get(2));
        assertEquals(version2, versionList.get(3));
        assertEquals(version3, versionList.get(4));
        assertEquals(version5, versionList.get(5));
        assertEquals(version6, versionList.get(6));
        assertEquals(version7, versionList.get(7));
        assertEquals(version8, versionList.get(8));
        assertEquals(version9, versionList.get(9));
        assertEquals(version10, versionList.get(10));
        assertEquals(version11, versionList.get(11));
        assertEquals(version12, versionList.get(12));
        assertEquals(version13, versionList.get(13));
        assertEquals(version14, versionList.get(14));
        assertEquals(version15, versionList.get(15));
        assertEquals(version17, versionList.get(16));
        assertEquals(version16, versionList.get(17));
        assertEquals(version18, versionList.get(18));
    }
}
