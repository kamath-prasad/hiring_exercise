package com.drmtx.app.model.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.drmtx.app.model.domain.WordCountInformation;

@Repository
public interface WordCountInfoRepository extends JpaRepository<WordCountInformation, String> {

    @Query(value = "SELECT o FROM WordCountInformation o WHERE o.commentAnalysisInformation.id = ?1 order by o.count desc")
    List<WordCountInformation> findByCommentAnalysisInfoId(String commentAnalysisInfoId, Pageable pageable);

}
