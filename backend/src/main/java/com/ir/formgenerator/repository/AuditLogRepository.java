package com.ir.formgenerator.repository;

import com.ir.formgenerator.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {


    List<AuditLog> findAllByOrderByTimestampDesc();
}
