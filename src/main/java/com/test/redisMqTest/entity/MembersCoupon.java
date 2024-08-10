package com.test.redisMqTest.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Table(name = "member_coupon")
public class MembersCoupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long membersCouponId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "members")
    private Members members;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "coupons")
    private Coupons coupons;

    @CreationTimestamp
    private LocalDateTime issuedAt;

    @Builder
    public MembersCoupon(Members members, Coupons coupons){
        this.members = members;
        this.coupons = coupons;
    }

}
