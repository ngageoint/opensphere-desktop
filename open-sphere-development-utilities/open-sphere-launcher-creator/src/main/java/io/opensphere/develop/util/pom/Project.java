package io.opensphere.develop.util.pom;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.text.StrSubstitutor;
import org.apache.log4j.Logger;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import io.opensphere.develop.util.launch.PropertyMapUtils;

/**
 * A hierarchical project model composed of the project and all of it's child
 * modules.
 */
public class Project
{
    /**
     * The {@link Logger} instance used to capture output.
     */
    private static final Logger LOG = Logger.getLogger(Project.class);

    /** The root project's model. */
    private Model myModel;

    /**
     * The parent project of this project. May be null if the project is a root
     * project.
     */
    private Project myParent;

    /** The child modules of this project. */
    private Collection<Project> myModules;

    /** The root directory for the product. */
    private String myRootDir;

    /** The directory prefix. */
    private String myDirPrefix;

    /** The launcher prefix. */
    private String myLauncherPrefix;

    /** The launcher prefix. */
    private String myLauncherSuffix;

    /** The application title. */
    private String myTitle;

    /** Additional VM arguments. */
    private String myAdditionalVmArgs;

    /** Additional classpath items. */
    private String[] myAdditionalClasspathItems;

    /** The substitution manager used to resolve properties. */
    private StrSubstitutor mySubstitutor;

    /** The registry from which other projects are resolved. */
    private ProjectRegistry myProjectRegistry;

    /**
     * Creates a new project model using the POM declared on the supplied path
     * (note that the path should point to the directory in which the pom.xml
     * file is stored, not the pom.xml itself).
     *
     * @param path the path of the project's POM.
     * @param projectRegistry The registry from which other projects are
     *            resolved.
     * @param excludedArtifactIds the set of artifact IDs that should not be
     *            resolved.
     */
    public Project(Path path, ProjectRegistry projectRegistry, Set<String> excludedArtifactIds)
    {
        this(path, null, projectRegistry, excludedArtifactIds);
    }

    /**
     * Creates a new project model using the POM declared on the supplied path
     * (note that the path should point to the directory in which the pom.xml
     * file is stored, not the pom.xml itself).
     *
     * @param path the path of the project's POM.
     * @param parent the parent project model from which this instance will
     *            inherit.
     * @param projectRegistry The registry from which other projects are
     *            resolved.
     * @param excludedArtifactIds the set of artifact IDs that should not be
     *            resolved.
     */
    public Project(Path path, Project parent, ProjectRegistry projectRegistry, Set<String> excludedArtifactIds)
    {
        LOG.info("Creating project model for path '" + path.toString() + "'");
        myRootDir = path.toString();
        myParent = parent;
        myProjectRegistry = projectRegistry;
        try
        {
            Path pomFilePath = Paths.get(path.toFile().getAbsolutePath(), "pom.xml");
            Reader reader = new FileReader(pomFilePath.toFile());
            MavenXpp3Reader xpp3Reader = new MavenXpp3Reader();
            myModel = xpp3Reader.read(reader);

            if (myParent == null && myProjectRegistry.contains(getParentCoordinate()))
            {
                myParent = myProjectRegistry.get(getParentCoordinate());
            }

            mySubstitutor = new StrSubstitutor(PropertyMapUtils.toMap(getProperties()));

            setTitle(myModel.getName());

            extractChildModules(excludedArtifactIds);
        }
        catch (IOException | XmlPullParserException e)
        {
            LOG.error("unable to read file at location '" + path.toString() + "'");
        }
    }

    /**
     * Extracts the set of child modules from the current project.
     *
     * @param excludedArtifactIds the set of artifact IDs that should not be
     *            resolved.
     */
    private void extractChildModules(Set<String> excludedArtifactIds)
    {
        myModules = new ArrayList<>();
        List<String> modules = myModel.getModules();

        if (excludedArtifactIds != null && excludedArtifactIds.size() > 0)
        {
            modules.removeIf(module -> excludedArtifactIds.contains(module));
        }

        for (String module : modules)
        {
            Path modulePath = Paths.get(myRootDir, module);

            LOG.debug(String.format("Processing child module '%s' of project '%s:%s:%s'", module, getGroupId(), getArtifactId(),
                    getVersion()));
            Project moduleProject = new Project(modulePath, this, myProjectRegistry, excludedArtifactIds);
            LOG.debug(String.format("Processed child module '%s' of project '%s'", moduleProject.getCoordinateWithoutPackaging(),
                    getCoordinateWithoutPackaging()));

            myModules.add(moduleProject);
        }
    }

