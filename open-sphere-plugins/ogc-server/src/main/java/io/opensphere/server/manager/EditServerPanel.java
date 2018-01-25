package io.opensphere.server.manager;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import io.opensphere.core.Toolbox;
import io.opensphere.core.util.DefaultValidatorSupport;
import io.opensphere.core.util.Validatable;
import io.opensphere.core.util.ValidationStatus;
import io.opensphere.core.util.ValidatorSupport;
import io.opensphere.core.util.ValidatorSupport.ValidationStatusChangeListener;
import io.opensphere.core.util.swing.ButtonPanel;
import io.opensphere.core.util.swing.DialogPanel;
import io.opensphere.core.util.swing.GridBagPanel;
import io.opensphere.mantle.datasources.IDataSource;
import io.opensphere.server.display.ServerSourceEditor;
import io.opensphere.server.toolbox.ServerSourceController;
import io.opensphere.server.toolbox.ServerSourceControllerManager;
import io.opensphere.server.toolbox.ServerToolbox;
import io.opensphere.server.toolbox.ServerToolboxUtils;

/**
 * The panel that contains the server type combo box and editor panels.
 */
public class EditServerPanel extends JPanel implements DialogPanel, Validatable, ValidationStatusChangeListener
{
    /** Default serial version UID. */
    private static final long serialVersionUID = 1L;

    /** Manager that maintains server controllers for different server types. */
    private final transient ServerSourceControllerManager myServerCtrlManager;

    /** The currently selected controller. */
    private transient ServerSourceController myCurrentController;

    /** The data source being edited. */
    private transient IDataSource myDataSource;

    /** Whether the data source is new. */
    private boolean myIsNew;

    /** Center panel that gets set dynamically based on the selected type. */
    private final JPanel myMainPanel = new JPanel(new BorderLayout());

    /** The server type combo box. */
    private final JComboBox<ComboBoxElement> myServerTypesCombo = new JComboBox<>();

    /** The validation support. */
    private final transient DefaultValidatorSupport myValidatorSupport = new DefaultValidatorSupport(this);

    /** The toolbox through which application state is accessed. */
    private final Toolbox myToolbox;

    /**
     * Instantiates a configuration panel for a new server.
     *
     * @param toolbox the toolbox through which application state is accessed.
     * @param serverCtrlManager the server controller manager
     */
    public EditServerPanel(Toolbox toolbox, ServerSourceControllerManager serverCtrlManager)
    {
        super(new BorderLayout());
        myToolbox = toolbox;
        myServerCtrlManager = serverCtrlManager;
        add(getTopPanel(), BorderLayout.NORTH);
        add(myMainPanel, BorderLayout.CENTER);
    }

    @Override
    public boolean accept()
    {
        boolean allowClose = true;
        if (myCurrentController != null)
        {
            allowClose = myCurrentController.accept();
        }
        return allowClose;
    }

    @Override
    public void cancel()
    {
    }

    @Override
    public Collection<? extends AbstractButton> getContentButtons()
    {
        return Collections.emptyList();
    }

    @Override
    public Collection<String> getDialogButtonLabels()
    {
        return ButtonPanel.OK_CANCEL;
    }

    @Override
    public String getTitle()
    {
        // This is set externally
        return null;
    }

    @Override
    public ValidatorSupport getValidatorSupport()
    {
        return myValidatorSupport;
    }

    /**
     * Sets the server data source.
     *
     * @param source the server data source
     * @param serverType the server type
     */
    public void setServerSource(IDataSource source, String serverType)
    {
        myDataSource = source;
        myIsNew = source == null;

        // Set the server type. This will fire an event that changes the editor
        // and sets the data source model in the editor.
        if (serverType != null)
        {
            myServerTypesCombo.setSelectedItem(new ComboBoxElement(serverType, null));
        }
        else
        {
            myServerTypesCombo.setSelectedIndex(0);
        }

        myServerTypesCombo.setEnabled(source == null);
    }

    @Override
    public void statusChanged(Object object, ValidationStatus valid, String message)
    {
        // Send the message from the inner editor up the chain
        myValidatorSupport.setValidationResult(valid, message);
    }

