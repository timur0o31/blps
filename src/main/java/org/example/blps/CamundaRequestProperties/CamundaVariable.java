package org.example.blps.CamundaRequestProperties;


// мапинг атрибутов
public record CamundaVariable(Object value, String type) {

    public static CamundaVariable string(Object value) {
        return new CamundaVariable(
                value == null ? null : value.toString(),
                "String"
        );
    }

    public static CamundaVariable bool(Boolean value) {
        return new CamundaVariable(value, "Boolean");
    }

    public static CamundaVariable longValue(Long value) {
        return new CamundaVariable(value, "Long");
    }

    public static CamundaVariable integer(Integer value) {
        return new CamundaVariable(value, "Integer");
    }

    public static CamundaVariable enumValue(Enum<?> value) {
        return new CamundaVariable(
                value == null ? null : value.name(),
                "String"
        );
    }
}