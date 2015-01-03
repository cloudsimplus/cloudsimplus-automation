package com.manoelcampos.cloudsim.automation;

import cloudreports.models.CustomerRegistry;
import cloudreports.models.DatacenterRegistry;
import cloudreports.models.HostRegistry;
import cloudreports.models.SanStorageRegistry;
import cloudreports.models.UtilizationProfile;
import cloudreports.models.VirtualMachineRegistry;
import com.esotericsoftware.yamlbeans.YamlConfig;
import com.esotericsoftware.yamlbeans.YamlException;
import com.esotericsoftware.yamlbeans.YamlReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.ParameterException;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.SanStorage;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.examples.LogUtils;
import org.cloudbus.cloudsim.examples.PolicyLoader;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

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
     * Loads CloudSim simulation scenarios from a YAML file.
     * 
     * @param yamlFileName The YAML scenario file to be loaded.
     * @return Returns a list of simulation scenarios specified
     * inside the YAML file. Each scenario inside the file
     * can be delimited using the line below:
     * --- #Delimits each Cloud Environment
     * @throws FileNotFoundException Throws when the YAML file
     * is not found.
     * @throws YamlException  Throws when there is any
     * error parsing the YAML file.
     */
    public static List<YamlScenario> loadScenarioFile(String yamlFileName) throws FileNotFoundException, YamlException{
        YamlScenario env;
        List<YamlScenario> envs = new ArrayList<>();
        YamlReader reader = new YamlReader(new FileReader(yamlFileName));
        YamlConfig cfg = reader.getConfig();
        //Defines the aliases in the YAML file that refers to specific java Classes.
        cfg.setClassTag("datacenter", DatacenterRegistry.class);
        cfg.setClassTag("customer", CustomerRegistry.class);
        cfg.setClassTag("san", SanStorageRegistry.class);
        cfg.setClassTag("host", HostRegistry.class);
        cfg.setClassTag("profile", UtilizationProfile.class);
        cfg.setClassTag("vm", VirtualMachineRegistry.class);

        do {
            env = reader.read(YamlScenario.class);
        } while (env!=null ? envs.add(env) : false);
        return envs;
    }

    /**
     * @return the datacenterRegistries
     */
    public List<DatacenterRegistry> getDatacenterRegistries() {
        return datacenterRegistries;
    }

    /**
     * @param datacenterRegistries the datacenterRegistries to set
     */
    public void setDatacenterRegistries(List<DatacenterRegistry> datacenterRegistries) {
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
    public void setCustomerRegistries(List<CustomerRegistry> customerRegistries) {
        this.customerRegistries = customerRegistries;
    }

    /**
     * Creates the concrete Datacenter list from the abstract
     * DatacenterRegistry list.
     * @see YamlScenario#datacenterRegistries
     * @return Returns the list of created CloudSim datacenters 
     * @throws ParameterException Throws when the method, 
     * starting from the information at YAML file, 
     * sets invalid parameters for CloudSim Datacenter objects
     * to be created.
     */
    private List<Datacenter> createDatacenters() throws ParameterException {
        for (DatacenterRegistry datacenterRegistrie : datacenterRegistries) {
            System.out.println(datacenterRegistrie.getArchitecture());
        }
            String datacenterName;
            int datacenterCount=0;
            List<Datacenter> list = new ArrayList<>();
            for(DatacenterRegistry dcr: datacenterRegistries){
                if(dcr.getAmount()==0) {
                    dcr.setAmount(1);
                }

                int hostCount=0;
                for(int i = 0; i < dcr.getAmount(); i++){
                    datacenterCount++;
                    datacenterName = dcr.getName();
                    if(dcr.getName()==null || dcr.getName().trim().equals("")){
                        datacenterName=String.format("datacenter%d", datacenterCount);
                    }

                    List<Host> hostList = createHosts(dcr, hostCount);
                    hostCount+=hostList.size();


                    // 5. Create a DatacenterCharacteristics object that stores the
                    //    properties of a data center: architecture, OS, list of
                    //    Machines, allocation policy: time- or space-shared, time zone
                    //    and its price (G$/Pe time unit).
                    double time_zone = 10.0;         // time zone this resource located
                    LinkedList<Storage> storageList = createSan(dcr);

                    DatacenterCharacteristics characteristics = 
                        createDatacenterCharacteristics(dcr, hostList, time_zone);


                    // 6. Finally, we need to create a PowerDatacenter object.
                    try {
                            Datacenter datacenter = 
                                  new Datacenter(
                                    datacenterName, characteristics, 
                                    PolicyLoader.vmAllocationPolicy(dcr.getAllocationPolicyAlias(), hostList), 
                                    storageList, dcr.getSchedulingInterval());
                            list.add(datacenter);
                    } catch (Exception e) {
                            e.printStackTrace(System.out);
                    }
                }
            }

            return list;
    }

    /**
     * Creates a map containing the abstract customer information (CustomerRegistry)
     * used to create each concrete CloudSim customer (DatacenterBroker).
     * @see YamlScenario#customerRegistries
     * @return Returns the map created.
     */
    private Map<DatacenterBroker, CustomerRegistry> createUserBrokers(){
            Map<DatacenterBroker, CustomerRegistry> list = new HashMap<>();
            int brokerCount = 0;
            String brokerName;
            for(CustomerRegistry cr: customerRegistries){
                if(cr.getAmount()==0) {
                    cr.setAmount(1);
                }
                for(int i = 0; i < cr.getAmount(); i++){
                    brokerCount++;
                    brokerName = cr.getName();
                    if(brokerName==null || brokerName.trim().equals("")){
                        brokerName = String.format("broker%d", brokerCount);
                    }

                    DatacenterBroker broker;
                    try {
                            broker = new DatacenterBroker(brokerName);
                            list.put(broker, cr);
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
     * @see YamlScenario#createUserBrokers() 
     * @return Returns the a map containing the list of created VMs
     * for each customer (DatacenterBroker).
    */
    private Map<DatacenterBroker, List<Vm>> createVMs(Map<DatacenterBroker, CustomerRegistry> brokers) {
            Map<DatacenterBroker, List<Vm>> list = new HashMap<>();

            int vmCount = 0;
            for(DatacenterBroker broker: brokers.keySet()){
                LinkedList<Vm> vms = new LinkedList<>();
                for(VirtualMachineRegistry vmr:  brokers.get(broker).getVmList()){
                    if(vmr.getAmount()==0){
                        vmr.setAmount(1);                            
                    }
                    for(int i=0; i < vmr.getAmount(); i++){
                        Vm vm = new Vm(
                                ++vmCount, broker.getId(), vmr.getMips(), vmr.getPesNumber(), 
                                vmr.getRam(), vmr.getBw(), vmr.getSize(), 
                                vmr.getVmm(), 
                                PolicyLoader.cloudletScheduler(vmr.getSchedulingPolicyAlias()));
                        vms.add(vm);
                    }
                }
                list.put(broker, vms);
            }

            return list;
    }

    /** 
     * Create a map that stores the cloudlet list for each customer (broker).
     * 
     * @param brokers The map containing the abstract customer information
     * (CustomerRegistry) obtained from the YAML file, for each 
     * concrete customer created (DatacenterBroker).
     * @return Returns the list of Cloudlets created.
     */
    private Map<DatacenterBroker, List<Cloudlet>> createCloudlets(Map<DatacenterBroker, CustomerRegistry> brokers){
            // Creates a container to store Cloudlets
            Map<DatacenterBroker, List<Cloudlet>> list = new HashMap<>();

            UtilizationModel utilizationModelFull = new UtilizationModelFull();
            int cloudletCount=0;
            for(DatacenterBroker broker: brokers.keySet()){
                List<Cloudlet> cloudlets = new ArrayList<>();
                for(UtilizationProfile up: brokers.get(broker).getUtilizationProfile()){
                    if(up.getNumOfCloudlets()==0){
                        up.setNumOfCloudlets(1);
                    }
                    for(int i=0;i<up.getNumOfCloudlets();i++){
                            Cloudlet cloudlet = 
                               new Cloudlet(++cloudletCount, up.getLength(), 
                                    up.getCloudletsPesNumber(), 
                                    up.getFileSize(), up.getOutputSize(), 
                                    PolicyLoader.utilizationModel(up.getUtilizationModelCpuAlias()), 
                                    PolicyLoader.utilizationModel(up.getUtilizationModelRamAlias()),
                                    PolicyLoader.utilizationModel(up.getUtilizationModelBwAlias())
                                    );
                            // setting the owner of these Cloudlets
                            cloudlet.setUserId(broker.getId());
                            cloudlets.add(cloudlet);
                    }
                }
                list.put(broker, cloudlets);
            }

            return list;
    }        

    /**
     * Prints, at the standard output, the Cloudlet list for the specified 
     * customer (DatacenterBroker).
     * 
     * @param broker Customer owning the VMs
     * @param list list of Cloudlets to be printed. 
     */
    private void printCloudletList(DatacenterBroker broker, List<Cloudlet> list) {
            int size = list.size();
            Cloudlet cloudlet;

            Log.printLine();
            Log.printLine("Broker: name " + broker.getName() +  " id "+broker.getId()+" cloudlets executed: "+
                    broker.getCloudletReceivedList().size()+"========================================================");
            String[] captions = {"###","CloudletID", "STATUS  ",
                            "DataCenterID", "VmID", "HostID", "ExecTime",
                            "Start Time", "Finish Time"};
            LogUtils.printCaptions(captions);
            LogUtils.printLine(captions, new String[]{"int","int","string","int","int","int","secs","secs", "secs"});

            DecimalFormat dft = new DecimalFormat("###.##");

            for (int i = 0; i < size; i++) {
                    cloudlet = list.get(i);
                    if (cloudlet.getCloudletStatus() == Cloudlet.SUCCESS) {
                            String hostId="";
                            Vm vm = getVm(broker, cloudlet.getVmId());
                            if(vm!=null){
                                if(vm.getHost()!=null) {
                                    hostId = String.valueOf(vm.getHost().getId());
                                }
                            }
                            Object[] data = {
                                String.format("%3d", i+1),
                                cloudlet.getCloudletId(), "SUCCESS", cloudlet.getResourceId(), 
                                cloudlet.getVmId(), hostId,
                                dft.format(cloudlet.getActualCPUTime()), 
                                dft.format(cloudlet.getExecStartTime()), 
                                dft.format(cloudlet.getFinishTime())};

                            LogUtils.printLine(captions, data);
                    }
            }

    }
       
    /**
     * Create the concrete host list from the abstract DatacenterRegistry
     * object.
     * 
     * @param datacenterRegistry A specific abstract datacenter information
     * get from the datacenterRegistries list at the YAML file.
     * @param hostCount The global created host count for the
     * specific simulation scenario loaded. This is used to incrementally
     * assign an id for each host created that was not explicitly defined 
     * on id at the YAML file.
     * @return Returns the list of created hosts from the specified datacenterRegistry.
     * @throws RuntimeException 
     * @see YamlScenario#datacenterRegistries
     */
    private List<Host> createHosts(DatacenterRegistry datacenterRegistry, int hostCount) throws RuntimeException {
        int hostId;
        // Here are the steps needed to create a PowerDatacenter:
        // 1. We need to create a list to store one or more
        //    Machines
        List<Host> hostList = new ArrayList<>();
        for(HostRegistry hr: datacenterRegistry.getHostList()){
            for(int i=0; i < hr.getAmount(); i++){
                List<Pe> peList = createHostProcessingElements(hr); 

                hostCount++;
                hostId = (int)hr.getId();
                if(hostId==0){
                    hostId=hostCount;
                }

                //4. Create Hosts with its id and list of PEs and add them to the list of machines
                hostList.add(
                        new Host(
                                hostId,
                                new RamProvisionerSimple(hr.getRam()),
                                new BwProvisionerSimple(hr.getBw()),
                                hr.getStorage(), peList,
                                PolicyLoader.vmScheduler(hr.getSchedulingPolicyAlias(), peList)
                        )
                ); 
            }
        }
        return hostList;
    }

    /**
     * Creates a CloudSim Storage Area Network (SAN) from an abstract DatacenterRegistry
     * information, get from a YAML specification file.
     * 
     * @param dcr A specific DatacenterRegistry information
     * to be used to create the SANs.
     * @return Returns the SANs created list.
     * @throws ParameterException Throws when the method, 
     * starting from the information at YAML file, 
     * sets invalid parameters for CloudSim SAN objects
     * to be created.
     * @see YamlScenario#datacenterRegistries
     */
    private LinkedList<Storage> createSan(DatacenterRegistry dcr) throws ParameterException {
        LinkedList<Storage> storageList = new LinkedList<>();	
        for(SanStorageRegistry sr: dcr.getSanList()){
            storageList.add(
                 new SanStorage(
                   sr.getCapacity(), sr.getBandwidth(), sr.getNetworkLatency()));
        }
        return storageList;
    }

    /**
     * Create a DatacenterCharacteristics object
     * that represents that characteristics of one CloudSim Datacenter,
     * including the list of hosts of the Datacenter.
     * 
     * @param dcr The abstract DatacenterRegistry information, obtained from the YAML file,
     * to be used to define the Datacenter characteristics.
     * @param hostList The list of hosts of the Datacenter
     * @param time_zone The timezone of the Datacenter.
     * @return Returns the DatacenterCharacteristics object created.
     * @see YamlScenario#createDatacenterCharacteristics(cloudreports.models.DatacenterRegistry, java.util.List, double) 
     */
    private DatacenterCharacteristics createDatacenterCharacteristics(DatacenterRegistry dcr, List<Host> hostList, double time_zone) {
        DatacenterCharacteristics characteristics = 
            new DatacenterCharacteristics(
                dcr.getArchitecture(), dcr.getOs(), dcr.getVmm(), 
                hostList, time_zone, 
                dcr.getCostPerSec(), dcr.getCostPerMem(), 
                dcr.getCostPerStorage(), dcr.getCostPerBw());
        return characteristics;
    }

    /** 
     * Creates a list of PEs (Processing Elements, i.e. CPUs/Cores) 
     * for a host or VM.
     * 
     * @param hr The abstract Host information obtained from the YAML file.
     * @return Returns the list of PEs created.
     */
    private List<Pe> createHostProcessingElements(HostRegistry hr) {
        List<Pe> list = new ArrayList<>();
        for(int j=0; j < hr.getNumOfPes() ; j++) {
            list.add(new Pe(j, new PeProvisionerSimple(hr.getMipsPerPe())));
        }
        return list;
    }

    /**
     * Searches for a VM, with the specified id, at the broker (that represents
     * the customer).
     * @param broker The broker (customer) where to search for the VM
     * @param vmId The desired VM to be located.
     * @return If the VM is found, returns it, otherwise, returns null.
     */
    private Vm getVm(DatacenterBroker broker, int vmId) {
        for (Vm vm : brokerVms.get(broker)) {
            if(vm.getId()==vmId){
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
     * simulation scenario. Commonly this can be the name
     * of the loaded YAML file.
     * @throws ParameterException Throws when the method, 
     * starting from the information at YAML file, 
     * sets invalid parameters for CloudSim objects
     * to be created.
     */
    public void run(String simulationLabel) throws ParameterException{
        // First step: Initialize the CloudSim package. It should be called
        // before creating any entities.
        int num_user = 1;   // number of grid users 
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        boolean trace_flag = false;  // mean trace events

        // Initialize the CloudSim library
        CloudSim.init(num_user, calendar, trace_flag);
        System.out.println("Hosts========================");
        this.datacenters = createDatacenters();
        for(Datacenter datacenter: datacenters){
            System.out.println(datacenter.getName() +": "+ datacenter.getHostList().size() +" hosts");
        }
        System.out.println("=============================");

        //Third step: Create Brokers
        Map<DatacenterBroker, CustomerRegistry> brokers = createUserBrokers();

        //Fourth step: Create VMs and Cloudlets and send them to broker
        this.brokerVms = createVMs(brokers); 
        this.brokerCloudlets = createCloudlets(brokers);

        for(DatacenterBroker broker: brokers.keySet()){
            broker.submitVmList(brokerVms.get(broker));
            broker.submitCloudletList(brokerCloudlets.get(broker));
        }

        // Fifth step: Starts the simulation
        CloudSim.startSimulation();

        Map<DatacenterBroker, List<Cloudlet>> receivedCloudletList = new HashMap<>();
        // Final step: Print results when simulation is over
        for(DatacenterBroker broker: brokers.keySet()){
            receivedCloudletList.put(broker, broker.getCloudletReceivedList());
        }

        CloudSim.stopSimulation();

        Log.printLine("\n============================Results: "+simulationLabel);
        for(DatacenterBroker broker: brokers.keySet()){
            printCloudletList(broker, receivedCloudletList.get(broker));
        }
        Log.printLine("\nCloudEnvironment Simulation finished!");

    }


    /**
     * Execute the command line interface of the applications.
     * 
     * @param args Accept the name of YAML file containing
     * the simulation scenarios to be created.
     * Each YAML file can contain multiples scenarios to be created together.
     * This is made only adding a --- separator between each scenario.
    */
    public static void main(String[] args) {
        String fileName;
        if(args.length>0){
            fileName = args[0];
        } else {
            System.err.println("\n\nERROR: You must specify a YAML file, containing the CloudSim simulation scenario, as command line parameter.\n");
            return;
        }

        List<YamlScenario> envs;
        try {
            envs = loadScenarioFile(fileName);
            //envs = null;
        } catch (FileNotFoundException | YamlException ex) {
            Logger.getLogger(YamlScenario.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }

        if(envs == null || envs.isEmpty()) {
            System.err.println("\n\nERROR: Your YAML file is empty or could not be loaded.\n");
            return;
        }

        Log.printLine("Starting " + YamlScenario.class.getSimpleName());
        try {
            String scenarioLabel = new File(fileName).getName();
            int i = 0;
            for (YamlScenario env : envs) {
                env.run(++i + " - " + scenarioLabel);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace(System.out);
            Log.printLine("The simulation has been terminated due to an unexpected error");
        }
    }            
}
