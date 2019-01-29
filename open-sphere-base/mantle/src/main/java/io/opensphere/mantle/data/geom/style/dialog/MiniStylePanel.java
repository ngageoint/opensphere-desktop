package io.opensphere.mantle.data.geom.style.dialog;

import java.util.List;
import java.util.Objects;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import io.opensphere.core.Toolbox;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.swing.EventQueueUtilities;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.VisualizationSupport;
import io.opensphere.mantle.data.geom.style.VisualizationStyleController;
import io.opensphere.mantle.data.geom.style.VisualizationStyleDatatypeChangeEvent;
import io.opensphere.mantle.data.geom.style.VisualizationStyleRegistry.VisualizationStyleRegistryChangeListener;
import io.opensphere.mantle.data.geom.style.impl.VisualizationStyleRegistryChangeAdapter;
import io.opensphere.mantle.util.MantleToolboxUtils;

/** The panel on which styles are modified within the layer manager. */
public class MiniStylePanel extends JPanel
{
    /** The unique identifier used for serialization operations. */
    private static final long serialVersionUID = 4705566945403309012L;

    /** The currently selected data group. */
    private final transient DataGroupInfo myDataGroup;

    /**
     * The currently selected data type. Package visibility to avoid synthetic
     * accessor.
     */
    final transient DataTypeInfo myDataType;

    /**
     * The Enable custom type check box. Package visibility to avoid synthetic
     * accessor.
     */
    final JCheckBox myEnableCustomTypeCheckBox;

    /** The Registry listener. */
    private final transient VisualizationStyleRegistryChangeListener myRegistryListener = new VisualizationStyleRegistryChangeAdapter()
    {
        @Override
        public void visualizationStyleDatatypeChanged(VisualizationStyleDatatypeChangeEvent event)
        {
            if ((event.getDTIKey() == null || Objects.equals(myDataType.getTypeKey(), event.getDTIKey()))
                    && myEnableCustomTypeCheckBox.isSelected() == event.isNewIsDefaultStyle())
            {
                rebuildUI();
            }
        }
    };

    /** The toolbox through which application state is accessed. */
    private final transient Toolbox myToolbox;

    /** The Type panels. */
    private final List<MiniStyleTypePanel> myTypePanels;

    /** The controller used for visualization styles. */
    private final VisualizationStyleController myVisualizationStyleController;

    /**
     * Instantiates a new mini style panel.
     *
     * @param toolbox the toolbox through which application state is accessed.
     * @param dataGroup the selected data group.
     * @param dataType the selected data type.
     */
    public MiniStylePanel(Toolbox toolbox, DataGroupInfo dataGroup, DataTypeInfo dataType)
    {
        super();
        myToolbox = toolbox;
        myTypePanels = New.list();
        myDataGroup = dataGroup;
        myDataType = dataType;

        myVisualizationStyleController = MantleToolboxUtils.getMantleToolbox(myToolbox).getVisualizationStyleController();

        myEnableCustomTypeCheckBox = new JCheckBox("Enable Custom Style", false);
        myEnableCustomTypeCheckBox.setBorder(null);
        myEnableCustomTypeCheckBox.addActionListener(e ->
        {
            myVisualizationStyleController.setUseCustomStyleForDataType(myDataGroup, myDataType,
                    myEnableCustomTypeCheckBox.isSelected(), MiniStylePanel.this);
            rebuildUI();
        });

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    }

    @Override
    public void addNotify()
    {
        super.addNotify();
        rebuildUI();
        MantleToolboxUtils.getMantleToolbox(myToolbox).getVisualizationStyleRegistry()
                .addVisualizationStyleRegistryChangeListener(myRegistryListener);
    }

    @Override
    public void removeNotify()
    {
        super.removeNotify();
        MantleToolboxUtils.getMantleToolbox(myToolbox).getVisualizationStyleRegistry()
                .removeVisualizationStyleRegistryChangeListener(myRegistryListener);
        destroy();
    }

    /** Destroy. */
    private void destroy()
    {
        removeAll();
        if (myTypePanels != null && !myTypePanels.isEmpty())
        {
            myTypePanels.forEach(p -> p.destroy());
            myTypePanels.clear();
        }
    }

    /** Rebuild ui. */
    protected void rebuildUI()
    {
        EventQueueUtilities.runOnEDT(() ->
        {
            destroy();

            Box hBox = Box.createHorizontalBox();
            hBox.add(myEnableCustomTypeCheckBox);
            hBox.add(Box.createHorizontalGlue());
            hBox.add(Box.createHorizontalStrut(5));
            add(hBox);

            boolean shouldBeSelected = myVisualizationStyleController.isTypeUsingCustom(myDataGroup, myDataType);
            if (shouldBeSelected != myEnableCustomTypeCheckBox.isSelected())
            {
                myEnableCustomTypeCheckBox.setSelected(shouldBeSelected);
            }

            if (myEnableCustomTypeCheckBox.isSelected())
            {
                List<Class<? extends VisualizationSupport>> featureClasses = StyleManagerUtils
                        .getDefaultFeatureClassesForType(myDataType);
                if (CollectionUtilities.hasContent(featureClasses))
                {
                    boolean isFirst = true;
                    for (Class<? extends VisualizationSupport> fc : featureClasses)
                    {
                        MiniStyleTypePanel mstp = new MiniStyleTypePanel(myToolbox, fc, myDataGroup, myDataType, isFirst);
                        myTypePanels.add(mstp);
                        add(mstp);
                        isFirst = false;
                    }
                }
            }

            revalidate();
            repaint();
        });
    }
}
