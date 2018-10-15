package io.opensphere.mantle.data.geom.style.impl;

import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import io.opensphere.core.Toolbox;
import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.MapVisualizationStyleCategory;
import io.opensphere.mantle.data.element.MetaDataProvider;
import io.opensphere.mantle.data.geom.MapGeometrySupport;
import io.opensphere.mantle.data.geom.MapLocationGeometrySupport;
import io.opensphere.mantle.data.geom.style.MutableVisualizationStyle;
import io.opensphere.mantle.data.geom.style.ParameterHint;
import io.opensphere.mantle.data.geom.style.VisualizationStyle;
import io.opensphere.mantle.data.geom.style.VisualizationStyleParameter;
import io.opensphere.mantle.data.geom.style.VisualizationStyleParameterFlags;
import io.opensphere.mantle.data.geom.style.impl.ui.AbstractStyleParameterEditorPanel;
import io.opensphere.mantle.data.geom.style.impl.ui.ComboBoxStyleParameterEditorPanel;
import io.opensphere.mantle.data.geom.style.impl.ui.GroupedMiniStyleEditorPanel;
import io.opensphere.mantle.data.geom.style.impl.ui.GroupedStyleParameterEditorPanel;
import io.opensphere.mantle.data.geom.style.impl.ui.PanelBuilder;
import io.opensphere.mantle.data.geom.style.impl.ui.StyleParameterEditorGroupPanel;
import io.opensphere.mantle.data.impl.specialkey.HeadingKey;
import io.opensphere.mantle.data.impl.specialkey.LineOfBearingKey;

/**
 * The Class DynamicEllipseFeatureVisualization.
 */
