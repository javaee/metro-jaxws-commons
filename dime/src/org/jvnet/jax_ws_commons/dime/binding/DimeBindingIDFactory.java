package org.jvnet.jax_ws_commons.dime.binding;

import javax.xml.ws.WebServiceException;

import com.sun.xml.ws.api.BindingID;
import com.sun.xml.ws.api.BindingIDFactory;

/**
 * BindingIDFactory that returns a DimeBindingID for endpoints that use a {@link DimeBindingID#DIME_BINDING}.
 * 
 * @author Oliver Treichel
 */
public class DimeBindingIDFactory extends BindingIDFactory {
    /**
     * Check if the declared binding id matches {@link DimeBindingID#DIME_BINDING} and return a {@link DimeBindingID} in
     * that case.
     */
    @Override
    public BindingID parse(final String lexical) throws WebServiceException {
        if (DimeBindingID.DIME_BINDING.equals(lexical)) {
            return DimeBindingID.getInstance();
        }

        return null;
    }
}
