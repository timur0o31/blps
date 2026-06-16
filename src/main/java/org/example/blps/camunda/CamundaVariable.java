package org.example.blps.camunda;

public class CamundaVariable {
    private Object value;
    private String type;

    public CamundaVariable() {
    }

    public CamundaVariable(Object value, String type) {
        this.value = value;
        this.type = type;
    }

    public static CamundaVariable string(String value) {
        return new CamundaVariable(value, "String");
    }

    public static CamundaVariable bool(Boolean value) {
        return new CamundaVariable(value, "Boolean");
    }

    public static CamundaVariable integer(Integer value) {
        return new CamundaVariable(value, "Integer");
    }

    public static CamundaVariable longValue(Long value) {
        return new CamundaVariable(value, "Long");
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
