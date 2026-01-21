package cn.iocoder.yudao.module.infra.framework.file.core.client.sftp;

import cn.hutool.core.io.FileUtil;
import cn.hutool.extra.ssh.Sftp;
import cn.iocoder.yudao.framework.common.util.io.FileUtils;
import cn.iocoder.yudao.module.infra.framework.file.core.client.AbstractFileClient;

import java.io.File;

/**
 * Sftp 文件客户端
 *
 * @author 长辉信息科技有限公司
 */
public class SftpFileClient extends AbstractFileClient<SftpFileClientConfig> {

    private Sftp sftp;

    public SftpFileClient(Long id, SftpFileClientConfig config) {
        super(id, config);
    }

    @Override
    protected void doInit() {
        // 初始化 Ftp 对象
        this.sftp = new Sftp(config.getHost(), config.getPort(), config.getUsername(), config.getPassword());
    }

    @Override
    public String upload(byte[] content, String path, String type) {
        // 执行写入
        String filePath = getFilePath(path);
        File file = FileUtils.createTempFile(content);
        reconnectIfTimeout();
        
        // 手动解析父目录路径（不使用 FileUtil.getParent，避免 Windows 系统加盘符）
        // 例如：/home/admin/20251106/xxx.svg → /home/admin/20251106
        String parentPath = getParentPath(filePath);
        if (parentPath != null && !parentPath.isEmpty()) {
            sftp.mkDirs(parentPath); // 需要创建父目录，不然会报错
        }
        
        sftp.upload(filePath, file);
        // 拼接返回路径（SFTP 直接拼接域名和路径，不需要通过 API 代理）
        return formatFileUrl(config.getDomain(), path);
    }

    @Override
    public void delete(String path) {
        String filePath = getFilePath(path);
        reconnectIfTimeout();
        sftp.delFile(filePath);
    }

    @Override
    public byte[] getContent(String path) {
        String filePath = getFilePath(path);
        File destFile = FileUtils.createTempFile();
        reconnectIfTimeout();
        sftp.download(filePath, destFile);
        return FileUtil.readBytes(destFile);
    }

    /**
     * 重写文件 URL 格式化方法
     * <p>
     * SFTP 存储的文件通过 Nginx 等静态文件服务器直接访问，
     * 不需要通过 /admin-api/infra/file/{id}/get/ 这样的 API 代理
     * 
     * @param domain 配置的域名，如 http://ibms.gzchanghui.cn/images
     * @param path 文件路径，如 20251106/考勤机_xxx.svg
     * @return 完整的文件访问 URL，如 http://ibms.gzchanghui.cn/images/20251106/考勤机_xxx.svg
     */
    @Override
    protected String formatFileUrl(String domain, String path) {
        // 统一路径分隔符为正斜杠
        String normalizedPath = path.replace("\\", "/");
        
        // 确保 domain 以斜杠结尾，path 不以斜杠开头
        String domainWithSlash = domain.endsWith("/") ? domain : domain + "/";
        String pathWithoutSlash = normalizedPath.startsWith("/") ? normalizedPath.substring(1) : normalizedPath;
        
        return domainWithSlash + pathWithoutSlash;
    }

    private String getFilePath(String path) {
        // 使用 Unix 风格的路径分隔符 /，因为 SFTP 服务器通常是 Linux
        // 避免 Windows 系统上使用 File.separator (反斜杠) 导致路径错误
        // 同时将 path 中可能存在的反斜杠统一替换为正斜杠
        String normalizedPath = path.replace("\\", "/");
        return config.getBasePath() + "/" + normalizedPath;
    }

    /**
     * 获取父目录路径（纯字符串解析，不依赖 FileUtil）
     * <p>
     * 为什么不用 FileUtil.getParent？
     * 因为在 Windows 系统上，FileUtil 会将 /home/admin/xxx 识别为相对路径，
     * 自动加上 Windows 盘符（如 F:\home\admin\xxx），导致 SFTP 路径错误
     * 
     * @param filePath 完整文件路径，如 /home/admin/20251106/xxx.svg
     * @return 父目录路径，如 /home/admin/20251106；如果没有父目录则返回 null
     */
    private String getParentPath(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return null;
        }
        
        // 统一为正斜杠
        filePath = filePath.replace("\\", "/");
        
        // 找到最后一个斜杠的位置
        int lastSlashIndex = filePath.lastIndexOf('/');
        
        if (lastSlashIndex <= 0) {
            // 没有父目录，或者是根目录
            return null;
        }
        
        // 返回父目录路径
        return filePath.substring(0, lastSlashIndex);
    }

    private synchronized void reconnectIfTimeout() {
        sftp.reconnectIfTimeout();
    }

}
