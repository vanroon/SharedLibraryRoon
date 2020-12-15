import org.svb.GlobalVariables
import org.svb.PipelineBuilder

def call(){
    pipeline {
    	agent any
	    parameters {
		    choice (
			    name: 'fortify',
			    choices: ['1', '0']
		    )
		    choice (
			    name: 'sonar',
			    choices: ['1', '0']
		    )
	    }
	    options {
    		timeout(time: 20, unit: 'MINUTES') 
	    }
	    stages {
    		stage ('fortify'){
			    steps {
				    runSonarTest(params.fortify)
			    }
    		}
    		stage ('sonar'){
			    steps {
				    runSonarTest(params.sonar)
			    }
            }
    	//    stage ('build'){
    	//        steps {
    	//			buildOCPImage(
    	//					BUILD_PROJECT_NAME,
    	//					APP_NAME,
    	//					BUILDER_IMAGE_TYPE
    	//			)
    	//		}
    	//	}
    	//	stage('deploy'){
    	//		steps {
    	//			deployOCPImage(
    	//					BUILD_PROJECT_NAME,
    	//					APP_NAME,
    	//					TMOS_APPLICATION_ROUTE
    	//		    )
    	//		}	
    	//	}
        }
    }
}
