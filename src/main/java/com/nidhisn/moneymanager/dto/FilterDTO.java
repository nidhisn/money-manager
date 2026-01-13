package com.nidhisn.moneymanager.dto;
import lombok.Data;
import java.time.LocalDate;

@Data
public class FilterDTO {

    private String type;
    private LocalDate startDate;
    private LocalDate endDate;
    private String keyword;
    private String sortField; //date, name, amount
    private String sortOrder; //asc, desc
}
