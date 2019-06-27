#!/usr/bin/env groovy
// vars/sayAlert.groovy

def call(String msg){
	echo "==== ALERT ====\n${msg}\n==== END OF ALERT ===="
}
