package com.drmtx.app.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import com.drmtx.app.exception.RedditCommentsAnalysisException;

public class CommonUtils {

    private static final Logger _logger = LogManager.getLogger(CommonUtils.class);

    private static final String JS_VAR_FOR_ATTR_VALUES = "attributeValues";
    /**
     * This javascript expression contains a function which evaluates all
     * possible places within the JSON object that contains value for attribute
     * recursively.
     */
    private static final String JS_EXPRESSION = "var jsonObj= %s ; var values = []; var property='%s'; addPropertyValue(jsonObj,values,property); var "
            + JS_VAR_FOR_ATTR_VALUES
            + " = values.join(' '); function addPropertyValue(jsonObj,values,property){ if(jsonObj instanceof Array){ for(var i in jsonObj){ addPropertyValue(i,values,property); }} if(jsonObj instanceof Object){ var temp= jsonObj[property]; if(temp){ values.push(temp); } for(var i in jsonObj){ addPropertyValue(jsonObj[i],values,property); } } }";
    private static final String SCRIPT_ENGINE = "JavaScript";

    /**
     * This method is used to perform HTTP GET request on a given URL and return
     * response as String
     * 
     * @param url
     *            URL to perform GET request
     * @return Response from the GET request as String
     * @throws RedditCommentsAnalysisException
     */
    public static String getStringResponseFromGet(String url) throws RedditCommentsAnalysisException {
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> jsonResponse = null;
        try {
            jsonResponse = restTemplate.getForEntity(url, String.class);
            _logger.debug(String.format("Response from GET request of URL : [%s] is [%s] ", url, jsonResponse));
        } catch (HttpStatusCodeException e) {
            throw new RedditCommentsAnalysisException(e.getStatusCode(), e.getMessage());
        }
        return (jsonResponse != null) ? jsonResponse.getBody() : null;
    }

    /**
     * This method counts the frequency of all non blank ( tab,space,new line)
     * words in a String and returns the result in a map.
     * 
     * @param text
     *            input text to analyze
     * @return map containing each distinct word from the string with its count.
     */
    public static Map<String, Long> getWordFrequency(String text) {
        Map<String, Long> result = new HashMap<>();
        if (!StringUtils.isEmpty(text)) {
            Scanner scanner = new Scanner(text);
            while (scanner.hasNext()) {
                String nextWord = scanner.next();
                if (StringUtils.hasText(nextWord)) {
                    if (result.containsKey(nextWord)) {
                        Long aLong = result.get(nextWord);
                        result.put(nextWord, ++aLong);
                    } else {
                        result.put(nextWord, 1L);
                    }
                }
            }
            scanner.close();
        }
        return result;
    }

    /**
     * This method returns all values as String (space concatenated) for a
     * particular attribute present in a JSON string
     * 
     * @param jsonString
     *            String representation of JSON
     * @param atrributeValue
     *            value of the attribute in the JSON whose values are needed
     * @return All values for attributeValue from JSON concatenated with space
     * @throws RedditCommentsAnalysisException
     */
    public static String getAllValuesForAttribute(String jsonString, String atrributeName)
            throws RedditCommentsAnalysisException {
        try {
            ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
            ScriptEngine javaScriptEngine = scriptEngineManager.getEngineByName(SCRIPT_ENGINE);
            Bindings bindings = javaScriptEngine.getBindings(ScriptContext.ENGINE_SCOPE);
            javaScriptEngine.eval(String.format(JS_EXPRESSION, jsonString, atrributeName));
            String result = String.valueOf(bindings.get(JS_VAR_FOR_ATTR_VALUES));
            _logger.debug(String.format("All values for attribute : [%s] in JSON string : [%s] is [%s]", atrributeName,
                    jsonString, result));
            return result;
        } catch (ScriptException e) {
            throw new RedditCommentsAnalysisException(e.getMessage());
        }
    }
}
