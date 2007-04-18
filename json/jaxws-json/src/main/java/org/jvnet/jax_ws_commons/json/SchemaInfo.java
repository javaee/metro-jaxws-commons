package org.jvnet.jax_ws_commons.json;

import com.sun.istack.NotNull;
import com.sun.xml.bind.unmarshaller.DOMScanner;
import com.sun.xml.stream.buffer.MutableXMLStreamBuffer;
import com.sun.xml.stream.buffer.stax.StreamWriterBufferCreator;
import com.sun.xml.ws.api.server.DocumentAddressResolver;
import com.sun.xml.ws.api.server.SDDocument;
import com.sun.xml.ws.api.server.ServiceDefinition;
import com.sun.xml.ws.api.server.WSEndpoint;
import com.sun.xml.xsom.XSElementDecl;
import com.sun.xml.xsom.XSSchema;
import com.sun.xml.xsom.XSSchemaSet;
import com.sun.xml.xsom.parser.JAXPParser;
import com.sun.xml.xsom.parser.XMLParser;
import com.sun.xml.xsom.parser.XSOMParser;
import com.sun.xml.xsom.visitor.XSVisitor;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.ContentHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.ws.WebServiceException;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Captures the information parsed from XML Schema.
 * Used to guide the JSON/XML conversion.
 *
 * @author Kohsuke Kawaguchi
 */
final class SchemaInfo {
    /**
     * Endpoint for which this schema info applies.
     */
    final @NotNull WSEndpoint endpoint;

    /**
     * Parent tag name to possible child tag names.
     */
    final Set<QName> tagNames = new HashSet<QName>();

    final SchemaConvention convention;


    /**
     * @throws WebServiceException
     *      If failed to parse schema portion inside WSDL.
     */
    public SchemaInfo(WSEndpoint endpoint) {
        this.endpoint = endpoint;

        final ServiceDefinition sd = endpoint.getServiceDefinition();
        final Map<String,SDDocument> byURL = new HashMap<String,SDDocument>();

        for (SDDocument doc : sd)
            byURL.put(doc.getURL().toExternalForm(),doc);

        // set up XSOMParser to read from SDDocuments
        XSOMParser p = new XSOMParser(new XMLParser() {
            private final XMLParser jaxp = new JAXPParser();

            public void parse(InputSource source, ContentHandler handler, ErrorHandler errorHandler, EntityResolver entityResolver) throws SAXException, IOException {
                SDDocument doc = byURL.get(source.getSystemId());
                if(doc!=null) {
                    try {
                        readToBuffer(doc).writeTo(handler,errorHandler,false);
                    } catch (XMLStreamException e) {
                        throw new SAXException(e);
                    }
                } else {
                    // default behavior
                    jaxp.parse(source,handler,errorHandler,entityResolver);
                }
            }
        });

        try {
            // parse the primary WSDL, and it should recursively parse all referenced schemas
            // TODO: this is super slow
            TransformerHandler h = ((SAXTransformerFactory) TransformerFactory.newInstance()).newTransformerHandler();
            DOMResult r = new DOMResult();
            h.setResult(r);
            readToBuffer(sd.getPrimary()).writeTo(h,false);
            Document dom = (Document)r.getNode();
            NodeList schemas = dom.getElementsByTagNameNS(XMLConstants.W3C_XML_SCHEMA_NS_URI, "schema");
            for( int i=0; i<schemas.getLength(); i++ ) {
                DOMScanner scanner = new DOMScanner();
                scanner.setContentHandler(p.getParserHandler());
                scanner.scan(schemas.item(i));
            }

            extractTagNames(p.getResult());

        } catch (XMLStreamException e) {
            throw new WebServiceException("Failed to parse WSDL",e);
        } catch (IOException e) {
            throw new WebServiceException("Failed to parse WSDL",e);
        } catch (SAXException e) {
            throw new WebServiceException("Failed to parse WSDL",e);
        } catch (TransformerConfigurationException e) {
            throw new AssertionError(e); // impossible
        }

        convention = new SchemaConvention(tagNames);
    }

    public XMLStreamWriter createXMLStreamWriter(Writer writer) throws XMLStreamException {
        return new MappedXMLStreamWriter(convention, writer) {
            public void writeEndDocument() throws XMLStreamException {
                try {
                    // unwrap the root
                    root = root.getJSONObject((String)root.keys().next());
                } catch (JSONException e) {
                    throw new XMLStreamException(e);
                }
                super.writeEndDocument();
            }
        };
    }

    public XMLStreamReader createXMLStreamReader(JSONTokener tokener) throws JSONException, XMLStreamException {
        return new MappedXMLStreamReader(new JSONObject(tokener), convention);
    }

    /**
     * Extracts parent/child tag name relationship.
     */
    private void extractTagNames(XSSchemaSet schemas) {
        XSVisitor collector = new SchemaWalker() {
            public void elementDecl(XSElementDecl decl) {
                tagNames.add(new QName(decl.getTargetNamespace(),decl.getName()));
            }
        };
        for( XSSchema s : schemas.getSchemas() )
            s.visit(collector);
    }

    private MutableXMLStreamBuffer readToBuffer(SDDocument doc) throws XMLStreamException, IOException {
        MutableXMLStreamBuffer buf = new MutableXMLStreamBuffer();
        doc.writeTo(null,resolver,new StreamWriterBufferCreator(buf));
        return buf;
    }

    private static final DocumentAddressResolver resolver = new DocumentAddressResolver() {
        public String getRelativeAddressFor(@NotNull SDDocument current, @NotNull SDDocument referenced) {
            return referenced.getURL().toExternalForm();
        }
    };

    //private static final String WSDL_NSURI = "http://schemas.xmlsoap.org/wsdl/";
}
