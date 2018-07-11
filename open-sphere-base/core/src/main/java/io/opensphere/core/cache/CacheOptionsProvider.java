package io.opensphere.core.cache;

import java.awt.Color;
import java.awt.Dimension;
import java.math.BigInteger;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.PlainDocument;

import io.opensphere.core.Toolbox;
import io.opensphere.core.options.impl.AbstractOptionsProvider;
import io.opensphere.core.preferences.PreferenceChangeEvent;
import io.opensphere.core.preferences.PreferenceChangeListener;
import io.opensphere.core.preferences.Preferences;
import io.opensphere.core.quantify.QuantifyToolboxUtils;
import io.opensphere.core.util.Constants;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.swing.DocumentListenerAdapter;

/**
 * Cache option provider.
 */
public class CacheOptionsProvider extends AbstractOptionsProvider
{
    /** The preferences key for the size limit. */
    public static final String SIZE_LIMIT_PREFS_KEY = "sizeLimitBytes";

    /** System property value for the default database size limit. */
    private static final long DEFAULT_SIZE_LIMIT_BYTES = Utilities.parseSystemProperty("opensphere.db.defaultSizeHintMB", -1L)
            * Constants.BYTES_PER_MEGABYTE;

    /** The cache. */
    private final Cache myCache;

    /** A label for displaying error messages. */
    private JLabel myErrorLabel;

    /** A listener for preferences changes. */
    private final PreferenceChangeListener myPreferenceChangeListener;

    /** The preferences. */
    private final Preferences myPrefs;

    /** A checkbox for enabling the database size limit. */
    private JCheckBox mySizeLimitEnabledCheckbox;

    /** The size limit entry field. */
    private JTextField mySizeLimitField;

    /** The label for the size limit field. */
    private JLabel mySizeLimitLabel;

    private final Toolbox myToolbox;

    /**
     * Constructor.
     *
     * @param prefsRegistry The preferences registry.
     * @param cache The cache instance.
     */
    public CacheOptionsProvider(Toolbox toolbox, Cache cache)
    {
        super("Cache");
        myToolbox = toolbox;
        myPrefs = toolbox.getPreferencesRegistry().getPreferences(Cache.class);
        myCache = cache;

        myPreferenceChangeListener = new PreferenceChangeListener()
        {
            @Override
            public void preferenceChange(PreferenceChangeEvent evt)
            {
                myCache.setOnDiskSizeLimitBytes(evt.getValueAsLong(DEFAULT_SIZE_LIMIT_BYTES));
            }
        };
        myPrefs.addPreferenceChangeListener(SIZE_LIMIT_PREFS_KEY, myPreferenceChangeListener);

        cache.setOnDiskSizeLimitBytes(myPrefs.getLong(SIZE_LIMIT_PREFS_KEY, DEFAULT_SIZE_LIMIT_BYTES));
    }

