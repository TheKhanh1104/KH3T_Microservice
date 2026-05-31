pipeline {
    agent any

    environment {
        DOCKER_REGISTRY = 'your-registry.com'
        APP_NAME = 'kh3t-shop'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build Backend') {
            steps {
                dir('kh3tshop-microservices') {
                    sh 'mvn clean package -DskipTests'
                }
            }
        }

        stage('Build Frontend') {
            steps {
                dir('kh3tshop-fe') {
                    sh 'npm install'
                    sh 'npm run build'
                }
            }
        }

        stage('Docker Build & Push') {
            steps {
                sh 'docker-compose -f docker-compose.yml -f docker-compose.services.yml build'
                // sh 'docker login -u $DOCKER_USER -p $DOCKER_PASS'
                // sh 'docker-compose -f docker-compose.yml -f docker-compose.services.yml push'
            }
        }

        stage('Deploy') {
            steps {
                sh 'docker-compose -f docker-compose.yml -f docker-compose.services.yml up -d'
            }
        }
    }

    post {
        always {
            cleanWs()
        }
    }
}
