package com.jokeep.framework.controller;

import com.jokeep.common.IdBuilder;
import com.jokeep.utils.PathKit;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
@RequestMapping("/file")
public class FileController extends BaseController {

    @ApiOperation(value="文件上传",notes="支持多文件上传，参数multipartFile")
    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    public List<Map> upload(@RequestParam("multipartFile") MultipartFile multipartFile) throws IOException {
        List<MultipartFile> files=this.multipartFiles();
        String rootlPath= PathKit.getAppPath();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        List<Map> result=new ArrayList<>();
        for (MultipartFile file :files){
            Map map=new HashMap();
            String fileAllName = file.getOriginalFilename();
            String extensionName = fileAllName.substring(fileAllName.lastIndexOf(".") + 1);
            String fileName = fileAllName.substring(0, fileAllName.lastIndexOf("."));
            String newFileName = IdBuilder.getUUID() + "." + extensionName;
            String filePath="/uploadFiles/"+sdf.format(new Date())+"/" + newFileName;
            String uploadPath =rootlPath + filePath;
            File saveFile=new File(uploadPath);
            File parentFile=saveFile.getParentFile();
            if(!parentFile.exists())
                parentFile.mkdirs();
            try(OutputStream out = new FileOutputStream(saveFile)) {
                out.write(file.getBytes());
            }
            map.put("FileAllName", fileAllName);
            map.put("FileUrl", filePath);
            map.put("NewFileName", newFileName);
            map.put("ExtensionName", extensionName);
            map.put("FileName", fileName);
            result.add(map);
        }
        return result;
    }

    @ApiOperation(value="文件下载",notes="sourcePath文件相对地址；targetName下载文件名")
    @RequestMapping(value = "/download", method = RequestMethod.GET)
    public void downloadWarFile(HttpServletRequest request,HttpServletResponse response, @RequestParam String sourcePath, @RequestParam String targetName) throws IOException {
        String filePath = PathKit.getAppPath() + "/uploadFiles/";
        String fileType = sourcePath.substring(sourcePath.lastIndexOf("."));
        File file = new File(filePath + "/" + sourcePath);
        String mimeType = request.getServletContext().getMimeType(targetName);
        response.setContentType(mimeType);
        response.setHeader("Content-Disposition", "attachment;filename*=UTF-8''" + URLEncoder.encode(targetName,"UTF-8"));
        response.setCharacterEncoding("UTF-8");
        byte[] buff = new byte[1024];
        try(BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));OutputStream os = response.getOutputStream())
        {
            int i = bis.read(buff);
            while (i != -1) {
                os.write(buff, 0, buff.length);
                os.flush();
                i = bis.read(buff);
            }
        }
    }
}
