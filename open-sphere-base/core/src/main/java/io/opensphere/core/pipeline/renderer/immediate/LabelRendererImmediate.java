package io.opensphere.core.pipeline.renderer.immediate;

import java.awt.Color;
import java.awt.Font;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;

import org.apache.log4j.Logger;

import com.jogamp.opengl.util.awt.TextRenderer;
import com.jogamp.opengl.util.texture.TextureCoords;

import io.opensphere.core.geometry.AbstractGeometry;
import io.opensphere.core.geometry.AbstractGeometry.RenderMode;
import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.geometry.LabelGeometry;
import io.opensphere.core.geometry.renderproperties.DefaultFragmentShaderProperties;
import io.opensphere.core.geometry.renderproperties.FragmentShaderProperties;
import io.opensphere.core.geometry.renderproperties.FragmentShaderProperties.ShaderPropertiesSet;
import io.opensphere.core.geometry.renderproperties.LabelRenderProperties;
import io.opensphere.core.math.Vector3d;
import io.opensphere.core.pipeline.cache.CacheProvider;
import io.opensphere.core.pipeline.processor.LabelProcessor.ModelCoordinates;
import io.opensphere.core.pipeline.renderer.AbstractRenderer;
import io.opensphere.core.pipeline.util.GLUtilities;
import io.opensphere.core.pipeline.util.PickManager;
import io.opensphere.core.pipeline.util.RenderContext;
import io.opensphere.core.pipeline.util.ShaderRendererUtilities.TileShader;
import io.opensphere.core.projection.Projection;
import io.opensphere.core.util.TimeBudget;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.awt.AWTUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.io.StreamReader;
import io.opensphere.core.util.lang.Pair;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.core.viewer.impl.MapContext;

/**
 * Immediate mode GL label renderer.
 */
public class LabelRendererImmediate extends AbstractRenderer<LabelGeometry> implements GeometryRendererImmediate<LabelGeometry>
{
    /** Bits used for {@link GL2#glPushAttrib(int)}. */
    private static final int ATTRIB_BITS = GL2.GL_ENABLE_BIT | GL2.GL_CURRENT_BIT;

    /** Bits used for {@link GL2#glPushClientAttrib(int)}. */
    private static final int CLIENT_ATTRIB_BITS = -1;

    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(LabelRendererImmediate.class);

    /** The shader code which produces an outline around the text. */
    private static final String ourShadowCode;

    /** A map of text renderers available for use. */
    private static final Map<String, TextRenderer> ourTextRendererMap = new HashMap<>();

    static
    {
        String resource = "/GLSL/TextOutline.glsl";
        String fragment = null;
        InputStream strm = LabelRendererImmediate.class.getResourceAsStream(resource);
        try
        {
            fragment = new StreamReader(strm).readStreamIntoString(StringUtilities.DEFAULT_CHARSET);
        }
        catch (IOException e)
        {
            LOGGER.error("Could not read shader code from [" + resource + "]: " + e, e);
        }
        finally
        {
            ourShadowCode = fragment;
            Utilities.close(strm);
        }
    }

    /**
     * Construct the renderer.
     *
     * @param cache the cache holding the model coordinates for the geometries
     */
    protected LabelRendererImmediate(CacheProvider cache)
    {
        super(cache);
    }

    @Override
    public void cleanupShaders(RenderContext rc, Collection<? extends LabelGeometry> input)
    {
        if (rc.getRenderMode() == RenderMode.DRAW && rc.is15Available() && !input.isEmpty())
        {
            LabelGeometry sample = input.iterator().next();
            if (sample.isOutlined())
            {
                rc.getShaderRendererUtilities().cleanupShaders(rc.getGL());
            }
        }
    }

