#!/usr/bin/env groovy
// vars/buildImage.groovy
import org.svb.GlobalVariables
import org.svb.PipelineBuilder

/**
 * buildImage: builds image and pushes to registry
 * INPUT:
 * OUTPUT:
 */


def call(
    String BUILD_PROJECT_NAME,
    String APP_NAME, 
    //String GIT_REF,
    String BUILDER_IMAGE_TYPE
) { 
    def helper = new PipelineBuilder(this)
    def gitBranch = helper.getGitBranch(env.GIT_BRANCH)
    timestamps {
        def msg = "Start building ${APP_NAME} using commit ${GIT_COMMIT}"
        helper.banner(msg, ":", 3)
        script {
            openshift.withCluster(GlobalVariables.OpenshiftSvbCluster) {
                openshift.withProject(BUILD_PROJECT_NAME) {
                    def build = openshift.apply(openshift.process(readFile(GlobalVariables.BuildTemplatePath), \
                        "-p", "BUILD_PROJECT_NAME=$BUILD_PROJECT_NAME", \
                        "-p", "APP_NAME=$APP_NAME", \
                        "-p", "GIT_SOURCE_REF=$gitBranch", \
                        "-p", "BUILDER_IMAGE_TYPE=$BUILDER_IMAGE_TYPE", \
                        "-p", "GIT_SOURCE_URL=${env.GIT_URL}", \
                        "-p", "GIT_COMMIT_SHA=${env.GIT_COMMIT}", \
                        "-p", "GIT_SOURCE_SECRET=${GlobalVariables.OpenshiftGitSecret}", \
                        "-p", "MAVEN_MIRROR_URL=${GlobalVariables.SecuredMavenMirrorRepo}", \
			"-p", "NPM_MIRROR_URL=${GlobalVariables.SecuredNPMRepo}"
                    )).narrow("bc")
                    def bc = build.startBuild("--follow")
                }
            }
	
	    if (gitBranch == 'master' ){
	    	echo "Building on GIT branch: ${gitBranch}"
                def SOURCE_IMAGE = "${OpenshiftImageRegistry}/${BUILD_PROJECT_NAME}/${APP_NAME}:${GIT_COMMIT}"
	        def DESTINATION_IMAGE = "${ArtifactoryImageRepo}/${BUILD_PROJECT_NAME}/${APP_NAME}:${GIT_COMMIT}" 
	        copyImage(SOURCE_IMAGE, DESTINATION_IMAGE) 
	    }
	} 
    }
}
