package org.vanroon;

import groovy.json.JsonSlurper

class PipelineBuilder implements Serializable {
    protected def steps

    PipelineBuilder(steps){
        assert steps != null
        this.steps = steps
    }

    /**
     * jEcho - added 'j' to show difference between this cmd (echo to jenkins console output)
     * and 'Groovy' echo (that output won't show up in Jenkins console output)
     * @param string
     * @return
     */
    def jEcho(string) {
        steps.echo string
    }

    def findUser(){
        return steps.env.BUILD_USER_ID
    }

    def getCurrentBuild() {

    }

    def sh(command){
        steps.sh command
    }

    /**
     * Return userId of user who started the buid in Jenkins
     * @return
     */
    //@NonCPS
    def getBuildUser() {
        return steps.currentBuild.rawBuild.getCause(Cause.UserIdCause).getUserId()
    }

    def encodeBase64(String string){
        return string.bytes.encodeBase64().toString()
    }

    def getHttpRequestJson(String url, String credentials="") {
        jEcho "URL = ${url}"
        def conn = new URL(url).openConnection() as HttpURLConnection
        conn.setRequestMethod("GET")
        if (credentials.length() > 0){
            def auth = encodeBase64(credentials)
            conn.setRequestProperty("Authorization", "Basic ${auth}")
        }
        if (conn.responseCode == 200){
           def resultText = conn.getInputStream().getText()
           return resultText
        }
    }

    def postHttpRequestWithXMLBody(String url, String jenkinsCrumb="", String auth="", String body="") {
        jEcho "URL = ${url}"
        def conn = new URL(url).openConnection() as HttpURLConnection
        conn.setRequestMethod("POST")   
        if (jenkinsCrumb.length() > 0){
            jEcho "Parsing crumb: ${jenkinsCrumb}"
            conn.setRequestProperty("Jenkins-Crumb", jenkinsCrumb)
        }
        if (auth.length() > 0){
            jEcho "parsing auth: ${auth}"
            conn.setRequestProperty("Authorization", "Basic ${auth}")
        }
        if (body.length() > 0){
            conn.setRequestProperty("Content-Type", "text/xml")
            jEcho "parsing body: ${body}"
            conn.setDoOutput(true)
            def OutputStream output = conn.getOutputStream()
            byte[] input = body.getBytes('utf-8')
            output.write(input, 0, input.length)
        }
        return conn.responseCode
    }
}