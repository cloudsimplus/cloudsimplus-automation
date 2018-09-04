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

import org.cloudbus.cloudsim.power.models.PowerModel;
import org.cloudbus.cloudsim.provisioners.PeProvisioner;
import org.cloudbus.cloudsim.provisioners.ResourceProvisioner;
import org.cloudbus.cloudsim.resources.Bandwidth;
import org.cloudbus.cloudsim.schedulers.vm.VmScheduler;

import java.io.Serializable;

/**
 * A host registry stores information about a specific host configuration.
 * It contains general information such as scheduling policy, power specifications
 * and amount of resources.
 *
 * @author      Thiago T. Sá
 * @since       1.0
 */
public final class HostRegistry implements Serializable {
    private int id;
    private int pes;
    private double mips;
    private double maxPower;
    private double staticPowerPercent;
    private int ram;
    private long bw;
    private String ramProvisioner;
    private String bwProvisioner;
    private String peProvisioner;
    private String vmScheduler;
    private String powerModel;
    private int amount;
    private long storage;

    public HostRegistry() {
        setAmount(1);
    }

    /**
     * Gets the host's id.
     *
     * @return the host's id.
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the host's id.
     *
     * @param   id  the host's id.
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Gets the class name suffix for the Host's {@link VmScheduler}.
     *
     * @return
     */
    public String getVmScheduler() {
        return vmScheduler;
    }

    /**
     * Sets the host's scheduling policy.
     *
     * @param   vmScheduler   the host's scheduling policy.
     */
    public void setVmScheduler(String vmScheduler) {
        this.vmScheduler = vmScheduler;
    }

    /**
     * Gets the host's number of processing elements.
     *
     * @return the host's number of processing elements.
     */
    public int getPes() {
        return pes;
    }

    /**
     * Sets the host's number of processing elements.
     *
     * @param   numOfpes    the host's number of processing elements.
     */
    public void setPes(int numOfpes) {
        this.pes = numOfpes;
    }

    /**
     * Gets the amount of mips per processing elements.
     *
     * @return the amount of mips per processing elements.
     */
    public double getMips() {
        return mips;
    }

    /**
     * Sets the amount of mips per processing elements.
     *
     * @param   mips   the amount of mips per processing elements.
     */
    public void setMips(double mips) {
        this.mips = mips;
    }

    /**
     * Gets the host's maximum power consumption.
     *
     * @return the host's maximum power consumption.
     */
    public double getMaxPower() {
        return maxPower;
    }

    /**
     * Sets the host's maximum power consumption.
     *
     * @param   maxPower    the host's maximum power consumption.
     */
    public void setMaxPower(double maxPower) {
        this.maxPower = maxPower;
    }

    /**
     * Gets the host's static power consumption percent.
     *
     * @return the host's static power consumption percent.
     */
    public double getStaticPowerPercent() {
        return staticPowerPercent;
    }
    /**
     * Sets the host's static power consumption percent.
     *
     * @param   staticPowerPercent  the host's static power consumption percent.
     */
    public void setStaticPowerPercent(double staticPowerPercent) {
        this.staticPowerPercent = staticPowerPercent;
    }

    /**
     * Gets the name suffix of the class for Host's {@link PowerModel}.
     *
     * @return
     */
    public String getPowerModel() {
        return powerModel;
    }

    /**
     * Sets the host's power model alias.
     *
     * @param   powerModel the host's power model alias.
     */
    public void setPowerModel(String powerModel) {
        this.powerModel = powerModel;
    }

    /**
     * Gets the host's amount of RAM.
     *
     * @return the host's amount of RAM.
     */
    public int getRam() {
        return ram;
    }

    /**
     * Sets the host's amount of RAM.
     *
     * @param   ram the host's amount of RAM.
     */
    public void setRam(int ram) {
        this.ram = ram;
    }

    /**
     * Gets the host's bandwidth.
     *
     * @return the host's bandwidth.
     */
    public long getBw() {
        return bw;
    }

    /**
     * Sets the host's bandwidth.
     *
     * @param   bw  the host's bandwidth.
     */
    public void setBw(long bw) {
        this.bw = bw;
    }

