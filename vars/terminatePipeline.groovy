
// var/terminatePipeline.groovy

/*
 * usage in Jenkinsfile, add step:  
 * `inputStep("lets go!", "yesyes", "admin")`
 */
def call(String status, String msg) {
    node {
        echo "oi"
        switch(status) {
            case "FAILURE":
                currentBuild.result == status
                throw new RuntimeException(msg)
                break
            case "SUCCESS":
                currentBuild.result == status
                // sh "exit"
                break;
            default:
                echo "do no thang"
                break
        }
    }
}
