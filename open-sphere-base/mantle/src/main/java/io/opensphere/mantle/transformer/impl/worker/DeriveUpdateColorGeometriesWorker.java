package io.opensphere.mantle.transformer.impl.worker;

import java.awt.Color;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import io.opensphere.core.geometry.renderproperties.BaseRenderProperties;
import io.opensphere.core.geometry.renderproperties.ColorRenderProperties;
import io.opensphere.core.geometry.renderproperties.DefaultZOrderRenderProperties;
import io.opensphere.core.geometry.renderproperties.PointRenderProperties;
import io.opensphere.core.geometry.renderproperties.ZOrderRenderProperties;
import io.opensphere.core.util.Utilities;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.MapVisualizationInfo;
import io.opensphere.mantle.data.element.VisualizationState;
import io.opensphere.mantle.util.MantleConstants;
import io.opensphere.mantle.util.MantleToolboxUtils;

/**
 * The Class DeriveUpdateColorGeometriesWorker.
 */
public class DeriveUpdateColorGeometriesWorker extends AbstractDeriveUpdateGeometriesWorker
{
    /** The Default vs. */
    private final VisualizationState myDefaultVS = new VisualizationState(true);

    /** The Id to vs map. */
    private final Map<Long, VisualizationState> myIdToVSMap;

    /**
     * Alter z order if necessary.
     *
     * @param isSelected the is selected
     * @param dti the {@link DataTypeInfo}
     * @param brp the {@link BaseRenderProperties}
     */
    protected static void alterZOrderIfNecessary(boolean isSelected, DataTypeInfo dti, BaseRenderProperties brp)
    {
        if (brp instanceof DefaultZOrderRenderProperties)
        {
            MapVisualizationInfo vi = dti == null ? null : dti.getMapVisualizationInfo();
            int zOrder = isSelected ? ZOrderRenderProperties.TOP_Z : vi == null ? 0 : vi.getZOrder();
            if (zOrder != brp.getZOrder())
            {
                DefaultZOrderRenderProperties drp = (DefaultZOrderRenderProperties)brp;
                drp.setZOrder(zOrder);
                drp.setRenderingOrder(isSelected ? 1 : 0);
            }
        }
    }

    /**
     * Instantiates a new derive update color geometries worker.
     *
     * @param provider the provider
     * @param idSet the id set
     */
    public DeriveUpdateColorGeometriesWorker(DataElementTransformerWorkerDataProvider provider, List<Long> idSet)
    {
        super(provider, idSet);
        myIdToVSMap = new HashMap<>(idSet.size());
    }

    @Override
    public void cleanup()
    {
    }

    /**
     * Determine color.
     *
     * @param currentGeomColor the current geom color
     * @param vs the vs
     * @return the color
     */
    public Color determineColor(Color currentGeomColor, VisualizationState vs)
    {
        Color toSetTo = vs.isSelected() ? MantleConstants.SELECT_COLOR : vs.getColor();
        return toSetTo.equals(currentGeomColor) ? currentGeomColor : toSetTo;
    }

    @Override
    public BaseRenderProperties getAlteredRenderProperty(Long id, BaseRenderProperties brp)
    {
        VisualizationState vs = myIdToVSMap.get(id);
        vs = vs == null ? myDefaultVS : vs;
        alterZOrderIfNecessary(vs != null && vs.isSelected(), getProvider().getDataType(), brp);

        if (brp instanceof PointRenderProperties)
        {
            PointRenderProperties dprp = (PointRenderProperties)brp;
            Color c = determineColor(dprp.getColor(), vs);
            if (!Utilities.sameInstance(c, dprp.getColor()))
            {
                dprp.setColor(c);
            }
        }
        else if (brp instanceof ColorRenderProperties)
        {
            ColorRenderProperties dcrp = (ColorRenderProperties)brp;
            Color c = determineColor(dcrp.getColor(), vs);
            if (!Utilities.sameInstance(c, dcrp.getColor()))
            {
                dcrp.setColor(c);
            }
        }
        return brp;
    }

    @Override
    public void retrieveNecessaryData()
    {
        List<VisualizationState> vsList = MantleToolboxUtils.getMantleToolbox(getProvider().getToolbox()).getDataElementCache()
                .getVisualizationStates(getIdsOfInterest());
        Iterator<VisualizationState> vsItr = vsList.iterator();
        for (Long id : getIdsOfInterest())
        {
            myIdToVSMap.put(id, vsItr.next());
        }
    }
}
