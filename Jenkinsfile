pipeline {
    agent any

    environment {
        SONARQUBE_ENV = 'sonar-local'              // optional: name of Sonar server in Jenkins
        SONAR_TOKEN = credentials('sonar-token')  // secret text credential id in Jenkins (create if needed)
        DOCKERHUB = credentials('docker-hub-token')// username/password credential id in Jenkins
        GIT_CREDS_ID = 'github-token'              // credential id for Git (create in Jenkins)
    }

    stages {
        stage('Checkout') {
            steps {
                git branch: 'main',
                    credentialsId: "${GIT_CREDS_ID}",
                    url: 'https://github.com/Ranjith-dev-ops/integration-project.git'
            }
        }

        stage('Build (Maven)') {
            steps {
                // Run Maven build - fails if tests or build fail
                bat "mvn -B clean package"
                // Show what was produced
                bat "echo ==== target listing ==== & dir target || echo target directory not found"
                // Fail fast if no jar found
                bat """
                if not exist target\\*.jar (
                  echo ERROR: No jar found in target\\
                  exit 1
                ) else (
                  echo Jar found.
                )
                """
                // Save the built jar to be used in another stage (in case Jenkins uses different nodes)
                stash name: 'app-jar', includes: 'target/*.jar'
            }
        }

        stage('SonarQube Analysis') {
            steps {
                // If you don't want Sonar, remove this whole stage.
                // This runs mvn sonar:sonar and uses the SONAR_TOKEN credential defined above.
                // Ensure you have added 'sonar-token' as Secret Text in Jenkins.
                withSonarQubeEnv("${SONARQUBE_ENV}") {
                    bat "mvn sonar:sonar -Dsonar.login=%SONAR_TOKEN%"
                }
            }
        }

        stage('Skip Quality Gate (no webhook)') {
            steps {
                echo "Not waiting for SonarQube callback (no webhook configured). Pipeline continues."
            }
        }

        stage('Prepare for Docker Build') {
            steps {
                // Ensure the artifact is present for docker build
                unstash 'app-jar'
                bat "echo ==== target listing before docker build ==== & dir target"
            }
        }

        stage('Docker Build') {
            steps {
                // Build image from Dockerfile in repo root (Dockerfile copies target/*.jar)
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
                // Avoid port conflict with Jenkins on 8080 — mapping host 8081 to container 8080
                bat """
                docker rm -f integration-project || exit 0
                docker run -d --name integration-project -p 8081:8080 %DOCKERHUB_USR%/integration-project:latest
                """
            }
        }
    }

    post {
        always {
            echo "Pipeline finished. Check console and Sonar for details."
        }
    }
}
