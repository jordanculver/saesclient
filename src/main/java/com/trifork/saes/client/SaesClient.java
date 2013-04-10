/**
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * Contributor(s): Contributors are attributed in the source code
 * where applicable.
 *
 * The Original Code is "Stamdata".
 *
 * The Initial Developer of the Original Code is Trifork Public A/S.
 *
 * Portions created for the Original Code are Copyright 2011,
 * Lægemiddelstyrelsen. All Rights Reserved.
 *
 * Portions created for the FMKi Project are Copyright 2011,
 * National Board of e-Health (NSI). All Rights Reserved.
 */
package com.trifork.saes.client;

import com.trifork.saes.jaxws.generated.*;
import dk.sosi.seal.model.Reply;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.stereotype.Component;
import org.w3c.dom.*;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.bind.*;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringWriter;

@Component
public class SaesClient {

    @Autowired
    private SosiUtil sosiUtil;

    @Value("${cpr}")
    private String cpr;

    @Value("${saes.url}")
    private String endpointUrl;

    private JAXBContext jaxContext;

    public SaesClient() throws JAXBException {
        jaxContext = JAXBContext.newInstance(AuthorizationRequestType.class, AuthorizationResponseType.class);
    }

    private final QName serviceName = new QName("http://nsi.dk/-/stamdata/3.0", "AuthorizationRequestStructure");

    public void performRequest() throws JAXBException, IOException, SAXException, ParserConfigurationException {
        AuthorizationRequestType request = new AuthorizationRequestType();
        request.setCpr(cpr);

        String requestString = createRequestString(request);
        Reply reply = sosiUtil.sendServiceRequest(endpointUrl, requestString);

        Element body = reply.getBody();

        Unmarshaller unmarshaller = jaxContext.createUnmarshaller();
        JAXBElement<AuthorizationResponseType> jaxbResponse = unmarshaller.unmarshal(body, AuthorizationResponseType.class);
        AuthorizationResponseType response = jaxbResponse.getValue();
        printResponse(response);
    }

    private void printResponse(AuthorizationResponseType response) {
        System.out.println("**********************************");
        System.out.println("* CPR: "+response.getCpr());
        if (response.getFirstName() != null) {
            System.out.println("* Firstname: " + response.getFirstName());
        }
        if (response.getLastName() != null) {
            System.out.println("* Lastname: " + response.getLastName());
        }
        if (response.getAuthorization().size() == 0) {
            System.out.println("* No authorizations");
        }
        for (AuthorizationType aut : response.getAuthorization()) {
            System.out.println("* Aut: " + aut.getAuthorizationCode());
            System.out.println("* Edu: " + aut.getEducationCode() + " : " + aut.getEducationName());
        }
        System.out.println("**********************************");
    }

    private String createRequestString(AuthorizationRequestType request) throws JAXBException {

        StringWriter writer = new StringWriter();
        Marshaller marshaller = jaxContext.createMarshaller();
        marshaller.marshal(new JAXBElement<AuthorizationRequestType>
                (serviceName, AuthorizationRequestType.class, request), writer);
        return writer.toString();
    }

}
