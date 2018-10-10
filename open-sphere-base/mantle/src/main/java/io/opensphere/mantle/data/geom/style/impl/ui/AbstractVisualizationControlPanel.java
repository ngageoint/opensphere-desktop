package io.opensphere.mantle.data.geom.style.impl.ui;

import java.util.Set;

import javax.swing.JPanel;

import io.opensphere.core.util.WeakChangeSupport;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.swing.EventQueueUtilities;
import io.opensphere.mantle.data.VisualizationSupport;
import io.opensphere.mantle.data.geom.style.FeatureVisualizationControlPanel;
import io.opensphere.mantle.data.geom.style.MutableVisualizationStyle;
import io.opensphere.mantle.data.geom.style.VisualizationStyle;
import io.opensphere.mantle.data.geom.style.VisualizationStyleParameter;
import io.opensphere.mantle.data.geom.style.VisualizationStyleParameterChangeEvent;
import io.opensphere.mantle.data.geom.style.VisualizationStyleParameterChangeListener;
import io.opensphere.mantle.data.geom.style.VisualizationStyleUtilities;

/**
 * The Class AbstractVisualizationControlPanel.
 *
 */
public abstract class AbstractVisualizationControlPanel extends JPanel
        implements FeatureVisualizationControlPanel, VisualizationStyleParameterChangeListener
{
    /**
     * serialVersionUID.
     */
    private static final long serialVersionUID = 1L;

    /** The Change support. */
    private final WeakChangeSupport<FeatureVisualizationControlPanelListener> myChangeSupport;

    /** The Live update previewable. */
    private final boolean myLiveUpdatePreviewableParameters;

    /** The Style. */
    private final MutableVisualizationStyle myStyle;

    /** The Style copy. */
    private MutableVisualizationStyle myStyleCopy;

    /** The Visibility dependencies. */
    private final Set<EditorPanelVisibilityDependency> myVisibilityDependencies;

    /**
     * Instantiates a new abstract visualization control panel.
     *
     * @param visualizationStyle the visualization style
     */
    public AbstractVisualizationControlPanel(MutableVisualizationStyle visualizationStyle)
    {
        this(visualizationStyle, false);
    }

    /**
     * Instantiates a new abstract visualization control panel.
     *
     * @param visualizationStyle the visualization style
     * @param liveUpdatePreviwable the live update previwable parameters
     */
    public AbstractVisualizationControlPanel(MutableVisualizationStyle visualizationStyle, boolean liveUpdatePreviwable)
    {
        super();
        myLiveUpdatePreviewableParameters = liveUpdatePreviwable;
        myStyle = visualizationStyle;
        myStyle.addStyleParameterChangeListener(this);
        myStyleCopy = (MutableVisualizationStyle)myStyle.clone();
        myStyleCopy.addStyleParameterChangeListener(this);
        myVisibilityDependencies = New.set();
        myChangeSupport = new WeakChangeSupport<>();
    }

    @Override
    public final void addListener(FeatureVisualizationControlPanelListener listener)
    {
        myChangeSupport.addListener(listener);
    }

    /**
     * Adds the visibility dependency.
     *
     * @param dependency the dependency
     */
    public void addVisibilityDependency(EditorPanelVisibilityDependency dependency)
    {
        myVisibilityDependencies.add(dependency);
    }

    @Override
    public void applyChanges()
    {
        Set<VisualizationStyleParameter> params = getChangedParameters();
        if (params != null && !params.isEmpty())
        {
            myStyle.setParameters(params, this);
            fireAcceptCancel(true);
        }
    }

    @Override
    public void cancelChanges()
    {
        myStyleCopy.removeStyleParameterChangeListener(this);
        myStyleCopy = (MutableVisualizationStyle)myStyle.clone();
        myStyleCopy.addStyleParameterChangeListener(this);
        update();
        fireAcceptCancel(false);
    }

    @Override
    public Set<VisualizationStyleParameter> getChangedParameters()
    {
        return VisualizationStyleUtilities.getChangedParameters(myStyle, myStyleCopy);
    }

    @Override
    public final MutableVisualizationStyle getChangedStyle()
    {
        return myStyleCopy;
    }

    @Override
    public JPanel getPanel()
    {
        return this;
    }

    @Override
    public final MutableVisualizationStyle getStyle()
    {
        return myStyle;
    }

    @Override
    public boolean hasChanges()
    {
        return VisualizationStyleUtilities.hasChangedParameters(myStyle, myStyleCopy);
    }

    @Override
    public final boolean isUpdateWithPreviewable()
    {
        return myLiveUpdatePreviewableParameters;
    }

    @Override
    public final void removeListener(FeatureVisualizationControlPanelListener listener)
    {
        myChangeSupport.removeListener(listener);
    }

    @Override
    public void styleParametersChanged(VisualizationStyleParameterChangeEvent evt)
    {
        if (evt.getStyle() == myStyle)
        {
            if (evt.getSource() != this)
            {
                Set<VisualizationStyleParameter> changedParams = evt.getChangedParameterSet();
                if (changedParams != null && !changedParams.isEmpty())
                {
                    myStyleCopy.setParameters(changedParams, evt.getSource());
                }
            }
        }
        else
        {
            if (myLiveUpdatePreviewableParameters)
            {
                Set<VisualizationStyleParameter> changedParams = evt.getChangedParameterSet();
                if (changedParams != null && !changedParams.isEmpty())
                {
                    Set<VisualizationStyleParameter> updateSet = New.set();
                    for (VisualizationStyleParameter param : changedParams)
                    {
                        if (param.getHint() != null && param.getHint().isRenderPropertyChangeOnly())
                        {
                            updateSet.add(param);
                        }
                    }

                    if (!updateSet.isEmpty())
                    {
                        String dtiKey = myStyle.getDTIKey();
                        Class<? extends VisualizationSupport> convertedClass = myStyle.getConvertedClassType();
                        Class<? extends VisualizationStyle> vsClass = myStyle.getClass();
                        performLiveParameterUpdate(dtiKey, convertedClass, vsClass, updateSet);
                    }
                }
            }
            EventQueueUtilities.runOnEDT(() ->
            {
                update();
                fireControlPanelStyleChanged();
            });
        }
    }

    /**
     * Update.
     */
    public abstract void update();

    /**
     * Update sync.
     */
    public void updateSync()
    {
        EventQueueUtilities.runOnEDT(() ->
        {
            update();
            if (CollectionUtilities.hasContent(myVisibilityDependencies))
            {
                for (EditorPanelVisibilityDependency dep : myVisibilityDependencies)
                {
                    dep.evaluateStyle();
                }
            }
        });
    }

    /**
     * Fire accept cancel.
     *
     * @param accept the accept
     */
    protected void fireAcceptCancel(final boolean accept)
    {
        myChangeSupport.notifyListeners(listener ->
        {
            if (accept)
            {
                listener.styleChangesAccepted();
            }
            else
            {
                listener.styleChangesCancelled();
            }
        });
    }

    /**
     * Fire control panel style changed.
     */
    protected void fireControlPanelStyleChanged()
    {
        final boolean hasChanges = hasChanges();
        myChangeSupport.notifyListeners(listener -> listener.styleChanged(hasChanges));
    }

    /**
     * Perform live parameter update.
     *
     * @param dtiKey the dti key
     * @param convertedClass the converted class
     * @param vsClass the vs class
     * @param updateSet the update set
     */
    private void performLiveParameterUpdate(final String dtiKey, final Class<? extends VisualizationSupport> convertedClass,
            final Class<? extends VisualizationStyle> vsClass, final Set<VisualizationStyleParameter> updateSet)
    {
        myChangeSupport.notifyListeners(listener -> listener.performLiveParameterUpdate(dtiKey, convertedClass, vsClass, updateSet));
    }
}
