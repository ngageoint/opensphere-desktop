package io.opensphere.kml.envoy;

import static org.junit.Assert.assertEquals;

import java.io.InputStream;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.junit.Test;

import de.micromata.opengis.kml.v_2_2_0.Coordinate;
import de.micromata.opengis.kml.v_2_2_0.Document;
import de.micromata.opengis.kml.v_2_2_0.Feature;
import de.micromata.opengis.kml.v_2_2_0.Folder;
import de.micromata.opengis.kml.v_2_2_0.Kml;
import de.micromata.opengis.kml.v_2_2_0.Placemark;
import io.opensphere.kml.gx.DateConverter;
import io.opensphere.kml.gx.Track;

/**
 * Unit test for {@link KMLParserPool}.
 */
public class KMLParserPoolTest
{
    /**
     * Tests parsing a kml with tracks and verifies the track geometry is
     * replaced with our custom geometry.
     *
     * @throws JAXBException bad jaxb.
     */
    @Test
    public void testProcessTracks() throws JAXBException
    {
        InputStream stream = KMLParserPoolTest.class.getResourceAsStream("/track.kml");
        KMLParserPool pool = new KMLParserPool();
        Kml kml = pool.process(stream);
        List<Feature> features = ((Folder)((Folder)((Document)kml.getFeature()).getFeature().get(0)).getFeature().get(0))
                .getFeature();

        assertEquals(1, features.size());
        Placemark placemark = (Placemark)features.get(0);
        Track track = (Track)placemark.getGeometry();

        assertEquals(42, track.getCoordinates().size());
        assertEquals(42, track.getWhen().size());

        DateConverter converter = new DateConverter();
        assertEquals("2017-05-28T14:50:00Z", converter.marshal(track.getWhen().get(0)));
        assertEquals("2017-05-28T14:54:00Z", converter.marshal(track.getWhen().get(1)));
        assertEquals("2017-05-28T15:38:00Z", converter.marshal(track.getWhen().get(20)));
        assertEquals("2017-05-28T16:30:00Z", converter.marshal(track.getWhen().get(40)));
        assertEquals("2017-05-28T16:32:00Z", converter.marshal(track.getWhen().get(41)));

        assertEquals(new Coordinate(-101.0797, 38.7619, 2552.7), track.getCoordinates().get(0));
        assertEquals(new Coordinate(-101.1883, 38.7871, 2552.7), track.getCoordinates().get(1));
        assertEquals(new Coordinate(-102.7842, 39.1448, 2484.12), track.getCoordinates().get(20));
        assertEquals(new Coordinate(-104.7859, 39.5541, 1851.66), track.getCoordinates().get(40));
        assertEquals(new Coordinate(-104.8386, 39.5699, 1684.02), track.getCoordinates().get(41));
    }
}
