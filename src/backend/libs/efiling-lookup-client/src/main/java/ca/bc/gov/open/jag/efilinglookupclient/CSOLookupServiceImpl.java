package ca.bc.gov.open.jag.efilinglookupclient;


import ca.bc.gov.ag.csows.LookupFacadeItf;
import ca.bc.gov.ag.csows.LookupsLookupFacade;
import ca.bc.gov.ag.csows.lookups.GetServiceFeeElement;
import ca.bc.gov.ag.csows.lookups.GetServiceFeeResponseElement;
import ca.bc.gov.ag.csows.lookups.ServiceFee;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.Date;
import java.util.GregorianCalendar;

@Service
public class CSOLookupServiceImpl implements EfilingLookupService {

    private LookupsLookupFacade lookupsLookupFacade;
    private static final Logger LOGGER = LoggerFactory.getLogger(CSOLookupServiceImpl.class);

    public CSOLookupServiceImpl(LookupsLookupFacade lookupsLookupFacade) {
        this.lookupsLookupFacade = lookupsLookupFacade;
    }

    @Override
    public ServiceFees getServiceFee(String serviceId)  {

        ServiceFees serviceFees = null;

        if (serviceId.isEmpty()) return serviceFees;

        try {

            LookupFacadeItf lookupFacadeItf = lookupsLookupFacade.getLookupFacade();
            GetServiceFeeElement getServiceFeeElement = new GetServiceFeeElement();
            getServiceFeeElement.setString1(serviceId);
            getServiceFeeElement.setDate2(Date2XMLGregorian(new Date()));

            GetServiceFeeResponseElement response = lookupFacadeItf.getServiceFee(getServiceFeeElement);
            serviceFees = SetServiceFees(response.getResult());
        }
        catch(DatatypeConfigurationException e) {

            LOGGER.error("Error calling getServiceFee: " + e.getMessage());
        }

        return serviceFees;
    }

    /**
     * Helper function to convert a Date to an XMLGregorianCalendar date for sending to SOAP
     * @param date
     * @return XMLGregorianCalendar
     * @throws DatatypeConfigurationException
     */
    private XMLGregorianCalendar Date2XMLGregorian(Date date) throws DatatypeConfigurationException {

        GregorianCalendar c = new GregorianCalendar();
        c.setTime(date);
        XMLGregorianCalendar date2 = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
        return date2;
    }

    /**
     * Helper function to map the SOAP ServiceFee response to the ServiceFees class
     * @param fee - SOAP response from GetServiceFee call
     * @return ServiceFees
     */
    private ServiceFees SetServiceFees(ServiceFee fee) {

        ServiceFees serviceFees = new ServiceFees(
                new DateTime(fee.getUpdDtm()),
                fee.getFeeAmt(),
                fee.getEntUserId(),
                fee.getServiceTypeCd(),
                new DateTime(fee.getEffectiveDt()),
                fee.getUpdUserId(),
                new DateTime(fee.getEntDtm()),
                new DateTime(fee.getExpiryDt()));

        return serviceFees;
    }
}
