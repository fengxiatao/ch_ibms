package cn.iocoder.yudao.module.iot.service.video.ffmpeg;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PreDestroy;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * FFmpeg 流管理器
 *
 * 负责管理FFmpeg进程，实现RTSP流到HLS流的转码。
 * 每个视频流对应一个独立的FFmpeg进程。
 *
 * 核心功能：
 * 1. 启动FFmpeg进程进行RTSP→HLS转码
 * 2. 管理FFmpeg进程生命周期
 * 3. 监控转码进程状态
 * 4. 清理HLS文件
 *
 * @author 长辉信息科技有限公司
 */
@Component
@Slf4j
public class FFmpegStreamManager {

    @Value("${iot.video.ffmpeg-path:ffmpeg}")
    private String ffmpegPath;

    @Value("${iot.video.hls-output-dir:/var/www/hls}")
    private String hlsOutputDir;

    @Value("${iot.video.hls-segment-duration:5}")
    private int hlsSegmentDuration;

    @Value("${iot.video.hls-list-size:10}")
    private int hlsListSize;

    /**
     * 存储所有活跃的FFmpeg进程
     * Key: streamId, Value: Process
     */
    private final Map<String, Process> activeProcesses = new ConcurrentHashMap<>();

    /**
     * 存储流的输出目录
     * Key: streamId, Value: outputDirPath
     */
    private final Map<String, String> streamOutputDirs = new ConcurrentHashMap<>();

