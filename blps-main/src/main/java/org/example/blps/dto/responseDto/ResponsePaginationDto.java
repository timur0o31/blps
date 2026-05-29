package org.example.blps.dto.responseDto;

import java.util.List;

public record ResponsePaginationDto<T>(List<T> data, String page, String size,Long totalElements, Long maxPage, Long minPage, Long totalPage){
}
