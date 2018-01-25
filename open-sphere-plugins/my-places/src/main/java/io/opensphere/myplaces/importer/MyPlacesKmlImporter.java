package io.opensphere.myplaces.importer;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;

import de.micromata.opengis.kml.v_2_2_0.Document;
import de.micromata.opengis.kml.v_2_2_0.Feature;
import de.micromata.opengis.kml.v_2_2_0.Folder;
import de.micromata.opengis.kml.v_2_2_0.Kml;
import de.micromata.opengis.kml.v_2_2_0.LineString;
import de.micromata.opengis.kml.v_2_2_0.Placemark;
import io.opensphere.core.Toolbox;
import io.opensphere.core.importer.ImportCallback;
import io.opensphere.core.util.collections.New;
import io.opensphere.kml.gx.Track;
import io.opensphere.kml.marshal.KmlMarshaller;
import io.opensphere.myplaces.export.ExporterUtilities;
import io.opensphere.myplaces.models.MyPlacesModel;

/**
 * MyPlaces KML importer.
 */
public class MyPlacesKmlImporter extends AbstractMyPlacesImporter
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(MyPlacesKmlImporter.class);

    /** The supported file extensions. */
    private static final List<String> ourFileExtensions = New.unmodifiableList("kml");

    /**
     * Converts the KML into placemarks under a folder.
     *
     * @param kml the KML object
     * @param folderName the name of the folder
     * @return the folder
     */
    private static Folder getFolderWithPlacemarks(Kml kml, String folderName)
    {
        Folder folder = null;
        if (kml != null && kml.getFeature() instanceof Document)
        {
            Document importDoc = (Document)kml.getFeature();

            folder = new Folder();
            folder.setName(folderName);
            folder.setVisibility(Boolean.TRUE);

            replaceLinesWithTracks(importDoc.getFeature());

            for (Feature feature : importDoc.getFeature())
            {
                folder.addToFeature(feature);
            }
        }
        return folder;
    }

    /**
     * Reads the file.
     *
     * @param file the file
     * @return the KML object
     */
    private static Kml readFile(File file)
    {
        Kml kml;
        try
        {
            kml = KmlMarshaller.getInstance().unmarshal(file);
        }
        catch (FileNotFoundException e)
        {
            kml = null;
            LOGGER.error(e, e);
        }
        return kml;
    }

    /**
     * Replaces all lines with tracks. This is done because newer Google Earth
     * does not show tracks without time.
     *
     * @param features the features
     */
    private static void replaceLinesWithTracks(Collection<Feature> features)
    {
        Collection<Placemark> placemarks = New.list();
        for (Feature feature : features)
        {
            ExporterUtilities.flattenToPlacemarks(feature, placemarks);
        }

        for (Placemark placemark : placemarks)
        {
            if (placemark.getGeometry() instanceof LineString)
            {
                LineString line = (LineString)placemark.getGeometry();
                Track track = new Track();
                track.getCoordinates().addAll(line.getCoordinates());
                for (int i = 0, n = line.getCoordinates().size(); i < n; i++)
                {
                    track.getWhen().add(null);
                }
                placemark.setGeometry(track);
            }
        }
    }

    /**
     * Constructor.
     *
     * @param toolbox the toolbox
     * @param model the model
     */
    public MyPlacesKmlImporter(Toolbox toolbox, MyPlacesModel model)
    {
        super(toolbox, model);
    }

    @Override
    public int getPrecedence()
    {
        return 100;
    }

    @Override
    public List<String> getSupportedFileExtensions()
    {
        return ourFileExtensions;
    }

    @Override
    public void importFile(File aFile, ImportCallback callback)
    {
        Kml kml = readFile(aFile);
        Folder folder = getFolderWithPlacemarks(kml, aFile.getName());
        addFolderOrFail(folder, aFile);
    }
}
