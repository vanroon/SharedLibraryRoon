package org.vanroon;

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

    /**
     * Return userId of user who started the buid in Jenkins
     * @return
     */
    @NonCPS
    def getBuildUser() {
        return steps.currentBuild.rawBuild.getCause(Cause.UserIdCause).getUserId()
    }

    def encodeBase64(String string){
        return string.bytes.encodeBase64().toString()
    }

    def getHttpRequest(String url, String credentials="") {
        // return url
        def conn = new URL(url).openConnection() as HttpURLConnection
        if (credentials.length() > 0){
            def auth = encodeBase64(credentials)
            conn.setRequestProperty("Authorization", "Basic ${auth}")
        }
        if (conn.responseCode == 200){
            return conn.getInputStream().getText()
        }
       
        // def getRC = get.getResponseCode()
        // //return getRC
        // //println(getRC);
        // if(getRC.equals(200)) {
        //     return get.getInputStream().getText()
        // }
    }

    def postHttpRequest(String url) {
        

    }
}