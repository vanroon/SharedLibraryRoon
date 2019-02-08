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
    @NonCPS
    def getBuildUser() {
        return steps.currentBuild.rawBuild.getCause(Cause.UserIdCause).getUserId()
    }

}