package io.opensphere.develop.util.launch;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.maven.model.Dependency;

import io.opensphere.develop.util.pom.AbstractCompositeProjectReader;
import io.opensphere.develop.util.pom.CompositeProjectModel;
import io.opensphere.develop.util.pom.OpenSphereProjectReader;
import io.opensphere.develop.util.pom.Project;

/**
 * A driver class in which launchers are created for all known products.
 */
public final class OpenSphereLauncherCreator
{
    /**
     * The {@link Logger} instance used to capture output.
     */
    private static final Logger LOG = Logger.getLogger(OpenSphereLauncherCreator.class);

    /**
     * The writer used to create the eclipse launcher file.
     */
    private ProjectLauncherWriter myWriter;

    /**
     * The project reader used to parse the project file(s).
     */
    private AbstractCompositeProjectReader myProjectReader;

    /**
     * Creates a new launcher creator.
     *
     * @param projectReader the reader used to parse the project file.
     */
    public OpenSphereLauncherCreator(AbstractCompositeProjectReader projectReader)
    {
        myProjectReader = projectReader;
        myWriter = new ProjectLauncherWriter();
    }

    /**
     * Executes the launcher creator. Use a single command line argument to
     * specify the profiles to use (comma separated if more than one).
     *
     * @param args the set of arguments supplied by the user.
     */
    public static void main(String[] args)
    {
        String root = System.getProperty("user.dir");
        root = root.substring(0, root.indexOf("OpenSphereDesktop"));

        String profile = "unclass";
        if (args.length == 1)
        {
            profile = args[0];
        }

        CompositeProjectModel compositeProjectModel = new CompositeProjectModel(Paths.get(root));

        OpenSphereLauncherCreator creator = new OpenSphereLauncherCreator(
                new OpenSphereProjectReader(compositeProjectModel, new HashSet<>(Arrays.asList(profile.split(",")))));
        creator.processProjects();

    }

    /**
     * Reads projects using the project reader, and writes the
     */
    public void processProjects()
    {
        Project project = myProjectReader.readProject();

        CompositeProjectModel compositeProjectModel = myProjectReader.getCompositeProjectModel();

        List<String> modules = compositeProjectModel.getProjects(project);
        Set<Dependency> dependencies = compositeProjectModel.getExternalDependencies(project);

        Project parent = project.getParent();
        while (parent != null)
        {
            modules.addAll(compositeProjectModel.getProjects(parent));
            dependencies.addAll(compositeProjectModel.getExternalDependencies(parent));

            parent = parent.getParent();
        }

        LOG.info(project.getTitle() + " Modules: " + modules.size());
        LOG.info(project.getTitle() + " Dependencies: " + dependencies.size());

        for (OsInfo osInfo : OsInfo.values())
        {
            myWriter.write(modules, dependencies, project, osInfo);
        }
    }
}
