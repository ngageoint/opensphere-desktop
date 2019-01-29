package io.opensphere.mantle.data.geom.style.dialog;

import java.util.Set;

import io.opensphere.core.Toolbox;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.concurrent.ProcrastinatingExecutor;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.VisualizationSupport;
import io.opensphere.mantle.data.geom.style.FeatureVisualizationControlPanel;
import io.opensphere.mantle.data.geom.style.FeatureVisualizationControlPanel.FeatureVisualizationControlPanelListener;
import io.opensphere.mantle.data.geom.style.VisualizationStyle;
import io.opensphere.mantle.data.geom.style.VisualizationStyleController;
import io.opensphere.mantle.data.geom.style.VisualizationStyleParameter;
import io.opensphere.mantle.data.geom.style.impl.AbstractVisualizationStyle;
import io.opensphere.mantle.util.MantleToolboxUtils;

/** The controller used to manage the mini-style type panel. */
public class MiniStyleTypePanelController implements FeatureVisualizationControlPanelListener
{
    /** The data group active in the controller. */
    private transient DataGroupInfo myDataGroup;

    /** The data type active in the controller. */
    private transient DataTypeInfo myDataType;

    /** The Feature class. */
    private transient Class<? extends VisualizationSupport> myFeatureClass;

    /** The FVCP. */
    private transient FeatureVisualizationControlPanel myVisualizationControlPanel;

    /** The toolbox through which application state is accessed. */
    private final Toolbox myToolbox;

    /** The Update executor. */
    private final ProcrastinatingExecutor myUpdateExecutor = new ProcrastinatingExecutor("MiniStyleControlPanel:Update", 500);

    /** The controller managing visualization styles. */
    private final VisualizationStyleController myStyleController;

    /**
     * Instantiates a new mini style panel controller.
     *
     * @param toolbox the {@link Toolbox} through which application state is
     *            accessed.
     */
    public MiniStyleTypePanelController(Toolbox toolbox)
    {
        myToolbox = toolbox;
        myStyleController = MantleToolboxUtils.getMantleToolbox(toolbox).getVisualizationStyleController();
    }

    @Override
    public void performLiveParameterUpdate(String dtiKey, Class<? extends VisualizationSupport> convertedClass,
            Class<? extends VisualizationStyle> vsClass, Set<VisualizationStyleParameter> updateSet)
    {
        VisualizationStyle visStyle = null;
        if (dtiKey == null)
        {
            visStyle = MantleToolboxUtils.getMantleToolbox(myToolbox).getVisualizationStyleRegistry()
                    .getDefaultStyle(convertedClass);
        }
        else
        {
            visStyle = MantleToolboxUtils.getMantleToolbox(myToolbox).getVisualizationStyleRegistry().getStyle(convertedClass,
                    dtiKey, false);
        }
        if (visStyle instanceof AbstractVisualizationStyle && Utilities.sameInstance(visStyle.getClass(), vsClass))
        {
            ((AbstractVisualizationStyle)visStyle).setParameters(updateSet, this);
        }
    }

    /**
     * Sets the items.
     *
     * @param visualizationControlPanel the f vcp
     * @param featureClass the feature class
     * @param dgi the dgi
     * @param dti the dti
     */
    public void setItems(FeatureVisualizationControlPanel visualizationControlPanel,
            Class<? extends VisualizationSupport> featureClass, DataGroupInfo dgi, DataTypeInfo dti)
    {
        myFeatureClass = featureClass;
        myVisualizationControlPanel = visualizationControlPanel;
        myDataGroup = dgi;
        myDataType = dti;
    }

    @Override
    public void styleChanged(boolean hasChangesFromBase)
    {
        myUpdateExecutor.execute(() -> myStyleController.updateStyle(myVisualizationControlPanel.getChangedStyle(),
                myFeatureClass, myDataGroup, myDataType, MiniStyleTypePanelController.this));
    }

    @Override
    public void styleChangesAccepted()
    {
        /* intentionally blank */
    }

    @Override
    public void styleChangesCancelled()
    {
        /* intentionally blank */
    }
}
