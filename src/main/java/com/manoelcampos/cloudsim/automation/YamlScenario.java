/*
 * CloudSim Plus Automation: A Human Readable Scenario Specification for Automated Creation of Simulations on CloudSim Plus.
 * https://github.com/manoelcampos/CloudSimAutomation
 *
 *     Copyright (C) 2015-2016  Universidade da Beira Interior (UBI, Portugal) and
 *     the Instituto Federal de Educação Ciência e Tecnologia do Tocantins (IFTO, Brazil).
 *
 *     This file is part of CloudSim Automation.
 *
 *     CloudSim Plus Automation is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     CloudSim Plus Automation is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with CloudSim Automation. If not, see <http://www.gnu.org/licenses/>.
 */
package com.manoelcampos.cloudsim.automation;

import cloudreports.models.*;
import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicy;
import org.cloudbus.cloudsim.brokers.DatacenterBroker;
import org.cloudbus.cloudsim.brokers.DatacenterBrokerSimple;
import org.cloudbus.cloudsim.cloudlets.Cloudlet;
import org.cloudbus.cloudsim.cloudlets.CloudletSimple;
import org.cloudbus.cloudsim.datacenters.Datacenter;
import org.cloudbus.cloudsim.datacenters.DatacenterCharacteristics;
import org.cloudbus.cloudsim.datacenters.DatacenterCharacteristicsSimple;
import org.cloudbus.cloudsim.datacenters.DatacenterSimple;
import org.cloudbus.cloudsim.hosts.Host;
import org.cloudbus.cloudsim.hosts.HostSimple;
import org.cloudbus.cloudsim.provisioners.ResourceProvisioner;
import org.cloudbus.cloudsim.resources.FileStorage;
import org.cloudbus.cloudsim.resources.Pe;
import org.cloudbus.cloudsim.resources.PeSimple;
import org.cloudbus.cloudsim.resources.SanStorage;
import org.cloudbus.cloudsim.schedulers.cloudlet.CloudletScheduler;
import org.cloudbus.cloudsim.schedulers.vm.VmScheduler;
import org.cloudbus.cloudsim.util.Log;
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModel;
import org.cloudbus.cloudsim.vms.Vm;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.provisioners.PeProvisioner;
import org.cloudbus.cloudsim.vms.VmSimple;
import org.cloudsimplus.builders.tables.CloudletsTableBuilderHelper;

import java.util.*;

/**
 * Creates a cloud environment autonomously from a YAML file.
 * The application has a command line interface that accept
 * the YAML file name as parameter.
 * Each scenario specified into a YAML file
 * will be represented by an object of this class.
 * Each scenario inside the file
 * can be delimited using the line below:
 * --- #Delimits each Cloud Environment
 *
 * @author Manoel Campos da Silva Filho
 */
public class YamlScenario {
    /**
     * Abstract information about data centers.
     * This object contains, for instance, the amount of datacenters
     * to be created and the host amount and configurations.
     * Each YAML scenario can have multiple datacenters
     * that are abstractly specified using DatacenterRegistry
     * objects. The concrete datacenters are
     * created by CloudSim.
     */
    private List<DatacenterRegistry> datacenterRegistries = new ArrayList<>();

    /**
     * Abstract information about customers (brokers).
     * This object contains, for instance, the amount of customers
     * to be created and the VM amount and configurations.
     * Each YAML scenario can have multiple customers
     * that are abstractly specified using CustomerRegistry
     * objects. The concrete customers are
     * created by CloudSim as its DatacenterBroker objects.
     */
    private List<CustomerRegistry> customerRegistries = new ArrayList<>();

    /**
     * Concrete cloudlet list.
     * The list of cloudlets (applications) for each customer (Broker)
     * to be run at CloudSim.
     */
    private Map<DatacenterBroker, List<Cloudlet>> brokerCloudlets;

    /**
     * Concrete VM list.
     * The list of Virtual Machines (VMs) for each customer (Broker)
     * to be instantiated at CloudSim.
     */
    private Map<DatacenterBroker, List<Vm>> brokerVms;

