package com.maidiploma.supplychainapp.repository;
import com.maidiploma.supplychainapp.model.Settings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface SettingsRepository extends JpaRepository<Settings, Long> {

    @Query("SELECT s.start_date FROM Settings s WHERE s.id = :id")
    LocalDate getStartDateById(@Param("id") Long id);

    @Query("SELECT s.cur_date FROM Settings s WHERE s.id = :id")
    LocalDate getCurDateById(@Param("id") Long id);

    @Query("SELECT s.r FROM Settings s WHERE s.id = :id")
    Integer getRById(long id);
}