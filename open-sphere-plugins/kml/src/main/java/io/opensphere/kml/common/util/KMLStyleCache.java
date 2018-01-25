package io.opensphere.kml.common.util;

import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;

import org.apache.commons.lang3.StringUtils;

import de.micromata.opengis.kml.v_2_2_0.DisplayMode;
import de.micromata.opengis.kml.v_2_2_0.Pair;
import de.micromata.opengis.kml.v_2_2_0.Style;
import de.micromata.opengis.kml.v_2_2_0.StyleMap;
import de.micromata.opengis.kml.v_2_2_0.StyleSelector;
import de.micromata.opengis.kml.v_2_2_0.StyleState;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.kml.common.model.KMLController;
import io.opensphere.kml.common.model.KMLDataEvent;
import io.opensphere.kml.common.model.KMLDataSource;
import io.opensphere.kml.common.model.KMLFeature;
import io.opensphere.kml.common.model.KMLStyleMap;

/**
 * Cache for Style data.
 */
@ThreadSafe
public final class KMLStyleCache implements KMLController
{
    /** The style map (dataTypeKey to (url to Style)). */
    @GuardedBy("this")
    private final Map<String, Map<String, Style>> myStyleMap;

    /** The style map map (dataTypeKey to (url to StyleMap)). */
    @GuardedBy("this")
    private final Map<String, Map<String, StyleMap>> myStyleMapMap;

    /**
     * Constructor.
     */
    public KMLStyleCache()
    {
        myStyleMap = New.map();
        myStyleMapMap = New.map();
    }

    @Override
    public void addData(KMLDataEvent dataEvent, boolean reload)
    {
        KMLDataSource dataSource = dataEvent.getDataSource();
        KMLFeature feature = dataEvent.getData();
        String styleUrlPrefix = getStyleUrlPrefix(dataSource);

        synchronized (this)
        {
            List<Style> styles = New.list();
            new StyleFeatureAccumulator().accumulate(feature, styles);
            for (Style style : styles)
            {
                String styleUrl = StringUtilities.concat(styleUrlPrefix, style.getId());

                Map<String, Style> urlToStyleMap = myStyleMap.computeIfAbsent(dataSource.getDataGroupKey(), k -> New.map());
                urlToStyleMap.put(styleUrl, style);
            }

            List<StyleMap> styleMaps = New.list();
            new StyleMapFeatureAccumulator().accumulate(feature, styleMaps);
            for (StyleMap styleMap : styleMaps)
            {
                String styleUrl = StringUtilities.concat(styleUrlPrefix, styleMap.getId());

                Map<String, StyleMap> urlToStyleMap = myStyleMapMap.computeIfAbsent(dataSource.getDataGroupKey(), k -> New.map());
                urlToStyleMap.put(styleUrl, styleMap);
            }
        }
    }

    @Override
    public synchronized void removeData(KMLDataSource dataSource)
    {
        myStyleMap.remove(dataSource.getDataGroupKey());
        myStyleMapMap.remove(dataSource.getDataGroupKey());
    }

    /**
     * Returns whether the given feature supports balloons.
     *
     * @param feature The feature
     * @return Whether the given feature supports balloons
     */
    public boolean supportsBalloon(KMLFeature feature)
    {
        boolean hideBalloon = false;
        String balloonStyleText = null;
        if (feature.getDataSource() != null)
        {
            Style style = getStyle(feature, StyleState.NORMAL);
            if (style != null && style.getBalloonStyle() != null)
            {
                hideBalloon = style.getBalloonStyle().getDisplayMode() == DisplayMode.HIDE;
                balloonStyleText = style.getBalloonStyle().getText();
            }
        }
        return !hideBalloon
                && (balloonStyleText != null || feature.getDescription() != null || feature.getExtendedData() != null);
    }

    /**
     * Gets the Style from the cache for the given data source and Feature.
     *
     * @param feature The feature
     * @param styleState The style state
     * @return The Style in the cache, or null
     */
    public Style getStyle(KMLFeature feature, StyleState styleState)
    {
        Style urlStyle = null;
        if (!StringUtils.isBlank(feature.getStyleUrl()))
        {
            urlStyle = getUrlStyle(feature, styleState, feature.getStyleUrl().trim());
        }

        Style selectorStyle = null;
        StyleSelector styleSelector = CollectionUtilities.getLastItemOrNull(feature.getStyleSelector());
        if (styleSelector != null)
        {
            selectorStyle = getSelectorStyle(feature, styleState, styleSelector);
        }

        Style style = Utilities.getNonNull(urlStyle, selectorStyle, StyleCombiner::combineStyles);
        return style;
    }