    /**
     * Concrete data center list.
     * The list of datacenters to be created at CloudSim, obtained from the
     * abstract DatacenterRegistry information from the YAML file.
     */
    private List<Datacenter> datacenters = null;
    
    private CloudSim cloudsim;

    /**
     * @return the datacenterRegistries
     */
    public List<DatacenterRegistry> getDatacenterRegistries() {
        return datacenterRegistries;
    }

    /**
     * @param datacenterRegistries the datacenterRegistries to set
     */
    public void setDatacenterRegistries(final List<DatacenterRegistry> datacenterRegistries) {
        this.datacenterRegistries = datacenterRegistries;
    }

    /**
     * @return the customerRegistries
     */
    public List<CustomerRegistry> getCustomerRegistries() {
        return customerRegistries;
    }

    /**
     * @param customerRegistries the customerRegistries to set
     */
    public void setCustomerRegistries(final List<CustomerRegistry> customerRegistries) {
        this.customerRegistries = customerRegistries;
    }

    /**
     * Creates the concrete Datacenter list from the abstract
     * DatacenterRegistry list.
     *
     * @return Returns the list of created CloudSim datacenters
     * @throws IllegalArgumentException Throws when the method,
     *                            starting from the information at YAML file,
     *                            sets invalid parameters for CloudSim Datacenter objects
     *                            to be created.
     * @see YamlScenario#datacenterRegistries
     */
    private List<Datacenter> createConcreteDatacentersFromAbstractDatacenterRegistries()
            throws IllegalArgumentException
    {
        String datacenterName;
        int datacenterCount = 0;
        final List<Datacenter> list = new ArrayList<>();
        for (DatacenterRegistry dcr : datacenterRegistries) {
            int hostCount = 0;
            for (int i = 0; i < dcr.getAmount(); i++) {
                datacenterName = generateDataCenterName(dcr, ++datacenterCount);

                List<Host> hostList = createConcreteHostsFromAbstractHostRegistries(dcr, hostCount);
                hostCount += hostList.size();

                try {
                    Datacenter dc = createDataCenter(datacenterName, dcr, hostList);
                    list.add(dc);
                } catch (Exception e) {
                    e.printStackTrace(System.out);
                }
            }
        }

        return list;
    }

    public Datacenter createDataCenter(
            final String datacenterName, final DatacenterRegistry dcr,
            final List<Host> hostList) throws Exception
    {
        // 5. Create a DatacenterCharacteristics object that stores the
        //    properties of a data center: architecture, OS, list of
        //    Machines, allocation policy: time- or space-shared, time zone
        //    and its price (G$/Pe time unit).
        final double time_zone = 10.0;  // time zone this resource is located
        final DatacenterCharacteristics characteristics =
                createDatacenterCharacteristics(dcr, hostList, time_zone);

        final LinkedList<FileStorage> storageList = createSan(dcr);

        VmAllocationPolicy allocationPolicy = PolicyLoader.vmAllocationPolicy(dcr.getAllocationPolicyAlias());

        return new DatacenterSimple(cloudsim, characteristics, allocationPolicy)
                .setStorageList(storageList)
                .setSchedulingInterval(dcr.getSchedulingInterval());
    }

    public String generateDataCenterName(final DatacenterRegistry dcr, final int datacenterCount) {
        String datacenterName = dcr.getName();
        if (dcr.getName() == null || dcr.getName().trim().equals("")) {
            datacenterName = String.format("datacenter%d", datacenterCount);
        }

        return datacenterName;
    }

    /**
     * Creates a map containing the abstract customer information (CustomerRegistry)
     * used to create each concrete CloudSim customer (DatacenterBroker).
     *
     * @return Returns the map created.
     * @see YamlScenario#customerRegistries
     */
    private Map<DatacenterBroker, CustomerRegistry> createConcreteDatacenterBrokersFromAbstractCustomerRegistries() {
        final Map<DatacenterBroker, CustomerRegistry> list = new HashMap<>();
        int brokerCount = 0;
        for (CustomerRegistry cr : customerRegistries) {
            for (int i = 0; i < cr.getAmount(); i++) {
                try {
                    list.put(new DatacenterBrokerSimple(cloudsim), cr);
                } catch (Exception e) {
                    e.printStackTrace(System.out);
                    return null;
                }
            }

        }

        return list;
    }

