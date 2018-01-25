package io.opensphere.mantle.data.merge.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.DropMode;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.TransferHandler;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.log4j.Logger;

import io.opensphere.core.util.swing.EventQueueUtilities;
import io.opensphere.mantle.data.merge.gui.DataTypeKeyMoveDNDCoordinator.KeyMoveListener;

/**
 * The Class DestinationKeyListPanel.
 */
@SuppressWarnings("serial")
public class DestinationKeyListPanel extends JPanel implements KeyMoveListener
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(DestinationKeyListPanel.class);

    /** The Dest key list. */
    private final JList<MappedTypeKeyPanelProxy> myDestKeyList;

    /** The Dest key list cell renderer. */
    private final DestKeyListCellRenderer myDestKeyListCellRenderer;

    /**
     * Instantiates a new destination key list panel.
     *
     * @param cdr the cdr
     */
    public DestinationKeyListPanel(DataTypeKeyMoveDNDCoordinator cdr)
    {
        super(new BorderLayout());
        myDestKeyListCellRenderer = new DestKeyListCellRenderer();
        myDestKeyList = new JList<>();
        myDestKeyList.setCellRenderer(myDestKeyListCellRenderer);
        JScrollPane jsp = new JScrollPane(myDestKeyList);
        add(jsp, BorderLayout.CENTER);
        JLabel lb = new JLabel("Merged Key names");
        lb.setHorizontalAlignment(SwingConstants.CENTER);
        add(lb, BorderLayout.NORTH);
        setBorder(BorderFactory.createEtchedBorder());
        setPreferredSize(new Dimension(150, 5000));
        setMinimumSize(new Dimension(150, 100));

        myDestKeyList.setDropMode(DropMode.USE_SELECTION);
        myDestKeyList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        myDestKeyList.addListSelectionListener(createListSelectionListener());
        myDestKeyList.setTransferHandler(new DestKeyPanelTransferHandler());
        cdr.addKeyMoveListener(this);
    }

    @Override
    public void keyMoveCompleted(final TypeKeyEntry entry, final TypeKeyPanel origPanel)
    {
        EventQueueUtilities.runOnEDT(new Runnable()
        {
            @Override
            public void run()
            {
                myDestKeyListCellRenderer.setDNDTypeKeyEntry(null);
                myDestKeyList.repaint();
            }
        });
    }

    @Override
    public void keyMoveInitiated(final TypeKeyEntry entry, final TypeKeyPanel sourcePanel, final Object source)
    {
        EventQueueUtilities.runOnEDT(new Runnable()
        {
            @Override
            public void run()
            {
                myDestKeyListCellRenderer.setDNDTypeKeyEntry(entry);
                myDestKeyList.repaint();
            }
        });
    }

    /**
     * Sets the model.
     *
     * @param dlm the new model
     */
    public void setModel(DefaultListModel<MappedTypeKeyPanelProxy> dlm)
    {
        myDestKeyList.setModel(dlm);
    }

    /**
     * Creates the list selection listener.
     *
     * @return the list selection listener
     */
    private ListSelectionListener createListSelectionListener()
    {
        return new ListSelectionListener()
        {
            @Override
            public void valueChanged(ListSelectionEvent e)
            {
                if (!e.getValueIsAdjusting())
                {
                    int index = myDestKeyList.getSelectedIndex();
                    if (index != -1)
                    {
                        MappedTypeKeyPanelProxy pp = myDestKeyList.getModel().getElementAt(index);
                        ((JComponent)pp.getPanel().getParent()).scrollRectToVisible(pp.getPanel().getBounds());
                    }
                }
            }
        };
    }

    /**
     * The Class DestKeyListCellRenderer.
     */
    public static class DestKeyListCellRenderer extends DefaultListCellRenderer
    {
        /** The Bold font. */
        private Font myBoldFont;

        /** The Default font. */
        private Font myDefaultFont;

        /** The Moving type key entry. */
        private TypeKeyEntry myMovingTypeKeyEntry;

        /**
         * Instantiates a new dest key list cell renderer.
         */
        public DestKeyListCellRenderer()
        {
            super();
        }

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
                boolean cellHasFocus)
        {
            if (myDefaultFont == null)
            {
                myDefaultFont = getFont();
                myBoldFont = getFont().deriveFont(Font.BOLD, myDefaultFont.getSize() + 2);
            }
            Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (myMovingTypeKeyEntry != null && value instanceof MappedTypeKeyPanelProxy)
            {
                boolean accepts = ((MappedTypeKeyPanelProxy)value).getPanel().allowsTypeKeyEntry(myMovingTypeKeyEntry, null);
                setForeground(accepts ? Color.green : Color.red);
                setFont(accepts ? myBoldFont : myDefaultFont);
            }
            return c;
        }

        /**
         * Sets the dND type key entry.
         *
         * @param entry the new dND type key entry
         */
        public void setDNDTypeKeyEntry(TypeKeyEntry entry)
        {
            myMovingTypeKeyEntry = entry;
        }
    }

    /**
     * The Class DestKeyPanelTransferHandler.
     */
    private class DestKeyPanelTransferHandler extends TransferHandler
    {
        /**
         * Instantiates a new dest key panel transfer handler.
         */
        public DestKeyPanelTransferHandler()
        {
            super();
        }

        @Override
        public boolean canImport(TransferHandler.TransferSupport info)
        {
            if (info.isDataFlavorSupported(TypeKeyEntry.ourDataFlavor))
            {
                // Get the string that is being dropped.
                Transferable t = info.getTransferable();
                TypeKeyEntry data = null;
                try
                {
                    data = (TypeKeyEntry)t.getTransferData(TypeKeyEntry.ourDataFlavor);
                    JList.DropLocation dl = (JList.DropLocation)info.getDropLocation();
                    DefaultListModel<MappedTypeKeyPanelProxy> listModel = (DefaultListModel<MappedTypeKeyPanelProxy>)myDestKeyList
                            .getModel();
                    int index = dl.getIndex();
                    if (index != -1)
                    {
                        Object obj = listModel.getElementAt(index);
                        if (obj instanceof MappedTypeKeyPanelProxy)
                        {
                            MappedTypeKeyPanelProxy pp = (MappedTypeKeyPanelProxy)obj;
                            return pp.getPanel().allowsTypeKeyEntry(data, null);
                        }
                    }
                }
                catch (UnsupportedFlavorException | IOException e)
                {
                    LOGGER.warn(e);
                }
            }
            return false;
        }

        @Override
        public int getSourceActions(JComponent c)
        {
            return MOVE;
        }

        @Override
        public boolean importData(TransferHandler.TransferSupport info)
        {
            if (!info.isDrop() || !info.isDataFlavorSupported(TypeKeyEntry.ourDataFlavor))
            {
                return false;
            }

            TypeKeyEntry data;
            try
            {
                data = (TypeKeyEntry)info.getTransferable().getTransferData(TypeKeyEntry.ourDataFlavor);
            }
            catch (UnsupportedFlavorException | IOException e)
            {
                if (LOGGER.isTraceEnabled())
                {
                    LOGGER.trace(e, e);
                }
                return false;
            }

            JList.DropLocation dl = (JList.DropLocation)info.getDropLocation();
            int index = dl.getIndex();
            if (index == -1)
            {
                return false;
            }
            DefaultListModel<MappedTypeKeyPanelProxy> listModel = (DefaultListModel<MappedTypeKeyPanelProxy>)myDestKeyList
                    .getModel();
            Object obj = listModel.getElementAt(index);
            if (obj instanceof MappedTypeKeyPanelProxy)
            {
                MappedTypeKeyPanelProxy pp = (MappedTypeKeyPanelProxy)obj;
                final List<String> errors = new ArrayList<>();
                if (pp.getPanel().allowsTypeKeyEntry(data, errors))
                {
                    TypeKeyPanel originPanel = (TypeKeyPanel)data.getOwner();
                    originPanel.acceptedTransferOfEntry(data);
                    pp.getPanel().addTypeKeyEntry(data);
                    return true;
                }
                else
                {
                    EventQueueUtilities.invokeLater(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            JOptionPane.showMessageDialog(myDestKeyList, "Could not accept key because:\n" + errors.get(0));
                        }
                    });
                }
            }

            return false;
        }

        @Override
        protected Transferable createTransferable(JComponent c)
        {
            JList<?> list = (JList<?>)c;
            TypeKeyTransferable transferable = new TypeKeyTransferable((TypeKeyEntry)list.getSelectedValue());
            return transferable;
        }
    }
}
