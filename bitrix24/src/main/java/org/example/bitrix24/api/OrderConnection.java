package org.example.bitrix24.api;

import jakarta.resource.ResourceException;
import org.example.bitrix24.dto.ResourceOrderDto;

public interface OrderConnection extends AutoCloseable {

    void createOrder(ResourceOrderDto order) throws ResourceException;

    void updateOrder(ResourceOrderDto order) throws ResourceException;

    @Override
    void close() throws ResourceException;
}
