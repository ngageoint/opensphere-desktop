package io.opensphere.core.util.swing.pie;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.UIManager;

import io.opensphere.core.util.ColorUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.swing.pie.IconModel.IconInfo;

/**
 * Multi-level pie chart.
 *
 * @param <T> The type of the data
 */
@SuppressWarnings("PMD.GodClass")
public class MultiLevelPieChart<T> extends JComponent
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /** The gap between the outer ring and labels. */
    private static final int LABEL_GAP = 4;

    /** The gap between the outer ring and icons. */
    private static final int ICON_GAP = 9;

    /** The data model. */
    private transient MultiLevelPieChartModel<T> myDataModel;

    /** The renderer. */
    private final transient MultiLevelPieCellRenderer<T> myRenderer;

    /** The selection model. */
    private final transient DefaultCellSelectionModel mySelectionModel;

    /** The icon model. */
    private final transient DefaultIconModel myIconModel;

    /** The Map wheel. */
    private List<List<OpsClockCell>> myMapWheel;

    /** Whether the mouse is being dragged. */
    private boolean myIsDragging;

    /** The Pie chart legend. */
    private final transient MultiLevelPieChartLegend<T> myPieChartLegend;

    /** The center point of the pie chart. */
    private final Point myCenterPoint = new Point(0, 0);

    /** The pie chart radius to the outer ring. */
    private float myRadius;

    /** The label radius (max of label width/height). */
    private double myLabelRadius;

    /** The icon radius (max of label width/height). */
    private double myIconRadius;

    /** The icon bounds. */
    private final transient Map<IconInfo, Rectangle> myIconBounds = New.map();

    /**
     * Indicates if the legend is visible or not.
     */
    private boolean myLegendVisible = true;

    /**
     * The color of the font.
     */
    private Color myFontColor;

    /** The selection border color. */
    private Color mySelectionColor = Color.LIGHT_GRAY;

    /**
     * Gets the color to paint given the renderer color and selection state.
     *
     * @param rendererColor the renderer color
     * @param isSelected whether the cell is selected
     * @param isMouseOver whether the mouse is over the cell
     * @return the color to paint
     */
    private static Color getColor(Color rendererColor, boolean isSelected, boolean isMouseOver)
    {
        Color color = rendererColor;
        if ((isSelected || isMouseOver) && rendererColor != null)
        {
            int brightness = ColorUtilities.getBrightness(rendererColor);
            if (brightness >= 125)
            {
                color = isSelected ? rendererColor.darker().darker() : rendererColor.darker();
            }
            else
            {
                int shift = isSelected ? 96 : 64;
                int red = Math.min(rendererColor.getRed() + shift, 255);
                int green = Math.min(rendererColor.getGreen() + shift, 255);
                int blue = Math.min(rendererColor.getBlue() + shift, 255);
                color = new Color(red, green, blue);
            }
        }
        return color;
    }

    /**
     * Constructor.
     *
     * @param model the model
     * @param renderer the renderer
     * @param valueComparator the value comparator, used for the legend
     */
    public MultiLevelPieChart(MultiLevelPieChartModel<T> model, MultiLevelPieCellRenderer<T> renderer,
            Comparator<T> valueComparator)
    {
        super();
        myPieChartLegend = new MultiLevelPieChartLegend<>(model, renderer, valueComparator);
        myDataModel = model;
        myRenderer = renderer;
        mySelectionModel = new DefaultCellSelectionModel(myDataModel.getSliceCount(), myDataModel.getRingCount());
        myIconModel = new DefaultIconModel();
        setFontColor(UIManager.getColor("Label.foreground"));
        addListeners();
    }

    /**
     * Gets the data model.
     *
     * @return the data model
     */
    public MultiLevelPieChartModel<T> getDataModel()
    {
        return myDataModel;
    }

    /**
     * Gets the font color for the chart.
     *
     * @return The font color.
     */
    public Color getFontColor()
    {
        return myFontColor;
    }

    /**
     * Gets the icon model.
     *
     * @return the icon model
     */
    public IconModel getIconModel()
    {
        return myIconModel;
    }

    /**
     * Gets the selection model.
     *
     * @return the selection model
     */
    public CellSelectionModel getSelectionModel()
    {
        return mySelectionModel;
    }

    @Override
    public void invalidate()
    {
        myMapWheel = null;
        super.invalidate();
    }

    /**
     * Sets the data model.
     *
     * @param dataModel the data model
     */
    public void setDataModel(MultiLevelPieChartModel<T> dataModel)
    {
        myDataModel = dataModel;
        myPieChartLegend.setDataModel(dataModel);
        mySelectionModel.updateCounts(myDataModel.getSliceCount(), myDataModel.getRingCount());
        myMapWheel = null;

        repaint();
    }

    /**
     * Sets the color of the font.
     *
     * @param color The color of the font.
     */
    public final void setFontColor(Color color)
    {
        myFontColor = color;
        myPieChartLegend.setTextColor(color);
    }

    /**
     * Sets if the legend is visible or not.
     *
     * @param isVisible True if visible, false if not.
     */
    public void setLegendVisible(boolean isVisible)
    {
        myLegendVisible = isVisible;
        repaint();
    }

    /**
     * Set selection color.
     *
     * @param selectionColor selection color
     */
    public void setSelectionColor(Color selectionColor)
    {
        mySelectionColor = selectionColor;
    }

    @Override
    protected void paintComponent(Graphics g)
    {
        super.paintComponent(g);

        if (myLegendVisible)
        {
            myPieChartLegend.paintLegend(g, getHeight());
        }

        paintCells(g);
        paintRingLabels(g);
        paintIcons(g);
        paintArcs(g);
        paintSliceLabels(g);
    }

    /**
     * Adds internal listeners to the chart.
     */
    private void addListeners()
    {
        addMouseListener(new MouseAdapter()
        {
            // Wire in cell selection
            @Override
            public void mouseClicked(MouseEvent e)
            {
                Point cell = getCellForPosition(e);
                if (cell != null)
                {
                    if ((e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) != 0)
                    {
                        if (mySelectionModel.isCellSelected(cell))
                        {
                            mySelectionModel.removeSelectedCell(cell, this);
                        }
                        else
                        {
                            mySelectionModel.addSelectedCell(cell, this);
                        }
                    }
                    else if ((e.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK) != 0)
                    {
                        mySelectionModel.setFinalCell(cell, false, this);
                    }
                    else
                    {
                        mySelectionModel.setSelectedCell(cell, this);
                    }
                    mySelectionModel.setAnchorCell(cell);
                }
            }

            // Clear drag selection
            @Override
            public void mouseReleased(MouseEvent e)
            {
                myIsDragging = false;
            }
        });

        addMouseMotionListener(new MouseMotionAdapter()
        {
            // Wire in cell drag selection
            @Override
            public void mouseDragged(MouseEvent e)
            {
                Point cell = getCellForPosition(e);
                if (cell != null)
                {
                    if (!myIsDragging)
                    {
                        mySelectionModel.setAnchorCell(cell);
                    }

                    boolean keepSelected = (e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) != 0;
                    mySelectionModel.setFinalCell(cell, keepSelected, this);
                    myIsDragging = true;
                }
            }

            // Wire in cell/icon mouse-over
            @Override
            public void mouseMoved(MouseEvent e)
            {
                Point cell = getCellForPosition(e);
                mySelectionModel.setMouseOverCell(cell, this);

                IconInfo iconInfo = null;
                Point mousePoint = e.getPoint();
                for (Map.Entry<IconInfo, Rectangle> entry : myIconBounds.entrySet())
                {
                    Rectangle bounds = entry.getValue();
                    if (bounds.contains(mousePoint))
                    {
                        iconInfo = entry.getKey();
                        break;
                    }
                }
                myIconModel.setMouseOverIcon(iconInfo, this);
            }
        });

        // Repaint when the selection model changes
        mySelectionModel.addChangeListener((changeType, source) -> repaint());
    }

    /**
     * Draws a string with a border.
     *
     * @param g The graphics.
     * @param text The text.
     * @param x The x location.
     * @param y The y location.
     */
    private void drawStringWithBorder(Graphics g, String text, int x, int y)
    {
        g.setColor(Color.black);
        g.drawString(text, x - 1, y + 1);
        g.drawString(text, x - 1, y - 1);
        g.drawString(text, x + 1, y + 1);
        g.drawString(text, x + 1, y - 1);
        g.drawString(text, x, y + 1);
        g.drawString(text, x, y - 1);
        g.drawString(text, x + 1, y);
        g.drawString(text, x - 1, y);
        g.setColor(myFontColor);
        g.drawString(text, x, y);
    }

    /**
     * Converts a screen coordinate relative to the chart, to a cell in the
     * model.
     *
     * @param e the mouse event
     * @return the cell, or null
     */
    private Point getCellForPosition(MouseEvent e)
    {
        Point cellPoint = null;
        Point mousePoint = e.getPoint();
        for (List<OpsClockCell> ring : getWheel(null))
        {
            for (OpsClockCell segment : ring)
            {
                if (segment.contains(mousePoint))
                {
                    cellPoint = segment.getTableCell();
                    break;
                }
            }
        }
        return cellPoint;
    }

    /**
     * Converts an angle and radius to a point in component space.
     *
     * @param modelAngle the model angle, where 0 &lt;= angle &lt; 360, and 0
     *            means up
     * @param radius the radius
     * @return the point
     */
    private Point getPoint(double modelAngle, double radius)
    {
        double angleInRadians = Math.toRadians(modelAngle);
        double cos = Math.cos(angleInRadians);
        double sin = Math.sin(angleInRadians);
        int x = myCenterPoint.x + (int)Math.round(radius * sin);
        int y = myCenterPoint.y + (int)Math.round(radius * -cos);
        return new Point(x, y);
    }

    /**
     * Converts an angle and radius to a point in component space. The item size
     * is used to calculate an offset for painting the item (label, icon, etc.)
     *
     * @param modelAngle the model angle, where 0 &lt;= angle &lt; 360, and 0
     *            means up
     * @param radius the radius
     * @param itemWidth the width of the item to paint
     * @param itemHeight the height of the item to paint
     * @param alignmentY the vertical alignment of the item when it's painted
     * @return the point
     */
    private Point getPoint(double modelAngle, double radius, double itemWidth, double itemHeight, float alignmentY)
    {
        double angleInRadians = Math.toRadians(modelAngle);
        double cos = Math.cos(angleInRadians);
        double sin = Math.sin(angleInRadians);
        double dx = (sin - 1.) * itemWidth / 2.;
        double dy = (cos + 1. - 2. * alignmentY) * itemHeight / -2.;
        int x = myCenterPoint.x + (int)Math.round(radius * sin + dx);
        int y = myCenterPoint.y + (int)Math.round(radius * -cos + dy);
        return new Point(x, y);
    }

    /**
     * Gets the radius of the pie chart to the outer ring.
     *
     * @param g the Graphics object
     * @return the radius
     */
    private float getRadius(Graphics g)
    {
        final int legendMaxX = myPieChartLegend.getLegendBounds() != null
                ? myPieChartLegend.getLegendBounds().x + myPieChartLegend.getLegendBounds().width : 0;

                float radius = Math.min(getWidth() - legendMaxX, getHeight()) / 2f;

                // Calculate buffer for slice labels
                if (g != null)
                {
                    double labelRadius = 0.;
                    for (int slice = 0; slice < myDataModel.getSliceCount(); slice++)
                    {
                        Rectangle2D labelBounds = g.getFontMetrics().getStringBounds(myDataModel.getSliceName(slice), g);
                        if (labelBounds.getWidth() > labelRadius)
                        {
                            labelRadius = labelBounds.getWidth();
                        }
                        if (labelBounds.getHeight() > labelRadius)
                        {
                            labelRadius = labelBounds.getHeight();
                        }
                    }
                    myLabelRadius = labelRadius;
                }

                // Calculate buffer for icons
                double iconRadius = 0.;
                for (IconInfo iconInfo : myIconModel.getIcons())
                {
                    Image icon = iconInfo.getIcon();
                    int width = icon.getWidth(null);
                    int height = icon.getHeight(null);
                    if (width > iconRadius)
                    {
                        iconRadius = width;
                    }
                    if (height > iconRadius)
                    {
                        iconRadius = height;
                    }
                }
                if (iconRadius > 0)
                {
                    myIconRadius = iconRadius;
                }

                radius -= Math.max(myLabelRadius + LABEL_GAP, myIconRadius + ICON_GAP);

                return radius;
    }

    /**
     * Gets the set of slices for each ring of the circle.
     *
     * @param g the Graphics object
     * @return the wheel
     */
    private List<List<OpsClockCell>> getWheel(Graphics g)
    {
        if (myMapWheel == null)
        {
            myMapWheel = New.list(myDataModel.getRingCount());
            myRadius = getRadius(g);
            // Make the inner circle scale with the chart
            float innerCircleRadius = myRadius / 20;
            float ringThickness = (myRadius - innerCircleRadius) / myDataModel.getRingCount();
            float extent = 360f / myDataModel.getSliceCount();
            int centerX = getWidth() >> 1;
            int centerY = getHeight() >> 1;
            myCenterPoint.setLocation(centerX, centerY);

            for (int ring = 0; ring < myDataModel.getRingCount(); ring++)
            {
                List<OpsClockCell> segments = New.list(myDataModel.getSliceCount());
                myMapWheel.add(segments);

                // To prevent the first slice from having 0 thickness, start
                // with ring + 1 when calculating the x and y as well as when
                // creating the segments.
                float innerRadius = ringThickness * (ring + 1) + innerCircleRadius;
                float x = centerX - innerRadius;
                float y = centerY - innerRadius;

                // InnerRadius can be less than 0 in the case where the ops
                // clock panel is resized to a very small size. It becomes
                // unusable when very small but would throw illegal path state
                // exceptions. The '> 0' check for innerRadius prevents this
                // issue.
                if (innerRadius > 0)
                {
                    float startAngle = 90;
                    for (int slice = 0; slice < myDataModel.getSliceCount(); slice++)
                    {
                        OpsClockSegment segment = new OpsClockSegment(ring + 1, slice, ringThickness, startAngle, extent,
                                innerCircleRadius);
                        OpsClockCell path = new OpsClockCell(segment, x, y, startAngle, myDataModel.getSliceCount() - 1 - slice,
                                myDataModel.getRingCount() - 1 - ring);
                        startAngle += extent;
                        PathIterator pi = segment.getPathIterator(AffineTransform.getTranslateInstance(x, y));
                        path.append(pi, true);
                        segments.add(path);
                    }
                }
            }
        }
        return myMapWheel;
    }

    /**
     * Paint arcs.
     *
     * @param g the Graphics object
     */
    private void paintArcs(Graphics g)
    {
        Graphics2D g2d = (Graphics2D)g;
        int xOrigin = (int)(myCenterPoint.x - myRadius);
        int yOrigin = (int)(myCenterPoint.y - myRadius);
        int arcDiameter = (int)(2 * myRadius + 2);
        g2d.setStroke(new BasicStroke(3));
        for (Arc arc : myIconModel.getArcs())
        {
            g2d.setColor(arc.getColor());

            // Since java2D arcs start at 3 o'clock for 0 degrees and the pie
            // chart starts at 12 o'clock for 0 degrees, translate the angles.
            g2d.drawArc(xOrigin, yOrigin, arcDiameter, arcDiameter, 450 - (int)arc.getStartAngle(), -arc.getExtent());
        }
    }

    /**
     * Paints the cells.
     *
     * @param g the Graphics object
     */
    private void paintCells(Graphics g)
    {
        Graphics2D g2d = (Graphics2D)g;

        List<List<OpsClockCell>> wheel = getWheel(g);
        float innerCircleRadius = myRadius / 20;
        float ringThickness = (myRadius - innerCircleRadius) / myDataModel.getRingCount();
        final float extent = 360f / myDataModel.getSliceCount();

        paintCellBackgrounds(g2d, wheel);
        paintGrid(g2d, innerCircleRadius, ringThickness, extent);
        paintSelections(g2d, wheel, innerCircleRadius, ringThickness, extent);
    }

    /**
     * Paints the cell backgrounds.
     *
     * @param g2d the Graphics object
     * @param wheel the wheel
     */
    private void paintCellBackgrounds(Graphics2D g2d, List<List<OpsClockCell>> wheel)
    {
        for (List<OpsClockCell> ring : wheel)
        {
            for (OpsClockCell segment : ring)
            {
                T value = myDataModel.getValueAt(segment.getTableCell().x, segment.getTableCell().y);
                boolean isMouseOver = mySelectionModel.isMouseOverCell(segment.getTableCell());
                Color rendererColor = myRenderer.getColor(value);
                Color color = getColor(rendererColor, false, isMouseOver);

                // Fill the cell
                if (color != null)
                {
                    g2d.setColor(color);
                    g2d.fill(segment);
                }
            }
        }
    }

    /**
     * Paints the grid.
     *
     * @param g2d the Graphics object
     * @param innerCircleRadius the inner circle radius
     * @param ringThickness the ring thickness
     * @param extent the slice extent
     */
    private void paintGrid(Graphics2D g2d, float innerCircleRadius, float ringThickness, float extent)
    {
        g2d.setColor(Color.GRAY);

        //        // Draw the rings
        //        for (int ring = 0; ring <= myDataModel.getRingCount(); ring++)
        //        {
        //            float innerRadius = ringThickness * ring + innerCircleRadius;
        //            if (innerRadius > 0)
        //            {
        //                int xOrigin = Math.round(myCenterPoint.x - innerRadius);
        //                int yOrigin = Math.round(myCenterPoint.y - innerRadius);
        //                int arcDiameter = Math.round(2 * innerRadius);
        //                g2d.drawArc(xOrigin, yOrigin, arcDiameter, arcDiameter, 0, 360);
        //            }
        //        }
        //
        //        // Draw the slices
        //        for (int slice = 0; slice < myDataModel.getSliceCount(); slice++)
        //        {
        //            float angle = slice * extent;
        //            Point startPoint = getPoint(angle, innerCircleRadius);
        //            Point endPoint = getPoint(angle, myRadius);
        //            g2d.drawLine(startPoint.x, startPoint.y, endPoint.x, endPoint.y);
        //        }

        // Old way
        for (List<OpsClockCell> ring : getWheel(g2d))
        {
            for (OpsClockCell segment : ring)
            {
                g2d.draw(segment);
            }
        }
    }

    /**
     * Paints the selections.
     *
     * @param g2d the Graphics object
     * @param wheel the wheel
     * @param innerCircleRadius the inner circle radius
     * @param ringThickness the ring thickness
     * @param extent the slice extent
     */
    private void paintSelections(Graphics2D g2d, List<List<OpsClockCell>> wheel, float innerCircleRadius, float ringThickness,
            float extent)
    {
        // Draw the selected cell borders
        g2d.setStroke(new BasicStroke(3));
        g2d.setColor(mySelectionColor);
        for (List<OpsClockCell> ring : wheel)
        {
            for (OpsClockCell segment : ring)
            {
                boolean isSelected = mySelectionModel.isCellSelected(segment.getTableCell());
                if (isSelected)
                {
                    g2d.draw(segment);
                }
            }
        }

        // Draw the selection dots
        final float radiusOffset = innerCircleRadius + ringThickness / 2;
        float angle = extent / 2f;
        for (int slice = 0; slice < myDataModel.getSliceCount(); slice++)
        {
            for (int ring = 0; ring < myDataModel.getRingCount(); ring++)
            {
                boolean isSelected = mySelectionModel.isCellSelected(new Point(slice, ring));
                if (isSelected)
                {
                    T value = myDataModel.getValueAt(slice, ring);
                    Color rendererColor = myRenderer.getColor(value);
                    Color oppositeColor = new Color(rendererColor.getRGB() ^ 0x00ffffff);

                    float radius = ringThickness * (myDataModel.getRingCount() - ring - 1) + radiusOffset;
                    Point point = getPoint(angle, radius);
                    g2d.setColor(oppositeColor);
                    g2d.drawOval(point.x, point.y, 2, 2);
                }
            }
            angle += extent;
        }
    }

    /**
     * Paints the icons.
     *
     * @param g the Graphics object
     */
    private void paintIcons(Graphics g)
    {
        float radius = myRadius + ICON_GAP;
        for (IconInfo iconInfo : myIconModel.getIcons())
        {
            Image icon = iconInfo.getIcon();
            float angle = iconInfo.getAngle();
            Point point = getPoint(angle, radius, icon.getWidth(null), icon.getHeight(null), TOP_ALIGNMENT);
            g.drawImage(icon, point.x, point.y, null);

            // Save off the icon bounds to support icon mouse-over
            Rectangle bounds = new Rectangle(point, new Dimension(icon.getWidth(null), icon.getHeight(null)));
            myIconBounds.put(iconInfo, bounds);
        }
    }

    /**
     * Paints the ring labels.
     *
     * @param g the Graphics object
     */
    private void paintRingLabels(Graphics g)
    {
        g.setColor(myFontColor);
        final int labelYOffset = g.getFont().getSize() >> 1;
            for (List<OpsClockCell> ring : getWheel(g))
            {
                for (OpsClockCell segment : ring)
                {
                    if (segment.getRingLabelLocation() != null)
                    {
                        String ringName = myDataModel.getRingName(segment.getTableCell().y);
                        drawStringWithBorder(g, ringName, segment.getRingLabelLocation().x + 2,
                                segment.getRingLabelLocation().y + labelYOffset);
                    }
                }
            }
    }

    /**
     * Paints the slice labels.
     *
     * @param g the Graphics object
     */
    private void paintSliceLabels(Graphics g)
    {
        g.setColor(myFontColor);
        float extent = 360f / myDataModel.getSliceCount();
        float radius = myRadius + LABEL_GAP;
        for (int slice = 0; slice < myDataModel.getSliceCount(); slice++)
        {
            String sliceName = myDataModel.getSliceName(slice);
            Rectangle2D labelBounds = g.getFontMetrics().getStringBounds(sliceName, g);
            float angle = extent * slice;
            Point point = getPoint(angle, radius, labelBounds.getWidth(), labelBounds.getHeight(), BOTTOM_ALIGNMENT);
            // The +/- 1 here are to account for Java2D apparently painting the
            // labels offset a little
            drawStringWithBorder(g, sliceName, point.x + 1, point.y - 1);
        }
    }
}
