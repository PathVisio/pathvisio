/*
 * Copyright 2001-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package samples.swa;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;

/**
 * Class Tester
 * 
 * @version %I%, %G%
 */
public class Tester {

    /** Field HEADER_CONTENT_TYPE */
    public static final String HEADER_CONTENT_TYPE = "Content-Type";

    /** Field HEADER_CONTENT_TRANSFER_ENCODING */
    public static final String HEADER_CONTENT_TRANSFER_ENCODING =
            "Content-Transfer-Encoding";

    /** Field address */
    private static final java.lang.String address =
            "http://localhost:8080/axis/services/SwaHttp";

    /**
     * Method main
     * 
     * @param args 
     * @throws Exception 
     */
    public static void main(String[] args) throws Exception {

        /*
         * Start to prepare service call. Once this is done, several
         * calls can be made on the port (see below)
         *
         * Fist: get the service locator. This implements the functionality
         * to get a client stub (aka port).
         */
        SwaServiceLocator service = new SwaServiceLocator();

        /*
         * Here we use an Axis specific call that allows to override the
         * port address (service endpoint address) with an own URL. Comes
         * in handy for testing.
         */
        java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(address);
        } catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }

        SwaPort port = (SwaPort) service.getSwaHttp(endpoint);

        /*
         * At this point all preparations are done. Using the port we can
         * now perform as many calls as necessary.
         */

        /*
         * Prepare the Multipart attachment. It consists of several data files. The
         * multipart container is of type "multipart/mixed"
         */
        MimeMultipart mpRoot = new MimeMultipart();
        System.out.println("MimeMultipart content: " + mpRoot.getContentType());
        DataHandler dh = new DataHandler(new FileDataSource("duke.gif"));
        addBodyPart(mpRoot, dh);
        dh = new DataHandler(new FileDataSource("pivots.jpg"));
        addBodyPart(mpRoot, dh);
        // perform call
        port.swaSend("AppName", mpRoot);
    }

    /**
     * Method addBodyPart
     * 
     * @param mp 
     * @param dh 
     */
    private static void addBodyPart(MimeMultipart mp, DataHandler dh) {
        MimeBodyPart messageBodyPart = new MimeBodyPart();
        try {
            messageBodyPart.setDataHandler(dh);
            String contentType = dh.getContentType();
            if ((contentType == null) || (contentType.trim().length() == 0)) {
                contentType = "application/octet-stream";
            }
            System.out.println("Content type: " + contentType);
            messageBodyPart.setHeader(HEADER_CONTENT_TYPE, contentType);
            messageBodyPart.setHeader(
                    HEADER_CONTENT_TRANSFER_ENCODING,
                    "binary");    // Safe and fastest for anything other than mail
            mp.addBodyPart(messageBodyPart);
        } catch (javax.mail.MessagingException e) {
        }
    }
}
