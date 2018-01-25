package io.opensphere.develop.util.launch;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.maven.model.Dependency;

import io.opensphere.develop.util.pom.Project;

/**
 * Creates launchers for the various deployments.
 */
public final class ProjectLauncherWriter
{
    /**
     * The {@link Logger} instance used to capture output.
     */
    private static final Logger LOG = Logger.getLogger(ProjectLauncherWriter.class);

    /** Standard XML header. */
    private static final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>";

    /**
     * Writes the data to a launcher file.
     *
     * @param projects the projects
     * @param dependencies the dependencies
     * @param rootProject the product
     * @param osInfo the OS info
     */
    public void write(Collection<String> projects, Collection<Dependency> dependencies, Project rootProject, OsInfo osInfo)
    {
        List<String> lines = generateLauncherLines(projects, dependencies, rootProject, osInfo);
        Path path = Paths.get(rootProject.getRootDir(), "eclipse", "launches",
                rootProject.getLauncherPrefix() + osInfo.getExtension() + ".launch");
        try
        {
            Files.write(path, lines);
            LOG.info("Updated " + path);
        }
        catch (IOException e)
        {
            LOG.error("Unable to write to file '" + path.toAbsolutePath().toString() + "'", e);
        }
    }

    /**
     * Generates the lines for the launcher file.
     *
     * @param projects the projects
     * @param dependencies the dependencies
     * @param rootProject the product
     * @param osInfo the OS info
     * @return the lines
     */
    private List<String> generateLauncherLines(Collection<String> projects, Collection<Dependency> dependencies,
            Project rootProject, OsInfo osInfo)
    {
        List<String> lines = new ArrayList<>();
        lines.add(XML_HEADER);

        // Beginning stuff
        lines.add("<launchConfiguration type=\"org.eclipse.jdt.launching.localJavaApplication\">");
        lines.add("<listAttribute key=\"org.eclipse.debug.core.MAPPED_RESOURCE_PATHS\">");
        lines.add(listEntry("/core/src/main/java/io/opensphere/core/appl/OpenSphere.java"));
        lines.add("</listAttribute>");
        lines.add("<listAttribute key=\"org.eclipse.debug.core.MAPPED_RESOURCE_TYPES\">");
        lines.add(listEntry("1"));
        lines.add("</listAttribute>");
        lines.add(strAttr("org.eclipse.debug.core.source_locator_id",
                "org.eclipse.jdt.launching.sourceLocator.JavaSourceLookupDirector"));
        lines.add(strAttr("org.eclipse.debug.core.source_locator_memento", createSourceLocator(projects)));
        lines.add(boolAttr("org.eclipse.jdt.launching.ATTR_USE_START_ON_FIRST_THREAD", "true"));

        // Classpath
        lines.add("<listAttribute key=\"org.eclipse.jdt.launching.CLASSPATH\">");
        lines.add(listEntryXml("containerPath",
                "org.eclipse.jdt.launching.JRE_CONTAINER/org.eclipse.jdt.internal.debug.ui.launcher.StandardVMType/JavaSE-1.8",
                "path", "1", "type", "4"));
        lines.add(listEntryXml("containerPath", "org.eclipse.m2e.MAVEN2_CLASSPATH_CONTAINER", "path", "3", "type", "4"));
        for (String additional : rootProject.getAdditionalClasspathItems())
        {
            lines.add(listEntryXml("internalArchive", "/" + additional, "path", "3", "type", "2"));
        }
        lines.add(listEntryXml("internalArchive", "/core/src/main/resources", "path", "3", "type", "2"));
        for (String project : projects)
        {
            lines.add(listEntryXml("path", "3", "projectName", project, "type", "1"));
        }
        for (Dependency dependency : dependencies)
        {
            if (!projects.contains(dependency.getArtifactId()))
            {
                lines.add(listEntryXml("containerPath", getJarPath(dependency, rootProject), "path", "3", "type", "3"));
            }
        }
        lines.add("</listAttribute>");

        // End stuff
        lines.add(strAttr("org.eclipse.jdt.launching.CLASSPATH_PROVIDER", "org.eclipse.m2e.launchconfig.classpathProvider"));
        lines.add(boolAttr("org.eclipse.jdt.launching.DEFAULT_CLASSPATH", "false"));
        lines.add(strAttr("org.eclipse.jdt.launching.MAIN_TYPE", "io.opensphere.core.appl.OpenSphere"));
        lines.add(strAttr("org.eclipse.jdt.launching.PROJECT_ATTR", "core"));
        lines.add(strAttr("org.eclipse.jdt.launching.SOURCE_PATH_PROVIDER", "org.eclipse.m2e.launchconfig.sourcepathProvider"));
        lines.add(strAttr("org.eclipse.jdt.launching.VM_ARGUMENTS", getVmArgs(rootProject, osInfo)));
        lines.add("</launchConfiguration>");

        return lines;
    }