    @Override
    public void doRender(RenderContext rc, Collection<? extends LabelGeometry> input, Collection<? super LabelGeometry> rejected,
            PickManager pickManager, MapContext<?> mapContext, ModelDataRetriever<LabelGeometry> dataRetriever)
    {
        LabelRenderData renderData = (LabelRenderData)getRenderData();
        if (renderData == null)
        {
            return;
        }

        if (rc.getRenderMode() == AbstractGeometry.RenderMode.DRAW)
        {
            rc.getGL().glEnable(GL.GL_BLEND);
        }
        else
        {
            rc.getGL().glDisable(GL.GL_BLEND);
        }
        rc.getGL().glDisable(GL.GL_DEPTH_TEST);

        List<Pair<LabelGeometry, ModelCoordinates>> geometryData = New.list(input.size());
        for (LabelGeometry label : input)
        {
            ModelCoordinates modelCoords = getModelCoordinates(rc, dataRetriever, renderData, label);
            geometryData.add(new Pair<>(label, modelCoords));
        }

        List<Map<String, List<LabelGeometry>>> sortedGeoms = sortLabels(geometryData);
        TextRenderer rend = null;
        try
        {
            for (Map<String, List<LabelGeometry>> zMap : sortedGeoms)
            {
                for (Entry<String, List<LabelGeometry>> entry : zMap.entrySet())
                {
                    rend = begin3DRendering(rend, entry.getKey());
                    int lastColor = Integer.MIN_VALUE;

                    for (LabelGeometry label : entry.getValue())
                    {
                        ModelCoordinates modelCoords = getModelCoordinates(rc, dataRetriever, renderData, label);

                        if (modelCoords == null)
                        {
                            rejected.add(label);
                        }
                        else if (modelCoords.getFontSize() >= 8)
                        {
                            Vector3d vector = modelCoords.getScreenModelCoords();
                            LabelRenderProperties lRen = label.getRenderProperties();

                            if (rc.getRenderMode() == RenderMode.DRAW && lRen.getShadowColorARGB() != 0)
                            {
                                if (lRen.getShadowColorARGB() != lastColor)
                                {
                                    lastColor = lRen.getShadowColorARGB();
                                    GLUtilities.setRendererColor(rend, lastColor);
                                }
                                drawMultiLine(rend, label.getText(),
                                        vector.getX() + lRen.getShadowOffsetX(),
                                        vector.getY() + lRen.getShadowOffsetY(),
                                        -modelCoords.getBaselineDelta());
                            }

                            lastColor = GLUtilities.setRendererColor(rend,
                                    pickManager, rc.getRenderMode(), label, lastColor);
                            drawMultiLine(rend, label.getText(), vector.getX(),
                                    vector.getY(), -modelCoords.getBaselineDelta());
                        }
                    }
                }
            }
        }
        finally
        {
            if (rend != null)
                rend.end3DRendering();
        }
    }

    private void drawMultiLine(TextRenderer rend, String txt, double x0, double y0, double dy0)
    {
        float x = (float)x0;
        float y = (float)y0;
        float dy = (float)dy0;
        String[] lines = txt.split("\\n");
        y -= (lines.length - 1) * dy;
        for (String ln :  lines)
        {
            rend.draw3D(ln, x, y, 0f, 1f);
            y += dy;
        }
    }

    @Override
    public int getAttribBits()
    {
        return ATTRIB_BITS;
    }

    @Override
    public int getClientAttribBits()
    {
        return CLIENT_ATTRIB_BITS;
    }

    @Override
    public Class<?> getType()
    {
        return LabelGeometry.class;
    }

    @Override
    public void initializeShaders(RenderContext rc, Collection<? extends LabelGeometry> input)
    {
        if (rc.getRenderMode() == RenderMode.DRAW && rc.is15Available() && !input.isEmpty())
        {
            LabelGeometry sample = input.iterator().next();
            if (sample.isOutlined())
            {
                Color outlineColor = sample.getOutlineColor();
                ShaderPropertiesSet propertiesSet = new ShaderPropertiesSet();
                Collection<Pair<String, float[]>> uniforms = New.collection(1);
                propertiesSet.setShaderCode(ourShadowCode);
                uniforms.add(new Pair<String, float[]>("uOutlineColor", outlineColor.getComponents(null)));
                propertiesSet.setFloatUniforms(uniforms);
                FragmentShaderProperties fragProps = new DefaultFragmentShaderProperties();
                fragProps.setupShader(propertiesSet);
                rc.getShaderRendererUtilities().enableShaderByName(rc.getGL(), TileShader.DRAW, fragProps,
                        new TextureCoords(0f, 0f, 1f, 1f));
            }
        }
    }

    @Override
    public void preRender(Collection<? extends LabelGeometry> input, Collection<? extends LabelGeometry> drawable,
            Collection<? extends LabelGeometry> pickable, PickManager pickManager,
            ModelDataRetriever<LabelGeometry> dataRetriever, Projection projection)
    {
        super.preRender(input, drawable, pickable, pickManager, dataRetriever, projection);
        Map<LabelGeometry, ModelCoordinates> renderMap = New.weakMap(input.size());

        for (LabelGeometry geom : input)
        {
            ModelCoordinates modelData = (ModelCoordinates)dataRetriever.getModelData(geom, projection, null, TimeBudget.ZERO);
            if (modelData != null)
            {
                renderMap.put(geom, modelData);
            }
        }

        setRenderData(new LabelRenderData(renderMap, projection));
    }

    @Override
    protected Logger getLogger()
    {
        return LOGGER;
    }

