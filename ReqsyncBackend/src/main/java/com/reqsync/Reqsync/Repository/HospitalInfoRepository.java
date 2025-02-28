package com.reqsync.Reqsync.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import com.reqsync.Reqsync.Entity.HospitalInfo;

public interface HospitalInfoRepository extends JpaRepository<HospitalInfo, String> {

    @Query("SELECT h FROM HospitalInfo h WHERE h.user.email = :email")
    Optional<HospitalInfo> findByUserEmail(@Param("email") String email);

}
