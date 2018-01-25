package io.opensphere.myplaces.export;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import org.w3c.dom.Node;

import de.micromata.opengis.kml.v_2_2_0.Document;
import de.micromata.opengis.kml.v_2_2_0.Feature;
import de.micromata.opengis.kml.v_2_2_0.Kml;
import de.micromata.opengis.kml.v_2_2_0.LineString;
import de.micromata.opengis.kml.v_2_2_0.Placemark;
import io.opensphere.core.util.MimeType;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.kml.gx.Track;
import io.opensphere.kml.marshal.KmlMarshaller;

/**
 * Exporter from MyPlaces to KML.
 */
public class MyPlacesKmlExporter extends AbstractMyPlacesExporter
{
    /**
     * Transforms the given features to a Kml.
     *
     * @param features the features
     * @return the Kml
     */
    private static Kml createKml(Collection<Feature> features)
    {
        Kml kml = new Kml();
        Document document = kml.createAndSetDocument();
        document.setName("My Places");
        for (Feature feature : features)
        {
            document.addToFeature(feature);
        }
        return kml;
    }

    /**
     * Replaces all tracks that don't have time with lines. This is done because
     * newer Google Earth does not show tracks without time.
     *
     * @param features the features
     */
    private static void replaceTracksWithLines(Collection<Feature> features)
    {
        Collection<Placemark> placemarks = New.list();
        for (Feature feature : features)
        {
            ExporterUtilities.flattenToPlacemarks(feature, placemarks);
        }

        for (Placemark placemark : placemarks)
        {
            if (placemark.getGeometry() instanceof Track)
            {
                Track track = (Track)placemark.getGeometry();
                boolean anyWhensAreNull = track.getWhen().stream().anyMatch(date -> date == null);
                if (anyWhensAreNull)
                {
                    LineString line = new LineString();
                    line.setCoordinates(track.getCoordinates());
                    placemark.setGeometry(line);
                }
            }
        }
    }

    /**
     * Writes the features to the file.
     *
     * @param file the file
     * @param features the features
     * @return the file written to
     * @throws IOException Signals that an I/O exception has occurred
     */
    private static File writeToFile(File file, Collection<Feature> features) throws IOException
    {
        Utilities.checkNull(file, "file");
        Utilities.checkNull(features, "features");

        replaceTracksWithLines(features);
        Kml kml = createKml(features);
        KmlMarshaller.getInstance().marshal(kml, file);
        return file;
    }

    @Override
    public File export(File file) throws IOException
    {
        File realFile = getExportFiles(file).iterator().next();

        return writeToFile(realFile, ExporterUtilities.getFeatures(getObjects()));
    }

    @Override
    public void export(Node node)
    {
        Collection<Feature> features = ExporterUtilities.getFeatures(getObjects());

        replaceTracksWithLines(features);
        Kml kml = createKml(features);
        KmlMarshaller.getInstance().marshal(kml, node);
    }

    @Override
    public MimeType getMimeType()
    {
        return MimeType.KML;
    }

    @Override
    public String getMimeTypeString()
    {
        return super.getMimeTypeString() + " (Recommended)";
    }
}
