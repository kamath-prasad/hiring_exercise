package com.drmtx.app.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.transaction.Transactional;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.drmtx.app.dto.WordCountDto;
import com.drmtx.app.exception.RedditCommentsAnalysisException;
import com.drmtx.app.model.domain.CommentAnalysisInformation;
import com.drmtx.app.model.domain.WordCountInformation;
import com.drmtx.app.model.repository.CommentAnalysisInfoRepository;
import com.drmtx.app.model.repository.WordCountInfoRepository;
import com.drmtx.app.util.CommonUtils;

@Service
public class RedditCommentsAnalysisService {

    private static final Logger _logger = LogManager.getLogger(RedditCommentsAnalysisService.class);
    private static final String CHARS_TO_IGNORE_REGEX = "[!,\\.\\?]";
    private static final String SPACE = " ";

    @Autowired
    private CommentAnalysisInfoRepository commentAnalysisInfoRepository;

    @Autowired
    private WordCountInfoRepository wordCountInfoRepository;

    /**
     * This method does the analysis to get count of all distinct words present
     * for the attribute present in the json returned by the URL
     * 
     * @param url
     *            URL which can return JSON on performing HTTP GET
     * @param attribute
     *            attribute name in the JSON
     * @return Map containing distinct words as key and frequency as value
     * @throws RedditCommentsAnalysisException
     */
    public Map<String, Long> getWordCounts(String url, String attribute) throws RedditCommentsAnalysisException {
        _logger.info(String.format("Starting word count analysis for url : [%s] and attribute [%s]", url, attribute));
        String jsonResponse = CommonUtils.getStringResponseFromGet(url);
        String valuesForAttribute = CommonUtils.getAllValuesForAttribute(jsonResponse, attribute);
        valuesForAttribute = valuesForAttribute.replaceAll(CHARS_TO_IGNORE_REGEX, SPACE).trim().toLowerCase();
        Map<String, Long> wordCounts = CommonUtils.getWordFrequency(valuesForAttribute);
        _logger.info(String.format("Completed word count analysis for url : [%s] and attribute [%s]", url, attribute));
        return wordCounts;
    }

    /**
     * This method is used to persist the Comment analysis data into the H2
     * database
     * 
     * @param wordCountsMap
     *            map containing word as key and number of occurances as value
     * @param url
     *            url for which the analysis was performed
     * @return CommentAnalysisInformation object that was saved to H2 DB
     * @throws RedditCommentsAnalysisException
     */
    @Transactional
    public CommentAnalysisInformation saveCommentAnalysisInformation(Map<String, Long> wordCountsMap, String url)
            throws RedditCommentsAnalysisException {
        _logger.info(String.format("Starting storage of analysis results url : [%s] ", url));

        CommentAnalysisInformation commentAnalysisInformation = new CommentAnalysisInformation();
        commentAnalysisInformation.setUrl(url);
        if (wordCountsMap != null && !wordCountsMap.isEmpty()) {
            for (String word : wordCountsMap.keySet()) {
                WordCountInformation wordCountInformation = new WordCountInformation();
                wordCountInformation.setWord(word);
                wordCountInformation.setCount(wordCountsMap.get(word));
                commentAnalysisInformation.addWordCountInformation(wordCountInformation);
            }
        }

        if (commentAnalysisInfoRepository.saveAndFlush(commentAnalysisInformation) == null) {
            throw new RedditCommentsAnalysisException(String.format(
                    "Unable to store analysis results for input URL : [%s]", url));
        }

        _logger.debug(String.format("Value of Object persisted in DB : [%s]", commentAnalysisInformation));

        _logger.info(String.format("Compelted storage of analysis results url : [%s] ", url));
        return commentAnalysisInformation;
    }

    /**
     * This method is used to return the top N words based on count in
     * descending order
     * 
     * @param n
     *            Limit for the number of words to be returned
     * @param id
     *            id of the word count analysis
     * @return list of word-count objects
     */
    public List<WordCountDto> getTopNWordsByWordCountDesc(Integer n, String id) {
        _logger.info(String.format(
                "Starting fetching of results of Top N : [%d] words by counts in desc order for id : [%s] ", n, id));

        PageRequest pageRequest = new PageRequest(0, n);
        List<WordCountInformation> wordCountInfos = wordCountInfoRepository
                .findByCommentAnalysisInfoId(id, pageRequest);

        List<WordCountDto> dtos = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(wordCountInfos)) {
            for (WordCountInformation wordCountInfo : wordCountInfos) {
                dtos.add(new WordCountDto(wordCountInfo.getWord(), wordCountInfo.getCount()));
            }
        }

        _logger.info(String.format(
                "Completed fetching of results of Top N : [%d] words by counts in desc order for id : [%s] ", n, id));

        return dtos;
    }

}
