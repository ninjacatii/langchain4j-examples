package dev.langchain4j.example.entity;

import java.time.LocalDate;

public record Booking(
        String bookingNumber,
        LocalDate bookingBeginDate,
        LocalDate bookingEndDate,
        Customer customer) {
}