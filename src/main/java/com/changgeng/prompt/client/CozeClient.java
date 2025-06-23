package com.changgeng.prompt.client;

import com.changgeng.prompt.pojo.LLMResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name="coze", path = "/v1", url = "${coze.url}")
public interface CozeClient {
    @PostMapping(value = "/workflow/run", headers = {"Content-Type=application/json;charset=UTF-8", "Authorization=${coze.token}"})
            // consumes = MediaType.APPLICATION_JSON_VALUE)
    LLMResult generate(Object params);

}
