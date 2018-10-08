package io.opensphere.core.security;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;

import javax.swing.Box;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import io.opensphere.core.SecurityManager;
import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.core.util.DefaultValidatorSupport;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.Validatable;
import io.opensphere.core.util.ValidationStatus;
import io.opensphere.core.util.ValidatorSupport;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.image.IconUtil;
import io.opensphere.core.util.image.IconUtil.IconType;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.core.util.security.PrivateKeyProvider;
import io.opensphere.core.util.security.PrivateKeyProviderException;
import io.opensphere.core.util.security.PrivateKeyProviderFilter;
import io.opensphere.core.util.swing.EventQueueUtilities;
import io.opensphere.core.util.swing.GridBagPanel;
import io.opensphere.core.util.swing.IconButton;

/**
 * Basic Certificate panel.
 */
@SuppressWarnings("PMD.GodClass")
public class CertificateSelectionPanel extends GridBagPanel implements Validatable
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(CertificateSelectionPanel.class);

    /** Minus icon. */
    private static final ImageIcon MINUS_ICON = new ImageIcon(CertificateSelectionPanel.class.getResource("/images/minus.gif"));

    /** Plus icon. */
    private static final ImageIcon PLUS_ICON = new ImageIcon(CertificateSelectionPanel.class.getResource("/images/plus.gif"));

    /** The serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** Indicates if new certificates can be added. */
    private final boolean myAllowAdd;

    /** The certificate combo box. */
    private JComboBox<PrivateKeyProvider> myCertificateComboBox;

    /** The certificate entry panel. */
    private CertificateEntryPanel myCertificateEntryPanel;

    /** The new cert button. */
    private JButton myNewButton;

    /** The preferences registry. */
    private final transient PreferencesRegistry myPreferencesRegistry;

    /**
     * If not {@code null}, filter the certificates that can be selected to ones
     * that match this filter.
     */
    private final transient PrivateKeyProviderFilter myProviderFilter;

    /** The system security manager. */
    private final transient SecurityManager mySecurityManager;

    /** The validation support. */
    private final transient DefaultValidatorSupport myValidatorSupport = new DefaultValidatorSupport(this);

    /** Label that displays a warning to the user. */
    private JLabel myWarningLabel;

    /**
     * Constructor.
     *
     * @param securityManager The system security manager.
     * @param prefsRegistry The preferences registry.
     * @param providerFilter If not {@code null}, filter the certificates that
     *            can be selected to ones that satisfy this filter.
     * @param preferred The preferred private key provider.
     * @param allowAdd Indicates if new certificates may be added.
     */
    public CertificateSelectionPanel(SecurityManager securityManager, PreferencesRegistry prefsRegistry,
            PrivateKeyProviderFilter providerFilter, PrivateKeyProvider preferred, boolean allowAdd)
    {
        mySecurityManager = Utilities.checkNull(securityManager, "securityManager");
        myPreferencesRegistry = Utilities.checkNull(prefsRegistry, "prefsRegistry");
        myProviderFilter = providerFilter;
        myAllowAdd = allowAdd;
        initialize(preferred);
    }

    /**
     * Adds an <code>ActionListener</code> to the panel.
     *
     * @param l the <code>ActionListener</code> to be added
     */
    public void addActionListener(ActionListener l)
    {
        myCertificateComboBox.addActionListener(l);
    }

    /**
     * Get the selected {@link PrivateKeyProvider}.
     *
     * @return The {@link PrivateKeyProvider}.
     */
    public final PrivateKeyProvider getSelectedItem()
    {
        return (PrivateKeyProvider)myCertificateComboBox.getSelectedItem();
    }

    @Override
    public ValidatorSupport getValidatorSupport()
    {
        return myValidatorSupport;
    }

    /**
     * Sets whether the panel is editable.
     *
     * @param editable Whether the panel is editable
     */
    public void setEditable(boolean editable)
    {
        myNewButton.setEnabled(editable);
        myCertificateComboBox.setEnabled(editable);
    }

    /**
     * Sets the selected {@link PrivateKeyProvider}.
     *
     * @param provider The {@link PrivateKeyProvider}.
     */
    public void setSelectedItem(PrivateKeyProvider provider)
    {
        if (provider != null)
        {
            myCertificateComboBox.setSelectedItem(provider);
        }
    }

    /**
     * Handle the user importing certificates.
     */
    protected final void handleAddCertificate()
    {
        boolean keepPrompting = true;
        while (keepPrompting)
        {
            try
            {
                int option = JOptionPane.showOptionDialog(this, myCertificateEntryPanel, "Add New Certificate",
                        JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);
                if (option == JOptionPane.OK_OPTION)
                {
                    try
                    {
                        Collection<? extends PrivateKeyProvider> newProviders = myCertificateEntryPanel.getPrivateKeyProviders();
                        Collection<String> alreadyAdded = New.collection();
                        for (PrivateKeyProvider provider : newProviders)
                        {
                            if (mySecurityManager.addPrivateKeyProvider(provider))
                            {
                                Collection<? extends PrivateKeyProvider> privateKeyProviders = mySecurityManager
                                        .getPrivateKeyProviders();
                                myCertificateComboBox.setModel(new DefaultComboBoxModel<>(
                                        privateKeyProviders.toArray(new PrivateKeyProvider[privateKeyProviders.size()])));
                                if (myCertificateComboBox.getItemCount() == 0 || myProviderFilter == null
                                        || myProviderFilter.isSatisfied(provider))
                                {
                                    myCertificateComboBox.setSelectedItem(provider);
                                }
                            }
                            else
                            {
                                alreadyAdded.add(provider.getAlias());
                            }
                        }
                        if (alreadyAdded.size() == 1)
                        {
                            JOptionPane.showMessageDialog(this, "The certificate with alias "
                                    + CollectionUtilities.getItem(alreadyAdded, 0) + " has already been added.");
                        }
                        else if (alreadyAdded.size() > 1)
                        {
                            JOptionPane.showMessageDialog(this,
                                    "The certificates with aliases " + alreadyAdded + " have already been added.");
                        }
                        else if (!newProviders.isEmpty())
                        {
                            keepPrompting = false;
                        }
                    }
                    catch (PrivateKeyProviderException e)
                    {
                        LOGGER.warn("Could not get certificate from provider: " + e, e);
                    }
                }
                else
                {
                    keepPrompting = false;
                }
            }
            finally
            {
                myCertificateEntryPanel.clearPassword();
            }
        }
    }

    /**
     * Get the combo box renderer that will render incompatible certificates in
     * red.
     *
     * @param listCellRenderer The default renderer.
     * @return The renderer.
     */
    private ListCellRenderer<PrivateKeyProvider> getComboBoxRenderer(
            final ListCellRenderer<? super PrivateKeyProvider> listCellRenderer)
    {
        return (JList<? extends PrivateKeyProvider> list, PrivateKeyProvider value, int index, boolean isSelected,
                boolean cellHasFocus) ->
        {
            Component component = listCellRenderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            try
            {
                PrivateKeyProvider pkp = value;
                if (myProviderFilter != null && !myProviderFilter.isSatisfied(pkp))
                {
                    setForeground(Color.RED);
                }
            }
            catch (PrivateKeyProviderException e)
            {
                LOGGER.warn("Could not get certificate from provider: " + e, e);
            }
            return component;
        };
    }

    /**
     * Get the action listener for the expand/collapse button.
     *
     * @param certificateDetailsScrollPane The details scroll pane.
     * @param expandButton The expand button.
     * @return The action listener.
     */
    private ActionListener getExpandCollapseActionListener(final JScrollPane certificateDetailsScrollPane,
            final JButton expandButton)
    {
        return e ->
        {
            Window window = SwingUtilities.getWindowAncestor(this);
            if (certificateDetailsScrollPane.isVisible())
            {
                expandButton.setIcon(PLUS_ICON);
                certificateDetailsScrollPane.setVisible(false);
                window.setSize(window.getWidth(), window.getHeight() - 200);
            }
            else
            {
                expandButton.setIcon(MINUS_ICON);
                certificateDetailsScrollPane.setVisible(true);
                window.setSize(window.getWidth(), window.getHeight() + 200);
            }
        };
    }

    /**
     * Guess which provider the user wants.
     *
     * @param preferred The preferred provider from the preferences.
     * @param privateKeyProviders The available providers.
     */
    private void guessProvider(PrivateKeyProvider preferred, Collection<? extends PrivateKeyProvider> privateKeyProviders)
    {
        boolean selected = false;
        try
        {
            if (preferred != null && (myProviderFilter == null || myProviderFilter.isSatisfied(preferred)))
            {
                myCertificateComboBox.setSelectedItem(preferred);
                selected = true;
            }
        }
        catch (PrivateKeyProviderException e)
        {
            LOGGER.warn("Could not get certificate from provider: " + e, e);
        }

        if (!selected)
        {
            for (PrivateKeyProvider privateKeyProvider : privateKeyProviders)
            {
                try
                {
                    if (myProviderFilter == null || myProviderFilter.isSatisfied(privateKeyProvider))
                    {
                        myCertificateComboBox.setSelectedItem(privateKeyProvider);
                        break;
                    }
                }
                catch (PrivateKeyProviderException e)
                {
                    LOGGER.warn("Could not get certificate from provider: " + e, e);
                }
            }
        }
    }

    /**
     * Initializes the panel.
     *
     * @param preferred The optional preferred private key provider.
     */
    private void initialize(PrivateKeyProvider preferred)
    {
        Collection<? extends PrivateKeyProvider> privateKeyProviders = mySecurityManager.getPrivateKeyProviders();

        myCertificateComboBox = new JComboBox<>(
                privateKeyProviders.toArray(new PrivateKeyProvider[privateKeyProviders.size()]));
        myCertificateComboBox.setRenderer(getComboBoxRenderer(myCertificateComboBox.getRenderer()));

        JTextArea certificateDetails = new JTextArea();
        certificateDetails.setEditable(false);
        certificateDetails.setBackground(getBackground());

        final JScrollPane certificateDetailsScrollPane = new JScrollPane(certificateDetails);
        certificateDetailsScrollPane.setPreferredSize(new Dimension(200, 200));
        certificateDetailsScrollPane.setVisible(false);

        final JButton expandButton = new JButton(PLUS_ICON);
        expandButton.setMargin(new Insets(0, 0, 0, 0));
        expandButton.addActionListener(getExpandCollapseActionListener(certificateDetailsScrollPane, expandButton));
        expandButton.setBorder(null);

        JPanel expandPanel = new JPanel();
        expandPanel.add(expandButton);
        expandPanel.add(new JLabel("Details"));

        myWarningLabel = new JLabel();
        myWarningLabel.setForeground(Color.RED);

        myCertificateComboBox.addActionListener(new ComboBoxActionListener(certificateDetails, certificateDetailsScrollPane));

        guessProvider(preferred, privateKeyProviders);

        setWarningLabel(getSelectedItem());

        myCertificateEntryPanel = new CertificateEntryPanel(myPreferencesRegistry, mySecurityManager.getCipherFactory());

        if (myAllowAdd)
        {
            myNewButton = new IconButton();
            IconUtil.setIcons(myNewButton, IconType.PLUS, Color.GREEN);
            myNewButton.setMargin(new Insets(6, 6, 6, 6));
            myNewButton.addActionListener(e -> handleAddCertificate());
        }

        GridBagPanel certPanel = new GridBagPanel();
        certPanel.fillHorizontal();
        certPanel.add(myCertificateComboBox);
        if (myAllowAdd)
        {
            certPanel.fillNone();
            certPanel.add(Box.createHorizontalStrut(4));
            certPanel.add(myNewButton);
        }

        // Build the actual panel
        style("label").anchorWest().setWeighty(0.);
        style("input").fillHorizontal().setInsets(0, 3, 0, 0).setWeighty(0);
        style("inputFill").anchorNorth().fillBoth().setInsets(0, 3, 5, 0).setGridwidth(2);
        style("label", "input").addRow(new JLabel("Certificate:"), certPanel);
        style("label").addRow(expandPanel);
        style("inputFill").addRow(certificateDetailsScrollPane);
        style("label", "input").addRow(null, myWarningLabel);
//        style("filler").fillVertical().addRow(new JLabel());
    }

    /**
     * Set the warning label based on the current selected item.
     *
     * @param selectedItem The current selected item (may be {@code null}.)
     */
    private void setWarningLabel(PrivateKeyProvider selectedItem)
    {
        try
        {
            String message;
            if (selectedItem == null)
            {
                message = "<html>No certificate selected. Please import one using the <big><font color=\"green\">+</font></big> button above.</html>";
                myWarningLabel.setText(message);
            }
            else if (myProviderFilter != null && !myProviderFilter.isSatisfied(selectedItem))
            {
                message = myProviderFilter.getErrorMessage(selectedItem);

                boolean foundAlternate = false;
                for (int index = 0; index < myCertificateComboBox.getItemCount();)
                {
                    PrivateKeyProvider itemAt = myCertificateComboBox.getItemAt(index++);
                    if (myProviderFilter.isSatisfied(itemAt))
                    {
                        foundAlternate = true;
                        break;
                    }
                }

                if (!foundAlternate)
                {
                    message = StringUtilities.concat("<html>", message, "<br/>You have no acceptable certificates loaded. "
                            + "Please import one using the <big><font color=\"green\">+</font></big> button above.</html>");
                    LOGGER.warn("No acceptable certificates found. Acceptable issuers are: "
                            + myProviderFilter.getAcceptableIssuers());
                }
            }
            else
            {
                message = null;
            }
            if (message == null)
            {
                myWarningLabel.setVisible(false);
                myValidatorSupport.setValidationResult(ValidationStatus.VALID, "Certificate is valid.");
            }
            else
            {
                myWarningLabel.setText(message);
                myWarningLabel.setVisible(true);
                myValidatorSupport.setValidationResult(ValidationStatus.ERROR, message);
            }
            Window window = SwingUtilities.getWindowAncestor(this);
            if (window != null)
            {
                window.pack();
            }
        }
        catch (PrivateKeyProviderException e)
        {
            LOGGER.warn("Could not get certificate from provider: " + e, e);
        }
    }

    /** Action listener for the combo box. */
    private final class ComboBoxActionListener implements ActionListener
    {
        /**
         * A text area to contain certificate details.
         */
        private final JTextArea myCertificateDetails;

        /** The details scroll pane. */
        private final JScrollPane myCertificateDetailsScrollPane;

        /**
         * Constructor.
         *
         * @param certificateDetails The details text area.
         * @param certificateDetailsScrollPane The details scroll pane.
         */
        public ComboBoxActionListener(JTextArea certificateDetails, JScrollPane certificateDetailsScrollPane)
        {
            myCertificateDetails = certificateDetails;
            myCertificateDetailsScrollPane = certificateDetailsScrollPane;
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            PrivateKeyProvider selectedItem = getSelectedItem();
            myCertificateDetails.setText(selectedItem == null ? "" : selectedItem.getDetailString());
            myCertificateDetails.scrollRectToVisible(new Rectangle());

            EventQueueUtilities.invokeLater(() -> myCertificateDetailsScrollPane.getVerticalScrollBar()
                    .setValue(myCertificateDetailsScrollPane.getVerticalScrollBar().getMinimum()));

            setWarningLabel(selectedItem);
        }
    }
}
