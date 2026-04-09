pipeline {
    agent {
        label 'master'
    }

    tools {
        jdk 'Java21'
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scmGit(
                    branches: [[name: '*/main']],
                    userRemoteConfigs: [[url: 'https://github.com/Ariyakrishna/CICD-Security.git']]
                )
            }
        }
        
        stage ('Docker Build'){
            steps{
                bat '''
                cd frontend 
                docker build -t frontend-image .
                '''
                
            }
        }


        
stage('OWASP Dependency Scan - Frontend') {
    steps {
        dependencyCheck additionalArguments: '--scan frontend --format ALL',
                        odcInstallation: 'owasp-dc'
    }
}



        stage('Publish Dependency Report') {
            steps {
                dependencyCheckPublisher pattern: '**/dependency-check-report.xml'
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('SonarQubeServer') {

                    // Frontend Scan
                    bat """
                    ${tool 'SonarScanner'}\\bin\\sonar-scanner.bat ^
                    -Dproject.settings=sonar-project-frontend.properties
                    """

                    // Backend Scan
                    bat """
                    ${tool 'SonarScanner'}\\bin\\sonar-scanner.bat ^
                    -Dproject.settings=sonar-project-backend.properties
                    """
                }
            }
        }
    }
}
