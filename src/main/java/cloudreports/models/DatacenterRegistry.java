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

import org.cloudsimplus.allocationpolicies.VmAllocationPolicy;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * A datacenter registry stores information about a specific datacenter.
 * It contains the list of hosts owned by the datacenter, cost values and
 * other general specifications.
 *
 * @author      Thiago T. Sá
 * @since       1.0
 */
public final class DatacenterRegistry implements Serializable{
    private long id;
    private String name;
    private Integer amount;
    private String architecture;
    private String os;
    private String vmm;
    private double timeZone;
    private String vmAllocationPolicy;
    private boolean vmMigration;
    private List<HostRegistry> hosts;
    private double costPerSec;
    private double costPerMem;
    private double costPerStorage;
    private double costPerBw;
    private List<SanStorageRegistry> sans;
    private double upperUtilizationThreshold;
    private double lowerUtilizationThreshold;
    private double schedulingInterval;

    public DatacenterRegistry() {
        setAmount(1);
        sans = new ArrayList<>();
        hosts = new ArrayList<>();
        setArchitecture("x86");
        setOs("Linux");
        setVmm("Xen");
        setSchedulingInterval(0);
    }

    /**
     * Gets the datacenter's id.
     *
     * @return the datacenter's id.
     */
    public long getId() {
        return id;
    }

    /**
     * Sets the datacenter's id.
     *
     * @param   id  the datacenter's id.
     */
    public void setId(long id) {
        this.id = id;
    }

    /**
     * Gets the datacenter's name.
     *
     * @return the datacenter's name.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the datacenter's name.
     *
     * @param   name    the datacenter's name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the datacenter's architecture.
     *
     * @return the datacenter's architecture.
     */
    public String getArchitecture() {
        return architecture;
    }

    /**
     * Sets the datacenter's architecture.
     *
     * @param   architecture    the datacenter's architecture.
     */
    public void setArchitecture(String architecture) {
        this.architecture = architecture;
    }

    /**
     * Gets the datacenter's operating system.
     *
     * @return the datacenter's operating system.
     */
    public String getOs() {
        return os;
    }

    /**
     * Sets the datacenter's operating system.
     *
     * @param   os  the datacenter's operating system.
     */
    public void setOs(String os) {
        this.os = os;
    }

    /**
     * Gets the datacenter's hypervisor.
     *
     * @return the datacenter's hypervisor.
     */
    public String getVmm() {
        return vmm;
    }

    /**
     * Sets the datacenter's hypervisor.
     *
     * @param   vmm the datacenter's hypervisor.
     */
    public void setVmm(String vmm) {
        this.vmm = vmm;
    }

    /**
     * Gets the datacenter's time zone.
     *
     * @return the datacenter's time zone.
     */
    public double getTimeZone() {
        return timeZone;
    }

    /**
     * Sets the datacenter's time zone.
     *
     * @param   timeZone    the datacenter's time zone.
     */
    public void setTimeZone(double timeZone) {
        this.timeZone = timeZone;
    }

    /**
     * Gets the class name suffix for Datacenter's {@link VmAllocationPolicy}.
     *
     * @return
     */
    public String getVmAllocationPolicy() {
        return vmAllocationPolicy;
    }

    /**
     * Sets the datacenter's allocation policy.
     *
     * @param   vmAllocationPolicy   the datacenter's allocation policy.
     */
    public void setVmAllocationPolicy(String vmAllocationPolicy) {
        this.vmAllocationPolicy = vmAllocationPolicy;
    }

    /**
     * Checks if virtual machines migrations are enabled.
     *
     * @return  <code>true</code> if virtual machines migrations are enabled;
     *          <code>false</code> otherwise.
     */
    public boolean isVmMigration() {
        return vmMigration;
    }

    /**
     * Enables/disables virtual machines migrations.
     *
     * @param   vmMigration indicates if virtual machines migrations are enabled
     *                      or not
     */
    public void setVmMigration(boolean vmMigration) {
        this.vmMigration = vmMigration;
    }

    /**
     * Gets the datacenter's hosts list.
     *
     * @return the datacenter's host list.
     */
    public List<HostRegistry> getHosts() {
        return hosts;
    }

    /**
     * Sets the datacenter's hosts list.
     *
     * @param   hosts    the datacenter's host list.
     */
    public void setHosts(List<HostRegistry> hosts) {
        this.hosts = hosts;
    }

    /**
     * Gets the datacenter's cost by second of processing.
     *
     * @return the datacenter's cost by second of processing.
     */
    public double getCostPerSec() {
        return costPerSec;
    }

    /**
     * Sets the datacenter's cost by second of processing.
     *
     * @param   costPerSec  the datacenter's cost by second of processing.
     */
    public void setCostPerSec(double costPerSec) {
        this.costPerSec = costPerSec;
    }

    /**
     * Gets the datacenter's cost by RAM usage.
     *
     * @return the datacenter's cost by RAM usage.
     */
    public double getCostPerMem() {
        return costPerMem;
    }

