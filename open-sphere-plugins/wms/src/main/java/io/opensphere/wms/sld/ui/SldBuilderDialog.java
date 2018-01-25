package io.opensphere.wms.sld.ui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.xml.bind.JAXBElement;

import org.apache.commons.lang3.StringUtils;

import io.opensphere.wms.config.v1.WMSLayerConfigurationSet;
import io.opensphere.wms.sld.SldRegistry;
import io.opensphere.wms.sld.ui.SldBuilderDeck.PanelType;
import net.opengis.sld._100.FeatureTypeStyle;
import net.opengis.sld._100.NamedLayer;
import net.opengis.sld._100.Rule;
import net.opengis.sld._100.StyledLayerDescriptor;
import net.opengis.sld._100.SymbolizerType;
import net.opengis.sld._100.UserStyle;

/**
 * This class will present the SLD dialog which presents users with the option
 * to create a point, line, or polygon type symbolizer.
 */
public final class SldBuilderDialog extends JDialog
{
    /** Serial. */
    private static final long serialVersionUID = 1L;

    /** The Constant OK_OPTION. */
    public static final String OK_OPTION = "OK_OPTION";

    /** The Constant CANCEL_OPTION. */
    public static final String CANCEL_OPTION = "CANCEL_OPTION";

    /** The top panel. */
    private JPanel myTopPanel;

    /** The sld name field. */
    private JTextField mySldNameField;

    /** The Shape type combo. */
    private JComboBox<PanelType> myShapeTypeCombo;

    /** The Data layer name. */
    private final JLabel myDataLayerTitle = new JLabel();

//    /** The action listeners. */
//    private final ActionListener myActionListener;

    /** The sld registry. */
    private final SldRegistry mySldRegistry;

    /** The Sld deck. */
    private SldBuilderDeck mySldDeck;

    /** The Active panel type. */
    private PanelType myActivePanelType;

    /** The Save cancel panel. */
    private JPanel mySaveCancelPanel;

    /** The ok button. */
    private JButton mySaveButton;

    /** The cancel button. */
    private JButton myCancelButton;

    /** The key that uniquely identifies the layer associated with this SLD. */
    private final String myActiveLayerKey;

    /** The Active sld name. */
//    private String myActiveSLDName;

    /** The Layer config set. */
    private final WMSLayerConfigurationSet myLayerConfigSet;

