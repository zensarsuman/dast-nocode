pipeline {
    agent any

    tools {
        maven 'maven 3.6.3'
    }

    environment {
        ZAP_KEY             =       credentials('ZAP_KEY')
        UAT_USER            =       credentials('UAT_USER')
        UAT_PASSWORD        =       credentials('UAT_PASSWORD')
        TARGET_URL          =       "https://tgtprac.managedbyparabellyx.com/"
        INCLUDEINCONTEXT    =       "https://tgtprac.managedbyparabellyx.com.*"
    }

    stages{
        stage('Start Zap'){
            steps {
                echo 'Starting Zap'
                sh '/opt/zap/zap.sh -port 9191 -daemon -config api.key=$ZAP_KEY > zap.logs &'
                sh """
                    while ! nc -z localhost 9191; do   
                        sleep 1 
                    done
                """
            }
        }
        stage('Initialize Zap'){
            steps {
                echo 'Initializing Zap'
                dir('dast'){
                    sh 'mvn package'
                    sh 'java -cp "target/dast-1.0-SNAPSHOT.jar:lib/*" com.parabellyx.dast.App init'
                }
            }
        }
        stage('Selenium'){
            steps {
                echo 'Running side runner'
                dir('selenium') {
                    // Below are the lines that can help remove credentials from the side file
                    // sh 'sed -i "s/##password##/${UAT_PASSWORD}/g" selenium/dvwa-dast.side'
                    // sh 'sed -i "s/##user##/${UAT_USER}/g" selenium/dvwa-dast.side'
                    sh 'selenium-side-runner -c "browserName=chrome goog:chromeOptions.args=[headless] acceptInsecureCerts=true" --proxy-type=manual --proxy-options="http=localhost:9191 https=localhost:9191" dvwa-dast.side'
                    sh 'sleep 15'
                }
            }
        }
        stage('Run Zap'){
            steps {
                echo 'Run Zap'
                dir('dast'){
                    sh 'java -cp "target/dast-1.0-SNAPSHOT.jar:lib/*" com.parabellyx.dast.App spider'
                    sh 'java -cp "target/dast-1.0-SNAPSHOT.jar:lib/*" com.parabellyx.dast.App attack'
                }
            }
        }
        stage('Saving Report'){
            steps {
                echo 'Archiving Report'
                dir('dast'){
                    archiveArtifacts artifacts: '**/*.html', fingerprint: true
                }
            }
        }
    }
}
