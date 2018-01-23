package net.ys.controller;

import net.ys.service.FileService;
import org.apache.http.HttpEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;

@Controller
@RequestMapping(value = "file")
public class FileController {

    @RequestMapping(value = "upload", method = RequestMethod.POST)
    public ModelAndView upload(@RequestParam(required = true) MultipartFile file) {
        ModelAndView modelAndView = new ModelAndView("failed");
        try {
            long start = System.currentTimeMillis();
            String fileName = file.getOriginalFilename();
            String storeName = System.currentTimeMillis() + fileName.substring(fileName.lastIndexOf("."));
            boolean flag = FileService.upload(file.getInputStream(), storeName);
            if (flag) {
                long userTime = System.currentTimeMillis() - start;
                modelAndView.setViewName("success");
                modelAndView.addObject("use_time", userTime);
                modelAndView.addObject("gen_file_name", storeName);
            }
        } catch (Exception e) {
        }
        return modelAndView;
    }

    @RequestMapping("download")
    public void download(HttpServletResponse response, String fileName) throws IOException {
        HttpEntity entity = FileService.download(fileName);
        if (entity == null) {
            return;
        }

        response.setCharacterEncoding("utf-8");
        response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
        InputStream is = entity.getContent();
        ServletOutputStream out = response.getOutputStream();
        byte[] bytes = new byte[1024];
        int len;
        while ((len = is.read(bytes)) > 0) {
            out.write(bytes, 0, len);
            out.flush();
        }
        out.close();
        entity.consumeContent();
    }
}
