package org.sf.ws;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.axiom.AxiomSoapMessage;
import org.springframework.ws.stream.StreamingWebServiceMessage;

@Endpoint
public class HolidayEndpoint {

	private static final Logger LOG = LoggerFactory.getLogger(HolidayEndpoint.class);

	private static final String NAMESPACE_URI = "http://ws.sf.org/hr/schemas";

	@PayloadRoot(namespace = NAMESPACE_URI, localPart = "holidayRequest")
	@ResponsePayload
	public void handle(@RequestPayload SoapMessage soapMessage) throws XMLStreamException {
		LOG.debug("soapMessage = " + soapMessage);
		LOG.debug("StreamingWebServiceMessage = " + (soapMessage instanceof StreamingWebServiceMessage));

		if (!(soapMessage instanceof AxiomSoapMessage)) {
			LOG.error("Неподдерживаемый формат SOAP-сообщения");

			return;
		}

		AxiomSoapMessage axiomSoapMessage = (AxiomSoapMessage) soapMessage;

		XMLStreamReader xmlStreamReader = axiomSoapMessage
			.getAxiomMessage()
			.getSOAPEnvelope()
			.getXMLStreamReaderWithoutCaching();

		XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
		xmlInputFactory.setProperty(XMLInputFactory.IS_COALESCING, Boolean.TRUE);
		xmlInputFactory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, Boolean.TRUE);

		XMLEventReader xmlEventReader = xmlInputFactory.createXMLEventReader(xmlStreamReader);

		while (xmlEventReader.hasNext()) {
			XMLEvent xmlEvent = xmlEventReader.nextEvent();

			if (xmlEvent.isStartElement()) {
				StartElement startElement = xmlEvent.asStartElement();

				System.out.print("<" + startElement.getName().getLocalPart());

				Iterator iterator = startElement.getAttributes();
				while (iterator.hasNext()) {
					Attribute attribute = (Attribute) iterator.next();
					QName name = attribute.getName();
					String value = attribute.getValue();

					System.out.print(name + "=" + value + (iterator.hasNext() ? " " : StringUtils.EMPTY));
				}

				System.out.println(">");
			} else if (xmlEvent.isEndElement()) {
				EndElement endElement = xmlEvent.asEndElement();

				System.out.println("</" + endElement.getName().getLocalPart() + ">");
			} else if (xmlEvent.isCharacters()) {
				Characters characters = xmlEvent.asCharacters();

				if (StringUtils.isNotBlank(characters.getData())) {
					System.out.println(characters.getData());
				}
			}
		}
	}

}
