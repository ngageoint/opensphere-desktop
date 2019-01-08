node {
    properties([[$class: 'BuildDiscarderProperty',
        strategy: [$class: 'LogRotator', artifactDaysToKeepStr: '', artifactNumToKeepStr: '', daysToKeepStr: '', numToKeepStr: '3']]]);

    env.WORKSPACE = pwd();
    env.LOCAL_REPO = "${env.WORKSPACE}/localRepository/${env.BUILD_NUMBER}";
    env.JAVA_HOME = tool 'JDK_10uLatest';
    env.PATH="${env.JAVA_HOME}/bin:${env.PATH}";
    env.mvnHome = tool 'Maven 3 (built-in)';
    env.SLOW_MACHINE = true;
    env.REMOTE_REPO_ARGS = "-DaltSnapshotDeploymentRepository=nexus-FADE-Snapshots::default::https://nexus.devops.geointservices.io/content/repositories/FADE-Snapshots -DaltReleaseDeploymentRepository=nexus-FADE-COTS::default::https://nexus.devops.geointservices.io/content/repositories/FADE-COTS/";

    stage ('Checkout') {
        try {
            checkout scm
        } catch (error) {
            notifyFailed();
            throw error;
        }
    }

    stage ('Compile') {
        try {
            if ("$env.JENKINS_URL".contains("jenkins.devops.geointservices.io")) {
                if("${env.BRANCH_NAME}".contains('release_')) {
                    env.REMOTE_REPO_ARGS = "-DaltSnapshotDeploymentRepository=nexus-FADE-Snapshots::default::https://nexus.devops.geointservices.io/content/repositories/FADE-Snapshots -DaltReleaseDeploymentRepository=nexus-FADE-COTS::default::https://nexus.devops.geointservices.io/content/repositories/FADE-COTS/ -DaltDeploymentRepository=nexus-FADE-COTS::default::https://nexus.devops.geointservices.io/content/repositories/FADE-COTS/"
                } else {
                    env.REMOTE_REPO_ARGS = "-DaltSnapshotDeploymentRepository=nexus-FADE-Snapshots::default::https://nexus.devops.geointservices.io/content/repositories/FADE-Snapshots -DaltReleaseDeploymentRepository=nexus-FADE-COTS::default::https://nexus.devops.geointservices.io/content/repositories/FADE-COTS/";
                }             
            } else {
                env.REMOTE_REPO_ARGS = "-DaltSnapshotDeploymentRepository=snapshot::default::http://archiva.stwan.bits/archiva/repository/snapshot -DaltReleaseDeploymentRepository=internal::default::http://archiva.stwan.bits/archiva/repository/internal -DaltDeploymentRepository=internal::default::http://archiva.stwan.bits/archiva/repository/internal/";
            }
			configFileProvider(
				[configFile(fileId: '3d2775d8-f723-465f-829a-969d0ae5f40b', variable: 'MAVEN_SETTINGS')]) {
	            sh "${env.mvnHome}/bin/mvn -s $MAVEN_SETTINGS --no-snapshot-updates -Dmaven.repo.local=${env.LOCAL_REPO} clean install deploy -Pautomated ${env.REMOTE_REPO_ARGS}"
	        }
        } catch (error) {
            notifyFailed();
            throw error;
        }
    }

    stage ('Archive Artifacts') {
        archive 'open-sphere-installers/open-sphere-installer-*/target/open-sphere-installer-*-*.jar'
    }
    
	if(!"${env.BRANCH_NAME}".startsWith('feature')) {
	    stage ('Site') {
	        try {
	            sh "${env.mvnHome}/bin/mvn --no-snapshot-updates -Dmaven.repo.local=${env.LOCAL_REPO} site"
	            sh "${env.mvnHome}/bin/mvn --no-snapshot-updates -Dmaven.repo.local=${env.LOCAL_REPO} site:stage"
	        } catch (error) {
	            notifyFailed();
	            throw error;
	        }
	    }
	
	    stage ('Archive Code Analysis') {
	        step([$class: 'PmdPublisher', canComputeNew: false, defaultEncoding: '', healthy: '', pattern: '**/pmd.xml', unHealthy: ''])
	        step([$class: 'CheckStylePublisher', canComputeNew: false, defaultEncoding: '', healthy: '', pattern: '**/target/checkstyle-result.xml', unHealthy: ''])
	        junit('**/target/surefire-reports/*.xml')
	        publishHTML([allowMissing: false, alwaysLinkToLastBuild: false, keepAll: false, reportDir: 'target/staging/', reportFiles: 'index.html', reportName: 'Site Documentation'])
	    }
    }
}

def notifyFailed() {
/*    emailext attachLog: true,
        body: "An OpenSphere build has failed for branch ${BRANCH_NAME}. See \n${BUILD_URL} for more information.",
        compressLog: false,
        recipientProviders: [
            [$class: 'CulpritsRecipientProvider'],
            [$class: 'DevelopersRecipientProvider'],
            [$class: 'RequesterRecipientProvider']],
        subject: 'OpenSphere Build Failure';*/
}
