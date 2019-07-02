#!/usr/bin/env groovy
package org.tibcocicd;

/**
 * Class NexusComponent represents a Nexus component and acts as a collection for all
 * information used to identify exactly one component.
 */

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

    /**
     * parseBundleVersionToMap: Takes in a Tibco Bundle-Version string and returns the values
     *   in a map.
     * INPUT:
     *  - String bundleVersion:     E.g.: "1.0.0.qualifier"
     * OUTPUT:
     *  - Map releaseVersionMap
     * NOTE:
     *  - Takes two ArrayLists. one with keys (versionDescList) and one with 
     *    values (versionNumList). Returned HashMap is a result of both list combined. 
     */
    def parseBundleVersionToMap(String bundleVersion){
        def versionDescList = GlobalVars.releaseTypes
        def versionNumList = bundleVersion.tokenize(".")
        def releaseVersionMap = [versionDescList, versionNumList].transpose().inject([:]) {
            desc, num -> desc[num[0]] = num[1];desc
        }
        return releaseVersionMap
    }

    /**
     * parseMapToBundleVersion: converts a releaseVersionMap to a usable Tibco "Bundle-Version"
     * INPUT:
     *  - Map rvm
     * OUTPUT:
     *  - String bundleVersion
     */
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

    /**
     * incrementBundleVersion: increments the "Bundle-Version" with 1, depending on the 
     *   type of release ("major", "minor" or "patch").
     * INPUT:
     *  - Map rvm
     *  - String releaseType
     * OUTPUT:
     *  - Map rvm
     * NOTE:
     *  - releaseTypes are defined in a list in GlobalVars.groovy
     */
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
