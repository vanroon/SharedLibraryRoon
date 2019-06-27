#!/usr/bin/env groovy
package org.vanroon;

interface Repository {
    def ping()
    def upload(String fileName, String repository, String groupId, String artifactId, String extension, String version)
    def download(String repository, String groupId, String artifactId, String extension, String version)
    def getAvailableVersions(String repository, String groupId, String artifactId)
    def getAvailableVersions(NexusComponent n)
    def getLatestVersion(String repository, String groupId, String artifactId)
    def getLatestVersion(NexusComponent n)
}