package io.opensphere.core.util.swing.wizard.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.image.IconUtil;
import io.opensphere.core.util.swing.wizard.model.WizardStepListModel;
import io.opensphere.core.util.swing.wizard.model.WizardStepListModel.StepState;

/**
 * View that displays the wizard steps and allows selection of the current step.
 */
public class WizardStepList extends JPanel
{
    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;

    /** The listener for model changes. */
    private final transient WizardStepListModel.WizardStepListModelChangeListener myChangeListener = new WizardStepListModel.WizardStepListModelChangeListener()
    {
        @Override
        public void currentStepChanged(WizardStepListModel source, int step, String stepTitle)
        {
            mySelectionModel.setSelectionInterval(step, step);
            repaint();
        }

        @Override
        public void stepStateChanged(WizardStepListModel source, int step, String stepTitle, StepState state)
        {
            repaint();
        }
    };

    /** Icon used to indicate the current step. */
    private final Icon myCurrentStateIndicatorIcon = IconUtil.getColorizedIcon("/images/open_gt_green.png", Color.WHITE);

    /** The model. */
    private WizardStepListModel myModel;

    /** Selection model used to disable selection for disabled nodes. */
    private final DefaultListSelectionModel mySelectionModel = new WizardStepSelectionModel();

    /** Map of states to icons. */
    private final Map<StepState, Icon> myStateToIconMap = New.map();

    /** The model for the JList. */
    private final DefaultListModel<String> myListModel = new DefaultListModel<>();

    /**
     * Constructor.
     */
    public WizardStepList()
    {
        super(new BorderLayout());

        JList<String> jList = new JList<>(myListModel);

        jList.setBackground(getBackground());
        jList.setCellRenderer(new WizardStepCellRenderer());
        jList.setSelectionModel(mySelectionModel);
        jList.getSelectionModel().addListSelectionListener(new ListSelectionListener()
        {
            @Override
            public void valueChanged(ListSelectionEvent e)
            {
                myModel.setCurrentStep(((ListSelectionModel)e.getSource()).getAnchorSelectionIndex());
            }
        });

        JLabel stepsLabel = new JLabel("Steps");
        stepsLabel.setFont(
                stepsLabel.getFont().deriveFont(stepsLabel.getFont().getStyle() | Font.BOLD, stepsLabel.getFont().getSize() + 4));
        stepsLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
        add(stepsLabel, BorderLayout.NORTH);
        JScrollPane scrollPane = new JScrollPane(jList);
        add(scrollPane);
    }

    /**
     * Get the model.
     *
     * @return The model.
     */
    public WizardStepListModel getModel()
    {
        return myModel;
    }

    /**
     * Set the icon associated with a state.
     *
     * @param state The state.
     * @param icon The icon.
     */
    public void setIcon(StepState state, Icon icon)
    {
        myStateToIconMap.put(state, icon);
    }

    /**
     * Set the model.
     *
     * @param model The model.
     */
    public void setModel(WizardStepListModel model)
    {
        if (myModel != null)
        {
            myModel.getChangeSupport().removeListener(myChangeListener);
        }
        myModel = Utilities.checkNull(model, "model");
        myModel.getChangeSupport().addListener(myChangeListener);

        for (int index = 0; index < myModel.getStepCount(); ++index)
        {
            myListModel.addElement(myModel.getStepTitle(index));
        }

        mySelectionModel.setSelectionInterval(myModel.getCurrentStep(), myModel.getCurrentStep());
    }

    /**
     * Get the icon to be used for a step state.
     *
     * @param stepState The step state.
     * @return The icon.
     */
    private Icon getIconForStepState(StepState stepState)
    {
        Icon icon = myStateToIconMap.get(stepState);
        if (icon == null)
        {
            switch (stepState)
            {
                case VALID:
                    icon = IconUtil.getColorizedIcon("/images/check_12x12.png", Color.GREEN);
                    break;
                case WARNING:
                    icon = IconUtil.getColorizedIcon("/images/check_12x12.png", Color.YELLOW);
                    break;
                case INVALID:
                    icon = IconUtil.getColorizedIcon("/images/bang_12x12.png", Color.RED);
                    break;
                default:
                    break;
            }
            if (icon != null)
            {
                myStateToIconMap.put(stepState, icon);
            }
        }

        return icon;
    }

    /** The cell renderer for the {@link JList}. */
    private final class WizardStepCellRenderer implements ListCellRenderer<String>
    {
        /** Panel to contain the labels. */
        private final JPanel myPanel;

        /** Label that indicates the state of each step. */
        private final JLabel myStateIconLabel = new JLabel();

        /** Label that indicates the current step. */
        private final JLabel myStepIconLabel = new JLabel();

        /** Label for the step title. */
        private final JLabel myTitleLabel = new JLabel();

        /** Constructor. */
        public WizardStepCellRenderer()
        {
            myStepIconLabel.setPreferredSize(new Dimension(12, 12));
            myStateIconLabel.setPreferredSize(new Dimension(12, 12));

            myPanel = new JPanel(new GridBagLayout());

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);

            myPanel.add(myStepIconLabel, gbc);

            gbc.insets.left = 0;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weightx = 1.;

            myPanel.add(myTitleLabel, gbc);
            gbc.fill = GridBagConstraints.NONE;
            gbc.weightx = 0.;
            myPanel.add(myStateIconLabel, gbc);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends String> list, String value, int index, boolean isSelected,
                boolean cellHasFocus)
        {
            myTitleLabel.setText(value);
            myStepIconLabel.setIcon(isSelected ? myCurrentStateIndicatorIcon : null);

            StepState stepState = myModel.getStepState(index);
            myStateIconLabel.setIcon(getIconForStepState(stepState));
            myTitleLabel.setEnabled(isSelected || stepState != StepState.DISABLED);

            return myPanel;
        }
    }

    /**
     * Selection model that does not allow selection for steps in the
     * {@link StepState#DISABLED} state.
     */
    private final class WizardStepSelectionModel extends DefaultListSelectionModel
    {
        /**
         * Serial version UID.
         */
        private static final long serialVersionUID = 1L;

        @Override
        public void setSelectionInterval(int index0, int index1)
        {
            if (myModel.getCurrentStep() == index0 || myModel.getStepState(index0) != StepState.DISABLED)
            {
                super.setSelectionInterval(index0, index1);
            }
        }
    }
}
