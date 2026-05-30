package org.example.bitrix24.api;

import jakarta.resource.ResourceException;

public interface OrderConnectionFactory {
    OrderConnection getConnection() throws ResourceException;
}
