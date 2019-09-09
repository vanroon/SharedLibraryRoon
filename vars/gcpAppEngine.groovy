#!/usr/bin/env groovy
// vars/gcpAppEngine.groovy

/**
 * gcpAppEngine: interacts with GCP App Engine
 */

def deploy(String serviceAccountKeyFile, String projectName) {
	println "serviceaAccountKeyFile = ${serviceAccountKeyFile}"
	println "projectName = ${projectName}"
//	gcloud auth activate-service-account --key-file ${serviceAccountKeyFile}
//	gcloud config set project ${projectName}
//	gcloud --quiet --verbosity=error app deploy
}
