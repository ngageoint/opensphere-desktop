package io.opensphere.kml.datasource.view;

import java.util.Collection;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;

import io.opensphere.core.util.ChangeListener;
import io.opensphere.core.util.DefaultValidatorSupport;
import io.opensphere.core.util.ObservableValue;
import io.opensphere.core.util.Validatable;
import io.opensphere.core.util.ValidationStatus;
import io.opensphere.core.util.ValidatorSupport;
import io.opensphere.core.util.swing.EventQueueUtilities;
import io.opensphere.core.util.swing.GridBagPanel;
import io.opensphere.core.util.swing.input.FactoryViewPanel;
import io.opensphere.core.util.swing.input.controller.ControllerFactory;
import io.opensphere.core.util.swing.input.model.ViewModel;
import io.opensphere.kml.common.model.KMLDataSource;
import io.opensphere.kml.datasource.model.v1.KMLDataSourceModel;
import io.opensphere.mantle.data.AbstractActivationListener;
import io.opensphere.mantle.data.ActivationListener;

/**
 * KML data source panel.
 */
public class KMLDataSourcePanel extends GridBagPanel implements Validatable
{
    /** The serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The KML data source model. */
    private final transient KMLDataSourceModel myDataSourceModel;

    /** True for new/import data source, false for existing. */
    private final boolean myIsImport;

    /** The label that explains why you can't edit anything. */
    private final JLabel myActiveExplanationLabel;

    /** The validation support. */
    private final transient DefaultValidatorSupport myValidatorSupport = new DefaultValidatorSupport(this);

    /**
     * Listener for changes to the activation state of the data group.
     */
    private final transient ActivationListener myActivationListener = new AbstractActivationListener()
    {
        @Override
        public void commit(io.opensphere.mantle.data.DataGroupActivationProperty property,
                io.opensphere.mantle.data.ActivationState state, io.opensphere.core.util.lang.PhasedTaskCanceller canceller)
        {
            EventQueueUtilities.invokeLater(() ->
            {
                boolean active = property.isActive();
                myDataSourceModel.setEnabled(!active);
                myActiveExplanationLabel.setVisible(active);
            });
        }
    };

    /**
     * Constructor.
     *
     * @param dataSource The data source
     * @param isImport True for new/import data source, false for existing
     * @param disallowedNames The disallowed names
     */
    public KMLDataSourcePanel(KMLDataSource dataSource, boolean isImport, Collection<String> disallowedNames)
    {
        myDataSourceModel = new KMLDataSourceModel(disallowedNames);
        myDataSourceModel.set(dataSource);
        myDataSourceModel.setEnabled(!dataSource.isActive());
        myIsImport = isImport;
        myActiveExplanationLabel = new JLabel("Note: deactivate layer to change settings");

        // Update the GUI when activation state changes
        if (!myIsImport && dataSource.getDataGroupInfo() != null)
        {
            dataSource.getDataGroupInfo().activationProperty().addListener(myActivationListener);
        }

        myValidatorSupport.setValidationResult(myDataSourceModel.getValidationStatus(), myDataSourceModel.getErrorMessage());
        myDataSourceModel.addListener(new ChangeListener<KMLDataSource>()
        {
            @Override
            public void changed(ObservableValue<? extends KMLDataSource> observable, KMLDataSource oldValue,
                    KMLDataSource newValue)
            {
                // Apply changes immediately when in "settings" mode
                if (!myIsImport && myDataSourceModel.getValidationStatus() == ValidationStatus.VALID)
                {
                    myDataSourceModel.applyChanges();
                }

                myValidatorSupport.setValidationResult(myDataSourceModel.getValidationStatus(),
                        myDataSourceModel.getErrorMessage());
            }
        });

        buildGUI();
    }

    /**
     * Gets the data source model.
     *
     * @return The data source model
     */
    public KMLDataSourceModel getDataSourceModel()
    {
        return myDataSourceModel;
    }

    @Override
    public ValidatorSupport getValidatorSupport()
    {
        return myValidatorSupport;
    }

    /**
     * Builds the panel.
     */
    private void buildGUI()
    {
        assert SwingUtilities.isEventDispatchThread();

        anchorWest().setWeightx(1);

        if (myIsImport)
        {
            fillHorizontal().addRow(buildBasicPanel());
        }
        else
        {
            setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

            addRow(buildAdvancedPanel());

            myActiveExplanationLabel.setVisible(myDataSourceModel.get().isActive());
            setInsets(8, 0, 0, 0).addRow(myActiveExplanationLabel);
        }

        fillVerticalSpace();
    }

    /**
     * Builds the basic panel.
     *
     * @return The basic panel
     */
    private FactoryViewPanel buildBasicPanel()
    {
        FactoryViewPanel panel = new FactoryViewPanel();
        JComponent pathComponent = addField(panel, myDataSourceModel.getPath());
        if (pathComponent instanceof JTextComponent)
        {
            ((JTextComponent)pathComponent).setEditable(false);
        }
        addField(panel, myDataSourceModel.getSourceName());
        return panel;
    }

    /**
     * Builds the advanced panel.
     *
     * @return The advanced panel
     */
    private FactoryViewPanel buildAdvancedPanel()
    {
        FactoryViewPanel panel = new FactoryViewPanel();
        if (myIsImport)
        {
            addField(panel, myDataSourceModel.getIncludeInTimeline());
        }
        JPanel refreshPanel = buildRefreshPanel();
        panel.addLabelComponent(ControllerFactory.createLabel(myDataSourceModel.getAutoRefresh(), refreshPanel.getComponent(0)),
                refreshPanel);
        addField(panel, myDataSourceModel.getPointType());
        addField(panel, myDataSourceModel.getFeatureAltitude());
        addField(panel, myDataSourceModel.getPolygonFill());
        addField(panel, myDataSourceModel.getScalingMethod());
        addField(panel, myDataSourceModel.getShowLabels());
        return panel;
    }

    /**
     * Builds the refresh panel.
     *
     * @return The refresh panel
     */
    private GridBagPanel buildRefreshPanel()
    {
        GridBagPanel panel = new GridBagPanel();
        panel.add(createComponent(myDataSourceModel.getAutoRefresh()));
        panel.add(Box.createHorizontalStrut(4));
        JComponent refreshRateComponent = createComponent(myDataSourceModel.getRefreshRate());
        panel.add(ControllerFactory.createLabel(myDataSourceModel.getRefreshRate(), refreshRateComponent));
        panel.fillHorizontal();
        panel.add(refreshRateComponent);
        return panel;
    }

    /**
     * Adds a field model to the given panel.
     *
     * @param panel The panel
     * @param model The model
     * @return The view component
     */
    private JComponent addField(FactoryViewPanel panel, ViewModel<?> model)
    {
        JComponent component = createComponent(model);
        panel.addLabelComponent(ControllerFactory.createLabel(model, component), component);
        return component;
    }

    /**
     * Creates a component from the model.
     *
     * @param model The model
     * @return The view component
     */
    private JComponent createComponent(ViewModel<?> model)
    {
        JComponent comp = ControllerFactory.createComponent(model, null);
        if (comp instanceof JCheckBox)
        {
            ((JCheckBox)comp).setText(null);
        }
        return comp;
    }
}
