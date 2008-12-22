/**
 * SwaBindingImpl.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2alpha Dec 07, 2003 (08:01:12 EST) WSDL2Java emitter.
 */
package samples.swa;

import org.apache.axis.AxisFault;
import org.apache.axis.Message;
import org.apache.axis.MessageContext;
import org.apache.axis.attachments.AttachmentPart;
import org.apache.axis.attachments.Attachments;

import javax.activation.DataHandler;
import javax.mail.internet.MimeBodyPart;
import java.util.Iterator;

/**
 * Class SwaBindingImpl
 * 
 * @version %I%, %G%
 */
public class SwaBindingImpl implements samples.swa.SwaPort {

    /**
     * Method swaSend
     * 
     * @param applicationName 
     * @param content         
     * @return 
     * @throws java.rmi.RemoteException 
     */
    public java.lang.String swaSend(
            java.lang.String applicationName, javax.mail.internet.MimeMultipart content)
            throws java.rmi.RemoteException {

        MimeBodyPart mpb = null;

        System.out.println("Application: " + applicationName);

        /*
         * Now do some printing to get information about the multipart
         * content and the associated attachments.  Axis performs
         * several steps during deserialization of this SOAP
         * call. Only the steps of interesst are described here.
         *
         * The MIME multipart that contains the other parts (because
         * it's multipart/mixed or multipart/related) is handled as
         * ONE Axis attachment during the first part of
         * deserialization. This attachment is identified by the CID:
         * prefixed string generated during serialization.
         *
         * The next step (see
         * MimeMultipartDataHandlerDeserializer.java) gets the data
         * handler of the Axis attachment and creates a MimeMultipart
         * object using the data source as input of the new
         * MimeMultipart object. The MimeMultipart object parses the
         * input (on demand? -> this need to be clarified) and builds
         * the associated body parts.
         *
         * The Axis attachment part is not disposed or otherwise
         * managed after it was serialized into the MimeMultipart
         * object. Therefore it is a good idea to call the dispose()
         * method of the Axis attachment part after processing is
         * complete.  Doing so releases all used resources, also
         * deleting disk cache files if necessary, of this attachment
         * part.
         */
        try {
            int contCount = content.getCount();

            System.out.println("Number of Mimeparts: " + contCount);

            for (int i = 0; i < contCount; i++) {
                mpb = (MimeBodyPart) content.getBodyPart(i);

                DataHandler dh = mpb.getDataHandler();

                System.out.println("Mime data type: " + dh.getContentType());
            }
        } catch (javax.mail.MessagingException ex) {
        }

        /*
         * the next prints are just for information only
         */
        AttachmentPart[] attParts = getMessageAttachments();

        System.out.println("Number of attachements: " + attParts.length);

        if (attParts.length > 0) {
            try {
                System.out.println("Att[0] type: "
                        + attParts[0].getContentType());
                System.out.println(
                        "Att[0] dh type: "
                        + attParts[0].getDataHandler().getContentType());
                System.out.println("Att[0] file: "
                        + attParts[0].getAttachmentFile());
            } catch (javax.xml.soap.SOAPException ex) {
            }
        }

        /*
         * Now process the parametes including the MimeMultipart
         */

        /*
         * Processing is done, now dispose the attachements. This is not done
         * by Axis, should be done by service.
         */
        MessageContext msgContext = MessageContext.getCurrentContext();
        Message reqMsg = msgContext.getRequestMessage();
        Attachments messageAttachments = reqMsg.getAttachmentsImpl();

        messageAttachments.dispose();

        return null;
    }

    /**
     * extract attachments from the current request
     * 
     * @return a list of attachmentparts or
     *         an empty array for no attachments support in this axis
     *         buid/runtime
     * @throws AxisFault 
     */
    private AttachmentPart[] getMessageAttachments() throws AxisFault {

        MessageContext msgContext = MessageContext.getCurrentContext();
        Message reqMsg = msgContext.getRequestMessage();
        Attachments messageAttachments = reqMsg.getAttachmentsImpl();
        int attachmentCount =
                messageAttachments.getAttachmentCount();
        AttachmentPart attachments[] = new AttachmentPart[attachmentCount];
        Iterator it =
                messageAttachments.getAttachments().iterator();
        int count = 0;

        while (it.hasNext()) {
            AttachmentPart part = (AttachmentPart) it.next();
            attachments[count++] = part;
        }
        return attachments;
    }
}
