pipeline {
    
    agent {
        dockerfile {
            args '-u root:root --network="host"'
        }
    }

    stages {
		stage('Build') {
            steps {
                sh 'mvn build'
            }
        }
        
        stage('Test') {
            steps {
                bat 'mvn test'
            }
        }
        
        stage('Check') {
            steps {
                bat 'mvn check'
            }
        }      
	
        }
    }

    post {
        always {
            sh "chmod -R 777 ."
            cleanWs ()
        }
    }
}
