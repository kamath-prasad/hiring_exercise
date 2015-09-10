package com.drmtx.app.model.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.drmtx.app.model.domain.CommentAnalysisInformation;

@Repository
public interface CommentAnalysisInfoRepository extends JpaRepository<CommentAnalysisInformation, String> {

}
