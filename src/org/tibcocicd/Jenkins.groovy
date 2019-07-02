#!/usr/bin/env groovy
package org.tibcocicd;

import groovy.transform.InheritConstructors

/**
 * Class Jenkins represents a Jenkins instance and contains operations
 * on and with Jenkins like API calls and deleting / creating jobs.
 */

@InheritConstructors
class Jenkins extends PipelineBuilder {

    def url
    def credentials
    def workspace

    Jenkins(steps, url, credentials){
        super(steps)
        this.url = url
        this.credentials = credentials
    }

    def setJenkinsWorkspace(workspace){
        this.workspace = workspace
    }

    /**
     * getCrumb: executes API GET call to Jenkins server with proper credentials to retrieve a
     * crumb. This string is required for getting and posting additional information
     * INPUT:
     *  - String userColonPass: conjoined username and password string, to be retrieved from
     *    Jenkins' credential store
     * OUTPUT:
     *  - String crumb
     */
    def getCrumb(userColonPass=""){
        def resultText = getHttpRequestJson(url + "/crumbIssuer/api/json", userColonPass)
        def result = steps.readJSON text: resultText
        return result['crumb']
    }

    /**
     * deleteJob: deletes a job from Jenkins
     * INPUT:
     *  - String userColonPass: conjoined username and password string, to be retrieved from
     *    Jenkins' credential store
     *  - String path: full path to job to be deleted
     *  - String jobName: jobName of job to be deleted
     * OUTPUT:
     *  - nothing
     * TODO: 
     *  - Method runs successfully but prints code 403.
     */
    def deleteJob(String userColonPass, String path, String jobName){
        def delUrl = url + path + "/job/${jobName}/doDelete"
        def jenkinsCrumb = getCrumb(userColonPass)
        def auth = encodeBase64(userColonPass)
        echo "Deleting job: ${jobName}"
        def result = postHttpRequestWithXMLBody(delUrl, jenkinsCrumb, auth)

        if (result == 200 || result == 201) {
            echo "HTTP Status code: " + result.toString()
            echo "job deleted"
        } else if (result == 400) {
            echo "HTTP Status code: " + result.toString()
            echo "Bad Request... Does the job exist?"
        } else if (result == 403) {
            echo "HTTP Status code: " + result.toString()
            echo "Code = 403, but job is deleted."
        }    
        else {
            echo "HTTP Status code: " + result.toString()
            echo "other error"
        }
    }

    /**
     * createPipelineConfigXml: Builds a config.xml file for Tibco build, deploy and
     * release Jenkinsfiles. Takes parameters to be replaced as input
     * INPUT:
     *  - String gitlabConnection:      Gitlab connection name as defined in Jenkins settings
     *  - String gitlabRepository:      GIT URL of git repository
     *  - String gitlabCredentialsId:   value of credential ID to be inserted in Jenkinsfile
     *  - String gitlabToken:           value of secret token for Gitlab hook
     *  - String scriptPath (optional): path to Jenkinsfile in Git repository
     * OUTPUT:
     *  - String pipelineConfig: Jenkins job config.xml file with values replaced
     */
    def createPipelineConfigXml(
        String gitlabConnection,
        String gitlabRepository,
        String gitlabCredentialsId,
        String gitlabToken,
        String scriptPath="Jenkinsfile"
    ){
        // Get config.xml
        def pipelineConfig = new URL(GlobalVars.defaultPipelineConfigXml).getText('UTF-8')
        // replace values
        pipelineConfig = pipelineConfig.replaceAll("@@gitlabconnection@@", "${gitlabConnection}")
        pipelineConfig = pipelineConfig.replaceAll("@@secrettoken@@", "${gitlabToken}")
        pipelineConfig = pipelineConfig.replaceAll("@@gitrepo@@", "${gitlabRepository}")
        pipelineConfig = pipelineConfig.replaceAll("@@credentialsid@@", "${gitlabCredentialsId}")
        pipelineConfig = pipelineConfig.replaceAll("@@scriptPath@@", "${scriptPath}")
        return pipelineConfig
    }

    /**
     * createJob: creates job in Jenkins 
     * INPUT:
     *  - String userColonPass: conjoined username and password string, to be retrieved from
     *    Jenkins' credential store
     *  - String path:      full target path of to be created job
     *  - String jobName:   name of to be created job
     *  - String configXml: Full job definition (config.xml) as string
     * OUTPUT:
     *  - nothing
     * NOTE:
     *  - Method returns nothing but prints HTTP response code and error message if any.
     */
    def createJob(
        String userColonPass,
        String path,
        String jobName,
        String configXml
    ){
        // Get Jenkins crumb
        def jenkinsCrumb = getCrumb(userColonPass)
        // Process authentication
        def auth = encodeBase64(userColonPass)
        // Pass request
        def result = postHttpRequestWithXMLBody(url + path + "/createItem?name=${jobName}", jenkinsCrumb, auth, configXml)
     
        if (result == 200 || result == 201) {
            echo result.toString()
            echo "job created"
        } else if (result == 400) {
            echo result.toString()
            echo "Bad Request.. Does the job already exist?"
        } else {
            echo result.toString()
            echo "other error"
        }
    }
}