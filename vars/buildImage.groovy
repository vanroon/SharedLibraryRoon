#!/usr/bin/env groovy
// vars/buildImage.groovy
import com.company.GlobalVars

/**
 * buildImage: builds image
 * INPUT:
 *  - String imageTag
 * OUTPUT:
 *  - 
 */


def call(String imageTag) {
    timestamps {
        script {
          sh '''
            echo oiIamAStepBuildingIMAGE
          '''
        }
    }
}
