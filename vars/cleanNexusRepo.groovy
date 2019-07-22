#!/usr/bin/env groovy
// vars/cleanNexusRepo.groovy

import org.tibcocicd.GlobalVars
import org.tibcocicd.PipelineBuilder
import org.tibcocicd.Nexus

/**
 * cleanNexusRepository: Removes excess of components (more than max num)
 * INPUT:
 *  - Nexus n
 *  - String repository
 *  - Integer maxComponents
 * OUTPUT:
 *  - none
 */
def call(Nexus n, String repository, Integer maxComponents) {
    withCredentials([usernameColonPassword(credentialsId: n.credentials, variable: 'USERPASS')]){

        def IDs = []
        def IDsToPurge = []
        def auth = n.encodeBase64(USERPASS)
        def headers = ["Authorization":"Basic ${auth}"]

        resultText = n.httpRequest("http://${GlobalVars.nexusUrl}/service/rest/v1/search?sort=version&direction=asc&repository=${repository}", headers, "GET")
        result = steps.readJSON text: resultText

        result['items'].each {
            IDs.add(it)
        }

        // If continuation token, processa pagination
        while (result['continuationToken'] != null){
            resultText = n.httpRequest("http://${GlobalVars.nexusUrl}/service/rest/v1/search?sort=version&direction=asc&repository=${repository}&continuationToken=${result['continuationToken']}",headers,"GET")
            result = steps.readJSON text: resultText
            result['items'].each {
                IDs.add(it)
            }
            if (result['continuationToken'].toString() == "null"){
                println "End of results"
                break
            } else {
                println "Found continuation token. Continue..."
            }
        }

        // In component list, group by GroupID and artifact ID.
        // If more than defined ${num}, add component ID to indexes
        // remove excess of components from top of list (because sort order is asc)
        IDs.countBy { it['group']+";"+it['name'] }.each { artifact, num ->
            if (num > maxComponents) {
                println "Artifact: ${artifact} has too many components! Number of components: ${num} (${maxComponents} allowed)"
                def (gid, aid )= artifact.tokenize(';')
                def indexes = IDs.findIndexValues { it['name'] == aid && it['group'] == gid }
                println "\n---------- COMPONENTS SCHEDULED FOR DELETION ----------\n"
                indexes[0..indexes.size() - (maxComponents + 1)].each {
                    IDsToPurge.add(IDs[it.toInteger()]['id'])
                     println n.httpRequest("http://${GlobalVars.nexusUrl}/service/rest/v1/components/${IDs[it.toInteger()]['id']}", headers, "GET")
                     println "\n---------- \n"
                }
            }
        }
        if (IDsToPurge.size() > 0 ){
            IDsToPurge.each {
                println "Deleting ID ${it}..."
                def RC = n.httpRequest("http://${GlobalVars.nexusUrl}/service/rest/v1/components/${it}", headers, "DELETE")
                switch (RC) {
                    case "204":
                        println "RC: ${RC}. Succes! Component with ID ${it} deleted."
                        steps.addInfoBadge id: '', text: "Component with ID: ${it} deleted."
                        break
                    default:
                        println  "RC: ${RC}. Something went wrong."
                        break
                }
            }
        } else {
            println "No components to remove."
        }
    }
}