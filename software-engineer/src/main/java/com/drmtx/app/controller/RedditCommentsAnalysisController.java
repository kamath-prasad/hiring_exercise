package com.drmtx.app.controller;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.drmtx.app.dto.WordCountDto;
import com.drmtx.app.exception.RedditCommentsAnalysisException;
import com.drmtx.app.model.domain.CommentAnalysisInformation;
import com.drmtx.app.model.repository.CommentAnalysisInfoRepository;
import com.drmtx.app.service.RedditCommentsAnalysisService;

@Controller
@RequestMapping(value = "/frequency")
public class RedditCommentsAnalysisController {

    private static final String NO_URL_PROVIDED = "No URL provided";
    private static final String NOT_VALID_URL = "Not valid URL : [%s]";
    private static final String NOT_VALID_REDDIT_COMMENTS_URL = "This is a invalid reddit comments URL : [%s]";
    private static final String URL_NOT_ENDING_WITH_JSON = NOT_VALID_REDDIT_COMMENTS_URL
            + " as it does not end with .json";
    private static final String URL_DOT_JSON = ".json";
    private static final String JSON_ATTR_NAME = "body";
    private static final String COMMENT_ANALYSIS_CANNOT_BE_FOUND = "Comment analysis not found for id : [%s]";
    private static final String INVALID_COUNT_VALUE = "Count value is invalid : [%d]";
    private static final String REDDIT_DOMAIN = "www.reddit.com";
    private static final String COMMENTS_IN_REDDIT_URL = "comments";

    @Autowired
    private RedditCommentsAnalysisService redditCommentsAnalysisService;

    @Autowired
    private CommentAnalysisInfoRepository commentAnalysisInfoRepository;

    /**
     * This method is used to perform a new comment word count analysis of a
     * reddit comments URL
     * 
     * @param url
     *            url to reddit comments in json format
     * @return id of the stored results of reddit comment word count analysis
     * @throws RedditCommentsAnalysisException
     */
    @ResponseBody
    @RequestMapping(value = "/new", method = RequestMethod.GET, produces = MediaType.TEXT_PLAIN_VALUE)
    public String generateNewCommentAnalysis(@RequestParam(value = "url") String url)
            throws RedditCommentsAnalysisException {

        // validate URL
        validateURL(url);

        // perform analysis
        Map<String, Long> wordCountsMap = redditCommentsAnalysisService.getWordCounts(url, JSON_ATTR_NAME);

        // store result
        CommentAnalysisInformation commentAnalysisInformation = redditCommentsAnalysisService
                .saveCommentAnalysisInformation(wordCountsMap, url);

        return commentAnalysisInformation.getId();
    }

    /**
     * This method is used to return the top N words based on counts in
     * descending order for the comment analysis
     * 
     * @param id
     *            id of the word count analysis result returned from the /new
     *            API
     * @param count
     *            number of word-count results to be returned
     * @return JSON representation of word to counts array
     * @throws RedditCommentsAnalysisException
     */
    @ResponseBody
    @RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<WordCountDto> fetchCommentAnalysis(@PathVariable(value = "id") String id,
            @RequestParam(value = "count", required = false) Integer count) throws RedditCommentsAnalysisException {

        // validate params
        validateRequestParams(id, count);

        return redditCommentsAnalysisService.getTopNWordsByWordCountDesc(count, id);

    }

    /**
     * THis method validate the correctness of the reddit comments URL
     * 
     * @param url
     *            value of the reddit comments URL
     * @throws RedditCommentsAnalysisException
     */
    private void validateURL(String url) throws RedditCommentsAnalysisException {
        if (StringUtils.isBlank(url)) {
            throw new RedditCommentsAnalysisException(HttpStatus.BAD_REQUEST, NO_URL_PROVIDED);
        }
        try {
            new URL(url);
        } catch (MalformedURLException e) {
            throw new RedditCommentsAnalysisException(HttpStatus.BAD_REQUEST, String.format(NOT_VALID_URL, url), e);
        }

        if (!url.contains(REDDIT_DOMAIN) || !url.contains(COMMENTS_IN_REDDIT_URL)) {
            throw new RedditCommentsAnalysisException(HttpStatus.BAD_REQUEST, String.format(
                    NOT_VALID_REDDIT_COMMENTS_URL, url));
        }

        if (!url.endsWith(URL_DOT_JSON)) {
            throw new RedditCommentsAnalysisException(HttpStatus.BAD_REQUEST, String.format(URL_NOT_ENDING_WITH_JSON,
                    url));
        }
    }

    /**
     * This method validates all the request params to fetch the top N words by
     * counts
     * 
     * @param id
     *            id of the word count analysis result
     * @param count
     *            Number of word-count analysis in the result
     * @throws RedditCommentsAnalysisException
     */
    private void validateRequestParams(String id, Integer count) throws RedditCommentsAnalysisException {
        CommentAnalysisInformation commentAnalysisInfo = commentAnalysisInfoRepository.findOne(id);
        if (commentAnalysisInfo == null) {
            throw new RedditCommentsAnalysisException(HttpStatus.NOT_FOUND, String.format(
                    COMMENT_ANALYSIS_CANNOT_BE_FOUND, id));
        }

        if (count != null && (count < 1 || count > Integer.MAX_VALUE)) {
            throw new RedditCommentsAnalysisException(HttpStatus.BAD_REQUEST, INVALID_COUNT_VALUE);
        }
    }
}
