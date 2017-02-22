CloudSimAutomation: A Human Readable Scenario Specification for Automated Creation of Simulations on CloudSim [![Build Status](https://travis-ci.org/manoelcampos/CloudSimAutomation.png?branch=master)](https://travis-ci.org/manoelcampos/CloudSimAutomation)
==============================================================================================================

CloudSim Automation is a Java command line tool based on CloudSim and CloudReports classes that is able to read specifications of CloudSim simulation scenarios from a YAML file, a very human readable data format. Simulation scenarios can be written inside a YAML file and CloudSim Automation reads these simulation scenarios, creates and runs them on CloudSim.  
The tool releases researchers from needing to write Java code just to run simulation scenarios. By this way, the attention can be focused on the problem to be solved, such as creation of new algorithms for load balancing, new virtual machine scheduling policies, VM placement, resource provisioning, workload prediction, server consolidation, energy efficiency, cost reduction and so on. 

The main contributions of this work are:

- to avoid programming on the creation of CloudSim simulation environments;
- to reduce learning curve on creation of CloudSim simulation scenarios;
- to facilitate and to automate CloudSim simulation environments creation;
- to use a human readable file format to specify cloud simulation scenarios and speed up such a simulation process phase;
- to allow reuse, extension and sharing of simulations scenarios.

# How to use the tool 

You can run the tool from a terminal using the following command:

```bash
java -jar [CloudSimAutomation-VERSION-jar-with-dependencies.jar](https://github.com/manoelcampos/CloudSimAutomation/releases/latest) PathToYamlSimulationScenarioFile
```

# Published Paper

For more information, read [this paper](paper_cloudsim_automation.pdf), that was published on the [Springer Lecture Notes in Computer Science Volume 8662](http://doi.org/10.1007/978-3-319-11167-4_34).

**If you are using this work for publishing a paper, please cite our paper above.**
