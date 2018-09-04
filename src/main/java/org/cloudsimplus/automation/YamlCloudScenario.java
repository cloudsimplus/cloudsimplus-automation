/*
 * CloudSim Plus Automation: A Human Readable Scenario Specification for Automated Creation of Simulations on CloudSim Plus.
 * https://github.com/manoelcampos/CloudSimAutomation
 *
 *     Copyright (C) 2015-2016  Universidade da Beira Interior (UBI, Portugal) and
 *     the Instituto Federal de Educação Ciência e Tecnologia do Tocantins (IFTO, Brazil).
 *
 *     This file is part of CloudSim Automation.
 *
 *     CloudSim Plus Automation is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     CloudSim Plus Automation is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with CloudSim Automation. If not, see <http://www.gnu.org/licenses/>.
 */
package org.cloudsimplus.automation;

import cloudreports.models.*;
import com.esotericsoftware.yamlbeans.YamlReader;

import java.util.*;

/**
 * Represents a Cloud Computing simulation scenario read from an YAML file.
 * Each scenario inside an YAML file is represented by an object of this class
 * and is used to create a CloudSim Plus simulation.
 * It stores all data representing the simulation to be created in <a href="http://cloudsimplus.org">CloudSim Plus</a>
 * and enables one to build and execute the simulation.
 *
 * <p><b>Objects of this class are created automatically by
 * a {@link YamlCloudScenarioReader} class.</b>
 * After you have one of these instances, you may
 * pass it to the the {@link CloudSimulation} constructor
 * to create the Cloud Computing simulation using CloudSim Plus
 * and further run it.</p>
 *
 * <p>
 * Each scenario inside the YAML file can be delimited using 3 dashes:<br>
 * <center><b>---</b></center>
 * </p>
 *
 * @author Manoel Campos da Silva Filho
 * @see YamlCloudScenarioReader
 */
public class YamlCloudScenario {
    /** @see #getDatacenters() */
    private List<DatacenterRegistry> datacenters;

    /** @see #getCustomers() */
    private List<CustomerRegistry> customers;

    /**
     * A default constructor that is called by a {@link YamlReader} using
     * reflection. This way, usually objects of this class don't have to be created manually.
     *
     * @see YamlCloudScenarioReader
     */
    public YamlCloudScenario() {
        this.customers = new ArrayList<>();
        this.datacenters = new ArrayList<>();
    }

    /**
     * Gets a List of {@link DatacenterRegistry} objects representing information about data centers.
     * These objects contain, for instance, the amount of datacenters
     * to be created and the host amount and configurations.
     *
     * <p>Each YAML scenario can have multiple datacenters
     * that are abstractly specified using DatacenterRegistry
     * objects. The concrete datacenters are
     * created by CloudSim Plus.</p>
     * @return
     */
    public List<DatacenterRegistry> getDatacenters() {
        return datacenters;
    }

    /**
     * Gets a List of {@link DatacenterRegistry} objects.
     *
     * @param datacenters the datacenters to set
     * @see #getDatacenters()
     */
    public void setDatacenters(final List<DatacenterRegistry> datacenters) {
        if(datacenters == null){
            return;
        }
        this.datacenters = datacenters;
    }

    /**
     * Gets a List of {@link CustomerRegistry} objects representing abstract information about customers (brokers).
     * These objects contain, for instance, the amount of customers
     * to be created and the VM amount and configurations.
     *
     * <p>Each YAML scenario can have multiple customers
     * that are abstractly specified using CustomerRegistry
     * objects. The concrete customers are
     * created by CloudSim Plus as its DatacenterBroker objects.
     * </p>
     * @return
     */
    public List<CustomerRegistry> getCustomers() {
        return customers;
    }

    /**
     * Sets a List of {@link CustomerRegistry} objects representing abstract information about customers (brokers).
     * @param customers the customers to set
     * @see #getCustomers()
     */
    public void setCustomers(final List<CustomerRegistry> customers) {
        if(customers == null){
            return;
        }
        this.customers = customers;
    }
}
