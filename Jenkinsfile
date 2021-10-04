pipeline {
    
    agent {
        dockerfile {
            args '-u root:root --network="host"'
        }
    }

    stages {
		stage('Build') {
            steps {
                sh 'mvn clean install'
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
