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
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicy;
import org.cloudbus.cloudsim.provisioners.PeProvisioner;
import org.cloudbus.cloudsim.provisioners.ResourceProvisioner;
import org.cloudbus.cloudsim.resources.Pe;
import org.cloudbus.cloudsim.schedulers.cloudlet.CloudletScheduler;
import org.cloudbus.cloudsim.schedulers.vm.VmScheduler;
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModel;

/**
 * Dynamically creates instances of classes such as VmScheduler, VmProvisioner, 
 * resource provisioners and others from the class name of
 * the object to be instantiated.
 * 
 * @author Manoel Campos da Silva Filho
 */
public class PolicyLoader {
    private static final String PKG = "org.cloudbus.cloudsim";
    public static VmScheduler vmScheduler(String classSufix, List<? extends Pe> pes) throws RuntimeException {
        try {
            classSufix = generateFullClassName("VmScheduler", classSufix);
            Class<? extends VmScheduler> klass = (Class<? extends VmScheduler>) Class.forName(classSufix);
            Constructor cons = klass.getConstructor(new Class[]{List.class});
            return (VmScheduler) cons.newInstance(pes);
        } catch (ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
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
     * @param resourceCapacity The resource capacity the provisioner has available to manage
     * @param resourceClass The class of the resource capacity property of the provisioner
     * @return A new instance of the provisioner with the given name.
     * For instance, if the class prefix is "Bw" and class suffix is "Simple", 
     * returns an instance the BwProvisionerSimple class.
     * @throws RuntimeException 
     * 
     * @todo When a base interface for all CloudSim provisioners be created
     * at the package org.cloudbus.cloudsim.provisioners,
     * it could be used generics in this method instead of dealing
     * with the raw object class. The other methods that call this one,
     * such as newBwProvisioner, could be erased.
     */
    private static Object resourceProvisioner(
            String classPrefix, String classSufix, Number resourceCapacity, 
            Class<? extends Number> resourceClass) throws RuntimeException {
        try {
            final String className = generateFullProvisionerClassName(classPrefix, classSufix);
            final Class resourceProvisionerClass = Class.forName(className);
            Constructor cons = resourceProvisionerClass.getConstructor(new Class[]{resourceClass});
            return cons.newInstance(resourceCapacity);
        } catch (ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            Logger.getLogger(PolicyLoader.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        }
    }

    public static ResourceProvisioner newBwProvisioner(
            String classPrefix, String classSufix, long bwCapacity) throws RuntimeException {
        Object obj = resourceProvisioner(classPrefix, classSufix, bwCapacity, long.class);
        if(obj != null && obj instanceof ResourceProvisioner)
            return (ResourceProvisioner)obj;
            
        return null;
    }
    
    public static ResourceProvisioner newRamProvisioner(
            String classPrefix, String classSufix, int ramCapacity) throws RuntimeException {
        Object obj = resourceProvisioner(classPrefix, classSufix, ramCapacity, int.class);
        if(obj != null && obj instanceof ResourceProvisioner)
            return (ResourceProvisioner)obj;
            
        return null;
    }

    public static PeProvisioner newPeProvisioner(
            String classPrefix, String classSufix, double peCapacity) throws RuntimeException {
        Object obj = resourceProvisioner(classPrefix, classSufix, peCapacity, double.class);
        if(obj != null && obj instanceof PeProvisioner)
            return (PeProvisioner)obj;
            
        return null;
    }

    public static VmAllocationPolicy vmAllocationPolicy(String classSufix) throws RuntimeException {
        try {
            classSufix = generateFullClassName("VmAllocationPolicy", classSufix);
            Class<? extends VmScheduler> klass = (Class<? extends VmScheduler>) Class.forName(classSufix);
            Constructor cons = klass.getConstructor(new Class[]{});
            return (VmAllocationPolicy) cons.newInstance();
        } catch (ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            Logger.getLogger(PolicyLoader.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        }
    }
    
    public static CloudletScheduler cloudletScheduler(String classSufix) throws RuntimeException {
        try {
            classSufix = generateFullClassName("CloudletScheduler", classSufix);
            Class<? extends VmScheduler> klass = (Class<? extends VmScheduler>) Class.forName(classSufix);
            Constructor cons = klass.getConstructor();
            return (CloudletScheduler) cons.newInstance();
        } catch (ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            Logger.getLogger(PolicyLoader.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        }
    }  

    private static String generateFullClassName(String classPrefix, String classSufix) {
        return String.format("%s.%s%s", PKG, classPrefix, classSufix);
    }
    
    private static String generateFullProvisionerClassName(String classPrefix, String classSufix) {
        return generateFullClassName(
                String.format("provisioners.%sProvisioner", classPrefix), classSufix);
    }

    public static UtilizationModel utilizationModel(String classSufix) throws RuntimeException {
        try {
            classSufix = PKG+".UtilizationModel" + classSufix;
            Class<? extends VmScheduler> klass = (Class<? extends VmScheduler>) Class.forName(classSufix);
            Constructor cons = klass.getConstructor();
            return (UtilizationModel) cons.newInstance();
        } catch (ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            Logger.getLogger(PolicyLoader.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        }
    }             
}
