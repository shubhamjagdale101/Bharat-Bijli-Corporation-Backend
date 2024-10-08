package com.shubham.lightbill.lightbill_backend.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.util.Date;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BillDto {
    @NotBlank(message = "User ID cannot be blank")
    private String userId;

    @Min(value = 0, message = "Unit consumption must be non-negative")
    @NotNull(message = "Unit consumption cannot be null")
    private Double unitConsumption;

    @NotNull(message = "month of the bill cannot be null")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM")
    private String monthOfTheBill;

    @NotNull(message = "Due date cannot be null")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private Date dueDate;
}

