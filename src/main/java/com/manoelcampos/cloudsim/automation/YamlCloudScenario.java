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
 * @author Manoel Campos da Silva Filho
 */
public class YamlCloudScenario {
    private List<DatacenterRegistry> datacenters;
    private List<CustomerRegistry> customers;

    public YamlCloudScenario(){
        this.datacenters = new ArrayList<>();
        this.customers = new ArrayList<>();
    }

    /**
     * Abstract information about data centers.
     * This object contains, for instance, the amount of datacenters
     * to be created and the host amount and configurations.
     * Each YAML scenario can have multiple datacenters
     * that are abstractly specified using DatacenterRegistry
     * objects. The concrete datacenters are
     * created by CloudSim.
     */ /**
     * @return the datacenters
     */
    public List<DatacenterRegistry> getDatacenters() {
        return datacenters;
    }

    /**
     * @param datacenters the datacenters to set
     */
    public void setDatacenters(final List<DatacenterRegistry> datacenters) {
        this.datacenters = datacenters;
    }

    /**
     * Abstract information about customers (brokers).
     * This object contains, for instance, the amount of customers
     * to be created and the VM amount and configurations.
     * Each YAML scenario can have multiple customers
     * that are abstractly specified using CustomerRegistry
     * objects. The concrete customers are
     * created by CloudSim as its DatacenterBroker objects.
     */ /**
     * @return the customers
     */
    public List<CustomerRegistry> getCustomers() {
        return customers;
    }

    /**
     * @param customers the customers to set
     */
    public void setCustomers(final List<CustomerRegistry> customers) {
        this.customers = customers;
    }
}
