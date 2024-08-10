package com.test.redisMqTest.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "coupons")
public class Coupons {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long couponId;

    private String couponName;

    private int quantity;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @Builder
    public Coupons(String couponName, int quantity){
        this.couponName = couponName;
        this.quantity = quantity;
    }

    @JsonIgnore
    public void minusCoupon(){
        if(this.quantity > 0){
            this.quantity -= 1;
        }
    }

}
