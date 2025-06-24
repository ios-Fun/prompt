package com.changgeng.prompt.service;

import com.alibaba.fastjson.JSONObject;
import com.changgeng.prompt.client.CozeClient;
import com.changgeng.prompt.pojo.LLMResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.ByteOrderMark;
import org.apache.commons.io.input.BOMInputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import com.changgeng.prompt.config.PromptConfig;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class ReadPromptService {

    private final CozeClient cozeClient;

    @Autowired
    PromptConfig promptConfig;

    private final static String DEFAULT_ENCODING = "GBK";


    private String readFile(String fileName) {
        StringBuilder content = new StringBuilder();
        try {
            BufferedReader bufferedReader  = new BufferedReader(new FileReader(fileName));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                content.append(line).append('\n');
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content.toString();
    }

    // 提取标题，如果文章过长，会影响提取结果准确性，然后裁剪content内容再次提取
    public String getTitle(MultipartFile file) {
        String content = getContentFromFile(file);
        if (content == null) {
            return null;
        }
        return getTitleByStr(content, 0);
    }

    private String getTitleByStr(String content,  int count) {
        if( count > 8 ) {
            return null;
        }
//        String title_prompt = String.format(title_template, content);
//        log.info(title_prompt);
        Map<String, Object> objectMap = new HashMap<>();
        objectMap.put("workflow_id", promptConfig.workflowIdTitle);
        Map<String, Object> paramsMap = new HashMap<>();
        paramsMap.put("input", content);
        objectMap.put("parameters", paramsMap);
        objectMap.put("userId", promptConfig.userId);
        LLMResult result = cozeClient.generate(objectMap);
        if (result.getData() != null) {
            // 判断是否为json，包含name字段
            log.info(result.getData());
            String jsonStr = result.getData().replaceAll("```json", "").replaceAll("```","").trim();
            try {
                JSONObject jsonObject = JSONObject.parseObject(jsonStr);
                // 再取一下outout
                String titleStr = jsonObject.getString("output");
                if (titleStr != null && titleStr.contains("name")) {
                    return titleStr;
                } else {
                    if (content.length() < 800) {
                        return null;
                    } else {
                        return getTitleByStr(cutContent(content), count++);
                    }
                }
            }catch (Exception e) {
                e.printStackTrace();
                if (content.length() < 800) {
                    return null;
                } else {
                    return getTitleByStr(cutContent(content), count++);
                }
            }


        }else {
            return null;
        }
    }

    // 读取文件
    private String getContentFromFile(MultipartFile file) {
//        BOMInputStream bomInputStream = null;
//        try {
//            bomInputStream = new BOMInputStream(file.getInputStream());
//
//            ByteOrderMark bom = bomInputStream.getBOM();
//            String charsetName = bom == null ? DEFAULT_ENCODING : bom.getCharsetName();
//            String str = inputStreamToString(bomInputStream, charsetName);
//            log.info(str);
//            return str;
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return null;
        String result = "";
        try {
            byte[] bytes = file.getBytes();
            String s = new String(bytes, "UTF-8");
            if (s.isEmpty()) {
                log.warn("getContentFromFile is empty");
                return null;
            }
            result = s;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    private String inputStreamToString(InputStream in, String encoding) throws IOException {

        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        int BUFFER_SIZE = 4090;
        byte[] data = new byte[BUFFER_SIZE];
        int count = -1;
        while ((count = in.read(data, 0, BUFFER_SIZE)) != -1) {
            outStream.write(data, 0, count);
        }
        return new String(outStream.toByteArray(), encoding);
    }

    // 内容裁剪
    private String cutContent(String content) {
        String[] parts = content.split("\n");
        StringBuilder stringBuilder = new StringBuilder();
        int subCount = parts.length / 3;
        for (int i = 0; i < parts.length - subCount; i++) {
            stringBuilder.append(parts[i]).append('\n');
        }
        return stringBuilder.toString();
    }

    // 提取故障树
    public String getTree(MultipartFile file) {
        String content = getContentFromFile(file);
        if (content == null) {
            return null;
        }
//        String tree_prompt = String.format(tree_template, content);
//        log.info(tree_prompt);
        Map<String, Object> objectMap = new HashMap<>();
        objectMap.put("workflow_id", promptConfig.workflowIdTree);
        Map<String, Object> paramsMap = new HashMap<>();
        paramsMap.put("input", content);
        objectMap.put("parameters", paramsMap);
        objectMap.put("userId", promptConfig.userId);
        LLMResult result = cozeClient.generate(objectMap);
        if (result.getData() != null) {
            log.info("reponse: {}", result.getData());
            String jsonStr = result.getData().replaceAll("```json", "").replaceAll("```","").trim();
            JSONObject jsonObject = JSONObject.parseObject(jsonStr);
            // 再取一下outout
            String treeStr = jsonObject.getString("output");

            if (treeStr.startsWith("[") && treeStr.endsWith("]")) {
                String resultStr = fixJson(treeStr);

                treeStr = "{" + resultStr + "}";
            }
            log.info(treeStr);
            return treeStr;
        }else {
            return null;
        }
    }

    // json有字段为空的时候的处理
    private String fixJson(String jsonStr) {
        List<Map> list = (List<Map>) JSONObject.parse(jsonStr);
        if (list != null && list.size() > 0) {
            for (int i = 0; i < list.size(); i++) {
                Map map = list.get(i);
                if (map != null) {
                    String fault_suggestion = (String) map.get("fault_suggestion");
                    String ok_suggestion = (String) map.get("ok_suggestion");
                    boolean needAsk = false;
                    if (fault_suggestion == null || ok_suggestion == null) {
                        needAsk = true;
                    } else {
                        if (fault_suggestion.isEmpty() || fault_suggestion.length() < 4) {
                            needAsk = true;
                        }
                        if (ok_suggestion.isEmpty() || ok_suggestion.equals("无")) {
                            needAsk = true;
                        }
                    }
                    if (needAsk) {
                        String question = (String) map.get("question");
                        if (question != null && !question.isEmpty()) {
                            String answerStr = getAnswer(question);
                            if (fault_suggestion == null || fault_suggestion.isEmpty() || fault_suggestion.length() < 4) {
                                map.put("fault_suggestion", answerStr);
                            }
                            if (ok_suggestion == null || ok_suggestion.isEmpty() || ok_suggestion.length() < 4) {
                                map.put("ok_suggestion", answerStr);
                            }
                        }
                    }
                }
            }
            return list.toString();
        }
        return jsonStr;
    }

    private String getAnswer(String question) {
        // String questionStr = String.format(promptConfig.getQuestion, question);
        log.info(question);
        Map<String, Object> objectMap = new HashMap<>();
        objectMap.put("workflow_id", promptConfig.workflowIdAnswer);
        Map<String, Object> paramsMap = new HashMap<>();
        paramsMap.put("input", question);
        objectMap.put("parameters", paramsMap);
        objectMap.put("userId", promptConfig.userId);
        LLMResult result = cozeClient.generate(objectMap);
        if (result.getData() != null) {
            log.info("reponse: {}", result.getData());
            String answerStr = result.getData().replaceAll("```json", "").replaceAll("```","").trim();
            JSONObject jsonObject = JSONObject.parseObject(answerStr);
            // 再取一下outout
            String outputStr = jsonObject.getString("output");
            return outputStr;
        }
        return null;
    }

}
