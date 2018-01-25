package io.opensphere.myplaces.dataaccess;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;

import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;

import de.micromata.opengis.kml.v_2_2_0.Document;
import de.micromata.opengis.kml.v_2_2_0.Folder;
import de.micromata.opengis.kml.v_2_2_0.Kml;
import io.opensphere.core.Toolbox;
import io.opensphere.core.preferences.Preferences;
import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.kml.envoy.KMLProcessor;
import io.opensphere.kml.marshal.KmlMarshaller;
import io.opensphere.myplaces.constants.Constants;

/**
 * Used to save and load all My Places data.
 *
 */
public class MyPlacesDataAccessor implements Runnable
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(MyPlacesDataAccessor.class);

    /**
     * The key to the my places save data.
     */
    private static final String KEY = "MyPlaces";

    /**
     * The toolbox.
     */
    private final Toolbox myToolbox;

    /**
     * The kml to save.
     */
    private Kml mySaveKml;

    /**
     * Constructs a new data accessor.
     *
     * @param toolbox The toolbox.
     */
    public MyPlacesDataAccessor(Toolbox toolbox)
    {
        myToolbox = toolbox;
    }

    /**
     * Loads the my places tree stored in the system.
     *
     * @return The my places tree or null if there isn't one.
     */
    public synchronized Kml loadMyPlaces()
    {
        Preferences preferences = getPreferences();

        Kml kml = null;

        String kmlString = preferences.getString(KEY, null);

        if (kmlString != null)
        {
            try
            {
                kml = KMLProcessor.PARSER_POOL.process(new ByteArrayInputStream(kmlString.getBytes()));
            }
            catch (JAXBException e)
            {
                LOGGER.error("Unable to load My Places", e);
            }
        }
        else
        {
            kml = new Kml();
            Document document = kml.createAndSetDocument();
            Folder folder = document.createAndAddFolder();
            folder.setName(Constants.MY_PLACES_LABEL);
            folder.setId(Constants.MY_PLACES_ID);
            folder.setVisibility(Boolean.TRUE);
        }

        return kml;
    }

    @Override
    public synchronized void run()
    {
        Preferences preferences = getPreferences();

        StringWriter stringWriter = new StringWriter();
        KmlMarshaller.getInstance().marshal(mySaveKml, stringWriter);

        String kmlString = stringWriter.getBuffer().toString();
        preferences.putString(KEY, kmlString, this);
    }

    /**
     * Saves the new my places kml to the system.
     *
     * @param kml The new my places tree.
     */
    public synchronized void saveMyPlaces(Kml kml)
    {
        Kml clonedKml = kml.clone();
        mySaveKml = clonedKml;

        Thread background = new Thread(this, "My Places Save");
        background.setDaemon(true);
        background.start();
    }

    /**
     * Saves the my places kml on the calling thread.
     *
     * @param kml The kml to save.
     */
    public synchronized void saveMyPlacesSynchronized(Kml kml)
    {
        mySaveKml = kml;
        run();
    }

    /**
     * Gets the preferences for my places.
     *
     * @return The preferences.
     */
    private Preferences getPreferences()
    {
        PreferencesRegistry registry = myToolbox.getPreferencesRegistry();
        Preferences preferences = registry.getPreferences(MyPlacesDataAccessor.class);

        return preferences;
    }
}
