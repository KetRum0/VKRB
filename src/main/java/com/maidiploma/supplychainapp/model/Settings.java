package com.maidiploma.supplychainapp.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "settings")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Settings {

    @Id
    private Long id;

    @Column(name = "start_date")
    private LocalDate start_date;

    @Column(name = "cur_date")
    private LocalDate cur_date;

    @Column(name = "r")
    private int r;



}
