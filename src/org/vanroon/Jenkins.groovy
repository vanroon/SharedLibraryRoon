#!/usr/bin/env groovy
package org.vanroon;

import groovy.transform.InheritConstructors

@InheritConstructors
class Jenkins extends PipelineBuilder {

    def url
    def credentials

    Jenkins(steps, url, credentials){
        super(steps)
        this.url = url
        this.credentials = credentials
    }

    def getCrumb(userColonPass=""){
        // steps.withCredentials (
        //     [steps.usernameColonPassword(credentialsId: credentials, variable: 'CREDENTIALS')]
        // ) {
            //String creds = CREDENTIALS
            def resultText = getHttpRequestJson(url + "/crumbIssuer/api/json", userColonPass)
            def result = steps.readJSON text: resultText
            //def crumb = result['crumb']
            //return crumb
            return result['crumb']

        //}
    }

    def getOldWay(){
        steps.withCredentials (
            [steps.usernameColonPassword(credentialsId: credentials, variable: 'USERPASS')]
        ){
            def searchUrl = url + "/crumbIssuer/api/json"
            def resultText = sh ([script: """ curl -s -v -u\${USERPASS} -X GET '${searchUrl}'""", returnStdout: true]).trim()
            def result = steps.readJSON text: resultText
            return result['crumb']
        }
    }

    def getCrumb2(userColonPass="") {
        // steps.withCredentials (
        //     [steps.usernameColonPassword(credentialsId: credentials, variable: 'creds')]
        // ){
            // return url
            // jEcho url
            // jEcho credentials.length().toString()
            // jEcho "${env.creds}"
            def conn = new URL(url).openConnection() as HttpURLConnection
            if (userColonPass.length() > 0){
                def auth = encodeBase64(userColonPass)
                conn.setRequestProperty("Authorization", "Basic ${auth}")
            }
            if (conn.responseCode == 200){
                // def json = conn.inputStream.withCloseable { inStream ->
                //     new JsonSlurper().parse( inStream as InputStream )
                // }
                // return json
                def resultText = conn.getInputStream().getText()
            //def result = steps.readJSON text: resultText
                return resultText
            /// return result['crumb']
            }
        
            // def getRC = get.getResponseCode()
            // //return getRC
            // //println(getRC);
            // if(getRC.equals(200)) {
            //     return get.getInputStream().getText()
            // }
       // }
    }

}