#!/usr/bin/env groovy
package com.dwg;

import groovy.transform.InheritConstructors

@InheritConstructors
class Nexus extends PipelineBuilder implements Repository {

    def url
    def credentials
    def nexusVersion

    Nexus(steps, url, credentials, nexusVersion) {
        super(steps)
        this.url = url
        this.credentials = credentials
        this.nexusVersion = nexusVersion
    }
    /**
    *   Pings Nexus. Should return 'pong'
    **/
    def ping(){
        steps.withCredentials([steps.usernameColonPassword(credentialsId: credentials, variable: 'USERPASS')]){
            echo "...Pinging Nexus..."
            sh """curl -u\$USERPASS ${url}service/metrics/ping"""
        }
    }

    def upload(String fileName, NexusComponent nc){
        upload(fileName, nc.repository, nc.groupId, nc.artifactId, nc.extension, nc.version)
    }

    def upload(String fileName, String repository, String groupId, String artifactId, String extension, String version, String classifier="") {
        echo "Upload artifact"
        echo "artifactId: ${artifactId}"
        echo "file: ${fileName}"
        echo "type: ${extension}"
        steps.nexusArtifactUploader artifacts: [
            [artifactId: "${artifactId}", classifier: "${classifier}", file: "${fileName}", type: "${extension}"]
        ],
        credentialsId: credentials, 
        groupId: groupId,
        nexusUrl: url,
        nexusVersion: nexusVersion,
        protocol: 'http', 
        repository: repository,
        version: "${version}"
    }

    def download(NexusComponent nc){
        def file = download(nc.repository, nc.groupId, nc.artifactId, nc.extension, nc.version)
        return file
    }

    def download(String repository, String groupId, String artifactId, String extension, String version, String classifier="") {
        String outPath = ""

        steps.withCredentials (
            [steps.usernameColonPassword(credentialsId: credentials, variable: 'USERPASS')]
        ){
            def searchUrl = "${url}/service/rest/v1/search/assets/?repository=${repository}&group=${groupId}&maven.artifactId=${artifactId}&version=${version}&maven.extension=${extension}"
            def resultText = sh ([script: """curl -s -v -u\${USERPASS} -X GET --header 'Accept: application/json' '${searchUrl}'""", returnStdout: true]).trim()
            
            def result = steps.readJSON text: resultText
            def artifact = result['items'][0]
            def downloadUrl = artifact['downloadUrl']
            def remoteSha = result['items']['checksum']['sha1'][0]
            outPath = "${steps.WORKSPACE}/${basename(artifact['path'])}"

            echo "downloading ${downloadUrl} to ${outPath}"
            sh """ curl -s -X GET -u\$USERPASS "${downloadUrl}" -o ${outPath}"""

            def localSha = steps.sha1 "${outPath}"
            def errorMsg = "SHA1 checksum of downloaded is not matching"
            try {
                assert remoteSha == localSha : errorMsg
                echo "Checksum match"
            } catch (AssertionError e) {
                echo "ERROR: " + e.getMessage()
                steps.addErrorBadge text: errorMsg
                error errorMsg
            }
            // outPath = result
        }
        return outPath
    }

    def getAvailableVersions(String repository, String groupId, String artifactId) {
        def resultText = getHttpRequestJson("http://${url}/service/rest/v1/search?sort=version&direction=desc&repository=${repository}&group=${groupId}&name=${artifactId}")
        def result = steps.readJSON text: resultText
        def versionList = []
        def items = result['items']
        result['items'].each {
            versionList += it['version']
        }
        versionList.add(0, "Latest")
        return versionList
    }

     def getAvailableVersions(NexusComponent nc) {
         return getAvailableVersions(nc.repository, nc.groupId, nc.artifactId)
    }

    def getLatestVersion(String repository, String groupId, String artifactId) {
        return getAvailableVersions(repository, groupId, artifactId)[1]
    }

    def getLatestVersion(NexusComponent nc) {
        return getAvailableVersions(nc)[1]
    }
}