#!/usr/bin/env groovy
package org.tibcocicd

// all classes in **/src/* need to be imported specifically and invoked:
// @Library('LIBNAME')
// import package.classname
// ...
// script {
// z = new package.classname()
// }
class GlobalVars {
	// Nexus Artifact Repository details
	static String nexusUrl = "127.0.0.1:8081"
	static String tibcoSnapshotRepo = "snapshot"
	static String tibcoGroupId = ""
	static String nexusVersion = "nexus3"
	static String tibcoReleaseRepo = "release-test"
	static ArrayList releaseTypes = ["major","minor","patch"]
	static String tibcoDeployApprovers = ""

	// Jenkins details
	static String jenkinsUrl = "127.0.0.1:8080"
	static String defaultPipelineConfigXml = "http://${gitlabUrl}/automation/raw/master/apiCalls/jenkins/addPipelineJob/pipelineConfig.xml"
	static String gitlabConnectionName = "gitlabConnLocal"

	// Git details
	static String gitlabUrl = "127.0.0.1:8082"
	static String rawGitHubHostname = "raw.githubusercontent.com"

	// Tibco installation details
	static String bwBin = "/opt/tibco/bw6/bw/6.4/bin"
	static String tibEmsAdminFolder = "/opt/tibco/bw6/ems/8.4/bin"

	// Tibco artifact build and deployment details
	static String sharedModulesRepository = "http://${gitlabUrl}/MyGroup/sharedmodules.git"
	static String tibcoArchiveExtension = "ear"
	static String emsServerUrl = "tcp://tibcoHost:7222"
	static String devSubstvar = "DEV.substvar"

	// Tibco agent URLs
	static String defaultBwAgent = "http://tiboHostDev:8079"
	static String bwAgent_DEV = "http://tibcoHostDev:8079"
	static String bwAgent_TEST = "http://tibcoHostTest:8079"
	static String bwAgent_UAT = "http://tibcoHostUAT:8079"
	static String bwAgent_PROD = "http://tibcoHostProd:8079"

	// Jenkins credentialIDs
	static String rootGitlabCID = "888ee2b7-05e5-486c-8c8c-78316e198659"
	static String adminNexusCID = "nexus" 
	static String jenkinsSystemUserCID = "1f91c74b-6322-4de2-9b70-6545c4de2c86"
	static String gitlabApiToken = "webhook-4-gitlab"

	// General
	static String date = new Date().format( 'yyyyMMdd-HHmmss' )
}
