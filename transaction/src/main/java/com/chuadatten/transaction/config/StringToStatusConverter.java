package com.chuadatten.transaction.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import com.chuadatten.transaction.common.Status;

@Component
public class StringToStatusConverter implements Converter<String, Status> {
    @Override
    public Status convert(String source) {
        if (source == null || source.isBlank()) {
            return null;
        }
        return Status.valueOf(source.trim().toUpperCase());
    }
}
