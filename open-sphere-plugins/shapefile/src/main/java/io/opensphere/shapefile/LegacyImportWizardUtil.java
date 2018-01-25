package io.opensphere.shapefile;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import io.opensphere.shapefile.config.v1.ShapeFileSource;

/** A utility class for helping the legacy import wizard. */
@SuppressWarnings("PMD.GodClass")
public final class LegacyImportWizardUtil
{
    /**
     * Helper function to reformat the label for the currently completed wizard
     * stage.
     *
     * @param label the label
     */
    public static void completeLabel(JLabel label)
    {
        if (label.isEnabled())
        {
            label.setEnabled(false);
            label.setText(label.getText().substring(0, label.getText().length() - 2));
        }
    }

    /**
     * Creates a text info panel.
     *
     * @param textFont the text font
     * @param text the text
     * @return JPanel
     */
    public static JPanel createInfoPanel(Font textFont, String text)
    {
        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        JTextArea jta = new JTextArea();
        jta.setFont(textFont);
        jta.setBorder(null);
        jta.setEditable(false);
        jta.setBackground(infoPanel.getBackground());
        jta.setText(text);
        infoPanel.add(jta, BorderLayout.CENTER);
        return infoPanel;
    }

    /**
     * Creates a JPanel limited to a certian height with the specified
     * background color to be used for spacing.
     *
     * @param height the height
     * @param c the c
     * @return the j panel
     */
    public static JPanel createSpacer(int height, Color c)
    {
        JPanel breakPanel = new JPanel();
        breakPanel.setMinimumSize(new Dimension(0, height));
        breakPanel.setMaximumSize(new Dimension(5000, height));
        if (c != null)
        {
            breakPanel.setBackground(c);
        }
        return breakPanel;
    }

    /**
     * Try to guess at the special column types.
     *
     * @param config The configuration which holds the special types.
     */
    public static void determineColumns(ShapeFileSource config)
    {
        String name;
        for (int i = 0; i < config.getColumnNames().size(); i++)
        {
            name = config.getColumnNames().get(i);

            if (config.getTimeColumn() == -1 && ("time".equalsIgnoreCase(name) || "timestamp".equalsIgnoreCase(name)))
            {
                config.setTimeColumn(i);
            }
            else if (config.getDateColumn() == -1 && "date".equalsIgnoreCase(name))
            {
                config.setDateColumn(i);
            }
            else if (config.getLobColumn() == -1 && ("lob".equalsIgnoreCase(name) || "bearing".equalsIgnoreCase(name)))
            {
                config.setLobColumn(i);
            }
            else if (config.getLobColumn() == -1 && ("sma".equalsIgnoreCase(name) || "semimajor".equalsIgnoreCase(name)
                    || "semi-major".equalsIgnoreCase(name) || "smaj".equalsIgnoreCase(name)))
            {
                config.setSmajColumn(i);
            }
            else if (config.getLobColumn() == -1 && ("smi".equalsIgnoreCase(name) || "smin".equalsIgnoreCase(name)
                    || "semiminor".equalsIgnoreCase(name) || "semi-minor".equalsIgnoreCase(name)))
            {
                config.setSminColumn(i);
            }
            else if (config.getLobColumn() == -1
                    && ("orient".equalsIgnoreCase(name) || "ornt".equalsIgnoreCase(name) || "orientation".equalsIgnoreCase(name)))
            {
                config.setOrientColumn(i);
            }
        }
    }

    /**
     * Try to guess at the special column types, but be less strict.
     *
     * @param config The configuration which holds the special types.
     */
    public static void determineColumnsLiberal(ShapeFileSource config)
    {
        String name;
        for (int i = 0; i < config.getColumnNames().size(); i++)
        {
            name = config.getColumnNames().get(i);

            if (config.getTimeColumn() == -1 && name.toLowerCase().startsWith("time"))
            {
                config.setTimeColumn(i);
            }

            if (config.getDateColumn() == -1 && name.toLowerCase().startsWith("date"))
            {
                config.setDateColumn(i);
            }

            if (config.getLobColumn() == -1 && name.toLowerCase().startsWith("lob"))
            {
                config.setLobColumn(i);
            }
        }
    }

