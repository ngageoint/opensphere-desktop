package io.opensphere.mantle.controller.impl;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import io.opensphere.core.Toolbox;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.swing.ListComboBoxModel;
import io.opensphere.core.util.swing.ToStringProxy;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.MetaDataInfo;
import io.opensphere.mantle.data.impl.DefaultMetaDataInfo;
import io.opensphere.mantle.util.MantleToolboxUtils;
import io.opensphere.mantle.util.NumericDataDeterminationUtil.NumericDetermination;

/**
 * The Class NumericColumnReset.
 */
public final class NumericColumnReset
{
    /**
     * Show numeric column reset dialog.
     *
     * @param tb the {@link Toolbox}
     * @param parent the {@link Component} for the dialog.
     */
    public static void showNumericColumnResetDialog(final Toolbox tb, Component parent)
    {
        List<DataTypeInfo> typeList = MantleToolboxUtils.getMantleToolbox(tb).getDataTypeController().getDataTypeInfo();
        if (!typeList.isEmpty())
        {
            final NumericColumnResetPanel panel = new NumericColumnResetPanel(tb, typeList);

            int result = JOptionPane.showConfirmDialog(parent, panel, "Choose Data Type", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION)
            {
                final DataTypeInfo dti = panel.getChosenDataType();
                final String key = panel.getChosenKey();

                Thread t = new Thread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        if (key == null)
                        {
                            dti.getMetaDataInfo().resetNumericMapForAllKeys(tb);
                        }
                        else
                        {
                            dti.getMetaDataInfo().resetKeyNumeric(tb, key);
                        }
                    }
                });
                t.start();
            }
        }
    }

    /**
     * Do not allow instantiation.
     */
    private NumericColumnReset()
    {
    }

    /**
     * The Class KeyInfo.
     */
    private static class KeyInfo
    {
        /** The Key class. */
        private final String myKeyDetermination;

        /** The Key name. */
        private final String myKeyName;

        /**
         * Instantiates a new key info.
         *
         * @param keyname the key name
         * @param keyDetermination the key determination
         */
        public KeyInfo(String keyname, String keyDetermination)
        {
            myKeyName = keyname;
            myKeyDetermination = keyDetermination;
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

        @Override
        public String toString()
        {
            return String.format("%-35s [%-12s]", myKeyName, myKeyDetermination);
        }
    }

    /**
     * The Class NumericColumnResetPanel.
     */
    @SuppressWarnings("serial")
    private static class NumericColumnResetPanel extends JPanel
    {
        /** The Column {@link JComboBox}. */
        private final JComboBox<Object> myColumnComboBox;

        /** The Data types {@link JComboBox}. */
        private final JComboBox<DataTypeInfoDisplayNameProxy> myDataTypesComboBox;

        /** The Toolbox. */
        private final Toolbox myToolbox;

        /**
         * Instantiates a new numeric column reset panel.
         *
         * @param tb the tb
         * @param typeList the type list
         */
        public NumericColumnResetPanel(Toolbox tb, List<DataTypeInfo> typeList)
        {
            myToolbox = tb;
            List<DataTypeInfoDisplayNameProxy> proxyList = DataTypeInfoDisplayNameProxy.toProxyList(typeList, null);

            myDataTypesComboBox = new JComboBox<>(new ListComboBoxModel<>(proxyList));
            myDataTypesComboBox.setFont(new Font(Font.MONOSPACED, Font.PLAIN, myDataTypesComboBox.getFont().getSize()));
            myDataTypesComboBox.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    rebuildColumnList();
                }
            });

            myColumnComboBox = new JComboBox<>();
            myColumnComboBox.setFont(new Font(Font.MONOSPACED, Font.PLAIN, myColumnComboBox.getFont().getSize()));

            StringBuilder builder = new StringBuilder(512);
            builder.append("As data types are added, the application attempts to determine which columns "
                    + "are numeric if not identified by the data provider. If sample data is "
                    + "limited from the provider, the application may choose the incorrect column. "
                    + "Often this manifests when the Count By tool and Histogram can not do "
                    + "numeric binning on a column that is composed of numbers. When this "
                    + "happens, reset the determination for the column to try to correct the "
                    + "problem. Then resort the column of interest or re-select it in the Count By Tool");

            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            JTextArea jta = new JTextArea(builder.toString());
            jta.setEditable(false);
            jta.setBackground(getBackground());
            jta.setWrapStyleWord(true);
            jta.setLineWrap(true);
            jta.setBorder(BorderFactory.createEmptyBorder());
            add(jta);

            JPanel dtPanel = new JPanel(new BorderLayout());
            dtPanel.add(new JLabel("Data Type"), BorderLayout.WEST);
            dtPanel.add(myDataTypesComboBox, BorderLayout.CENTER);
            add(dtPanel);

            JPanel keyPanel = new JPanel(new BorderLayout());
            keyPanel.add(new JLabel("Column"), BorderLayout.WEST);
            keyPanel.add(myColumnComboBox, BorderLayout.CENTER);
            add(keyPanel);

            dtPanel.setMinimumSize(new Dimension(430, 40));
            dtPanel.setPreferredSize(new Dimension(430, 40));
            dtPanel.setMaximumSize(new Dimension(430, 40));

            keyPanel.setMinimumSize(new Dimension(430, 40));
            keyPanel.setPreferredSize(new Dimension(430, 40));
            keyPanel.setMaximumSize(new Dimension(430, 40));
            add(Box.createVerticalStrut(20));

            setMinimumSize(new Dimension(450, 300));
            setPreferredSize(new Dimension(450, 300));
            setMaximumSize(new Dimension(2000, 860));
            rebuildColumnList();
        }

        /**
         * Gets the chosen data type.
         *
         * @return the chosen data type
         */
        public DataTypeInfo getChosenDataType()
        {
            return ((DataTypeInfoDisplayNameProxy)myDataTypesComboBox.getSelectedItem()).getItem();
        }

        /**
         * Gets the chosen key.
         *
         * @return the chosen key
         */
        public String getChosenKey()
        {
            String result = null;
            Object item = myColumnComboBox.getSelectedItem();
            if (item instanceof KeyInfo)
            {
                result = ((KeyInfo)item).getKeyName();
            }
            return result;
        }

        /**
         * Rebuild column list.
         */
        @SuppressWarnings("unchecked")
        private void rebuildColumnList()
        {
            ToStringProxy<DataTypeInfo> type = (ToStringProxy<DataTypeInfo>)myDataTypesComboBox.getSelectedItem();

            List<Object> cbList = New.list();
            cbList.add("All Columns");
            if (type != null && type.getItem() != null && type.getItem().getMetaDataInfo() != null)
            {
                MetaDataInfo mdi = type.getItem().getMetaDataInfo();
                List<String> keyNames = mdi.getKeyNames();
                List<KeyInfo> keyInfoList = New.list(keyNames.size());
                for (String key : keyNames)
                {
                    NumericDetermination dt = NumericDetermination.UNDETERMINED;
                    if (mdi instanceof DefaultMetaDataInfo)
                    {
                        dt = ((DefaultMetaDataInfo)mdi).checkKeyNumericDetermination(myToolbox, key);
                    }
                    if (!Objects.equals(NumericDetermination.UNDETERMINED, dt))
                    {
                        keyInfoList.add(new KeyInfo(key, dt.toString()));
                    }
                }

                Collections.sort(keyInfoList, new Comparator<KeyInfo>()
                {
                    @Override
                    public int compare(KeyInfo o1, KeyInfo o2)
                    {
                        return o1.toString().compareTo(o2.toString());
                    }
                });
                cbList.addAll(keyInfoList);
            }
            myColumnComboBox.setModel(new ListComboBoxModel<>(cbList));
        }
    }
}
