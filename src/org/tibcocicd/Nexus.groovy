#!/usr/bin/env groovy
package org.tibcocicd;

import groovy.transform.InheritConstructors

/**
 * Class Nexus represents a Nexus instance and contains operations
 * on and with Nexus like API calls
 */

@InheritConstructors
class Nexus extends PipelineBuilder {
// class Nexus extends PipelineBuilder implements Repository {
    def url
    def credentials
    def nexusVersion
    def userColonPassword

    Nexus(steps, url, credentials, nexusVersion) {
        super(steps)
        this.url = url
        this.credentials = credentials
        this.nexusVersion = nexusVersion
    }

    def setUserColonPassword(userColonPassword){
        this.userColonPassword = userColonPassword
    }

    /**
    *   Pings Nexus. Should return 'pong'
    *   INPUT:
    *    - String usernameColonPassword:    The usernameColonPassword variable assign in a withCredentials block
    **/
    def ping(String usernameColonPassword){
        echo "...Pinging Nexus..."
        echo "http://${url}/service/metrics/ping"
        if (steps.NODE_NAME == "master") {
            echo "I am on master"
            def res = getHttpRequestJson("http://${url}/service/metrics/ping", usernameColonPassword)
            echo res
        } else {
            echo "I am not on master"
        }
    }

    /**
     * upload: uploads file to Nexus
     * INPUT:
     *  - String fileName:   full filename with path to be uploaded
     *  - NexusComponent nc: Instantiated NexusComponent object (see NexusComponent.groovy)
     * OUTPUT:
     *  - nothing
     * NOTE:
     *  - Method is a wrapper that uses a NexusComponent object for calling the upload
     *    method rather than passing all required string seperately.
     */
    def upload(String fileName, NexusComponent nc){
        upload(fileName, nc.repository, nc.groupId, nc.artifactId, nc.extension, nc.version)
    }

    /**
     * upload: uploads file to Nexus
     * INPUT:
     *  - String fileName:              full filename with path to be uploaded
     *  - String repository:            name of repository to upload artifact to
     *  - String groupId:               groupId of artifact
     *  - String artifactId:            artifactId of artifact
     *  - String extension:             extension of artifact
     *  - String version:               version of artifact
     *  - String classifier (optional): classifier of artifact
     * OUTPUT:
     *  - nothing
     * NOTE:
     *  - requires Nexus Artifact Uploader plugin in Jenkins
     */
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

    /**
     * download: Downloads artifact from Nexus
     * INPUT:
     *  - NexusComponent nc: Instantiated NexusComponent object to be downloaded and pass to this method.
     * OUTPUT:
     *  - String file: full path to downloaded artifact
     * NOTE:
     *  - Method is a wrapper that uses a NexusComponent object for calling the download
     *    method rather than passing all required string seperately.
     */
    def download(NexusComponent nc){
        def file = download(nc.repository, nc.groupId, nc.artifactId, nc.extension, nc.version)
        return file
    }

    /**
     * download: Downloads artifact from Nexus
     * INPUT:
     *  - String repository:            name of repository in Nexus
     *  - String groupId:               groupId of artifact to download
     *  - String artifactId:            artifactId of artifact to download
     *  - String extension:             extension of artifact to download
     *  - String version:               version of artifact to downlaod
     *  - String classifier (optional): classifier of artifact to download
     * OUTPUT:
     *  - String outPath: full path to downloaded artifact
     */
    def download(String repository, String groupId, String artifactId, String extension, String version, String classifier="") {
        String outPath = ""
        def errorMsg
        def result
        def artifact
        def downloadUrl

        steps.withCredentials (
            [steps.usernameColonPassword(credentialsId: credentials, variable: 'USERPASS')]
        ){
            try {
                def searchUrl = "${url}/service/rest/v1/search/assets/?repository=${repository}&group=${groupId}&maven.artifactId=${artifactId}&version=${version}&maven.extension=${extension}"
                def resultText = sh ([script: """curl -s -v -u\${USERPASS} -X GET --header 'Accept: application/json' '${searchUrl}'""", returnStdout: true]).trim()
            

                result = steps.readJSON text: resultText
                artifact = result['items'][0]
                downloadUrl = artifact['downloadUrl']
            } catch (NullPointerException npe) {
                errorMsg = "Failed to download artifact. Artifact not found in Nexus"
                steps.addErrorBadge text: errorMsg
                error errorMsg
            }
            def remoteSha = result['items']['checksum']['sha1'][0]
            outPath = "${steps.WORKSPACE}/${basename(artifact['path'])}"

            echo "downloading ${downloadUrl} to ${outPath}"
            sh """ curl -s -X GET -u\$USERPASS "${downloadUrl}" -o ${outPath}"""

            def localSha = steps.sha1 "${outPath}"
            try {
                assert remoteSha == localSha : errorMsg
                echo "Checksum match"
            } catch (AssertionError e) {
                errorMsg = "SHA1 checksum of downloaded is not matching"
                echo "ERROR: " + e.getMessage()
                steps.addErrorBadge text: errorMsg
                error errorMsg
            }
            // outPath = result
        }
        return outPath
    }