    /**
     * Gets the name suffix of the class for the host's RAM {@link ResourceProvisioner}.
     *
     * @return
     */
    public String getRamProvisioner() {
        return ramProvisioner;
    }

    /**
     * Sets the name suffix of the class for the host's RAM {@link ResourceProvisioner}.
     *
     * @param   ramProvisioner the host's RAM provisioner class name suffix to set.
     */
    public void setRamProvisioner(String ramProvisioner) {
        this.ramProvisioner = ramProvisioner;
    }

    /**
     * Gets the name suffix of the class for Host's {@link Bandwidth} {@link ResourceProvisioner}.
     *
     * @return
     */
    public String getBwProvisioner() {
        return bwProvisioner;
    }

    /**
     * Sets the name suffix of the class for Host's {@link Bandwidth} {@link ResourceProvisioner}.
     *
     * @param bwProvisioner    the host's bandwidth provisioner class name suffix to set.
     */
    public void setBwProvisioner(String bwProvisioner) {
        this.bwProvisioner = bwProvisioner;
    }


    /**
     * Gets the name suffix of the class for the Host's {@link PeProvisioner}.
     *
     * @return
     */
    public String getPeProvisioner() {
        return peProvisioner;
    }

    /**
     * Sets the name suffix of the class for the Host's {@link PeProvisioner}.
     *
     * @param   peProvisioner  the host's processing elements provisioner class name suffix to set.
     */
    public void setPeProvisioner(String peProvisioner) {
        this.peProvisioner = peProvisioner;
    }

    /**
     * Gets the amount of hosts with this configuration.
     *
     * @return the amount of hosts with this configuration.
     */
    public int getAmount() {
        return amount;
    }

    /**
     * Sets the amount of hosts with this configuration.
     *
     * @param   amount  the amount of hosts with this configuration.
     */
    public void setAmount(int amount) {
        this.amount = amount;
    }

    /**
     * Gets the host's storage capacity.
     *
     * @return the host's storage capacity.
     */
    public long getStorage() {
        return storage;
    }

    /**
     * Sets the host's storage capacity.
     *
     * @param   storage the host's storage capacity.
     */
    public void setStorage(long storage) {
        this.storage = storage;
    }

    /**
     * Indicates whether the host can allocate a virtual machine or not.
     *
     * @param   vmr     the virtual machine to be allocated.
     * @return          <code>true</code> if the host can allocate the virtual
     *                  machine; <code>false</code> otherwise.
     * @since           1.0
     */
    public boolean canRunVM(VmRegistry vmr) {
        if(this.getRam()<vmr.getRam()) return false;
        if((this.getPes()*this.getMips()) < vmr.getMips()) return false;
        if(this.getBw()<vmr.getBw()) return false;
        if(this.getStorage()<vmr.getSize()) return false;

        return true;
    }

    @Override
    public boolean equals(Object host){
      if ( this == host ) return true;
      if ( !(host instanceof HostRegistry) ) return false;
      HostRegistry hr = (HostRegistry)host;
      return this.getId() == hr.getId();
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + (int) (this.id ^ (this.id >>> 32));
        return hash;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder("Host Id="+getId()+"\n");
        s.append("Amount="+getAmount()+"\n");
        s.append("VM Scheduling ="+ getVmScheduler()+"\n");
        s.append("Processing Elements="+ getPes()+"\n");
        s.append("MIPS/PE="+ getMips()+"\n");
        s.append("PE Provisioner ="+ getPeProvisioner()+"\n");
        s.append("Maximum Power="+getMaxPower()+"\n");
        s.append("Static Power Percent="+getStaticPowerPercent()+"\n");
        s.append("Power Model ="+ getPowerModel()+"\n");
        s.append("RAM="+getRam()+"\n");
        s.append("RAM Provisioner ="+ getRamProvisioner()+"\n");
        s.append("Bandwidth="+getBw()+"\n");
        s.append("Bandwidth Provisioner ="+ getBwProvisioner()+"\n");
        s.append("Storage="+getStorage()+"\n");

        return s.toString();
    }
}
