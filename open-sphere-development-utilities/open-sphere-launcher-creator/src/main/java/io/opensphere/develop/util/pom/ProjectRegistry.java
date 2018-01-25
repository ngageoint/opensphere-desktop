package io.opensphere.develop.util.pom;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

/**
 * A registry of projects and modules defined one or more related Project Object
 * Models (POMs).
 */
public class ProjectRegistry
{
    /**
     * The registry in which all projects are stored. The project is associated
     * with its coordinate as the key.
     */
    private final Map<Coordinate, Project> myRegistry;

    /**
     * Creates a new project registry.
     */
    public ProjectRegistry()
    {
        myRegistry = new HashMap<>();
    }

    /**
     * Adds the supplied project model to the registry.
     *
     * @param model the model to add to the registry.
     */
    public void add(Project model)
    {
        String groupId = model.resolveProperties(model.getGroupId());
        String artifactId = model.resolveProperties(model.getArtifactId());
        String version = model.resolveProperties(model.getVersion());
        String packaging = model.resolveProperties(model.getModel().getPackaging());
        Coordinate coordinate = new Coordinate(groupId, artifactId, version, packaging, null);

        myRegistry.put(coordinate, model);
    }

    /**
     * Gets the project associated with the supplied coordinate, or null if none
     * is known.
     *
     * @param coordinate the coordinate for which to get the project.
     * @return the project associated with the supplied value, or null if none
     *         is known.
     */
    public Project get(Coordinate coordinate)
    {
        return myRegistry.get(coordinate);
    }

    /**
     * Gets the project associated with the supplied coordinate string, or null
     * if none is known.
     *
     * @param coordinate the coordinate string for which to get the project.
     * @return the project associated with the supplied value, or null if none
     *         is known.
     */
    public Project get(String coordinate)
    {
        return myRegistry.get(new Coordinate(coordinate));
    }

    /**
     * Gets the project associated with the supplied coordinate, or null if none
     * is known.
     *
     * @param groupId the group ID portion of the target artifact's coordinate.
     * @param artifactId the artifact ID portion of the target artifact's
     *            coordinate.
     * @param version the version portion of the target artifact's coordinate.
     * @return the project associated with the supplied values, or null if none
     *         is known.
     */
    public Project get(String groupId, String artifactId, String version)
    {
        return myRegistry.get(new Coordinate(groupId, artifactId, version));
    }

    /**
     * Gets the project associated with the supplied coordinate, or null if none
     * is known.
     *
     * @param groupId the group ID portion of the target artifact's coordinate.
     * @param artifactId the artifact ID portion of the target artifact's
     *            coordinate.
     * @param version the version portion of the target artifact's coordinate.
     * @param packaging the packaging portion of the target artifact's
     *            coordinate.
     * @return the project associated with the supplied values, or null if none
     *         is known.
     */
    public Project get(String groupId, String artifactId, String version, String packaging)
    {
        return myRegistry.get(new Coordinate(groupId, artifactId, version, packaging));
    }

    /**
     * Gets the project associated with the supplied coordinate, or null if none
     * is known.
     *
     * @param groupId the group ID portion of the target artifact's coordinate.
     * @param artifactId the artifact ID portion of the target artifact's
     *            coordinate.
     * @param version the version portion of the target artifact's coordinate.
     * @param packaging the packaging portion of the target artifact's
     *            coordinate.
     * @param classifier the classifier portion of the target artifact's
     *            coordinate.
     * @return the project associated with the supplied values, or null if none
     *         is known.
     */
    public Project get(String groupId, String artifactId, String version, String packaging, String classifier)
    {
        return myRegistry.get(new Coordinate(groupId, artifactId, version, packaging, classifier));
    }

    /**
     * Tests to determine if the registry contains an entry associated with the
     * supplied coordinate.
     *
     * @param coordinate the coordinate string for which to search in the
     *            registry.
     * @return true if the registry contains a project associated with the
     *         supplied coordinate, false otherwise.
     */
    public boolean contains(Coordinate coordinate)
    {
        return myRegistry.containsKey(coordinate);
    }

    /**
     * Tests to determine if the registry contains an entry associated with the
     * supplied coordinate.
     *
     * @param coordinate the coordinate string for which to search in the
     *            registry.
     * @return true if the registry contains a project associated with the
     *         supplied coordinate, false otherwise.
     */
    public boolean contains(String coordinate)
    {
        return StringUtils.isNotBlank(coordinate) && myRegistry.containsKey(new Coordinate(coordinate));
    }

    /**
     * Tests to determine if the registry contains an entry associated with the
     * supplied coordinate.
     *
     * @param groupId the group ID portion of the target artifact's coordinate.
     * @param artifactId the artifact ID portion of the target artifact's
     *            coordinate.
     * @param version the version portion of the target artifact's coordinate.
     * @return true if the registry contains a project associated with the
     *         supplied coordinate, false otherwise.
     */
    public boolean contains(String groupId, String artifactId, String version)
    {
        return myRegistry.containsKey(new Coordinate(groupId, artifactId, version));
    }

    /**
     * Tests to determine if the registry contains an entry associated with the
     * supplied coordinate.
     *
     * @param groupId the group ID portion of the target artifact's coordinate.
     * @param artifactId the artifact ID portion of the target artifact's
     *            coordinate.
     * @param version the version portion of the target artifact's coordinate.
     * @param packaging the packaging portion of the target artifact's
     *            coordinate.
     * @return true if the registry contains a project associated with the
     *         supplied coordinate, false otherwise.
     */
    public boolean contains(String groupId, String artifactId, String version, String packaging)
    {
        return myRegistry.containsKey(new Coordinate(groupId, artifactId, version, packaging));
    }

    /**
     * Tests to determine if the registry contains an entry associated with the
     * supplied coordinate.
     *
     * @param groupId the group ID portion of the target artifact's coordinate.
     * @param artifactId the artifact ID portion of the target artifact's
     *            coordinate.
     * @param version the version portion of the target artifact's coordinate.
     * @param packaging the packaging portion of the target artifact's
     *            coordinate.
     * @param classifier the classifier portion of the target artifact's
     *            coordinate.
     * @return true if the registry contains a project associated with the
     *         supplied coordinate, false otherwise.
     */
    public boolean contains(String groupId, String artifactId, String version, String packaging, String classifier)
    {
        return myRegistry.containsKey(new Coordinate(groupId, artifactId, version, packaging, classifier));
    }
}
