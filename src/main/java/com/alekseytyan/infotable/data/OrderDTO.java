package com.alekseytyan.infotable.data;

import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
public class OrderDTO {

    private Long id;

    private boolean isFinished;

    private boolean isVerified;

    @ToString.Exclude
    private List<LoadDTO> loads;

    @ToString.Exclude
    private LorryDTO lorry;

    @ToString.Exclude
    private List<DriverDTO> drivers;
}
