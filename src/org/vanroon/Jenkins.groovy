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

    def getCrumb(){
        steps.withCredentials (
            [steps.usernameColonPassword(credentialsId: credentials, variable: 'CREDENTIALS')]
        ) {
            res = getHttpRequest(url + "/crumbIssuer/api/json", credentials)
            println res
        }
    }
}