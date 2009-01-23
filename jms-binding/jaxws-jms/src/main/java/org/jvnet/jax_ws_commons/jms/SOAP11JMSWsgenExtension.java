package org.jvnet.jax_ws_commons.jms;

import com.sun.tools.ws.api.WsgenExtension;
import com.sun.tools.ws.api.WsgenProtocol;

/**
 * @author Jitendra Kotamraju
 */
@WsgenProtocol(token="soap1.1/jms", lexical=JMSBindingID.SOAP11JMS_BINDING)
public class SOAP11JMSWsgenExtension extends WsgenExtension {
}