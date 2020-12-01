#!/usr/bin/env groovy
// vars/buildImage.groovy
import com.company.GlobalVars

/**
 * buildImage: builds image and pushes to registry
 * INPUT:
 * OUTPUT:
 */


def call(String BuildTemplate) {
    timestamps {
        openshift.withCluster() {
            openshift.withProject(GlobalVars.BUILD_PROJECT) {
                def build = openshift.apply(openshift.process( BuildTemplate, \
                    "-p", "APP_NAME=$APP_NAME", \
                    "-p", "GIT_SOURCE_URL=$GIT_SOURCE_URL", \
                    "-p", "GIT_SOURCE_REF=$GIT_REF", \
                    "-p", "BUILDER_IMAGE_TYPE=$BUILDER_IMAGE_TYPE", \
                    "-p", "GIT_SOURCE_SECRET=$GIT_SOURCE_SECRET", \
                    "-p", "GIT_COMMIT_SHA=$GIT_COMMIT"
                ))
                // def created = openshift.apply( msbuild )
                def bc = build.startBuild("--follow")
            }
        }
        script {
          sh '''
            echo oiIamAStepBuildingIMAGE
          '''
        }
    }
}
