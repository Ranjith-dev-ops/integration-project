pipeline {
    agent any

    environment {
        SONARQUBE_ENV = 'sonar-local'
        DOCKERHUB_CREDENTIALS = credentials('docker-hub-token')
        GITHUB_CREDS = 'github-token'
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
                sh "mvn clean package"
            }
        }

        stage('Test') {
            steps {
                sh "mvn test"
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv("${SONARQUBE_ENV}") {
                    sh "mvn sonar:sonar"
                }
            }
        }

        stage('Quality Gate') {
            steps {
                waitForQualityGate abortPipeline: true
            }
        }

        stage('Docker Build') {
            steps {
                sh "docker build -t ${DOCKERHUB_CREDENTIALS_USR}/integration-project:latest ."
            }
        }

        stage('Docker Login & Push') {
            steps {
                sh """
                    echo ${DOCKERHUB_CREDENTIALS_PSW} | docker login -u ${DOCKERHUB_CREDENTIALS_USR} --password-stdin
                    docker push ${DOCKERHUB_CREDENTIALS_USR}/integration-project:latest
                """
            }
        }

        stage('Deploy') {
            steps {
                sh """
                    docker rm -f integration-project || true
                    docker run -d --name integration-project -p 8080:8080 ${DOCKERHUB_CREDENTIALS_USR}/integration-project:latest
                """
            }
        }
    }
}
