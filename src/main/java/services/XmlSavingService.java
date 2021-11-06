package services;

import helpingclasses.Transactions;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.File;

public class XmlSavingService {

    static void saveToXml(Transactions transactions, String fileName) {
        try {

            JAXBContext jaxbContext = JAXBContext.newInstance(Transactions.class);
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            jaxbMarshaller.marshal(transactions, new File(fileName + ".xml"));

        } catch (JAXBException ex) {
            ex.printStackTrace();
        }
    }

}
