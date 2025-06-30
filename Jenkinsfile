pipeline {
    agent any

    environment {
        DOCKER_IMAGE = 'event-backend'
        DOCKER_TAG = 'latest'
    }

    stages {
        stage('Checkout') {
            steps {
                git branch: 'main', url: 'https://github.com/NINIAA-fatimaezzahraa/event-backend.git'
            }
        }

        stage('Build Backend') {
            steps {
                sh './mvnw clean package -DskipTests'
            }
        }

        stage('Build Docker Image') {
            steps {
                sh 'docker build -t $DOCKER_IMAGE:$DOCKER_TAG .'
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
    }
}
