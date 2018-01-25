package io.opensphere.develop.util.pom;

import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

/**
 * A java representation of a Maven coordinate. A coordinate, per the Maven
 * specification, is composed of 3 to 5 elements, including at least the group
 * ID, the artifact ID, and the version. A coordinate may also optionally
 * include the packaging of the artifact (e.g.: JAR, pom, WAR, etc.), and / or
 * the classifier of the artifact.
 *
 * From https://maven.apache.org/pom.html#Maven_Coordinates:
 * <h2>Maven Coordinates</h2>
 * <p>
 * The POM defined above is the minimum that both Maven 2 & 3 will allow.
 * groupId:artifactId:version are all required fields (although, groupId and
 * version need not be explicitly defined if they are inherited from a parent -
 * more on inheritance later). The three fields act much like an address and
 * timestamp in one. This marks a specific place in a repository, acting like a
 * coordinate system for Maven projects.
 * </p>
 * <ul>
 * <li>groupId: This is generally unique amongst an organization or a project.
 * For example, all core Maven artifacts do (well, should) live under the
 * groupId org.apache.maven. Group ID's do not necessarily use the dot notation,
 * for example, the junit project. Note that the dot-notated groupId does not
 * have to correspond to the package structure that the project contains. It is,
 * however, a good practice to follow. When stored within a repository, the
 * group acts much like the Java packaging structure does in an operating
 * system. The dots are replaced by OS specific directory separators (such as
 * '/' in Unix) which becomes a relative directory structure from the base
 * repository. In the example given, the org.codehaus.mojo group lives within
 * the directory $M2_REPO/org/codehaus/mojo.</li>
 * <li>artifactId: The artifactId is generally the name that the project is
 * known by. Although the groupId is important, people within the group will
 * rarely mention the groupId in discussion (they are often all be the same ID,
 * such as the Codehaus Mojo project groupId: org.codehaus.mojo). It, along with
 * the groupId, create a key that separates this project from every other
 * project in the world (at least, it should :) ). Along with the groupId, the
 * artifactId fully defines the artifact's living quarters within the
 * repository. In the case of the above project, my-project lives in
 * $M2_REPO/org/codehaus/mojo/my-project.</li>
 * <li>version: This is the last piece of the naming puzzle. groupId:artifactId
 * denote a single project but they cannot delineate which incarnation of that
 * project we are talking about. Do we want the junit:junit of today (version
 * 4), or of four years ago (version 2)? In short: code changes, those changes
 * should be versioned, and this element keeps those versions in line. It is
 * also used within an artifact's repository to separate versions from each
 * other. my-project version 1.0 files live in the directory structure
 * $M2_REPO/org/codehaus/mojo/my-project/1.0.</li>
 * </ul>
 * <p>
 * The three elements given above point to a specific version of a project
 * letting Maven knows who we are dealing with, and when in its software
 * lifecycle we want them.
 * </p>
 * <ul>
 * <li>
 * <p>
 * packaging: Now that we have our address structure of
 * groupId:artifactId:version, there is one more standard label to give us a
 * really complete address. That is the project's artifact type. In our case,
 * the example POM for org.codehaus.mojo:my-project:1.0 defined above will be
 * packaged as a jar. We could make it into a war by declaring a different
 * packaging:
 * </p>
 * <p>
 *
 * <pre>
 * &lt;project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi=
 * "http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation=
 * "http://maven.apache.org/POM/4.0.0
 * http://maven.apache.org/xsd/maven-4.0.0.xsd"&gt; ...
 * &lt;packaging&gt;war&lt;/packaging&gt; ... &lt;/project&gt;
 * </pre>
 * </p>
 * <p>
 * When no packaging is declared, Maven assumes the artifact is the default:
 * jar. The valid types are Plexus role-hints (read more on Plexus for a
 * explanation of roles and role-hints) of the component role
 * org.apache.maven.lifecycle.mapping.LifecycleMapping. The current core
 * packaging values are: pom, jar, maven-plugin, ejb, war, ear, rar, par. These
 * define the default list of goals which execute to each corresponding build
 * lifecycle stage for a particular package structure.
 * </p>
 * </li>
 * </ul>
 * <p>
 * You will sometimes see Maven print out a project coordinate as
 * groupId:artifactId:packaging:version.
 * </p>
 * <ul>
 * <li>classifier: You may occasionally find a fifth element on the coordinate,
 * and that is the classifier. We will visit the classifier later, but for now
 * it suffices to know that those kinds of projects are displayed as
 * groupId:artifactId:packaging:classifier:version.</li>
 * </ul>
 *
 * @see <a href=
 *      "https://maven.apache.org/pom.html#Maven_Coordinates">https://maven.apache.org/pom.html#Maven_Coordinates</a>
 */
public class Coordinate
{
    /**
     * The default value for the packaging portion of the coordinate.
     */
    private static final String DEFAULT_PACKAGING = "jar";

    /**
     * The group ID portion of the coordinate associated with the artifact.
     */
    private final String myGroupId;

    /**
     * The artifact ID portion of the coordinate associated with the artifact.
     */
    private final String myArtifactId;

    /**
     * The version portion of the coordinate associated with the artifact.
     */
    private final String myVersion;

    /**
     * The packaging portion of the coordinate associated with the artifact.
     */
    private final String myPackaging;

    /**
     * The classifier portion of the coordinate associated with the artifact.
     */
    private final String myClassifier;

