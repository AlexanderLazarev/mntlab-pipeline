node(env.SLAVE) {

	def gitURL = "https://github.com/AlexanderLazarev/mntlab-pipeline"
	def gradle = "/usr/bin/gradle"

	stage('Preparation') {
		git([url: gitURL + '.git', branch: 'alazarev'])
	}
	
	stage('Build') {
		echo 'BUILDING'
		sh gradle + ' build'
	}
	
	stage('Test') {
		echo 'TESTING'
		parallel (
			'Unit Tests': {sh gradle + ' test'},
			'Jacoco Test': {sh gradle + ' jacocoTestReport'},
			'Cucumber Tests': {sh gradle + ' cucumber'}
		)
	}

	stage('Trigger') {
		echo 'TRIGGERING'
		build job: "Job_child1", parameters: [string(name: 'BRANCH_NAME', value: 'master')]
		step([
		    $class: 'CopyArtifact', 
		    filter: 'alazarev_dsl_script.tar.gz', 
		    flatten: true, 
		    projectName: 'Job_child1', 
		    selector: [$class: 'StatusBuildSelector', stable: false]])
	}
	
	stage('Packaging and Publishing') {
		echo 'PACKAGING AND PUBLISHING'
		sh 'cp ./build/libs/gradle-simple.jar ./'
		sh 'tar -czf pipeline-alazarev-${BUILD_NUMBER}.tar.gz script.groovy Jenkinsfile gradle-simple.jar'
		archiveArtifacts 'pipeline-alazarev-${BUILD_NUMBER}.tar.gz'
	}

	stage('Asking for manual approval') {
		timeout(time:1, unit:'HOURS') {
           input message:'Do you deployment this artefact?', ok: 'yes'
		}
	}
	
	stage('Deployment') {
		echo 'DEPLOYMENT'
		sh 'java -jar gradle-simple.jar'
	}
	
	stage('Sending status') {
		sh 'echo "SUCCESS"'
	}


}