package io.opensphere.core.util.swing.wizard.view;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;

import io.opensphere.core.util.DefaultValidatorSupport;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.Validatable;
import io.opensphere.core.util.ValidationStatus;
import io.opensphere.core.util.ValidatorSupport.ValidationStatusChangeListener;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.image.IconUtil;
import io.opensphere.core.util.swing.OptionDialog;
import io.opensphere.core.util.swing.wizard.model.WizardPanelModel;
import io.opensphere.core.util.swing.wizard.model.WizardStepListModel;
import io.opensphere.core.util.swing.wizard.model.WizardStepListModel.StepState;
import io.opensphere.core.util.swing.wizard.model.WizardStepListModelChangeAdapter;

/**
 * A dialog that leads the user through a series of steps.
 */
public class WizardDialog
{
    /** Label for the back button. */
    public static final String BACK_BUTTON_LABEL = "< Back";

    /** Label for the cancel button. */
    public static final String CANCEL_BUTTON_LABEL = "Cancel";

    /** Label for the finish button. */
    public static final String FINISH_BUTTON_LABEL = "Finish";

    /** Label for the next button. */
    public static final String NEXT_BUTTON_LABEL = "Next >";

    /**
     * The center panel, which contains the view for the current step in the
     * wizard.
     */
    private final JPanel myCenterPanel = new JPanel(new CardLayout());

    /** The dialog that presents the wizard. */
    private final OptionDialog myDialog;

    /** The model for the wizard panels. */
    private WizardPanelModel myPanelModel;

    /**
     * Listener for validation on wizard panels.
     */
    private final ValidationStatusChangeListener myPanelValidationListener = (object, valid, message) ->
    {
        if (Utilities.sameInstance(myWizardPanels.get(myStepListModel.getCurrentStepTitle()), object))
        {
            myValidator.setValidationResult(valid, message);
        }
        for (int step = 0; step < myStepListModel.getStepCount(); ++step)
        {
            String stepTitle = myStepListModel.getStepTitle(step);
            if (Utilities.sameInstance(myWizardPanels.get(stepTitle), object))
            {
                if (valid == ValidationStatus.VALID)
                {
                    myStepListModel.setStepState(stepTitle, StepState.VALID);
                }
                else
                {
                    StepState stepState = myStepListModel.getStepState(step);
                    if (stepState == StepState.VALID || stepState == StepState.WARNING || stepState == StepState.INVALID
                            || myStepListModel.getCurrentStep() == step)
                    {
                        if (valid == ValidationStatus.ERROR)
                        {
                            myStepListModel.setStepState(stepTitle, StepState.INVALID);
                        }
                        else
                        {
                            myStepListModel.setStepState(stepTitle, StepState.WARNING);
                        }
                    }
                }

                break;
            }
        }
    };

    /**
     * Listener for changes to the step list model.
     */
    private final WizardStepListModel.WizardStepListModelChangeListener myStepListModelChangeListener = new WizardStepListModelChangeAdapter()
    {
        @Override
        public void currentStepChanged(WizardStepListModel source, int step, String stepTitle)
        {
            showCurrentStepPanel(stepTitle);
        }
    };

    /** The model for the list of steps. */
    private WizardStepListModel myStepListModel;

    /** The panel containing the wizard steps. */
    private final WizardStepList myStepPanel;

    /**
     * The validation support for the wizard dialog.
     */
    private final DefaultValidatorSupport myValidator = new DefaultValidatorSupport(null);

    /** Map of step titles to wizard panels. */
    private final Map<String, Component> myWizardPanels = New.map();

    /** The minimum size. */
    private Dimension myMinimumSize;

