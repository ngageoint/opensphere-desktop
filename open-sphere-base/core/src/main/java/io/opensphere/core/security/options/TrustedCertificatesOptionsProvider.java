package io.opensphere.core.security.options;

import java.awt.Component;
import java.awt.EventQueue;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import org.apache.log4j.Logger;

import io.opensphere.core.SecurityManager;
import io.opensphere.core.options.OptionsProvider;
import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.core.quantify.Quantify;
import io.opensphere.core.security.config.v1.SecurityConfiguration;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.ImpossibleException;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.core.util.security.CertificateUtilities;

/**
 * An {@link OptionsProvider} for trusted server certs.
 */
public class TrustedCertificatesOptionsProvider extends AbstractTableOptionsProvider
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(TrustedCertificatesOptionsProvider.class);

    /**
     * Construct the security options provider.
     *
     * @param securityManager The system security manager.
     * @param prefsRegistry The system preferences registry.
     */
    public TrustedCertificatesOptionsProvider(SecurityManager securityManager, PreferencesRegistry prefsRegistry)
    {
        super(securityManager, prefsRegistry, "Trusted Certificates");
    }

    @Override
    protected TableModel buildTableModel()
    {
        final Object[] columnIdentifiers = new Object[] { "Source", "Issued To", "Issued By", "Valid End" };
        final DefaultTableModel model = new DefaultTableModel((Object[][])null, columnIdentifiers)
        {
            /** Serial version UID. */
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isCellEditable(int row, int column)
            {
                return false;
            }
        };

        SwingWorker<Object[][], Void> worker = new SwingWorker<Object[][], Void>()
        {
            @Override
            protected Object[][] doInBackground()
            {
                Set<? extends X509Certificate> userTrustedServerCerts = New.set(getSecurityManager().getUserTrustedServerCerts());

                Collection<X509Certificate> trustedServerCerts = getSecurityManager().getTrustedServerCerts();
                Object[][] data = new Object[trustedServerCerts.size()][];
                int index = 0;
                for (X509Certificate cert : trustedServerCerts)
                {
                    data[index] = new Object[4];
                    data[index][0] = userTrustedServerCerts.contains(cert) ? "User" : "System";
                    data[index][1] = CertificateUtilities.getLastDistinguishedNamePart(cert.getSubjectDN().getName());
                    data[index][2] = CertificateUtilities.getLastDistinguishedNamePart(cert.getIssuerDN().getName());
                    data[index][3] = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z").format(cert.getNotAfter());
                    ++index;
                }
                return data;
            }

            @Override
            protected void done()
            {
                try
                {
                    Object[][] data = get();
                    model.setDataVector(data, columnIdentifiers);
                }
                catch (InterruptedException e)
                {
                    throw new ImpossibleException(e);
                }
                catch (ExecutionException e)
                {
                    LOGGER.debug("Execution exception encountered.", e);
                    if (e.getCause() instanceof Error)
                    {
                        throw (Error)e.getCause();
                    }
                    else
                    {
                        throw (RuntimeException)e.getCause();
                    }
                }
            }
        };
        worker.execute();

        return model;
    }

    @Override
    protected boolean canDeleteRows(int[] selectedRows)
    {
        for (int row : selectedRows)
        {
            byte[] encoded;
            try
            {
                encoded = getEncodedCert(row);
            }
            catch (CertificateEncodingException e)
            {
                if (LOGGER.isDebugEnabled())
                {
                    LOGGER.debug("The supplied certificate from row '" + row + "' encountered an encoding exception.", e);
                }
                return false;
            }

            boolean found = false;
            Collection<byte[]> userTrustedCerts = getConfig().getUserTrustedCerts();
            for (byte[] encoded2 : userTrustedCerts)
            {
                if (Arrays.equals(encoded, encoded2))
                {
                    found = true;
                    break;
                }
            }
            if (!found)
            {
                return false;
            }
        }
        return true;
    }

    @Override
    protected void deleteRow(int row)
    {
        Quantify.collectMetric("mist3d.settings.security.trusted-certificates.delete-button");

        byte[] encoded;
        try
        {
            encoded = getEncodedCert(row);
        }
        catch (CertificateEncodingException e)
        {
            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug("The supplied certificate from row '" + row + "' encountered an encoding exception.", e);
            }
            JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(getTable()), "The certificate could not be encoded.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        boolean found = false;
        SecurityConfiguration config = getConfig().clone();
        Collection<byte[]> userTrustedCerts = config.getUserTrustedCerts();
        for (Iterator<byte[]> iter = userTrustedCerts.iterator(); iter.hasNext() && !found;)
        {
            byte[] encoded2 = iter.next();
            if (Arrays.equals(encoded, encoded2))
            {
                found = true;
                iter.remove();
            }
        }

        if (found)
        {
            saveConfig(config);
        }
        else
        {
            JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(getTable()),
                    "That is a system certificate and cannot be deleted.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    protected String getDeleteToolTipText()
    {
        return "Select a row to delete. Only \"User\" certificates can be deleted.";
    }

    @Override
    protected Component getDescriptionComponent()
    {
        return new JLabel(
                "<html>These are certificates that you trust. Other certificates linked to these certificates will also be trusted implicitly.</html>");
    }

    /**
     * Get the encoded certificate on a certain row.
     *
     * @param row The row.
     * @return The encoded certificate.
     * @throws CertificateEncodingException If the certificate cannot be
     *             encoded.
     */
    protected byte[] getEncodedCert(int row) throws CertificateEncodingException
    {
        return CollectionUtilities.getItem(getSecurityManager().getTrustedServerCerts(), row).getEncoded();
    }

    @Override
    protected boolean showDeleteMessageDialog()
    {
        assert EventQueue.isDispatchThread();
        return JOptionPane.showOptionDialog(SwingUtilities.getWindowAncestor(getTable()),
                "If you delete this certificate, you will be prompted to connect to servers that use it. Are you sure?",
                "Confirm Delete", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE, null, null,
                null) == JOptionPane.OK_OPTION;
    }

    @Override
    protected void showDetails(int row)
    {
        Quantify.collectMetric("mist3d.settings.security.trusted-certificates.details-button");

        assert EventQueue.isDispatchThread();

        X509Certificate item = CollectionUtilities.getItem(getSecurityManager().getTrustedServerCerts(), row);

        String detail = StringUtilities.convertToHTMLTable(CertificateUtilities.getDetailString("", item));
        JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(getTable()), detail, "Certificate Details",
                JOptionPane.INFORMATION_MESSAGE);
    }
}