    /**
     * Gets the URL style.
     *
     * @param feature The feature
     * @param styleState The style state
     * @param styleUrl The style URL
     * @return The style, or null
     */
    private Style getUrlStyle(KMLFeature feature, StyleState styleState, String styleUrl)
    {
        Style style = null;
        if (isInternal(styleUrl))
        {
            // Walk up the parent chain until we find the style
            final String styleUrlMod = StringUtilities.startsWith(styleUrl, '#') ? styleUrl.substring(1) : styleUrl;
            KMLFeature aFeature = feature;
            while (style == null && aFeature != null)
            {
                Optional<StyleSelector> styleOptional = aFeature.getStyleSelector().stream()
                        .filter(s -> styleUrlMod.equals(s.getId())).findAny();
                if (styleOptional.isPresent())
                {
                    style = getSelectorStyle(aFeature, styleState, styleOptional.get());
                }
                aFeature = aFeature.getParent();
            }
        }
        else
        {
            URL url = KMLLinkHelper.createFullURL(styleUrl, feature.getDataSource());
            if (url != null)
            {
                String fullStyleUrl = url.toExternalForm();
                style = getData(feature, fullStyleUrl, styleState);
            }
        }
        return style;
    }

    /**
     * Gets the URL style.
     *
     * @param feature The feature
     * @param styleState The style state
     * @param styleSelector The style selector
     * @return The style, or null
     */
    private Style getSelectorStyle(KMLFeature feature, StyleState styleState, StyleSelector styleSelector)
    {
        Style style = null;
        if (styleSelector instanceof Style)
        {
            style = (Style)styleSelector;
        }
        else if (styleSelector instanceof StyleMap)
        {
            StyleMap styleMap = (StyleMap)styleSelector;
            KMLStyleMap kmlStyleMap = getStyleMap(feature, styleMap, null);
            style = kmlStyleMap.get(styleState);
        }
        return style;
    }

    /**
     * Gets the Style from the cache for the given dataTypeKey and style URL
     * string.
     *
     * @param feature The feature
     * @param styleUrl The style URL string
     * @param styleState The style state
     * @return The Style in the cache, or null
     */
    private Style getData(KMLFeature feature, String styleUrl, StyleState styleState)
    {
        Style style = null;
        final KMLDataSource dataSource = feature.getDataSource();
        if (dataSource != null)
        {
            synchronized (this)
            {
                {
                    Map<String, Style> urlToStyleMap = myStyleMap.get(dataSource.getDataGroupKey());
                    if (urlToStyleMap != null)
                    {
                        style = urlToStyleMap.get(styleUrl);
                    }
                }

                if (style == null)
                {
                    Map<String, StyleMap> urlToStyleMap = myStyleMapMap.get(dataSource.getDataGroupKey());
                    if (urlToStyleMap != null)
                    {
                        StyleMap styleMap = urlToStyleMap.get(styleUrl);
                        if (styleMap != null)
                        {
                            KMLStyleMap kmlStyleMap = getStyleMap(feature, styleMap, styleUrl);
                            style = kmlStyleMap.get(styleState);
                        }
                    }
                }
            }
        }
        return style;
    }

    /**
     * Gets a KMLStyleMap from a StyleMap.
     *
     * @param feature The feature
     * @param styleMap the StyleMap
     * @param styleMapUrl The style URL referencing the style map
     * @return the KMLStyleMap
     */
    private KMLStyleMap getStyleMap(KMLFeature feature, StyleMap styleMap, String styleMapUrl)
    {
        KMLStyleMap kmlStyleMap = new KMLStyleMap();
        for (Pair pair : styleMap.getPair())
        {
            StyleState styleState = pair.getKey();

            Style style = null;
            if (pair.getStyleSelector() instanceof Style)
            {
                style = (Style)pair.getStyleSelector();
            }
            if (style == null)
            {
                String styleUrl = styleMapUrl != null
                        ? styleMapUrl.substring(0, styleMapUrl.lastIndexOf('#')) + pair.getStyleUrl() : pair.getStyleUrl();
                style = getUrlStyle(feature, styleState, styleUrl);
            }

            kmlStyleMap.put(styleState, style);
        }
        return kmlStyleMap;
    }

    /**
     * Gets the styleUrl prefix for the given data source.
     *
     * @param dataSource The data source
     * @return The styleUrl prefix
     */
    private static String getStyleUrlPrefix(KMLDataSource dataSource)
    {
        String urlPrefix = "";
        URL url = KMLLinkHelper.toURL(dataSource);
        if (url != null)
        {
            String urlString = url.toExternalForm();
            urlPrefix = new StringBuilder(urlString.length() + 1).append(urlString).append('#').toString();
        }
        return urlPrefix;
    }

    /**
     * Determines if the style URL is an internal URL.
     *
     * @param styleUrl the style URL
     * @return true for internal, false for external
     */
    private static boolean isInternal(String styleUrl)
    {
        return StringUtilities.startsWith(styleUrl, '#') || styleUrl.indexOf('#') == -1;
    }

    /** Style feature accumulator. */
    private static class StyleFeatureAccumulator extends KMLFeatureAccumulator<Style>
    {
        @Override
        protected boolean process(KMLFeature feature, Collection<? super Style> values)
        {
            values.addAll(CollectionUtilities.filterDowncast(feature.getStyleSelector(), Style.class));
            return true;
        }
    }

    /** StyleMap feature accumulator. */
    private static class StyleMapFeatureAccumulator extends KMLFeatureAccumulator<StyleMap>
    {
        @Override
        protected boolean process(KMLFeature feature, Collection<? super StyleMap> values)
        {
            values.addAll(CollectionUtilities.filterDowncast(feature.getStyleSelector(), StyleMap.class));
            return true;
        }
    }
}
