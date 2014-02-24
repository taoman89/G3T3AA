
package org.tempuri;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
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
 *         &lt;element name="teamId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="teamPassword" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
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
    "teamId",
    "teamPassword"
})
@XmlRootElement(name = "Clear")
public class Clear {

    protected String teamId;
    protected String teamPassword;

    /**
     * Gets the value of the teamId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTeamId() {
        return teamId;
    }

    /**
     * Sets the value of the teamId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTeamId(String value) {
        this.teamId = value;
    }

    /**
     * Gets the value of the teamPassword property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTeamPassword() {
        return teamPassword;
    }

    /**
     * Sets the value of the teamPassword property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTeamPassword(String value) {
        this.teamPassword = value;
    }

}
