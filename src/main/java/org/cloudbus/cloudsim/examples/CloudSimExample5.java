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
import org.cloudbus.cloudsim.VmSchedulerSpaceShared;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;


/**
 * A simple example showing how to create
 * two datacenters with one host each and
 * run cloudlets of two users on them.
 */
public class CloudSimExample5 {

	/** The cloudlet lists. */
	private static List<Cloudlet> cloudletList1;
	private static List<Cloudlet> cloudletList2;

	/** The vmlists. */
	private static List<Vm> vmlist1;
	private static List<Vm> vmlist2;

	/**
	 * Creates main() to run this example
	 */
	public static void main(String[] args) {

		Log.printLine("Starting CloudSimExample5...");

		try {
			// First step: Initialize the CloudSim package. It should be called
			// before creating any entities.
			int num_user = 2;   // number of cloud users 
			Calendar calendar = Calendar.getInstance();
			boolean trace_flag = false;  // mean trace events

			// Initialize the CloudSim library
			CloudSim.init(num_user, calendar, trace_flag);

			// Second step: Create Datacenters
			//Datacenters are the resource providers in CloudSim. We need at list one of them to run a CloudSim simulation
			@SuppressWarnings("unused")
			Datacenter datacenter0 = createDatacenter("Datacenter_0");
			@SuppressWarnings("unused")
			Datacenter datacenter1 = createDatacenter("Datacenter_1");

			//Third step: Create Brokers for the users
			DatacenterBroker broker1 = createBroker(1);
			int brokerId1 = broker1.getId();

			DatacenterBroker broker2 = createBroker(2);
			int brokerId2 = broker2.getId();

			//Fourth step: Create one virtual machine for each broker/user
			vmlist1 = new ArrayList<>();
			vmlist2 = new ArrayList<>();

			//VM description
			int vmid = 0;
			int vmMips = 250; //MIPS = Millions of Instructions Per Second
			long vmSize = 10000; //image size (MB)
			int vmRam = 512; //vm memory (MB)
			long vmBw = 1000; //1.000 bits/s = 1Mbps
			int vmPesNumber = 1; //number of cpus
			String vmm = "Xen"; //VMM name

			//create two VMs: the first one belongs to user1
			Vm vm1 = new Vm(vmid, brokerId1, vmMips, vmPesNumber, vmRam, vmBw, vmSize, vmm, new CloudletSchedulerTimeShared());

                        //TODO: VMs with the same id????
			//the second VM: this one belongs to user2
			Vm vm2 = new Vm(vmid, brokerId2, vmMips, vmPesNumber, vmRam, vmBw, vmSize, vmm, new CloudletSchedulerTimeShared());

			//add the VMs to the vmlists
			vmlist1.add(vm1);
			vmlist2.add(vm2);

			//submit vm list to the broker
			broker1.submitVmList(vmlist1);
			broker2.submitVmList(vmlist2);

			//Fifth step: Create two Cloudlets
			cloudletList1 = new ArrayList<>();
			cloudletList2 = new ArrayList<>();

			//Cloudlet properties
			int cloudletId = 0;
			long length = 40000;
			long fileSize = 300;
			long outputSize = 300;
			UtilizationModel utilizationModel = new UtilizationModelFull();

			Cloudlet cloudlet1 = new Cloudlet(cloudletId, length, vmPesNumber, fileSize, outputSize, utilizationModel, utilizationModel, utilizationModel);
			cloudlet1.setUserId(brokerId1);

			Cloudlet cloudlet2 = new Cloudlet(cloudletId, length, vmPesNumber, fileSize, outputSize, utilizationModel, utilizationModel, utilizationModel);
			cloudlet2.setUserId(brokerId2);

			//add the cloudlets to the lists: each cloudlet belongs to one user
			cloudletList1.add(cloudlet1);
			cloudletList2.add(cloudlet2);

			//submit cloudlet list to the brokers
			broker1.submitCloudletList(cloudletList1);
			broker2.submitCloudletList(cloudletList2);

			// Sixth step: Starts the simulation
			CloudSim.startSimulation();

			// Final step: Print results when simulation is over
			List<Cloudlet> newList1 = broker1.getCloudletReceivedList();
			List<Cloudlet> newList2 = broker2.getCloudletReceivedList();

			CloudSim.stopSimulation();

			printCloudletList("=============> OUTPUT: User "+brokerId1+"    ", newList1);

			printCloudletList("=============> OUTPUT: User "+brokerId2+"    ", newList2);

			Log.printLine("CloudSimExample5 finished!");
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

		int mips=1000;

		// 3. Create PEs and add these into a list.
		peList.add(new Pe(0, new PeProvisionerSimple(mips))); // need to store Pe id and MIPS Rating

		//4. Create Host with its id and list of PEs and add them to the list of machines
		int hostId=0;
		int ram = 2048; //host memory (MB)
		long storage = 1000000; //host storage
		int bw = 10000; //10.000 bits/s = 10Mbps


		//in this example, the VMAllocatonPolicy in use is SpaceShared. It means that only one VM
		//is allowed to run on each Pe. As each Host has only one Pe, only one VM can run on each Host.
		hostList.add(
    			new Host(
    				hostId,
    				new RamProvisionerSimple(ram),
    				new BwProvisionerSimple(bw),
    				storage,
    				peList,
    				new VmSchedulerSpaceShared(peList)
    			)
    		); // This is our first machine

		// 5. Create a DatacenterCharacteristics object that stores the
		//    properties of a data center: architecture, OS, list of
		//    Machines, allocation policy: time- or space-shared, time zone
		//    and its price (G$/Pe time unit).
		String arch = "x86";      // system architecture
		String os = "Linux";          // operating system
		String vmm = "Xen";
		double time_zone = 10.0;         // time zone this resource located
		double cost = 3.0;              // the cost of using processing in this resource
		double costPerMem = 0.05;		// the cost of using memory in this resource
		double costPerStorage = 0.001;	// the cost of using storage in this resource
		double costPerBw = 0.0;			// the cost of using bw in this resource
		LinkedList<Storage> storageList = new LinkedList<>();	//we are not adding SAN devices by now

		DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
                arch, os, vmm, hostList, time_zone, cost, costPerMem, costPerStorage, costPerBw);


		// 6. Finally, we need to create a PowerDatacenter object.
		Datacenter datacenter = null;
		try {
			datacenter = new Datacenter(name, characteristics, new VmAllocationPolicySimple(hostList), storageList, 0);
		} catch (Exception e) {
			e.printStackTrace(System.out);
		}

		return datacenter;
	}

