package org.cloudbus.cloudsim.examples;

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
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

/**
 * Creates a cloud environment autonomously from a YAML file.
 * The application has a command line interface that accept
 * the YAML file name as parameter.
 * @author manoelcampos
 */
public class Automation {
        /** Abstract information about data centers. */
        private List<DatacenterRegistry> datacenterRegistries = new ArrayList<>();
        
        /** Abstract information about customers (brokers). */
        private List<CustomerRegistry> customerRegistries = new ArrayList<>();
        
	/** Concrete cloudlet list. */
	private Map<DatacenterBroker, List<Cloudlet>> brokerCloudlets;
        
	/** Concrete VM list. */
	private Map<DatacenterBroker, List<Vm>> brokerVms;
        
        /** Concrete data center list. */
        private List<Datacenter> datacenters;
        
        public static List<Automation> loadConfigFile(String fileName) throws FileNotFoundException, YamlException{
            Automation env;
            List<Automation> envs = new ArrayList<>();
            YamlReader reader = new YamlReader(new FileReader(fileName));
            YamlConfig cfg = reader.getConfig();
            cfg.setClassTag("datacenter", DatacenterRegistry.class);
            cfg.setClassTag("customer", CustomerRegistry.class);
            cfg.setClassTag("san", SanStorageRegistry.class);
            cfg.setClassTag("host", HostRegistry.class);
            cfg.setClassTag("profile", UtilizationProfile.class);
            cfg.setClassTag("vm", VirtualMachineRegistry.class);
            do {
                env = reader.read(Automation.class);
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
        
	private List<Datacenter> createDatacenters() throws ParameterException{
                for(int i = 0; i < datacenterRegistries.size(); i++){
                    System.out.println(datacenterRegistries.get(i).getArchitecture());
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

	//We strongly encourage users to develop their own broker policies, to submit vms and cloudlets according
	//to the specific rules of the simulated scenario
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
        
         /** Create a list of VMs to the specified user.
         * @return Returns the list of VMs created.
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
        

        /** Create a list of Cloudlets to the specified user.
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
	 * Prints the Cloudlet objects
	 * @param list  list of Cloudlets
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
        
        public void run(String simulationLabel) throws ParameterException{
            // First step: Initialize the CloudSim package. It should be called
            // before creating any entities.
            int num_user = 1;   // number of grid users 
            Calendar calendar = Calendar.getInstance();
            boolean trace_flag = false;  // mean trace events

            // Initialize the CloudSim library
            CloudSim.init(num_user, calendar, trace_flag);
            System.out.println("Hosts========================");
            datacenters = createDatacenters();
            for(Datacenter datacenter: datacenters){
                System.out.println(datacenter.getName() +": "+ datacenter.getHostList().size() +" hosts");
            }
            System.out.println("=============================");
            
            //Third step: Create Brokers
            Map<DatacenterBroker, CustomerRegistry> brokers = createUserBrokers();

            //Fourth step: Create VMs and Cloudlets and send them to broker
            brokerVms = createVMs(brokers); 
            brokerCloudlets = createCloudlets(brokers);

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
         * @param args Accept the name of YAML file containing
         * the simulation scenario to be created.
         * Each YAML file can contain multiples scenarios to be created together.
         * This is made only adding a --- separator between each scenario.
         * Multiples files can be 
	 */
	public static void main(String[] args) {
            String fileName = "CloudEnvironment1.yml";
            if(args.length>0){
                fileName = args[0];
            }
            
            List<Automation> envs;
            try {
                envs = loadConfigFile(fileName);
                //envs = null;
            } catch (FileNotFoundException | YamlException ex) {
                Logger.getLogger(Automation.class.getName()).log(Level.SEVERE, null, ex);
                return;
            }
            
            if(envs == null || envs.isEmpty()) {
                return;
            }
            
            Log.printLine("Starting " + Automation.class.getSimpleName());
            try {
                envs.get(0).run(new File(fileName).getName());
            }
            catch (Exception e)
            {
                e.printStackTrace(System.out);
                Log.printLine("The simulation has been terminated due to an unexpected error");
            }
    }        

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

    private LinkedList<Storage> createSan(DatacenterRegistry dcr) throws ParameterException {
        LinkedList<Storage> storageList = new LinkedList<>();	//we are not adding SAN devices by now
        for(SanStorageRegistry sr: dcr.getSanList()){
            storageList.add(
                 new SanStorage(
                   sr.getCapacity(), sr.getBandwidth(), sr.getNetworkLatency()));
        }
        return storageList;
    }

    private DatacenterCharacteristics createDatacenterCharacteristics(DatacenterRegistry dcr, List<Host> hostList, double time_zone) {
        DatacenterCharacteristics characteristics = 
            new DatacenterCharacteristics(
                dcr.getArchitecture(), dcr.getOs(), dcr.getVmm(), 
                hostList, time_zone, 
                dcr.getCostPerSec(), dcr.getCostPerMem(), 
                dcr.getCostPerStorage(), dcr.getCostPerBw());
        return characteristics;
    }

    /** Creates a list of PEs (Processing Elements) or CPUs/Cores for a host/VM for a host
     */
    private List<Pe> createHostProcessingElements(HostRegistry hr) {
        List<Pe> list = new ArrayList<>();
        for(int j=0; j < hr.getNumOfPes() ; j++) {
            list.add(new Pe(j, new PeProvisionerSimple(hr.getMipsPerPe())));
        }
        return list;
    }

    private Vm getVm(DatacenterBroker broker, int vmId) {
        for (Vm vm : brokerVms.get(broker)) {
            if(vm.getId()==vmId){
                return vm;
            }
        }
        return null;        
    }

}
