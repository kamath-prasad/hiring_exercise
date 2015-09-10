package com.drmtx.app.model.domain;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.hibernate.annotations.GenericGenerator;

@Table(name = "CommentAnalysisInformation")
@Entity
public class CommentAnalysisInformation {

    @GeneratedValue(generator="system-uuid",strategy = GenerationType.AUTO)
    @GenericGenerator(name="system-uuid", strategy = "uuid")
    @Column(name = "id", unique = true, nullable = false)
    @Id
    private String id;

    private String url;

    @OneToMany(targetEntity = WordCountInformation.class, cascade = CascadeType.ALL, mappedBy = "commentAnalysisInformation")
    private Set<WordCountInformation> wordCountInformations = new HashSet<>(0);

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Set<WordCountInformation> getWordCountInformations() {
        return wordCountInformations;
    }

    public void setWordCountInformations(Set<WordCountInformation> wordCountInformations) {
        this.wordCountInformations = wordCountInformations;
    }

    public void addWordCountInformation(WordCountInformation wordCountInformation) {
        wordCountInformation.setCommentAnalysisInformation(this);
        this.wordCountInformations.add(wordCountInformation);
    }

    public void removeWordCountInformation(WordCountInformation wordCountInformation) {
        wordCountInformation.setCommentAnalysisInformation(null);
        this.wordCountInformations.remove(wordCountInformation);
    }

    @Override
    public String toString() {
        ReflectionToStringBuilder toStringBuilder = new ReflectionToStringBuilder(this,
                ToStringStyle.SHORT_PREFIX_STYLE);
        toStringBuilder.setExcludeFieldNames(new String[] { "wordCountInformations" });
        return toStringBuilder.toString();
    }
}
