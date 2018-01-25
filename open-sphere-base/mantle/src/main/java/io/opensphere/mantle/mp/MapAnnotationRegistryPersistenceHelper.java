package io.opensphere.mantle.mp;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import io.opensphere.core.Toolbox;

/**
 * The Interface MapAnnotationRegistryPersistanceHelper.
 *
 * Helps save/load the registry to/from the preferences.
 */
public interface MapAnnotationRegistryPersistenceHelper
{
    /**
     * Loads a {@link MapAnnotationPointGroup} from a file.
     *
     * @param tb the {@link Toolbox}
     * @param aFile the a file
     * @return the map annotation point group or null.
     * @throws IOException Signals that an I/O exception has occurred.
     */
    MapAnnotationPointGroup loadFromFile(Toolbox tb, File aFile) throws IOException;

    /**
     * Load registry from preferences.
     *
     * @param tb the {@link Toolbox}
     * @param registry the {@link MapAnnotationPointRegistry}
     */
    void loadFromPreferences(Toolbox tb, MapAnnotationPointRegistry registry);

    /**
     * Save registry to preferences.
     *
     * @param tb the {@link Toolbox}
     * @param userDefaultPoint the user default point
     * @return true, if successful
     */
    boolean saveDefaultToPreferences(Toolbox tb, MapAnnotationPoint userDefaultPoint);

    /**
     * Saves a MapAnnotationPointGroup to an XML file.
     *
     * @param aFile the a file to save to.
     * @param group the group to save.
     * @throws IOException Signals that an I/O exception has occurred.
     */
    void saveToFile(File aFile, MapAnnotationPointGroup group) throws IOException;

    /**
     * Save registry to preferences.
     *
     * @param tb the {@link Toolbox}
     * @param rootGroups the root groups
     * @return true, if successful
     */
    boolean saveToPreferences(Toolbox tb, Set<MapAnnotationPointGroup> rootGroups);
}
