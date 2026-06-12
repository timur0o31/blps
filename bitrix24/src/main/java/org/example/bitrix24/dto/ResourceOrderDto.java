package org.example.bitrix24.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class ResourceOrderDto {
    private Long backendId;
    private String address;
    private String content;
    private Long courierId;
    private ResourceOrderStatus status;
}