    /**
     * Creates a coordinate using the minimum required elements, and the default
     * packaging.
     *
     * @param groupId The group ID portion of the coordinate associated with the
     *            artifact.
     * @param artifactId The artifact ID portion of the coordinate associated
     *            with the artifact.
     * @param version The version portion of the coordinate associated with the
     *            artifact.
     */
    public Coordinate(String groupId, String artifactId, String version)
    {
        this(groupId, artifactId, version, DEFAULT_PACKAGING);
    }

    /**
     * Creates a coordinate using the minimum required elements, along with the
     * supplied packaging.
     *
     * @param groupId The group ID portion of the coordinate associated with the
     *            artifact.
     * @param artifactId The artifact ID portion of the coordinate associated
     *            with the artifact.
     * @param version The version portion of the coordinate associated with the
     *            artifact.
     * @param packaging The packaging portion of the coordinate associated with
     *            the artifact.
     */
    public Coordinate(String groupId, String artifactId, String version, String packaging)
    {
        this(groupId, artifactId, version, packaging, null);
    }

    /**
     * Creates a coordinate using the minimum required elements, along with the
     * supplied packaging and classifier.
     *
     * @param groupId The group ID portion of the coordinate associated with the
     *            artifact.
     * @param artifactId The artifact ID portion of the coordinate associated
     *            with the artifact.
     * @param version The version portion of the coordinate associated with the
     *            artifact.
     * @param packaging The packaging portion of the coordinate associated with
     *            the artifact.
     * @param classifier The classifier portion of the coordinate associated
     *            with the artifact.
     */
    public Coordinate(String groupId, String artifactId, String version, String packaging, String classifier)
    {
        myGroupId = groupId;
        myArtifactId = artifactId;
        myVersion = version;
        myPackaging = packaging;
        myClassifier = classifier;
    }

    /**
     * Creates a coordinate by parsing the supplied coordinate string. The
     * string must be delimited according to the Maven coordinate definition,
     * available at <a href=
     * "https://maven.apache.org/pom.html#Maven_Coordinates">https://maven.apache.org/pom.html#Maven_Coordinates</a>.
     * The supplied string may contain two or more elements, with the group ID
     * and artifact ID required.
     * <p>
     * According to the Maven definition, the order of these elements changes
     * depending on how many tokens are in use. This ordering is summarized as:
     * <ul>
     * <li>3 Tokens: groupId:artifactId:version</li>
     * <li>4 Tokens: groupId:artifactId:packaging:version</li>
     * <li>5 Tokens: groupId:artifactId:packaging:classifier:version</li>
     * </ul>
     * </p>
     *
     * @param coordinateString the delimited coordinate string to parse to
     *            extract the coordinate elements.
     */
    public Coordinate(String coordinateString)
    {
        String[] elements = coordinateString.split(":");
        int elementCount = elements.length;
        if (elementCount < 2 || elementCount > 5)
        {
            throw new IllegalArgumentException("Unable to parse '" + coordinateString
                    + "' into coordinate. Must contain at least two and no more than 5 items delimited by a colon (':')");
        }

        myGroupId = elements[0];
        myArtifactId = elements[1];
        switch (elementCount)
        {
            case 3:
                myVersion = elements[2];
                myPackaging = DEFAULT_PACKAGING;
                myClassifier = null;
                break;
            case 4:
                myPackaging = elements[2];
                myVersion = elements[3];
                myClassifier = null;
                break;
            case 5:
                myPackaging = elements[2];
                myClassifier = elements[3];
                myVersion = elements[4];
                break;
            default:
                throw new IllegalArgumentException("Unable to parse '" + coordinateString
                        + "' into coordinate. Must contain at least two and no more than 5 items delimited by a colon (':')");

        }
    }

    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        return Objects.hash(myGroupId, myArtifactId, myVersion, myPackaging, myClassifier);
    }

    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof Coordinate)
        {
            return hashCode() == ((Coordinate)obj).hashCode();
        }
        return super.equals(obj);
    }

    /**
     * Gets the value of the {@link #myGroupId} field.
     *
     * @return the value stored in the {@link #myGroupId} field.
     */
    public String getGroupId()
    {
        return myGroupId;
    }

    /**
     * Gets the value of the {@link #myArtifactId} field.
     *
     * @return the value stored in the {@link #myArtifactId} field.
     */
    public String getArtifactId()
    {
        return myArtifactId;
    }

    /**
     * Gets the value of the {@link #myVersion} field.
     *
     * @return the value stored in the {@link #myVersion} field.
     */
    public String getVersion()
    {
        return myVersion;
    }

    /**
     * Gets the value of the {@link #myPackaging} field.
     *
     * @return the value stored in the {@link #myPackaging} field.
     */
    public String getPackaging()
    {
        return myPackaging;
    }

    /**
     * Gets the value of the {@link #myClassifier} field.
     *
     * @return the value stored in the {@link #myClassifier} field.
     */
    public String getClassifier()
    {
        return myClassifier;
    }

    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        StringBuilder returnValue = new StringBuilder();
        returnValue.append(myGroupId);
        returnValue.append(":");
        returnValue.append(myArtifactId);
        if (StringUtils.isNotBlank(myPackaging))
        {
            returnValue.append(":");
            returnValue.append(myPackaging);
        }
        if (StringUtils.isNotBlank(myClassifier))
        {
            returnValue.append(":");
            returnValue.append(myClassifier);
        }

        if (StringUtils.isNotBlank(myVersion))
        {
            returnValue.append(":");
            returnValue.append(myVersion);

        }

        return returnValue.toString();
    }
}
