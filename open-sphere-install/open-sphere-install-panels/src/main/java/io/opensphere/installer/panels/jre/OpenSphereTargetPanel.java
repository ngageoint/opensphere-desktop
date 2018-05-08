package io.opensphere.installer.panels.jre;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.File;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.apache.commons.lang3.StringUtils;

import com.izforge.izpack.api.data.Panel;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.gui.log.Log;
import com.izforge.izpack.installer.data.GUIInstallData;
import com.izforge.izpack.installer.gui.InstallerFrame;
import com.izforge.izpack.panels.target.TargetPanel;
import com.izforge.izpack.util.Platform.Name;

/**
 * An extension to the target panel on which additional information is
 * displayed, and a preview of the install path is displayed.
 */
public class OpenSphereTargetPanel extends TargetPanel
{
    /** The unique identifier used for serialization. */
    private static final long serialVersionUID = -7051346499405134772L;

    /** A label in which text is displayed to the user. */
    private JPanel myBottomInfoArea;

    /** The label on which a preview of the path is displayed. */
    private JLabel myPathPreview;

    /**
     * Creates a new panel.
     *
     * @param panel the panel meta-data
     * @param parent the parent window
     * @param installData the installation data
     * @param resources the resources
     * @param log the log
     */
    public OpenSphereTargetPanel(Panel panel, InstallerFrame parent, GUIInstallData installData, Resources resources, Log log)
    {
        super(panel, parent, installData, resources, log);
    }

    /**
     * {@inheritDoc}
     *
     * @see com.izforge.izpack.panels.path.PathInputPanel#createLayoutBottom()
     */
    @Override
    public void createLayoutBottom()
    {
        String message = getString(getI18nStringForClass("bottom-message", "OpenSphereTargetPanel"));

        GridBagLayout layout = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        myBottomInfoArea = new JPanel(layout);

        JTextArea textArea = new JTextArea();
        textArea.setText(message);
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setBackground(Color.decode("#C6C9CF"));

        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;
        c.gridheight = 1;
        c.weightx = 1;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.NORTH;
        layout.setConstraints(textArea, c);
        myBottomInfoArea.add(textArea);

        JLabel pathLabel = new JLabel("This version will be installed to:");
        c.gridy++;
        c.gridwidth = 1;
        c.weightx = .15;
        c.weighty = 0;
        c.anchor = GridBagConstraints.SOUTHWEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        layout.setConstraints(pathLabel, c);
        myBottomInfoArea.add(pathLabel);

        myPathPreview = new JLabel();
        myPathPreview.setBorder(UIManager.getBorder("TextField.border"));
        c.gridx = 1;
        c.weightx = .85;
        layout.setConstraints(myPathPreview, c);
        myBottomInfoArea.add(myPathPreview);

        // Path Preview listener
        pathSelectionPanel.getPathInputField().getDocument().addDocumentListener(new DocumentListener()
        {
            @Override
            public void removeUpdate(DocumentEvent e)
            {
                updatePreview();
            }

            @Override
            public void insertUpdate(DocumentEvent e)
            {
                updatePreview();
            }

            @Override
            public void changedUpdate(DocumentEvent e)
            {
                // this probably won't be fired according to documentation
                updatePreview();
            }

            private void updatePreview()
            {
                String preview = pathSelectionPanel.getPathInputField().getText();
                if (!preview.endsWith(File.separator))
                {
                    preview += File.separator;
                }
                preview += installData.getVariable("InstallVersion");

                if (installData.getPlatform().getName() == Name.WINDOWS && preview.length() > 2)
                {
                    parent.getNavigator().setNextEnabled(true);
                    if (StringUtils.isBlank(installData.getVariable("INSTALL_DRIVE")))
                    {
                        parent.getNavigator().setNextEnabled(false);
                        preview = "Warning: The application may only be installed to a letter drive. Installing to a network or "
                                + "OneDrive location is not possible.";
                    }
                }

                myPathPreview.setText(preview);
            }
        });

        add(new JScrollPane(myBottomInfoArea), NEXT_LINE);
    }
}
