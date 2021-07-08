package com.demo.controller;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentStyle;

import com.demo.annotation.Limit;
import com.demo.aspect.LimitType;
import com.demo.utils.FileUtil;
import com.demo.utils.StringUtils;
import lombok.Data;
import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.springframework.core.io.buffer.DefaultDataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@Controller
@Slf4j
public class IndexController {
    @GetMapping(value = "/")
    @Limit(period = 60, count = 10, name = "testLimit", prefix = "limit", limitType= LimitType.IP)
    public Mono<Void> IndexAction(ServerHttpRequest request, ServerHttpResponse response) {
        log.info("xxxx！！！");
        log.debug("dddddd");
        return FileUtil.writeHtml(response, StringUtils.getIp(request));
    }

    @GetMapping("/download")
    public Mono<Void> downloadAction(ServerHttpResponse response) {
        List<DemoData> dataList = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            DemoData data = new DemoData();
            data.setUsername("tr1" + i);
            data.setPassword("tr2" + i);
            dataList.add(data);
        }

        HttpHeaders headers = response.getHeaders();
        headers.setContentType(MediaType.valueOf("application/vnd.ms-excel"));
        headers.setContentDisposition(ContentDisposition.parse("attachment;filename=xxxx.xlsx"));
        DefaultDataBuffer dataBuffer = new DefaultDataBufferFactory().allocateBuffer();
        EasyExcel.write(dataBuffer.asOutputStream(), DemoData.class)
                .autoCloseStream(Boolean.TRUE).sheet("模板")
                .doWrite(dataList);
//        Flux<DataBuffer> data = Flux.create(emitter -> {
//            emitter.next(dataBuffer);
//            emitter.complete();
//        });
        return response.writeAndFlushWith(Mono.just(Mono.just(dataBuffer)));
    }

    @Data
    private static class DemoData {
        @ColumnWidth(50)  // 定义宽度
        @ExcelProperty("用户名") // 定义列名称
        @ContentStyle(fillPatternType = FillPatternType.SOLID_FOREGROUND, fillForegroundColor = 40)
        private String username;
        @ExcelProperty("密码")
        private String password;
    }
}
