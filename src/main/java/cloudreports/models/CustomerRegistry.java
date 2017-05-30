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

import org.cloudbus.cloudsim.cloudlets.Cloudlet;
import org.cloudbus.cloudsim.vms.Vm;

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
    private long id;
    private String name;
    private Integer amount;
    private List<VmRegistry> vms;
    private List<CloudletRegistry> cloudlets;

    public CustomerRegistry() {
        vms = new ArrayList<>();
        cloudlets = new ArrayList<>();
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
        this.vms = new LinkedList<>();
        this.vms.add(new VmRegistry());

        //Create the utilization profile
        this.cloudlets = new LinkedList<>();
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
     * Gets the list of {@link Vm}s owned by this customer.
     *
     * @return
     */
    public List<VmRegistry> getVms() {
        return vms;
    }

    /**
     * Sets the list of {@link Vm}s owned by this customer.
     *
     * @param   vms    the customer's virtual machines list to set.
     */
    public void setVms(List<VmRegistry> vms) {
        this.vms = vms;
    }

    /**
     * Gets the list of {@link Cloudlet}'s owned by this customer.
     *
     * @return
     */
    public List<CloudletRegistry> getCloudlets() {
        return cloudlets;
    }

    /**
     * Sets the list of {@link Cloudlet}'s owned by this customer.
     *
     * @param   cloudlets    the customer's cloudlets list to set.
     */
    public void setCloudlets(List<CloudletRegistry> cloudlets) {
        this.cloudlets = cloudlets;
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
        for(VmRegistry vmr : getVms()) {
            s.append("\n"+vmr.toString());
        }
        s.append("\n++End of virtual machines description++\n");

        s.append("\n++Utilization profile of this costumer++\n");
        s.append(this.getCloudlets().toString());
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

}
