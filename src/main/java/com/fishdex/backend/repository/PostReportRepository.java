package com.fishdex.backend.repository;

import com.fishdex.backend.entity.PostReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostReportRepository extends JpaRepository<PostReport, Long> {

    List<PostReport> findByPostGroupId(Long groupId);
}
