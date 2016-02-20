package xml;

import javax.jms.*;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.StringWriter;


import java.io.IOException;

public class XmlGeneratorServlet extends HttpServlet {

    // Constants  !!! need to parametrize
    private final String AMQP_BROKER_URL = ActiveMQConnection.DEFAULT_BROKER_URL;
    private final String QUEUE_NAME = "TEST";
    private final String XSD_TEMPLATE_PATH  = "template.xsd";

    // AMQP
    private ConnectionFactory factory = null;
    private Connection connection = null;
    private Session session = null;
    private Destination destination = null;
    private MessageProducer producer = null;

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String param1 = req.getParameter("name");
        String param2 = req.getParameter("phone");
        Boolean testMode = Boolean.valueOf(req.getParameter("test"));

        try {
            String xsdString = generateXmlFromXSD(param1, param2);
            if (xsdString!= null && !xsdString.isEmpty()){
                if (testMode) {
                    resp.getWriter().print(xsdString);
                } else {
                    sendMessage(xsdString);
                }
            }

        } catch (JAXBException e) {
            resp.setStatus(500);
            resp.getWriter().print(e.toString());
        }
    }

    public void sendMessage(String messageText) {
        try {
            factory = new ActiveMQConnectionFactory(AMQP_BROKER_URL);
            connection = factory.createConnection();
            connection.start();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            destination = session.createQueue(QUEUE_NAME);
            producer = session.createProducer(destination);
            TextMessage message = session.createTextMessage();
            message.setText(messageText);
            producer.send(message);
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    private String generateXmlFromXSD(String name, String phone) throws JAXBException {

        TestUser user = new TestUser();
        user.setName(name);
        user.setPhone(phone);

        JAXBContext context = JAXBContext.newInstance(TestUser.class);

        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty("jaxb.formatted.output",Boolean.TRUE);
        StringWriter sw = new StringWriter();
        marshaller.marshal(user,sw);
        return sw.toString();
    }

    /*
    public static void main(String[] args){
        try {
            System.out.println(generateXmlFromXSD("aafrikanov", "123"));
        } catch (JAXBException e) {
            e.printStackTrace();
        }

    }
    */
}
