pipeline {
    agent any

    tools {
        jdk 'java-21'
        maven 'maven-3.9.6'
    }

    environment {
        DOCKER_IMAGE = 'event-backend'
        DOCKER_TAG = 'latest'
        DOCKERFILE = 'Dockerfile.prod'
        JAVA_HOME = tool('java-21')
        PATH = "${JAVA_HOME}/bin:${PATH}"
    }

    stages {
        stage('Checkout') {
            steps {
                git branch: 'main', url: 'https://github.com/NINIAA-fatimaezzahraa/event-backend.git'
            }
        }

        stage('SonarQube analysis') {
            steps {
                withSonarQubeEnv('SonarQube') {
                    sh 'mvn clean verify sonar:sonar'
                }
            }
        }

        stage('Run Unit Tests') {
            steps {
                sh 'chmod +x ./mvnw'
                sh './mvnw test -Dspring.profiles.active=prod'
            }
        }

        stage('Build Backend') {
            steps {
                sh 'chmod +x ./mvnw'
                sh './mvnw clean package -DskipTests'
            }
        }

        stage('Build Docker Image') {
            steps {
                sh 'docker build -f $DOCKERFILE -t $DOCKER_IMAGE:$DOCKER_TAG .'
            }
        }

        stage('Deploy Container') {
            steps {
                sh 'docker stop $DOCKER_IMAGE || true'
                sh 'docker rm $DOCKER_IMAGE || true'
                sh 'docker run -d --name $DOCKER_IMAGE -p 8081:8080 $DOCKER_IMAGE:$DOCKER_TAG'
            }
        }
    }

    post {
        failure {
            echo "Build failed"
        }
        success {
            echo "Build successful"
        }
    }
}
