Structure of a Plugin
---------------------

    myplugin/
       |- .checkstyle                           <- checkstyle configuration used during builds 
       |- .classpath                            <- Eclipse project classpath
       |- .project                              <- Eclipse project definition
       |- pom.xml                               <- Maven build file
       |- project-checkstyle-suppressions.xml   <- PMD project-specific suppression configuration
       |- src/                                  <- Folder in which all resources are stored
           |- main/                             <- Folder in which all non-test resources are stored
               |- java/                         <- Folder in which all non-test Java Source is stored
               |- resources/                    <- Folder in which all non-Java resources are stored
           |- test/                             <- Folder in which all test resources are stored
               |- java/                         <- Folder in which all test Java Source is stored
               |- resources/                    <- Folder in which all non-Java test resources are stored
               

Creating a new plugin project using Eclipse
---------------------------
1. Create a pom.xml file in the root directory of the project.
2. Put a pluginLoader.xml file in src/main/resources
3. Write code
4. Share the project to GIT
5. Set svn:ignore property on the top level directory (trunk) to the following: 'bin', 'target', '.pmd'
