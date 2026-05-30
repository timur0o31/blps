package org.example.bitrix24.outbound;

import jakarta.resource.ResourceException;
import lombok.extern.slf4j.Slf4j;
import org.example.bitrix24.api.OrderConnection;
import org.example.bitrix24.dto.ResourceOrderDto;

@Slf4j
public class OrderConnectionImpl implements OrderConnection {

    private OrderManagedConnection managedConnection;
    private boolean valid = true;

    OrderConnectionImpl(OrderManagedConnection managedConnection) {
        this.managedConnection = managedConnection;
    }

    @Override
    public Long createOrder(ResourceOrderDto order) throws ResourceException {
        if (!valid || managedConnection == null) {
            throw new ResourceException("Connection is closed");
        }
        return managedConnection.createOrder(order);
    }

    @Override
    public Long updateOrder(ResourceOrderDto order) throws ResourceException {
        if (!valid || managedConnection == null) {
            throw new ResourceException("Connection is closed");
        }
        return managedConnection.updateOrder(order);
    }

    void setManagedConnection(OrderManagedConnection managedConnection) {
        this.managedConnection = managedConnection;
    }

    @Override
    public void close() throws ResourceException {
        log.info("[OrderConnectionImpl] close()");
        OrderManagedConnection connection = managedConnection;
        valid = false;
        managedConnection = null;
        if (connection != null) {
            connection.closeConnection(this);
        }
    }
}
