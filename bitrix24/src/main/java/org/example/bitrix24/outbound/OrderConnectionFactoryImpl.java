package org.example.bitrix24.outbound;

import jakarta.resource.Referenceable;
import jakarta.resource.ResourceException;
import jakarta.resource.spi.ConnectionManager;
import jakarta.resource.spi.ManagedConnectionFactory;
import lombok.extern.slf4j.Slf4j;

import org.example.bitrix24.api.OrderConnection;
import org.example.bitrix24.api.OrderConnectionFactory;

import javax.naming.Reference;
import java.io.Serializable;

@Slf4j
public class OrderConnectionFactoryImpl implements OrderConnectionFactory, Referenceable, Serializable {

    private final ManagedConnectionFactory managedConnectionFactory;
    private final ConnectionManager connectionManager;
    private Reference reference;

    public OrderConnectionFactoryImpl(ManagedConnectionFactory managedConnectionFactory, ConnectionManager connectionManager) {
        this.managedConnectionFactory = managedConnectionFactory;
        this.connectionManager = connectionManager;
    }

    //Приложения вызывают этот метод, который делегирует менеджеру
    // соединений контейнера
    // получение экземпляра соединения через
    @Override
    public OrderConnection getConnection() throws ResourceException {
        log.info("[OrderConnectionFactoryImpl] getConnection()");
            return (OrderConnection) connectionManager.allocateConnection(managedConnectionFactory, null);
    }

    @Override
    public void setReference(Reference reference) {
        this.reference = reference;
    }

    @Override
    public Reference getReference() {
        return reference;
    }

}
