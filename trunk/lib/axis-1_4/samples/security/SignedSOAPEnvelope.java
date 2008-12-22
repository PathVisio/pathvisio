/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 2001-2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Axis" and "Apache Software Foundation" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

package samples.security;

import org.apache.axis.Constants;
import org.apache.axis.Message;
import org.apache.axis.MessageContext;
import org.apache.axis.client.AxisClient;
import org.apache.axis.configuration.NullProvider;
import org.apache.axis.encoding.SerializationContext;
import org.apache.axis.encoding.SerializationContext;
import org.apache.axis.encoding.DeserializationContext;
import org.apache.axis.message.SOAPEnvelope;
import org.apache.axis.message.SOAPHeaderElement;
import org.apache.axis.utils.Mapping;
import org.apache.axis.utils.Messages;
import org.apache.axis.utils.XMLUtils;
import org.apache.xml.security.c14n.Canonicalizer;
import org.apache.xml.security.signature.XMLSignature;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import java.io.FileInputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;


public class SignedSOAPEnvelope extends SOAPEnvelope {
    static String SOAPSECNS = "http://schemas.xmlsoap.org/soap/security/2000-12";
    static String SOAPSECprefix = "SOAP-SEC";

    static String keystoreType = "JKS";
    static String keystoreFile = "keystore.jks";
    static String keystorePass = "xmlsecurity";
    static String privateKeyAlias = "test";
    static String privateKeyPass = "xmlsecurity";
    static String certificateAlias = "test";
    private MessageContext msgContext;

    static {
        org.apache.xml.security.Init.init();
    }

    public SignedSOAPEnvelope(MessageContext msgContext, SOAPEnvelope env, String baseURI, String keystoreFile) {
        this.msgContext = msgContext;
        init(env, baseURI, keystoreFile);
    }

    public SignedSOAPEnvelope(SOAPEnvelope env, String baseURI) {
        init(env, baseURI, keystoreFile);
    }

    private void init(SOAPEnvelope env, String baseURI, String keystoreFile) {
        try {
            System.out.println("Beginning Client signing...");
            env.addMapping(new Mapping(SOAPSECNS, SOAPSECprefix));
            env.addAttribute(Constants.URI_SOAP11_ENV, "actor", "some-uri");
            env.addAttribute(Constants.URI_SOAP11_ENV, "mustUnderstand", "1");

            SOAPHeaderElement header = 
                new SOAPHeaderElement(XMLUtils.StringToElement(SOAPSECNS,
                                                               "Signature",
                                                               ""));
            env.addHeader(header);

            Document doc = getSOAPEnvelopeAsDocument(env, msgContext);

            KeyStore ks = KeyStore.getInstance(keystoreType);
            FileInputStream fis = new FileInputStream(keystoreFile);

            ks.load(fis, keystorePass.toCharArray());

            PrivateKey privateKey = (PrivateKey) ks.getKey(privateKeyAlias,
                    privateKeyPass.toCharArray());

            Element soapHeaderElement = (Element) ((Element) doc.getFirstChild()).getElementsByTagNameNS("*", "Header").item(0);
            Element soapSignatureElement = (Element) soapHeaderElement.getElementsByTagNameNS("*", "Signature").item(0);

            //Id attribute creation
            Element body = (Element)doc.getElementsByTagNameNS("http://schemas.xmlsoap.org/soap/envelope/", "Body").item(0);
            body.setAttribute("Id", "Body");

            XMLSignature sig = new XMLSignature(doc, baseURI,
                    XMLSignature.ALGO_ID_SIGNATURE_DSA);

            soapSignatureElement.appendChild(sig.getElement());
            sig.addDocument("#Body");


            X509Certificate cert =
                    (X509Certificate) ks.getCertificate(certificateAlias);


            sig.addKeyInfo(cert);
            sig.addKeyInfo(cert.getPublicKey());
            sig.sign(privateKey);

            Canonicalizer c14n = Canonicalizer.getInstance(Canonicalizer.ALGO_ID_C14N_WITH_COMMENTS);
            byte[] canonicalMessage = c14n.canonicalizeSubtree(doc);

            InputSource is = new InputSource(new java.io.ByteArrayInputStream(canonicalMessage));
            DeserializationContext dser = null;
            if (msgContext == null) {
                AxisClient tmpEngine = new AxisClient(new NullProvider());
                msgContext = new MessageContext(tmpEngine);
            }
            dser = new DeserializationContext(is, msgContext,
                    Message.REQUEST, this);

            dser.parse();
            System.out.println("Client signing complete.");
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.toString());
        }
    }

    private Document getSOAPEnvelopeAsDocument(SOAPEnvelope env, MessageContext msgContext)
            throws Exception {
        StringWriter writer = new StringWriter();
        SerializationContext serializeContext = new SerializationContext(writer, msgContext);
        env.output(serializeContext);
        writer.close();

        Reader reader = new StringReader(writer.getBuffer().toString());
        Document doc = XMLUtils.newDocument(new InputSource(reader));
        if (doc == null)
            throw new Exception(
                    Messages.getMessage("noDoc00", writer.getBuffer().toString()));
        return doc;
    }
}
