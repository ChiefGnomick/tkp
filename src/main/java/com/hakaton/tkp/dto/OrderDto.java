package com.hakaton.tkp.dto;

import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderDto {
    private int orderNumber;
    private List<String> components;
    private BigDecimal finalPrice; 
}
