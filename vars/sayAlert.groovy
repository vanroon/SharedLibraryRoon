#!/usr/bin/env groovy
// vars/sayAlert.groovy

def call(String msg){
	echo "==== ALERT ===="
	echo "${msg}"
	echo "==== END OF ALERT ===="
}