    /**
     * Constructor.
     *
     * @param parent The parent component, used to position the dialog.
     * @param title The title for the dialog.
     * @param modality The modality type for the dialog.
     */
    public WizardDialog(Component parent, String title, Dialog.ModalityType modality)
    {
        myStepPanel = new WizardStepList();
        myStepPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));

        myDialog = new OptionDialog(parent, buildTopContainer(), title);
        myDialog.setModalityType(modality);
        myDialog.setValidator(myValidator);

        myDialog.setButtonLabels(Arrays.asList(BACK_BUTTON_LABEL, NEXT_BUTTON_LABEL, FINISH_BUTTON_LABEL, CANCEL_BUTTON_LABEL));
        myDialog.setDisposingButtonLabels(Arrays.asList(FINISH_BUTTON_LABEL, CANCEL_BUTTON_LABEL));
        IconUtil.setIcons(getFinishButton(), "/images/check_12x12.png");

        getNextButton().setMnemonic('N');
        getBackButton().setMnemonic('B');
    }

    /**
     * Add a listener to be notified of dialog button actions.
     *
     * @param actionListener The listener.
     */
    public void addActionListener(ActionListener actionListener)
    {
        myDialog.getDialogButtonPanel().addActionListener(actionListener);
    }

    /**
     * Sets the minimum size of the dialog.
     *
     * @param minimumSize the minimum size
     */
    public void setMinimumSize(Dimension minimumSize)
    {
        myMinimumSize = minimumSize;
    }

    /**
     * Build and show the dialog.
     */
    public void buildAndShow()
    {
        myDialog.build();
        if (myMinimumSize != null)
        {
            myDialog.setMinimumSize(myMinimumSize);
        }
        myDialog.showDialog();
    }

    /**
     * Get the 'back' button.
     *
     * @return The 'back' button.
     */
    public JButton getBackButton()
    {
        return myDialog.getDialogButtonPanel().getButton(BACK_BUTTON_LABEL);
    }

    /**
     * Get the cancel button.
     *
     * @return The cancel button.
     */
    public final JButton getCancelButton()
    {
        return myDialog.getDialogButtonPanel().getButton(CANCEL_BUTTON_LABEL);
    }

    /**
     * Get the AWT Component for the wizard.
     *
     * @return The component.
     */
    public Component getComponent()
    {
        return myDialog;
    }

    /**
     * Get the finish button.
     *
     * @return The finish button.
     */
    public final JButton getFinishButton()
    {
        return myDialog.getDialogButtonPanel().getButton(FINISH_BUTTON_LABEL);
    }

    /**
     * Get the 'next' button.
     *
     * @return The 'next' button.
     */
    public JButton getNextButton()
    {
        return myDialog.getDialogButtonPanel().getButton(NEXT_BUTTON_LABEL);
    }

    /**
     * Access the panel that displays the list of steps.
     *
     * @return The step panel.
     */
    public WizardStepList getStepPanel()
    {
        return myStepPanel;
    }

    /**
     * Set the models for the wizard step list and the main panel. If any of the
     * panels provided by the {@code panelModel} are {@link Validatable}, their
     * validation statuses will be used to set the corresponding statuses in the
     * {@code stepModel}, and validation error messages for the current panel
     * will be displayed on the wizard dialog.
     *
     * @param stepListModel A model for the wizard step list.
     * @param panelModel A model for the wizard panels.
     */
    public final void setModels(WizardStepListModel stepListModel, WizardPanelModel panelModel)
    {
        Utilities.checkNull(stepListModel, "stepListModel");
        myPanelModel = Utilities.checkNull(panelModel, "panelModel");

        if (myStepListModel != null)
        {
            myStepListModel.getChangeSupport().removeListener(myStepListModelChangeListener);
        }

        myStepListModel = stepListModel;
        stepListModel.getChangeSupport().addListener(myStepListModelChangeListener);

        myStepPanel.setModel(stepListModel);

        for (Component wizardPanel : myCenterPanel.getComponents())
        {
            if (wizardPanel instanceof Validatable)
            {
                ((Validatable)wizardPanel).getValidatorSupport().removeValidationListener(myPanelValidationListener);
            }
        }
        myCenterPanel.removeAll();
        myWizardPanels.clear();
        for (String stepTitle : stepListModel.getStepTitles())
        {
            Component wizardPanel = panelModel.getWizardPanel(stepTitle);
            myWizardPanels.put(stepTitle, wizardPanel);
            myCenterPanel.add(wizardPanel, stepTitle);
            if (wizardPanel instanceof Validatable)
            {
                ((Validatable)wizardPanel).getValidatorSupport().addAndNotifyValidationListener(myPanelValidationListener);
            }
        }
    }

    /**
     * Show the panel for the current step according to the step list panel.
     *
     * @param currentStepTitle The current step title.
     */
    protected void showCurrentStepPanel(String currentStepTitle)
    {
        Component oldPanel = myWizardPanels.get(currentStepTitle);
        Component newPanel = myPanelModel.getWizardPanel(currentStepTitle);

        if (!Utilities.sameInstance(oldPanel, newPanel))
        {
            myCenterPanel.remove(oldPanel);
            myCenterPanel.add(newPanel, currentStepTitle);
            myWizardPanels.put(currentStepTitle, newPanel);

            if (oldPanel instanceof Validatable)
            {
                ((Validatable)oldPanel).getValidatorSupport().removeValidationListener(myPanelValidationListener);
            }
            if (newPanel instanceof Validatable)
            {
                ((Validatable)newPanel).getValidatorSupport().addAndNotifyValidationListener(myPanelValidationListener);
            }
        }
        else if (newPanel instanceof Validatable)
        {
            myPanelValidationListener.statusChanged(newPanel, ((Validatable)newPanel).getValidatorSupport().getValidationStatus(),
                    ((Validatable)newPanel).getValidatorSupport().getValidationMessage());
        }

        ((CardLayout)myCenterPanel.getLayout()).show(myCenterPanel, currentStepTitle);
        for (Component comp : myCenterPanel.getComponents())
        {
            if (comp.isVisible() && comp instanceof Validatable)
            {
                myValidator.setValidationResult(((Validatable)comp).getValidatorSupport());
                break;
            }
        }
    }

    /**
     * Build the top-level container.
     *
     * @return The container.
     */
    private Container buildTopContainer()
    {
        Container topPanel = new JPanel(new BorderLayout());
        topPanel.add(myStepPanel, BorderLayout.WEST);
        topPanel.add(myCenterPanel, BorderLayout.CENTER);
        return topPanel;
    }
}
