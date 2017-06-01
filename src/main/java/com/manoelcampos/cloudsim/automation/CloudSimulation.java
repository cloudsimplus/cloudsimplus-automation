/*
 * CloudSim Automation: A Human Readable Scenario Specification for Automated Creation of Simulations on CloudSim.
 * https://github.com/manoelcampos/CloudSimAutomation
 *
 *     Copyright (C) 2015-2016  Universidade da Beira Interior (UBI, Portugal) and
 *     the Instituto Federal de Educação Ciência e Tecnologia do Tocantins (IFTO, Brazil).
 *
 *     This file is part of CloudSim Automation.
 *
 *     CloudSim Automation is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     CloudSim Automation is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with CloudSim Automation. If not, see <http://www.gnu.org/licenses/>.
 */
package com.manoelcampos.cloudsim.automation;

import cloudreports.models.*;
import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.provisioners.PeProvisioner;

import java.util.*;

import static java.util.Comparator.comparingInt;

/**
 * Represents a simulation created in CloudSim.
 * @author Manoel Campos da Silva Filho
 */
public class CloudSimulation {
    private final YamlCloudScenario scenario;
    /**
     * A label to be used to identify the running
     * simulation scenario. Commonly this can be the name
     * of the loaded YAML file.
     */
    private final String label;
    /**
     * Concrete cloudlet list.
     * The list of cloudlets (applications) for each customer (Broker)
     * to be run at CloudSim.
     */
    Map<DatacenterBroker, List<Cloudlet>> brokerCloudlets;

    /**
     * Concrete VM list.
     * The list of Virtual Machines (VMs) for each customer (Broker)
     * to be instantiated at CloudSim.
     */
    Map<DatacenterBroker, List<Vm>> brokerVms;

    List<Datacenter> datacenters;
    private boolean logEnabled;
    private boolean printScenariosConfiguration;
    private boolean showResults;

    public CloudSimulation(YamlCloudScenario scenario) {
        this(scenario, "");
    }

    public CloudSimulation(YamlCloudScenario scenario, String label) {
        this.scenario = scenario;
        this.brokerVms = new HashMap<>();
        this.brokerCloudlets = new HashMap<>();
        this.label = label;
        this.logEnabled = false;
        this.showResults = true;
        this.printScenariosConfiguration = true;
    }

    /**
     * Creates the concrete Datacenter list from the abstract
     * DatacenterRegistry list.
     *
     * @return Returns the list of created CloudSim datacenters
     * @throws ParameterException Throws when the method,
     *                            starting from the information at YAML file,
     *                            sets invalid parameters for CloudSim Datacenter objects
     *                            to be created.
     * @see YamlCloudScenario#getDatacenters()
     */
    List<Datacenter> createDatacentersFromDatacenterRegistries()
            throws ParameterException {
        String datacenterName;
        int datacenterCount = 0;
        final List<Datacenter> list = new ArrayList<Datacenter>();
        for (DatacenterRegistry dcr : scenario.getDatacenters()) {
            int hostCount = 0;
            for (int i = 0; i < dcr.getAmount(); i++) {
                datacenterName = generateDataCenterName(dcr, ++datacenterCount);

                List<Host> hostList = createHostsFromHostRegistries(dcr, hostCount);
                hostCount += hostList.size();

                try {
                    Datacenter datacenter =
                            createDataCenter(datacenterName, dcr, hostList);
                    list.add(datacenter);
                } catch (Exception e) {
                    e.printStackTrace(System.out);
                }
            }
        }

        return list;
    }

