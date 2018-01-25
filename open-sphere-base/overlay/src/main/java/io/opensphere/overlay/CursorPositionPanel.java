package io.opensphere.overlay;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.Collection;

import javax.swing.JLabel;
import javax.swing.JPanel;

import io.opensphere.core.UnitsRegistry;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.units.UnitsProvider;
import io.opensphere.core.units.UnitsProvider.UnitsChangeListener;
import io.opensphere.core.units.angle.Angle;
import io.opensphere.core.units.length.Length;

/** Panel that displays the cursor position. */
public class CursorPositionPanel extends JPanel
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /** The Alt label. */
    private final JLabel myAltLabel = new JLabel();

    /** A listener for changes to the preferred angle units. */
    private final transient UnitsChangeListener<Angle> myAngleUnitsChangeListener = new UnitsChangeListener<Angle>()
    {
        @Override
        public void availableUnitsChanged(Class<Angle> superType, Collection<Class<? extends Angle>> newTypes)
        {
        }

        @Override
        public void preferredUnitsChanged(Class<? extends Angle> type)
        {
            myPreferredAngleUnits = type;
        }
    };

    /** The Lat label. */
    private final JLabel myLatLabel = new JLabel();

    /** A listener for changes to the preferred length units. */
    private final transient UnitsChangeListener<Length> myLengthUnitsChangeListener = new UnitsChangeListener<Length>()
    {
        @Override
        public void availableUnitsChanged(Class<Length> superType, Collection<Class<? extends Length>> newTypes)
        {
        }

        @Override
        public void preferredUnitsChanged(Class<? extends Length> type)
        {
            myPreferredLengthUnits = type;
        }
    };

    /** The Lon label. */
    private final JLabel myLonLabel = new JLabel();

    /** The MGRS label. */
    private final JLabel myMGRSLabel = new JLabel();

    /** The currently preferred angle units. */
    private volatile Class<? extends Angle> myPreferredAngleUnits;

    /** The currently preferred length units. */
    private volatile Class<? extends Length> myPreferredLengthUnits;

    /**
     * Constructor.
     *
     * @param font The font.
     * @param unitsRegistry The units registry.
     */
    public CursorPositionPanel(Font font, UnitsRegistry unitsRegistry)
    {
        super(new GridBagLayout());

        UnitsProvider<Length> lengthProvider = unitsRegistry.getUnitsProvider(Length.class);
        lengthProvider.addListener(myLengthUnitsChangeListener);
        myPreferredLengthUnits = lengthProvider.getPreferredUnits();
        UnitsProvider<Angle> angleProvider = unitsRegistry.getUnitsProvider(Angle.class);
        angleProvider.addListener(myAngleUnitsChangeListener);
        myPreferredAngleUnits = angleProvider.getPreferredUnits();

        setName("ViewerPosition");
        setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        this.add(createPanel(125, myMGRSLabel), gbc);

        gbc.gridx = 1;
        this.add(createPanel(100, myLatLabel), gbc);

        gbc.gridx = 2;
        this.add(createPanel(100, myLonLabel), gbc);

        gbc.gridx = 3;
        this.add(createPanel(75, myAltLabel), gbc);

        this.setSize(400, 20);
        setPreferredSize(this.getSize());
        setMinimumSize(this.getSize());
        setMaximumSize(this.getSize());

        myLatLabel.setFont(font);
        myLonLabel.setFont(font);
        myAltLabel.setFont(font);
        myMGRSLabel.setFont(font);
    }

    /**
     * Clear the label text.
     */
    public void clearText()
    {
        myMGRSLabel.setText("");
        myLatLabel.setText("");
        myLonLabel.setText("");
        myAltLabel.setText("");
    }

    /**
     * Get the alt text.
     *
     * @return The alt text.
     */
    public String getAltText()
    {
        return myAltLabel.getText();
    }

    /**
     * Get the lat text.
     *
     * @return The lat text.
     */
    public String getLatText()
    {
        return myLatLabel.getText();
    }

    /**
     * Get the lon text.
     *
     * @return The lon text.
     */
    public String getLonText()
    {
        return myLonLabel.getText();
    }

    /**
     * Get the mGRS text.
     *
     * @return The mGRS text.
     */
    public String getMGRSText()
    {
        return myMGRSLabel.getText();
    }

    /**
     * Set the screen position label.
     *
     * @param mgrsText The text of the first row label.
     * @param latLonAlt the loc map
     * @param hasElevationProvider the has elevation provider
     */
    public void setLabels(String mgrsText, LatLonAlt latLonAlt, boolean hasElevationProvider)
    {
        myMGRSLabel.setText(mgrsText);
        if (latLonAlt != null)
        {
            if (myPreferredAngleUnits != null)
            {
                Angle lat = Angle.create(myPreferredAngleUnits, latLonAlt.getLatD());
                Angle lon = Angle.create(myPreferredAngleUnits, latLonAlt.getLonD());

                myLatLabel.setText(lat.toShortLabelString(15, 6, 'N', 'S'));
                myLonLabel.setText(lon.toShortLabelString(15, 6, 'E', 'W'));
            }
            if (hasElevationProvider)
            {
                if (myPreferredLengthUnits != null)
                {
                    Length alt = Length.create(myPreferredLengthUnits, latLonAlt.getAltitude().getMagnitude());
                    myAltLabel.setText(alt.toShortLabelString(10, 0));
                }
            }
            else
            {
                myAltLabel.setText("");
            }
        }
    }

    /**
     * Create a fixed size panel to hold the given label.
     *
     * @param width The pixel width of the panel.
     * @param label The label which the panel will contain.
     * @return The newly created panel.
     */
    private JPanel createPanel(int width, JLabel label)
    {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setSize(width, 20);
        panel.setPreferredSize(panel.getSize());
        panel.setMaximumSize(panel.getSize());
        panel.add(label, BorderLayout.CENTER);
        return panel;
    }
}
