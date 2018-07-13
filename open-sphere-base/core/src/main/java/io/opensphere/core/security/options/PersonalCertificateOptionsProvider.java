package io.opensphere.core.security.options;

import java.awt.Component;
import java.awt.EventQueue;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import org.apache.log4j.Logger;

import io.opensphere.core.SecurityManager;
import io.opensphere.core.options.OptionsProvider;
import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.core.quantify.Quantify;
import io.opensphere.core.security.config.v1.SecurityConfiguration;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.core.util.security.CertificateUtilities;
import io.opensphere.core.util.security.EncryptedPrivateKeyAndCertChain;
import io.opensphere.core.util.security.PrivateKeyProvider;
import io.opensphere.core.util.security.PrivateKeyProviderException;
import io.opensphere.core.util.security.SimplePrivateKeyProvider;

/**
 * An {@link OptionsProvider} for personal certificates.
 */
public class PersonalCertificateOptionsProvider extends AbstractTableOptionsProvider
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(PersonalCertificateOptionsProvider.class);

    /**
     * Construct the security options provider.
     *
     * @param securityManager The system security manager.
     * @param prefsRegistry The system preferences registry.
     */
    public PersonalCertificateOptionsProvider(SecurityManager securityManager, PreferencesRegistry prefsRegistry)
    {
        super(securityManager, prefsRegistry, "Personal Certificates");
    }

    @Override
    protected TableModel buildTableModel()
    {
        Collection<? extends PrivateKeyProvider> privateKeyProviders = getSecurityManager().getPrivateKeyProviders();
        Object[][] data = new Object[privateKeyProviders.size()][];
        int index = 0;
        for (PrivateKeyProvider privateKeyProvider : privateKeyProviders)
        {
            data[index] = new Object[5];
            data[index][0] = privateKeyProvider.getAlias();
            data[index][1] = privateKeyProvider.getSource();
            data[index][2] = "Load error";
            data[index][3] = "Load error";
            data[index][4] = "Load error";

            try
            {
                List<? extends X509Certificate> certificateChain = privateKeyProvider.getCertificateChain();
                if (!certificateChain.isEmpty())
                {
                    X509Certificate cert = certificateChain.get(0);
                    data[index][2] = CertificateUtilities.getLastDistinguishedNamePart(cert.getSubjectDN().getName());
                    data[index][3] = CertificateUtilities.getLastDistinguishedNamePart(cert.getIssuerDN().getName());
                    data[index][4] = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z").format(cert.getNotAfter());
                }
            }
            catch (PrivateKeyProviderException e)
            {
                LOGGER.warn(e, e);
            }
            ++index;
        }
        return new DefaultTableModel(data, new Object[] { "Alias", "Source", "Issued To", "Issued By", "Valid End", })
        {
            /** Serial version UID. */
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isCellEditable(int row, int column)
            {
                return false;
            }
        };
    }

    @Override
    protected boolean canDeleteRows(int[] selectedRows)
    {
        for (int row : selectedRows)
        {
            if (!getTable().getModel().getValueAt(row, 1).equals("User"))
            {
                return false;
            }
        }
        return super.canDeleteRows(selectedRows);
    }

    @Override
    protected void deleteRow(int row)
    {
        Quantify.collectMetric("mist3d.settings.security.personal-certificates.delete-button");
        assert EventQueue.isDispatchThread();

        Collection<? extends PrivateKeyProvider> privateKeyProviders = getSecurityManager().getPrivateKeyProviders();
        PrivateKeyProvider pkp = CollectionUtilities.getItem(privateKeyProviders, row);

        SecurityConfiguration config = getConfig().clone();
        Collection<? extends EncryptedPrivateKeyAndCertChain> encryptedPrivateKeyAndCertChains = config
                .getEncryptedPrivateKeyAndCertChains();
        boolean found = false;
        for (Iterator<? extends EncryptedPrivateKeyAndCertChain> iter = encryptedPrivateKeyAndCertChains.iterator(); iter
                .hasNext() && !found;)
        {
            EncryptedPrivateKeyAndCertChain encryptedPrivateKeyAndCertChain = iter.next();
            try
            {
                SimplePrivateKeyProvider pkp2 = encryptedPrivateKeyAndCertChain.getPrivateKeyProvider(null);
                if (pkp2.hasDigest(pkp.getDigest("MD5")))
                {
                    found = true;
                    iter.remove();
                }
            }
            catch (PrivateKeyProviderException | NoSuchAlgorithmException | CertificateException e)
            {
                LOGGER.warn(e, e);
            }
        }

        if (found)
        {
            saveConfig(config);
        }
        else
        {
            JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(getTable()), "That private key cannot be deleted.",
                    "Cannot Delete", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    protected String getDeleteToolTipText()
    {
        return "Select a \"User\" certificate to delete.";
    }

    @Override
    protected Component getDescriptionComponent()
    {
        return new JLabel(
                "<html>These are certificates with private keys that you have imported. The private keys are stored encrypted.</html>");
    }

    @Override
    protected boolean showDeleteMessageDialog()
    {
        assert EventQueue.isDispatchThread();
        return JOptionPane.showOptionDialog(SwingUtilities.getWindowAncestor(getTable()),
                "If you delete this certificate, you will no longer be able to use it to authenticate. Are you sure?",
                "Confirm Delete", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE, null, null,
                null) == JOptionPane.OK_OPTION;
    }

    @Override
    protected void showDetails(int row)
    {
        Quantify.collectMetric("mist3d.settings.security.personal-certificates.details-button");
        assert EventQueue.isDispatchThread();

        Collection<? extends PrivateKeyProvider> privateKeyProviders = getSecurityManager().getPrivateKeyProviders();
        PrivateKeyProvider pkp = CollectionUtilities.getItem(privateKeyProviders, row);

        String detail = StringUtilities.convertToHTMLTable(pkp.getDetailString());
        JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(getTable()), detail, "Certificate Details",
                JOptionPane.INFORMATION_MESSAGE);
    }
}
