package org.example.bitrix24;
import jakarta.resource.ResourceException;
import jakarta.resource.spi.ActivationSpec;
import jakarta.resource.spi.BootstrapContext;
import jakarta.resource.spi.Connector;
import jakarta.resource.spi.ResourceAdapter;
import jakarta.resource.spi.ResourceAdapterInternalException;
import jakarta.resource.spi.TransactionSupport.TransactionSupportLevel;
import jakarta.resource.spi.endpoint.MessageEndpointFactory;
import lombok.extern.slf4j.Slf4j;

import javax.transaction.xa.XAResource;

@Connector(displayName = "Bitrix24ResourceAdapter",
        vendorName = "org.example",
        version = "1.0",
        transactionSupport = TransactionSupportLevel.NoTransaction)

@Slf4j
public class Bitrix24ResourceAdapter implements ResourceAdapter {



    @Override
    public void start(BootstrapContext ctx) throws ResourceAdapterInternalException {
        log.info("[Bitrix24ResourceAdapter] start()");
    }

    @Override
    public void stop() {
        log.info("[Bitrix24ResourceAdapter] stop()");
    }

    @Override
    public void endpointActivation(MessageEndpointFactory endpointFactory, ActivationSpec spec)
            throws ResourceException {
        log.info("[Bitrix24ResourceAdapter] endpointActivation()");
    }

    @Override
    public void endpointDeactivation(MessageEndpointFactory endpointFactory, ActivationSpec spec) {
        log.info("[Bitrix24ResourceAdapter] endpointDeactivation()");
    }

    @Override
    public XAResource[] getXAResources(ActivationSpec[] specs) throws ResourceException {
        return null;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Bitrix24ResourceAdapter;
    }

    @Override
    public int hashCode() {
        return Bitrix24ResourceAdapter.class.hashCode();
    }
}