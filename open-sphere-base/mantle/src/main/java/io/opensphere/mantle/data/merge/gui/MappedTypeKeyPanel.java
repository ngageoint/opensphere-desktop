package io.opensphere.mantle.data.merge.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import org.apache.log4j.Logger;

import io.opensphere.core.common.util.Tuple2;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.data.merge.MergeKeySpecification;
import io.opensphere.mantle.data.merge.MergeKeySpecification.ConversionHint;
import io.opensphere.mantle.data.merge.gui.DataTypeKeyMoveDNDCoordinator.KeyMoveListener;
import io.opensphere.mantle.util.MyButtons;

/**
 * The Class SourceTypeKeyPanel.
 */
@SuppressWarnings({ "serial", "PMD.GodClass" })
public class MappedTypeKeyPanel extends TypeKeyPanel implements ActionListener, KeyMoveListener
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(MappedTypeKeyPanel.class);

    /** The Class type. */
    private final String myClassType;

    /** The Data type merge panel. */
    private final DataTypeMergePanel myDataTypeMergePanel;

    /** The Type. */
    private String myKeyName;

    /** The Popup menu. */
    private final JPopupMenu myPopupMenu;

    /** The Popup selected index. */
    private int myPopupSelectedIndex;

    /** The Remove bt. */
    private final JButton myRemoveBT;

    /** The Remove mi. */
    private final JMenuItem myRemoveMI;

    /** The Rename bt. */
    private final JButton myRenameBT;

    /** The Restrict by class type. */
    private final boolean myRestrictByClassType;

    /**
     * Instantiates a new source type key panel.
     *
     * @param cdr the cdr
     * @param dtmp the dtmp
     * @param title the title
     * @param classType the class type
     * @param restrictByClassType the restrict by class type
     */
    public MappedTypeKeyPanel(DataTypeKeyMoveDNDCoordinator cdr, DataTypeMergePanel dtmp, String title, String classType,
            boolean restrictByClassType)
    {
        super(title, cdr);
        myDataTypeMergePanel = dtmp;
        myKeyName = title;
        myClassType = classType;
        myRestrictByClassType = restrictByClassType;
        cdr.addKeyMoveListener(this);
        getJList().setCellRenderer(new CustomListCellRenderer());
        myPopupMenu = new JPopupMenu();
        myRemoveMI = new JMenuItem("Remove");
        myRemoveMI.addActionListener(e ->
        {
            if (myPopupSelectedIndex != -1)
            {
                try
                {
                    TypeKeyEntry obj = getJList().getModel().getElementAt(myPopupSelectedIndex);
                    myDataTypeMergePanel.returnTypeEntryToSource(obj);
                    myPopupSelectedIndex = -1;
                }
                catch (RuntimeException ex)
                {
                    LOGGER.warn(e);
                }
            }
        });
        myPopupMenu.add(myRemoveMI);
        getJList().addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
            }

            @Override
            public void mousePressed(MouseEvent e)
            {
                if (e.getButton() == 3)
                {
                    int index = getJList().locationToIndex(e.getPoint());
                    myPopupSelectedIndex = index;
                    if (index != -1)
                    {
                        myPopupMenu.show(getJList(), e.getPoint().x, e.getPoint().y);
                    }
                }
            }
        });

        myRenameBT = new JButton("Rename");
        myRenameBT.setToolTipText("Rename this key");
        myRenameBT.setMargin(new Insets(1, 2, 1, 2));
        myRenameBT.setFocusable(false);
        myRenameBT.addActionListener(this);

        myRemoveBT = MyButtons.createMinusButton();
        myRemoveBT.setToolTipText("Remove this key and return all components to their respective types below.");
        myRemoveBT.setMinimumSize(myRemoveBT.getPreferredSize());
        myRemoveBT.setMargin(new Insets(5, 5, 5, 5));
        myRemoveBT.setFocusable(false);
        myRemoveBT.addActionListener(this);

        JPanel btPanel = new JPanel();
        btPanel.setLayout(new BoxLayout(btPanel, BoxLayout.X_AXIS));
        btPanel.add(myRenameBT);
        btPanel.add(Box.createHorizontalStrut(2));
        btPanel.add(myRemoveBT);
        btPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 3, 3));

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(btPanel, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        if (e.getSource() == myRemoveBT)
        {
            myDataTypeMergePanel.removeMappedType(this);
        }
        else if (e.getSource() == myRenameBT)
        {
            Set<String> inUseNames = myDataTypeMergePanel.getDestinationKeyNames(true);
            inUseNames.remove(getKeyName().toLowerCase());

            boolean validNameSelected = false;
            boolean cancelled = false;
            String tempName = getKeyName();
            String message = "Enter the new name for the type:";
            while (!validNameSelected && !cancelled)
            {
                String result = JOptionPane.showInputDialog(this, message, tempName);
                if (result == null)
                {
                    cancelled = true;
                }
                else
                {
                    if (inUseNames.contains(result.toLowerCase()))
                    {
                        message = "The type name \"" + result
                                + "\" is already in use.\nPlease choose another name for the new type:";
                        validNameSelected = false;
                    }
                    else
                    {
                        validNameSelected = true;
                        myKeyName = result;
                        setBorder(BorderFactory.createTitledBorder(myKeyName));
                        myDataTypeMergePanel.targetPanelRenamed(this);
                    }
                }
            }
        }
    }

    @Override
    public boolean addTypeKeyEntry(TypeKeyEntry entry)
    {
        if (allowsTypeKeyEntry(entry, null))
        {
            entry.setOwner(this);
            getListModel().addElement(entry);
            sortListEntries();
            return true;
        }
        return false;
    }

    @Override
    public boolean allowsTypeKeyEntry(TypeKeyEntry entry, List<String> errors)
    {
        if (hasOneOfThisEntryDataType(entry))
        {
            if (errors != null)
            {
                errors.add("An entry of the type \"" + entry.getDataTypeDispName()
                        + "\" is already in the set.\nOnly one entry of each data type is allowed.");
            }
            return false;
        }
        else if (myRestrictByClassType && !entry.getClassType().equals(myClassType) && errors != null)
        {
            errors.add("An entry of the class \"" + entry.getClassType()
                    + "\" is not allowed in this set set.\nOnly one entries of the type \"" + myClassType + "\"are allowed.");
        }
        return true;
    }

    /**
     * Gets the class type.
     *
     * @return the class type
     */
    public String getClassType()
    {
        return myClassType;
    }

    /**
     * Gets the conversion hint.
     *
     * @return the conversion hint
     */
    public Tuple2<MergeKeySpecification.ConversionHint, String> getConversionHintAndMergeClassName()
    {
        ConversionTypeSet typeSet = new ConversionTypeSet();
        if (!getListModel().isEmpty())
        {
            for (Object obj : getListModel().toArray())
            {
                if (obj instanceof TypeKeyEntry)
                {
                    TypeKeyEntry tke = (TypeKeyEntry)obj;
                    String classType = tke.getClassType();
                    typeSet.addType(classType);
                }
            }
        }
        return typeSet.getConversionHintAndMergeClassName();
    }

    /**
     * Gets the key name.
     *
     * @return the key name
     */
    public String getKeyName()
    {
        return myKeyName;
    }

    /**
     * Checks for one of this entry data type.
     *
     * @param entry the entry
     * @return true, if successful
     */
    public boolean hasOneOfThisEntryDataType(TypeKeyEntry entry)
    {
        boolean hasOne = false;
        if (!getListModel().isEmpty())
        {
            Set<String> dtSet = new HashSet<>();
            for (Object obj : getListModel().toArray())
            {
                if (obj instanceof TypeKeyEntry && !Utilities.sameInstance(obj, entry))
                {
                    String dt = ((TypeKeyEntry)obj).getDataTypeKey();
                    dtSet.add(dt);
                }
            }
            hasOne = dtSet.contains(entry.getDataTypeKey());
        }
        return hasOne;
    }

    @Override
    public void keyMoveCompleted(TypeKeyEntry entry, TypeKeyPanel origPanel)
    {
        resetInnerPanelBorder();
    }

    @Override
    public void keyMoveInitiated(TypeKeyEntry entry, TypeKeyPanel sourcePanel, Object source)
    {
        if (allowsTypeKeyEntry(entry, null))
        {
            setInnerPanelBorderColor(Color.green);
        }
        else
        {
            setInnerPanelBorderColor(Color.red);
        }
    }

    @Override
    public void setEditable(boolean editable)
    {
        super.setEditable(editable);
        myRenameBT.setEnabled(editable);
        myRemoveBT.setEnabled(editable);
        myRemoveMI.setEnabled(editable);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(64);
        sb.append(getClass().getSimpleName()).append("Type[").append(myKeyName).append("] ClassType[").append(myClassType)
                .append("] RestrictByClass[ ").append(myRestrictByClassType).append("]\n");
        for (TypeKeyEntry tke : getTypeEntryList())
        {
            sb.append("   ").append(tke.toString()).append('\n');
        }
        return sb.toString();
    }

    /**
     * A set of conversion types.
     */
    private static class ConversionTypeSet
    {
        /** Flag indicating if a date type exists. */
        private boolean myHasDateType;

        /** Flag indicating if an float type exists. */
        private boolean myHasFloatType;

        /** Flag indicating if an integer type exists. */
        private boolean myHasIntegerType;

        /** Flag indicating if a string type exists. */
        private boolean myHasStringType;

        /** Flag indicating if an unrecognized type exists. */
        private boolean myHasUnrecognizedType;

        /** The set of types. */
        private final Set<String> myTypes = New.set();

        /**
         * Add a type to the type set.
         *
         * @param classType The type.
         */
        public void addType(String classType)
        {
            myTypes.add(classType);

            if (Integer.class.getName().equals(classType) || Byte.class.getName().equals(classType)
                    || Short.class.getName().equals(classType) || Long.class.getName().equals(classType))
            {
                myHasIntegerType = true;
            }
            else if (Double.class.getName().equals(classType) || Float.class.getName().equals(classType))
            {
                myHasFloatType = true;
            }
            else if (Date.class.getName().equals(classType))
            {
                myHasDateType = true;
            }
            else if (String.class.getName().equals(classType))
            {
                myHasStringType = true;
            }
            else
            {
                myHasUnrecognizedType = true;
            }
        }

        /**
         * Gets the conversion hint.
         *
         * @return the conversion hint
         */
        public Tuple2<MergeKeySpecification.ConversionHint, String> getConversionHintAndMergeClassName()
        {
            ConversionHint hint = ConversionHint.NONE;
            String mergeClassName = String.class.getName();

            if (myTypes.size() > 1)
            {
                if (myHasStringType || myHasDateType || myHasUnrecognizedType)
                {
                    hint = ConversionHint.CONVERT_TO_STRING;
                }
                else if (myHasFloatType && myHasIntegerType)
                {
                    hint = ConversionHint.CONVERT_TO_DOUBLE;
                    mergeClassName = Double.class.getName();
                }
                else if (myTypes.contains(Double.class.getName()))
                {
                    hint = ConversionHint.CONVERT_TO_DOUBLE;
                    mergeClassName = Double.class.getName();
                }
                else if (myTypes.contains(Long.class.getName()))
                {
                    hint = ConversionHint.CONVERT_TO_LONG;
                    mergeClassName = Long.class.getName();
                }
                else if (myTypes.contains(Integer.class.getName()))
                {
                    hint = ConversionHint.CONVERT_TO_INTEGER;
                    mergeClassName = Integer.class.getName();
                }
                else if (myTypes.contains(Short.class.getName()))
                {
                    hint = ConversionHint.CONVERT_TO_SHORT;
                    mergeClassName = Short.class.getName();
                }
            }
            else if (myTypes.size() == 1)
            {
                mergeClassName = myTypes.iterator().next();
            }
            return new Tuple2<>(hint, mergeClassName);
        }
    }

    /**
     * The Class CustomListCellRenderer.
     */
    private static class CustomListCellRenderer extends DefaultListCellRenderer
    {
        /**
         * Instantiates a new custom list cell renderer.
         */
        public CustomListCellRenderer()
        {
            super();
        }

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
                boolean cellHasFocus)
        {
            Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (c instanceof JLabel && value instanceof TypeKeyEntry)
            {
                TypeKeyEntry tke = (TypeKeyEntry)value;
                String classType = TypeKeyEntry.extractSimpleClassName(tke.getClassType());
                setText(tke.getKeyName() + "   [" + classType + "] [" + tke.getDataTypeDispName() + "]");
            }
            return c;
        }
    }
}
