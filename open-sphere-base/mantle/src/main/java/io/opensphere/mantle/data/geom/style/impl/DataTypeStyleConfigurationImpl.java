package io.opensphere.mantle.data.geom.style.impl;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.data.VisualizationSupport;
import io.opensphere.mantle.data.geom.style.DataTypeStyleConfiguration;
import io.opensphere.mantle.data.geom.style.VisualizationStyle;
import io.opensphere.mantle.data.geom.style.VisualizationStyleUtilities;

/**
 * The Class DataTypeStyleConfigurationImpl.
 */
public class DataTypeStyleConfigurationImpl implements DataTypeStyleConfiguration
{
    /** The DTI key. */
    private final String myDTIKey;

    /** The Style class to style instance map. */
    private final Map<Class<? extends VisualizationSupport>, VisualizationStyle> myFeatureClassToStyleInstanceMap;

    /**
     * Instantiates a new data group style configuration impl.
     *
     * @param dtiKey the dti key
     */
    public DataTypeStyleConfigurationImpl(String dtiKey)
    {
        Utilities.checkNull(dtiKey, "dtiId");
        myDTIKey = dtiKey;
        myFeatureClassToStyleInstanceMap = New.map();
    }

    @Override
    public void clear()
    {
        synchronized (myFeatureClassToStyleInstanceMap)
        {
            myFeatureClassToStyleInstanceMap.clear();
        }
    }

    @Override
    public String getDTIKey()
    {
        return myDTIKey;
    }

    @Override
    public Set<Class<? extends VisualizationSupport>> getFeatureTypes()
    {
        Set<Class<? extends VisualizationSupport>> result = New.set();
        synchronized (myFeatureClassToStyleInstanceMap)
        {
            result.addAll(myFeatureClassToStyleInstanceMap.keySet());
        }
        return result.isEmpty() ? Collections.<Class<? extends VisualizationSupport>>emptySet()
                : Collections.unmodifiableSet(result);
    }

    @Override
    public Set<Class<? extends VisualizationSupport>> getMGSClasses()
    {
        Set<Class<? extends VisualizationSupport>> resultSet = null;
        synchronized (myFeatureClassToStyleInstanceMap)
        {
            resultSet = New.set(myFeatureClassToStyleInstanceMap.keySet());
        }
        return resultSet.isEmpty() ? Collections.<Class<? extends VisualizationSupport>>emptySet()
                : Collections.unmodifiableSet(resultSet);
    }

    @Override
    public VisualizationStyle getStyle(Class<? extends VisualizationSupport> mgsClass)
    {
        VisualizationStyle vs = null;
        synchronized (myFeatureClassToStyleInstanceMap)
        {
            vs = VisualizationStyleUtilities.searchForItemByMGSClass(myFeatureClassToStyleInstanceMap, mgsClass);
        }
        return vs;
    }

    @Override
    public Set<VisualizationStyle> getStyles()
    {
        Set<VisualizationStyle> resultSet = null;
        synchronized (myFeatureClassToStyleInstanceMap)
        {
            resultSet = New.set(myFeatureClassToStyleInstanceMap.values());
        }
        return resultSet.isEmpty() ? Collections.<VisualizationStyle>emptySet() : Collections.unmodifiableSet(resultSet);
    }

    @Override
    public boolean hasStyle(Class<? extends VisualizationSupport> mgsClass)
    {
        return getStyle(mgsClass) != null;
    }

    @Override
    public VisualizationStyle removeStyle(Class<? extends VisualizationSupport> mgsClass)
    {
        VisualizationStyle removed = null;
        synchronized (myFeatureClassToStyleInstanceMap)
        {
            removed = myFeatureClassToStyleInstanceMap.remove(mgsClass);
        }
        return removed;
    }

    @Override
    public VisualizationStyle removeStyle(VisualizationStyle style)
    {
        VisualizationStyle removed = null;
        synchronized (myFeatureClassToStyleInstanceMap)
        {
            Class<? extends VisualizationSupport> foundKey = null;
            for (Map.Entry<Class<? extends VisualizationSupport>, VisualizationStyle> entry : myFeatureClassToStyleInstanceMap
                    .entrySet())
            {
                if (Utilities.sameInstance(entry.getValue(), style))
                {
                    foundKey = entry.getKey();
                    break;
                }
            }
            if (foundKey != null)
            {
                removed = myFeatureClassToStyleInstanceMap.remove(foundKey);
            }
        }
        return removed;
    }

    @Override
    public void setStyle(Class<? extends VisualizationSupport> mgsClass, VisualizationStyle style)
    {
        Utilities.checkNull(mgsClass, "mgsClass");
        synchronized (myFeatureClassToStyleInstanceMap)
        {
            myFeatureClassToStyleInstanceMap.put(mgsClass, style);
        }
    }
}
