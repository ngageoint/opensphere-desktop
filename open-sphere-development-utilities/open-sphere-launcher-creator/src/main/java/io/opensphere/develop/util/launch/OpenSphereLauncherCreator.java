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
    private final ProjectLauncherWriter myWriter;

    /**
     * The project reader used to parse the project file(s).
     */
    private final AbstractCompositeProjectReader myProjectReader;

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
        if (args.length < 1)
        {
            LOG.error("arguments: <opensphere directory name> [<profile>]");
            return;
        }

        String projName = args[0];
        String root = System.getProperty("user.dir");
        System.out.println(root);

        root = root.substring(0, root.indexOf(projName));

        String profile = "unclass";
        if (args.length == 2)
        {
            profile = args[1];
        }

        CompositeProjectModel compositeProjectModel = new CompositeProjectModel(Paths.get(root));

        OpenSphereLauncherCreator creator = new OpenSphereLauncherCreator(
                new OpenSphereProjectReader(compositeProjectModel, new HashSet<>(Arrays.asList(profile.split(",")))));
        creator.processProjects(projName);

    }

    /**
     * Reads projects using the project reader, and writes the
     *
     * @param projectName the name of the project folder
     */
    public void processProjects(String projectName)
    {
        Project project = myProjectReader.readProject(projectName);

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
