#!/usr/bin/env groovy
// vars/deployOCPImage.groovy
import org.svb.GlobalVariables
import org.svb.PipelineBuilder

def call(
    String BUILD_PROJECT_NAME,
    String APP_NAME,
    String TMOS_APPLICATION_ROUTE
){
    def helper = new PipelineBuilder(this)
    timestamps {
        def msg = "Deploying ${APP_NAME}"
        helper.banner(msg, ":", 3)
        openshift.withCluster(GlobalVariables.OpenshiftSvbCluster) {
            openshift.withProject(BUILD_PROJECT_NAME) {
                def deployment = openshift.apply(openshift.process(readFile(GlobalVariables.DeploymentTemplatePath),
                    "--ignore-unknown-parameters", \
                    "-p" , "TMOS_APPLICATION_NAME=$APP_NAME", \
                    "-p" , "TMOS_PROJECT_KEY=$BUILD_PROJECT_NAME", \
                    "-p" , "TMOS_APPLICATION_ROUTE=$TMOS_APPLICATION_ROUTE", \
                    "-p" , "GIT_COMMIT=${env.GIT_COMMIT}"
                )).narrow("dc")
                echo "Apply created ${deployment.count()} objects named: ${deployment.names()}"
                deployment.describe()
            }
        }
    }

}