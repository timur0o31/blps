package org.example.bitrix24.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class BitrixOrderDto {
    private Long backendId;
    private Long bitrixId;
    private String address;
    private String content;
    private BitrixOrderStatus status;
}
