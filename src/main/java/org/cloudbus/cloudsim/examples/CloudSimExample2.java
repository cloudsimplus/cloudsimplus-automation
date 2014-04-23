/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation
 *               of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.examples;


import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.CloudletSchedulerTimeShared;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;


/**
 * A simple example showing how to create
 * a datacenter with one host and run two
 * cloudlets on it. The cloudlets run in
 * VMs with the same MIPS requirements.
 * The cloudlets will take the same time to
 * complete the execution.
 */
public class CloudSimExample2 {

	/** The cloudlet list. */
	private static List<Cloudlet> cloudletList;

	/** The vmlist. */
	private static List<Vm> vmlist;

	/**
	 * Creates main() to run this example
	 */
	public static void main(String[] args) {
		Log.printLine("Starting CloudSimExample2...");

	        try {
	        	// First step: Initialize the CloudSim package. It should be called
	            	// before creating any entities.
	            	int num_user = 1;   // number of cloud users 
	            	Calendar calendar = Calendar.getInstance();
	            	boolean trace_flag = false;  // mean trace events

	            	// Initialize the CloudSim library
	            	CloudSim.init(num_user, calendar, trace_flag);

	            	// Second step: Create Datacenters
	            	//Datacenters are the resource providers in CloudSim. We need at list one of them to run a CloudSim simulation
	            	@SuppressWarnings("unused")
			Datacenter datacenter0 = createDatacenter("Datacenter_0");

	            	//Third step: Create Broker
	            	DatacenterBroker broker = createBroker();
	            	int brokerId = broker.getId();

	            	//Fourth step: Create one virtual machine
	            	vmlist = new ArrayList<>();

	            	//VM description
	            	int vmid = 0;
	            	int vmMips = 250; //MIPS = Millions of Instructions Per Second
	            	long vmSize = 10000; //image size (MB)
	            	int vmRam = 512; //vm memory (MB)
	            	long vmBw = 1000; //1.000 bits/s = 1Mbps
	            	int vmPesNumber = 1; //number of cpus (PE = Processor Element)
	            	String vmm = "Xen"; //VMM name

	            	//create two VMs
	            	Vm vm1 = new Vm(vmid++, brokerId, vmMips, vmPesNumber, vmRam, vmBw, vmSize, vmm, new CloudletSchedulerTimeShared());
	            	Vm vm2 = new Vm(vmid++, brokerId, vmMips, vmPesNumber, vmRam, vmBw, vmSize, vmm, new CloudletSchedulerTimeShared());

	            	//add the VMs to the vmList
	            	vmlist.add(vm1);
	            	vmlist.add(vm2);

	            	//submit vm list to the broker
	            	broker.submitVmList(vmlist);


	            	//Fifth step: Create two Cloudlets
	            	cloudletList = new ArrayList<>();

	            	//Cloudlet properties
	            	int cloudletId = 0;
	            	long cloudletLength = 250000; //in MI (Millions of Instructions)
	            	long fileSize = 300; //in bytes
	            	long outputSize = 300; //in bytes
	            	UtilizationModel utilizationModel = new UtilizationModelFull();

	            	Cloudlet cloudlet1 = new Cloudlet(cloudletId++, cloudletLength, vmPesNumber, fileSize, outputSize, utilizationModel, utilizationModel, utilizationModel);
	            	cloudlet1.setUserId(brokerId);

	            	Cloudlet cloudlet2 = new Cloudlet(cloudletId++, cloudletLength, vmPesNumber, fileSize, outputSize, utilizationModel, utilizationModel, utilizationModel);
	            	cloudlet2.setUserId(brokerId);

	            	//add the cloudlets to the list
	            	cloudletList.add(cloudlet1);
	            	cloudletList.add(cloudlet2);

	            	//submit cloudlet list to the broker
	            	broker.submitCloudletList(cloudletList);


                        //TODO: what is the difference among cloudlet.setVmId(vmid) and the line below????
	            	//bind the cloudlets to the vms. This way, the broker
	            	// will submit the bounded cloudlets only to the specified VM
	            	broker.bindCloudletToVm(cloudlet1.getCloudletId(),vm1.getId());
	            	broker.bindCloudletToVm(cloudlet2.getCloudletId(),vm2.getId());

	            	// Sixth step: Starts the simulation
	            	CloudSim.startSimulation();


	            	// Final step: Print results when simulation is over
	            	List<Cloudlet> newList = broker.getCloudletReceivedList();

	            	CloudSim.stopSimulation();

	            	printCloudletList(newList);

	            	Log.printLine("CloudSimExample2 finished!");
	        }
	        catch (Exception e) {
	            e.printStackTrace(System.out);
	            Log.printLine("The simulation has been terminated due to an unexpected error");
	        }
	}

