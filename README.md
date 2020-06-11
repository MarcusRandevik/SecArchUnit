# SecArchUnit

Repository for the SecArchUnit master's thesis at Software Engineering.

The ArchUnit extension can be found in this separate repository: [SecArchUnit/ArchUnit](https://github.com/SecArchUnit/ArchUnit/tree/extension)

The thesis report (both the LaTeX workspace and the compiled PDF) is synced nightly in [the thesis branch](https://github.com/MarcusRandevik/SecArchUnit/tree/thesis).

## Prerequisites

* Java 11
* Maven (to build SecArchUnit)
* Gradle (to build our fork of ArchUnit)

## Using SecArchUnit

1. Open the relevant test project in your IDE
2. Run the SecurityTest class (constraints 1-5) as a JUnit test
3. Run the ExtensionTest class (constraints 6-7) as a JUnit test

## Using the SonarQube plugin

### Prerequisites

* SonarQube (tested on version 8.2.0.32929)
* sonar-scanner (tested on version 4.2.0.1873)

### Validate constraints

1. Go to the directory of the relevant test project
2. Copy `sonarcustomrules-1.0-SNAPSHOT.jar` to `$SONARQUBE_HOME/extensions/plugins`
3. Restart SonarQube
4. Set up the project in SonarQube
   1. Open SonarQube frontend
   2. Create project with projectKey matching the one in `sonar-project.properties`
   3. Create a custom quality profile for the project
   4. Activate the applicable rules (found under the secarchunit tag)
   5. Assign the quality profile to the project
5. Run sonar-scanner on the project: `sonar-scanner`
6. See constraint violations in the SonarQube frontend

## Using the PMD plugin

### Prerequisites

* PMD (tested on version 6.23.0)

### Validate constraints

1. Go to the directory of the relevant test project
2. Copy `pmd-custom-rules-1.0-SNAPSHOT.jar` to `$PMD_HOME/lib`
3. Extract annotations from the system: `./pmd-dump-annotations.sh`
4. Validate the constraints: `./pmd-test.sh`