// var/userId.groovy

def call() {
    node {
        println "oi"
        println currentBuild.getBuildCauses[0]
    }
}