    def downloadGroovy(NexusComponent nc, String usernameColonPassword){
        def file = downloadGroovy(nc.repository, nc.groupId, nc.artifactId, nc.extension, nc.version, usernameColonPassword)
        return file
    }

    def downloadGroovy(String repository, String groupId, String artifactId, String extension, String version, String classifier="", usernameColonPassword) {
        String outPath = ""
        def errorMsg
        def result
        def artifact
        def downloadUrl

      //  steps.withCredentials (
       //     [steps.usernameColonPassword(credentialsId: credentials, variable: 'USERPASS')]
       // ){
            try {
                def searchUrl = "http://${url}/service/rest/v1/search/assets/?repository=${repository}&group=${groupId}&maven.artifactId=${artifactId}&version=${version}&maven.extension=${extension}"
                //def resultText = sh ([script: """curl -s -v -u\${USERPASS} -X GET --header 'Accept: application/json' '${searchUrl}'""", returnStdout: true]).trim()
                def resultText = getHttpRequestJson(searchUrl, usernameColonPassword)

                result = steps.readJSON text: resultText
                artifact = result['items'][0]
                downloadUrl = artifact['downloadUrl']
            } catch (NullPointerException npe) {
                errorMsg = "Failed to download artifact. Artifact not found in Nexus"
                steps.addErrorBadge text: errorMsg
                error errorMsg
            }
            def remoteSha = result['items']['checksum']['sha1'][0]
            outPath = "${steps.WORKSPACE}\\${basename(artifact['path'])}"

            echo "downloading ${downloadUrl} to ${outPath}"
            echo "${steps.NODE_NAME}"
            //getHttpRequestJson(downloadUrl)
            powershell ([script: """ Invoke-WebRequest -Uri ${downloadUrl} -OutFile  "${outPath}" """ ])
            //sh """ curl -s -X GET -u\$USERPASS "${downloadUrl}" -o ${outPath}"""

            def localSha = steps.sha1 "${outPath}"
            try {
                assert remoteSha == localSha : errorMsg
                echo "Checksum match"
            } catch (AssertionError e) {
                errorMsg = "SHA1 checksum of downloaded is not matching"
                echo "ERROR: " + e.getMessage()
                steps.addErrorBadge text: errorMsg
                error errorMsg
            }
       // }
        return outPath
    }

    /**
     * getAvailableVersions: retrieves a list of all versions in Nexus for a given artifact AND adds
     *   an entry with value "Latest" at index 0. This is done so "Latest" will be the default value
     *   when the list is used for a choice parameter (drop down) in Jenkins.
     * Nexus is searched with passed parameters and a list of all versions in descending order is
     *    returned.
     * INPUT:
     *  - String repository
     *  - String groupId
     *  - String artifactId
     * OUTPUT:
     *  - ArrayList versionList
     */
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

    /**
     * getAvailableVersions: retrieves a list of all versions in Nexus for a given artifact AND adds
     *   an entry with value "Latest" at index 0. This is done so "Latest" will be the default value
     *   when the list is used for a choice parameter (drop down) in Jenkins.
     * Nexus is searched with passed parameters and a list of all versions in descending order is
     *    returned.
     * INPUT:
     *  - NexusComponent nc: Instantiated NexusComponent object to be downloaded and pass to this method. 
     * OUTPUT:
     *  - ArrayList versionList
     * NOTE:
     *  - Method is a wrapper for getAvailableVersions method that takes all required parameters
     *    seperately.
     */
    def getAvailableVersions(NexusComponent nc) {
        return getAvailableVersions(nc.repository, nc.groupId, nc.artifactId)
    }
     /**
      * getLatestVersion: returns the latest version from versionList (called from getAvailableVersion)
      * INPUT:
      *  - String repository
      *  - String groupId
      *  - String artifactId
      * OUTPUT:
      *  - String version
      * NOTE:
      *  - 1st index of versionList is the latest
      */
    def getLatestVersion(String repository, String groupId, String artifactId) {
        return getAvailableVersions(repository, groupId, artifactId)[1]
    }

    /**
      * getLatestVersion: returns the latest version from versionList (called from getAvailableVersion)
      * INPUT:
      *  - NexusComponent nc
      * OUTPUT:
      *  - String version
      * NOTE:
      *  - 1st index of versionList is the latest
      */
    def getLatestVersion(NexusComponent nc) {
        return getAvailableVersions(nc)[1]
    }
}