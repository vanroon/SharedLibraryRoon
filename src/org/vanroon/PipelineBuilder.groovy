package org.vanroon;

class PipelineBuilder implements Serializable {
    protected def steps

    PipelineBuilder(steps, currentBuild){
        assert steps != null
        assert currentBuild != null
        this.steps = steps
        this.currentBuild = currentBuild
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

    @NonCPS
    def getBuildUser() {
        return currentBuild.rawBuild.getCause(Cause.UserIdCause).getUserId()
    }

}