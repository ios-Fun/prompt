package com.changgeng.prompt.controller;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.support.hsf.HSFJSONUtils;
import com.changgeng.prompt.service.ReadPromptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/prompt")
public class PromptController {

    @Autowired
    ReadPromptService readPromptService;

    // 提取故障树
    @RequestMapping("/tree/generate")
    public String treeGenerate(@RequestParam(value = "file") MultipartFile file) {
//        String result = "";
//        try {
//            byte[] bytes = file.getBytes();
//            String s = new String(bytes, "GBK");
//            System.out.println(s);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }

        String result = readPromptService.getTree(file);
        return result;
//        Map<String, Object> map = new HashMap<String, Object>();
//        map.put("code", 200);
//        map.put("message", "操作成功");
//        map.put("result", result);
//        map.put("success", true);
//        return map;
    }

    // 提取标题
    @PostMapping("/title/generate")
    public String titleGenerate(@RequestParam(value = "file") MultipartFile file) {
//        String result = "";
//        try {
//            byte[] bytes = file.getBytes();
//            String s = new String(bytes, "GBK");
//            System.out.println(s);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }

        String result = readPromptService.getTitle(file);
        Map parse = (Map) JSONObject.parse(result);

        return parse.get("name").toString();
//        String result = readPromptService.getTitle(file);
//        Map<String, Object> map = new HashMap<String, Object>();
//        map.put("code", 200);
//        map.put("message", "操作成功");
//        map.put("result", result);
//        map.put("success", true);
//        return map;
    }

}