	private static Datacenter createDatacenter(String name){

	        // Here are the steps needed to create a PowerDatacenter:
	        // 1. We need to create a list to store
	    	//    our machine
	    	List<Host> hostList = new ArrayList<>();

	        // 2. A Machine contains one or more PEs or CPUs/Cores.
	    	// In this example, it will have only one core.
	    	List<Pe> peList = new ArrayList<>();

	    	int hostMips = 1000;

	        // 3. Create PEs and add these into a list.
	    	peList.add(new Pe(0, new PeProvisionerSimple(hostMips))); // need to store Pe id and MIPS Rating

	        //4. Create Host with its id and list of PEs and add them to the list of machines
	        int hostId=0;
	        int hostRam = 2048; //host memory (MB)
	        long hostStorage = 1000000; //host storage
	        int hostBw = 10000; //10.000 bits/s = 10Mbps

	        hostList.add(
	    			new Host(
	    				hostId,
	    				new RamProvisionerSimple(hostRam),
	    				new BwProvisionerSimple(hostBw),
	    				hostStorage,
	    				peList,
	    				new VmSchedulerTimeShared(peList)
	    			)
	    		); // This is our machine


	        // 5. Create a DatacenterCharacteristics object that stores the
	        //    properties of a data center: architecture, OS, list of
	        //    Machines, allocation policy: time- or space-shared, time zone
	        //    and its price (G$/Pe time unit).
	        String arch = "x86";      // system architecture
	        String os = "Linux";          // operating system
	        String vmm = "Xen";
	        double time_zone = 10.0;         // time zone this resource located
	        double costPerCpu = 3.0;              // the cost of using processing in this resource (per second)
	        double costPerMem = 0.05;		// the cost of using memory in this resource
	        double costPerStorage = 0.001;	// the cost of using storage in this resource
	        double costPerBw = 0.0;			// the cost of using bw in this resource
	        LinkedList<Storage> storageList = new LinkedList<>();	//we are not adding SAN devices by now

	        DatacenterCharacteristics characteristics = 
                      new DatacenterCharacteristics(
	                arch, os, vmm, hostList, time_zone, costPerCpu, 
                        costPerMem, costPerStorage, costPerBw);


	        // 6. Finally, we need to create a PowerDatacenter object.
	        Datacenter datacenter = null;
	        try {
	            datacenter = 
                           new Datacenter(
                            name, characteristics, 
                            new VmAllocationPolicySimple(hostList), storageList, 0);
	        } catch (Exception e) {
	            e.printStackTrace(System.out);
	        }

	        return datacenter;
	}

        //We strongly encourage users to develop their own broker policies, to submit vms and cloudlets according
        //to the specific rules of the simulated scenario
        private static DatacenterBroker createBroker(){

            DatacenterBroker broker;
            try {
                    broker = new DatacenterBroker("Broker");
            } catch (Exception e) {
                    e.printStackTrace(System.out);
                    return null;
            }
            return broker;
        }

        /**
         * Prints the Cloudlet objects
         * @param list  list of Cloudlets
         */
        private static void printCloudletList(List<Cloudlet> list) {
            int size = list.size();
            Cloudlet cloudlet;

            Log.printLine();
            Log.printLine("========== OUTPUT ==========");
            String captions[] = {"Cloudlet ID", "STATUS   ",
                    "Data center ID", "VM ID", "Time" , "Start Time", "Finish Time"};
            LogUtils.printCaptions(captions);
            LogUtils.printLine(captions, new String[]{"int","string","int","int","secs","secs", "secs"});

            DecimalFormat dft = new DecimalFormat("###.##");
            for (int i = 0; i < size; i++) {
                cloudlet = list.get(i);

                if (cloudlet.getCloudletStatus() == Cloudlet.SUCCESS){
                    Object data[] = {cloudlet.getCloudletId(), "SUCCESS", cloudlet.getResourceId(), cloudlet.getVmId(),
                         dft.format(cloudlet.getActualCPUTime()), dft.format(cloudlet.getExecStartTime()),
                          dft.format(cloudlet.getFinishTime())};
                    LogUtils.printLine(captions, data);
                }
            }

        }
}
