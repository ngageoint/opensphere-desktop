package io.opensphere.mantle.data.geom.style.dialog;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

import io.opensphere.core.Toolbox;
import io.opensphere.core.util.swing.EventQueueUtilities;
import io.opensphere.mantle.data.VisualizationSupport;
import io.opensphere.mantle.data.geom.style.FeatureVisualizationControlPanel;
import io.opensphere.mantle.data.geom.style.dialog.StyleEditPanelController.StyleEditPanelControllerListener;

/**
 * The Class StyleEditPanel.
 */
public class StyleEditPanel extends JPanel implements StyleEditPanelControllerListener
{
    /**
     * serialVersionUID.
     */
    private static final long serialVersionUID = 1L;

    /** The Accept button. */
    @SuppressWarnings("PMD.SingularField")
    private final JButton myAcceptButton;

    /** The Accept cancel panel. */
    private final JPanel myAcceptCancelPanel;

    /** The Cancel button. */
    @SuppressWarnings("PMD.SingularField")
    private final JButton myCancelButton;

    /** The Center panel. */
    private final JPanel myCenterPanel;

    /** The Data type. */
    @SuppressWarnings("PMD.SingularField")
    private final DataTypeNodeUserObject myDataType;

    /** The Edit panel controller. */
    private final StyleEditPanelController myEditPanelController;

    /** The type of data this panel will hold. */
    private final VisualizationStyleGroup myGroupType;

    /** The Style select tree panel. */
    @SuppressWarnings("PMD.SingularField")
    private final StyleTypeSelectTreePanel myStyleSelectTreePanel;

    /** The Toolbox. */
    @SuppressWarnings("PMD.SingularField")
    private final Toolbox myToolbox;

    /**
     * Instantiates a new style edit panel.
     *
     * @param tb the {@link Toolbox}
     * @param smc the {@link StyleManagerController}
     * @param dataType the data type
     * @param groupType the type of data to be edited on the panel.
     */
    public StyleEditPanel(Toolbox tb, StyleManagerController smc, DataTypeNodeUserObject dataType,
            VisualizationStyleGroup groupType)
    {
        super(new BorderLayout());
        myToolbox = tb;
        myGroupType = groupType;
        myDataType = dataType;
        myEditPanelController = new StyleEditPanelController(myToolbox, smc, dataType);
        myStyleSelectTreePanel = new StyleTypeSelectTreePanel(myToolbox, myEditPanelController, myDataType);
        setBorder(BorderFactory.createLoweredBevelBorder());

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(BorderFactory.createRaisedBevelBorder());
        JLabel topLabel = new JLabel(dataType.getDisplayName());
        topLabel.setHorizontalAlignment(SwingConstants.CENTER);
        topLabel.setFont(topLabel.getFont().deriveFont(Font.BOLD, topLabel.getFont().getSize() + 2));
        topPanel.setMaximumSize(new Dimension(1000, 30));
        topPanel.setMinimumSize(new Dimension(100, 30));
        topPanel.setPreferredSize(new Dimension(100, 30));
        topPanel.add(topLabel, BorderLayout.CENTER);
        add(topPanel, BorderLayout.NORTH);

        add(myStyleSelectTreePanel, BorderLayout.WEST);

        myAcceptCancelPanel = new JPanel(new GridLayout(1, 2, 40, 0));
        myAcceptCancelPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createRaisedBevelBorder(),
                BorderFactory.createEmptyBorder(10, 50, 10, 50)));
        myAcceptButton = new JButton("Accept Changes");
        myAcceptButton.addActionListener(event -> myEditPanelController.acceptChanges());
        myAcceptCancelPanel.add(myAcceptButton);

        myCancelButton = new JButton("Cancel Changes");
        myCancelButton.addActionListener(event -> myEditPanelController.cancelChanges());
        myAcceptCancelPanel.add(myCancelButton);

        myCenterPanel = new JPanel(new BorderLayout());
        JScrollPane centerSP = new JScrollPane(myCenterPanel);

        add(centerSP, BorderLayout.CENTER);
        myEditPanelController.addListener(this);
        EventQueueUtilities.runOnEDT(new Runnable()
        {
            @Override
            public void run()
            {
                switch (myGroupType)
                {
                    case FEATURES:
                        if (!myEditPanelController.getPrimaryFeatureClasses().isEmpty())
                        {
                            Class<? extends VisualizationSupport> cl = myEditPanelController.getPrimaryFeatureClasses().iterator()
                                    .next();
                            StyleNodeUserObject uo = myEditPanelController.getSelectedNodeForFeatureType(cl);
                            if (uo != null)
                            {
                                myEditPanelController.setEditSelectedStyle(uo, true);
                                myStyleSelectTreePanel.ensureLabelSelected(uo);
                            }
                        }
                        break;
                    case TILES:
                        if (!myEditPanelController.getPrimaryTileClasses().isEmpty())
                        {
                            Class<? extends VisualizationSupport> cl = myEditPanelController.getPrimaryTileClasses().iterator()
                                    .next();
                            StyleNodeUserObject uo = myEditPanelController.getSelectedNodeForTileType(cl);
                            if (uo != null)
                            {
                                myEditPanelController.setEditSelectedStyle(uo, true);
                                myStyleSelectTreePanel.ensureLabelSelected(uo);
                            }
                        }
                        break;
                    case HEATMAPS:
                        if (!myEditPanelController.getPrimaryHeatmapClasses().isEmpty())
                        {
                            Class<? extends VisualizationSupport> supportClass = myEditPanelController.getPrimaryHeatmapClasses()
                                    .iterator().next();
                            StyleNodeUserObject styleNodeUserObject = myEditPanelController
                                    .getSelectedNodeForHeatmapType(supportClass);
                            if (styleNodeUserObject != null)
                            {
                                myEditPanelController.setEditSelectedStyle(styleNodeUserObject, true);
                                myStyleSelectTreePanel.ensureLabelSelected(styleNodeUserObject);
                            }

                        }
                        break;
                    default:
                        // fail fast:
                        throw new UnsupportedOperationException("Unable to configure editor for style group type " + myGroupType);

                }
            }
        });
    }

    @Override
    public void lockFromChanges(boolean lock)
    {
        if (lock)
        {
            add(myAcceptCancelPanel, BorderLayout.SOUTH);
        }
        else
        {
            remove(myAcceptCancelPanel);
        }
        revalidate();
    }

    @Override
    public void refreshDisplay()
    {
    }

    @Override
    public void styleEditSelectionChanged(final StyleNodeUserObject styleToEdit,
            final FeatureVisualizationControlPanel editorPanel)
    {
        EventQueueUtilities.runOnEDT(() -> rebuildEditorPane(styleToEdit, editorPanel));
    }

    /**
     * Rebuild editor pane.
     *
     * @param styleToEdit the style to edit
     * @param editorPanel the editor panel
     */
    private void rebuildEditorPane(StyleNodeUserObject styleToEdit, final FeatureVisualizationControlPanel editorPanel)
    {
        myCenterPanel.removeAll();
        if (styleToEdit != null && editorPanel != null)
        {
            myCenterPanel.add(editorPanel.getPanel(), BorderLayout.CENTER);
        }
        myCenterPanel.revalidate();
        myCenterPanel.repaint();
    }
}
