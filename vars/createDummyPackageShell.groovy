#!/usr/bin/env groovy
// vars/createDummyPackageShell.groovy

def call(){
	sh '''
		touch file1.txt file2.txt file3.txt
		tar -cvf dummyArchive.tar file*.txt
	'''
}
