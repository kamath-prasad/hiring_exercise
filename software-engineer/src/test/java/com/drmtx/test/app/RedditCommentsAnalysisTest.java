package com.drmtx.test.app;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.util.NestedServletException;

import com.drmtx.app.Application;
import com.drmtx.app.controller.RedditCommentsAnalysisController;
import com.drmtx.app.model.domain.CommentAnalysisInformation;
import com.drmtx.app.model.domain.WordCountInformation;
import com.drmtx.app.model.repository.CommentAnalysisInfoRepository;
import com.drmtx.app.model.repository.WordCountInfoRepository;
import com.drmtx.app.service.RedditCommentsAnalysisService;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
public class RedditCommentsAnalysisTest {

    private static final String GEN_NEW_COMM_ANALYSIS_URL = "/frequency/new";
    private static final String FETCH_COMM_ANALYSIS_URL = "/frequency";
    private static final String REDDIT_JSON_URL = "https://www.reddit.com/r/java/comments/32pj67/java_reference_in_gta_v_beautiful/.json";
    private MockMvc mvcMockObject;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Mock
    private RedditCommentsAnalysisService redditCommentsAnalysisServiceMock;

    @Mock
    private CommentAnalysisInfoRepository commentAnalysisInfoRepositoryMock;

    @Mock
    private WordCountInfoRepository wordCountInfoRepository;

    @InjectMocks
    private RedditCommentsAnalysisController redditCommentsAnalysisController;

    @InjectMocks
    private RedditCommentsAnalysisService redditCommentsAnalysisService;

    @Before
    public void setUp() throws Exception {
        this.mvcMockObject = MockMvcBuilders.webAppContextSetup(this.webApplicationContext).build();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void generateNewCommentAnalysisWithoutURLParam() throws Exception {
        this.mvcMockObject.perform(MockMvcRequestBuilders.get(GEN_NEW_COMM_ANALYSIS_URL).accept(MediaType.TEXT_PLAIN))
                .andExpect(status().isBadRequest());
    }

    @Test(expected = NestedServletException.class)
    public void generateNewCommentAnalysisWithEmptyURLParam() throws Exception {
        this.mvcMockObject.perform(
                MockMvcRequestBuilders.get(GEN_NEW_COMM_ANALYSIS_URL).param("url", "").accept(MediaType.TEXT_PLAIN))
                .andExpect(status().isBadRequest());
    }

    @Test(expected = NestedServletException.class)
    public void generateNewCommentAnalysisWithInvalidURL() throws Exception {
        this.mvcMockObject.perform(
                MockMvcRequestBuilders.get(GEN_NEW_COMM_ANALYSIS_URL).param("url", "hello")
                        .accept(MediaType.TEXT_PLAIN)).andExpect(status().isBadRequest());
    }

    @Test(expected = NestedServletException.class)
    public void generateNewCommentAnalysisWithInvalidRedditURL() throws Exception {
        this.mvcMockObject.perform(
                MockMvcRequestBuilders
                        .get(GEN_NEW_COMM_ANALYSIS_URL)
                        .param("url",
                                "https://www.reddit.com/r/java/comments/32pj67/java_reference_in_gta_v_beautiful/")
                        .accept(MediaType.TEXT_PLAIN)).andExpect(status().isBadRequest());
    }

    @Test
    public void generateNewCommentAnalysisWithValidURL() {
        try {
            this.mvcMockObject.perform(
                    MockMvcRequestBuilders.get(GEN_NEW_COMM_ANALYSIS_URL).param("url", REDDIT_JSON_URL)
                            .accept(MediaType.TEXT_PLAIN)).andExpect(status().is2xxSuccessful());

        } catch (Exception e) {
            Assert.assertThat(e, CoreMatchers.instanceOf(NestedServletException.class));
        }
    }

    @Test
    public void generateNewCommentAnalysisInternalCalls() {
        try {
            Map<String, Long> wordCountsMap = new HashMap<>();
            CommentAnalysisInformation commentAnalysisInformation = new CommentAnalysisInformation();
            commentAnalysisInformation.setUrl(REDDIT_JSON_URL);

            when(redditCommentsAnalysisServiceMock.getWordCounts(REDDIT_JSON_URL, "body")).thenReturn(wordCountsMap);
            when(redditCommentsAnalysisServiceMock.saveCommentAnalysisInformation(wordCountsMap, REDDIT_JSON_URL))
                    .thenReturn(commentAnalysisInformation);

            redditCommentsAnalysisController.generateNewCommentAnalysis(REDDIT_JSON_URL);

            verify(redditCommentsAnalysisServiceMock).getWordCounts(REDDIT_JSON_URL, "body");
            verify(redditCommentsAnalysisServiceMock).saveCommentAnalysisInformation(wordCountsMap, REDDIT_JSON_URL);
        } catch (Exception e) {
            Assert.assertThat(e, CoreMatchers.instanceOf(NestedServletException.class));
        }
    }

    @Test
    public void fetchCommentAnalysisWithInvalidId() {
        try {
            this.mvcMockObject.perform(
                    MockMvcRequestBuilders.get(FETCH_COMM_ANALYSIS_URL).requestAttr("id", "1234")
                            .accept(MediaType.TEXT_PLAIN)).andExpect(status().isNotFound());
        } catch (Exception e) {
            Assert.assertThat(e, CoreMatchers.instanceOf(NestedServletException.class));
        }
    }

    @Test
    public void testGetWordCounts() {
        try {
            Assert.assertTrue(redditCommentsAnalysisService.getWordCounts(REDDIT_JSON_URL, "body").size() > 0);
        } catch (Exception e) {
            Assert.assertThat(e, CoreMatchers.instanceOf(NestedServletException.class));
        }
    }

    @Test
    public void testGetTopNWordsByWordCountDesc() {
        try {
            PageRequest pageRequest = new PageRequest(0, 2);
            List<WordCountInformation> wordCountInfos = new ArrayList<>();
            when(wordCountInfoRepository.findByCommentAnalysisInfoId("some id", pageRequest))
                    .thenReturn(wordCountInfos);
            redditCommentsAnalysisService.getTopNWordsByWordCountDesc(2, "some id");
            verify(wordCountInfoRepository).findByCommentAnalysisInfoId("some id", pageRequest);
        } catch (Exception e) {
            Assert.assertThat(e, CoreMatchers.instanceOf(NestedServletException.class));
        }
    }

}
