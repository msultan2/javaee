/*
 * THIS SOFTWARE IS PROVIDED BY COSTAIN INTEGRATED TECHNOLOGY SOLUTIONS
 * LIMITED ``AS IS'', WITH NO WARRANTY, TERM OR CONDITION OF ANY KIND,
 * EXPRESS OR IMPLIED, AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING,
 * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL COSTAIN
 * INTEGRATED TECHNOLOGY SOLUTIONS LIMITED BE LIABLE FOR ANY LOSSES, CLAIMS
 * OR DAMAGES OF WHATEVER NATURE, INCLUDING ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGES OR LOSSES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE.
 *
 * Copyright 2016 © Costain Integrated Technology Solutions Limited.
 * All Rights Reserved.
 */
package com.ssl.rmas.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.oxm.UnmarshallingFailureException;
import org.springframework.oxm.XmlMappingException;
import org.springframework.oxm.support.AbstractMarshaller;
import org.w3c.dom.Node;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.DefaultHandler;

import com.ssl.rmas.entities.Device;

public class StaticDeviceDataXMLToDeviceUnmarshaller extends AbstractMarshaller {

    private final Logger logger = LoggerFactory.getLogger(StaticDeviceDataXMLToDeviceUnmarshaller.class);

    @Override
    public boolean supports(Class<?> clazz) {
        return Device.class.equals(clazz);
    }

    @Override
    protected void marshalDomNode(Object graph, Node node) throws XmlMappingException {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void marshalXmlEventWriter(Object graph, XMLEventWriter eventWriter) throws XmlMappingException {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void marshalXmlStreamWriter(Object graph, XMLStreamWriter streamWriter) throws XmlMappingException {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void marshalSaxHandlers(Object graph, ContentHandler contentHandler, LexicalHandler lexicalHandler)
            throws XmlMappingException {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void marshalOutputStream(Object graph, OutputStream outputStream)
            throws XmlMappingException, IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void marshalWriter(Object graph, Writer writer) throws XmlMappingException, IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    protected Object unmarshalDomNode(Node node) throws XmlMappingException {
        throw new UnsupportedOperationException();
    }

    @Override
    protected Object unmarshalXmlEventReader(XMLEventReader eventReader) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected Object unmarshalXmlStreamReader(XMLStreamReader streamReader) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected Object unmarshalSaxReader(XMLReader xmlReader, InputSource inputSource)
            throws XmlMappingException, IOException {

        try {
            DeviceContentHandler contentHandler = new DeviceContentHandler();
            xmlReader.setContentHandler(contentHandler);
            xmlReader.parse(inputSource);
            return contentHandler.getDevice();
        } catch (SAXException e) {
            throw new UnmarshallingFailureException("SAX reader exception", e);
        }
    }

    @Override
    protected Object unmarshalInputStream(InputStream inputStream) throws XmlMappingException, IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    protected Object unmarshalReader(Reader reader) throws XmlMappingException, IOException {
        throw new UnsupportedOperationException();
    }

    private class DeviceContentHandler extends DefaultHandler {
        private final String rmasSchemaUri = "http//www.nmcs2.org/schemas/RAP2";
        private Device device = null;
        private Stack<String> elementStack = new Stack<>();
        private Stack<StringBuilder> elementText = new Stack<>();

        @Override
        public void startDocument() throws SAXException {
            this.device = new Device();
            super.startDocument();
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes)
                throws SAXException {
            elementStack.push(uri + ":" + localName);
            elementText.push(new StringBuilder());
            logger.trace("Start of element uri: {}, localName: {}, qName: {}, attributes: {}", uri, localName, qName, attributes);
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            elementStack.pop();
            String elementTextString = elementText.pop().toString().trim();
            logger.trace("End of element uri: {}, localName: {}, qName: {} with text {}", uri, localName, qName, elementTextString);

            if(rmasSchemaUri.equals(uri)) {
                switch(localName) {
                    case "ipAddress":
                        device.setIpAddress(elementTextString);
                        break;
                    case "manufacturerName":
                        device.setManufacturer(elementTextString);
                        break;
                    case "manufacturerTypeNo":
                        device.setManufacturerType(elementTextString);
                        break;
                    case "serialNo":
                        device.setSerialNumber(elementTextString);
                        break;
                    case "hardwareVersion":
                        device.setHardwareVersion(elementTextString);
                        break;
                    case "firmwareVersion":
                        device.setFirmwareVersion(elementTextString);
                        break;
                    case "hostName":
                        try {
                            device.setHostname(new URI(elementTextString));
                        } catch (URISyntaxException e) {
                            throw new SAXException("Failed to create hostname", e);
                        }
                        break;
                    case "haGeographicAddress":
                        device.setHaGeographicAddress(elementTextString);
                        break;
                    case "latitude":
                        device.setLatitude(Double.parseDouble(elementTextString));
                        break;
                    case "longitude":
                        device.setLongitude(Double.parseDouble(elementTextString));
                        break;
                    case "manufacturerSpecificData":
                        device.setManufacturerSpecificData(elementTextString);
                        break;
                    case "etrs89Position":
                    case "devicetype":
                    case "deviceList":
                    case "staticDeviceData":
                        break;
                    default:
                        if(elementStack.size()>2 && elementStack.contains(rmasSchemaUri + ":deviceList")) {
                            Map<String, List<String>> deviceTypeList = device.getDeviceList();
                            if(deviceTypeList==null) {
                                deviceTypeList = new HashMap<>();
                                device.setDeviceList(deviceTypeList);
                            }
                            //We are processing the device list
                            if((rmasSchemaUri + ":devicetype").equals(elementStack.peek())) {
                                //We are processing a type
                                logger.trace("Processed type {}", localName);
                                if(!deviceTypeList.containsKey(localName)) {
                                    deviceTypeList.put(localName, new ArrayList<String>());
                                }
                            } else {
                                //We are processing a sub type
                                String deviceTypeParent = elementStack.peek().replace(rmasSchemaUri + ":", "");
                                logger.trace("Processed type {} sub type {}", deviceTypeParent, localName);
                                if(!deviceTypeList.containsKey(deviceTypeParent)) {
                                    deviceTypeList.put(deviceTypeParent, new ArrayList<String>());
                                }
                                deviceTypeList.get(deviceTypeParent).add(localName);
                            }
                        } else {
                            logger.debug("Unhandled element {} with text {}", localName, elementTextString);
                        }
                }
            } else {
                logger.debug("Unhandled element {}:{} with text {}", uri, localName, elementTextString);
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            elementText.peek().append(ch, start, length);
        }

        public Device getDevice() {
            return device;
        }
    }
}
