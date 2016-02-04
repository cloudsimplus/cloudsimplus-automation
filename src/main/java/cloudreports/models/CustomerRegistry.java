/* 
 * Copyright (c) 2010-2012 Thiago T. Sá
 * 
 * This file is part of CloudReports.
 *
 * CloudReports is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * CloudReports is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * For more information about your rights as a user of CloudReports,
 * refer to the LICENSE file or see <http://www.gnu.org/licenses/>.
 */

package cloudreports.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * A customer registry stores information about a specific customer.
 * It contains the list of virtual machines owned by the customer as well as 
 * its resources utilization profile.
 * 
 * @author      Thiago T. Sá
 * @since       1.0
 */
public final class CustomerRegistry implements Serializable{

    /** The customer's id. */
    private long id;
    
    /** The customer's name. */
    private String name;
    
    private Integer amount;
    
    /**
     * The multiplicative factor that, if specified,
     * indicate how:
     * - the RAM must be defined in terms of CPU, when the RAM is not specified
     * - the CPU must be defined in terms of RAM, when the CPU is not specified
     */
    private double ramCpuFactor;
    
    /**
     * Defines the mean of cloudlet's length (in MIPS).
     * If specified, the length of each created cloudlet
     * will be dynamic, defined based on this mean.
     */
    private double cloudletLengthMean = 0;

    /** The list of virtual machines owned by the customer. */
    private List<VirtualMachineRegistry> vmList;
    
    /** The resources utilization profile. */
    private List<UtilizationProfile> utilizationProfile;

    /** The default constructor. */
    public CustomerRegistry() {
        vmList = new ArrayList<>();
        utilizationProfile = new ArrayList<>();
        setAmount(1);
    }
    
    /** 
     * Creates a new customer registry with the given name. 
     * 
     * @param   name    the name of the customer registry.
     * @since           1.0
     */
    public CustomerRegistry(String name) {
        this();
        this.name = name;
        
        //Create VM list
        this.vmList = new LinkedList<>();
        this.vmList.add(new VirtualMachineRegistry());
        
        //Create the utilization profile
        this.utilizationProfile = new LinkedList<>();
    }

    /**
     * Gets the customer's id.
     * 
     * @return the customer's id.
     */
    public long getId() {
        return id;
    }

    /**
     * Sets the customer's id.
     * 
     * @param   id  the customer's id.
     */
    public void setId(long id) {
        this.id = id;
    }
    
    /**
     * Gets the customer's name.
     * 
     * @return the customer's name.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the customer's name.
     * 
     * @param   name    the customer's name.
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * Gets the customer's virtual machines list.
     * 
     * @return the customer's virtual machines list.
     */
    public List<VirtualMachineRegistry> getVmList() {
        return vmList;
    }

    /**
     * Sets the customer's virtual machines list.
     * 
     * @param   vmList    the customer's virtual machines list.
     */
    public void setVmList(List<VirtualMachineRegistry> vmList) {
        this.vmList = vmList;
    }
    
    /**
     * Gets the customer's utilization profile.
     * 
     * @return the customer's utilization profile.
     */
    public List<UtilizationProfile> getUtilizationProfile() {
        return utilizationProfile;
    }

    /**
     * Sets the customer's utilization profile.
     * 
     * @param   utilizationProfile    the customer's utilization profile.
     */
    public void setUtilizationProfile(List<UtilizationProfile> utilizationProfile) {
        this.utilizationProfile = utilizationProfile;
    }

    @Override
    public boolean equals(Object customer){
      if ( this == customer ) return true;
      if ( !(customer instanceof CustomerRegistry) ) return false;
      CustomerRegistry cr = (CustomerRegistry)customer;
      return this.getName().equals(cr.getName());
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + (this.getName() != null ? this.getName().hashCode() : 0);
        return hash;
    }
    
    @Override
    public String toString() {
        StringBuilder s = new StringBuilder("Name="+getName()+"\n");

        s.append("\n++List of Virtual Machines per user from this customer++\n");
        for(VirtualMachineRegistry vmr : getVmList()) {
            s.append("\n"+vmr.toString());
        }
        s.append("\n++End of virtual machines description++\n");

        s.append("\n++Utilization profile of this costumer++\n");
        s.append(this.getUtilizationProfile().toString());
        s.append("\n++End of utilization profile description++\n");

        return s.toString();
    }

    /**
     * @return the amount
     */
    public Integer getAmount() {
        return amount;
    }

    /**
     * @param amount the amount to set
     */
    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    /**
     * @return the ramCpuFactor
     */
    public double getRamCpuFactor() {
        return ramCpuFactor;
    }

    /**
     * @param ramCpuFactor the ramCpuFactor to set
     */
    public void setRamCpuFactor(double ramCpuFactor) {
        this.ramCpuFactor = ramCpuFactor;
    }

    /**
     * @return the cloudletLengthMean
     */
    public double getCloudletLengthMean() {
        return cloudletLengthMean;
    }

    /**
     * @param cloudletLengthMean the cloudletLengthMean to set
     */
    public void setCloudletLengthMean(double cloudletLengthMean) {
        this.cloudletLengthMean = cloudletLengthMean;
    }

}
