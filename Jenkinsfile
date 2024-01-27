pipeline {
    agent any 
    stages {
        stage('Build') { 
            steps {
                echo 'build echo'
                sh "./gradlew assembleDebug"
                echo 'build ddecho'
            }
        }
        stage('Test') { 
            steps {
               echo 'test echo'
            }
        }
        stage('Deploy') { 
            steps {
                  echo 'Deploy echo'
            }
        }
    }
}
