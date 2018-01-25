package io.opensphere.overlay;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.bric.swing.ColorPicker;

import io.opensphere.core.api.DefaultTransformer;
import io.opensphere.core.data.DataRegistry;
import io.opensphere.core.geometry.ImageManager;
import io.opensphere.core.geometry.PointGeometry;
import io.opensphere.core.geometry.PointSetGeometry;
import io.opensphere.core.geometry.SingletonImageProvider;
import io.opensphere.core.geometry.TileGeometry;
import io.opensphere.core.geometry.renderproperties.BaseAltitudeRenderProperties;
import io.opensphere.core.geometry.renderproperties.DefaultBaseAltitudeRenderProperties;
import io.opensphere.core.geometry.renderproperties.DefaultPointRenderProperties;
import io.opensphere.core.geometry.renderproperties.DefaultPointSizeRenderProperty;
import io.opensphere.core.geometry.renderproperties.DefaultTileRenderProperties;
import io.opensphere.core.geometry.renderproperties.PointRenderProperties;
import io.opensphere.core.geometry.renderproperties.PointSizeRenderProperty;
import io.opensphere.core.geometry.renderproperties.TileRenderProperties;
import io.opensphere.core.geometry.renderproperties.ZOrderRenderProperties;
import io.opensphere.core.model.Altitude;
import io.opensphere.core.model.GeographicConvexQuadrilateral;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.model.Position;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.image.ImageUtil;
import io.opensphere.core.util.swing.ColorIcon;

/**
 * Transformer for the overlay plug-in.
 */
class RandomDotTransformer extends DefaultTransformer
{
    /** Number of bits in a Java color. */
    private static final int COLOR_DEPTH = 24;

    /** Max latitude. */
    private static final float MAX_LAT = 90f;

    /** Max longitude. */
    private static final float MAX_LON = 180f;

    /** The number of random points. */
    private volatile int myDotCount;

    /** True when dots have been published. */
    private boolean myDotsPublished;

    /** An image provider which provides the broken image. */
    private final SingletonImageProvider myImageProvider = new SingletonImageProvider(ImageUtil.BROKEN_IMAGE);

    /** The number of sets to divide the dots into. */
    private int myNumSets;

    /** A list of point sets which have been published. */
    private final List<PointSetGeometry> myPointSets = New.list();

    /** The collection of random points. */
    private List<PointGeometry> myRandomPoints;

    /** The render properties currently in used. */
    private final List<PointRenderProperties> myRenderProperties = new ArrayList<>();

    /** The currently published test tile or null if no tile is published. */
    private TileGeometry myTile;

    /**
     * Constructor.
     */
    public RandomDotTransformer()
    {
        super((DataRegistry)null);
    }

    /** Allow modification of the properties of the dots. */
    public synchronized void manageDotProperties()
    {
        if (myRenderProperties.isEmpty())
        {
            return;
        }

        RandomDotPropertiesDialog dialog = new RandomDotPropertiesDialog();
        dialog.pack();
        dialog.setVisible(true);
    }

    /**
     * Publish a test tile or un-publish the tile if it is already published.
     */
    public void publishTile()
    {
        final double idecide = 22.;
        final double who = 30.;
        final double lives = 35.;
        final double ordies = 23.;
        // I decide.
        if (myTile == null)
        {
            GeographicPosition ll = new GeographicPosition(LatLonAlt.createFromDegrees(idecide, idecide));
            GeographicPosition lr = new GeographicPosition(LatLonAlt.createFromDegrees(idecide, who));
            GeographicPosition ur = new GeographicPosition(LatLonAlt.createFromDegrees(who, lives));
            GeographicPosition ul = new GeographicPosition(LatLonAlt.createFromDegrees(lives, ordies));

            GeographicConvexQuadrilateral location = new GeographicConvexQuadrilateral(ll, lr, ur, ul);

            TileGeometry.Builder<Position> tileBuilder = new TileGeometry.Builder<Position>();

            tileBuilder.setImageManager(new ImageManager((Void)null, myImageProvider));
            tileBuilder.setDivider(null);
            tileBuilder.setParent(null);
            tileBuilder.setRapidUpdate(true);
            tileBuilder.setBounds(location);

            TileRenderProperties props = new DefaultTileRenderProperties(10100, true, false);
            props.setRenderingOrder(5);
            props.setOpacity(1.0f);
            props.setHidden(false);

            myTile = new TileGeometry(tileBuilder, props, null);
            publishGeometries(Collections.singleton(myTile), Collections.<PointGeometry>emptySet());
        }
        else
        {
            publishGeometries(Collections.<PointGeometry>emptySet(), Collections.singleton(myTile));
            myTile = null;
        }
    }

    /**
     * Set the number of random dots to generate.
     *
     * @param count The count of random dots.
     * @param sets The number of sets.
     */
    public synchronized void setRandomDotCount(int count, int sets)
    {
        myDotCount = count;
        myNumSets = sets;
    }