    /**
     * Creates the source locator value.
     *
     * @param projects the projects
     * @return the line
     */
    private static String createSourceLocator(Collection<String> projects)
    {
        List<String> lines = new ArrayList<>();
        lines.add(XML_HEADER);
        lines.add("<sourceLookupDirector>");
        lines.add("<sourceContainers duplicates=\"false\">");
        for (String project : projects)
        {
            lines.add("<container memento=\"" + xmlEncode(XML_HEADER + "\r\n<javaProject name=\"" + project + "\"/>\r\n")
                    + "\" typeId=\"org.eclipse.jdt.launching.sourceContainer.javaProject\"/>");
        }
        lines.add("<container memento=\"" + xmlEncode(XML_HEADER + "\r\n<default/>\r\n")
                + "\" typeId=\"org.eclipse.debug.core.containerType.default\"/>");
        lines.add("</sourceContainers>");
        lines.add("</sourceLookupDirector>");
        lines.add("");
        return xmlEncode(String.join("\r\n", lines));
    }

    /**
     * Gets the path in the repo for the dependency.
     *
     * @param dependency the dependency
     * @param project the project in which the dependency is defined.
     * @return the path
     */
    private static String getJarPath(Dependency dependency, Project project)
    {
        String groupId = project.resolveProperties(dependency.getGroupId());
        String version = project.resolveProperties(dependency.getVersion());
        String artifactId = project.resolveProperties(dependency.getArtifactId());
        String classifier = project.resolveProperties(dependency.getClassifier());

        StringBuilder path = new StringBuilder("M2_REPO/").append(groupId.replace('.', '/')).append('/');
        path.append(artifactId).append('/').append(version).append('/');
        path.append(artifactId).append('-').append(version);
        if (classifier != null)
        {
            path.append('-').append(classifier);
        }
        path.append(".jar");
        return path.toString();
    }

    /**
     * Gets the VM arguments.
     *
     * @param rootProject the product
     * @param osInfo the OS info
     * @return the VM arguments
     */
    private String getVmArgs(Project rootProject, OsInfo osInfo)
    {
        return "-Dopensphere.productionMode=false -Djava.security.policy=&quot;${workspace_loc:/core/java.policy}&quot;"
                + " -Djava.library.path=&quot;${workspace_loc:/core/lib/" + osInfo.getDirectory() + "/x86_64}&quot;"
                + " -Xmx3000m -ea -XX:+AggressiveOpts -XX:+UseMembar -XX:+UseG1GC -XX:G1ReservePercent=40"
                + " -Dopensphere.version=" + rootProject.getVersion() + " -Dopensphere.useragent=&quot;" + rootProject.getTitle()
                + "&quot;" + rootProject.getAdditionalVmArgs();
    }

    /**
     * Creates a list entry line.
     *
     * @param values the key/value pairs
     * @return the line
     */
    private static String listEntryXml(String... values)
    {
        StringBuilder sb = new StringBuilder(XML_HEADER).append("\r\n<runtimeClasspathEntry");
        for (int i = 0; i < values.length - 1; i += 2)
        {
            String key = values[i];
            String value = values[i + 1];
            sb.append(' ').append(key).append("=\"").append(value).append('"');
        }
        sb.append("/>\r\n");
        return listEntry(xmlEncode(sb.toString()));
    }

    /**
     * Creates a list entry line.
     *
     * @param value the value
     * @return the line
     */
    private static String listEntry(String value)
    {
        return new StringBuilder("<listEntry value=\"").append(value).append("\"/>").toString();
    }

    /**
     * Creates a string attribute line.
     *
     * @param key the key
     * @param value the value
     * @return the line
     */
    private static String strAttr(String key, String value)
    {
        return new StringBuilder("<stringAttribute key=\"").append(key).append("\" value=\"").append(value).append("\"/>")
                .toString();
    }

    /**
     * Creates a boolean attribute line.
     *
     * @param key the key
     * @param value the value
     * @return the line
     */
    private static String boolAttr(String key, String value)
    {
        return new StringBuilder("<booleanAttribute key=\"").append(key).append("\" value=\"").append(value).append("\"/>")
                .toString();
    }

    /**
     * XML-encodes the string.
     *
     * @param s the string
     * @return the encoded string
     */
    private static String xmlEncode(String s)
    {
        String e = s;
        e = e.replace("&", "&amp;");
        e = e.replace("\"", "&quot;");
        e = e.replace("<", "&lt;");
        e = e.replace(">", "&gt;");
        e = e.replace("\n", "&#10;");
        e = e.replace("\r", "&#13;");
        return e;
    }
}
