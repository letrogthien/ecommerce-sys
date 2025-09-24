package com.chuadatten.user.repository;

import java.util.List;
import java.util.UUID;

import com.chuadatten.user.entity.DeviceManager;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DeviceManagerRepository extends JpaRepository<DeviceManager, UUID> {
    List<DeviceManager> findByUserId(UUID userId);
    
    @Query("SELECT dm FROM DeviceManager dm WHERE dm.user.id = :userId")
    Page<DeviceManager> findByUserId(@Param("userId") UUID userId, Pageable pageable);
    
    Page<DeviceManager> findByDeviceType(String deviceType, Pageable pageable);
    
    @Query("SELECT dm FROM DeviceManager dm WHERE dm.user.id = :userId AND dm.deviceType = :deviceType")
    Page<DeviceManager> findByUserIdAndDeviceType(@Param("userId") UUID userId, @Param("deviceType") String deviceType, Pageable pageable);
}
