
package org.tempuri;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="ProcessTransactionResult" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "processTransactionResult"
})
@XmlRootElement(name = "ProcessTransactionResponse")
public class ProcessTransactionResponse {

    @XmlElement(name = "ProcessTransactionResult")
    protected boolean processTransactionResult;

    /**
     * Gets the value of the processTransactionResult property.
     * 
     */
    public boolean isProcessTransactionResult() {
        return processTransactionResult;
    }

    /**
     * Sets the value of the processTransactionResult property.
     * 
     */
    public void setProcessTransactionResult(boolean value) {
        this.processTransactionResult = value;
    }

}