    /**
     * Create the VM list for each customer represented by the brokers list.
     *
     * @return Returns the a map containing the list of created VMs
     * for each customer (DatacenterBroker).
     * @see YamlScenario#createConcreteDatacenterBrokersFromAbstractCustomerRegistries()
     */
    private Map<DatacenterBroker, List<Vm>> createConcreteVmListForAllBrokers(
            final Map<DatacenterBroker, CustomerRegistry> customerRegistries)
    {
        final Map<DatacenterBroker, List<Vm>> list = new HashMap<>();

        int vmCount = 0;
        for (DatacenterBroker broker : customerRegistries.keySet()) {
            List<Vm> vms = createConcreteVmListForOneBroker(broker, customerRegistries.get(broker), ++vmCount);
            list.put(broker, vms);
        }

        return list;
    }

    public List<Vm> createConcreteVmListForOneBroker(
            final DatacenterBroker broker,
            final CustomerRegistry customerRegistry,
            final int vmCount) throws RuntimeException
    {
        final List<Vm> list = new ArrayList<>();
        for (VirtualMachineRegistry vmr : customerRegistry.getVmList()) {
            for (int i = 0; i < vmr.getAmount(); i++) {
                list.add(createVm(vmCount, broker, vmr));
            }
        }

        return list;
    }

    public Vm createVm(final int vmCount,
                       final DatacenterBroker broker,
                       final VirtualMachineRegistry vmr) throws RuntimeException
    {
        CloudletScheduler scheduler = PolicyLoader.cloudletScheduler(vmr.getSchedulingPolicyAlias());
        return new VmSimple(vmCount, vmr.getMips(), vmr.getPesNumber())
                .setBroker(broker)
                .setRam(vmr.getRam())
                .setBw(vmr.getBw())
                .setSize(vmr.getSize())
                .setCloudletScheduler(scheduler);
    }

    /**
     * Create a map that stores the cloudlet list for each customer (broker).
     *
     * @param brokers The map containing the abstract customer information
     *                (CustomerRegistry) obtained from the YAML file, for each
     *                concrete customer created (DatacenterBroker).
     * @return Returns the list of Cloudlets created.
     */
    private Map<DatacenterBroker, List<Cloudlet>> createConcreteCloudletsFromAbstractUtilizationProfiles(
            final Map<DatacenterBroker, CustomerRegistry> brokers)
    {
        // Creates a container to store Cloudlets
        final Map<DatacenterBroker, List<Cloudlet>> list = new HashMap<>();

        int cloudletCount = 0;
        for (DatacenterBroker broker : brokers.keySet()) {
            List<Cloudlet> cloudlets = new ArrayList<>();
            for (UtilizationProfile up : brokers.get(broker).getUtilizationProfile()) {
                for (int i = 0; i < up.getNumOfCloudlets(); i++) {
                    cloudlets.add(createCloudlet(++cloudletCount, up, broker));
                }
            }
            list.put(broker, cloudlets);
        }

        return list;
    }

    public Cloudlet createCloudlet(
            final int cloudletCount,
            final UtilizationProfile up, final DatacenterBroker broker) throws RuntimeException
    {
        UtilizationModel cpuUtilization = PolicyLoader.utilizationModel(up.getUtilizationModelCpuAlias());
        UtilizationModel ramUtilization = PolicyLoader.utilizationModel(up.getUtilizationModelRamAlias());
        UtilizationModel bwUtilization = PolicyLoader.utilizationModel(up.getUtilizationModelBwAlias());

        return new CloudletSimple(cloudletCount, up.getLength(), up.getCloudletsPesNumber())
                    .setFileSize(up.getFileSize())
                    .setOutputSize(up.getOutputSize())
                    .setUtilizationModelCpu(cpuUtilization)
                    .setUtilizationModelRam(ramUtilization)
                    .setUtilizationModelBw(bwUtilization)
                    .setBroker(broker);
    }



