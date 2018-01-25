package io.opensphere.mantle.data.geom.style.config.v1;

import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.EqualsHelper;

/**
 * The Class StyleManagerConfig.
 */
@XmlRootElement(name = "StyleManagerConfig")
@XmlAccessorType(XmlAccessType.FIELD)
public class StyleManagerConfig
{
    /** The Data type styles. */
    @XmlElement(name = "DataTypeStyles")
    private final List<DataTypeStyleConfig> myDataTypeStyles;

    /** The use custom type keys. */
    @XmlElement(name = "UseCustomTypeKey")
    private final Set<String> myUseCustomTypeKey;

    /**
     * Instantiates a new style manager config.
     */
    public StyleManagerConfig()
    {
        myUseCustomTypeKey = New.set();
        myDataTypeStyles = New.list();
    }

    /**
     * Instantiates a new style manager config.
     *
     * @param other the other
     */
    public StyleManagerConfig(StyleManagerConfig other)
    {
        Utilities.checkNull(other, "other");
        myUseCustomTypeKey = New.set(other.myUseCustomTypeKey);
        myDataTypeStyles = New.list();
        if (other.myDataTypeStyles != null)
        {
            for (DataTypeStyleConfig dts : other.myDataTypeStyles)
            {
                myDataTypeStyles.add(new DataTypeStyleConfig(dts));
            }
        }
    }

    /**
     * Adds the type key overridden.
     *
     * @param typeKey the type key
     */
    public void addUseCustomTypeKey(String typeKey)
    {
        myUseCustomTypeKey.add(typeKey);
    }

    /**
     * Clears the entire configuration.
     */
    public void clear()
    {
        myUseCustomTypeKey.clear();
        myDataTypeStyles.clear();
    }

    /**
     * Searches the list of data type styles and retrieves the first style that
     * matches the type key or null if not found.
     *
     * @param typeKey the type key to search for in the styles. (can not be
     *            null)
     * @return the {@link DataTypeStyleConfig} or null if not found.
     */
    public DataTypeStyleConfig getDataTypeStyle(String typeKey)
    {
        Utilities.checkNull(typeKey, "typeKey");
        DataTypeStyleConfig result = null;
        for (DataTypeStyleConfig cfg : myDataTypeStyles)
        {
            if (cfg != null && EqualsHelper.equals(typeKey, cfg.getDataTypeKey()))
            {
                result = cfg;
                break;
            }
        }
        return result;
    }

    /**
     * Searches the list of data type styles and retrieves the first style that
     * matches the type key or null if not found.
     *
     * @param typeKey the type key to search for in the styles. (can not be
     *            null)
     * @return the {@link DataTypeStyleConfig} or null if not found.
     */
    public DataTypeStyleConfig getDataTypeStyleByTypeKey(String typeKey)
    {
        Utilities.checkNull(typeKey, "typeKey");
        DataTypeStyleConfig result = null;
        for (DataTypeStyleConfig cfg : myDataTypeStyles)
        {
            if (cfg != null && cfg.getDataTypeKey().contains(typeKey))
            {
                result = cfg;
                break;
            }
        }
        return result;
    }

    /**
     * Gets the data type styles.
     *
     * @return the data type styles
     */
    public List<DataTypeStyleConfig> getDataTypeStyles()
    {
        return myDataTypeStyles;
    }

    /**
     * Gets the feature type style for dt key and mgs class name.
     *
     * @param typeKey the type key
     * @param mgsClassName the mgs class name
     * @return the feature type style for dt key and mgs class name
     */
    public FeatureTypeStyleConfig getFeatureTypeStyle(String typeKey, String mgsClassName)
    {
        FeatureTypeStyleConfig result = null;

        DataTypeStyleConfig dtsc = getDataTypeStyle(typeKey);
        if (dtsc != null)
        {
            result = dtsc.getFeatureTypeStyleConfigForMGSBaseClass(mgsClassName);
        }
        return result;
    }

    /**
     * Gets the style parameter set for dti key mgs class and style class.
     *
     * @param typeKey the type key
     * @param mgsClass the mgs class
     * @param styleClass the style class
     * @return the style parameter set for dti key mgs class and style class
     */
    public StyleParameterSetConfig getStyleParameterSet(String typeKey, String mgsClass, String styleClass)
    {
        StyleParameterSetConfig result = null;
        FeatureTypeStyleConfig ftsc = getFeatureTypeStyle(typeKey, mgsClass);
        if (ftsc != null)
        {
            result = ftsc.getStyleParameterSetConfigForStyleClass(styleClass);
        }
        return result;
    }

    /**
     * Gets the overridden type keys.
     *
     * @return the overridden type keys
     */
    public Set<String> getUseCustomTypeKeysSet()
    {
        return myUseCustomTypeKey;
    }

    /**
     * Checks if is type key overridden.
     *
     * @param typeKey the type key
     * @return true, if is type key overridden
     */
    public boolean isUseCustomType(String typeKey)
    {
        return myUseCustomTypeKey.contains(typeKey);
    }

