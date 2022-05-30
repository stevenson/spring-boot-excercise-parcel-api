package com.stevenson.parcel.api.dto;

import com.stevenson.parcel.model.Parcel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ParcelResponse {

    private long id;
    private double weight;
    private double length;
    private double width;
    private double height;
    private double cost;
    private double volume;

    public ParcelResponse(Parcel parcel){
        this.id = parcel.getId();
        this.weight = parcel.getWeight();
        this.length = parcel.getLength();
        this.width = parcel.getWidth();
        this.height = parcel.getHeight();
        this.volume = parcel.getVolume();
        this.cost = parcel.getCost();
    }
}