    /**
     * Try to guess at the special column types, maybe we'll get lucky.
     *
     * @param config The configuration which holds the special types.
     */
    public static void graspAtStraws(ShapeFileSource config)
    {
        String name;
        for (int i = 0; i < config.getColumnNames().size(); i++)
        {
            name = config.getColumnNames().get(i);

            if (config.getTimeColumn() == -1 && name.toLowerCase().contains("time"))
            {
                config.setTimeColumn(i);
            }

            if (config.getDateColumn() == -1 && name.toLowerCase().contains("date"))
            {
                config.setDateColumn(i);
            }
        }
    }

    /**
     * Helper function to restore the label for a prevously run stage.
     *
     * @param label the label
     */
    public static void restoreLabel(JLabel label)
    {
        label.setEnabled(true);
        if (label.getText().endsWith("<"))
        {
            label.setText(label.getText().substring(0, label.getText().length() - 2));
        }
    }

    /**
     * Take any of the column types which were set in the configuration and add
     * them to the collection of defined columnTypes.
     *
     * @param config The configuration which holds the special types.
     * @param columnTypes The collection of defined columnTypes.
     */
    public static void setupColumnTypes(ShapeFileSource config, List<ColumnType> columnTypes)
    {
        if (config.getLobColumn() != -1)
        {
            columnTypes.remove(config.getLobColumn());
            columnTypes.add(config.getLobColumn(), ColumnType.LOB);
        }

        if (config.getSmajColumn() != -1)
        {
            columnTypes.remove(config.getSmajColumn());
            columnTypes.add(config.getSmajColumn(), ColumnType.SEMIMAJOR);
        }

        if (config.getSminColumn() != -1)
        {
            columnTypes.remove(config.getSminColumn());
            columnTypes.add(config.getSminColumn(), ColumnType.SEMIMINOR);
        }

        if (config.getOrientColumn() != -1)
        {
            columnTypes.remove(config.getOrientColumn());
            columnTypes.add(config.getOrientColumn(), ColumnType.ORIENTATION);
        }

        if (config.getTimeColumn() != -1 && config.getDateColumn() == -1)
        {
            columnTypes.remove(config.getTimeColumn());
            columnTypes.add(config.getTimeColumn(), ColumnType.TIMESTAMP);
        }
        else if (config.getTimeColumn() == -1 && config.getDateColumn() != -1)
        {
            columnTypes.remove(config.getDateColumn());
            columnTypes.add(config.getDateColumn(), ColumnType.TIMESTAMP);
        }
        else if (config.getTimeColumn() != -1 && config.getDateColumn() != -1)
        {
            columnTypes.remove(config.getTimeColumn());
            columnTypes.add(config.getTimeColumn(), ColumnType.TIME);

            columnTypes.remove(config.getDateColumn());
            columnTypes.add(config.getDateColumn(), ColumnType.DATE);
        }
    }

    /**
     * Helper function to reformat the label for the currently starting wizard
     * stage.
     *
     * @param label the label
     */
    public static void startLabel(JLabel label)
    {
        label.setText(label.getText() + " <");
    }

    /** Disallow instantiation. */
    private LegacyImportWizardUtil()
    {
    }

    /**
     * The Enum ColumnType.
     */
    public enum ColumnType
    {
        /** The DATE. */
        DATE,

        /** The LOB. */
        LOB,

        /** The ORIENTATION. */
        ORIENTATION,

        /** The OTHER. */
        OTHER,

        /** The SEMIMAJOR. */
        SEMIMAJOR,

        /** The SEMIMINOR. */
        SEMIMINOR,

        /** The TIME. */
        TIME,

        /** The TIMESTAMP. */
        TIMESTAMP
    }
}
