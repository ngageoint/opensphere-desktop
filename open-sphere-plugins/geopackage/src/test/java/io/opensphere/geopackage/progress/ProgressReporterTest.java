package io.opensphere.geopackage.progress;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.taskactivity.CancellableTaskActivity;
import io.opensphere.geopackage.model.GeoPackageLayer;
import io.opensphere.geopackage.model.LayerType;
import io.opensphere.geopackage.model.ProgressModel;

/**
 * Unit test for the {@link ProgressReporter} class.
 */
public class ProgressReporterTest
{
    /**
     * Tests import progress reporting.
     */
    @Test
    public void test()
    {
        ProgressModel model = new ProgressModel();

        GeoPackageLayer layer1 = new GeoPackageLayer("package", "c:\\somefile.gpkg", "one", LayerType.FEATURE, 2);
        GeoPackageLayer layer2 = new GeoPackageLayer(layer1.getPackageName(), layer1.getPackageFile(), "two", LayerType.FEATURE,
                3);
        GeoPackageLayer layer3 = new GeoPackageLayer(layer1.getPackageName(), layer1.getPackageFile(), "one", LayerType.FEATURE,
                5);

        List<GeoPackageLayer> layers = New.list(layer1, layer2, layer3);

        CancellableTaskActivity ta = new CancellableTaskActivity();

        ProgressReporter reporter = new ProgressReporter(model, layers, ta);

        for (int i = 0; i < 10; i++)
        {
            model.setCompletedCount(model.getCompletedCount() + 1);
            assertEquals((i + 1) / 10d, ta.getProgress(), 0.01d);
            assertEquals((i + 1) * 10 + "% complete importing " + "c:\\somefile.gpkg", ta.getLabelValue());
        }

        reporter.close();

        assertEquals(0, model.countObservers());
    }

    /**
     * Tests import progress reporting.
     */
    @Test
    public void testExport()
    {
        ProgressModel model = new ProgressModel();

        CancellableTaskActivity ta = new CancellableTaskActivity();

        ProgressReporter reporter = new ProgressReporter(model, "c:\\somefile.gpkg", 10, ta);

        for (int i = 0; i < 10; i++)
        {
            model.setCompletedCount(model.getCompletedCount() + 1);
            assertEquals((i + 1) / 10d, ta.getProgress(), 0.01d);
            assertEquals((i + 1) * 10 + "% complete exporting " + "c:\\somefile.gpkg", ta.getLabelValue());
        }

        reporter.close();

        assertEquals(0, model.countObservers());
    }
}
