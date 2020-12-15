#!/usr/bin/env groovy
// vars/copyImage.groovy
import org.svb.GlobalVariables
import org.svb.PipelineBuilder

def call(
	String SOURCE_IMAGE,
	String DESTINATION_IMAGE
) {
    def helper = new PipelineBuilder(this)
	timestamps {
		helper.banner("Retagging image",":",1)
		script{
			echo "Retagging ${SOURCE_IMAGE} to ${DESTINATION_IMAGE}"
			withEnv([
				"SOURCE_IMAGE=${SOURCE_IMAGE}",
				"DESTINATION_IMAGE=${DESTINATION_IMAGE}"
		    ]) {
    		    sh """/bin/bash
    			sudo docker pull "${SOURCE_IMAGE}"
    		    sudo docker tag "${SOURCE_IMAGE}" "${DESTINATION_IMAGE}"
    			sudo docker push "${DESTINATION_IMAGE}"
    			"""
		   	}
		}
	}
}
