pipeline {
    agent any 
    stages {
         stage('Setup') { 
              steps {
                echo 'Compile project'
                sh "chmod +x gradlew"
                sh "./gradlew clean build --no-daemon"
                }
         }
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