    /**
     * Gets the top panel.
     *
     * @return the top panel
     */
    private JPanel getTopPanel()
    {
        GridBagPanel topPanel = new GridBagPanel();

        myServerTypesCombo.setFocusable(false);
        for (ServerSourceController ctrl : myServerCtrlManager.getControllers())
        {
            for (String typeName : ctrl.getTypeNames())
            {
                myServerTypesCombo.addItem(new ComboBoxElement(typeName, ctrl));
            }
        }
        myServerTypesCombo.addActionListener(new ActionListener()
        {
            @SuppressWarnings("rawtypes")
            @Override
            public void actionPerformed(ActionEvent e)
            {
                if (((JComboBox)e.getSource()).getSelectedItem() instanceof ComboBoxElement)
                {
                    ComboBoxElement element = (ComboBoxElement)((JComboBox)e.getSource()).getSelectedItem();
                    switchServerType(element.getType(), element.getController());
                }
                else
                {
                    switchServerType(null, null);
                }
            }
        });
        myServerTypesCombo.setSelectedItem(null);

        topPanel.setInsets(0, 0, 6, 6);
        topPanel.add(new JLabel("Type:"));
        topPanel.setInsets(0, 0, 6, 0);
        topPanel.add(myServerTypesCombo);
        topPanel.fillHorizontalSpace();

        return topPanel;
    }

    /**
     * Sets the content of the main panel to the supplied component.
     *
     * @param component the new main panel contents
     */
    private void setMainPanel(Component component)
    {
        myMainPanel.removeAll();
        if (component != null)
        {
            myMainPanel.add(component, BorderLayout.CENTER);
        }
        myMainPanel.revalidate();
        myMainPanel.repaint();
    }

    /**
     * Switches the server type to the new controller.
     *
     * @param typeName the server type name
     * @param controller the new controller
     */
    private void switchServerType(String typeName, ServerSourceController controller)
    {
        // Remove validation listener for old editor
        if (myCurrentController != null)
        {
            myCurrentController.getCurrentSourceEditor().getValidatorSupport().removeValidationListener(this);
        }

        // Use the new controller
        myCurrentController = controller;

        if (myCurrentController != null)
        {
            myCurrentController.setCurrentTypeName(typeName);

            // Set the data source model in the editor
            List<IDataSource> otherSources = myCurrentController.getSourceList();
            if (myIsNew)
            {
                myDataSource = myCurrentController.createNewSource(typeName);
            }
            else
            {
                otherSources.remove(myDataSource);
            }
            myCurrentController.getCurrentSourceEditor().openSource(myDataSource, myIsNew, otherSources);

            ServerSourceEditor editor = myCurrentController.getCurrentSourceEditor();

            // Switch in the new editor
            setMainPanel(editor.getEditor());

            // Add validation listener for new editor
            editor.getValidatorSupport().addAndNotifyValidationListener(this);
        }
        else
        {
            setMainPanel(null);
            myValidatorSupport.setValidationResult(ValidationStatus.ERROR, null);
        }
    }

    /**
     * Holder class for elements that are added to the local combo box.
     */
    private class ComboBoxElement
    {
        /** The controller to use when building the selected type's UI. */
        private final ServerSourceController myController;

        /** The label to display in the combo box. */
        private final String myLabel;

        /** Brief description of the server's implementation type. */
        private final String myType;

        /**
         * Constructor.
         *
         * @param type String that identifies the server type
         * @param controller the controller that handles server sources of the
         *            specified type
         */
        protected ComboBoxElement(String type, ServerSourceController controller)
        {
            myType = type;

            ServerToolbox serverToolbox = ServerToolboxUtils.getServerToolbox(myToolbox);
            myLabel = serverToolbox.getServerLabelGenerator().buildLabelFromType(type);
            myController = controller;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj)
            {
                return true;
            }
            if (obj == null)
            {
                return false;
            }
            if (getClass() != obj.getClass())
            {
                return false;
            }
            ComboBoxElement other = (ComboBoxElement)obj;
            return myType == null ? other.myType == null : myType.equalsIgnoreCase(other.myType);
        }

        @Override
        public int hashCode()
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + (myType == null ? 0 : myType.hashCode());
            return result;
        }

        @Override
        public String toString()
        {
            return myLabel;
        }

        /**
         * Gets the controller.
         *
         * @return the controller
         */
        protected ServerSourceController getController()
        {
            return myController;
        }

        /**
         * Gets the string that identifies the server type.
         *
         * @return the server type string
         */
        protected String getType()
        {
            return myType;
        }
    }
}
