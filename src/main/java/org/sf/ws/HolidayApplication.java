package org.sf.ws;

import java.util.List;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.ws.config.annotation.EnableWs;
import org.springframework.ws.config.annotation.WsConfigurerAdapter;
import org.springframework.ws.server.EndpointInterceptor;
import org.springframework.ws.soap.SoapVersion;
import org.springframework.ws.soap.axiom.AxiomSoapMessageFactory;
import org.springframework.ws.soap.server.endpoint.interceptor.PayloadValidatingInterceptor;
import org.springframework.ws.transport.http.MessageDispatcherServlet;
import org.springframework.ws.wsdl.wsdl11.SimpleWsdl11Definition;
import org.springframework.ws.wsdl.wsdl11.Wsdl11Definition;
import org.springframework.xml.xsd.SimpleXsdSchema;
import org.springframework.xml.xsd.XsdSchema;

@SpringBootApplication
@EnableWs
public class HolidayApplication {

	public static void main(String[] args) {
		SpringApplication.run(HolidayApplication.class, args);
	}

	@Bean
	public ServletRegistrationBean messageDispatcherServlet(ApplicationContext applicationContext) {
		MessageDispatcherServlet messageDispatcherServlet = new MessageDispatcherServlet();
		messageDispatcherServlet.setApplicationContext(applicationContext);
		messageDispatcherServlet.setTransformWsdlLocations(true);
		messageDispatcherServlet.setTransformSchemaLocations(true);

		return new ServletRegistrationBean(messageDispatcherServlet, "/api/soap/*");
	}

	@Bean
	public AxiomSoapMessageFactory messageFactory() {
		// SAAJ is based on DOM, the Document Object Model. This means that all SOAP messages are stored in memory.
		// For larger SOAP messages, this may not be very performant.
		// In that case, the AxiomSoapMessageFactory might be more applicable.
		AxiomSoapMessageFactory axiomSoapMessageFactory = new AxiomSoapMessageFactory();
		axiomSoapMessageFactory.setSoapVersion(SoapVersion.SOAP_12);
//		// To increase reading performance on the AxiomSoapMessageFactory, you can set the payloadCaching property
//		// to false (default is true). This will read the contents of the SOAP body directly from the socket stream.
//		// When this setting is enabled, the payload can only be read once. This means that you have to make sure
//		// that any pre-processing (logging etc.) of the message does not consume it.
//		axiomSoapMessageFactory.setPayloadCaching(false);

		return axiomSoapMessageFactory;
	}

	@Bean
	public WsConfigurerAdapter wsConfigurerAdapter() {
		return new WsConfigurerAdapter() {
			@Override
			public void addInterceptors(List<EndpointInterceptor> interceptors) {
				interceptors.add(validatingInterceptor());
			}
		};
	}

	@Bean
	public PayloadValidatingInterceptor validatingInterceptor() {
		PayloadValidatingInterceptor payloadValidatingInterceptor = new PayloadValidatingInterceptor();
		payloadValidatingInterceptor.setSchemas(getHrSchema());
		payloadValidatingInterceptor.setValidateRequest(true);
		payloadValidatingInterceptor.setValidateResponse(true);

		return payloadValidatingInterceptor;
	}

	@Bean
	public Wsdl11Definition hrWs() {
		SimpleWsdl11Definition simpleWsdl11Definition = new SimpleWsdl11Definition();
		simpleWsdl11Definition.setWsdl(new ClassPathResource("wsdl/hr.wsdl"));

		return simpleWsdl11Definition;
	}

	@Bean
	public XsdSchema hr() {
		SimpleXsdSchema simpleXsdSchema = new SimpleXsdSchema();
		simpleXsdSchema.setXsd(getHrSchema());

		return simpleXsdSchema;
	}

	private ClassPathResource getHrSchema() {
		return new ClassPathResource("wsdl/hr.xsd");
	}

}
