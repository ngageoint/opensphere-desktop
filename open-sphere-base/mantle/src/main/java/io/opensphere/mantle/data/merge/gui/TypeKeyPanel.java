package io.opensphere.mantle.data.merge.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.DropMode;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.TransferHandler;

import org.apache.log4j.Logger;

import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.swing.EventQueueUtilities;

/**
 * The Class TypeKeyPanel.
 */
@SuppressWarnings({ "serial", "PMD.GodClass" })
public abstract class TypeKeyPanel extends JPanel
{
    /** The Constant ourDataFlavor. */
    public static final DataFlavor ourDataFlavor = new DataFlavor(TypeKeyPanel.class, TypeKeyPanel.class.getName());

    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(TypeKeyPanel.class);

    /** The Inner panel. */
    private final JPanel myInnerPanel;

    /** The J list. */
    private final JList<TypeKeyEntry> myJList;

    /** The Move coordinator. */
    private final DataTypeKeyMoveDNDCoordinator myMoveCoordinator;

    /**
     * Instantiates a new type key panel.
     *
     * @param title the title
     * @param moveCoordinator the move coordinator
     */
    public TypeKeyPanel(String title, DataTypeKeyMoveDNDCoordinator moveCoordinator)
    {
        super();
        myMoveCoordinator = moveCoordinator;
        myJList = new JList<>();
        myJList.setModel(new DefaultListModel<TypeKeyEntry>());
        myJList.setDragEnabled(true);
        myJList.setDropMode(DropMode.INSERT);
        myJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane jsp = new JScrollPane(myJList);

        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder(title));
        setMinimumSize(new Dimension(250, 150));
        setPreferredSize(new Dimension(250, 150));
        setMaximumSize(new Dimension(400, 300));

        myInnerPanel = new JPanel(new BorderLayout());
        myInnerPanel.add(jsp, BorderLayout.CENTER);
        myInnerPanel.setBorder(BorderFactory.createLineBorder(Color.gray));

        add(myInnerPanel, BorderLayout.CENTER);

