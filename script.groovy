def CJobs = 4
def repo = "AlexanderLazarev/mntlab-dsl"
def repoURL = "https://github.com/" + repo + ".git"
def command = "git ls-remote -h $repoURL"

def proc = command.execute()
proc.waitFor()

if ( proc.exitValue() != 0 ) {
    println "Error, ${proc.err.text}"
    System.exit(-1)
}

def branches = proc.in.text.readLines().collect {
    it.replaceAll(/[a-z0-9]*\trefs\/heads\//, '')
}

job('Job_main') {
	scm {
		github repo, '$BRANCH_NAME'
	}
	parameters {
		choiceParam('BRANCH_NAME', ['alazarev', 'master'], '')
		activeChoiceParam('BUILDS_TRIGGER') {
			description('Available options')
			filterable()
            choiceType('CHECKBOX')
            groovyScript {
                script('["Job_child1", "Job_child2", "Job_child3", "Job_child4"]')
            }
		}
    }
	steps {
        downstreamParameterized {
            trigger('$BUILDS_TRIGGER') {
                block {
                    buildStepFailure('FAILURE')
                    failure('FAILURE')
                    unstable('UNSTABLE')
                }
               parameters {
                    currentBuild()
				}
			}
			shell(' echo "Publish artefact for childs jobs"')
		}	
	}
	publishers { 
        archiveArtifacts {
            pattern('script.sh')
            onlyIfSuccessful()
		}
	}
	
}
 

for (int i = 1; i <= CJobs; i++) {
	job('Job_child'+i) {
		parameters {
			choiceParam('BRANCH_NAME', branches)
		}
		steps {
			copyArtifacts('Job_main') {
				includePatterns('script.sh')
				targetDirectory('./')
				flatten()
				optional()
				buildSelector {
					workspace()
				}
			}
			shell('bash ./script.sh > output.txt && tar -czvf ${BRANCH_NAME}_dsl_script.tar.gz ./*')
		}
	}
}
