#!/usr/bin/env groovy
// vars/getGitlabProjectId.groovy

def call(String repositoryUrl, String secretToken){
	println "Getting ID for Repository URL: " + repositoryUrl
	resultText = sh ([script: """ curl -s -v --header 'PRIVATE-TOKEN: ${secretToken}' -X GET '${repositoryUrl}'""", returnStdout: true]).trim()
    result = readJSON text: resultText
    return result['id'][0]
}
