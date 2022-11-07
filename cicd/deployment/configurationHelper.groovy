def applyConfiguration(def deploymentFolder, def targetEnvironment, def targetNamespace) {
	
	dir (deploymentFolder) {

		if (targetEnvironment == 'cicd') {
			sh """
    			oc apply -f cicd/configmap-fhb-fis-o-rollback-transfer.yml -n ${targetNamespace}
    			oc apply -f secret-kafka-authentication.yml -n ${targetNamespace}
  			"""

		} else {
			sh """
    			oc apply -f ${targetEnvironment}/configmap-fhb-fis-o-rollback-transfer.yml -n ${targetNamespace}
  			"""
		
		}
		
      
  	}
}

return this
