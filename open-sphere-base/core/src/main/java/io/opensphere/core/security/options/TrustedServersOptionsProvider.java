package io.opensphere.core.security.options;

import java.awt.Component;
import java.awt.EventQueue;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import io.opensphere.core.SecurityManager;
import io.opensphere.core.options.OptionsProvider;
import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.core.quantify.Quantify;
import io.opensphere.core.security.config.v1.SecurityConfiguration;

/**
 * An {@link OptionsProvider} for trusted servers.
 */
public class TrustedServersOptionsProvider extends AbstractTableOptionsProvider
{
    /**
     * Construct the security options provider.
     *
     * @param securityManager The system security manager.
     * @param prefsRegistry The system preferences registry.
     */
    public TrustedServersOptionsProvider(SecurityManager securityManager, PreferencesRegistry prefsRegistry)
    {
        super(securityManager, prefsRegistry, "Trusted Servers");
    }

    @Override
    protected TableModel buildTableModel()
    {
        Collection<? extends String> trustedServers = getSecurityManager().getTrustedServers();
        Object[][] data = new Object[trustedServers.size()][];
        int index = 0;
        for (String server : trustedServers)
        {
            data[index] = new Object[1];
            data[index][0] = server;
            ++index;
        }
        return new DefaultTableModel(data, new Object[] { "Server Name", })
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
    protected void deleteRow(int row)
    {
        Quantify.collectMetric("mist3d.settings.security.trusted-servers.delete-button");

        Object selectedServer = getTable().getModel().getValueAt(row, 0);
        SecurityConfiguration config = getConfig().clone();
        Collection<String> userTrustedServers = config.getUserTrustedServers();
        for (Iterator<String> iter = userTrustedServers.iterator(); iter.hasNext();)
        {
            String server = iter.next();
            if (server.equals(selectedServer))
            {
                iter.remove();
            }
        }

        saveConfig(config);
    }

    @Override
    protected Component getDescriptionComponent()
    {
        return new JLabel(
                "<html>These are servers that you trust despite their certificates not being issued by a trusted authority.</html>");
    }

    @Override
    protected boolean showDeleteMessageDialog()
    {
        assert EventQueue.isDispatchThread();
        return JOptionPane.showOptionDialog(SwingUtilities.getWindowAncestor(getTable()),
                "If you delete this server, you will be prompted to trust it if its certificate is not already trusted. Are you sure?",
                "Confirm Delete", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE, null, null,
                null) == JOptionPane.OK_OPTION;
    }

    @Override
    protected void showDetails(int row)
    {
        Quantify.collectMetric("mist3d.settings.security.trusted-servers.details-button");

        assert EventQueue.isDispatchThread();

        Object selectedServer = getTable().getModel().getValueAt(row, 0);

        JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(getTable()),
                "This server is trusted, ignoring its certificate: " + selectedServer);
    }
}