    /**
     * Instantiates a new sld builder dialog.
     *
     * @param sldRegistry the sld registry
     * @param layerConfigSet the layer config set
     * @param object the object
     */
    public SldBuilderDialog(SldRegistry sldRegistry, WMSLayerConfigurationSet layerConfigSet, Object object)
    {
        super(SwingUtilities.getWindowAncestor(sldRegistry.getToolbox().getUIRegistry().getMainFrameProvider().get()),
                "SLD Builder", ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(HIDE_ON_CLOSE);
        mySldRegistry = sldRegistry;
        myLayerConfigSet = layerConfigSet;
//        myActionListener = listener;
        myActiveLayerKey = layerConfigSet.getLayerConfig().getLayerKey();
        setMinimumSize(new Dimension(400, 600));
        setPreferredSize(new Dimension(400, 600));
        setLocationRelativeTo(mySldRegistry.getToolbox().getUIRegistry().getMainFrameProvider().get());
        myDataLayerTitle.setText(layerConfigSet.getLayerConfig().getLayerTitle());
        setContentPane(getDialogContentPanel());
    }

    /**
     * Edits the sld.
     *
     * @param sld the sld
     */
    public void editSld(StyledLayerDescriptor sld)
    {
        // TODO
    }

    /**
     * Gets the cancel button.
     *
     * @return the cancel button
     */
    public JButton getCancelButton()
    {
        if (myCancelButton == null)
        {
            myCancelButton = new JButton("Cancel");
            myCancelButton.setSize(40, 20);
            myCancelButton.setPreferredSize(myCancelButton.getSize());
            myCancelButton.setMinimumSize(myCancelButton.getSize());
            myCancelButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent arg0)
                {
                    SldBuilderDialog.this.setVisible(false);
                }
            });
        }
        return myCancelButton;
    }

    /**
     * Gets the save button.
     *
     * @return the save button
     */
    public JButton getSaveButton()
    {
        if (mySaveButton == null)
        {
            mySaveButton = new JButton("Save");
            mySaveButton.setSize(40, 20);
            mySaveButton.setPreferredSize(mySaveButton.getSize());
            mySaveButton.setMinimumSize(mySaveButton.getSize());
            mySaveButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent evt)
                {
                    if (evt.getSource().equals(mySaveButton))
                    {
                        boolean valid = true;
                        if (StringUtils.isBlank(getNameField().getText()))
                        {
                            valid = false;
                            JOptionPane.showMessageDialog(SldBuilderDialog.this, "Sld name cannot be blank.",
                                    "Sld Name Empty Error", JOptionPane.ERROR_MESSAGE);
                        }

                        if (mySldRegistry.getSldNamesForLayer(myActiveLayerKey).contains(getNameField().getText()))
                        {
                            valid = false;
                            getNameField().setText("");
                            JOptionPane.showMessageDialog(SldBuilderDialog.this, "Please choose another name for this SLD.",
                                    "Duplicate Sld Name Error", JOptionPane.ERROR_MESSAGE);
                            getNameField().setText("");
                        }

                        if (valid)
                        {
                            JAXBElement<? extends SymbolizerType> st = getSldDeck().getSelectedPanel().validateInputs();
                            if (st != null)
                            {
                                mySldRegistry.addNewSld(myActiveLayerKey, buildSLD(st));
                            }
                        }
                    }
                }
            });
        }
        return mySaveButton;
    }

    /**
     * Gets the save cancel panel.
     *
     * @return the save cancel panel
     */
    public JPanel getSaveCancelPanel()
    {
        if (mySaveCancelPanel == null)
        {
            mySaveCancelPanel = new JPanel(new GridLayout(1, 2, 20, 0));
            mySaveCancelPanel.setBorder(BorderFactory.createEmptyBorder(5, 50, 5, 40));
            mySaveCancelPanel.add(getSaveButton());
            mySaveCancelPanel.add(getCancelButton());
        }
        return mySaveCancelPanel;
    }

    /**
     * Builds the sld.
     *
     * @param symbolizerType the symbolizer type
     * @return the styled layer descriptor
     */
    private StyledLayerDescriptor buildSLD(JAXBElement<? extends SymbolizerType> symbolizerType)
    {
//        net.opengis.sld._100.ObjectFactory factory = new net.opengis.sld._100.ObjectFactory();

        Rule rule = new Rule();
        rule.getSymbolizer().add(symbolizerType);

        FeatureTypeStyle fts = new FeatureTypeStyle();
        fts.getRule().add(rule);

        UserStyle us = new UserStyle();
        us.setName(getNameField().getText());
        us.getFeatureTypeStyle().add(fts);

        NamedLayer nl = new NamedLayer();

        nl.setName(myLayerConfigSet.getLayerConfig().getLayerName());
        nl.getNamedStyleOrUserStyle().add(us);

        StyledLayerDescriptor sld = new StyledLayerDescriptor();
        sld.getNamedLayerOrUserLayer().add(nl);

        return sld;
    }

    /**
     * Gets the dialog's main content panel.
     *
     * @return the dialog content panel
     */
    private JPanel getDialogContentPanel()
    {
        if (myTopPanel == null)
        {
            myTopPanel = new JPanel(new GridBagLayout());
            myTopPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

            GridBagConstraints gbc = new GridBagConstraints();

            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.gridwidth = 2;
            gbc.insets = new Insets(0, 0, 10, 0);
            gbc.weightx = 1.0;

            JPanel namePanel = new JPanel(new GridBagLayout());
            GridBagConstraints nameGBC = new GridBagConstraints();
            nameGBC.anchor = GridBagConstraints.CENTER;
            nameGBC.gridx = 0;
            nameGBC.gridy = 0;
            namePanel.add(myDataLayerTitle, nameGBC);
            myTopPanel.add(namePanel, gbc);

            gbc.fill = GridBagConstraints.NONE;
            gbc.gridy = 1;
            gbc.gridwidth = 1;
            gbc.insets = new Insets(0, 0, 5, 0);
            gbc.weightx = 0;
            myTopPanel.add(new JLabel("Sld Name:"), gbc);

            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.gridx = 1;
            gbc.weightx = 1.0;
            myTopPanel.add(getNameField(), gbc);

            gbc.fill = GridBagConstraints.NONE;
            gbc.gridx = 0;
            gbc.gridy = 2;
            gbc.gridwidth = 1;
            gbc.weightx = 0;
            myTopPanel.add(new JLabel("Shape Type:"), gbc);

            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.gridx = 1;
            gbc.weightx = 1.0;
            myTopPanel.add(getShapeTypeCombo(), gbc);

            gbc.fill = GridBagConstraints.BOTH;
            gbc.gridx = 0;
            gbc.gridy = 3;
            gbc.gridwidth = 2;
            gbc.weighty = 1.0;
            getSldDeck().showPanel(PanelType.POINT);
            myTopPanel.add(getSldDeck(), gbc);

            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.gridy = 4;
            gbc.weighty = 0;
            myTopPanel.add(getSaveCancelPanel(), gbc);
        }
        return myTopPanel;
    }

    /**
     * Gets the name field.
     *
     * @return the name field
     */
    private JTextField getNameField()
    {
        if (mySldNameField == null)
        {
            mySldNameField = new JTextField();
        }
        return mySldNameField;
    }

    /**
     * Gets the shape type combo.
     *
     * @return the shape type combo
     */
    private JComboBox<PanelType> getShapeTypeCombo()
    {
        if (myShapeTypeCombo == null)
        {
            myShapeTypeCombo = new JComboBox<>(PanelType.values());
            myShapeTypeCombo.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent evt)
                {
                    if (evt.getSource().equals(myShapeTypeCombo))
                    {
                        myActivePanelType = (PanelType)myShapeTypeCombo.getSelectedItem();
                        mySldDeck.showPanel(myActivePanelType);
                    }
                }
            });
        }
        return myShapeTypeCombo;
    }

    /**
     * Gets the sld deck.
     *
     * @return the sld deck
     */
    private SldBuilderDeck getSldDeck()
    {
        if (mySldDeck == null)
        {
            mySldDeck = new SldBuilderDeck(mySldRegistry);
        }
        return mySldDeck;
    }

    /**
     * Validate inputs.
     *
     * @return the sld configuration
     */
//    private SldConfiguration validateInputs()
//    {
////        boolean valid = true;
//
//        mySldResult = new SldConfiguration();
//        mySldResult.setName(mySldNameField.getText());
//        mySldResult.setColor(myColor);
//        return mySldResult;
//    }
}
