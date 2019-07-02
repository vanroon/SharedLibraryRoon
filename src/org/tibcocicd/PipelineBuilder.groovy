package org.tibcocicd;

/**
 * PipelineBuilder is a class that defines basic pipeline operations as 
 * groovy methods, so they can easily be used inside classes that implement this 
 * class as a parent. It has to implement 'Serializable' in order to make use of
 * Jenkins' pipeline functions, steps and variables.
 */

class PipelineBuilder implements Serializable {
    protected def steps

    PipelineBuilder(steps){
        assert steps != null
        this.steps = steps
    }

    def basename(filename){
        filename.split("/")[-1]
    }

    def sh(command) {
        steps.sh command
    }

    def echo(string){
        steps.echo string
    }

    def error(msg){
        steps.error msg
    }

     def getBuildUser() {
        return steps.currentBuild.rawBuild.getCause(Cause.UserIdCause).getUserId()
    }

    /**
     * returns a encoded base64 string, based on input string
     */
    def encodeBase64(String string){
        return string.bytes.encodeBase64().toString()
    }

    def getHttpRequestJson(String url, String credentials="") {
        echo "URL = ${url}"
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
        echo "URL = ${url}"
        def conn = new URL(url).openConnection() as HttpURLConnection
        conn.setRequestMethod("POST")   
        if (jenkinsCrumb.length() > 0){
            echo "Parsing crumb: ${jenkinsCrumb}"
            conn.setRequestProperty("Jenkins-Crumb", jenkinsCrumb)
        }
        if (auth.length() > 0){
            echo "parsing auth: ${auth}"
            conn.setRequestProperty("Authorization", "Basic ${auth}")
        }
        if (body.length() > 0){
            conn.setRequestProperty("Content-Type", "text/xml")
            echo "parsing body: ${body}"
            conn.setDoOutput(true)
            def OutputStream output = conn.getOutputStream()
            byte[] input = body.getBytes('utf-8')
            output.write(input, 0, input.length)
        }
        return conn.responseCode
    }

    def postHttpRequestNoBody(String url, Map headers){
        echo "URL = ${url}"
        def conn = new URL(url).openConnection() as HttpURLConnection
        conn.setRequestMethod("POST")
        if (headers.size() > 0) {
            headers.each { k, v ->
                conn.setRequestProperty(k, v)
            }
        }
        return conn.responseCode
    }
}