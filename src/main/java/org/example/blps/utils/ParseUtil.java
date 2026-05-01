package org.example.blps.utils;

import java.util.Arrays;
import java.util.stream.Collectors;

public class ParseUtil {
    public static <E extends Enum<E>> E parseEnum(String value, Class<E> enumClass){
        if (value == null || value.isBlank()) return null;
        try{
            return Enum.valueOf(enumClass, value.toUpperCase());
        }catch(IllegalArgumentException e){
            String enumVal = Arrays.stream(enumClass.getEnumConstants())
                    .map((enumV) ->enumV.name()).collect(Collectors.joining("; "));
            throw new IllegalArgumentException("Недопустимое значение = " +value+". Допустимые значения = "+enumVal);
        }
    }
}