    /**
     * Sets the datacenter's cost by RAM usage.
     *
     * @param   costPerMem  the datacenter's cost by RAM usage.
     */
    public void setCostPerMem(double costPerMem) {
        this.costPerMem = costPerMem;
    }

    /**
     * Gets the datacenter's cost by storage usage.
     *
     * @return the datacenter's cost by storage usage.
     */
    public double getCostPerStorage() {
        return costPerStorage;
    }

    /**
     * Sets the datacenter's cost by storage usage.
     *
     * @param   costPerStorage  the datacenter's cost by storage usage.
     */
    public void setCostPerStorage(double costPerStorage) {
        this.costPerStorage = costPerStorage;
    }

    /**
     * Gets the datacenter's cost by bandwidth usage.
     *
     * @return the datacenter's cost by bandwidth usage.
     */
    public double getCostPerBw() {
        return costPerBw;
    }

    /**
     * Sets the datacenter's cost by bandwidth usage.
     *
     * @param   costPerBw   the datacenter's cost by bandwidth usage.
     */
    public void setCostPerBw(double costPerBw) {
        this.costPerBw = costPerBw;
    }

    /**
     * Gets the datacenter's SAN list.
     *
     * @return the datacenter's SAN list.
     */
    public List<SanStorageRegistry> getSans() {
        return sans;
    }

    /**
     * Sets the datacenter's SAN list.
     *
     * @param   sans the datacenter's SAN list.
     */
    public void setSans(List<SanStorageRegistry> sans) {
        this.sans = sans;
    }

    /**
     * Gets the datacenter's scheduling interval.
     *
     * @return the datacenter's scheduling interval.
     */
    public double getSchedulingInterval() {
        return schedulingInterval;
    }

    /**
     * Sets the datacenter's scheduling interval.
     *
     * @param   schedulingInterval  the datacenter's scheduling interval.
     */
    public void setSchedulingInterval(double schedulingInterval) {
        this.schedulingInterval = schedulingInterval;
    }

    /**
     * Gets the datacenter's upper utilization threshold.
     *
     * @return the datacenter's upper utilization threshold.
     */
    public double getUpperUtilizationThreshold() {
        return upperUtilizationThreshold;
    }

    /**
     * Sets the datacenter's upper utilization threshold.
     *
     * @param   upperUtilizationThreshold   the datacenter's upper utilization
     *                                      threshold.
     */
    public void setUpperUtilizationThreshold(double upperUtilizationThreshold) {
        this.upperUtilizationThreshold = upperUtilizationThreshold;
    }

    /**
     * Gets the datacenter's lower utilization threshold.
     *
     * @return the datacenter's lower utilization threshold.
     */
    public double getLowerUtilizationThreshold() {
        return lowerUtilizationThreshold;
    }

    /**
     * Sets the datacenter's lower utilization threshold.
     *
     * @param lowerUtilizationThreshold the datacenter's lower utilization
     *                                  threshold.
     */
    public void setLowerUtilizationThreshold(double lowerUtilizationThreshold) {
        this.lowerUtilizationThreshold = lowerUtilizationThreshold;
    }

    @Override
    public boolean equals(Object datacenter){
      if ( this == datacenter ) return true;
      if ( !(datacenter instanceof DatacenterRegistry) ) return false;
      DatacenterRegistry dr = (DatacenterRegistry)datacenter;
      return this.getName().equals(dr.getName());
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 79 * hash + (this.getName() != null ? this.getName().hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder("Name="+getName()+"\n");
        s.append("Architecture=").append(getArchitecture()).append("\n");
        s.append("Operating System=").append(getOs()).append("\n");
        s.append("Hypervisor=").append(getVmm()).append("\n");
        s.append("Allocation Policy ID=").append(getVmAllocationPolicy()).append("\n");
        s.append("Time Zone (GMT)=").append(getTimeZone()).append("\n");
        s.append("VM Migrations=").append(isVmMigration()).append("\n");
        s.append("Upper Utilization threshold=").append(getUpperUtilizationThreshold()).append("\n");
        s.append("Lower Utilization threshold=").append(getLowerUtilizationThreshold()).append("\n");
        s.append("Scheduling interval=").append(getSchedulingInterval()).append("\n");
        s.append("Processing Cost=").append(getCostPerSec()).append("\n");
        s.append("Memory Cost=").append(getCostPerMem()).append("\n");
        s.append("Storage Cost=").append(getCostPerStorage()).append("\n");
        s.append("Bandwidth Cost=").append(getCostPerBw()).append("\n");

        s.append("\n++Beginning of hosts list++\n");
        for(HostRegistry hr : getHosts()) {
            s.append("\n").append(hr.toString());
        }
        s.append("\n++End of hosts list++\n");

        s.append("\n++Beginning of SAN list++\n");
        for(SanStorageRegistry sr : getSans()) {
            s.append("\n").append(sr.toString());
        }
        s.append("\n++End of SAN list++\n");

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

}
