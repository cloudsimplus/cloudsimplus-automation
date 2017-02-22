package com.manoelcampos.cloudsim.automation;

import cloudreports.models.*;
import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.provisioners.PeProvisioner;

import java.text.DecimalFormat;
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
 * @author <a href="http://manoelcampos.com">Manoel Campos da Silva Filho</a>
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
     * @throws ParameterException Throws when the method,
     *                            starting from the information at YAML file,
     *                            sets invalid parameters for CloudSim Datacenter objects
     *                            to be created.
     * @see YamlScenario#datacenterRegistries
     */
    private List<Datacenter> createConcreteDatacentersFromAbstractDatacenterRegistries()
            throws ParameterException
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
            final List<Host> hostList) throws Exception
    {
        // 5. Create a DatacenterCharacteristics object that stores the
        //    properties of a data center: architecture, OS, list of
        //    Machines, allocation policy: time- or space-shared, time zone
        //    and its price (G$/Pe time unit).
        final double time_zone = 10.0;  // time zone this resource is located
        final DatacenterCharacteristics characteristics =
                createDatacenterCharacteristics(dcr, hostList, time_zone);

        final LinkedList<Storage> storageList = createSan(dcr);

        return
                new Datacenter(
                        datacenterName, characteristics,
                        PolicyLoader.vmAllocationPolicy(dcr.getAllocationPolicyAlias(), hostList),
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
     * @see YamlScenario#customerRegistries
     */
    private Map<DatacenterBroker, CustomerRegistry> createConcreteDatacenterBrokersFromAbstractCustomerRegistries() {
        final Map<DatacenterBroker, CustomerRegistry> list = new HashMap<>();
        int brokerCount = 0;
        String brokerName;
        for (CustomerRegistry cr : customerRegistries) {
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
     * @see YamlScenario#createConcreteDatacenterBrokersFromAbstractCustomerRegistries()
     */
    private Map<DatacenterBroker, List<Vm>> createConcreteVmListForAllBrokers(
            final Map<DatacenterBroker, CustomerRegistry> brokers)
    {
        final Map<DatacenterBroker, List<Vm>> list = new HashMap<>();

        int vmCount = 0;
        for (DatacenterBroker broker : brokers.keySet()) {
            LinkedList<Vm> vms =
                    createConcreteVmListForOneBroker(
                            broker, brokers.get(broker), ++vmCount);
            list.put(broker, vms);
        }

        return list;
    }

    public LinkedList<Vm> createConcreteVmListForOneBroker(
            final DatacenterBroker broker,
            final CustomerRegistry customerRegistry,
            final int vmCount) throws RuntimeException
    {
        final LinkedList<Vm> list = new LinkedList<>();
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
        return new Vm(
                vmCount, broker.getId(), vmr.getMips(), vmr.getPesNumber(),
                vmr.getRam(), vmr.getBw(), vmr.getSize(),
                vmr.getVmm(),
                PolicyLoader.cloudletScheduler(vmr.getSchedulingPolicyAlias()));
    }

    /**
     * Create a map that stores the cloudlet list for each customer (broker).
     *
     * @param brokers The map containing the abstract customer information
     *                (CustomerRegistry) obtained from the YAML file, for each
     *                concrete customer created (DatacenterBroker).
     * @return Returns the list of Cloudlets created.
     */
    private Map<DatacenterBroker, List<Cloudlet>> createConcreteCloudletsFromAbstractUtilizationProfiles(final Map<DatacenterBroker, CustomerRegistry> brokers) {
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
            final UtilizationProfile up, final DatacenterBroker broker) throws RuntimeException {
        final Cloudlet cloudlet =
                new Cloudlet(cloudletCount, up.getLength(),
                        up.getCloudletsPesNumber(),
                        up.getFileSize(), up.getOutputSize(),
                        PolicyLoader.utilizationModel(up.getUtilizationModelCpuAlias()),
                        PolicyLoader.utilizationModel(up.getUtilizationModelRamAlias()),
                        PolicyLoader.utilizationModel(up.getUtilizationModelBwAlias())
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
    private void printCloudletList(final DatacenterBroker broker, final List<Cloudlet> list) {
        final int size = list.size();
        Cloudlet cloudlet;

        Log.printLine();
        Log.printLine(
                "Broker: name " + broker.getName() + " id " + broker.getId() + " cloudlets executed: " +
                        broker.getCloudletReceivedList().size() +
                        "========================================================");
        final String[] captions = {
                "###", "CloudletID", "STATUS  ",
                "DataCenterID", "VmID", "HostID", "ExecTime",
                "Start Time", "Finish Time"};
        LogUtils.printCaptions(captions);
        LogUtils.printLine(captions, new String[]{"int", "int", "string", "int", "int", "int", "secs", "secs", "secs"});

        final DecimalFormat dft = new DecimalFormat("###.##");

        for (int i = 0; i < size; i++) {
            cloudlet = list.get(i);
            if (cloudlet.getCloudletStatus() == Cloudlet.SUCCESS) {
                Vm vm = findVm(broker, cloudlet.getVmId());
                final Object[] data = {
                        String.format("%3d", i + 1),
                        cloudlet.getCloudletId(), "SUCCESS", cloudlet.getResourceId(),
                        cloudlet.getVmId(), getHostIdOfVm(vm),
                        dft.format(cloudlet.getActualCPUTime()),
                        dft.format(cloudlet.getExecStartTime()),
                        dft.format(cloudlet.getFinishTime())};

                LogUtils.printLine(captions, data);
            }
        }
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
            final DatacenterRegistry datacenterRegistry, int hostCount) throws RuntimeException {
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
        return new Host(
                hostId,
                PolicyLoader.newRamProvisioner("Ram", hr.getRamProvisionerAlias(), hr.getRam()),
                PolicyLoader.newBwProvisioner("Bw", hr.getBwProvisionerAlias(), hr.getBw()),
                hr.getStorage(), peList,
                PolicyLoader.vmScheduler(hr.getSchedulingPolicyAlias(), peList)
        );
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
     * @throws ParameterException Throws when the method,
     *                            starting from the information at YAML file,
     *                            sets invalid parameters for CloudSim SAN objects
     *                            to be created.
     * @see YamlScenario#datacenterRegistries
     */
    private LinkedList<Storage> createSan(final DatacenterRegistry dcr) throws ParameterException {
        final LinkedList<Storage> list = new LinkedList<>();
        for (SanStorageRegistry sr : dcr.getSanList()) {
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
     * @see YamlScenario#createDatacenterCharacteristics(cloudreports.models.DatacenterRegistry, java.util.List, double)
     */
    private DatacenterCharacteristics createDatacenterCharacteristics(
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
    private List<Pe> createHostProcessingElements(final HostRegistry hr) {
        final List<Pe> list = new ArrayList<>();
        for (int j = 0; j < hr.getNumOfPes(); j++) {
            PeProvisioner peProvisioner =
                    PolicyLoader.newPeProvisioner(
                            "Pe", hr.getPeProvisionerAlias(), hr.getMipsPerPe());
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
     * @throws ParameterException Throws when the method,
     *                            starting from the information at YAML file,
     *                            sets invalid parameters for CloudSim objects
     *                            to be created.
     */
    public void run(final String simulationLabel) throws ParameterException {
        // First step: Initialize the CloudSim package. It should be called
        // before creating any entities.
        int num_user = 1;   // number of grid users 
        final Calendar calendar = Calendar.getInstance(Locale.getDefault());
        boolean trace_flag = false;  // mean trace events

        // Initialize the CloudSim library
        CloudSim.init(num_user, calendar, trace_flag);
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

        // Fifth step: Starts the simulation
        CloudSim.startSimulation();

        final Map<DatacenterBroker, List<Cloudlet>> receivedCloudletList = new HashMap<>();
        // Final step: Print results when simulation is over
        for (DatacenterBroker broker : brokers.keySet()) {
            receivedCloudletList.put(broker, broker.getCloudletReceivedList());
        }

        CloudSim.stopSimulation();

        Log.printLine("\n============================Results: " + simulationLabel);
        for (DatacenterBroker broker : brokers.keySet()) {
            printCloudletList(broker, receivedCloudletList.get(broker));
        }
        Log.printLine("\nCloudEnvironment Simulation finished!");
    }


}
