package io.opensphere.hud.dashboard.widget;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import io.opensphere.core.metrics.NumberMetricsProvider;
import io.opensphere.core.metrics.PercentageMetricsProvider;
import io.opensphere.core.metrics.RangedNumberMetricsProvider;
import io.opensphere.core.preferences.Preferences;
import io.opensphere.core.util.swing.AbstractHUDPanel;
import io.opensphere.core.util.swing.EventQueueUtilities;

/**
 * The Class NumericWidgetDecorator.
 */
public class NumericWidgetDecorator extends AbstractHUDPanel
{
    /** The Constant _5_0F. */
    private static final float ourFontSizePlus5 = 5.0f;

    /**
     * serialVersionUID.
     */
    private static final long serialVersionUID = 1L;

    /** The Mode. */
    private Mode myMode = Mode.EMPTY;

    /** The Bar with persistence. */
    private final BarWithPersistence myBarWithPersistence;

    /** The Time history plot. */
    private final TimeHistoryPlot myTimeHistoryPlot;

    /** The I font. */
    private Font myIFont;

    /** The O font. */
    private Font myOFont;

    /** The Mouse in bounds. */
    private boolean myMouseInBounds;

    /** The Mouse pressed. */
    private boolean myMousePressed;

    /** The Preference key. */
    private final String myPreferenceKey;

    /** The preferences. */
    private final Preferences myPrefs;

    /**
     * Gets the preference key.
     *
     * @param provider the provider
     * @return the preference key
     */
    private static String getPreferenceKey(NumberMetricsProvider provider)
    {
        StringBuilder sb = new StringBuilder(32);
        sb.append("NumericWidgetDecorator.");
        if (provider.getTopic() != null)
        {
            sb.append(provider.getTopic()).append('_');
        }
        if (provider.getSubTopic() != null)
        {
            sb.append(provider.getSubTopic()).append('_');
        }
        if (provider.getLabel() != null)
        {
            sb.append(provider.getLabel());
        }
        sb.append(".Mode");
        return sb.toString();
    }

    /**
     * Instantiates a new numeric widget decorator.
     *
     * @param provider the provider
     * @param prefs optional preferences
     */
    public NumericWidgetDecorator(NumberMetricsProvider provider, Preferences prefs)
    {
        super();
        myPrefs = prefs;
        myPreferenceKey = getPreferenceKey(provider);
        myBarWithPersistence = new BarWithPersistence(new Dimension(100, 20), true, 50, BarWithPersistence.Orientation.HORIZONTAL,
                Color.orange, Color.DARK_GRAY, 1);
        myBarWithPersistence.setBorderOn(false);
        myTimeHistoryPlot = new TimeHistoryPlot(20, 100, new Color(0, 0, 0, 0), Color.orange, 1, TimeHistoryPlot.PlotType.BAR);
        setOpaque(false);
        addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                toggleMode();
            }

            @Override
            public void mouseEntered(MouseEvent e)
            {
                myMouseInBounds = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e)
            {
                myMouseInBounds = false;
                myMousePressed = false;
                repaint();
            }