    @Override
    public void applyChanges()
    {
        if (mySizeLimitEnabledCheckbox.isSelected())
        {
            final int size = getSizeLimitFromField();
            if (size < 0)
            {
                JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(mySizeLimitEnabledCheckbox),
                        myErrorLabel.getText());
            }
            else
            {
                myPrefs.putLong(SIZE_LIMIT_PREFS_KEY, (long)size * Constants.BYTES_PER_MEGABYTE, this);
            }
        }
        else
        {
            myPrefs.putLong(SIZE_LIMIT_PREFS_KEY, -1L, this);
        }
    }

    @Override
    public JPanel getOptionsPanel()
    {
        final long sizeLimitBytes = myPrefs.getLong(SIZE_LIMIT_PREFS_KEY, DEFAULT_SIZE_LIMIT_BYTES);

        final JComponent sizeLimitPanel = new JPanel();
        sizeLimitPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 0));

        mySizeLimitEnabledCheckbox = new JCheckBox();
        mySizeLimitEnabledCheckbox.setSelected(sizeLimitBytes > 0L);
        mySizeLimitEnabledCheckbox.addChangeListener(e ->
        {
            QuantifyToolboxUtils.collectMetric(myToolbox, "mist3d.settings.cache.size-limit-enabled-checkbox");
            mySizeLimitLabel.setEnabled(mySizeLimitEnabledCheckbox.isSelected());
            mySizeLimitField.setEnabled(mySizeLimitEnabledCheckbox.isSelected());
        });
        sizeLimitPanel.add(mySizeLimitEnabledCheckbox);

        mySizeLimitLabel = new JLabel("Cache Size Threshold:");

        final PlainDocument sizeLimitDocument = new PlainDocument();
        sizeLimitDocument.setDocumentFilter(new DocumentFilter()
        {
            @Override
            public void insertString(FilterBypass fb, int offset, String string, javax.swing.text.AttributeSet attr)
                throws javax.swing.text.BadLocationException
            {
                if (string.matches("\\d+"))
                {
                    super.insertString(fb, offset, string, attr);
                }
            }

            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
                throws BadLocationException
            {
                if (text.matches("\\d+"))
                {
                    super.replace(fb, offset, length, text, attrs);
                }
            }
        });

        mySizeLimitField = new JTextField(sizeLimitDocument, null, 10);
        if (sizeLimitBytes > 0L)
        {
            mySizeLimitField.setText(Long.toString(sizeLimitBytes / Constants.BYTES_PER_MEGABYTE));
        }
        mySizeLimitField.setEnabled(mySizeLimitEnabledCheckbox.isSelected());
        sizeLimitPanel.add(mySizeLimitLabel);
        sizeLimitPanel.add(mySizeLimitField);
        sizeLimitPanel.add(new JLabel("MB"));

        sizeLimitDocument.addDocumentListener(new DocumentListenerAdapter()
        {
            @Override
            protected void updateAction(DocumentEvent e)
            {
                getSizeLimitFromField();
            }
        });

        final JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        final JTextArea jta = new JTextArea("Configure a size limit on the cache. The cache may temporarily"
                + " grow larger than this limit, but then the data that expires the earliest will be deleted.");
        jta.setLineWrap(true);
        jta.setWrapStyleWord(true);
        jta.setEditable(false);
        jta.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        jta.setMaximumSize(new Dimension(480, 50));
        panel.add(jta);
        panel.add(sizeLimitPanel);
        myErrorLabel = new JLabel();
        myErrorLabel.setForeground(Color.RED);
        panel.add(myErrorLabel);
        return panel;
    }

    @Override
    public void restoreDefaults()
    {
        if (DEFAULT_SIZE_LIMIT_BYTES <= 0L)
        {
            mySizeLimitEnabledCheckbox.setSelected(false);
            mySizeLimitLabel.setEnabled(false);
            mySizeLimitField.setEnabled(false);
        }
        else
        {
            mySizeLimitEnabledCheckbox.setSelected(true);
            mySizeLimitLabel.setEnabled(true);
            mySizeLimitField.setEnabled(true);
            mySizeLimitField.setText(Long.toString(DEFAULT_SIZE_LIMIT_BYTES / Constants.BYTES_PER_MEGABYTE));
        }
        applyChanges();
    }

    /**
     * Get the size limit from the text field.
     *
     * @return The size limit, or -1 if it is invalid.
     */
    protected int getSizeLimitFromField()
    {
        try
        {
            final String text = mySizeLimitField.getText();
            final BigInteger size = text.isEmpty() ? BigInteger.ZERO : new BigInteger(text);
            if (size.compareTo(BigInteger.valueOf(100)) < 0)
            {
                myErrorLabel.setText("Size cannot be less than 100 MB");
                return -1;
            }
            else if (size.compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) > 0)
            {
                myErrorLabel.setText("Value is too large");
                return -1;
            }
            else
            {
                myErrorLabel.setText("");
                return size.intValue();
            }
        }
        catch (final NumberFormatException e1)
        {
            myErrorLabel.setText("Value could not be parsed");
            return -1;
        }
    }
}
