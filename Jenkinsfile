pipeline {
    agent any

    environment {
        SONARQUBE_ENV = 'sonar-local'                      // Jenkins SonarQube server name (must exist)
        SONAR_TOKEN = credentials('jenkins-sonar-token')           // Jenkins credential id for Sonar token (secret text)
        DOCKERHUB = credentials('docker-hub-token')        // docker-hub-token must be created in Jenkins (username/password)
        GIT_CREDS_ID = 'github-token'                      // credential id (string) used by git step
    }

    stages {
        stage('Checkout') {
            steps {
                // Use the actual credentials id (not the env name)
                git branch: 'main',
                    credentialsId: "${GIT_CREDS_ID}",
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
                // Either rely on withSonarQubeEnv (requires Sonar server configured in Jenkins)
                // and also pass token explicitly to mvn to be sure:
                withSonarQubeEnv("${SONARQUBE_ENV}") {
                    // use %SONAR_TOKEN% (Windows bat) to pass token to Maven
                    bat "mvn sonar:sonar -Dsonar.login=%SONAR_TOKEN%"
                }
            }
        }

        stage('Quality Gate') {
            steps {
                // Protect against infinite waiting: wrap waitForQualityGate in a timeout
                timeout(time: 5, unit: 'MINUTES') {
                    script {
                        def qg = waitForQualityGate(abortPipeline: true)
                        echo "Quality Gate status: ${qg.status}"
                    }
                }
            }
        }

        stage('Docker Build') {
            steps {
                // DOCKERHUB_USR and _PSW will be available from credentials('docker-hub-token')
                bat "docker build -t %DOCKERHUB_USR%/integration-project:latest ."
            }
        }

        stage('Docker Login & Push') {
            steps {
                bat """
                echo %DOCKERHUB_PSW% | docker login -u %DOCKERHUB_USR% --password-stdin
                docker push %DOCKERHUB_USR%/integration-project:latest
                """
            }
        }

        stage('Deploy') {
            steps {
                bat """
                docker rm -f integration-project || exit 0
                docker run -d --name integration-project -p 8081:8080 %DOCKERHUB_USR%/integration-project:latest
                """
            }
        }
    }
}
