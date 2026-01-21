@XmlSchema(
    namespace = "http://www.onvif.org/ver10/media/wsdl",
    elementFormDefault = XmlNsForm.QUALIFIED,
    xmlns = {
        @XmlNs(prefix = "trt", namespaceURI = "http://www.onvif.org/ver10/media/wsdl"),
        @XmlNs(prefix = "tt", namespaceURI = "http://www.onvif.org/ver10/schema")
    }
)
package cn.iocoder.yudao.module.iot.core.onvif.media;

import javax.xml.bind.annotation.XmlNs;
import javax.xml.bind.annotation.XmlNsForm;
import javax.xml.bind.annotation.XmlSchema;
