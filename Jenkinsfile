pipeline {
    agent any

    environment {
        SONARQUBE_ENV = 'sonar-local'              // Name of SonarQube server configured in Jenkins (optional)
        SONAR_TOKEN = credentials('sonar-token')  // Secret Text credential id in Jenkins (Sonar token)
        DOCKERHUB = credentials('docker-hub-token')// Username/Password credential id in Jenkins
        GIT_CREDS_ID = 'github-token'              // Credential id for Git (personal access token)
    }

    stages {
        stage('Checkout') {
            steps {
                // Use the actual credential id created in Jenkins
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
                // Use Sonar token explicitly to ensure scanner authenticates
                // withSonarQubeEnv will also set SONAR_HOST_URL if configured in Jenkins
                withSonarQubeEnv("${SONARQUBE_ENV}") {
                    bat "mvn sonar:sonar -Dsonar.login=%SONAR_TOKEN%"
                }
            }
        }

        // NOTE: Quality Gate stage removed to avoid pipeline hang
        stage('Skip Quality Gate (no webhook)') {
            steps {
                echo "Not waiting for SonarQube callback. Analysis will run on SonarQube but pipeline won't block."
            }
        }

        stage('Docker Build') {
            steps {
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
                // Use host port 8081 to avoid colliding with Jenkins on 8080
                bat """
                docker rm -f integration-project || exit 0
                docker run -d --name integration-project -p 8081:8080 %DOCKERHUB_USR%/integration-project:latest
                """
            }
        }
    }

    post {
        always {
            echo "Pipeline finished. Check Sonar and Jenkins console for details."
        }
    }
}
