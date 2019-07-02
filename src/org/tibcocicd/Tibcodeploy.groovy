package org.tibcocicd;

import groovy.transform.InheritConstructors
import org.tibcocicd.Tibco
import org.tibcocicd.Gitlab

@InheritConstructors
class Tibcodeploy extends Tibco {
    def bwProfile
    def bwAgentUrl
    def appName
    def bwAppSpace
    def bwDomain

    Tibcodeploy(steps, environment, appName) {
        super(steps, environment)
        this.appName = appName
        switch(environment){
            case "DEV":
                this.bwProfile = "DEV.substvar"
                this.bwAgentUrl = GlobalVars.bwAgent_DEV
                break
            case "TEST":
                this.bwProfile = "Test.substvar"
                this.bwAgentUrl = GlobalVars.bwAgent_TEST
                break
            case "UAT":
                this.bwProfile = "UAT.substvar"
                this.bwAgentUrl = GlobalVars.bwAgent_UAT
                break
            case "Prod":
                this.bwProfile = "Prod.substvar"
                this.bwAgentUrl = GlobalVars.bwAgent_PROD
                break
            default:
                this.bwProfile = "DEV.substvar"
                this.bwAgentUrl = GlobalVars.bwAgent_DEV
                break
        }
    }

    def setbwAgentUrl(String url){
        this.bwAgentUrl = url
    } 

    def setbwProfile(String profile){
        this.bwProfile = profile
    }

    def setenvironment(String environment){
        this.environment = environment
    }

    def setVars(bwDomain = "", bwAppSpace = ""){
        if (bwDomain == "") {
        this.bwDomain = getDomain()
        this.bwAppSpace = getAppSpace()
        }
        else {
            this.bwDomain = bwDomain
            this.bwAppSpace = bwAppSpace
        }
    }

    def echoVars(){
        echo "Profile = " + bwProfile
        echo "Url = " + bwAgentUrl
        echo "Domain = " + bwDomain
        echo "AppSpace = " + bwAppSpace
    }
    
    def getDomain(){
        echo "getting domain for "+"${appName}"
        def gitlab = new Gitlab(this, "webhook-4-gitlab")
        switch(gitlab.getsubgroup(steps.GIT_URL)){
            case "moka":
                return "mka_${environment}"
                break
            case "eloket":
                return "elk_${environment}"
                break
            case "einvoicing":
                return "eInv_${environment}"
                break
            case "gis":
                return "gis_${environment}"
                break
            case "vesta":
                return "vst_${environment}"
                break
            case "eurides":
                return "eur_${environment}"
                break
            case "common":
                return "common_${environment}"
                break
            case "poc":
                return "poc_${environment}"
                break
            case "porteau":
                return "pto_${environment}"
                break
            case "mig":
                return "mig_${environment}"
                break
            case "dkp":
                return "dkp_${environment}"
                break
            default:
                error "Domain not found, check in which domain this app is supposed to go, and if it already exists"
        // steps.withEnv(["bwAgentUrl=${bwAgentUrl}"]){
        // def resultText = sh ([script: """ curl -s -v -X GET "${bwAgentUrl}/bw/v1/browse/domains?full=false&status=false" """, returnStdout: true]).trim()
        // def result = steps.readJSON text: resultText
        // def names = []
        // result.each {
        //     boolean b = it.appspaceRefs ==~ ("^(.*)(/${appName})(.*\$)")                    //     if (bollie){
        //     if (b){
        //     names += it.name
        //     }
        // }
        // if (names[0] == null) {
        //     error "Domain not found, check if the application is deployed in an Appspace and if the name of the Appspace starts with ${appName}"
        // }
        // else {
        //     return names[0]
        // }
        // }
        }
    }
    def createDomain(String name){
        steps.withEnv(["bwAgentUrl=${bwAgentUrl}", "name=${name}"]){
        sh '''
        echo "Creating Domain..."
        curl -XPOST "${bwAgentUrl}/bw/v1/domains/${name}"

        '''
        }
    }