    /**
     * 启动FFmpeg转码进程
     *
     * @param streamId 流ID
     * @param rtspUrl RTSP源地址
     * @return HLS播放列表文件路径
     */
    public String startStream(String streamId, String rtspUrl) {
        log.info("[startStream] 启动FFmpeg转码: streamId={}, rtspUrl={}", streamId, rtspUrl);

        // 1. 创建输出目录
        String outputDir = createOutputDir(streamId);
        streamOutputDirs.put(streamId, outputDir);

        // 2. 构建FFmpeg命令
        List<String> command = buildFFmpegCommand(rtspUrl, outputDir);

        // 3. 启动FFmpeg进程
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.redirectErrorStream(true);
            
            Process process = processBuilder.start();
            activeProcesses.put(streamId, process);

            // 4. 启动日志读取线程
            startLogReader(streamId, process);

            log.info("[startStream] FFmpeg进程已启动: streamId={}, pid={}", 
                    streamId, process.pid());

            // 5. 返回播放列表路径
            return outputDir + "/playlist.m3u8";

        } catch (IOException e) {
            log.error("[startStream] 启动FFmpeg进程失败: streamId={}, 错误: {}", 
                    streamId, e.getMessage(), e);
            throw new RuntimeException("启动FFmpeg进程失败: " + e.getMessage());
        }
    }

    /**
     * 停止FFmpeg转码进程
     *
     * @param streamId 流ID
     */
    public void stopStream(String streamId) {
        log.info("[stopStream] 停止FFmpeg转码: streamId={}", streamId);

        Process process = activeProcesses.remove(streamId);
        if (process != null && process.isAlive()) {
            process.destroy();
            
            try {
                // 等待进程结束，最多3秒
                if (!process.waitFor(3, java.util.concurrent.TimeUnit.SECONDS)) {
                    process.destroyForcibly();
                }
                log.info("[stopStream] FFmpeg进程已停止: streamId={}", streamId);
            } catch (InterruptedException e) {
                log.warn("[stopStream] 等待进程结束被中断: streamId={}", streamId);
                Thread.currentThread().interrupt();
            }
        }

        // 清理输出目录
        cleanupOutputDir(streamId);
    }

    /**
     * 检查流是否正在运行
     *
     * @param streamId 流ID
     * @return true if running
     */
    public boolean isStreamRunning(String streamId) {
        Process process = activeProcesses.get(streamId);
        return process != null && process.isAlive();
    }

    /**
     * 获取所有活跃流的数量
     *
     * @return 活跃流数量
     */
    public int getActiveStreamCount() {
        return activeProcesses.size();
    }

    /**
     * 应用销毁时停止所有FFmpeg进程
     */
    @PreDestroy
    public void destroy() {
        log.info("[destroy] 停止所有FFmpeg进程...");
        activeProcesses.keySet().forEach(this::stopStream);
    }

    /**
     * 创建流的输出目录
     *
     * @param streamId 流ID
     * @return 输出目录路径
     */
    private String createOutputDir(String streamId) {
        Path outputPath = Paths.get(hlsOutputDir, streamId);
        try {
            Files.createDirectories(outputPath);
            log.debug("[createOutputDir] 创建输出目录: {}", outputPath);
            return outputPath.toString();
        } catch (IOException e) {
            log.error("[createOutputDir] 创建输出目录失败: {}", outputPath, e);
            throw new RuntimeException("创建输出目录失败: " + e.getMessage());
        }
    }

    /**
     * 构建FFmpeg命令
     *
     * FFmpeg HLS转码命令示例：
     * ffmpeg -rtsp_transport tcp -i rtsp://admin:pass@ip:554/path \
     *        -c:v copy -c:a aac -f hls \
     *        -hls_time 5 -hls_list_size 10 \
     *        -hls_flags delete_segments+append_list \
     *        /var/www/hls/streamId/playlist.m3u8
     *
     * @param rtspUrl RTSP源地址
     * @param outputDir 输出目录
     * @return FFmpeg命令列表
     */
    private List<String> buildFFmpegCommand(String rtspUrl, String outputDir) {
        List<String> command = new ArrayList<>();
        
        command.add(ffmpegPath);
        
        // 输入选项
        command.add("-rtsp_transport");
        command.add("tcp"); // 使用TCP传输，更稳定
        command.add("-i");
        command.add(rtspUrl);
        
        // 编码选项
        command.add("-c:v");
        command.add("copy"); // 视频流复制，不重新编码，降低CPU负载
        command.add("-c:a");
        command.add("aac"); // 音频转AAC
        
        // HLS输出选项
        command.add("-f");
        command.add("hls");
        command.add("-hls_time");
        command.add(String.valueOf(hlsSegmentDuration)); // 每个TS分片时长
        command.add("-hls_list_size");
        command.add(String.valueOf(hlsListSize)); // 播放列表保留的分片数量
        command.add("-hls_flags");
        command.add("delete_segments+append_list"); // 自动删除旧分片，追加列表
        
        // 输出文件
        command.add(outputDir + "/playlist.m3u8");
        
        log.debug("[buildFFmpegCommand] FFmpeg命令: {}", String.join(" ", command));
        
        return command;
    }

    /**
     * 启动日志读取线程
     *
     * 监控FFmpeg进程的输出日志，用于调试和监控
     *
     * @param streamId 流ID
     * @param process FFmpeg进程
     */
    private void startLogReader(String streamId, Process process) {
        Thread logThread = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                
                String line;
                while ((line = reader.readLine()) != null) {
                    log.debug("[FFmpeg][{}] {}", streamId, line);
                    
                    // 检测关键错误信息
                    if (line.contains("error") || line.contains("Error")) {
                        log.warn("[FFmpeg][{}] 检测到错误: {}", streamId, line);
                    }
                }
                
            } catch (IOException e) {
                log.error("[FFmpeg][{}] 读取日志失败: {}", streamId, e.getMessage());
            }
            
            log.info("[FFmpeg][{}] 日志读取线程结束", streamId);
        });
        
        logThread.setName("ffmpeg-log-" + streamId);
        logThread.setDaemon(true);
        logThread.start();
    }

    /**
     * 清理流的输出目录
     *
     * @param streamId 流ID
     */
    private void cleanupOutputDir(String streamId) {
        String outputDir = streamOutputDirs.remove(streamId);
        if (outputDir == null) {
            return;
        }

        try {
            Path dirPath = Paths.get(outputDir);
            if (Files.exists(dirPath)) {
                // 删除目录中的所有文件
                Files.walk(dirPath)
                        .sorted((a, b) -> b.compareTo(a)) // 反向排序，先删除文件再删除目录
                        .forEach(path -> {
                            try {
                                Files.delete(path);
                            } catch (IOException e) {
                                log.warn("[cleanupOutputDir] 删除文件失败: {}", path, e);
                            }
                        });
                
                log.info("[cleanupOutputDir] 输出目录已清理: {}", outputDir);
            }
        } catch (IOException e) {
            log.error("[cleanupOutputDir] 清理输出目录失败: {}", outputDir, e);
        }
    }
}