    /**
     * Removes the data type style specific for the type.
     *
     * @param typeKey The key of the type to remove the data type style for.
     */
    public void removeDataTypeStyle(String typeKey)
    {
        int index = 0;
        for (DataTypeStyleConfig cfg : myDataTypeStyles)
        {
            if (cfg != null && EqualsHelper.equals(typeKey, cfg.getDataTypeKey()))
            {
                break;
            }
            index++;
        }

        if (index < myDataTypeStyles.size())
        {
            myDataTypeStyles.remove(index);
        }
    }

    /**
     * Removes the type key overridden.
     *
     * @param typeKey the type key
     * @return true, if successful
     */
    public boolean removeUseCustomTypeKey(String typeKey)
    {
        return myUseCustomTypeKey.remove(typeKey);
    }

    /**
     * Sets or updates the style parameters for a data type, mgs class, and
     * style class. If any of the necessary components do not exist in the
     * hierarchy, they will be created to ensure the operation succeeds.
     *
     * @param update the update
     * @param mgsClass the mgs class
     * @param typeKey the type key
     */
    public void setOrUpdateStyleParameterSet(StyleParameterSetConfig update, String mgsClass, String typeKey)
    {
        FeatureTypeStyleConfig ftsc = getFeatureTypeStyle(typeKey, mgsClass);
        if (ftsc == null)
        {
            DataTypeStyleConfig dtsc = getDataTypeStyle(typeKey);
            if (dtsc == null)
            {
                dtsc = new DataTypeStyleConfig(typeKey);
                getDataTypeStyles().add(dtsc);
            }
            ftsc = new FeatureTypeStyleConfig();
            ftsc.setBaseMGSClassName(mgsClass);
            ftsc.setSelectedStyleClassName(update.getStyleClassName());
            dtsc.getFeatureTypeStyleConfigList().add(ftsc);
        }
        StyleParameterSetConfig oldSet = ftsc.getStyleParameterSetConfigForStyleClass(update.getStyleClassName());
        if (oldSet != null)
        {
            oldSet.getParameterSet().clear();
            oldSet.getParameterSet().addAll(update.getParameterSet());
        }
        else
        {
            ftsc.getStyleParameterSetConfigList().add(update);
        }
    }

    /**
     * Sets the selected style class for feature type style config.
     *
     * @param typeKey the type key
     * @param styleClass the style class
     * @param mgsClass the mgs class
     * @return true, if successful
     */
    public boolean setSelectedStyleClass(String typeKey, String mgsClass, String styleClass)
    {
        boolean success = false;
        FeatureTypeStyleConfig ftsc = getFeatureTypeStyle(typeKey, mgsClass);
        if (ftsc != null)
        {
            ftsc.setSelectedStyleClassName(styleClass);
            success = true;
        }
        return success;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(64);
        sb.append(this.getClass().getSimpleName()).append("\n" + "  UseCustomTypeKey Size[").append(myUseCustomTypeKey.size())
                .append("]\n");

        if (!myUseCustomTypeKey.isEmpty())
        {
            sb.append('\n');
            for (String spc : myUseCustomTypeKey)
            {
                sb.append("    ").append(spc).append('\n');
            }
        }

        sb.append("  DataTypeStyles   Size[").append(myDataTypeStyles.size()).append("]\n");

        if (!myDataTypeStyles.isEmpty())
        {
            sb.append('\n');
            for (DataTypeStyleConfig spc : myDataTypeStyles)
            {
                sb.append(spc.toString()).append("\n\n");
            }
        }
        return sb.toString();
    }

//    public static void main(String[] args)
//    {
//        File aFile = new File("C:\\testStyleManagerConfig.xml");
//        File aFile2 = new File("C:\\testStyleManagerConfig2.xml");
//        StyleManagerConfig cfg = new StyleManagerConfig();
//        cfg.getOverriddenTypeKeys().add("Test");
//        cfg.getOverriddenTypeKeys().add("Boo");
//
//        DataTypeStyleConfig dts = new DataTypeStyleConfig();
//        dts.setDataTypeKey("Test");
//        FeatureTypeStyleConfig fts = new FeatureTypeStyleConfig();
//        fts.setBaseMGSClassName(MapLocationGeometrySupport.class.getName());
//        fts.setSelectedStyleClassName(PointFeatureVisualizationStyle.class.getName());
//        StyleParameterSetConfig pset = new StyleParameterSetConfig();
//        pset.setStyleClassName(PointFeatureVisualizationStyle.class.getName());
//        StyleParameterConfig spc = new StyleParameterConfig("TestKey", Integer.class.getName(), Integer.valueOf(1000).toString());
//        pset.getParameterSet().add(spc);
//        fts.getStyleParameterSetConfigList().add(pset);
//        dts.setFeatureTypeStyleConfigList(Collections.singletonList(fts));
//        cfg.getDataTypeStyles().add(dts);
//
//        cfg.save(aFile);
//
//        StyleManagerConfig cfg2 = new StyleManagerConfig();
//        cfg2.load(aFile);
//        cfg2.save(aFile2);
//
//    }
}
