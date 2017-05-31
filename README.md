# CloudSim Plus Automation: A Human Readable Scenario Specification for Automated Creation of Simulations on CloudSim [![Build Status](https://travis-ci.org/manoelcampos/cloudsim-plus-automation.png?branch=master)](https://travis-ci.org/manoelcampos/cloudsim-plus-automation) [![Dependency Status](https://www.versioneye.com/user/projects/58aeeecd0693850016ef1ed8/badge.svg?style=rounded-square)](https://www.versioneye.com/user/projects/58aeeecd0693850016ef1ed8) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.cloudsimplus/cloudsim-plus-automation/badge.svg)] [![Javadocs](https://www.javadoc.io/badge/org.cloudsimplus/cloudsim-plus-automation.svg)](https://www.javadoc.io/doc/org.cloudsimplus/cloudsim-plus-automation) [![GPL licensed](https://img.shields.io/badge/license-GPL-blue.svg)](http://www.gnu.org/licenses/gpl-3.0)

**CloudSim Plus Automation** is a Java command line tool based on [CloudSim Plus](http://cloudsimplus.org) and [CloudReports](https://github.com/thiagotts/CloudReports) classes which is able to read specifications of CloudSim Plus simulation scenarios from a YAML file, a very human readable data format. Simulation scenarios can be written inside a YAML file and CloudSim Plus Automation reads these simulation scenarios, creates and runs them on CloudSim Plus.  

The tool releases researchers from writing Java code just to run simulation scenarios. This way, the attention can be focused on the problem to be solved, such as the creation of new algorithms for load balancing, new virtual machine scheduling policies, VM placement, resource provisioning, workload prediction, server consolidation, energy efficiency, cost reduction and so on. 

A snippet of an YAML file used to automate the creation of CloudSim Plus simulation scenarios is presented below:

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
    amount: 2
    vms:
      - !vm
        amount: 2
        size: 500
        pes: 4
        mips: 1000
        ram: 2000
        bw: 1000
        cloudletScheduler: SpaceShared
    cloudlets:
      - !cloudlet
        amount: 6
        pes: 2
        length: 100
        fileSize: 50
        outputSize: 70
        utilizationModelCpu: Full
        utilizationModelRam: Full
        utilizationModelBw: Full
```

The main contributions of this work are:

- to avoid programming on the creation of CloudSim Plus simulation environments;
- to reduce learning curve on creation of CloudSim Plus simulation scenarios;
- to facilitate and automate CloudSim Plus simulation environments creation;
- to use a human readable file format to specify cloud simulation scenarios and speed up such a simulation process phase;
- to allow reuse, extension and sharing of simulations scenarios.

# Using the command line tool 

You can simply download the [jar file from the latest release](https://github.com/manoelcampos/cloudsim-plus-automation/releases/latest) and run it in a terminal
by issuing the following command (check the correct version number of the jar file):

```bash
java -jar cloudsim-plus-automation-1.2.1-with-dependencies.jar PathToYamlSimulationScenarioFile
```

# Using it as a maven dependency into your own project
You can build your own applications on top of CloudSim Plus Automation to automate the creation cloud computing simulations.
This way, your applications will be able to read simulation scenarios from YAML files, build and execute them on CloudSim Plus.
Just add CloudSim Plus Automation as a Maven dependency into your own project and start coding:

```xml
<dependency>
  <groupId>org.cloudsimplus</groupId>
  <artifactId>cloudsim-plus-automation</artifactId>
  <version>1.2.1</version>
</dependency>
```

# Published Paper

For more information, read the paper published on the [Springer Lecture Notes in Computer Science Volume 8662](http://doi.org/10.1007/978-3-319-11167-4_34). Realize that the paper is for the older version of the tool, which is compatible with CloudSim 3. 
The YAML structure have changed since there too, making it simpler and matching the name of entries with CloudSim and CloudSim Plus classes (such as VmAllocationPolicy, VmScheduler, CloudletScheduler). See the last section for more information.

**If you are using this work for publishing a paper, please cite our paper above.**

# Notice

If you are looking for the **CloudSim Automation**, which is the version compatible with [CloudSim 4](http://github.com/Cloudslab/cloudsim), it is available at [cloudsim-version](https://github.com/manoelcampos/cloudsim-plus-automation/tree/cloudsim-version) branch. However, that is not an actively maintained version anymore.
