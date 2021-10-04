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
            archiveArtifacts artifacts: '**/*.jar', fingerprint: true
            sh "chmod -R 777 ."
            cleanWs ()
        }
    }
}
