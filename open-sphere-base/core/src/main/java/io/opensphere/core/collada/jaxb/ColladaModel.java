package io.opensphere.core.collada.jaxb;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import io.opensphere.core.util.collections.New;

/**
 * Top level COLLADA model.
 */
@XmlRootElement(name = "COLLADA")
@XmlAccessorType(XmlAccessType.NONE)
public class ColladaModel
{
    /** The asset. */
    @XmlElement(name = "asset")
    private Asset myAsset;

    /** The library images. */
    @XmlElement(name = "library_images")
    private Images myLibraryImages;

    /** The library effects. */
    @XmlElement(name = "library_effects")
    private Effects myLibraryEffects;

    /** The library materials. */
    @XmlElement(name = "library_materials")
    private Materials myLibraryMaterials;

    /** The library geometries. */
    @XmlElementWrapper(name = "library_geometries")
    @XmlElement(name = "geometry")
    private final List<Geometry> myLibraryGeometries = New.list();

    /** The library visual scenes. */
    @XmlElementWrapper(name = "library_visual_scenes")
    @XmlElement(name = "visual_scene")
    private final List<VisualScene> myVisualScenes = New.list();

    /** The library nodes. */
    @XmlElementWrapper(name = "library_nodes")
    @XmlElement(name = "node")
    private final List<Node> myLibraryNodes = New.list();

    /** The main scene. */
    @XmlElement(name = "scene")
    private Scene myScene;

    /**
     * Gets the asset.
     *
     * @return the asset
     */
    public Asset getAsset()
    {
        return myAsset;
    }

    /**
     * Gets the library images.
     *
     * @return the library images
     */
    public Images getLibraryImages()
    {
        return myLibraryImages;
    }

    /**
     * Gets the library effects.
     *
     * @return the library effects
     */
    public Effects getLibraryEffects()
    {
        return myLibraryEffects;
    }

    /**
     * Gets the library materials.
     *
     * @return the library materials
     */
    public Materials getLibraryMaterials()
    {
        return myLibraryMaterials;
    }

    /**
     * Get the library geometries.
     *
     * @return The library geometries.
     */
    public List<Geometry> getLibraryGeometries()
    {
        return myLibraryGeometries;
    }

    /**
     * Get the library nodes.
     *
     * @return The library nodes.
     */
    public List<Node> getLibraryNodes()
    {
        return myLibraryNodes;
    }

    /**
     * Get the scene.
     *
     * @return The scene.
     */
    public Scene getScene()
    {
        return myScene;
    }

    /**
     * Get the visual scenes.
     *
     * @return The visual scenes.
     */
    public List<VisualScene> getVisualScenes()
    {
        return myVisualScenes;
    }

    /**
     * Set the scene.
     *
     * @param scene The scene.
     */
    public void setScene(Scene scene)
    {
        myScene = scene;
    }
}
