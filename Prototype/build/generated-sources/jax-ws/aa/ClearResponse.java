
package aa;

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
 *         &lt;element name="ClearResult" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
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
    "clearResult"
})
@XmlRootElement(name = "ClearResponse")
public class ClearResponse {

    @XmlElement(name = "ClearResult")
    protected boolean clearResult;

    /**
     * Gets the value of the clearResult property.
     * 
     */
    public boolean isClearResult() {
        return clearResult;
    }

    /**
     * Sets the value of the clearResult property.
     * 
     */
    public void setClearResult(boolean value) {
        this.clearResult = value;
    }

}
