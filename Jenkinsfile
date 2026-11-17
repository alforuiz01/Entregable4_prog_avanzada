pipeline {
    agent any

    options {
        timestamps()
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '15'))
        timeout(time: 20, unit: 'MINUTES')
    }

    // Se dispara con el webhook de GitHub (githubPush).
    // pollSCM queda como respaldo si el webhook no llega.
    triggers {
        githubPush()
        pollSCM('* * * *')
    }

    environment {
        APP_NAME       = 'mascotas'
        PROD_CONTAINER = 'mascotas-prod'
        PROD_NETWORK   = 'cicd-net'
        PROD_PORT      = '8081'
        PROD_VOLUME    = 'mascotas-data'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
                sh 'git log -1 --pretty="Commit: %h - %an - %s"'
            }
        }

        stage('Build') {
            steps {
                // Compila los dos modulos (core y web) sin ejecutar tests
                sh 'mvn -B clean compile -DskipTests'
            }
        }

        stage('Pruebas unitarias') {
            steps {
                // surefire: clases *Test de ambos modulos
                sh 'mvn -B test'
            }
            post {
                always { junit testResults: '**/target/surefire-reports/*.xml', allowEmptyResults: true }
            }
        }

        stage('Pruebas de integracion') {
            steps {
                // failsafe: clases *IT (los unitarios ya corrieron en la etapa anterior)
                sh 'mvn -B verify -DskipUnitTests=true'
            }
            post {
                always { junit testResults: '**/target/failsafe-reports/*.xml', allowEmptyResults: true }
            }
        }

        stage('Empaquetado') {
            steps {
                sh 'mvn -B package -DskipTests'
                archiveArtifacts artifacts: 'mascotas-web/target/mascotas-web.jar', fingerprint: true
            }
        }

        stage('Deploy a produccion simulada') {
            when {
                expression { env.GIT_BRANCH ==~ /(origin\/)?main/ }
            }
            steps {
                sh '''
                    docker build -t ${APP_NAME}:${BUILD_NUMBER} -t ${APP_NAME}:latest .
                    docker rm -f ${PROD_CONTAINER} || true
                    docker run -d --name ${PROD_CONTAINER} \
                        --network ${PROD_NETWORK} \
                        -p ${PROD_PORT}:8080 \
                        -v ${PROD_VOLUME}:/app/data \
                        ${APP_NAME}:${BUILD_NUMBER}
                '''
            }
        }

        stage('Smoke test') {
            when {
                expression { env.GIT_BRANCH ==~ /(origin\/)?main/ }
            }
            steps {
                // Espera hasta 60 s a que la app responda UP
                sh '''
                    for i in $(seq 1 20); do
                        if curl -sf http://${PROD_CONTAINER}:8080/actuator/health | grep -q UP; then
                            echo "Aplicacion desplegada y saludable"; exit 0
                        fi
                        sleep 3
                    done
                    echo "La aplicacion no levanto"; docker logs ${PROD_CONTAINER} || true; exit 1
                '''
            }
        }
    }

    post {
        success { echo "Pipeline OK - build #${env.BUILD_NUMBER}" }
        failure { echo "Pipeline FALLO - revisar la etapa marcada en rojo" }
        cleanup { sh 'docker image prune -f || true' }
    }
}
