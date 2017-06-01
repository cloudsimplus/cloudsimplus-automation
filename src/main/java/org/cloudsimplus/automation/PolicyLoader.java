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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cloudsimplus.automation;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import cloudreports.models.DatacenterRegistry;
import cloudreports.models.HostRegistry;
import cloudreports.models.VmRegistry;
import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicy;
import org.cloudbus.cloudsim.provisioners.PeProvisioner;
import org.cloudbus.cloudsim.provisioners.ResourceProvisioner;
import org.cloudbus.cloudsim.schedulers.cloudlet.CloudletScheduler;
import org.cloudbus.cloudsim.schedulers.vm.VmScheduler;
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModel;

/**
 * Dynamically creates instances of classes such as {@link VmScheduler}, {@link VmAllocationPolicy},
 * {@link CloudletScheduler}, {@link ResourceProvisioner} and others from the class name of
 * the object to be instantiated.
 *
 * @author Manoel Campos da Silva Filho
 */
public class PolicyLoader {
    /**
     * Map of already loaded class, which are stored
     * to speedup getting a class from its name.
     * This way, after a class is get from the first time,
     * it isn't used reflection anymore when a class with the same
     * name is requested again.
     * Each key is a full class name and each value is the class itself.
     */
    private static final Map<String, Class> map = new HashMap<>();

    /**
     * The base CloudSim package name.
     */
    private static final String PKG = "org.cloudbus.cloudsim";

    /**
     * Try to get a class corresponding to its full name from
     * the map of already loaded classes.
     * If the class was not loaded yet, try to load
     * and return it.
     * @param fullClassName the full qualified name of the class (including package name)
     * @return the loaded class
     */
    private static <T> Class<T> loadClass(final String fullClassName){
        Class<T> klass = map.get(fullClassName);
        if(klass == null){
            try {
                klass = (Class<T>) Class.forName(fullClassName);;
                map.put(fullClassName, klass);
                return klass;
            } catch (ClassNotFoundException e) {
                Logger.getLogger(PolicyLoader.class.getName()).log(Level.SEVERE, null, e);
                throw new RuntimeException(e);
            }
        }

        return klass;
    }

    public static VmScheduler vmScheduler(final String classSuffix) throws RuntimeException {
        try {
            final String className = generateFullClassName(PKG+".schedulers.vm","VmScheduler", classSuffix);
            Class<VmScheduler> klass = PolicyLoader.<VmScheduler>loadClass(className);
            Constructor cons = klass.getConstructor(new Class[]{});
            return (VmScheduler) cons.newInstance();
        } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            Logger.getLogger(PolicyLoader.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        }
    }

    /**
     * Gets an instance of a resource provisioner with a given
     * class name information.
     *
     * @param classPrefix The class prefix for the provisioner.
     * If you want to instantiate the provisioner class BwProvisionerSimple,
     * the provisioner prefix is "Bw"
     * @param classSufix The class suffix of the provisioner.
     * If you want to instantiate the provisioner class BwProvisionerSimple,
     * the provisioner suffix is just "Simple"
     * @return A new instance of the provisioner with the given name.
     * For instance, if the class suffix is "Simple",
     * returns an instance the ResourceProvisionerSimple class.
     * @throws RuntimeException
     */
    private static <T extends ResourceProvisioner> T resourceProvisioner(
        final String classPrefix, final String classSufix) throws RuntimeException {
        try {
            final String className = generateFullProvisionerClassName(classPrefix, classSufix);
            Class<ResourceProvisioner> klass = PolicyLoader.<ResourceProvisioner>loadClass(className);
            Constructor cons = klass.getConstructor(new Class[]{});
            return (T)cons.newInstance();
        } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            Logger.getLogger(PolicyLoader.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        }
    }

    public static ResourceProvisioner newResourceProvisioner(final HostRegistry hr) throws RuntimeException {
        return resourceProvisioner("", hr.getBwProvisioner());
    }

    public static PeProvisioner newPeProvisioner(final HostRegistry hr) throws RuntimeException {
        return resourceProvisioner("Pe", hr.getPeProvisioner());
    }

    public static VmAllocationPolicy vmAllocationPolicy(final DatacenterRegistry dcr) throws RuntimeException {
        try {
            final String className = generateFullClassName(PKG+".allocationpolicies","VmAllocationPolicy", dcr.getVmAllocationPolicy());
            Class<VmAllocationPolicy> klass = PolicyLoader.<VmAllocationPolicy>loadClass(className);
            Constructor cons = klass.getConstructor(new Class[]{});
            return (VmAllocationPolicy) cons.newInstance();
        } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            Logger.getLogger(PolicyLoader.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        }
    }

    public static CloudletScheduler cloudletScheduler(final VmRegistry vmr) throws RuntimeException {
        try {
            final String className = generateFullClassName(PKG+".schedulers.cloudlet","CloudletScheduler", vmr.getCloudletScheduler());
            Class<CloudletScheduler> klass = PolicyLoader.<CloudletScheduler>loadClass(className);
            Constructor cons = klass.getConstructor();
            return (CloudletScheduler) cons.newInstance();
        } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            Logger.getLogger(PolicyLoader.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        }
    }

    private static String generateFullClassName(String packageName, String classPrefix, String classSuffix) {
        return String.format("%s.%s%s", packageName, classPrefix, classSuffix);
    }

    private static String generateFullProvisionerClassName(String classPrefix, String classSuffix) {
        classPrefix = (classPrefix.isEmpty() ? "ResourceProvisioner" : classPrefix+"Provisioner");
        return generateFullClassName(PKG+".provisioners", classPrefix, classSuffix);
    }

    public static UtilizationModel utilizationModel(final String classSuffix) throws RuntimeException {
        try {
            final String className = generateFullClassName(PKG+".utilizationmodels", "UtilizationModel", classSuffix);
            Class<UtilizationModel> klass = PolicyLoader.<UtilizationModel>loadClass(className);
            Constructor cons = klass.getConstructor();
            return (UtilizationModel) cons.newInstance();
        } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            Logger.getLogger(PolicyLoader.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        }
    }
}
