package com.drmtx.app.model.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.hibernate.annotations.GenericGenerator;

@Table(name = "WordCountInformation")
@Entity
public class WordCountInformation implements Comparable<WordCountInformation> {

    @GeneratedValue(generator = "uuid", strategy = GenerationType.AUTO)
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    @Column(name = "id", unique = true, nullable = false)
    @Id
    private String id;

    private String word;

    private Long count;

    @ManyToOne(targetEntity = CommentAnalysisInformation.class)
    @JoinColumn(name = "commentAnalysisInformationId", nullable = false, updatable = false)
    private CommentAnalysisInformation commentAnalysisInformation;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    public CommentAnalysisInformation getCommentAnalysisInformation() {
        return commentAnalysisInformation;
    }

    public void setCommentAnalysisInformation(CommentAnalysisInformation commentAnalysisInformation) {
        this.commentAnalysisInformation = commentAnalysisInformation;
    }

    @Override
    public int compareTo(WordCountInformation other) {
        return other.getCount().compareTo(this.getCount());
    }

    @Override
    public String toString() {
        ReflectionToStringBuilder toStringBuilder = new ReflectionToStringBuilder(this,
                ToStringStyle.SHORT_PREFIX_STYLE);
        toStringBuilder.setExcludeFieldNames(new String[] { "commentAnalysisInformation" });
        return toStringBuilder.toString();
    }
}