    /**
     * Begin text rendering on the renderer for the given font. If the current
     * renderer is the correct one for the font it will be assumed that text
     * rendering has already begun and no action will be taken. Otherwise,
     * rendering for the current one will be ended and rendering for the correct
     * one will be started.
     *
     * @param currentRenderer The renderer which is current prior to this
     *            operation.
     * @param font the font string.
     * @return the text renderer which is current after this operation.
     */
    private TextRenderer begin3DRendering(TextRenderer currentRenderer, String font)
    {
        TextRenderer rend = ourTextRendererMap.get(font);
        if (rend == null)
        {
            Font drawFont = Font.decode(font);
            rend = new TextRenderer(drawFont);
            // Set use vertex arrays to false to prevent label colors from being
            // swapped when rendering text in a display list. VORTEX-3113.
            rend.setUseVertexArrays(false);
            rend.setSmoothing(false);
            ourTextRendererMap.put(font, rend);
        }

        if (!Utilities.sameInstance(rend, currentRenderer))
        {
            if (currentRenderer != null)
            {
                currentRenderer.end3DRendering();
            }
            rend.begin3DRendering();
        }
        return rend;
    }

    /**
     * Gets the model coordinates from the renderData, updating the value from
     * the data retriever if it's missing.
     *
     * @param rc the render context
     * @param dataRetriever the data retriever
     * @param renderData the render data
     * @param label the label geometry
     * @return the model coordinates
     */
    private ModelCoordinates getModelCoordinates(RenderContext rc, ModelDataRetriever<LabelGeometry> dataRetriever,
            LabelRenderData renderData, LabelGeometry label)
    {
        ModelCoordinates modelCoords = renderData.getData().get(label);
        if (modelCoords == null)
        {
            modelCoords = (ModelCoordinates)dataRetriever.getModelData(label, renderData.getProjection(), null,
                    rc.getTimeBudget());
            if (modelCoords != null)
            {
                renderData.getData().put(label, modelCoords);
            }
        }
        return modelCoords;
    }

    /**
     * Sort labels by z-order and font string so that the TextRenderer(s) can be
     * used as efficiently as possible.
     *
     * @param geometryData geometry data being rendered.
     * @return the sorted geometries.
     */
    private List<Map<String, List<LabelGeometry>>> sortLabels(Collection<Pair<LabelGeometry, ModelCoordinates>> geometryData)
    {
        List<Map<String, List<LabelGeometry>>> sortedGeoms = new ArrayList<>();

        int lastZorder = -1;
        // The map which is currently being populated.
        HashMap<String, List<LabelGeometry>> zMap = null;

        for (Pair<LabelGeometry, ModelCoordinates> data : geometryData)
        {
            LabelGeometry label = data.getFirstObject();
            ModelCoordinates modelCoords = data.getSecondObject();
            if (zMap == null || label.getRenderProperties().getZOrder() != lastZorder)
            {
                zMap = new HashMap<>();
                sortedGeoms.add(zMap);
                lastZorder = label.getRenderProperties().getZOrder();
            }

            // The list of labels which will use the same renderer as the
            // current label.
            Font propertyFont = Font.decode(label.getRenderProperties().getFont());
            Font scaledFont = propertyFont.deriveFont((float)modelCoords.getFontSize());
            String font = AWTUtilities.encode(scaledFont);
            List<LabelGeometry> rendList = zMap.get(font);
            if (rendList == null)
            {
                rendList = new ArrayList<>();
                zMap.put(font, rendList);
            }
            rendList.add(label);
        }

        return sortedGeoms;
    }

    /** A factory for creating this renderer. */
    public static class Factory extends GeometryRendererImmediate.Factory<LabelGeometry>
    {
        @Override
        public GeometryRendererImmediate<LabelGeometry> createRenderer()
        {
            return new LabelRendererImmediate(getCache());
        }

        @Override
        public Class<? extends Geometry> getType()
        {
            return LabelGeometry.class;
        }

        @Override
        public boolean isViable(RenderContext rc, Collection<String> warnings)
        {
            return true;
        }
    }

    /** Data used for rendering. */
    private static class LabelRenderData extends RenderData
    {
        /** The map of geometries to pairs of data. */
        private final Map<LabelGeometry, ModelCoordinates> myData;

        /**
         * Constructor.
         *
         * @param data The map of geometries to pairs of render data.
         * @param projection The projection used to generate this data.
         */
        public LabelRenderData(Map<LabelGeometry, ModelCoordinates> data, Projection projection)
        {
            super(projection);
            myData = data;
        }

        /**
         * Access to the data map. This is not thread-safe and should only be
         * called from the GL thread.
         *
         * @return The data map.
         */
        public Map<LabelGeometry, ModelCoordinates> getData()
        {
            return myData;
        }
    }
}
