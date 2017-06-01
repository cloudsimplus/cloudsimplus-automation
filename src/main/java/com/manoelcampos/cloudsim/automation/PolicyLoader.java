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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.manoelcampos.cloudsim.automation;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.cloudbus.cloudsim.CloudletScheduler;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.VmAllocationPolicy;
import org.cloudbus.cloudsim.VmScheduler;
import org.cloudbus.cloudsim.provisioners.BwProvisioner;
import org.cloudbus.cloudsim.provisioners.PeProvisioner;
import org.cloudbus.cloudsim.provisioners.RamProvisioner;

/* @todo Utilizar a classe do CloudReports para carregamento dinâmico de classes de políticas.
 * O grande problema é que as classes do CloudReports tem um alto acoplamento,
 * criando muitas dependências umas das outras, como depender
 * de classes da GUI ou do ORM.
 */

/**
 * Dynamically creates instances of classes such as VmScheduler, VmProvisioner, 
 * resource provisioners and others from the class name of
 * the object to be instantiated.
 * 
 * @author Manoel Campos da Silva Filho
 */
public class PolicyLoader {
    private static final String PKG = "org.cloudbus.cloudsim";

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
     * Try to get a class corresponding to its full name from
     * the map of already loaded classes.
     * If the class was not loaded yet, try to load and return it.
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

    public static VmScheduler vmScheduler(final String classSuffix, final List<? extends Pe> pes) throws RuntimeException {
        try {
            final String className = generateFullClassName("VmScheduler", classSuffix);
            Class<VmScheduler> klass = PolicyLoader.<VmScheduler>loadClass(className);
            Constructor cons = klass.getConstructor(new Class[]{List.class});
            return (VmScheduler) cons.newInstance(pes);
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
     * @param classSuffix The class suffix of the provisioner.
     * If you want to instantiate the provisioner class BwProvisionerSimple,
     * the provisioner suffix is just "Simple"
     * @param resourceCapacity The resource capacity the provisioner has available to manage
     * @param resourceClass The class of the resource capacity property of the provisioner
     * @return A new instance of the provisioner with the given name.
     * For instance, if the class prefix is "Bw" and class suffix is "Simple", 
     * returns an instance the BwProvisionerSimple class.
     * @throws RuntimeException 
     */
    private static Object resourceProvisioner(
            final String classPrefix, final String classSuffix, Number resourceCapacity,
            Class<? extends Number> resourceClass) throws RuntimeException {
        try {
            final String className = generateFullProvisionerClassName(classPrefix, classSuffix);
            Class klass = loadClass(className);
            Constructor cons = klass.getConstructor(new Class[]{resourceClass});
            return cons.newInstance(resourceCapacity);
        } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            Logger.getLogger(PolicyLoader.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        }
    }

    public static BwProvisioner newBwProvisioner(
            final String classPrefix, final String classSuffix, final long bwCapacity) throws RuntimeException {
        Object obj = resourceProvisioner(classPrefix, classSuffix, bwCapacity, long.class);
        if(obj != null && obj instanceof BwProvisioner) {
            return (BwProvisioner) obj;
        }
            
        return null;
    }
    
    public static RamProvisioner newRamProvisioner(
            final String classPrefix, final String classSufix, final int ramCapacity) throws RuntimeException {
        Object obj = resourceProvisioner(classPrefix, classSufix, ramCapacity, int.class);
        if(obj != null && obj instanceof RamProvisioner) {
            return (RamProvisioner) obj;
        }
            
        return null;
    }

    public static PeProvisioner newPeProvisioner(
            final String classPrefix, final String classSuffix, final double peCapacity) throws RuntimeException {
        Object obj = resourceProvisioner(classPrefix, classSuffix, peCapacity, double.class);
        if(obj != null && obj instanceof PeProvisioner) {
            return (PeProvisioner) obj;
        }
            
        return null;
    }

    public static VmAllocationPolicy vmAllocationPolicy(final String classSuffix, final List<? extends Host> hosts) throws RuntimeException {
        try {
            final String className = generateFullClassName("VmAllocationPolicy", classSuffix);
            Class<VmAllocationPolicy> klass = PolicyLoader.<VmAllocationPolicy>loadClass(className);
            Constructor cons = klass.getConstructor(new Class[]{List.class});
            return (VmAllocationPolicy) cons.newInstance(hosts);
        } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            Logger.getLogger(PolicyLoader.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        }
    }
    
    public static CloudletScheduler cloudletScheduler(final String classSuffix) throws RuntimeException {
        try {
            final String className = generateFullClassName("CloudletScheduler", classSuffix);
            Class<CloudletScheduler> klass = PolicyLoader.<CloudletScheduler>loadClass(className);
            Constructor cons = klass.getConstructor();
            return (CloudletScheduler) cons.newInstance();
        } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            Logger.getLogger(PolicyLoader.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        }
    }  

    private static String generateFullClassName(final String classPrefix, final String classSuffix) {
        return String.format("%s.%s%s", PKG, classPrefix, classSuffix);
    }
    
    private static String generateFullProvisionerClassName(final String classPrefix, String classSuffix) {
        return generateFullClassName(
                String.format("provisioners.%sProvisioner", classPrefix), classSuffix);
    }

    public static UtilizationModel utilizationModel(final String classSuffix) throws RuntimeException {
        try {
            final String className = PKG+".UtilizationModel" + classSuffix;
            Class<UtilizationModel> klass = PolicyLoader.<UtilizationModel>loadClass(className);
            Constructor cons = klass.getConstructor();
            return (UtilizationModel) cons.newInstance();
        } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            Logger.getLogger(PolicyLoader.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        }
    }             
}
