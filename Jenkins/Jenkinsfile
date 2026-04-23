node {

    stage('Checkout') {
        git(
            url: 'https://github.com/alexkv2000/orderOne.git', 
            branch: 'master', 
            credentialsId: 'github-credentials'
        )
    }

    stage('Build') {
        steps {
            sh 'mvn --version'
            sh 'mvn clean package'
        }
    }

    stage('Stop Old JAR') {
        steps {
            script {
                def targetJar = "E:\\Develop\\orderone\\orderOne.jar"
                if (fileExists(targetJar)) {
                    bat "taskkill /F /IM java /T"
                }
            }
        }
    }

    stage('Copy JAR') {
        steps {
            script {
                def targetJar = "E:\\Develop\\orderone\\orderOne.jar"
                def workspaceJar = "${WORKSPACE}\\target\\orderOne.jar"

                if (fileExists(targetJar)) {
                    bat "taskkill /F /IM java /T"
                }

                bat "copy /Y ${workspaceJar} ${targetJar}"
                bat "echo порядок один.jar был скопирован в E:\\Develop\\orderone\\"
            }
        }
    }
}
