#!/usr/bin/env groovy
// vars/buildImage.groovy

/**
 * buildImage: builds image
 * INPUT:
 *  - String imageTag
 * OUTPUT:
 *  - 
 */
def call(String imageTag) {
    //def imageHash = steps.sh ([script: """docker build -t ${imageTag}""", returnStdout: true]).trim()
	println "Tag: ${imageTag}"

}