    public String getHostIdOfVm(final Vm vm) {
        if (vm != null && vm.getHost() != null) {
            return String.valueOf(vm.getHost().getId());
        }

        return "";
    }

    /**
     * Create the concrete host list from the abstract DatacenterRegistry
     * object.
     *
     * @param datacenterRegistry A specific abstract datacenter information
     *                           get from the datacenterRegistries list at the YAML file.
     * @param hostCount          The global created host count for the
     *                           specific simulation scenario loaded. This is used to incrementally
     *                           assign an id for each host created that was not explicitly defined
     *                           on id at the YAML file.
     * @return Returns the list of created hosts from the specified datacenterRegistry.
     * @throws RuntimeException
     * @see YamlScenario#datacenterRegistries
     */
    private List<Host> createConcreteHostsFromAbstractHostRegistries(
            final DatacenterRegistry datacenterRegistry, int hostCount) throws RuntimeException
    {
        int hostId;
        final List<Host> hostList = new ArrayList<>();
        for (HostRegistry hr : datacenterRegistry.getHostList()) {
            for (int i = 0; i < hr.getAmount(); i++) {
                List<Pe> peList = createHostProcessingElements(hr);
                hostId = generateHostId(hr, ++hostCount);
                hostList.add(createHost(hostId, hr, peList));
            }
        }
        return hostList;
    }


    public Host createHost(final int hostId, final HostRegistry hr,
                           final List<Pe> peList)
            throws RuntimeException {
        ResourceProvisioner ramProvisioner =
                PolicyLoader.newRamProvisioner("Ram", hr.getRamProvisionerAlias(), hr.getRam());
        ResourceProvisioner bwProvisioner =
                PolicyLoader.newBwProvisioner("Bandwidth", hr.getBwProvisionerAlias(), hr.getBw());
        VmScheduler vmScheduler = PolicyLoader.vmScheduler(hr.getSchedulingPolicyAlias(), peList);

        return new HostSimple(hostId,hr.getStorage(), peList)
                .setRamProvisioner(ramProvisioner)
                .setBwProvisioner(bwProvisioner)
                .setVmScheduler(vmScheduler);
    }

    private int generateHostId(final HostRegistry hr, final int currentHostCount) {
        /*Necessary convertion due to CloudReports' HostRegistry uses long and
        the value is used in the CloudSim's Host class that uses int.
        */
        return (hr.getId() != 0 ? (int) hr.getId() : currentHostCount);
    }

    /**
     * Creates a CloudSim StorageSystem Area Network (SAN) from an abstract DatacenterRegistry
     * information, get from a YAML specification file.
     *
     * @param dcr A specific DatacenterRegistry information
     *            to be used to create the SANs.
     * @return Returns the SANs created list.
     * @throws IllegalArgumentException Throws when the method,
     *                            starting from the information at YAML file,
     *                            sets invalid parameters for CloudSim SAN objects
     *                            to be created.
     * @see YamlScenario#datacenterRegistries
     */
    private LinkedList<FileStorage> createSan(final DatacenterRegistry dcr) throws IllegalArgumentException {
        final LinkedList<FileStorage> list = new LinkedList<>();
        for (SanStorageRegistry sr : dcr.getSanList()) {
            SanStorage san = new SanStorage(sr.getCapacity(), sr.getBandwidth(), sr.getNetworkLatency());
            list.add(san);
        }

        return list;
    }