    /**
     * Gets the set of properties defined in this project, and any inherited
     * from the parent project (if set). Inherited properties are overridden by
     * their children.
     *
     * @return the set of properties applicable to this project.
     */
    public Properties getProperties()
    {
        Properties properties = new Properties();

        Project parent = myParent;
        while (parent != null)
        {
            // need to do this backwards, as the children override the
            // properties of the parent.
            Properties parentProperties = parent.getProperties();
            parentProperties.putAll(properties);
            properties = new Properties();
            properties.putAll(parentProperties);
            parent = parent.getParent();
        }

        Properties localProperties = myModel.getProperties();
        localProperties.put("project.groupId", getRawGroupId());
        localProperties.put("project.artifactId", getRawArtifactId());
        localProperties.put("project.version", getRawVersion());

        properties.putAll(localProperties);

        return properties;
    }

    /**
     * Resolves any properties in the supplied string, using the properties
     * defined in the current project. If no properties are noted within the
     * supplied string, then no change will occur. If properties are defined but
     * not resolvable, the string will also be returned without un-resolvable
     * properties being substituted.
     *
     * @param string the text in which the substitution will occur.
     * @return the resolved string.
     */
    public String resolveProperties(String string)
    {
        return mySubstitutor.replace(string);
    }

    /**
     * Gets the groupId of the Maven project. This method is recursive, designed
     * to respect the inheritance of the project from it's parent. If the
     * project's groupId is null, and the parent has been set, then the parent's
     * groupId will be resolved recursively.
     *
     * @return the groupId of the project.
     */
    public String getGroupId()
    {
        return mySubstitutor.replace(getRawGroupId());
    }

    /**
     * Gets the groupId of the Maven project. This method is recursive, designed
     * to respect the inheritance of the project from it's parent. If the
     * project's groupId is null, and the parent has been set, then the parent's
     * groupId will be resolved recursively.
     *
     * @return the groupId of the project.
     */
    public String getRawGroupId()
    {
        String groupId = myModel.getGroupId();
        if (StringUtils.isBlank(groupId) && myParent != null)
        {
            groupId = myParent.getGroupId();
        }

        if (StringUtils.isBlank(groupId))
        {
            groupId = myModel.getParent().getGroupId();
        }

        return groupId;
    }

    /**
     * Gets the artifactId of the Maven project.
     *
     * @return the artifactId of the project.
     */
    public String getArtifactId()
    {
        return mySubstitutor.replace(getRawArtifactId());
    }

    /**
     * Gets the artifactId of the Maven project.
     *
     * @return the artifactId of the project.
     */
    public String getRawArtifactId()
    {
        return myModel.getArtifactId();
    }

    /**
     * Gets the version of the Maven project. This method is recursive, designed
     * to respect the inheritance of the project from it's parent. If the
     * project's version is null, and the parent has been set, then the parent's
     * version will be resolved recursively.
     *
     * @return the version of the project.
     */
    public String getVersion()
    {
        return mySubstitutor.replace(getRawVersion());
    }

