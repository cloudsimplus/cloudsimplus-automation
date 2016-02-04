/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.manoelcampos.cloudsim.automation;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.cloudbus.cloudsim.CloudletScheduler;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.resources.Pe;
import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.VmAllocationPolicy;
import org.cloudbus.cloudsim.VmScheduler;
import org.cloudbus.cloudsim.provisioners.VmResourceProvisioner;

/* TODO: Utilizar a classe do CloudReports para carregamento dinâmico de classes de políticas.
 * O grande problema é que as classes do CloudReports tem um alto acoplamento,
 * criando muitas dependências umas das outras, como depender
 * de classes da GUI ou do ORM
 */

/**
 *
 * @author manoelcampos
 */
public class PolicyLoader {
    public static final String PKG = "org.cloudbus.cloudsim.";
    public static VmScheduler vmScheduler(String name, List<? extends Pe> pes) throws RuntimeException {
        try {
            name = PKG+"VmScheduler" + name;
            Class<? extends VmScheduler> klass = (Class<? extends VmScheduler>) Class.forName(name);
            Constructor cons = klass.getConstructor(new Class[]{List.class});
            return (VmScheduler) cons.newInstance(pes);
        } catch (ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            Logger.getLogger(PolicyLoader.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        }
    }
    
    public static <T> VmResourceProvisioner<T> resourceProvisioner(
            String name, T resourceCapacity, Class resourceCapacityClass,
            String classPrefix) throws RuntimeException {
        try {
            name = PKG+"provisioners." + classPrefix + "Provisioner" + name;
            Class<? extends VmResourceProvisioner> resourceProvisionerClass = (Class<? extends VmResourceProvisioner>) Class.forName(name);
            Constructor cons = resourceProvisionerClass.getConstructor(new Class[]{resourceCapacityClass});
            return (VmResourceProvisioner) cons.newInstance(resourceCapacity);
        } catch (ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            Logger.getLogger(PolicyLoader.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        }
    }

    public static VmAllocationPolicy vmAllocationPolicy(String name, List<? extends Host> hosts) throws RuntimeException {
        try {
            name = PKG+"VmAllocationPolicy" + name;
            Class<? extends VmScheduler> klass = (Class<? extends VmScheduler>) Class.forName(name);
            Constructor cons = klass.getConstructor(new Class[]{List.class});
            return (VmAllocationPolicy) cons.newInstance(hosts);
        } catch (ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            Logger.getLogger(PolicyLoader.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        }
    }
    
    
    
    public static CloudletScheduler cloudletScheduler(String name) throws RuntimeException {
        try {
            name = PKG+"CloudletScheduler" + name;
            Class<? extends VmScheduler> klass = (Class<? extends VmScheduler>) Class.forName(name);
            Constructor cons = klass.getConstructor();
            return (CloudletScheduler) cons.newInstance();
        } catch (ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            Logger.getLogger(PolicyLoader.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        }
    }  
    
    
    public static UtilizationModel utilizationModel(String name) throws RuntimeException {
        try {
            name = PKG+"UtilizationModel" + name;
            Class<? extends VmScheduler> klass = (Class<? extends VmScheduler>) Class.forName(name);
            Constructor cons = klass.getConstructor();
            return (UtilizationModel) cons.newInstance();
        } catch (ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            Logger.getLogger(PolicyLoader.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        }
    }             
}
