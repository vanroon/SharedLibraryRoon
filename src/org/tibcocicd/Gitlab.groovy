#!/usr/bin/env groovy
package org.tibcocicd;

import groovy.transform.InheritConstructors

/**
 * Class Gitlab represents a gitlab instance (not a repository).
 * Gitlab operations and interactions with its API are collected in
 * this class as methods.
 */
@InheritConstructors
class Gitlab extends PipelineBuilder {

    def privateToken
    def gitLabAddress = GlobalVars.gitlabUrl
    def apiRoot = "/api/v4"
    def projectsEndpoint = "/projects/"
    
    Gitlab(steps, privateToken) {
        super(steps)
        this.privateToken = privateToken
    }

    Gitlab(steps){
        super(steps)
    }

    /**
     * Split git repository url and return the group
     */
    def getgroup(repoUrl) {
        def url = repoUrl.tokenize('/')
        return url[2]
    }

    /**
     * Split git repository url and return the subgroup
     */
    def getsubgroup(repoUrl) {
        def url = repoUrl.tokenize('/')
        return url[3]
    }

    /**
     * Split git repository url and return the repository name
     */
    def getreponame(repoUrl) {
        def url = repoUrl.tokenize('/')
        return url[4].tokenize('.')[0]
    }

    /**
     * getGitLabProjectId: gets Git project ID for a given repository
     * INPUT: 
     *  - Git repository url
     *  - Git privateToken (access token)
     * OUTPUT:
     *  - Git repository project ID.
     */
    def getGitlabProjectId(repoUrl, privateToken){
        echo "Getting ID for Repository URL: " + repoUrl
        def var = repoUrl.tokenize('/')
        def group = var[2]
        def subgroup = var[3]
        def repoName = var[4].tokenize('.')[0]

        def resultText = sh ([script: """ curl -s -v --header 'PRIVATE-TOKEN: ${privateToken}' -X GET 'http://${gitLabAddress}${apiRoot}/search?scope=projects&search=${repoName}' """, returnStdout: true]).trim()
        def result = steps.readJSON text: resultText
        echo resultText
        echo "Searchgin for " + repoName
        // loop through result and find exact match
        def id
        result.each { project ->
            echo "Search: " + repoName
            echo "result: " + project['name'][0]
            if (project['name'].matches(repoName)) {
                id = project['id']
            }
        }
        return id
    }

    /**
     * getTags: Finds all tags for a repository
     * INPUT:
     *  - Git repository url
     *  - Git privateToken (access token)
     * OUTPUT:
     *  - A list with all tags in given repository
     */
    def getTags(repoUrl, privateToken){
        def var = repoUrl.tokenize('/')
        def group = var[2]
        def subgroup = var[3]
        def repoName = var[4].tokenize('.')[0]
        def id = getGitlabProjectId(repoUrl, privateToken)
        
        def resultText = sh ([script: """ curl -s -v --header "PRIVATE-TOKEN: ${privateToken}" -X GET 'http://${gitLabAddress}${apiRoot}${projectsEndpoint}/${id}/repository/tags' """, returnStdout: true]).trim()
        echo resultText
        def result = steps.readJSON text: resultText
        def tagList = []
        result.each {
            tagList.add(it['name'].toString())
        }
        return tagList
    }

    /**
     * addHookToRepo: Adds a webhook with token to a git repository
     * INPUT:
     *  - Git repository project ID
     *  - Hook url
     *  - Hook token
     *  - Git privateToken (access token)
     * OUTPUT:
     *  - An HTTP response code. 
     */
    def addHookToRepo(projectId, hookUrl, hookToken, privateToken){
        def url = "http://${gitLabAddress}/api/v4/projects/${projectId}/hooks?url=${hookUrl}&token=${hookToken}"
        def headers = [:]
        headers["PRIVATE-TOKEN"] = "${privateToken}"
        
        def rc = postHttpRequestNoBody(url, headers)
        return rc
    }
}