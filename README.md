# CloudSimAutomation: A Human Readable Scenario Specification for Automated Creation of Simulations on CloudSim [![GPL licensed](https://img.shields.io/badge/license-GPL-blue.svg)](http://www.gnu.org/licenses/gpl-3.0)

CloudSim Automation is a Java command line tool based on CloudSim and CloudReports classes that is able to read specifications of CloudSim simulation scenarios from a YAML file, a very human readable data format. Simulation scenarios can be written inside a YAML file and CloudSim Automation reads these simulation scenarios, creates and runs them on CloudSim.  
The tool releases researchers from needing to write Java code just to run simulation scenarios. By this way, the attention can be focused on the problem to be solved, such as creation of new algorithms for load balancing, new virtual machine scheduling policies, VM placement, resource provisioning, workload prediction, server consolidation, energy efficiency, cost reduction and so on. 

This work contributes to:

- avoid programming on the creation of CloudSim Plus simulation environments;
- reduce learning curve on creation of CloudSim Plus simulation scenarios;
- facilitate and automate CloudSim Plus simulation environments creation;
- use a human readable file format to specify cloud simulation scenarios and speed up such a simulation process phase;
- allow reuse, extension and sharing of simulations scenarios.

# How to use the tool 

You can run the tool from a terminal using the following command (check the correct version number of the jar file):

*java -jar [CloudSimAutomation-0.4.0-jar-with-dependencies.jar](https://github.com/manoelcampos/cloudsim-plus-automation/releases/tag/v0.4.0) PathToYamlSimulationScenarioFile*

Execute the tool without any parameter to see the usage help.

# Published Paper

For more information, read the paper published on the [Springer Lecture Notes in Computer Science Volume 8662](http://doi.org/10.1007/978-3-319-11167-4_34). Realize the paper is related to an older version of the tool, which is compatible with CloudSim 3. 
The YAML structure has changed since there too, making it simpler and matching the name of entries with CloudSim and CloudSim Plus classes (such as VmAllocationPolicy, VmScheduler, CloudletScheduler). See the last section for more information.

**If you are using this work for publishing a paper, please cite our paper above.**

# Notice
This is a version compatible with CloudSim 4, which is not actively maintained. Updates may be performed after you create an [issue ticket](https://github.com/manoelcampos/cloudsim-plus-automation/issues). Every issue will be assessed and there is no guarantee that it will be fixed. If you would like to contribute to this version, feel free to submit a pull request.

The active developed version compatible with [CloudSim Plus](http://cloudsimplus.org) is available at the [master branch](https://github.com/manoelcampos/cloudsim-plus-automation).
