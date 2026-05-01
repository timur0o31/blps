package org.example.blps.utils;
import org.example.blps.dto.responseDto.ResponsePaginationDto;

import java.util.List;

public class PaginationUtil {
    public record Params(long page, long size){
    }
    public static Params parse(String page, String size){
        try {
            long pageValue = Long.parseLong(page);
            long sizeValue = Long.parseLong(size);
            if (pageValue < 0) throw new IllegalArgumentException("page должен быть не отрицательным целым числом");
            if (sizeValue <= 0) throw new IllegalArgumentException("size должен быть положитеным целым числом!");
            return new Params(pageValue, sizeValue);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Параметры page и size должны быть целыми числами");
        }
    }
    public static <T> ResponsePaginationDto<T> responsePaginationDto(List<T> data,
            Params params, long totalElements) {
        long sizeValue = params.size();
        Long totalPages = 0L;
        if (totalElements/sizeValue!=0) totalPages = totalElements/sizeValue+1;
        Long lastPage = 0L;
        if (totalPages!=0) lastPage = totalPages-1;
        return new ResponsePaginationDto<>(data, String.valueOf(params.page()), String.valueOf(params.size()), totalElements,
                lastPage,
                0L,
                totalPages
        );
    }
}
