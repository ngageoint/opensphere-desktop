package io.opensphere.analysis.heatmap;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.RadialGradientPaint;
import java.awt.image.BufferedImage;

import org.jdesktop.swingx.image.AbstractFilter;
import org.jdesktop.swingx.image.GaussianBlurFilter;

/** Creates heat maps. */
public class HeatmapCreator
{
    /**
     * Creates a buffered image from the model.
     *
     * @param model the model
     * @param options the options
     * @return the buffered image
     */
    public BufferedImage createImage(HeatmapModel model, HeatmapOptions options)
    {
        int width = model.getImageSize().width;
        int height = model.getImageSize().height;

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D g = (Graphics2D)image.getGraphics();

        double altitude = model.getMapManager().getStandardViewer().getAltitude() / 1000;
        double sizeAdjustment = 1 + Math.pow(altitude, .4) / 25.0;

        int size = (int)(options.getSize() * sizeAdjustment) / 10;

        combineGradients(model, g, size, options.getIntensity());
        mapToGradient(image, options.getGradient().getGradients());
        BufferedImage blur = blur(image);

        return blur;
    }

    /**
     * @param model
     * @param options
     * @param g
     */
    public void drawCircles(HeatmapModel model, HeatmapOptions options, Graphics2D g)
    {
        // Render points
        int radius = options.getSize();
        int diameter = radius * 2;
        model.forEachValue((Point coord, Integer count) ->
        {
            g.setPaint(Color.BLACK);
            g.setStroke(new BasicStroke(5, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.drawOval(coord.x - radius, coord.y - radius, diameter, diameter);
            g.drawLine(coord.x, coord.y - radius, coord.x, coord.y + radius);
            g.drawLine(coord.x-radius, coord.y, coord.x + radius, coord.y);
            g.setPaint(Color.WHITE);
            g.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.drawOval(coord.x - radius, coord.y - radius, diameter, diameter);
            g.drawLine(coord.x, coord.y - radius, coord.x, coord.y + radius);
            g.drawLine(coord.x-radius, coord.y, coord.x + radius, coord.y);
            g.setPaint(Color.RED);
            g.setStroke(new BasicStroke(1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.drawOval(coord.x - radius, coord.y - radius, diameter, diameter);
            g.drawLine(coord.x, coord.y - radius, coord.x, coord.y + radius);
            g.drawLine(coord.x-radius, coord.y, coord.x + radius, coord.y);
        });
    }

    /**
     * Paints a gradient around each pixel, combining them additively to produce
     * a new image representing pixel values.
     *
     * @param model the model
     * @param g the image graphics
     * @param size the size of the element boundary used to generate
     *            intersections.
     * @param intensity the number of points needed for the maximum intensity.
     */
    private void combineGradients(HeatmapModel model, Graphics2D g, float size, int intensity)
    {
        int radius = (int)size;
        int diameter = radius * 2;
        float[] gradientFractions = { 0f, 1f };
        Color[] gradientColors = new Color[] { null, Color.BLACK };
        double maxCount = intensity;

        // composite function: just keep adding color
        g.setComposite(new AddComposite());

        // Render points
        model.forEachValue((Point coord, Integer count) ->
        {
            double percent = count.intValue() / maxCount;
            gradientColors[0] = new Color(rgbFromPercent(percent));

            g.setPaint(new RadialGradientPaint(coord, size, gradientFractions, gradientColors));
            g.fillOval(coord.x - radius, coord.y - radius, diameter, diameter);
        });

        // Render lines & polygons
        if (!model.getPolylines().isEmpty() || !model.getPolygons().isEmpty())
        {
            final double lowColorPercent = 1.05 - .1 * (intensity / 50.0);
            Color shapeColor = new Color(rgbFromPercent(lowColorPercent));
            g.setColor(shapeColor);

            for (Polygon polygon : model.getPolygons())
            {
                g.fillPolygon(polygon);
            }

            g.setStroke(new BasicStroke(size, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            for (Polygon polygon : model.getPolylines())
            {
                g.drawPolyline(polygon.xpoints, polygon.ypoints, polygon.npoints);
            }
        }
    }

    /**
     * Maps the value of each pixel to a gradient color and updates the image.
     *
     * @param image the image
     * @param gradient the gradient
     */
    private void mapToGradient(BufferedImage image, int[] gradient)
    {
        for (int y = 0, h = image.getHeight(); y < h; y++)
        {
            for (int x = 0, w = image.getWidth(); x < w; x++)
            {
                int rgb = image.getRGB(x, y);
                if (rgb > 0)
                {
                    double percent = percentFromRgb(rgb);
                    int color = getColor(percent, gradient);
                    image.setRGB(x, y, color);
                }
            }
        }
    }

    /**
     * Blurs the image using a filter.
     *
     * @param image the image to blur.
     * @return a new {@link BufferedImage} in which the contents of the supplied
     *         image have been blurred.
     */
    private BufferedImage blur(BufferedImage image)
    {
        BufferedImage filteredImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
        AbstractFilter filter = new GaussianBlurFilter(8);
        filteredImage = filter.filter(image, filteredImage);
        return filteredImage;
    }

    /**
     * Converts a percent into an rgb value.
     *
     * @param percent the percent
     * @return the rgb value
     */
    private static int rgbFromPercent(double percent)
    {
        return (int)Math.round(0xFFFFFF * percent);
    }

    /**
     * Converts an rgb value into a percent.
     *
     * @param rgb the rgb value
     * @return the percent
     */
    private static double percentFromRgb(int rgb)
    {
        return (rgb & 0x00FFFFFF) / (double)0xFFFFFF;
    }

    /**
     * Gets the color for the given percent and gradients.
     *
     * @param percent the percent
     * @param gradient the gradients
     * @return the color in rgb
     */
    private static int getColor(double percent, int[] gradient)
    {
        int gradientIndex = (int)(gradient.length * percent);
        int color1 = gradient[Math.min(gradientIndex, gradient.length - 1)];

        /* Interpolate between the two colors that surround the value. We'll
         * leave the first three indices alone since they produce a pink
         * artifact with the current algorithm. */
        if (gradientIndex > 2 && gradientIndex < gradient.length - 1)
        {
            int color2 = gradient[gradientIndex + 1];

            double binPercent = gradientIndex / (double)gradient.length;
            double nextBinPercent = (gradientIndex + 1) / (double)gradient.length;

            color1 = interpolateColor(color1, color2, percent - binPercent, nextBinPercent - binPercent);
        }
        return color1;
    }

    /**
     * Interpolates between two colors.
     *
     * @param color1 the first color
     * @param color2 the second color
     * @param step the step?
     * @param max the max?
     * @return the interpolated color
     */
    private static int interpolateColor(int color1, int color2, double step, double max)
    {
        final long alphaMask = 0xFF000000L;
        long a1 = (color1 & alphaMask) >> 24;
        int r1 = (color1 & 0xFF0000) >> 16;
        int g1 = (color1 & 0xFF00) >> 8;
        int b1 = color1 & 0xFF;

        long a2 = (color2 & alphaMask) >> 24;
        int r2 = (color2 & 0xFF0000) >> 16;
        int g2 = (color2 & 0xFF00) >> 8;
        int b2 = color2 & 0xFF;
        int a3 = (int)Math.floor(interpolate(a1, a2, step, max)) & 0xFF;
        int r3 = (int)Math.floor(interpolate(r1, r2, step, max)) & 0xFF;
        int g3 = (int)Math.floor(interpolate(g1, g2, step, max)) & 0xFF;
        int b3 = (int)Math.floor(interpolate(b1, b2, step, max)) & 0xFF;

        int toReturn = ((a3 << 8 | r3) << 8 | g3) << 8 | b3;

        return toReturn;
    }

    /**
     * Interpolates between two values.
     *
     * @param begin the first value
     * @param end the second value
     * @param step the step?
     * @param max the max?
     * @return the interpolated value
     */
    private static double interpolate(long begin, long end, double step, double max)
    {
        long diff = end > begin ? end - begin : begin - end;
        double multiplier = Math.abs(step / max);
        double interpolateBy = diff * multiplier;

        if (interpolateBy > diff)
        {
            return end > begin ? end - diff >> 1 : end + diff >> 1;
        }
        else
        {
            return end > begin ? begin + interpolateBy : end + interpolateBy;
        }
    }
}
