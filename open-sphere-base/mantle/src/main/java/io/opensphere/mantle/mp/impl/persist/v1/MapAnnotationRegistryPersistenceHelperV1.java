package io.opensphere.mantle.mp.impl.persist.v1;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import javax.xml.bind.JAXBException;

import io.opensphere.core.Toolbox;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.XMLUtilities;
import io.opensphere.mantle.mp.MapAnnotationPoint;
import io.opensphere.mantle.mp.MapAnnotationPointGroup;
import io.opensphere.mantle.mp.MapAnnotationPointRegistry;
import io.opensphere.mantle.mp.MapAnnotationRegistryPersistenceHelper;
import io.opensphere.mantle.mp.impl.DefaultMapAnnotationPoint;
import io.opensphere.mantle.mp.impl.DefaultMapAnnotationPointGroup;

/**
 * The Class MapAnnotationRegistryPersistenceHelperV1.
 */
public class MapAnnotationRegistryPersistenceHelperV1 implements MapAnnotationRegistryPersistenceHelper
{
    /** The Constant REGISTRY_PREFS_KEY. */
    public static final String REGISTRY_PREFS_KEY = "MapAnnotationPointRegistry";

    /** The Constant USER_DEFAULT_PREFS_KEY. */
    public static final String USER_DEFAULT_PREFS_KEY = "UserDefaultMapAnnotationPoint";

    /**
     * Instantiates a new map annotation registry persistence helper v1.
     */
    public MapAnnotationRegistryPersistenceHelperV1()
    {
    }

    @Override
    public MapAnnotationPointGroup loadFromFile(Toolbox tb, File aFile) throws IOException
    {
        MapAnnotationPointGroup result = null;
        try
        {
            JAXBMapAnnotationPointGroup rootGroup = XMLUtilities.readXMLObject(aFile, JAXBMapAnnotationPointGroup.class);
            if (rootGroup != null)
            {
                result = new DefaultMapAnnotationPointGroup(tb, rootGroup);
            }
        }
        catch (JAXBException e)
        {
            throw new IOException(e);
        }
        return result;
    }

    @Override
    public void loadFromPreferences(Toolbox tb, MapAnnotationPointRegistry registry)
    {
        Utilities.checkNull(tb, "tb");
        Utilities.checkNull(registry, "registry");
        JAXBMapAnnotationPointGroup rootGroup = tb.getPreferencesRegistry()
                .getPreferences(MapAnnotationRegistryPersistenceHelper.class)
                .getJAXBObject(JAXBMapAnnotationPointGroup.class, REGISTRY_PREFS_KEY, null);
        if (rootGroup != null)
        {
            List<MapAnnotationPointGroup> groupSet = rootGroup.getChildren();
            if (groupSet != null && !groupSet.isEmpty())
            {
                for (MapAnnotationPointGroup group : groupSet)
                {
                    DefaultMapAnnotationPointGroup rootNode = new DefaultMapAnnotationPointGroup(tb, group);
                    registry.addRootGroup(rootNode, this);
                }
            }
        }
        JAXBMapAnnotationPoint userDefaultPoint = tb.getPreferencesRegistry()
                .getPreferences(MapAnnotationRegistryPersistenceHelper.class)
                .getJAXBObject(JAXBMapAnnotationPoint.class, USER_DEFAULT_PREFS_KEY, null);
        if (userDefaultPoint != null)
        {
            registry.setUserDefaultPoint(new DefaultMapAnnotationPoint(userDefaultPoint), this);
        }
    }

    @Override
    public boolean saveDefaultToPreferences(Toolbox tb, MapAnnotationPoint userDefaultPoint)
    {
        Utilities.checkNull(tb, "tb");
        Utilities.checkNull(userDefaultPoint, "userDefaultPoint");
        JAXBMapAnnotationPoint jaxbPoint = new JAXBMapAnnotationPoint(userDefaultPoint);
        tb.getPreferencesRegistry().getPreferences(MapAnnotationRegistryPersistenceHelper.class)
                .putJAXBObject(USER_DEFAULT_PREFS_KEY, jaxbPoint, false, this);
        return true;
    }

    @Override
    public void saveToFile(File aFile, MapAnnotationPointGroup group) throws IOException
    {
        JAXBMapAnnotationPointGroup groupToWrite = new JAXBMapAnnotationPointGroup(group);
        try
        {
            XMLUtilities.writeXMLObject(groupToWrite, aFile);
        }
        catch (JAXBException e)
        {
            throw new IOException(e);
        }
    }

    @Override
    public boolean saveToPreferences(Toolbox tb, Set<MapAnnotationPointGroup> rootGroups)
    {
        Utilities.checkNull(tb, "tb");
        Utilities.checkNull(rootGroups, "rootGroups");
        JAXBMapAnnotationPointGroup rootGroup = new JAXBMapAnnotationPointGroup("ROOT");
        if (rootGroups != null && !rootGroups.isEmpty())
        {
            for (MapAnnotationPointGroup group : rootGroups)
            {
                rootGroup.addChild(new JAXBMapAnnotationPointGroup(group));
            }
        }
        tb.getPreferencesRegistry().getPreferences(MapAnnotationRegistryPersistenceHelper.class).putJAXBObject(REGISTRY_PREFS_KEY,
                rootGroup, false, this);

        return true;
    }
}
