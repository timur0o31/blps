package org.example.bitrix24.outbound;

import jakarta.resource.ResourceException;
import jakarta.resource.spi.*;
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
public class OrderManagedConnectionFactory implements ManagedConnectionFactory {

    private String webhookUrl;
    private Integer entityTypeId;
    private String backendOrderIdFieldName;
    private String contentFieldName;
    private String addressFieldName;
    private String statusFieldName;
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
    public ManagedConnection matchManagedConnections(Set set, Subject subject, ConnectionRequestInfo connectionRequestInfo) throws ResourceException {
        for (Object candidate : set) {
            if (candidate instanceof OrderManagedConnection) {
                OrderManagedConnection connection = (OrderManagedConnection) candidate;
                if (equals(connection.getManagedConnectionFactory())) {
                    return connection;
                }
            }
        }
        return null;
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
                statusFieldName,
                titleFieldName
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof OrderManagedConnectionFactory)) {
            return false;
        }
        OrderManagedConnectionFactory other = (OrderManagedConnectionFactory) obj;
        return Objects.equals(webhookUrl, other.webhookUrl)
                && Objects.equals(entityTypeId, other.entityTypeId)
                && Objects.equals(backendOrderIdFieldName, other.backendOrderIdFieldName)
                && Objects.equals(contentFieldName, other.contentFieldName)
                && Objects.equals(addressFieldName, other.addressFieldName)
                && Objects.equals(statusFieldName, other.statusFieldName)
                && Objects.equals(titleFieldName, other.titleFieldName);
    }


    // достаем конфигурацию из ra.xml
    public String getWebhookUrl() {
        return webhookUrl;
    }

    public void setWebhookUrl(String webhookUrl) {
        this.webhookUrl = webhookUrl;
    }

    public Integer getEntityTypeId() {
        return entityTypeId;
    }

    public void setEntityTypeId(Integer entityTypeId) {
        this.entityTypeId = entityTypeId;
    }

    public String getBackendOrderIdFieldName() {
        return backendOrderIdFieldName;
    }

    public void setBackendOrderIdFieldName(String backendOrderIdFieldName) {
        this.backendOrderIdFieldName = backendOrderIdFieldName;
    }

    public String getContentFieldName() {
        return contentFieldName;
    }

    public void setContentFieldName(String contentFieldName) {
        this.contentFieldName = contentFieldName;
    }

    public String getAddressFieldName() {
        return addressFieldName;
    }

    public void setAddressFieldName(String addressFieldName) {
        this.addressFieldName = addressFieldName;
    }

    public String getStatusFieldName() {
        return statusFieldName;
    }

    public void setStatusFieldName(String statusFieldName) {
        this.statusFieldName = statusFieldName;
    }

    public String getTitleFieldName() {
        return titleFieldName;
    }

    public void setTitleFieldName(String titleFieldName) {
        this.titleFieldName = titleFieldName;
    }




}
