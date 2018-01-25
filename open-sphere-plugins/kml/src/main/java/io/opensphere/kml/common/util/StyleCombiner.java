package io.opensphere.kml.common.util;

import java.util.function.BinaryOperator;
import java.util.function.Function;

import de.micromata.opengis.kml.v_2_2_0.BalloonStyle;
import de.micromata.opengis.kml.v_2_2_0.IconStyle;
import de.micromata.opengis.kml.v_2_2_0.LabelStyle;
import de.micromata.opengis.kml.v_2_2_0.LineStyle;
import de.micromata.opengis.kml.v_2_2_0.ListStyle;
import de.micromata.opengis.kml.v_2_2_0.PolyStyle;
import de.micromata.opengis.kml.v_2_2_0.Style;
import de.micromata.opengis.kml.v_2_2_0.SubStyle;
import io.opensphere.core.util.Utilities;

/** Combines KML styles. */
final class StyleCombiner
{
    /**
     * Combines two styles into one.
     *
     * @param submissiveStyle the submissive style
     * @param dominantStyle the dominant style
     * @return the combined style
     */
    public static Style combineStyles(Style submissiveStyle, Style dominantStyle)
    {
        Style style = new Style();
        style.setBalloonStyle(combineStyles(submissiveStyle, dominantStyle, s -> s.getBalloonStyle(), StyleCombiner::combine));
        style.setIconStyle(combineStyles(submissiveStyle, dominantStyle, s -> s.getIconStyle(), StyleCombiner::combine));
        style.setLabelStyle(combineStyles(submissiveStyle, dominantStyle, s -> s.getLabelStyle(), StyleCombiner::combine));
        style.setLineStyle(combineStyles(submissiveStyle, dominantStyle, s -> s.getLineStyle(), StyleCombiner::combine));
        style.setListStyle(combineStyles(submissiveStyle, dominantStyle, s -> s.getListStyle(), StyleCombiner::combine));
        style.setPolyStyle(combineStyles(submissiveStyle, dominantStyle, s -> s.getPolyStyle(), StyleCombiner::combine));
        return style;
    }

    /**
     * Combines particular sub styles of the given styles.
     *
     * @param <T> the type of the sub style
     * @param submissiveStyle the submissive style
     * @param dominantStyle the dominant style
     * @param getter the sub style getter
     * @param combiner the sub style combiner
     * @return The combined style
     */
    private static <T extends SubStyle> T combineStyles(Style submissiveStyle, Style dominantStyle, Function<Style, T> getter,
            BinaryOperator<T> combiner)
    {
        return Utilities.getNonNull(getter.apply(submissiveStyle), getter.apply(dominantStyle), combiner::apply);
    }

    /**
     * Combines balloon styles.
     *
     * @param submissiveStyle the submissive style
     * @param dominantStyle the dominant style
     * @return The combined style
     */
    private static BalloonStyle combine(BalloonStyle submissiveStyle, BalloonStyle dominantStyle)
    {
        BalloonStyle style = new BalloonStyle();
        style.setBgColor(dominantStyle.getBgColor() != null ? dominantStyle.getBgColor() : submissiveStyle.getBgColor());
        style.setTextColor(dominantStyle.getTextColor() != null ? dominantStyle.getTextColor() : submissiveStyle.getTextColor());
        style.setText(dominantStyle.getText() != null ? dominantStyle.getText() : submissiveStyle.getText());
        return style;
    }

    /**
     * Combines icon styles.
     *
     * @param submissiveStyle the submissive style
     * @param dominantStyle the dominant style
     * @return The combined style
     */
    private static IconStyle combine(IconStyle submissiveStyle, IconStyle dominantStyle)
    {
        IconStyle style = new IconStyle();
        style.setColor(dominantStyle.getColor() != null ? dominantStyle.getColor() : submissiveStyle.getColor());
        style.setHeading(dominantStyle.getHeading());
        style.setHotSpot(dominantStyle.getHotSpot() != null ? dominantStyle.getHotSpot() : submissiveStyle.getHotSpot());
        style.setIcon(dominantStyle.getIcon() != null ? dominantStyle.getIcon() : submissiveStyle.getIcon());
        style.setScale(dominantStyle.getScale());
        return style;
    }

    /**
     * Combines label styles.
     *
     * @param submissiveStyle the submissive style
     * @param dominantStyle the dominant style
     * @return The combined style
     */
    private static LabelStyle combine(LabelStyle submissiveStyle, LabelStyle dominantStyle)
    {
        LabelStyle style = new LabelStyle();
        style.setColor(dominantStyle.getColor() != null ? dominantStyle.getColor() : submissiveStyle.getColor());
        style.setScale(dominantStyle.getScale());
        return style;
    }

    /**
     * Combines line styles.
     *
     * @param submissiveStyle the submissive style
     * @param dominantStyle the dominant style
     * @return The combined style
     */
    private static LineStyle combine(LineStyle submissiveStyle, LineStyle dominantStyle)
    {
        LineStyle style = new LineStyle();
        style.setColor(dominantStyle.getColor() != null ? dominantStyle.getColor() : submissiveStyle.getColor());
        style.setWidth(dominantStyle.getWidth());
        return style;
    }

    /**
     * Combines list styles. TODO incomplete.
     *
     * @param submissiveStyle the submissive style
     * @param dominantStyle the dominant style
     * @return The combined style
     */
    private static ListStyle combine(ListStyle submissiveStyle, ListStyle dominantStyle)
    {
        return dominantStyle;
    }

    /**
     * Combines polygon styles.
     *
     * @param submissiveStyle the submissive style
     * @param dominantStyle the dominant style
     * @return The combined style
     */
    private static PolyStyle combine(PolyStyle submissiveStyle, PolyStyle dominantStyle)
    {
        PolyStyle style = new PolyStyle();
        style.setColor(dominantStyle.getColor() != null ? dominantStyle.getColor() : submissiveStyle.getColor());
        style.setFill(dominantStyle.isFill() != null ? dominantStyle.isFill() : submissiveStyle.isFill());
        style.setOutline(dominantStyle.isOutline() != null ? dominantStyle.isOutline() : submissiveStyle.isOutline());
        return style;
    }

    /** Private constructor. */
    private StyleCombiner()
    {
    }
}
