#!/usr/bin/env groovy
// var/inputStep.groovy

import org.vanroon.Nexus

/*
 * usage in Jenkinsfile, add step:  
 * `inputStep("lets go!", "yesyes", "admin")`
 */
def call() {
	def d = new Dummy(this, "Erik")
    return d.name
}