    /**
     * Publishes some random dots.
     */
    protected synchronized void publishRandomDots()
    {
        if (!myDotsPublished)
        {
            myRandomPoints = New.list(myDotCount);
            Random random = new Random();
            float pointSize = 2f;
            PointGeometry.Builder<GeographicPosition> pointBuilder = new PointGeometry.Builder<GeographicPosition>();
            final float altM = 50f;

            for (int j = 0; j < myNumSets; ++j)
            {
                PointRenderProperties props = new DefaultPointRenderProperties(ZOrderRenderProperties.TOP_Z, true, true, true);
                props.setColor(new Color(random.nextInt(1 << COLOR_DEPTH)));
                props.setSize(pointSize);
                if (j % 2 == 0)
                {
                    props.setHighlightColor(Color.ORANGE);
                }
                myRenderProperties.add(props);
                for (int i = 0; i < myDotCount; ++i)
                {
                    LatLonAlt lla = LatLonAlt.createFromDegreesMeters((float)Math.random() * MAX_LAT * 2 - MAX_LAT,
                            (float)Math.random() * MAX_LON * 2 - MAX_LON, altM, Altitude.ReferenceLevel.TERRAIN);
                    pointBuilder.setPosition(new GeographicPosition(lla));
                    myRandomPoints.add(new PointGeometry(pointBuilder, props, null));
                }
            }

            List<PointGeometry> adds = New.list(myRandomPoints);
            publishGeometries(adds, Collections.<PointGeometry>emptySet());
            myDotsPublished = true;
        }
        else
        {
            List<PointGeometry> removes = New.list(myRandomPoints);
            publishGeometries(Collections.<PointGeometry>emptySet(), removes);
            myRandomPoints.clear();
            myRenderProperties.clear();
            myDotsPublished = false;
        }
    }

    /**
     * Publishes some random points as a PointSetGeometry.
     */
    protected synchronized void publishRandomSet()
    {
        if (myPointSets.isEmpty())
        {
            Random random = new Random();
            final float pointSize = 5f;
            PointSetGeometry.Builder<GeographicPosition> pointBuilder = new PointSetGeometry.Builder<GeographicPosition>();
            final float altM = 50f;

            for (int j = 0; j < myNumSets; ++j)
            {
                List<Color> colors = New.list(myDotCount);
                List<GeographicPosition> positions = New.list(myDotCount);

                BaseAltitudeRenderProperties baseRenderProperties = new DefaultBaseAltitudeRenderProperties(
                        ZOrderRenderProperties.TOP_Z, true, true, true);
                PointSizeRenderProperty sizeProperty = new DefaultPointSizeRenderProperty();
                sizeProperty.setSize(pointSize);
                PointRenderProperties props = new DefaultPointRenderProperties(baseRenderProperties, sizeProperty);

                Color color = new Color(random.nextInt(1 << COLOR_DEPTH));
                for (int i = 0; i < myDotCount; ++i)
                {
                    LatLonAlt lla = LatLonAlt.createFromDegreesMeters((float)Math.random() * MAX_LAT * 2 - MAX_LAT,
                            (float)Math.random() * MAX_LON * 2 - MAX_LON, altM, Altitude.ReferenceLevel.ELLIPSOID);
                    positions.add(new GeographicPosition(lla));
                    colors.add(color);
                }

                // Since all of the points have the same color, we could just
                // set the color in the render properties, but this will test
                // the color buffer instead.
                pointBuilder.setColors(colors);
                pointBuilder.setPositions(positions);
                myPointSets.add(new PointSetGeometry(pointBuilder, props, null));
            }

            List<PointSetGeometry> adds = New.list(myPointSets);
            publishGeometries(adds, Collections.<PointGeometry>emptySet());
        }
        else
        {
            List<PointSetGeometry> removes = New.list(myPointSets);
            publishGeometries(Collections.<PointGeometry>emptySet(), removes);
            myPointSets.clear();
        }
    }

    /** Dialog for changing render properties. */
    private class RandomDotPropertiesDialog extends JDialog
    {
        /** serialVersionUID. */
        private static final long serialVersionUID = 1L;

        /** Constructor. */
        public RandomDotPropertiesDialog()
        {
            JPanel topPanel = new JPanel(new GridLayout(myRenderProperties.size(), 1));
            setContentPane(topPanel);

            for (PointRenderProperties props : myRenderProperties)
            {
                addPropConfigLine(topPanel, props);
            }
        }

        /**
         * Add a line to the configuration dialog.
         *
         * @param topPanel The top level panel.
         * @param props The properties for this line.
         */
        private void addPropConfigLine(JPanel topPanel, final PointRenderProperties props)
        {
            JPanel subPanel = new JPanel(new FlowLayout());

            final ColorIcon colorIcon = new ColorIcon();
            colorIcon.setIconWidth(16);
            colorIcon.setIconHeight(12);
            colorIcon.setColor(props.getColor());

            JButton colorButton = new JButton(colorIcon);
            colorButton.setMargin(new Insets(3, 6, 3, 6));
            colorButton.setText("Dot Color");
            colorButton.setFocusPainted(false);
            colorButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent evt)
                {
                    Color selectColor = ColorPicker.showDialog(null, "Choose Dot Color", colorIcon.getColor(), true);
                    if (selectColor != null)
                    {
                        colorIcon.setColor(selectColor);
                        props.setColor(selectColor);
                    }
                }
            });
            subPanel.add(colorButton);

            final SpinnerNumberModel sizeSpinModel = new SpinnerNumberModel(2, 1, 50, 1);
            sizeSpinModel.addChangeListener(new ChangeListener()
            {
                @Override
                public void stateChanged(ChangeEvent e)
                {
                    props.setSize(sizeSpinModel.getNumber().intValue());
                }
            });
            JSpinner sizeSpin = new JSpinner(sizeSpinModel);
            subPanel.add(sizeSpin);

            // For the highlight size
            final SpinnerNumberModel hSizeSpinModel = new SpinnerNumberModel(5, 1, 50, 1);
            hSizeSpinModel.addChangeListener(new ChangeListener()
            {
                @Override
                public void stateChanged(ChangeEvent e)
                {
                    props.setHighlightSize(hSizeSpinModel.getNumber().intValue());
                }
            });
            JSpinner hSpin = new JSpinner(hSizeSpinModel);
            subPanel.add(hSpin);

            final JCheckBox check = new JCheckBox("Hidden");
            check.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    props.setHidden(check.isSelected());
                }
            });
            subPanel.add(check);

            topPanel.add(subPanel);
        }
    }
}
