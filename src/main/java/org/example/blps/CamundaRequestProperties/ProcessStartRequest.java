package org.example.blps.CamundaRequestProperties;

import java.util.Map;

public record ProcessStartRequest(Map<String, CamundaVariable> variables, boolean withVariablesInReturn) {
}