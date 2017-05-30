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

/**
 * Represents the resources utilization cloudlet of a customer.
 * It stores the customer's broker policy, cloudlets configuration and
 * utilization models.
 * 
 * @author      Thiago T. Sá
 * @since       1.0
 */
public final class CloudletRegistry implements Serializable{
    private long id;
    private int amount;
    private double timeZone;
    private long length;
    private long fileSize;
    private long outputSize;
    private int pes;
    private String utilizationModelCpu;
    private String utilizationModelRam;
    private String utilizationModelBw;
    private double submissionDelay;

    public CloudletRegistry() {
        setAmount(1);
    }

    /**
     * Gets the cloudlet's id.
     * 
     * @return the cloudlet's id.
     */
    public long getId() {
        return id;
    }

    /**
     * Sets the cloudlet's id.
     * 
     * @param   id  the cloudlet's id.
     */
    public void setId(long id) {
        this.id = id;
    }
    
    /**
     * Gets the cloudlet's time zone.
     * 
     * @return the cloudlet's time zone.
     */
    public double getTimeZone() {
        return timeZone;
    }

    /**
     * Sets the cloudlet's time zone.
     * 
     * @param   timeZone    the cloudlet's time zone.
     */
    public void setTimeZone(double timeZone) {
        this.timeZone = timeZone;
    }

    /**
     * Gets the number of cloudlets.
     * 
     * @return the number of cloudlets.
     */
    public int getAmount() {
        return amount;
    }

    /**
     * Sets the number of cloudlets.
     * 
     * @param   amount  the number of cloudlets.
     */
    public void setAmount(int amount) {
        this.amount = amount;
    }

    /**
     * Gets the maximum length of a cloudlet.
     * 
     * @return the maximum length of a cloudlet.
     */
    public long getLength() {
        return length;
    }

    /**
     * Sets the maximum length of a cloudlet.
     * 
     * @param   length  the maximum length of a cloudlet.
     */
    public void setLength(long length) {
        this.length = length;
    }

    /**
     * Gets the cloudlets' file size.
     * 
     * @return the cloudlets' file size.
     */
    public long getFileSize() {
        return fileSize;
    }

    /**
     * Sets the cloudlets' file size.
     * 
     * @param fileSize  the cloudlets' file size.
     */
    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    /**
     * Gets the cloudlets' output size.
     * 
     * @return the cloudlets' output size.
     */
    public long getOutputSize() {
        return outputSize;
    }

    /**
     * Sets the cloudlets' output size.
     * 
     * @param   outputSize  the cloudlets' output size.
     */
    public void setOutputSize(long outputSize) {
        this.outputSize = outputSize;
    }

    /**
     * Gets the number of processing elements required to run a cloudlet.
     * 
     * @return the number of processing elements required to run a cloudlet.
     */
    public int getPes() {
        return pes;
    }

    /**
     * Sets the number of processing elements required to run a cloudlet.
     * 
     * @param   pesNumber   the number of processing elements required to run
     *                      a cloudlet.
     */
    public void setPes(int pesNumber) {
        this.pes = pesNumber;
    }

    /**
     * Gets the cloudlet's CPU utilization model class name suffix.
     * 
     * @return
     */
    public String getUtilizationModelCpu() {
        return utilizationModelCpu;
    }

    /**
     * Sets the cloudlet's CPU utilization model class name suffix.
     * 
     * @param   utilizationModelCpu    the cloudlet's CPU utilization model class name suffix to set
     */
    public void setUtilizationModelCpu(String utilizationModelCpu) {
        this.utilizationModelCpu = utilizationModelCpu;
    }

    /**
     * Gets the cloudlet's RAM utilization model class name suffix.
     * 
     * @return
     */
    public String getUtilizationModelRam() {
        return utilizationModelRam;
    }

    /**
     * Sets the cloudlet's RAM utilization model class name suffix.
     * 
     * @param   utilizationModelRam    the cloudlet's RAM utilization model class name suffix to set
     */
    public void setUtilizationModelRam(String utilizationModelRam) {
        this.utilizationModelRam = utilizationModelRam;
    }

    /**
     * Gets the cloudlet's bandwidth utilization model class name suffix.
     * 
     * @return
     */
    public String getUtilizationModelBw() {
        return utilizationModelBw;
    }

    /**
     * Sets the cloudlet's bandwidth utilization model class name suffix.
     * 
     * @param   utilizationModelBw the cloudlet's bandwidth utilization model class name suffix to set
     */
    public void setUtilizationModelBw(String utilizationModelBw) {
        this.utilizationModelBw = utilizationModelBw;
    }

    /**
     * Gets the time to send the next cloudlet.
     * 
     * @return the time to send the next cloudlet.
     */
    public double getSubmissionDelay() {
        return submissionDelay;
    }

    /**
     * Sets the time to send the next cloudlet.
     * 
     * @param   submissionDelay  the time to send the next cloudlet.
     */
    public void setSubmissionDelay(double submissionDelay) {
        this.submissionDelay = submissionDelay;
    }
    
    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append("Time Zone (GMT)="+getTimeZone()+"\n");
        s.append("Cloudlets per minute="+ getAmount()+"\n");
        s.append("Max length="+getLength()+"\n");
        s.append("Max File Size="+getFileSize()+"\n");
        s.append("Max Output Size="+getOutputSize()+"\n");
        s.append("Cloudlets PEs="+ getPes()+"\n");
        s.append("CPU UM="+ getUtilizationModelCpu()+"\n");
        s.append("RAM UM ="+ getUtilizationModelRam()+"\n");
        s.append("Bandwidth UM ="+ getUtilizationModelBw()+"\n");

        return s.toString();
    }
}
