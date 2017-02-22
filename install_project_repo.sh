#!/bin/bash
#
# CloudSim Automation: A Human Readable Scenario Specification for Automated Creation of Simulations on CloudSim.
# https://github.com/manoelcampos/CloudSimAutomation
#
#     Copyright (C) 2015-2016  Universidade da Beira Interior (UBI, Portugal) and
#     the Instituto Federal de Educação Ciência e Tecnologia do Tocantins (IFTO, Brazil).
#
#     This file is part of CloudSim Automation.
#
#     CloudSim Automation is free software: you can redistribute it and/or modify
#     it under the terms of the GNU General Public License as published by
#     the Free Software Foundation, either version 3 of the License, or
#     (at your option) any later version.
#
#     CloudSim Automation is distributed in the hope that it will be useful,
#     but WITHOUT ANY WARRANTY; without even the implied warranty of
#     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#     GNU General Public License for more details.
#
#     You should have received a copy of the GNU General Public License
#     along with CloudSim Automation. If not, see <http://www.gnu.org/licenses/>.
#

echo "Copy dependencies from your local maven repository, usually at ~/.m2, to the projects's repositorty"
mvn install:install-file -Dfile=/Users/manoelcampos/.m2/repository/org/cloudbus/cloudsim/cloudsim/3.1-SNAPSHOT/cloudsim-3.1-SNAPSHOT.jar -DgroupId=org.cloudbus.cloudsim -DartifactId=modules -Dversion=3.1-SNAPSHOT -Dpackaging=jar -DlocalRepositoryPath=./repo/ 