    def getAppSpace() {
        echo "Getting AppSpace for " + "${appName}"
        steps.withEnv(["bwAgentUrl=${bwAgentUrl}", "bwDomain=${bwDomain}"]){
        def resultText = sh ([script: """ curl -s -v -X GET '${bwAgentUrl}/bw/v1/browse/appspaces?domain=${bwDomain}&full=false&status=false'""", returnStdout: true]).trim()
        def result = steps.readJSON text: resultText
        def names = []
        result.each {
            boolean b = it.applicationRefs ==~ ("^(.*)(${appName})(.*\$)") 
            boolean c = it.name ==~ ("^(.*)(${appName})(.*\$)") 
            if (b) {
                names += it.name
            }
            else if(c){
                names += it.name
            }
        }
        if (names[0] == null) {
            error "Appspace not found, check if the application has an existing Appspace and if the name starts with ${appName}"
        }
        else {
            return names[0]
        }
        }
    }

    def createAppSpace(String name){
        steps.withEnv(["bwAgentUrl=${bwAgentUrl}", "bwDomain=${bwDomain}", "name=${name}"]){
        sh '''
        echo "Creating AppSpace..."
        curl -XPOST "${bwAgentUrl}/bw/v1/domains/${bwDomain}/appspaces/${name}?elastic=true&minNodes=1"

        '''
        }
    }

    def startAppSpace(){
        steps.withEnv(["bwAgentUrl=${bwAgentUrl}", "bwDomain=${bwDomain}", "bwAppSpace=${bwAppSpace}"]){
        sh '''
        echo "Starting AppSpace..."
        curl -XPOST "${bwAgentUrl}/bw/v1/domains/${bwDomain}/appspaces/${bwAppSpace}/start"

        '''
        }
    }
    def stopAppSpace(){
        steps.withEnv(["bwAgentUrl=${bwAgentUrl}", "bwDomain=${bwDomain}", "bwAppSpace=${bwAppSpace}"]){
        sh '''
        echo "Stopping AppSpace..."
        curl -XPOST "${bwAgentUrl}/bw/v1/domains/${bwDomain}/appspaces/${bwAppSpace}/stop"

        '''
        }
    }

    def uploadApp(String FILE) {
        steps.withEnv(["FILE=${FILE}","bwAgentUrl=${bwAgentUrl}", "bwDomain=${bwDomain}"]){
            sh '''
                curl -XPOST -F "file=@${FILE}" "${bwAgentUrl}/bw/v1/domains/${bwDomain}/archives"
            '''
        }
    }

    def undeployApp( int version) {
        steps.withEnv(["appName=${appName}","bwAgentUrl=${bwAgentUrl}", "bwDomain=${bwDomain}", "bwAppSpace=${bwAppSpace}", "version=${version}"]){
        sh '''
            curl -XDELETE  "${bwAgentUrl}/bw/v1/domains/${bwDomain}/appspaces/${bwAppSpace}/applications/${appName}/{version}"
        '''
        }
    }
    def deployApp(String bwName) {
        steps.withEnv(["bwName=${bwName}","bwAgentUrl=${bwAgentUrl}", "bwDomain=${bwDomain}", "bwAppSpace=${bwAppSpace}","PROFILE=${bwProfile}"]){
        sh '''
            curl --write-out '%{http_code}' --silent --output /dev/null -XPOST "${bwAgentUrl}/bw/v1/domains/${bwDomain}/appspaces/${bwAppSpace}/applications?archivename=${bwName}&startondeploy=true&replace=true&profile=${PROFILE}"
        '''
        }
    }
    def deleteApp(String bwName) {
        steps.withEnv(["bwName=${bwName}","bwAgentUrl=${bwAgentUrl}", "bwDomain=${bwDomain}", "bwAppSpace=${bwAppSpace}","PROFILE=${bwProfile}"]){
        sh '''
            curl -XDELETE "${bwAgentUrl}/bw/v1/domains/${bwDomain}/archives/${bwName}"
        '''
        }
    }
}