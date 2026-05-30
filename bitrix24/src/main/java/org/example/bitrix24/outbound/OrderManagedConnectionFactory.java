package org.example.bitrix24.outbound;

import jakarta.resource.ResourceException;
import jakarta.resource.spi.*;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.example.bitrix24.api.OrderConnection;
import org.example.bitrix24.api.OrderConnectionFactory;

import javax.security.auth.Subject;
import java.io.PrintWriter;
import java.util.Objects;
import java.util.Set;

// @ConnectionDefinition нужен, чтобы JCA-контейнер понял, какие классы составляют “соединение” твоего адаптера.
@ConnectionDefinition(
        connectionFactory = OrderConnectionFactory.class,
        connectionFactoryImpl = OrderConnectionFactoryImpl.class,
        connection = OrderConnection.class,
        connectionImpl = OrderConnectionImpl.class
    )

@Slf4j
@Getter
@Setter
public class OrderManagedConnectionFactory implements ManagedConnectionFactory {

    private String webhookUrl;
    private Integer entityTypeId;
    private String backendOrderIdFieldName;
    private String contentFieldName;
    private String addressFieldName;
    private String titleFieldName;

    @Override
    public Object createConnectionFactory() throws ResourceException {
        log.info("[OrderManagedConnectionFactory] createConnectionFactory()-NM");
        ConnectionManager connectionManager = (managedConnectionFactory, cxRequestInfo) ->
                managedConnectionFactory.createManagedConnection(null, cxRequestInfo)
                        .getConnection(null, cxRequestInfo);
        return new OrderConnectionFactoryImpl(this, connectionManager);
    }

    // Сервер приложений предоставляет менеджер подключений */
    @Override
    public Object createConnectionFactory(ConnectionManager connectionManager) throws ResourceException {
        log.info("Creating OrderConnectionFactory");
        return new OrderConnectionFactoryImpl(this, connectionManager);
    }

    // createConnectionFactory создаёт объект фабрики, через который приложение потом будет получать соединения.
    @Override
    public ManagedConnection createManagedConnection(Subject subject, ConnectionRequestInfo cxRequestInfo) throws ResourceException {
        log.info("[OrderManagedConnectionFactory] createManagedConnection()");
        try {
            return new OrderManagedConnection(this);
        } catch (Exception e) {
            throw new ResourceException(e.getCause());
        }
    }

    @Override
    public ManagedConnection matchManagedConnections(Set connectionSet,
                                                     Subject subject,
                                                     ConnectionRequestInfo cxRequestInfo) throws ResourceException {
        log.info("[TradeManagedConnectionFactory] matchManagedConnections()");
        /* This resource adapter does not use security (Subject) */
        OrderManagedConnection match = null;
        /* This resource adapter has no additional parameters for connections,
         * so any open connection can be used by an application */
        for (Object mco : connectionSet) {
            if (mco != null) {
                match = (OrderManagedConnection) mco;
                log.info("Connection match!");
                break;
            }
        }
        return match;
    }
    @Override
    public void setLogWriter(PrintWriter printWriter) throws ResourceException {

    }

    @Override
    public PrintWriter getLogWriter() throws ResourceException {
        return null;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                webhookUrl,
                entityTypeId,
                backendOrderIdFieldName,
                contentFieldName,
                addressFieldName,
                titleFieldName
        );
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof OrderManagedConnectionFactory other
                && Objects.equals(webhookUrl, other.webhookUrl)
                && Objects.equals(entityTypeId, other.entityTypeId)
                && Objects.equals(backendOrderIdFieldName, other.backendOrderIdFieldName)
                && Objects.equals(contentFieldName, other.contentFieldName)
                && Objects.equals(addressFieldName, other.addressFieldName)
                && Objects.equals(titleFieldName, other.titleFieldName);
    }
}
