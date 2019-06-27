#!/usr/bin/env groovy
// var/extractFile.groovy
import java.util.zip.*

/**
 * Unzips content of ${zipFile} to directory ${outputDir}
 */
def call(String outputDir, String zipFile) {
    withEnv([
        "outputDir=${outputDir}",
        "zipFile=${zipFile}"
    ]){
        sh """
            if ! [ -d "${outputDir}" ]; then
                mkdir "${outputDir}"
            fi
            unzip "${zipFile}" -d "${outputDir}"
        """
    }
    // def zip = new ZipFile(new File(zipFile))
    // def outputDir = zip.name.take(zip.name.lastIndexOf('.')).substring(zip.name.lastIndexOf('/')+1)

    // zip.entries().each {
    // if(!it.isDirectory()){
    //     def fOut = new File(outputDir + File.separator + it.name)
    //     //create output dir if not exist
    //     new File(fOut.parent).mkdirs()
    //     def fos = new FileOutputStream(fOut)
    //     def buf = new byte[it.size]
    //     def len = zip.getInputStream(it).read(buf)
    //     fos.write(buf, 0, len)
    //     fos.close()
    // }

}