#!/usr/bin/env groovy
package org.tibcocicd;

import groovy.transform.InheritConstructors
import org.tibcocicd.Tibco

/**
 * Class Tibco acts as a collection for all operations regarding a tibco instance and 
 * tibco artifacts.
 */

@InheritConstructors
class Tibco extends PipelineBuilder {
    def environment

    Tibco(steps, environment) {
        super(steps)
        this.environment = environment
    }
    
    /**
     * createAppWorkspace: creates an appWorkspace based on Tibco source code using bwdesign
     * INPUT:
     *  - nothing
     * OUTPUT:
     *  - nothing
     * NOTE:
     *  - command xvfb-run is used to simulate a grahpical environment
     */
    def createAppWorkspace() {
        sh '''
            echo ${WORKSPACE}
#           //Create workspace dir inside jenkins'home file
            mkdir ${WORKSPACE}/workspace 	
            echo $appName	
#           //Set variables for application source and shared modules source
            appSourceDir=${WORKSPACE}/application_source/src/bw
        
            sharedModulesSourceDir=${WORKSPACE}/shared_modules_source/src/bw
#           // Check shared modules folder. If it is not empty, them copy the folder into application source folder
            if [ "$(ls ${sharedModulesSourceDir} | wc -l ) -gt 0" ] ; 
                then export appSourceDir="${appSourceDir}, ${sharedModulesSourceDir}"
            else 
                echo " sharedmodules directory is Empty" 
                export appSourceDir="${appSourceDir},${sharedModulesSourceDir}"
                echo "but also: combined appsharedDir= ${addSourceDir}"
            fi
            bwDesignExecutable=${bwBin}/bwdesign
#           import tibco projects from application source folder into workspace folder in jenkin's home directory
            bwDesignCommand="${bwDesignExecutable} --propFile ${bwBin}/bwdesign.tra -data ${WORKSPACE}/workspace system:import -f $appSourceDir"
            xvfb-run -a ${bwDesignCommand}
        '''
    }

    /**
     * upload: uploads Tibco application to TEA server
     * INPUT:
     *  - String FILE:          full path of file to upload including filename
     *  - String bwAgentUrl:    URL of target agent
     *  - String bwDomain:      Name of Tibco domain to deploy application in
     * OUTPUT:
     *  - nothing
     * TODO: errorhandling
     */
    def upload(String FILE, String bwAgentUrl, String bwDomain) {
        steps.withEnv(["FILE=${FILE}","bwAgentUrl=${bwAgentUrl}", "bwDomain=${bwDomain}"]){
            sh '''
                curl -XPOST -F "file=@${FILE}" "${bwAgentUrl}/bw/v1/domains/${bwDomain}/archives"
            '''
        }
    }

    /**
     * undeploy: undeploys a Tibco application
     * INPUT:
     *  - String bwAgentUrl:    URL of agent where application is running
     *  - String bwDomain:      name of domain where application is running in
     *  - String bwAppSpace:    name of appSpace of application
     *  - String earFile
     *  - String version        version of application
     * OUTPUT:
     *  - Nothing
     */
    def undeploy(String bwAgentUrl, String bwDomain, String bwAppSpace, String earFile, int version) {
        sh '''
            curl -XDELETE  "${bwAgentUrl}/bw/v1/domains/${bwDomain}/appspaces/${bwAppSpace}/applications/${earFile}/{version}"
        '''

    }

    /**
     * deploy: deploys a Tibco application on a TEA server
     * INPUT:
     *  - String bwAgentUrl
     *  - String bwDomain
     *  - String bwAppSpace
     *  - String earFile
     *  - String PROFILE
     * OUTPUT:
     *  - Nothing
     */
    def deploy(String bwAgentUrl, String bwDomain, String bwAppSpace, String earFile, String PROFILE) {
        steps.withEnv(["earFile=${basename(earFile)}","bwAgentUrl=${bwAgentUrl}", "bwDomain=${bwDomain}", "bwAppSpace=${bwAppSpace}","PROFILE=${PROFILE}"]){
        sh '''
            curl --write-out '%{http_code}' --silent --output /dev/null -XPOST "${bwAgentUrl}/bw/v1/domains/${bwDomain}/appspaces/${bwAppSpace}/applications?archivename=${earFile}&startondeploy=true&replace=true&profile=${PROFILE}"
        '''
        }
    }

    /**
     * delete: Deletes a Tibco application from a TEA server
     * INPUT:
     *  - String bwAgentUrl
     *  - String bwDomain
     *  - String earFile
     * OUTPUT:
     *  - Nothing
     */
    def delete(String bwAgentUrl, String bwDomain, String earFile) {
        sh '''
            curl -XDELETE "${bwAgentUrl}/bw/v1/domains/${bwDomain}/archives/${earFile}"
        '''
    }