	//We strongly encourage users to develop their own broker policies, to submit vms and cloudlets according
	//to the specific rules of the simulated scenario
	private static DatacenterBroker createBroker(int id){

		DatacenterBroker broker;
		try {
			broker = new DatacenterBroker("Broker"+id);
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
	private static void printCloudletList(String title, List<Cloudlet> list) {
		int size = list.size();
		Cloudlet cloudlet;

		String indent = "    ";
		Log.printLine();
		Log.printLine(title);
                String[] captions = {"CloudletID", "STATUS  ",
				"DataCenterID", "VmID",  "ExecTime",
				"Start Time", "Finish Time"};
		LogUtils.printCaptions(captions);
                LogUtils.printLine(captions, new String[]{"int","string","int","int","secs","secs", "secs"});

		DecimalFormat dft = new DecimalFormat("###.##");
                
		for (int i = 0; i < size; i++) {
			cloudlet = list.get(i);
			if (cloudlet.getCloudletStatus() == Cloudlet.SUCCESS) {
                                Object[] data = {
                                    cloudlet.getCloudletId(), "SUCCESS", cloudlet.getResourceId(), 
                                    cloudlet.getVmId(), 
                                    dft.format(cloudlet.getActualCPUTime()), 
                                    dft.format(cloudlet.getExecStartTime()), 
                                    dft.format(cloudlet.getFinishTime())};
                                
                                LogUtils.printLine(captions, data);
			}
		}

	}
}