        myJList.setTransferHandler(new TypeKeyPanelTransferHandler());
    }

    /**
     * Accepted transfer of entry.
     *
     * @param entry the entry
     */
    public void acceptedTransferOfEntry(final TypeKeyEntry entry)
    {
        EventQueueUtilities.invokeLater(() -> getListModel().removeElement(entry));
    }

    /**
     * Adds the type key entry.
     *
     * @param entry the entry
     * @return true, if successful
     */
    public abstract boolean addTypeKeyEntry(TypeKeyEntry entry);

    /**
     * Allows type key entry.
     *
     * @param entry the entry
     * @param errors the errors
     * @return true, if successful
     */
    public abstract boolean allowsTypeKeyEntry(TypeKeyEntry entry, List<String> errors);

    /**
     * Gets the unique set of class types in the set.
     *
     * @return the class type set
     */
    public Set<String> getClassTypeSet()
    {
        Set<String> resultSet = new HashSet<>();
        for (TypeKeyEntry tke : getTypeEntryList())
        {
            resultSet.add(tke.getClassType());
        }
        return resultSet;
    }

    /**
     * Gets unique set of data types in the set.
     *
     * @return the data type set
     */
    public Set<String> getDataTypeKeySet()
    {
        Set<String> resultSet = new HashSet<>();
        for (TypeKeyEntry tke : getTypeEntryList())
        {
            resultSet.add(tke.getDataTypeKey());
        }
        return resultSet;
    }

    /**
     * Gets the inner panel.
     *
     * @return the inner panel
     */
    public JPanel getInnerPanel()
    {
        return myInnerPanel;
    }

    /**
     * Gets the key count.
     *
     * @return the key count
     */
    public int getKeyCount()
    {
        return myJList.getModel().getSize();
    }

    /**
     * Gets the unique set of key names.
     *
     * @param lowerCase if true convert all key names to lower case
     * @return the key name set
     */
    public Set<String> getKeyNameSet(boolean lowerCase)
    {
        Set<String> resultSet = new HashSet<>();
        for (TypeKeyEntry tke : getTypeEntryList())
        {
            resultSet.add(lowerCase ? tke.getKeyName().toLowerCase() : tke.getKeyName());
        }
        return resultSet;
    }

    /**
     * Gets the map of key to type key entry.
     *
     * @return the map of key to type key entry
     */
    public Map<String, TypeKeyEntry> getMapOfKeyToTypeKeyEntry()
    {
        Map<String, TypeKeyEntry> map = new HashMap<>();
        for (TypeKeyEntry entry : getTypeEntryList())
        {
            map.put(entry.getKeyName(), entry);
        }
        return map;
    }

    /**
     * Gets the move coordinator.
     *
     * @return the move coordinator
     */
    public DataTypeKeyMoveDNDCoordinator getMoveCoordinator()
    {
        return myMoveCoordinator;
    }

    /**
     * Gets the type list.
     *
     * @return the type list
     */
    public Collection<TypeKeyEntry> getTypeEntryList()
    {
        if (getListModel().isEmpty())
        {
            return Collections.<TypeKeyEntry>emptyList();
        }

        return CollectionUtilities.filterDowncast(Arrays.asList(getListModel().toArray()), TypeKeyEntry.class);
    }

    /**
     * Reset inner panel border.
     */
    public void resetInnerPanelBorder()
    {
        EventQueueUtilities.runOnEDT(new Runnable()
        {
            @Override
            public void run()
            {
                myInnerPanel.setBorder(BorderFactory.createLineBorder(Color.gray));
            }
        });
    }

    /**
     * Sets the editable.
     *
     * @param editable the new editable
     */
    public void setEditable(boolean editable)
    {
        myJList.setDragEnabled(editable);
    }

    /**
     * Sets the inner panel border color.
     *
     * @param c the new inner panel border color
     */
    public void setInnerPanelBorderColor(final Color c)
    {
        EventQueueUtilities.runOnEDT(() -> myInnerPanel.setBorder(BorderFactory.createLineBorder(c)));
    }

    /**
     * Sets the type list.
     *
     * @param entryList the new type list
     */
    public void setTypeEntryList(List<TypeKeyEntry> entryList)
    {
        List<TypeKeyEntry> list = new ArrayList<>(entryList);
        Collections.sort(list, new Comparator<TypeKeyEntry>()
        {
            @Override
            public int compare(TypeKeyEntry o1, TypeKeyEntry o2)
            {
                return o1.getKeyName().compareTo(o2.getKeyName());
            }
        });

        DefaultListModel<TypeKeyEntry> model = new DefaultListModel<>();
        for (TypeKeyEntry entry : list)
        {
            entry.setOwner(this);
            model.addElement(entry);
        }
        myJList.setModel(model);
    }

    /**
     * Sort list entries.
     */
    public void sortListEntries()
    {
        EventQueueUtilities.runOnEDT(new Runnable()
        {
            @SuppressWarnings({ "rawtypes", "unchecked" })
            @Override
            public void run()
            {
                if (!getListModel().isEmpty())
                {
                    Object[] objs = getListModel().toArray();
                    getListModel().removeAllElements();
                    List<Object> objList = New.list(objs);
                    Collections.sort(objList, new Comparator<>()
                    {
                        @Override
                        public int compare(Object o1, Object o2)
                        {
                            if (o1 instanceof TypeKeyEntry && o2 instanceof TypeKeyEntry)
                            {
                                String val1 = ((TypeKeyEntry)o1).getKeyName();
                                String val2 = ((TypeKeyEntry)o2).getKeyName();
                                return val1.compareTo(val2);
                            }
                            else
                            {
                                return 0;
                            }
                        }
                    });
                    DefaultListModel model = new DefaultListModel();
                    for (Object entry : objList)
                    {
                        model.addElement(entry);
                    }
                    myJList.setModel(model);
                }
            }
        });
    }

    /**
     * Gets the j list.
     *
     * @return the j list
     */
    protected JList<TypeKeyEntry> getJList()
    {
        return myJList;
    }

    /**
     * Gets the list model.
     *
     * @return the list model
     */
    protected DefaultListModel<TypeKeyEntry> getListModel()
    {
        return (DefaultListModel<TypeKeyEntry>)myJList.getModel();
    }

    /**
     * Display drop location.
     *
     * @param string the string
     */
    private void displayDropLocation(final String string)
    {
        EventQueueUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null, string));
    }

    /**
     * The Class TypeKeyPanelTransferHandler.
     */
    public class TypeKeyPanelTransferHandler extends TransferHandler
    {
        /**
         * Instantiates a new type key panel transfer handler.
         */
        public TypeKeyPanelTransferHandler()
        {
            super();
        }

        @Override
        public boolean canImport(TransferHandler.TransferSupport info)
        {
            if (!info.isDataFlavorSupported(TypeKeyEntry.ourDataFlavor))
            {
                return false;
            }

            Transferable tf = info.getTransferable();
            try
            {
                TypeKeyEntry tke = (TypeKeyEntry)tf.getTransferData(TypeKeyEntry.ourDataFlavor);
                return allowsTypeKeyEntry(tke, null);
            }
            catch (UnsupportedFlavorException | IOException e)
            {
                LOGGER.warn(e);
            }
            return false;
        }

        @Override
        public int getSourceActions(JComponent c)
        {
            return MOVE;
        }

        @Override
        public boolean importData(TransferHandler.TransferSupport info2)
        {
            if (!info2.isDrop())
            {
                return false;
            }

            // Check for String flavor
            if (!info2.isDataFlavorSupported(TypeKeyEntry.ourDataFlavor))
            {
                displayDropLocation("List doesn't accept a drop of this type.");
                return false;
            }

            Transferable t = info2.getTransferable();
            TypeKeyEntry data = null;
            TypeKeyPanel originPanel = null;
            boolean isMoveWithinPanel = false;
            try
            {
                data = (TypeKeyEntry)t.getTransferData(TypeKeyEntry.ourDataFlavor);
                originPanel = data.getOwner() == null ? null : (TypeKeyPanel)data.getOwner();
                if (Utilities.sameInstance(originPanel, TypeKeyPanel.this))
                {
                    isMoveWithinPanel = true;
                }
            }
            catch (UnsupportedFlavorException | IOException e)
            {
                if (LOGGER.isTraceEnabled())
                {
                    LOGGER.trace(e, e);
                }
                return false;
            }

            List<String> errors = new ArrayList<>();
            if (!allowsTypeKeyEntry(data, errors))
            {
                displayDropLocation(errors.isEmpty() ? "List doesn't accept a drop of this type." : errors.get(0));
                return false;
            }

            if (isMoveWithinPanel)
            {
                return false;
            }

            JList.DropLocation dl = (JList.DropLocation)info2.getDropLocation();
            DefaultListModel<TypeKeyEntry> listModel = (DefaultListModel<TypeKeyEntry>)myJList.getModel();
            acceptTransfer(dl, listModel, data, originPanel);

            return true;
        }

        @Override
        protected Transferable createTransferable(JComponent c)
        {
            @SuppressWarnings("unchecked")
            JList<TypeKeyEntry> list = (JList<TypeKeyEntry>)c;
            List<TypeKeyEntry> values = list.getSelectedValuesList();
            TypeKeyEntry entry = values.get(0);
            myMoveCoordinator.moveInitiated(entry, (TypeKeyPanel)entry.getOwner(), TypeKeyPanel.this);
            TypeKeyTransferable transferable = new TypeKeyTransferable(values.get(0));
            return transferable;
        }

        @Override
        protected void exportDone(JComponent source, Transferable data, int action)
        {
            myMoveCoordinator.moveComplete();
        }

        /**
         * Accept transfer.
         *
         * @param dl the dl
         * @param listModel the list model
         * @param data the data
         * @param originPanel the origin panel
         */
        private void acceptTransfer(JList.DropLocation dl, DefaultListModel<TypeKeyEntry> listModel, TypeKeyEntry data,
                TypeKeyPanel originPanel)
        {
            if (dl.isInsert())
            {
                listModel.add(dl.getIndex(), data);
            }
            else
            {
                listModel.set(dl.getIndex(), data);
            }
            data.setOwner(TypeKeyPanel.this);
            if (originPanel != null && !Utilities.sameInstance(originPanel, TypeKeyPanel.this))
            {
                originPanel.acceptedTransferOfEntry(data);
            }
        }
    }
}