    /**
     * buildAppArtifact: Builds an application using bwdesign
     * INPUT:
     *  - Nothing
     * OUTPUT:
     *  - Nothing
     */
    def buildAppArtifact() {
        sh '''
        #   //Get appVersion from MANIFEST.MF file which is located in a folder where TIBCO.xml is located.												
            mkdir -p ${WORKSPACE}/exports/${appName}/${appVersion}
            bwDesignExecutable=${bwBin}/bwdesign
            bwDesignCommand="${bwDesignExecutable} --propFile ${bwBin}/bwdesign.tra -data ${WORKSPACE}/workspace system:export -e ${appName} ${WORKSPACE}/exports/${appName}/${appVersion}"
            echo "bwDesignCommand = ${bwDesignCommand}"
            xvfb-run ${bwDesignCommand} | tee ${WORKSPACE}/build.out
        #    TODO: error parsing
        #    //If error or exception occurs this will show up in build.out folder
        #    if [ "$(cat ${WORKSPACE}/workspace/build.out | grep exception)" ] ; 
        #        then echo " exception occured, check build.out file for details"; fi
        #    if [ "$(cat ${WORKSPACE}/workspace/build.out | grep error)" ] ; 
        #        then echo " error occured, check build.out file for details"; fi

            SourceDirectory=${WORKSPACE}/application_source/resources/deploy/envSettings
            DestinationDirectory=${WORKSPACE}/exports/${appName}/${appVersion}

            for substvar in "${WORKSPACE}"/application_source/resources/deploy/envSettings/*.substvar
            do 
                cp "${substvar}" "${WORKSPACE}/exports/${appName}/${appVersion}/."
            done
        '''
    }

    //def createEmsQueues(String tibEmsAdminFolder, String emsServerUrl, String emsServerUsername, String emsServerPassword) {
//        def createEmsQueues(String tibEmsAdminFolder, String emsServerUrl) {
    /**
     * createEmsQueues: Creates EMS queues based on content of scripts/ems directory
     * INPUT:
     *  - Nothing
     * OUTPUT:
     *  - Nothing
     */
    def createEmsQueues() {
        // steps.withEnv(["tibEmsAdminFolder=${tibEmsAdminFolder}", "emsServerUrl=${emsServerUrl}"]){
        sh '''
            emsQueueDir="application_source/scripts/ems"
            # Check if file exists
            if [ "$(find ${emsQueueDir} -iname '*.txt' 2> /dev/null | wc -l)" -gt 0  ]; then
                echo "=============== EMS Script exists ==========="
                createQueuesFileName=$(find ${emsQueueDir} -iname '*.txt')
                createQueuesFileContent=$(cat "${createQueuesFileName}")
                                            
                # create file "ShowQueue.txt"
                showQueueFile="${WORKSPACE}/ShowQueue.txt"
                sed s/create/show/g "${createQueuesFileName}" >  "${showQueueFile}"
            
                # build ShowQueue cmd
                tibEmsAdminFolder="/opt/tibco/bw6/ems/8.4/bin"
                emsServerUrl="tcp://t-vmwltibco03.vmwitest.be:7222"
                ShowQueueCmd="${tibEmsAdminFolder}/tibemsadmin -server ${emsServerUrl} -user ${emsServerUsername} -password ${emsServerPassword} -script ${showQueueFile}"

                # invoke ShowQueue cmd
                ${ShowQueueCmd} | tee "${WORKSPACE}/build.out"
                        
                # Check for errors
                if [ $(cat "${WORKSPACE}/build.out" | grep -e "[a-zA-Z]*error[a-zA-Z]*" | wc -l ) -gt 0 ]; then
                    echo "An error has occured while showing queue" 
                fi
                # print output of showqueue command
                cat "${WORKSPACE}/build.out"

                # Check for 'not found' errors
                notFoundCount=$(cat "${WORKSPACE}/build.out" | grep "not foud" | wc -l)
                if [ "${notFoundCount}" -gt 0 ]; then
                    # Create CreateQueue.txt file and add content
                    createQueueFile="${WORKSPACE}/CreateQueue.txt"
                    cp "${createQueuesFileName}" "${createQueueFile}" && echo "commit" >> "${createQueueFile}" 
                                
                    # Append commit again
                    echo "commit" >> "${createQueueFile}"	
                    
                    # Create Queue command
                    CreateQueueCmd="${tibEmsAdminFolder}/tibemsadmin -server ${emsServerUrl} -user ${emsServerUsername} -password ${emsServerPassword} -script ${createQueueFile}"
                    
                    # Run command and write output to create.out
                    ${CreateQueueCmd} | tee "${WORKSPACE}/create.out"
                    
                    # Check for errors in create.out
                    if [ $(cat "${WORKSPACE}/create.out" | grep -e "[a-zA-Z]*error[a-zA-Z]") -gt 0 ]; then
                        echo "An error has occured while creating queue" 
                    fi
                fi
            fi
        '''
        // }`
    }

