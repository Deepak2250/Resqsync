package com.reqsync.Reqsync.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.reqsync.Reqsync.Entity.ReportEntity;
import com.reqsync.Reqsync.Entity.User;

public interface ReportRepository extends JpaRepository<ReportEntity, Long> {

    @Query("SELECT re FROM ReportEntity re WHERE re.user = :user")
    List<ReportEntity> findByUser(@Param("user") User user);

    @Query("SELECT re FROM ReportEntity re WHERE re.fileName = :fileName")
    ReportEntity findByFileName(@Param("fileName") String fileName);

}