            @Override
            public void mousePressed(MouseEvent e)
            {
                myMousePressed = true;
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e)
            {
                myMousePressed = false;
                repaint();
            }
        });

        if (myPrefs != null)
        {
            myMode = Mode.valueOf(myPrefs.getString(myPreferenceKey, Mode.EMPTY.toString()));
        }
    }

    /**
     * Adds the value.
     *
     * @param provider the provider
     */
    public void addValue(NumberMetricsProvider provider)
    {
        int val = 0;
        if (provider instanceof PercentageMetricsProvider)
        {
            val = (int)(provider.getValueAsNumber().doubleValue() * 100);
            myBarWithPersistence.setMaxBarUnits(100);
            myTimeHistoryPlot.setMaxBarUnits(100);
        }
        else if (provider instanceof RangedNumberMetricsProvider)
        {
            RangedNumberMetricsProvider rnP = (RangedNumberMetricsProvider)provider;
            double minV = rnP.getMinValue().doubleValue();
            double maxV = rnP.getMaxValue().doubleValue() - minV;
            double dVal = rnP.getValueAsNumber().doubleValue() - minV;
            double p = dVal / maxV;
            val = (int)p;
            myBarWithPersistence.setMaxBarUnits(100);
            myTimeHistoryPlot.setMaxBarUnits(100);
        }
        else
        {
            val = provider.getValueAsNumber().intValue();
            int max = myBarWithPersistence.getMaxBarUnits();
            if (val > max)
            {
                myBarWithPersistence.setMaxBarUnits(val);
            }
            max = myTimeHistoryPlot.getMaxBarUnits();
            if (val > max)
            {
                myTimeHistoryPlot.setMaxBarUnits(val);
            }
        }

        myBarWithPersistence.addMeas(val);
        myTimeHistoryPlot.addMeas(val);

        if (myMode != Mode.EMPTY)
        {
            EventQueueUtilities.runOnEDT(new Runnable()
            {
                @Override
                public void run()
                {
                    revalidate();
                    repaint();
                }
            });
        }
    }

    @Override
    public void paint(Graphics g)
    {
        super.paint(g);
        Color w = new Color(255, 255, 255, 150);

        if (myIFont == null)
        {
            myIFont = g.getFont().deriveFont(Font.BOLD);
            myIFont = myIFont.deriveFont(myIFont.getSize() - 1.0f);
            myOFont = g.getFont().deriveFont(g.getFont().getSize() + ourFontSizePlus5);
        }
        Graphics2D g2D = (Graphics2D)g;
        switch (myMode)
        {
            case BAR:
                myBarWithPersistence.setDim(getWidth(), getHeight());
                myBarWithPersistence.reDraw();
                myBarWithPersistence.setMaxPersistMeasures(getWidth());
                g2D.drawImage(myBarWithPersistence.getImage(), 0, 0, null);
                break;
            case HISTORY:
                myTimeHistoryPlot.setDim(getWidth(), getHeight());
                myTimeHistoryPlot.reDraw();
                g2D.drawImage(myTimeHistoryPlot.getImage(), 0, 0, null);
                break;
            default:
                Font f = g2D.getFont();
                g2D.setColor(w);
                g2D.setFont(myIFont);
                g2D.drawString("i", getWidth() / 2 - 1, getHeight() / 2 + 4);
                g2D.setFont(myOFont);
                g2D.drawString("O", getWidth() / 2 - 6, getHeight() / 2 + 6);
                g2D.setFont(f);
                break;
        }

        if (myMousePressed)
        {
            g2D.setColor(w);
            g2D.fillRect(0, 0, getWidth() - 1, getHeight() - 1);
        }

        if (myMouseInBounds)
        {
            g2D.setColor(w);
            g2D.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
        }
    }

    /**
     * Sets the mode.
     *
     * @param aMode the new mode
     */
    public void setMode(final Mode aMode)
    {
        if (myPrefs != null)
        {
            myPrefs.putString(myPreferenceKey, aMode.toString(), this);
        }
        EventQueueUtilities.runOnEDT(new Runnable()
        {
            @Override
            public void run()
            {
                myMode = aMode;
                revalidate();
                repaint();
            }
        });
    }

    /**
     * Toggle mode.
     */
    public void toggleMode()
    {
        switch (myMode)
        {
            case EMPTY:
                setMode(Mode.BAR);
                break;
            case BAR:
                setMode(Mode.HISTORY);
                break;
            case HISTORY:
                setMode(Mode.EMPTY);
                break;
            default:
                break;
        }
    }

    /**
     * The Enum Mode.
     */
    public enum Mode
    {
        /** The EMPTY. */
        EMPTY,

        /** The BAR. */
        BAR,

        /** The HISTORY. */
        HISTORY
    }
}
