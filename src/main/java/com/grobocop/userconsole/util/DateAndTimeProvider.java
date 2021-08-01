package com.grobocop.userconsole.util;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Date;

@Component
public class DateAndTimeProvider {

    public Date getCurrentDate() {
        return new Date();
    }

    public LocalDateTime getCurrentLocalDateTime() {
        return LocalDateTime.now();
    }
}
