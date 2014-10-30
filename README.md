CloudSimAutomation: A Human Readable Scenario Specification for Automated Creation of Simulations on CloudSim.
==================

CloudSim Automation is a Java command line tool based on CloudSim and CloudReports 
classes that is able to read the specification of CloudSim simulation
scenarios from a YAML file, a very human readable data format.
Simulation scenarios can be written inside a YAML file
and the Cloud Automation Tool reads this scenario specification,
creates and runs it on CloudSim.

## At a terminal, the application can be run using one of the following methods:
1. java com.manoelcampos.cloudsim.automation.YamlScenario NameOfYamlSimulationScenarioFile
2. java -jar CloudSimAutomation-1.0-jar-with-dependencies.jar NameOfYamlSimulationScenarioFile

For more information, read the paper at [a relative link](paper_cloudsim_automation.pdf)