
package aa;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;


/**
 * This class was generated by the JAX-WS RI.
 * JAX-WS RI 2.2.6-1b01 
 * Generated source version: 2.1
 * 
 */
@WebService(name = "ServiceSoap", targetNamespace = "http://tempuri.org/")
@XmlSeeAlso({
    ObjectFactory.class
})
public interface ServiceSoap {


    /**
     * 
     * @param teamPassword
     * @param transactionDescription
     * @param teamId
     * @return
     *     returns boolean
     */
    @WebMethod(operationName = "ProcessTransaction", action = "http://tempuri.org/ProcessTransaction")
    @WebResult(name = "ProcessTransactionResult", targetNamespace = "http://tempuri.org/")
    @RequestWrapper(localName = "ProcessTransaction", targetNamespace = "http://tempuri.org/", className = "aa.ProcessTransaction")
    @ResponseWrapper(localName = "ProcessTransactionResponse", targetNamespace = "http://tempuri.org/", className = "aa.ProcessTransactionResponse")
    public boolean processTransaction(
        @WebParam(name = "teamId", targetNamespace = "http://tempuri.org/")
        String teamId,
        @WebParam(name = "teamPassword", targetNamespace = "http://tempuri.org/")
        String teamPassword,
        @WebParam(name = "transactionDescription", targetNamespace = "http://tempuri.org/")
        String transactionDescription);

    /**
     * 
     * @param teamPassword
     * @param teamId
     * @return
     *     returns boolean
     */
    @WebMethod(operationName = "Clear", action = "http://tempuri.org/Clear")
    @WebResult(name = "ClearResult", targetNamespace = "http://tempuri.org/")
    @RequestWrapper(localName = "Clear", targetNamespace = "http://tempuri.org/", className = "aa.Clear")
    @ResponseWrapper(localName = "ClearResponse", targetNamespace = "http://tempuri.org/", className = "aa.ClearResponse")
    public boolean clear(
        @WebParam(name = "teamId", targetNamespace = "http://tempuri.org/")
        String teamId,
        @WebParam(name = "teamPassword", targetNamespace = "http://tempuri.org/")
        String teamPassword);

}
