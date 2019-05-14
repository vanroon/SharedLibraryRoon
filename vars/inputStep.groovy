#!/usr/bin/env groovy
// var/inputStep.groovy

/*
 * usage in Jenkinsfile, add step:  
 * `inputStep("lets go!", "yesyes", "admin")`
 */
def call(String msg, String okButton="Continue", String approver) {
	input message: "${msg}", ok: "${okButton}", submitter: "${approver}"
}
