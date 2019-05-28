#!/usr/bin/env groovy
// vars/shellCmd.groovy

def call(String command){
	sh  "${command}"
}
