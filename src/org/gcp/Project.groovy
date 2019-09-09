#!/usr/bin/env groovy
package org.gcp;

class Project {

	private def projectName

	Project(projectName) {
		this.projectName = projectName
	}

	def getProjectName(){
		return projectName
	}
}
