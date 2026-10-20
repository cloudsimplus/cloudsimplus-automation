# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Overview

CloudSim Plus Automation is a Java 17 command line tool (and library) that reads cloud simulation scenarios from a YAML file, then builds and runs them on [CloudSim Plus](https://cloudsimplus.org). Users describe datacenters, hosts, SANs, customers (brokers), VMs and cloudlets in YAML instead of writing Java code. See `CloudEnvironment*.yml` at the root for sample scenarios.

## Commands

```bash
./mvnw clean install                 # build; produces target/cloudsimplus-automation-<ver>.jar and the uber jar *-with-dependencies.jar
./mvnw -B package                    # what CI runs (.github/workflows/maven.yml)
./mvnw license:format                # add missing GPL headers (COPYRIGHT template)
```

- Run the tool: `java -jar target/cloudsimplus-automation-<version>-with-dependencies.jar [-v] [-s] [-c] [-h] CloudEnvironment1.yml`
  (`-v` enables CloudSim Plus log, `-s` suppresses results tables, `-c` prints the scenario config; no args shows usage).
- Run the programmatic example: `org.cloudsimplus.automation.examples.Example1` (loads `src/main/resources/CloudEnvironment1.yml`).
- Tests: there are none (`src/test/java` is empty). If you add tests, add a JUnit dependency and Surefire to `pom.xml` first; then a single test runs with `./mvnw test -Dtest=ClassName#method`.
- Lint: no Checkstyle/Spotless. The `maven-license-plugin` `check` goal runs in `process-sources` with `strictCheck`, so a new `.java` file outside `cloudreports/models` without the header from `COPYRIGHT` fails the build. Run `./mvnw license:format` to fix it.
- Release: `mvn clean deploy -P sonatype` (`deploy.sh`), or automatic via `.github/workflows/deploy.yml` when a `v*.*.*` tag is pushed. The profile adds sources, javadoc, GPG signing and Nexus staging.

## Dependency on CloudSim Plus

- The `cloudsimplus` dependency version is `${project.version}`: this project's version **must match** a released CloudSim Plus version (currently `8.5.1`). Bumping the project version bumps the framework version too.
- The sibling directory `../cloudsimplus` is at `9.0.0-SNAPSHOT`, so it is **not** what this build uses. Maven resolves `8.5.1` from Central / `~/.m2`. To build against local framework changes, `mvn install` the sibling and align the versions.
- Other deps: `yamlbeans` (YAML to bean mapping) and `commons-cli`.

## Architecture

Flow: `Start` (CLI, commons-cli) -> `YamlCloudScenarioReader` -> list of `YamlCloudScenario` -> `CloudSimulation.run()` for each one.

- **YAML mapping**: `YamlCloudScenarioReader` registers the YAML tags `!datacenter`, `!customer`, `!san`, `!host`, `!vm`, `!cloudlet` to the `cloudreports.models.*Registry` beans. A single file can hold several YAML documents, and each one is a separate scenario. YAML keys map to bean setters through reflection, so adding a new YAML field means adding a property (getter/setter) to the matching `*Registry` class and then using it in `CloudSimulation`.
- **Registry classes** (`cloudreports.models`) were taken from the CloudReports project. They are plain descriptors with an `amount` field: one entry creates `amount` copies (for example, `customer.amount` brokers, each with `vm.amount` VMs). They are excluded from the license header check.
- **`CloudSimulation`** turns the registries into real CloudSim Plus objects (`DatacenterSimple`, `HostSimple`, `VmSimple`, `CloudletSimple`, `DatacenterBrokerSimple`, SAN storage, cost characteristics). It submits VMs and cloudlets per broker, starts `CloudSimPlus` and prints a `CloudletsTableBuilder` table per broker.
- **`PolicyLoader`** builds policy objects by reflection from short names in the YAML. It builds the class name as `org.cloudsimplus.<package>.<Prefix><Suffix>`, for example `vmAllocationPolicy: Simple` -> `org.cloudsimplus.allocationpolicies.VmAllocationPolicySimple`, and `cloudletScheduler: SpaceShared` -> `org.cloudsimplus.schedulers.cloudlet.CloudletSchedulerSpaceShared`. The same applies to `VmScheduler`, `UtilizationModel` and provisioners. Only classes with a public no-arg constructor work. If CloudSim Plus renames or moves these packages/classes, scenarios break at runtime, not at compile time.
- Known detail: `CloudSimulation.createHost` builds both the RAM and BW provisioners from `hr.getBwProvisioner()`, so the YAML `ramProvisioner` value is currently ignored.

## Conventions

- `.editorconfig`: 4-space indentation for Java/XML, LF line endings, UTF-8.
- The `paper/` dir and the `cloudsim-version` tag relate to the older CloudSim 3/4 version of the tool (no longer supported). Don't use them as a reference for the current YAML format.
