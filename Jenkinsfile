pipeline {
    agent any

    environment {
        SONARQUBE_ENV = 'sonar-local'                  // Jenkins SonarQube server ID
        DOCKERHUB_CREDENTIALS = credentials('docker-hub-token') // DockerHub credentials
        GITHUB_CREDS = 'github-token'                  // GitHub credentials
    }

    stages {

        stage('Checkout') {
            steps {
                git branch: 'main',
                    credentialsId: 'GITHUB_CREDS',
                    url: 'https://github.com/Ranjith-dev-ops/integration-project.git'
            }
        }

        stage('Build') {
            steps {
                bat "mvn clean package"
            }
        }

        stage('Test') {
            steps {
                bat "mvn test"
            }
        }

        stage('SonarQube Analysis') {
            steps {
                // Wrap analysis to link it to this pipeline run
                withSonarQubeEnv("${SONARQUBE_ENV}") {
                    bat "mvn sonar:sonar"
                }
            }
        }

        stage('Quality Gate') {
            steps {
                // Wait for SonarQube to finish processing this run
                waitForQualityGate abortPipeline: true
            }
        }

        stage('Docker Build') {
            steps {
                bat "docker build -t %DOCKERHUB_CREDENTIALS_USR%/integration-project:latest ."
            }
        }

        stage('Docker Login & Push') {
            steps {
                bat """
                echo %DOCKERHUB_CREDENTIALS_PSW% | docker login -u %DOCKERHUB_CREDENTIALS_USR% --password-stdin
                docker push %DOCKERHUB_CREDENTIALS_USR%/integration-project:latest
                """
            }
        }

        stage('Deploy') {
            steps {
                bat """
                docker rm -f integration-project || exit 0
                docker run -d --name integration-project -p 8080:8080 %DOCKERHUB_CREDENTIALS_USR%/integration-project:latest
                """
            }
        }
    }
}
