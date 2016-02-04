#!/bin/bash
echo "Copy dependencies from your local maven repository, usually at ~/.m2, to the projects's repositorty"
mvn install:install-file -Dfile=/Users/manoelcampos/.m2/repository/org/cloudbus/cloudsim/cloudsim/3.1-SNAPSHOT/cloudsim-3.1-SNAPSHOT.jar -DgroupId=org.cloudbus.cloudsim -DartifactId=modules -Dversion=3.1-SNAPSHOT -Dpackaging=jar -DlocalRepositoryPath=./repo/ 
