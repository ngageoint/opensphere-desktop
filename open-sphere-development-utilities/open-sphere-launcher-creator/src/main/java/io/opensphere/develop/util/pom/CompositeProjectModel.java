package io.opensphere.develop.util.pom;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.DependencyManagement;
import org.apache.maven.model.Profile;

import io.opensphere.develop.util.launch.DependencyComparator;

/**
 * A model in which a project is defined. The model allows for multiple projects
 * to inherit from one another, including inherited dependencies, properties,
 * dependency management, etc.
 */
public class CompositeProjectModel
{
    /**
     * The {@link Logger} instance used to capture output.
     */
    private static final Logger LOG = Logger.getLogger(CompositeProjectModel.class);

    /**
     * The registry containing projects parsed in this composite model.
     */
    private ProjectRegistry myModuleRegistry;

    /**
     * A dictionary of managed dependencies, using the partial coordinate
     * (groupId:artifactId:type) as the key, and the managed dependency as the
     * version.
     */
    private Map<String, Dependency> myManagedDependencies;

    /**
     * The set of profiles to use when parsing the POM.
     */
    private Set<String> myProfiles;

    /** The root path in which all base projects are defined. */
    private Path myRootPath;

    /**
     * Creates a new composite project model, configured to find root projects
     * in the supplied path. Root projects may be hierarchical.
     *
     * @param rootPath the path in which to search for root projects.
     */
    public CompositeProjectModel(Path rootPath)
    {
        myRootPath = rootPath;
        myModuleRegistry = new ProjectRegistry();
        myManagedDependencies = new HashMap<>();
    }

    /**
     * Gets the value of the {@link #myRootPath} field.
     *
     * @return the value stored in the {@link #myRootPath} field.
     */
    public Path getRootPath()
    {
        return myRootPath;
    }

    /**
     * Sets the value of the {@link #myProfiles} field.
     *
     * @param profiles the value to store in the {@link #myProfiles} field.
     */
    public void setProfiles(Set<String> profiles)
    {
        myProfiles = profiles;
    }

    /**
     * Recursively stores the supplied project and all of it's modules in the
     * composite data store.
     *
     * @param projectModel the project model to store.
     */
    public void storeProject(Project projectModel)
    {
        updateManagedDependencies(projectModel);

        myModuleRegistry.add(projectModel);
        Collection<Project> modules = projectModel.getModules();

        for (Project module : modules)
        {
            storeProject(module);
        }
    }

    /**
     * Updates the dictionary of managed dependencies.
     *
     * @param projectModel the project model from which to read managed
     *            dependencies.
     */
    public void updateManagedDependencies(Project projectModel)
    {
        DependencyManagement dependencyManagement = projectModel.getModel().getDependencyManagement();
        if (dependencyManagement != null)
        {
            List<Dependency> dependencies = dependencyManagement.getDependencies();
            for (Dependency dependency : dependencies)
            {
                myManagedDependencies.put(dependency.getManagementKey(), dependency);
            }
        }
    }

    /**
     * Gets the managed dependency entry associated with the supplied management
     * key, if present.
     *
     * @param managementKey the key for which to get the managed dependency.
     * @return the managed dependency associated with the supplied key, or null
     *         if none is known.
     */
    public Dependency resolveManagedDependency(String managementKey)
    {
        return myManagedDependencies.get(managementKey);
    }

    /**
     * Gets the list of project artifactIds that are JAR projects descended from
     * the supplied project model.
     *
     * @param projectModel the project model for which to get child module
     *            projects.
     * @return a collection of the artifact IDs of JAR projects within the
     *         supplied module, may be an empty list.
     */
    public List<String> getProjects(Project projectModel)
    {
        List<String> artifactIds = new ArrayList<>();
        Collection<Project> modules = projectModel.getModules();

        for (Project module : modules)
        {
            String packaging = module.getModel().getPackaging();
            if (StringUtils.equals("jar", packaging))
            {
                artifactIds.add(module.getModel().getArtifactId());
            }
            else if (StringUtils.equals("pom", packaging))
            {
                artifactIds.addAll(getProjects(module));
            }
        }

        return artifactIds;
    }

    /**
     * Gets the value of the {@link #myModuleRegistry} field.
     *
     * @return the value stored in the {@link #myModuleRegistry} field.
     */
    public ProjectRegistry getModuleRegistry()
    {
        return myModuleRegistry;
    }

    /**
     * Gets the set of external dependencies from the supplied project model,
     * excluding those defined in the supplied project model.
     *
     * @param projectModel the project for which the external dependencies are
     *            retrieved.
     * @return the set of external dependencies, resolved from the project.
     *         Managed versions are also resolved from the composite project's
     *         dependency management.
     */
    public Set<Dependency> getExternalDependencies(Project projectModel)
    {
        Set<Dependency> dependencies = new TreeSet<>(new DependencyComparator());
        Collection<Project> modules = projectModel.getModules();

        for (Project module : modules)
        {
            String packaging = module.getModel().getPackaging();
            if (StringUtils.equals("jar", packaging))
            {
                extractDependencies(dependencies, module);
            }
            else if (StringUtils.equals("pom", packaging))
            {
                // pom projects may contain their own dependencies, as well as
                // submodules. If a pom project contains it's own dependency,
                // that dependency is inherited by all of it's children, so just
                // resolve it here.
                extractDependencies(dependencies, module);
                // get the dependencies from the child module:
                dependencies.addAll(getExternalDependencies(module));
            }
        }

        return dependencies;
    }

    /**
     * Extracts the dependencies from the supplied module, using the declared
     * set, plus any declared within active profiles (a profile is 'active' if
     * contained within {@link #myProfiles}). During this process, managed
     * dependencies are resolved.
     *
     * @param dependencies the set into which extracted dependencies are placed.
     * @param module the module from which dependencies are extracted.
     */
    protected void extractDependencies(Set<Dependency> dependencies, Project module)
    {
        LOG.info(String.format("Reading dependencies for module '%s'", module.getCoordinateWithoutPackaging()));

        List<Dependency> moduleDependencies = new ArrayList<>();

        if (module.getModel().getDependencies() != null)
        {
            moduleDependencies.addAll(module.getModel().getDependencies());
        }

        if (module.getModel().getProfiles() != null)
        {
            for (Profile profile : module.getModel().getProfiles())
            {
                if (myProfiles.contains(profile.getId()))
                {
                    if (profile.getDependencies() != null)
                    {
                        moduleDependencies.addAll(profile.getDependencies());
                    }
                }
            }
        }

        for (Dependency dependency : moduleDependencies)
        {
            LOG.info(String.format("Examining dependency '%s' for module '%s'",
                    module.resolveProperties(dependency.getManagementKey()), module.getCoordinateWithoutPackaging()));
            if (StringUtils.equals("jar", dependency.getType()) && !StringUtils.equals("test", dependency.getScope()))
            {
                if (dependency.getVersion() == null)
                {
                    Dependency managedDependency = resolveManagedDependency(
                            module.resolveProperties(dependency.getManagementKey()));

                    if (managedDependency != null)
                    {
                        managedDependency.setVersion(module.resolveProperties(managedDependency.getVersion()));
                        dependencies.add(managedDependency);
                    }
                    else
                    {
                        LOG.warn("Dependency '" + dependency.getManagementKey() + "' was managed, but could not be resolved.");
                    }
                }
                else
                {
                    dependencies.add(dependency);
                }
            }
        }
    }
}