    /**
     * getDevDomain: returns domain based on name of subgroup in Gitlab
     * INPUT:
     *  - String subgroup
     * OUTPUT:
     *  - String domain
     */
    def getDevDomain(String subgroup) {
        def domain
        switch(subgroup){
            case "eurides":
                domain     = "eur_DEV"
                break
            case "eloket":
                domain = "elk_DEV"
                break
            case "moka":
                domain = "mka_DEV"
                break
            case "vesta":
                domain = "vst_DEV"
                break
            default:
                domain = ""
                break
        }
        return domain
    }

    /**
     * getDomain: returns list of domain names from Tibco TEA server
     * INPUT:
     *  - String bwAgentUrl
     * OUTPUT:
     *  - ArrayList names
     */
    def getDomain(String bwAgentUrl){
     //   steps.withEnv(["bwAgentUrl=${bwAgentUrl}"]){
        println "Getting Domains..."
        def resultText = sh ([script: """ curl -s -v -u\${USERPASS} -X GET "${bwAgentUrl}/bw/v1/browse/domains?full=false&status=false" """, returnStdout: true]).trim()
        def result = steps.readJSON text: resultText
        def names = []
        result.each {
            names += it.name
        }
        return names
      //  }
    }

    /**
     * createDomain: creates a domain on a Tibco TEA server
     * INPUT:
     *  - String bwAgentUrl
     *  - String name
     * OUTPUT:
     *  - Nothing
     */
    def createDomain(String bwAgentUrl, String name){
        steps.withEnv(["bwAgentUrl=${bwAgentUrl}"]){
        sh '''
            echo "Creating Domain..."
            curl -XPOST "${bwAgentUrl}/bw/v1/domains/${name}"
        '''
        }
    }

    /**
     * getAppSpace: returns list of AppSpaces for an application
     * INPUT:
     *  - String bwAgentUrl
     *  - String bwDomain
     *  - String appName
     * OUTPUT:
     *  - ArrayList names:  contains appSpaces for application ${appName}
     */
    def getAppspace(String bwAgentUrl,String bwDomain, String appName) {
        steps.withEnv(["bwAgentUrl=${bwAgentUrl}", "bwDomain=${bwDomain}", "appName=${appName}"]){
        println "Getting Appspace..."
        def resultText = sh ([script: """ curl -s -v -u\${USERPASS} -X GET '${bwAgentUrl}/bw/v1/browse/appspaces?domain=${bwDomain}&full=false&status=false'""", returnStdout: true]).trim()
        def result = steps.readJSON text: resultText
        def names = []
        result.each {
            boolean b = it.applicationRefs ==~ ("^(.*)(/${appName})(.*\$)")
            if (b) {
                names += it.name
            }
        }
        return names
        }
    }

    /**
     * createAppSpace: creates an appSpace on a Tibco TEA server
     * INPUT:
     *  - String bwAgentUrl
     *  - String bwDomain
     *  - String name
     * OUTPUT:
     *  - Nothing
     */
    def createAppspace(String bwAgentUrl, String bwDomain, String name){
        steps.withEnv(["bwAgentUrl=${bwAgentUrl}", "bwDomain=${bwDomain}"]){
        sh '''
            echo "Creating Appspace..."
            curl -XPOST "${bwAgentUrl}/bw/v1/domains/${bwDomain}/appspaces/${name}?elastic=true&minNodes=1"
        '''
        }
    }

    /**
     * startAppspace: starts an appSpace on a Tibco TEA server
     * INPUT:
     *  - String bwAgentUrl
     *  - String bwDomain
     *  - String name
     * OUTPUT:
     *  - Nothing
     */
    def startAppspace(String bwAgentUrl, String bwDomain, String name){
        steps.withEnv(["bwAgentUrl=${bwAgentUrl}", "bwDomain=${bwDomain}"]){
        sh '''
            echo "Starting Appspace..."
            curl -XPOST "${bwAgentUrl}/bw/v1/domains/${bwDomain}/appspaces/${name}/start"   
        '''
        }
    }

    /**
     * stopAppspace: stops an appSpaces
     * INPUT:
     *  - String bwAgentUrl
     *  - String bwDomain
     *  - String name
     * OUTPUT:
     *  - Nothing
     */    
    def stopAppspace(String bwAgentUrl, String bwDomain, String name){
        steps.withEnv(["bwAgentUrl=${bwAgentUrl}", "bwDomain=${bwDomain}"]){
        sh '''
            echo "Stopping Appspace..."
            curl -XPOST "${bwAgentUrl}/bw/v1/domains/${bwDomain}/appspaces/${name}/stop"
        '''
        }
    }
}