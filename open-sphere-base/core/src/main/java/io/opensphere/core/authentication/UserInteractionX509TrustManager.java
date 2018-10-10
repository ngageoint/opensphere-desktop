package io.opensphere.core.authentication;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertSelector;
import java.security.cert.CertificateException;
import java.security.cert.PKIXBuilderParameters;
import java.security.cert.TrustAnchor;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import javax.net.ssl.CertPathTrustManagerParameters;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import io.opensphere.core.SecurityManager;
import io.opensphere.core.util.PausingTimeBudget;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.core.util.security.CertificateUtilities;
import io.opensphere.core.util.swing.ButtonPanel;
import io.opensphere.core.util.swing.EventQueueUtilities;
import io.opensphere.core.util.swing.GridBagPanel;
import io.opensphere.core.util.swing.OptionDialog;

/**
 * An {@link X509TrustManager} that will prompt the user for action when a
 * server is untrusted.
 */
public final class UserInteractionX509TrustManager extends UserInteractionAuthenticator implements X509TrustManager
{
    /** Button label. */
    private static final String ALWAYS_CONNECT_TO_THIS_SERVER = "Always connect to this server";

    /** Button label. */
    private static final String ALWAYS_TRUST_THIS_CERTIFICATE = "Always trust this server's certificate";

    /** Button label. */
    private static final String CONNECT_THIS_TIME = "Connect just this once";

    /** Button label. */
    private static final String DO_NOT_CONNECT = "Do not connect";

    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(UserInteractionX509TrustManager.class);

    /** The accepted issuers. */
    private final List<X509Certificate> myAcceptedIssuers = New.list();

    /** The standard trust manager. */
    private X509TrustManager myX509TrustManager;

