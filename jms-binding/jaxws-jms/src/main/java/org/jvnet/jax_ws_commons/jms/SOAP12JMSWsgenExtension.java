package org.jvnet.jax_ws_commons.jms;

import com.sun.tools.ws.api.WsgenExtension;
import com.sun.tools.ws.api.WsgenProtocol;

/**
 * @author Jitendra Kotamraju
 */
@WsgenProtocol(token="soap1.2/jms", lexical=JMSBindingID.SOAP12JMS_BINDING)
public class SOAP12JMSWsgenExtension extends WsgenExtension {
}