    /**
     * Gets the version of the Maven project. This method is recursive, designed
     * to respect the inheritance of the project from it's parent. If the
     * project's version is null, and the parent has been set, then the parent's
     * version will be resolved recursively.
     *
     * @return the version of the project.
     */
    public String getRawVersion()
    {
        String version = myModel.getVersion();
        if (StringUtils.isBlank(version) && myParent != null)
        {
            version = myParent.getVersion();
        }

        if (StringUtils.isBlank(version))
        {
            version = myModel.getParent().getVersion();
        }
        return version;
    }

    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return getRawGroupId() + ":" + getRawArtifactId() + ":" + myModel.getPackaging() + ":" + getRawVersion();
    }

    /**
     * Gets the value of the {@link #myRootDir} field.
     *
     * @return the value stored in the {@link #myRootDir} field.
     */
    public String getRootDir()
    {
        return myRootDir;
    }

    /**
     * Gets the value of the {@link #myDirPrefix} field.
     *
     * @return the value stored in the {@link #myDirPrefix} field.
     */
    public String getDirPrefix()
    {
        return myDirPrefix;
    }

    /**
     * Gets the value of the {@link #myLauncherPrefix} field.
     *
     * @return the value stored in the {@link #myLauncherPrefix} field.
     */
    public String getLauncherPrefix()
    {
        return myLauncherPrefix;
    }

    /**
     * Gets the value of the {@link #myLauncherSuffix} field.
     *
     * @return the value stored in the {@link #myLauncherSuffix} field.
     */
    public String getLauncherSuffix()
    {
        return myLauncherSuffix;
    }

    /**
     * Gets the value of the {@link #myTitle} field.
     *
     * @return the value stored in the {@link #myTitle} field.
     */
    public String getTitle()
    {
        return mySubstitutor.replace(myTitle);
    }

    /**
     * Gets the value of the {@link #myAdditionalVmArgs} field.
     *
     * @return the value stored in the {@link #myAdditionalVmArgs} field.
     */
    public String getAdditionalVmArgs()
    {
        return myAdditionalVmArgs;
    }

    /**
     * Gets the value of the {@link #myAdditionalClasspathItems} field.
     *
     * @return the value stored in the {@link #myAdditionalClasspathItems}
     *         field.
     */
    public String[] getAdditionalClasspathItems()
    {
        return myAdditionalClasspathItems;
    }

    /**
     * Sets the value of the {@link #myRootDir} field.
     *
     * @param rootDir the value to store in the {@link #myRootDir} field.
     */
    public void setRootDir(String rootDir)
    {
        myRootDir = rootDir;
    }

    /**
     * Sets the value of the {@link #myDirPrefix} field.
     *
     * @param dirPrefix the value to store in the {@link #myDirPrefix} field.
     */
    public void setDirPrefix(String dirPrefix)
    {
        myDirPrefix = dirPrefix;
    }

    /**
     * Sets the value of the {@link #myLauncherPrefix} field.
     *
     * @param launcherPrefix the value to store in the {@link #myLauncherPrefix}
     *            field.
     */
    public void setLauncherPrefix(String launcherPrefix)
    {
        myLauncherPrefix = launcherPrefix;
    }

    /**
     * Sets the value of the {@link #myLauncherSuffix} field.
     *
     * @param launcherSuffix the value to store in the {@link #myLauncherSuffix}
     *            field.
     */
    public void setLauncherSuffix(String launcherSuffix)
    {
        myLauncherSuffix = launcherSuffix;
    }

    /**
     * Sets the value of the {@link #myTitle} field.
     *
     * @param title the value to store in the {@link #myTitle} field.
     */
    public void setTitle(String title)
    {
        myTitle = title;
    }

    /**
     * Sets the value of the {@link #myAdditionalVmArgs} field.
     *
     * @param additionalVmArgs the value to store in the
     *            {@link #myAdditionalVmArgs} field.
     */
    public void setAdditionalVmArgs(String additionalVmArgs)
    {
        myAdditionalVmArgs = additionalVmArgs;
    }

    /**
     * Sets the value of the {@link #myAdditionalClasspathItems} field.
     *
     * @param additionalClasspathItems the value to store in the
     *            {@link #myAdditionalClasspathItems} field.
     */
    public void setAdditionalClasspathItems(String... additionalClasspathItems)
    {
        myAdditionalClasspathItems = additionalClasspathItems;
    }

    /**
     * Sets the value of the {@link #myParent} field.
     *
     * @param parent the value to store in the {@link #myParent} field.
     */
    public void setParent(Project parent)
    {
        myParent = parent;
    }

    /**
     * Gets the value of the {@link #myParent} field.
     *
     * @return the value stored in the {@link #myParent} field.
     */
    public Project getParent()
    {
        return myParent;
    }

    /**
     * Gets the value of the {@link #myModel} field.
     *
     * @return the value stored in the {@link #myModel} field.
     */
    public Model getModel()
    {
        return myModel;
    }

    /**
     * Gets the value of the {@link #myModules} field.
     *
     * @return the value stored in the {@link #myModules} field.
     */
    public Collection<Project> getModules()
    {
        return myModules;
    }

    /**
     * Gets the Maven coordinate for the current project. The coordinate is
     * composed of groupId:artifactId:packaging:version
     *
     * @return the Maven coordinate for the current project
     */
    public String getCoordinate()
    {
        return getGroupId() + ":" + getArtifactId() + ":" + myModel.getPackaging() + ":" + getVersion();
    }

    /**
     * Gets the Maven coordinate for the current project. The coordinate is
     * composed of groupId:artifactId:version
     *
     * @return the Maven coordinate for the current project
     */
    public String getCoordinateWithoutPackaging()
    {
        return getGroupId() + ":" + getArtifactId() + ":" + getVersion();
    }

    /**
     * Gets the coordinate of the parent project, in the form
     * groupId:artifactId:packaging:version. If the project declares no parent,
     * null is returned.
     *
     * @return the coordinate of the parent project, in the form
     *         groupId:artifactId:version.
     */
    public String getParentCoordinate()
    {
        if (myModel.getParent() != null)
        {
            // a parent project must always, by definition, be a pom project:
            return myModel.getParent().getGroupId() + ":" + myModel.getParent().getArtifactId() + ":pom:"
                    + myModel.getParent().getVersion();
        }
        return null;
    }
}
