#!/usr/bin/env groovy
// var/compressFile.groovy

/**
 * Zips all content in passed directory
 * 
 * inputDir -> full path of dir of which the contents are to be zipped
 * outputDir -> destinationPath of zipfile (default is Jenkins workspace)
 * zipFile -> full path + name of zipFile
 */
def call(String inputDir, String zipFile) {
    withEnv([
        "inputDir=${inputDir}",
        //"outputDir=${outputDir}",
        "zipFile=${zipFile}"
    ]){
        sh """
         #   cd "${inputDir}"
            /bin/zip "${zipFile}" "${inputDir}/*"
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