package com.changgeng.prompt.config;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

@Component
public class PromptConfig {

    @Value("${prompt.model}")
    public String model;

    @Value("${prompt.title}")
    public String titlePromptPath;

    // 开关内容直接开始就1/3 截取，方便提取
//    @Value("${prompt.title}")
//    public Boolean istitleFull;

    @Value("${prompt.tree}")
    public String treePromptPath;

    @Value("${prompt.getQuestion}")
    public String getQuestion;

    @Value("${coze.workflow_id_title}")
    public String workflowIdTitle;

    @Value("${coze.workflow_id_tree}")
    public String workflowIdTree;

    @Value("${coze.token}")
    public String token;

    @Value("${coze.userId}")
    public String userId;
}
