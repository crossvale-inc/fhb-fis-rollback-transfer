// Returns an application name code ()
def getAppName() {
    return "fhb-fis-o-rollback-transfer"
}

def getAppFriendlyName() {
    return "FHB :: FIS :: Orchestrated service :: fhb-fis-o-rollback-transfer"
}

def getAdditionalAdminPeopleForTemporaryProject() {
    return [
        users: [ "ipavon" ],
        groups: [ ]
    ]
}

def getPerformanceTestsSuites(def performanceTestsFolder, def applicationName, def buildNumber) {
    def performanceTestsSuites = []

    try {
        def files = ''
        dir(performanceTestsFolder) {
            files = sh(script: "ls -1 *.jmx", returnStdout: true).trim() 
        }

        def testFileNames = files.split('\n')

        for (int i=0; i<testFileNames.size(); i++) {
            def file = testFileNames[i]
            def fileName = file.replaceAll('.jmx','')
            performanceTestsSuites.push([name: "jmeter-test-suite", applicationName: "${applicationName}", filename: "${fileName}", buildNumber: "${buildNumber}"])
        }
    }
    catch(Exception ex) {
        println("No performance tests suites found.");
    }
    
    return performanceTestsSuites
}

def getIntegrationTestsInfo(def projectName) {
    return [isAtive: false, command: '', args: '']
}

def getChangeRequestInfo() {
    return [
            
            externalReference: "",
            shortDescription: "",
            description: "",
            environment: "",
            start_date_time: ""
       
    ]
}

def getApplicationType() {
    return [
        buildApplication: true,
        publishArtifacts: false,
        buildImage: true
    ]
}

return this