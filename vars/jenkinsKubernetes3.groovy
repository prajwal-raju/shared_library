def call(String registryCred = 'a', String registryin = 'a', String docTag = 'a', String grepo = 'a', String gbranch = 'a', String gitcred = 'a') {

pipeline {
environment { 
		registryCredential = "${registryCred}"
		registry = "$registryin" 	
		dockerTag = "${docTag}$BUILD_NUMBER"
		gitRepo = "${grepo}"
		gitBranch = "${gbranch}"
		gitCredId = "${gitcred}"
	}
		
    agent none

    stages {
        stage("POLL SCM"){
		agent{label 'docker'}
            	steps {
                	git branch: 'gitBranch', credentialsId: ['gitCredID'], url: 'gitRepo'             
            	}
        } 
        stage('BUILD IMAGE') {
		agent{label 'docker'}
            	steps {
                	sh 'docker build -t $registry:$dockerTag .'             
            	}
        }
        stage('PUSH HUB') { 
		agent{label 'docker'}
            	steps {
                	script {
                    		docker.withRegistry( '', '$registryCredential' ) {
                        	sh 'docker push $registry:$dockerTag'
                    		}
                	}    
            	}
        }
        stage('DEPLOY IMAGE') {
		agent{label 'kubernetes'}
		steps {
			git branch: '$gitBranch', credentialsId: ['$gitCredID'], url: '$gitRepo'
			sh 'kubectl apply -f manifest.yml --record'
		}
	}  
    }
}  
}
