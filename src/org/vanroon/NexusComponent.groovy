#!/usr/bin/env groovy
package org.vanroon

class NexusComponent extends PipelineBuilder {
    def repository
    def groupId
    def artifactId
    def extension
    def version
    
    NexusComponent(steps, repository, groupId, artifactId){
        super(steps)
        this.repository = repository
        this.groupId = groupId.toLowerCase()
        this.artifactId = artifactId.toLowerCase()
        this.extension = "ear"
    }
    
    def setRepo(String repo){
        this.repository = repository
    }

    def setGroupID(String groupId){
        this.groupId = groupId
    }

    def setArtifactId(String artifactId){
        this.artifactId = artifactId
    }

    def setExtension(String extension){
        this.extension = extension
    }

    def setVersion(String version){
        this.version = version
    }

    def parseBundleVersionToMap(String bundleVersion){
        def versionDescList = GlobalVars.releaseTypes
        def versionNumList = bundleVersion.tokenize(".")
        def releaseVersionMap = [versionDescList, versionNumList].transpose().inject([:]) {
            desc, num -> desc[num[0]] = num[1];desc
        }
        return releaseVersionMap
    }

    def parseMapToBundleVersion(Map rvm){
        def bv = ""
        def i = 0
        rvm.each { a, b ->
            bv += b
            if (i < 3) {
                bv += "."
                i ++
            }
        }
        return bv + "qualifier"
    }

    def incrementBundleVersion(Map rvm, String releaseType){
        rvm[releaseType] = (rvm[releaseType].toInteger() + 1 ).toString()
        return rvm
    }

    def dumpVars(){
        echo "Repository: " + repository
        echo "groupId: " + groupId
        echo "artifactId: " + artifactId
        echo "version: " + version
        echo "extension: " + extension
    }
}
