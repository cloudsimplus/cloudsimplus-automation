# CloudSim Plus Automation: Human-Readable Scenario Specification Tool for Automated Creation of Simulations on CloudSim and CloudSim Plus <a href="https://buymeacoff.ee/manoelcampos" target="_blank"><img src="https://www.buymeacoffee.com/assets/img/custom_images/orange_img.png" alt="Buy Me A Coffee" style="height: 30px !important;width: 174px !important;box-shadow: 0px 3px 2px 0px rgba(190, 190, 190, 0.5) !important;-webkit-box-shadow: 0px 3px 2px 0px rgba(190, 190, 190, 0.5) !important;" ></a>

[![Build Status](https://github.com/manoelcampos/cloudsim-plus-automation/actions/workflows/maven.yml/badge.svg)](https://github.com/manoelcampos/cloudsim-plus-automation/actions/workflows/maven.yml) [![Maven Central](https://img.shields.io/maven-central/v/org.cloudsimplus/cloudsim-plus-automation.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22org.cloudsimplus%22%20AND%20a:%22cloudsim-plus-automation%22) [![Javadocs](https://www.javadoc.io/badge/org.cloudsimplus/cloudsim-plus-automation.svg)](https://www.javadoc.io/doc/org.cloudsimplus/cloudsim-plus-automation) [![GPL licensed](https://img.shields.io/badge/license-GPL-blue.svg)](http://www.gnu.org/licenses/gpl-3.0)

## 1. Introduction

**CloudSim Plus Automation** is a Java 17+ command line tool based on [CloudSim Plus](http://cloudsimplus.org) 
(and some [CloudReports](https://github.com/thiagotts/CloudReports) classes) 
which is able to read specifications of CloudSim Plus simulation scenarios from a YAML file, 
a very human-readable data format. 
Simulation scenarios can be written inside a YAML file and CloudSim Plus Automation reads these simulation scenarios, creates and runs them on CloudSim Plus.  

The tool releases researchers from writing Java code just to run simulation scenarios. 
This way, the attention can be focused on the problem to be solved, such as the creation of new algorithms for load balancing, 
new virtual machine scheduling policies, VM placement, resource provisioning, workload prediction, server consolidation, 
energy efficiency, cost reduction and so on. 

A snippet of an YAML file used to automate the creation of CloudSim Plus simulation scenarios is presented below. 
Check a complete example in some files such as the [CloudEnvironment1.yml](CloudEnvironment1.yml).

```yml
datacenters:
  - !datacenter
    amount: 1
    vmAllocationPolicy: Simple
    hosts:
      - !host
        amount: 8
        ram: 1000000
        bw: 100000
        storage: 40000
        pes: 4
        mips: 50000
        vmScheduler: TimeShared
        ramProvisioner: Simple
        bwProvisioner: Simple
        peProvisioner: Simple
customers:
  - !customer
    amount: 4
    vms:
      - !vm
        amount: 4
        size: 500
        pes: 2
        mips: 1000
        ram: 2000
        bw: 1000
        cloudletScheduler: SpaceShared
    cloudlets:
      - !cloudlet
        amount: 8
        pes: 2
        length: 1000
        fileSize: 50
        outputSize: 70
        utilizationModelCpu: Full
        utilizationModelRam: Full
        utilizationModelBw: Full
      - !cloudlet
        amount: 8
        pes: 2
        length: 2000
        fileSize: 50
        outputSize: 70
        utilizationModelCpu: Full
        utilizationModelRam: Full
        utilizationModelBw: Full
```

This work contributes to:

- avoid programming on the creation of CloudSim Plus simulation environments;
- reduce learning curve on creation of CloudSim Plus simulation scenarios;
- facilitate and automate CloudSim Plus simulation environments creation;
- use a human readable file format to specify cloud simulation scenarios and speed up such a simulation process phase;
- allow reuse, extension and sharing of simulations scenarios.

## 2. Requirements

In order to build the jar file to run the tool, you need JDK 17+ installed.
You can use any IDE of your choice or run maven at the command line:

```bash
mvn clean install
```

## 3. Using the command line tool 

You can simply download the [jar file from the latest release](https://github.com/manoelcampos/cloudsim-plus-automation/releases/latest) and run it in a terminal
by issuing the following command (check the correct version number of the jar file):

```bash
java -jar cloudsim-plus-automation-7.1.0-with-dependencies.jar PathToSimulationScenario.yml
```

Execute the tool without any parameter to see the usage help.

## 4. Using it as a maven dependency into your own project

You can build your own applications on top of CloudSim Plus Automation to automate the creation cloud computing simulations.
This way, your applications will be able to read simulation scenarios from YAML files, build and execute them on CloudSim Plus.
Just add CloudSim Plus Automation as a Maven dependency into your own project and start coding. 

```xml
<dependency>
    <groupId>org.cloudsimplus</groupId>
    <artifactId>cloudsim-plus-automation</artifactId>
    <!-- Set a specific version or use the latest one -->
    <version>LATEST</version>
</dependency>
```

You can programmatically load a YAML file containing simulation scenarios using some code such as the example below.
The complete example project is available [here](example).

```java
try {
    //Loads a YAML file containing 1 or more simulation scenarios.
    final YamlCloudScenarioReader reader = new YamlCloudScenarioReader("PATH TO YOUR YAML FILE");
    //Gets the list or parsed scenarios.
    final List<YamlCloudScenario> simulationScenarios = reader.getScenarios();
    //For each existing scenario, creates and runs it in CloudSim Plus, printing results.
    for (YamlCloudScenario scenario : simulationScenarios) {
        new CloudSimulation(scenario).run();
    }
} catch (FileNotFoundException | YamlException e) {
    System.err.println("Error when trying to load the simulation scenario from the YAML file: "+e.getMessage());
}
```

## 5. Published Paper

For more information, read the paper published on the [Springer Lecture Notes in Computer Science Volume 8662](http://doi.org/10.1007/978-3-319-11167-4_34). Realize the paper is related to an older version of the tool, which is compatible with CloudSim 3. 
The YAML structure has changed since there too, making it simpler and matching the name of entries with CloudSim and CloudSim Plus classes (such as VmAllocationPolicy, VmScheduler, CloudletScheduler). See the last section for more information.

**If you are using this work for publishing a paper, please cite our paper above.**

## 6. Notice

If you are looking for the **CloudSim Automation**, 
which is the version compatible with [CloudSim 4](http://github.com/Cloudslab/cloudsim), 
it is available at [cloudsim-version](https://github.com/manoelcampos/cloudsim-plus-automation/tree/cloudsim-version) branch. 
However, that version is not supported anymore.
