DO NOT ADD ANY NEW LIBRARIES TO THIS FOLDER UNLESS ABSOLUTELY NECESSARY.

The libraries contained within this folder are essentially forked versions of 
their original projects. This approach was taken either because the original 
project no longer existed and / or because the necessary changes were not 
accepted by the original project but were needed as part of the Open Sphere
ecosystem.

DO NOT ADD ANY NEW LIBRARIES TO THIS FOLDER UNLESS ABSOLUTELY NECESSARY.

This approach is in direct contravention of the standard Maven practices, and 
should not be replicated or extended. The use of this strategy use was chosen
AS A LAST RESORT.

DO NOT ADD ANY NEW LIBRARIES TO THIS FOLDER UNLESS ABSOLUTELY NECESSARY.

All libraries contained within this folder must adhere to their original 
license terms, which, in most cases (such as GPL licensed software) requires
distribution of all source code, including revisions made as part of the fork.
As a standard practice, all source files are included in JAR files with the 
"-sources" extension, and are installed into the user's local repository during
the build with the classifier "sources", following the standard Maven practice.
Additionally, the license file governing each dependency is also stored with 
the artifact in this folder, but is not installed to the user's local 
repository during the build. Finally, if a "javadoc" artifact could be found, 
it is also included in this directory, and also installed into the user's local
repository during the build.

DO NOT ADD ANY NEW LIBRARIES TO THIS FOLDER UNLESS ABSOLUTELY NECESSARY.  

Finally, to indicate that the artifact is a forked version, a convention was 
chosen to change the Group ID of each artifact to be prefaced by 
'io.open-sphere' followed by the final token from the original group ID, along
with an appended '.1' version number. For example, the original 
net.sourceforge.hatbox:hatbox:1.0.b9 artifact, the Group ID and Version are 
changed to io.open-sphere.hatbox:hatbox:1.0.b9.1 to indicate that the forked
version is used (and to indicate that, on no uncertain terms is this original 
version of the artifact).

DO NOT ADD ANY NEW LIBRARIES TO THIS FOLDER UNLESS ABSOLUTELY NECESSARY.  
