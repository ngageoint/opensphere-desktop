package io.opensphere.controlpanels.timeline.chart;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import io.opensphere.controlpanels.timeline.AbstractTimelineLayer;
import io.opensphere.controlpanels.timeline.chart.model.ChartLayerModel;
import io.opensphere.controlpanels.timeline.chart.model.ChartLayerModels;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.model.time.TimeSpanFormatter;
import io.opensphere.core.timeline.StyledTimelineDatum;
import io.opensphere.core.timeline.TimelineDatum;
import io.opensphere.core.timeline.TimelineDrawType;
import io.opensphere.core.util.ColorUtilities;
import io.opensphere.core.util.MathUtil;
import io.opensphere.core.util.awt.AWTUtilities;
import io.opensphere.core.util.collections.StreamUtilities;
import io.opensphere.core.util.lang.StringUtilities;

/**
 * Chart layer that directly plots time spans.
 */
class DirectChartLayer extends AbstractTimelineLayer
{
    /** The layer models. */
    private final ChartLayerModels myLayerModels;

    /** The tool tip text. */
    private String myToolTipText;

    /** Whether this layer is the current chart type. */
    private boolean myIsCurrentChart;

    /**
     * Constructor.
     *
     * @param layerModels the layer models
     */
    public DirectChartLayer(ChartLayerModels layerModels)
    {
        super();
        myLayerModels = layerModels;
    }

    /**
     * Sets whether this layer is the current chart type.
     *
     * @param isCurrentChart whether this is the current chart type
     */
    public void setCurrentChart(boolean isCurrentChart)
    {
        myIsCurrentChart = isCurrentChart;
    }

    @Override
    public String getToolTipText(MouseEvent event, String incoming)
    {
        return myToolTipText;
    }

    @Override
    public void paint(Graphics2D g2d)
    {
        super.paint(g2d);

        List<ChartLayerModel> layersToPaint = StreamUtilities.filter(myLayerModels.getLayers(),
            layer -> layer.isVisible() && (myIsCurrentChart || !layer.isBinnable()));
        if (!layersToPaint.isEmpty())
        {
            paintLayers(g2d, layersToPaint);
        }
    }

    /**
     * Paints the layers.
     *
     * @param g2d the graphics context
     * @param layers the layers
     */
    private void paintLayers(Graphics2D g2d, Collection<ChartLayerModel> layers)
    {
        Point cursorPosition = getUIModel().getCursorPosition();
        myToolTipText = null;
        int verticalSpacing = getUIModel().getTimelinePanelBounds().height / (layers.size() + 1);
        int y = AWTUtilities.getMaxY(getUIModel().getTimelinePanelBounds()) - verticalSpacing / 2;
        int barHeight = 8;
        TimeSpan uiSpan = getUIModel().getUISpan().get();
        Rectangle rect = new Rectangle();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        for (ChartLayerModel layer : layers)
        {
            y -= verticalSpacing;
            for (TimelineDatum datum : layer.getTimeSpans())
            {
                StyledTimelineDatum styledDatum = null;
                if (datum instanceof StyledTimelineDatum)
                {
                    styledDatum = (StyledTimelineDatum)datum;
                }

                TimeSpan span = datum.getTimeSpan();
                if ((myIsCurrentChart || !span.isInstantaneous() || styledDatum != null) && uiSpan.overlaps(span))
                {
                    int startX;
                    int endX = 0;

                    if (span.isInstantaneous())
                    {
                        startX = getUIModel().timeToX(span.getStart());

                        if (styledDatum != null && TimelineDrawType.LINE.equals(styledDatum.getDrawType()))
                        {
                            rect.setBounds(startX, y - barHeight * 2, 2, barHeight * 4);
                        }
                        else
                        {
                            rect.setBounds(MathUtil.subtractSafe(startX, DOT_PADDING), y - DOT_PADDING, DOT_CIRCUMFERENCE,
                                    DOT_CIRCUMFERENCE);
                        }
                    }
                    else
                    {
                        startX = span.isUnboundedStart() ? getUIModel().getTimelinePanelBounds().x - 1
                                : getUIModel().timeToX(span.getStart());
                        endX = span.isUnboundedEnd() ? AWTUtilities.getMaxX(getUIModel().getTimelinePanelBounds())
                                : getUIModel().timeToX(span.getEnd());
                        rect.setBounds(startX, y - (barHeight >> 1), MathUtil.subtractSafe(endX, startX), barHeight);
                    }

                    boolean isSelected = cursorPosition != null && rect.contains(cursorPosition);
                    if (isSelected)
                    {
                        myToolTipText = StringUtilities.concat(layer.getName(), " ", TimeSpanFormatter.toDisplayString(span));
                    }

                    Color color = isSelected ? layer.getSelectedColor() : layer.getColor();
                    if (styledDatum != null && styledDatum.getColor() != null)
                    {
                        color = styledDatum.getColor();
                    }

                    if (span.isInstantaneous())
                    {
                        g2d.setColor(color);
                        g2d.fillOval(rect.x, rect.y, rect.width, rect.height);
                    }
                    else
                    {
                        g2d.setColor(ColorUtilities.opacitizeColor(color, 50));
                        g2d.fill(rect);
                        g2d.setColor(color);
                        g2d.draw(rect);
                    }

                    drawText(g2d, styledDatum, rect);
                }
            }
        }
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_DEFAULT);
    }

    /**
     * Draws the text of the datum, if there is one.
     *
     * @param g2d The graphics to draw to.
     * @param styledDatum The datum.
     * @param rect The location the datum was drawn.
     */
    private void drawText(Graphics2D g2d, StyledTimelineDatum styledDatum, Rectangle rect)
    {
        if (styledDatum != null && StringUtils.isNotEmpty(styledDatum.getText()))
        {
            g2d.setColor(styledDatum.getTextColor());
            g2d.drawChars(styledDatum.getText().toCharArray(), 0, styledDatum.getText().length(), rect.x + 5, rect.y + 5);
        }
    }
}