    /**
     * Construct the trust manager.
     *
     * @param serverName The server name to present to the user in dialog text.
     * @param serverKey The unique key to use for the server in the preferences.
     * @param parentProvider The parent component provider.
     * @param securityManager The system security manager used to get trusted
     *            servers and certs.
     * @param timeBudget Optional time budget.
     */
    public UserInteractionX509TrustManager(String serverName, String serverKey, Supplier<? extends Component> parentProvider,
            SecurityManager securityManager, PausingTimeBudget timeBudget)
    {
        super(serverName, serverKey, parentProvider, securityManager, timeBudget);
    }

    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void checkServerTrusted(final X509Certificate[] chain, final String authType) throws CertificateException
    {
        if (getSecurityManager().getTrustedServers().contains(getServerKey().toLowerCase()))
        {
            return;
        }
        try
        {
            getX509TrustManager().checkServerTrusted(chain, authType);
        }
        catch (final CertificateException ex)
        {
            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug("Server is not trusted: " + ex, ex);
            }

            // Avoid starting up multiple dialogs at a time.
            synchronized (UserInteractionX509TrustManager.class)
            {
                if (EventQueueUtilities.happyOnEdt(() -> queryUser(chain, authType)).booleanValue())
                {
                    return;
                }
                throw ex;
            }
        }
    }

    @Override
    public X509Certificate[] getAcceptedIssuers()
    {
        return myAcceptedIssuers.toArray(new X509Certificate[myAcceptedIssuers.size()]);
    }

    /**
     * Get a button that may be used to display the certificate chain.
     *
     * @param chain The certificate chain.
     * @return The button.
     */
    private JButton getViewCertificatesButton(final X509Certificate[] chain)
    {
        JButton viewCertificateButton = new JButton("View Certificates");
        viewCertificateButton.addActionListener(e ->
        {
            JTextArea textArea = new JTextArea(
                    CertificateUtilities.getDetailString(StringUtilities.EMPTY, Arrays.asList(chain)));
            textArea.setEditable(false);
            JScrollPane scrollPane = new JScrollPane(textArea);
            textArea.setBackground(scrollPane.getBackground());
            scrollPane.setPreferredSize(new Dimension(600, 400));
            OptionDialog dialog = new OptionDialog((JButton)e.getSource(), scrollPane, "Certificate Details");
            dialog.setButtonLabels(Collections.singleton(ButtonPanel.OK));
            dialog.buildAndShow();
        });
        return viewCertificateButton;
    }

    /**
     * Get the standard X.509 trust manager.
     *
     * @return The trust manager.
     * @throws CertificateException If the trust manager cannot be initialized.
     */
    private synchronized X509TrustManager getX509TrustManager() throws CertificateException
    {
        if (myX509TrustManager == null)
        {
            Set<TrustAnchor> trustAnchors = New.set();

            for (X509Certificate x509Certificate : getSecurityManager().getTrustedServerCerts())
            {
                trustAnchors.add(new TrustAnchor(x509Certificate, (byte[])null));
                myAcceptedIssuers.add(x509Certificate);
            }

            if (trustAnchors.isEmpty())
            {
                throw new CertificateException("No trusted servers");
            }

            TrustManagerFactory tmfactory;
            try
            {
                tmfactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                CertSelector certSelector = null;
                PKIXBuilderParameters certpathparameters = new PKIXBuilderParameters(trustAnchors, certSelector);
                certpathparameters.setRevocationEnabled(false);
                tmfactory.init(new CertPathTrustManagerParameters(certpathparameters));
            }
            catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException e)
            {
                throw new CertificateException("Failed to initialize trust manager: " + e, e);
            }

            myX509TrustManager = (X509TrustManager)tmfactory.getTrustManagers()[0];
        }
        return myX509TrustManager;
    }

    /**
     * Ask the user if this server should be trusted.
     *
     * @param chain The server's certificate chain.
     * @param authType The type of authentication.
     * @return {@link Boolean#TRUE} if the server can be trusted.
     */
    private Boolean queryUser(final X509Certificate[] chain, final String authType)
    {
        if (getSecurityManager().getTrustedServers().contains(getServerKey().toLowerCase()))
        {
            return Boolean.TRUE;
        }

        // Try creating a new trust manager just in case the user
        // added a new certificate while this task was waiting to
        // run.
        synchronized (this)
        {
            myX509TrustManager = null;
        }
        try
        {
            getX509TrustManager().checkClientTrusted(chain, authType);
            return Boolean.TRUE;
        }
        catch (GeneralSecurityException e)
        {
            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug(e, e);
            }
        }

        boolean timeBudgetPaused = getTimeBudget() != null && getTimeBudget().pause();
        try
        {
            Box box = Box.createVerticalBox();
            ButtonGroup bg = new ButtonGroup();
            JRadioButton thisTime = new JRadioButton(CONNECT_THIS_TIME);
            bg.add(thisTime);
            box.add(thisTime);
            JRadioButton trustServer = new JRadioButton(ALWAYS_CONNECT_TO_THIS_SERVER);
            bg.add(trustServer);
            box.add(trustServer);
            JRadioButton trustCert = new JRadioButton(ALWAYS_TRUST_THIS_CERTIFICATE, true);
            bg.add(trustCert);
            box.add(trustCert);
            JRadioButton noTrust = new JRadioButton(DO_NOT_CONNECT);
            bg.add(noTrust);
            box.add(noTrust);

            GridBagPanel northPanel = new GridBagPanel();
            northPanel.setAnchor(GridBagConstraints.WEST);
            northPanel.addRow(new JLabel("<html><h2>This Connection is Untrusted.</h2><hr/></html>"));
            northPanel.addRow(new JLabel("The server at " + getServerKey()
                    + (StringUtils.isEmpty(getServerName()) ? "" : " (" + getServerName() + ")")));
            northPanel.addRow(new JLabel("is presenting an unrecognized certificate."));

            northPanel.setAnchor(GridBagConstraints.CENTER).setInsets(10, 0, 10, 0).addRow(getViewCertificatesButton(chain));

            JPanel centerPanel = new JPanel();
            box.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
            centerPanel.add(box);

            JPanel panel = new JPanel(new BorderLayout());
            panel.add(northPanel, BorderLayout.NORTH);
            panel.add(centerPanel, BorderLayout.CENTER);

            int selection = JOptionPane.showOptionDialog(getParent(), panel, "Server Trust", JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.WARNING_MESSAGE, null, null, null);

            if (selection == JOptionPane.CANCEL_OPTION || selection == JOptionPane.CLOSED_OPTION || noTrust.isSelected())
            {
                return Boolean.FALSE;
            }
            else if (trustServer.isSelected())
            {
                getSecurityManager().addTrustedServer(getServerKey().toLowerCase());
            }
            else if (trustCert.isSelected())
            {
                getSecurityManager().addTrustedServerCerts(Arrays.asList(chain));
            }
        }
        finally
        {
            if (timeBudgetPaused)
            {
                getTimeBudget().unpause();
            }
        }

        return Boolean.TRUE;
    }
}