    public Datacenter createDataCenter(
            final String datacenterName, final DatacenterRegistry dcr,
            final List<Host> hostList) throws Exception {
        final double time_zone = 10.0;  // time zone this resource is located
        final DatacenterCharacteristics characteristics =
                createDatacenterCharacteristics(dcr, hostList, time_zone);

        final LinkedList<Storage> storageList = createSan(dcr);

        return
                new Datacenter(
                        datacenterName, characteristics,
                        PolicyLoader.vmAllocationPolicy(dcr.getVmAllocationPolicy(), hostList),
                        storageList, dcr.getSchedulingInterval());
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
     * @see YamlCloudScenario#getDatacenters()
     */
    Map<DatacenterBroker, CustomerRegistry> createDatacenterBrokersFromCustomerRegistries() {
        final Map<DatacenterBroker, CustomerRegistry> list = new HashMap<DatacenterBroker, CustomerRegistry>();
        int brokerCount = 0;
        String brokerName;
        for (CustomerRegistry cr : scenario.getCustomers()) {
            for (int i = 0; i < cr.getAmount(); i++) {
                brokerName = generateBrokerName(cr, ++brokerCount);
                try {
                    list.put(new DatacenterBroker(brokerName), cr);
                } catch (Exception e) {
                    e.printStackTrace(System.out);
                    return null;
                }
            }

        }

        return list;
    }

    public String generateBrokerName(final CustomerRegistry cr, final int brokerCount) {
        String brokerName = cr.getName();
        if (brokerName == null || brokerName.trim().equals("")) {
            brokerName = String.format("broker%d", brokerCount);
        }
        return brokerName;
    }

    /**
     * Create the VM list for each customer represented by the brokers list.
     *
     * @return Returns the a map containing the list of created VMs
     * for each customer (DatacenterBroker).
     * @see #createDatacenterBrokersFromCustomerRegistries()
     */
    Map<DatacenterBroker, List<Vm>> createVmListForAllBrokers(
            final Map<DatacenterBroker, CustomerRegistry> brokers) {
        final Map<DatacenterBroker, List<Vm>> list = new HashMap<DatacenterBroker, List<Vm>>();

        int vmCount = 0;
        for (DatacenterBroker broker : brokers.keySet()) {
            LinkedList<Vm> vms =
                    createVmListForOneBroker(
                            broker, brokers.get(broker), vmCount++);
            list.put(broker, vms);
        }

        return list;
    }

    public LinkedList<Vm> createVmListForOneBroker(
            final DatacenterBroker broker,
            final CustomerRegistry customerRegistry,
            final int vmCount) throws RuntimeException {
        final LinkedList<Vm> list = new LinkedList<Vm>();
        for (VmRegistry vmr : customerRegistry.getVms()) {
            for (int i = 0; i < vmr.getAmount(); i++) {
                list.add(createVm(vmCount + i, broker, vmr));
            }
        }
        return list;
    }

    public Vm createVm(final int vmCount,
                       final DatacenterBroker broker,
                       final VmRegistry vmr) throws RuntimeException {
        return new Vm(
                vmCount, broker.getId(), vmr.getMips(), vmr.getPes(),
                vmr.getRam(), vmr.getBw(), vmr.getSize(),
                vmr.getVmm(),
                PolicyLoader.cloudletScheduler(vmr.getCloudletScheduler()));
    }

    /**
     * Create a map that stores the cloudlet list for each customer (broker).
     *
     * @param brokers The map containing the abstract customer information
     *                (CustomerRegistry) obtained from the YAML file, for each
     *                concrete customer created (DatacenterBroker).
     * @return Returns the list of Cloudlets created.
     */
    Map<DatacenterBroker, List<Cloudlet>> createCloudletsFromUtilizationProfiles(final Map<DatacenterBroker, CustomerRegistry> brokers) {
        // Creates a container to store Cloudlets
        final Map<DatacenterBroker, List<Cloudlet>> list = new HashMap<DatacenterBroker, List<Cloudlet>>();

        int cloudletCount = 0;
        for (DatacenterBroker broker : brokers.keySet()) {
            List<Cloudlet> cloudlets = new ArrayList<Cloudlet>();
            for (CloudletRegistry up : brokers.get(broker).getCloudlets()) {
                for (int i = 0; i < up.getAmount(); i++) {
                    cloudlets.add(createCloudlet(++cloudletCount, up, broker));
                }
            }
            list.put(broker, cloudlets);
        }

        return list;
    }

    public Cloudlet createCloudlet(
            final int cloudletCount,
            final CloudletRegistry up, final DatacenterBroker broker) throws RuntimeException {
        final Cloudlet cloudlet =
                new Cloudlet(cloudletCount, up.getLength(),
                        up.getPes(),
                        up.getFileSize(), up.getOutputSize(),
                        PolicyLoader.utilizationModel(up.getUtilizationModelCpu()),
                        PolicyLoader.utilizationModel(up.getUtilizationModelRam()),
                        PolicyLoader.utilizationModel(up.getUtilizationModelBw())
                );
        // setting the owner of the Cloudlet
        cloudlet.setUserId(broker.getId());
        return cloudlet;
    }

    /**
     * Prints, at the standard output, the Cloudlet list for the specified
     * customer (DatacenterBroker).
     *
     * @param broker Customer owning the VMs
     * @param list   list of Cloudlets to be printed.
     */
    void printCloudletList(final DatacenterBroker broker, final List<Cloudlet> list) {
        final int size = list.size();
        Cloudlet cloudlet;

        Log.printLine();
        final String[] captions = {
                "Cloudlet", "Status ", "DC", "Host", "Host PEs ", "VM", "VM PEs   ",
                "CloudletLen", "CloudletPEs", "StartTime", "FinishTime", "ExecTime"};
        final String[] units = new String[]{
                "ID", "", "ID", "ID", "CPU cores", "ID", "CPU cores", "MI", "CPU cores", "Seconds", "Seconds", "Seconds"};

        LogUtils.printCentralizedString(captions,"DatacenterBroker " + broker.getId());
        LogUtils.printCaptions(captions);
        LogUtils.printLine(captions, units);
        LogUtils.printLineSeparator(captions);

        for (int i = 0; i < size; i++) {
            cloudlet = list.get(i);
            if (cloudlet.getStatus() == Cloudlet.SUCCESS) {
                Vm vm = findVm(broker, cloudlet.getVmId());
                final Object[] data = {
                        cloudlet.getCloudletId(), cloudlet.getCloudletStatusString(),
                        cloudlet.getResourceId(),
                        getHostId(vm), getHostPes(vm),
                        vm.getId(), vm.getNumberOfPes(),
                        cloudlet.getCloudletLength(), cloudlet.getNumberOfPes(),
                        (long)cloudlet.getExecStartTime(),
                        (long)cloudlet.getFinishTime(),
                        (long)cloudlet.getActualCPUTime()};

                LogUtils.printLine(captions, data);
            }
        }
        LogUtils.printLineSeparator(captions);
    }

    public Host getHost(final Vm vm) {
        if (vm != null && vm.getHost() != null) {
            return vm.getHost();
        }

        return null;
    }

    public String getHostId(final Vm vm) {
        Host h = getHost(vm);
        if(h == null) {
            return "";
        }

        return String.valueOf(h.getId());
    }

    public String getHostPes(final Vm vm) {
        Host h = getHost(vm);
        if(h == null) {
            return "";
        }

        return String.valueOf(h.getNumberOfPes());
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
     * @see YamlCloudScenario#getDatacenters()
     */
    List<Host> createHostsFromHostRegistries(
            final DatacenterRegistry datacenterRegistry, int hostCount) throws RuntimeException {
        int hostId;
        final List<Host> hostList = new ArrayList<Host>();
        for (HostRegistry hr : datacenterRegistry.getHosts()) {
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
        return new Host(
                hostId,
                PolicyLoader.newRamProvisioner("Ram", hr.getRamProvisioner(), hr.getRam()),
                PolicyLoader.newBwProvisioner("Bw", hr.getBwProvisioner(), hr.getBw()),
                hr.getStorage(), peList,
                PolicyLoader.vmScheduler(hr.getVmScheduler(), peList)
        );
    }

    int generateHostId(final HostRegistry hr, final int currentHostCount) {
        return (hr.getId() != 0 ? hr.getId() : currentHostCount);
    }

    /**
     * Creates a CloudSim StorageSystem Area Network (SAN) from an abstract DatacenterRegistry
     * information, get from a YAML specification file.
     *
     * @param dcr A specific DatacenterRegistry information
     *            to be used to create the SANs.
     * @return Returns the SANs created list.
     * @throws ParameterException Throws when the method,
     *                            starting from the information at YAML file,
     *                            sets invalid parameters for CloudSim SAN objects
     *                            to be created.
     * @see YamlCloudScenario#getDatacenters()
     */
    LinkedList<Storage> createSan(final DatacenterRegistry dcr) throws ParameterException {
        final LinkedList<Storage> list = new LinkedList<Storage>();
        for (SanStorageRegistry sr : dcr.getSans()) {
            list.add(
                    new SanStorage(
                            sr.getCapacity(), sr.getBandwidth(), sr.getNetworkLatency()));
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
     * @see #createDatacenterCharacteristics(DatacenterRegistry, List, double)
     */
    DatacenterCharacteristics createDatacenterCharacteristics(
            final DatacenterRegistry dcr, List<Host> hostList, final double time_zone) {
        return
                new DatacenterCharacteristics(
                        dcr.getArchitecture(), dcr.getOs(), dcr.getVmm(),
                        hostList, time_zone,
                        dcr.getCostPerSec(), dcr.getCostPerMem(),
                        dcr.getCostPerStorage(), dcr.getCostPerBw());
    }

    /**
     * Creates a list of PEs (Processing Elements, i.e. CPUs/Cores)
     * for a host or VM.
     *
     * @param hr The abstract Host information obtained from the YAML file.
     * @return Returns the list of PEs created.
     */
    List<Pe> createHostProcessingElements(final HostRegistry hr) {
        final List<Pe> list = new ArrayList<Pe>();
        for (int j = 0; j < hr.getPes(); j++) {
            PeProvisioner peProvisioner =
                    PolicyLoader.newPeProvisioner(
                            "Pe", hr.getPeProvisioner(), hr.getMips());
            list.add(new Pe(j, peProvisioner));
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
    Vm findVm(final DatacenterBroker broker, final int vmId) {
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
     */
    public void run() {
        final double startTime = System.currentTimeMillis();
        int num_user = 1;   // number of cloud customers
        final Calendar calendar = Calendar.getInstance(Locale.getDefault());

        Log.setDisabled(!logEnabled);
        CloudSim.init(num_user, calendar, logEnabled);
        printScenariosConfiguration();

        final Map<DatacenterBroker, CustomerRegistry> brokers = createDatacenterBrokersFromCustomerRegistries();

        this.brokerVms = createVmListForAllBrokers(brokers);
        this.brokerCloudlets = createCloudletsFromUtilizationProfiles(brokers);

        for (DatacenterBroker broker : brokers.keySet()) {
            broker.submitVmList(brokerVms.get(broker));
            broker.submitCloudletList(brokerCloudlets.get(broker));
        }

        CloudSim.startSimulation();

        CloudSim.stopSimulation();

        if(showResults) {
            Log.enable();
            Log.printLine("\n============================Results: " + label);
            for (DatacenterBroker broker : brokers.keySet()) {
                List<Cloudlet> list = broker.getCloudletReceivedList();
                list.sort(comparingInt((Cloudlet c) -> c.getVmId()).thenComparingInt(Cloudlet::getCloudletId));
                printCloudletList(broker, list);
            }
        }

        final double finishTimeSecs = (System.currentTimeMillis() - startTime) / 1000;
        printFinalResults(finishTimeSecs);
    }

    private void printScenariosConfiguration() {
        if(!isPrintScenariosConfiguration()){
            return;
        }

        System.out.println("Hosts========================");
        try {
            this.datacenters = createDatacentersFromDatacenterRegistries();
        } catch (ParameterException e) {
            throw new RuntimeException(e);
        }
        for (Datacenter datacenter : datacenters) {
            System.out.println(datacenter.getName() + ": " + datacenter.getHostList().size() + " hosts");
        }
        System.out.println("=============================");
    }

    private void printFinalResults(final double finishTimeSecs) {
        Log.enable();
        LogUtils.setColSeparator(";");
        final String[] captions =
                {"Framework    ", "Simulation time (seconds)", "Simulation time (minutes)", "Simulation time (hours)",
                        "Datacenters", "Hosts from all DCs", "VMs from all Customers", "Cloudlets from all Customers"};

        LogUtils.printCaptions(captions);
        LogUtils.printLine(captions,
                "CloudSim     ",
                finishTimeSecs,
                String.format("%.4f", finishTimeSecs/60.0),
                String.format("%.6f", finishTimeSecs/3600.0),
                getNumDatacenters(),
                getNumHostsFromAllDatacenters(),
                getNumVmsFromAllCustomers(),
                getNumCloudletsFromAllCustomers());
    }

    private int getNumDatacenters() {
        return scenario.getDatacenters().stream().mapToInt(DatacenterRegistry::getAmount).sum();
    }

    private int getNumHostsFromAllDatacenters() {
        return scenario.getDatacenters().stream().mapToInt(dc -> dc.getAmount() * getNumOfHostsFromDatacenter(dc)).sum();
    }

    private int getNumOfHostsFromDatacenter(DatacenterRegistry dc) {
        return dc.getHosts().stream().mapToInt(h -> h.getAmount()).sum();
    }

    private int getNumVmsFromAllCustomers() {
        return scenario.getCustomers().stream().mapToInt(c -> c.getAmount() * getNumVmsForCustomer(c)).sum();
    }

    private int getNumVmsForCustomer(CustomerRegistry customer) {
        return customer.getVms().stream().mapToInt(vm -> vm.getAmount()).sum();
    }

    private int getNumCloudletsFromAllCustomers() {
        return scenario.getCustomers().stream().mapToInt(c -> c.getAmount() * getNumCloudletsForCustomer(c)).sum();
    }

    private int getNumCloudletsForCustomer(CustomerRegistry customer) {
        return customer.getCloudlets().stream().mapToInt(cloudlet -> cloudlet.getAmount()).sum();
    }

    /**
     * Concrete data center list.
     * The list of datacenters to be created at CloudSim, obtained from the
     * abstract DatacenterRegistry information from the YAML file.
     */
    public List<Datacenter> getDatacenters() {
        return datacenters;
    }

    public boolean isShowResults() {
        return showResults;
    }

    public CloudSimulation setShowResults(boolean showResults) {
        this.showResults = showResults;
        return this;
    }

    public boolean isLogEnabled() {
        return logEnabled;
    }

    public CloudSimulation setLogEnabled(boolean logEnabled) {
        this.logEnabled = logEnabled;
        return this;
    }

    public boolean isPrintScenariosConfiguration() {
        return printScenariosConfiguration;
    }

    public CloudSimulation setPrintScenariosConfiguration(boolean printScenariosConfiguration) {
        this.printScenariosConfiguration = printScenariosConfiguration;
        return this;
    }
}