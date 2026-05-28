package org.example.bitrix24.api;

import jakarta.resource.ResourceException;
import org.example.bitrix24.dto.BitrixOrderDto;

public interface OrderConnection extends AutoCloseable {

    Long createOrder(BitrixOrderDto order) throws ResourceException;

    @Override
    void close() throws ResourceException;
}