    /**
     * Create a DatacenterCharacteristics object
     * that represents that characteristics of one CloudSim Datacenter,
     * including the list of hosts of the Datacenter.
     *
     * @param dcr       The abstract DatacenterRegistry information, obtained from the YAML file,
     *                  to be used to define the Datacenter characteristics.
     * @param hostList  The list of hosts of the Datacenter
     * @param time_zone The timezone of the Datacenter.
     * @return Returns the DatacenterCharacteristics object created.
     * @see YamlScenario#createDatacenterCharacteristics(cloudreports.models.DatacenterRegistry, java.util.List, double)
     */
    private DatacenterCharacteristics createDatacenterCharacteristics(
            final DatacenterRegistry dcr, List<Host> hostList, final double time_zone)
    {
        return new DatacenterCharacteristicsSimple(hostList)
                .setArchitecture(dcr.getArchitecture())
                .setOs(dcr.getOs())
                .setVmm(dcr.getVmm())
                .setTimeZone(time_zone)
                .setCostPerSecond(dcr.getCostPerSec())
                .setCostPerMem(dcr.getCostPerMem())
                .setCostPerStorage(dcr.getCostPerStorage())
                .setCostPerBw(dcr.getCostPerBw());
    }

    /**
     * Creates a list of PEs (Processing Elements, i.e. CPUs/Cores)
     * for a host or VM.
     *
     * @param hr The abstract Host information obtained from the YAML file.
     * @return Returns the list of PEs created.
     */
    private List<Pe> createHostProcessingElements(final HostRegistry hr) {
        final List<Pe> list = new ArrayList<>();
        for (int j = 0; j < hr.getNumOfPes(); j++) {
            PeProvisioner peProvisioner =
                    PolicyLoader.newPeProvisioner(
                            "Pe", hr.getPeProvisionerAlias(), hr.getMipsPerPe());
            list.add(new PeSimple(j, peProvisioner));
        }
        return list;
    }

    /**
     * Searches for a VM, with the specified id, at the broker (that represents
     * the customer).
     *
     * @param broker The broker (customer) where to search for the VM
     * @param vmId   The desired VM to be located.
     * @return If the VM is found, returns it, otherwise, returns null.
     */
    private Vm findVm(final DatacenterBroker broker, final int vmId) {
        for (Vm vm : brokerVms.get(broker)) {
            if (vm.getId() == vmId) {
                return vm;
            }
        }
        return null;
    }

    /**
     * From the abstract simulation scenario, loaded from the YAML file,
     * creates the CloudSim concretes objects and runs the simulation
     * using CloudSim.
     *
     * @param simulationLabel A label to be used to identify the running
     *                        simulation scenario. Commonly this can be the name
     *                        of the loaded YAML file.
     * @throws IllegalArgumentException Throws when the method,
     *                            starting from the information at YAML file,
     *                            sets invalid parameters for CloudSim objects
     *                            to be created.
     */
    public void run(final String simulationLabel) throws IllegalArgumentException {
        cloudsim = new CloudSim();
        System.out.println("Hosts========================");
        this.datacenters = createConcreteDatacentersFromAbstractDatacenterRegistries();
        for (Datacenter datacenter : datacenters) {
            System.out.println(datacenter.getName() + ": " + datacenter.getHostList().size() + " hosts");
        }
        System.out.println("=============================");

        //Third step: Create Brokers
        final Map<DatacenterBroker, CustomerRegistry> brokers = createConcreteDatacenterBrokersFromAbstractCustomerRegistries();

        //Fourth step: Create VMs and Cloudlets and send them to broker
        this.brokerVms = createConcreteVmListForAllBrokers(brokers);
        this.brokerCloudlets = createConcreteCloudletsFromAbstractUtilizationProfiles(brokers);

        for (DatacenterBroker broker : brokers.keySet()) {
            broker.submitVmList(brokerVms.get(broker));
            broker.submitCloudletList(brokerCloudlets.get(broker));
        }

        cloudsim.start();

        final Map<DatacenterBroker, List<Cloudlet>> receivedCloudletList = new HashMap<>();
        // Final step: Print results when simulation is over
        for (DatacenterBroker broker : brokers.keySet()) {
            receivedCloudletList.put(broker, broker.getCloudletsFinishedList());
        }

        for (DatacenterBroker broker : brokers.keySet()) {
            new CloudletsTableBuilderHelper(broker.getCloudletsFinishedList())
                    .setTitle(broker.getName())
                    .build();
        }
        Log.printLine("\nCloud Simulation finished!");
    }


}
