package org.example.bitrix24.outbound;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.resource.ResourceException;
import jakarta.resource.spi.*;
import org.example.bitrix24.dto.BitrixResponceDto;
import org.example.bitrix24.dto.BitrixResponceUpdateDto;
import org.example.bitrix24.dto.ResourceOrderDto;
import org.example.bitrix24.dto.ResourceOrderStatus;

import javax.security.auth.Subject;
import javax.transaction.xa.XAResource;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class OrderManagedConnection implements ManagedConnection {

    private final OrderManagedConnectionFactory managedConnectionFactory;
    private final Set<OrderConnectionImpl> connections = new HashSet<>();
    private final Set<ConnectionEventListener> listeners = new HashSet<>();

    OrderManagedConnection(OrderManagedConnectionFactory factory) {
        this.managedConnectionFactory = factory;
    }

    @Override
    public Object getConnection(Subject subject, ConnectionRequestInfo cxRequestInfo) {
        OrderConnectionImpl connection = new OrderConnectionImpl(this);
        connections.add(connection);
        return connection;
    }


    ObjectMapper objectMapper = new ObjectMapper();

    private String mapStatusToStageId(ResourceOrderStatus status) {
        return switch (status) {
            case NEW -> "DT1040_15:NEW";
            case WAITING -> "DT1040_15:PREPARATION";
            case PENDING -> "DT1040_15:CLIENT";
            case ACCEPTED -> "DT1040_15:UC_70WZ8L";
            case PICKED_UP -> "DT1040_15:UC_UKHRN8";
            case ON_THE_WAY -> "DT1040_15:UC_7XYPQQ";
            case DELIVERED -> "DT1040_15:UC_7X4QXU";
            case FAILED -> "DT1040_15:UC_UR4J9L";
        };
    }


    private Map<String, Object> mapToBitrixFields(ResourceOrderDto order) {
        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put(managedConnectionFactory.getTitleFieldName(), "Order #" + order.getBackendId());
        fields.put(managedConnectionFactory.getBackendOrderIdFieldName(), order.getBackendId());
        fields.put(managedConnectionFactory.getContentFieldName(), order.getContent());
        fields.put(managedConnectionFactory.getAddressFieldName(), order.getAddress());
        fields.put("stageId", mapStatusToStageId(order.getStatus()));
        return fields;
    }

    Long createOrder(ResourceOrderDto order) throws ResourceException {
        Map<String, Object> fields = mapToBitrixFields(order);
        BitrixResponceDto request = new BitrixResponceDto(managedConnectionFactory.getEntityTypeId(), fields);
        try {
            String json = objectMapper.writeValueAsString(request);
            String response = post("crm.item.add.json", json);
            JsonNode root = objectMapper.readTree(response);
            return root.path("result").path("item").path("id").asLong();
        } catch (Exception e) {
            throw new ResourceException("Не удалось создать заказ в Bitrix", e);
        }
    }

    private Long findBitrixIdByBackendId(Long backendId) throws Exception {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("entityTypeId", managedConnectionFactory.getEntityTypeId());
        payload.put("filter", Map.of(managedConnectionFactory.getBackendOrderIdFieldName(), backendId));
        String response = post("crm.item.list.json", objectMapper.writeValueAsString(payload));
        JsonNode items = objectMapper.readTree(response).path("result").path("items");
        if (!items.isArray() || items.isEmpty()) {
            throw new ResourceException("Bitrix заказ не найден по backendId: " + backendId);
        }
        return items.get(0).path("id").asLong();
    }

    Long updateOrder(ResourceOrderDto order) throws ResourceException {
        try {
            Long bitrixId = findBitrixIdByBackendId(order.getBackendId());
            BitrixResponceUpdateDto request = new BitrixResponceUpdateDto(managedConnectionFactory.getEntityTypeId(), bitrixId, mapToBitrixFields(order));
            post("crm.item.update.json", objectMapper.writeValueAsString(request));
            return bitrixId;
        } catch (Exception e) {
            throw new ResourceException("Не удалось обновить заказ в Bitrix", e);
        }
    }

    private String post(String method, String json) throws Exception {
        URL url = new URL(managedConnectionFactory.getWebhookUrl() + method);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/json");
        try (OutputStream outputStream = connection.getOutputStream()) {
            outputStream.write(json.getBytes(StandardCharsets.UTF_8));
        }
        try (InputStream inputStream = connection.getInputStream()) {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    @Override
    public void destroy() throws ResourceException {
        cleanup();
    }

    @Override
    public void cleanup() throws ResourceException {
        for (OrderConnectionImpl connection : new ArrayList<>(connections)) {
            connection.setManagedConnection(null);
        }
        connections.clear();
    }

    @Override
    public void associateConnection(Object o) throws ResourceException {
        if (!(o instanceof OrderConnectionImpl)) {
            throw new ResourceException("Неправильный connection!: " + o);
        }
        OrderConnectionImpl connection = (OrderConnectionImpl) o;
        connection.setManagedConnection(this);
        connections.add(connection);
    }

    @Override
    public void addConnectionEventListener(ConnectionEventListener connectionEventListener) {
        listeners.add(connectionEventListener);
    }

    @Override
    public void removeConnectionEventListener(ConnectionEventListener connectionEventListener) {
        listeners.remove(connectionEventListener);
    }

    @Override
    public XAResource getXAResource() throws ResourceException {
        return null;
    }

    @Override
    public LocalTransaction getLocalTransaction() throws ResourceException {
        return null;
    }

    @Override
    public ManagedConnectionMetaData getMetaData() throws ResourceException {
        return null;
    }

    @Override
    public void setLogWriter(PrintWriter printWriter) throws ResourceException {
    }

    @Override
    public PrintWriter getLogWriter() throws ResourceException {
        return null;
    }

    void closeConnection(OrderConnectionImpl connection) {
        connections.remove(connection);
    }

}

