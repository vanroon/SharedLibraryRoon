#!/usr/bin/env groovy
package com.company

// all classes in **/src/* need to be imported specifically and invoked:
// @Library('LIBNAME')
// import package.classname
// ...
// script {
// z = new package.classname()
// }
class GlobalVars {
	// Nexus Artifact Repository details

	// Jenkins details
	static String jenkinsUrl = "127.0.0.1:8080"

	// Git details

	// Jenkins credentialIDs

	// General
	static String date = new Date().format( 'yyyyMMdd-HHmmss' )
}