public class DynamicLOBFeatureVisualization extends AbstractLOBFeatureVisualizationStyle
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(DynamicLOBFeatureVisualization.class);

    /** The Constant ourPropertyKeyPrefix. */
    @SuppressWarnings("hiding")
    public static final String ourPropertyKeyPrefix = "DynamicLOBFeatureVisualization";

    /** The Constant ourLOBOrientationColumnKey. */
    public static final String ourLOBOrientationColumnKey = ourPropertyKeyPrefix + ".LOBOrientationColumnKey";

    /** The Constant ourDefaultLOBOrientationParameter. */
    public static final VisualizationStyleParameter ourDefaultLOBOrientationParameter = new VisualizationStyleParameter(
            ourLOBOrientationColumnKey, "Lob Orientation Column", null, String.class,
            new VisualizationStyleParameterFlags(true, true), ParameterHint.hint(false, true));

    /**
     * Instantiates a new dynamic LOB feature visualization.
     *
     * @param tb the {@link Toolbox}
     */
    public DynamicLOBFeatureVisualization(Toolbox tb)
    {
        super(tb);
    }

    /**
     * Instantiates a new dynamic LOB feature visualization.
     *
     * @param tb the {@link Toolbox}
     * @param dtiKey the dti key
     */
    public DynamicLOBFeatureVisualization(Toolbox tb, String dtiKey)
    {
        super(tb, dtiKey);
    }

    @Override
    public DynamicLOBFeatureVisualization clone()
    {
        return (DynamicLOBFeatureVisualization)super.clone();
    }

    @Override
    public DynamicLOBFeatureVisualization deriveForType(String dtiKey)
    {
        DynamicLOBFeatureVisualization clone = clone();
        clone.setDTIKey(dtiKey);
        clone.initializeFromDataType();
        return clone;
    }

    @Override
    public AppliesTo getAppliesTo()
    {
        return AppliesTo.INDIVIDUAL_ELEMENT;
    }

    @Override
    public Class<? extends MapGeometrySupport> getConvertedClassType()
    {
        return MapLocationGeometrySupport.class;
    }

    /**
     * Gets the lOB orientation column key.
     *
     * @return the lOB orientation column key
     */
    public String getLOBOrientationColumnKey()
    {
        return (String)getStyleParameterValue(ourLOBOrientationColumnKey);
    }

    @Override
    public GroupedMiniStyleEditorPanel getMiniUIPanel()
    {
        GroupedMiniStyleEditorPanel mUIPanel = super.getMiniUIPanel();

        List<AbstractStyleParameterEditorPanel> paramList = New.list();
        MutableVisualizationStyle style = mUIPanel.getChangedStyle();

        DataTypeInfo dti = StyleUtils.getDataTypeInfoFromKey(getToolbox(), style.getDTIKey());
        if (dti != null && dti.getMetaDataInfo() != null && dti.getMetaDataInfo().getKeyCount() > 0)
        {
            paramList.add(new ComboBoxStyleParameterEditorPanel(StyleUtils.createComboBoxMiniPanelBuilder("LOB Column"), style,
                    ourLOBOrientationColumnKey, false, false, true, dti.getMetaDataInfo().getKeyNames()));

            StyleParameterEditorGroupPanel paramGrp = new StyleParameterEditorGroupPanel(null, paramList, false, 1);
            mUIPanel.addGroupAtTop(paramGrp);
        }

        return mUIPanel;
    }

    @Override
    public MapVisualizationStyleCategory getStyleCategory()
    {
        return MapVisualizationStyleCategory.LOCATION_FEATURE;
    }

    @Override
    public String getStyleDescription()
    {
        return "Feature visualization controls for dynamic Line-of-bearing(LOB), where the LOB parameter"
                + " for orientation can be selected from meta-data column values";
    }

    @Override
    public String getStyleName()
    {
        return "Lines of Bearing (Dynamic)";
    }

    @Override
    public GroupedStyleParameterEditorPanel getUIPanel()
    {
        GroupedStyleParameterEditorPanel uiPanel = super.getUIPanel();

        if (getDTIKey() != null)
        {
            MutableVisualizationStyle style = uiPanel.getChangedStyle();
            DataTypeInfo dti = StyleUtils.getDataTypeInfoFromKey(getToolbox(), style.getDTIKey());
            if (dti != null && dti.getMetaDataInfo() != null && dti.getMetaDataInfo().getKeyCount() > 0)
            {
                List<AbstractStyleParameterEditorPanel> paramList = New.list();

                VisualizationStyleParameter param = style.getStyleParameter(ourLOBOrientationColumnKey);
                paramList.add(new ComboBoxStyleParameterEditorPanel(PanelBuilder.get(param.getName()), style,
                        ourLOBOrientationColumnKey, false, false, true, dti.getMetaDataInfo().getKeyNames()));

                StyleParameterEditorGroupPanel paramGrp = new StyleParameterEditorGroupPanel("Dynamic LOB", paramList);
                uiPanel.addGroup(paramGrp);
            }
        }

        return uiPanel;
    }

    @Override
    public void initialize()
    {
        super.initialize();
        setParameter(ourDefaultLOBOrientationParameter);
    }

    @Override
    public void initialize(Set<VisualizationStyleParameter> paramSet)
    {
        super.initialize(paramSet);
        paramSet.stream().filter(p -> p.getKey() != null && p.getKey().startsWith(ourPropertyKeyPrefix))
                .forEach(this::setParameter);
    }

    @Override
    public void initializeFromDataType()
    {
        super.initializeFromDataType();
        if (getDTIKey() != null)
        {
            DataTypeInfo dti = StyleUtils.getDataTypeInfoFromKey(getToolbox(), getDTIKey());
            if (dti != null && dti.getMetaDataInfo() != null)
            {
                String lobOrientKey = dti.getMetaDataInfo().getKeyForSpecialType(LineOfBearingKey.DEFAULT);
                if (lobOrientKey != null)
                {
                    setParameter(ourLOBOrientationColumnKey, lobOrientKey, NO_EVENT_SOURCE);
                }
                else
                {
                    String headingKey = dti.getMetaDataInfo().getKeyForSpecialType(HeadingKey.DEFAULT);
                    if (headingKey != null)
                    {
                        setParameter(ourLOBOrientationColumnKey, headingKey, NO_EVENT_SOURCE);
                    }
                }
            }
        }
    }

    @Override
    public VisualizationStyle newInstance(Toolbox tb)
    {
        VisualizationStyle vs = new DynamicLOBFeatureVisualization(tb);
        vs.initialize();
        return vs;
    }

    /**
     * Sets the lob orientation column key.
     *
     * @param key the key
     * @param source the source
     */
    public void setLOBOrientationColumnKey(String key, Object source)
    {
        StyleUtils.setMetaDataColumnKeyProperty(this, ourLOBOrientationColumnKey, key, source);
    }

    @Override
    public boolean supportsLabels()
    {
        return true;
    }

    @Override
    public Float getLobOrientation(long elementId, MapGeometrySupport mgs, MetaDataProvider mdi)
    {
        Float result = null;
        String ornKey = getLOBOrientationColumnKey();
        if (ornKey != null)
        {
            try
            {
                double ornVal = StyleUtils.convertValueToDouble(mdi.getValue(ornKey));
                result = Float.valueOf((float)ornVal);
            }
            catch (NumberFormatException e)
            {
                result = null;
                LOGGER.error("Error converting LOB ORN from data type" + getDTIKey() + " ElId[" + elementId + "] Values: ORN["
                        + mdi.getValue(ornKey) + "]");
            }
        }
        return result;
    }
}
