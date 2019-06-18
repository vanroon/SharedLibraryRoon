#!/usr/bin/env groovy
package org.vanroon;

import groovy.transform.InheritConstructors

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

    def getCrumb(userColonPass=""){
        def resultText = getHttpRequestJson(url + "/crumbIssuer/api/json", userColonPass)
        def result = steps.readJSON text: resultText
        return result['crumb']
    }

    def deleteJob(String userColonPass, String jobName){
        def delUrl = url + "/job/${jobName}/doDelete"
        def jenkinsCrumb = getCrumb(userColonPass)
        def auth = encodeBase64(userColonPass)
        jEcho "Deleting job: ${jobName}"
        def result = postHttpRequestWithXMLBody(delUrl, jenkinsCrumb, auth)

        if (result == 200 || result == 201) {
            jEcho "HTTP Status code: " + result.toString()
            jEcho "job deleted"
        } else if (result == 400) {
            jEcho "HTTP Status code: " + result.toString()
            jEcho "Bad Request... Does the job exist?"
        } else if (result == 403) {
            jEcho "HTTP Status code: " + result.toString()
            jEcho "Code = 403, but job is deleted."
        }    
        else {
            jEcho "HTTP Status code: " + result.toString()
            jEcho "other error"
        }

    def addPipelineJob(
        String userColonPass="", 
        String jobName, 
        String gitlabRepository,
        String gitlabConnection,
        String gitlabToken,
        String gitlabCredentialsId,
        String scriptPath="Jenkinsfile"
    ){
        def pipelineConfig = new URL('http://10.212.0.109:8082/dwg-overhead/dwg-automation/raw/master/apiCalls/jenkins/addPipelineJob/pipelineConfig.xml').getText('UTF-8')
        pipelineConfig = pipelineConfig.replaceAll("@@gitlabconnection@@", "${gitlabConnection}")
        pipelineConfig = pipelineConfig.replaceAll("@@secrettoken@@", "${gitlabToken}")
        pipelineConfig = pipelineConfig.replaceAll("@@gitrepo@@", "${gitlabRepository}")
        pipelineConfig = pipelineConfig.replaceAll("@@credentialsid@@", "${gitlabCredentialsId}")
        pipelineConfig = pipelineConfig.replaceAll("@@scriptPath@@", "${scriptPath}")
        
        // Get Jenkins crumb
        def jenkinsCrumb = getCrumb(userColonPass)
        // Process authentication
        def auth = encodeBase64(userColonPass)
        // Pass request
        def result = postHttpRequestWithXMLBody(url + "/createItem?name=${jobName}", jenkinsCrumb, auth, pipelineConfig)
      
        if (result == 200 || result == 201) {
            jEcho result.toString()
            jEcho "job created"
        } else if (result == 400) {
            jEcho result.toString()
            jEcho "Bad Request.. Does the job already exist?"
        } else {
            jEcho result.toString()
            jEcho "other error"
        }
    }
}