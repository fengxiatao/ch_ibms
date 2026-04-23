package cn.iocoder.yudao.module.iot.newgateway.util;

import com.sun.jna.Platform;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 本地库加载器
 * 
 * <p>从资源路径提取并加载大华 NetSDK 相关本地库（按平台/架构）。</p>
 * 
 * <p>使用场景：SDK 初始化前确保库文件已加载到可用路径</p>
 * <ul>
 *     <li>Windows: 使用系统临时目录</li>
 *     <li>Linux: 优先使用 /opt/dahua-sdk，如果没有权限则使用 ~/.dahua-sdk</li>
 * </ul>
 * 
 * <p>支持通过环境变量或系统属性自定义库文件路径：</p>
 * <ul>
 *     <li>系统属性: dahua.native.path</li>
 *     <li>环境变量: DAHUA_SDK_PATH</li>
 * </ul>
 * 
 * @author IoT Gateway Team
 */
@Slf4j
public class NativeLibraryLoader {

    private static final String LOG_PREFIX = "[NativeLibraryLoader]";

    /**
     * 是否已加载
     */
    private static volatile boolean loaded = false;

    /**
     * 获取本地库存放目录
     */
    private static String getNativeLibDir() {
        // 优先使用系统属性
        String override = System.getProperty("dahua.native.path");
        if (override == null || override.isEmpty()) {
            // 其次使用环境变量
            override = System.getenv("DAHUA_SDK_PATH");
        }
        if (override != null && !override.isEmpty()) {
            return override;
        }
        // 根据操作系统选择默认路径
        if (Platform.isLinux()) {
            return "/opt/dahua-sdk";
        }
        return System.getProperty("java.io.tmpdir");
    }

    /**
     * 加载大华 SDK 本地库
     * 
     * <p>按操作系统与架构顺序加载大华 SDK 本地库。</p>
     * <p>此方法是线程安全的，多次调用只会加载一次。</p>
     * 
     * @throws RuntimeException 加载失败时抛出
     */
    public static synchronized void loadDahuaLibraries() {
        if (loaded) {
            log.debug("{} 大华SDK库已加载，跳过", LOG_PREFIX);
            return;
        }

        try {
            String osArch = getOsArch();
            log.info("{} 检测到操作系统架构: {}", LOG_PREFIX, osArch);
            log.info("{} 本地库存放目录: {}", LOG_PREFIX, getNativeLibDir());

            // Windows：为 NetSDKLib 兼容准备 ./libs/{osArch} 目录（netSdk demo jar 往往硬编码从该目录加载）
            if (Platform.isWindows()) {
                prepareWindowsCompatLibDir(osArch);
            }
            
            // Linux：准备库文件目录并设置 JNA 路径
            if (Platform.isLinux()) {
                prepareLinuxLibDir(osArch);
            }

            String[] libraries = getLibraries();

            for (String library : libraries) {
                loadLibrary(osArch, library);
            }

            loaded = true;
            log.info("{} ✅ 大华SDK库加载成功", LOG_PREFIX);

        } catch (Exception e) {
            log.error("{} 加载大华SDK库失败", LOG_PREFIX, e);
            throw new RuntimeException("加载大华SDK库失败", e);
        }
    }

    /**
     * 获取需要加载的库文件列表
     */
    private static String[] getLibraries() {
        if (Platform.isLinux()) {
            // Linux 64位系统需要加载的库文件（按依赖顺序）
            return new String[]{
                "libcrypto.so",
                "libssl.so",
                "libImageAlg.so",
                "libStreamConvertor.so",
                "libavnetsdk.so",
                "libdhconfigsdk.so",
                "libdhnetsdk.so"
            };
        } else if (Platform.isWindows()) {
            // Windows 64位系统需要加载的库文件（按依赖顺序）
            return new String[]{
                "libeay32.dll",
                "ssleay32.dll",
                "Infra.dll",
                "ImageAlg.dll",
                "IvsDrawer.dll",
                "StreamConvertor.dll",
                "play.dll",
                "avnetsdk.dll",
                "dhconfigsdk.dll",
                "dhnetsdk.dll"
            };
        } else {
            throw new UnsupportedOperationException("不支持的操作系统: " + System.getProperty("os.name"));
        }
    }

    /**
     * 从资源路径提取并加载单个库文件
     * <p>
     * 加载顺序：
     * 1. 优先从外部SDK路径加载（通过系统属性或环境变量配置）
     * 2. 如果外部路径不存在，尝试从资源路径提取
     * </p>
     * 
     * @param osArch      操作系统架构标识（如 win64/linux64）
     * @param libraryName 库文件名
     * @throws Exception 提取或加载失败抛出异常
     */
    private static void loadLibrary(String osArch, String libraryName) throws Exception {
        log.debug("{} 正在加载库文件: {}", LOG_PREFIX, libraryName);

        // 1. 优先从外部SDK路径加载
        File externalFile = resolveExternalLibraryFile(osArch, libraryName);
        if (externalFile != null) {
            log.info("{} 从外部路径加载库文件: {}", LOG_PREFIX, externalFile.getAbsolutePath());
            System.load(externalFile.getAbsolutePath());
            log.debug("{} 库文件加载成功: {}", LOG_PREFIX, libraryName);
            return;
        }

        // 2. 尝试从资源路径提取
        String resourcePath = "/native/" + osArch + "/" + libraryName;
        InputStream inputStream = NativeLibraryLoader.class.getResourceAsStream(resourcePath);
        if (inputStream == null) {
            log.warn("{} 库文件不存在于资源路径: {}，且外部路径未找到（DAHUA_SDK_PATH/dahua.native.path/../libs/{}）",
                    LOG_PREFIX, resourcePath, osArch);
            return;
        }

        try {
            // 创建库文件存放目录（使用临时目录）
            String tempDir = System.getProperty("java.io.tmpdir");
            Path libDir = Paths.get(tempDir, "dahua-sdk");
            if (!libDir.toFile().exists()) {
                boolean created = libDir.toFile().mkdirs();
                if (!created) {
                    log.warn("{} 无法创建目录: {}", LOG_PREFIX, libDir);
                }
            }
            
            Path tempFile = libDir.resolve(libraryName);
            File file = tempFile.toFile();

            // 如果文件已存在且可用，直接使用
            if (file.exists() && file.canRead()) {
                log.debug("{} 使用已存在的库文件: {}", LOG_PREFIX, tempFile);
            } else {
                // 提取到目标目录
                log.info("{} 提取库文件到: {}", LOG_PREFIX, tempFile);
                try (FileOutputStream outputStream = new FileOutputStream(file)) {
                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                }
                
                // 设置可执行权限（Linux）
                if (Platform.isLinux()) {
                    file.setExecutable(true, false);
                    file.setReadable(true, false);
                }
            }

            // 加载库文件
            System.load(file.getAbsolutePath());
            log.debug("{} 库文件加载成功: {}", LOG_PREFIX, libraryName);
            
        } finally {
            inputStream.close();
        }
    }

    /**
     * 解析外部 SDK 目录下的库文件路径
     *
     * <p>优先级：</p>
     * <ol>
     *   <li>系统属性/环境变量指定的目录（{@link #NATIVE_LIB_DIR}）</li>
     *   <li>从当前工作目录向上逐级查找 <code>libs/{osArch}</code>（适配 mono-repo 结构）</li>
     * </ol>
     *
     * @return 找到则返回 File，否则返回 null
     */
    private static File resolveExternalLibraryFile(String osArch, String libraryName) {
        // 1) 显式配置路径
        File configured = new File(getNativeLibDir(), libraryName);
        if (configured.exists() && configured.canRead()) {
            return configured;
        }

        // 2) 自动回退：从 user.dir 逐级向上查找 ../libs/{osArch}/{libraryName}
        try {
            Path cur = Paths.get(System.getProperty("user.dir"));
            // 最多向上找 6 层，避免无穷遍历
            for (int i = 0; i < 6 && cur != null; i++) {
                Path candidate = cur.resolve("libs").resolve(osArch).resolve(libraryName);
                if (Files.exists(candidate) && Files.isReadable(candidate)) {
                    return candidate.toFile();
                }
                cur = cur.getParent();
            }
        } catch (Exception e) {
            log.debug("{} 解析外部库文件路径失败: library={}, err={}", LOG_PREFIX, libraryName, e.getMessage());
        }
        return null;
    }

    /**
     * Windows 下准备 NetSDK 兼容目录：{user.dir}/libs/{osArch}
     *
     * <p>很多 NetSDK demo 封装会在类初始化时尝试从 ./libs/win64 加载 dhnetsdk.dll，
     * 因此这里在启动期把仓库根目录的 libs/win64（若存在）复制到该位置，避免 Spring 启动失败。</p>
     */
    private static void prepareWindowsCompatLibDir(String osArch) {
        try {
            Path targetDir = Paths.get(System.getProperty("user.dir")).resolve("libs").resolve(osArch);
            Path targetDhnetsdk = targetDir.resolve("dhnetsdk.dll");
            if (Files.exists(targetDhnetsdk)) {
                return;
            }

            // 源目录：优先显式配置（dahua.native.path/DAHUA_SDK_PATH），否则向上查找 libs/{osArch}
            Path sourceDir = Paths.get(getNativeLibDir());
            if (!Files.exists(sourceDir.resolve("dhnetsdk.dll"))) {
                Path cur = Paths.get(System.getProperty("user.dir"));
                for (int i = 0; i < 6 && cur != null; i++) {
                    Path candidate = cur.resolve("libs").resolve(osArch);
                    if (Files.exists(candidate.resolve("dhnetsdk.dll"))) {
                        sourceDir = candidate;
                        break;
                    }
                    cur = cur.getParent();
                }
            }

            if (!Files.exists(sourceDir.resolve("dhnetsdk.dll"))) {
                log.warn("{} 未找到 Windows NetSDK DLL 源目录，跳过复制。你可以设置 DAHUA_SDK_PATH 或 dahua.native.path 指向包含 dhnetsdk.dll 的目录",
                        LOG_PREFIX);
                return;
            }

            Files.createDirectories(targetDir);
            for (String lib : getLibraries()) {
                Path src = sourceDir.resolve(lib);
                Path dst = targetDir.resolve(lib);
                if (Files.exists(dst)) {
                    continue;
                }
                if (Files.exists(src)) {
                    Files.copy(src, dst);
                }
            }

            log.info("{} 已准备 Windows NetSDK 兼容目录: {}", LOG_PREFIX, targetDir.toAbsolutePath());
        } catch (Exception e) {
            log.warn("{} 准备 Windows NetSDK 兼容目录失败: {}", LOG_PREFIX, e.getMessage());
        }
    }

    /**
     * Linux 下准备 SDK 库目录并设置 JNA 路径
     *
     * <p>JNA 在加载本地库时会从 jna.library.path 指定的目录搜索库文件。
     * 此方法会：</p>
     * <ol>
     *     <li>确定库文件目录（外部配置 > 提取到临时目录）</li>
     *     <li>从 JAR 资源提取库文件到目标目录</li>
     *     <li>设置 jna.library.path 系统属性</li>
     * </ol>
     */
    private static void prepareLinuxLibDir(String osArch) {
        try {
            // 1. 确定目标目录
            String configuredDir = getNativeLibDir();
            Path targetDir;
            boolean useConfigured = false;
            
            // 检查配置的目录是否存在 libdhnetsdk.so
            Path configuredPath = Paths.get(configuredDir);
            if (Files.exists(configuredPath.resolve("libdhnetsdk.so"))) {
                targetDir = configuredPath;
                useConfigured = true;
                log.info("{} 使用外部配置的 SDK 目录: {}", LOG_PREFIX, targetDir);
            } else {
                // 使用临时目录
                targetDir = Paths.get(System.getProperty("java.io.tmpdir"), "dahua-sdk");
                log.info("{} 外部目录无 SDK 文件，将提取到临时目录: {}", LOG_PREFIX, targetDir);
            }
            
            // 2. 如果不是使用外部配置的目录，从 JAR 提取库文件
            if (!useConfigured) {
                Files.createDirectories(targetDir);
                
                String[] libraries = getLibraries();
                for (String library : libraries) {
                    Path targetFile = targetDir.resolve(library);
                    
                    // 如果文件已存在且可读，跳过
                    if (Files.exists(targetFile) && Files.isReadable(targetFile)) {
                        log.debug("{} 库文件已存在: {}", LOG_PREFIX, targetFile);
                        continue;
                    }
                    
                    // 从资源路径提取
                    String resourcePath = "/native/" + osArch + "/" + library;
                    try (InputStream is = NativeLibraryLoader.class.getResourceAsStream(resourcePath)) {
                        if (is == null) {
                            log.warn("{} 资源中不存在库文件: {}", LOG_PREFIX, resourcePath);
                            continue;
                        }
                        
                        try (FileOutputStream fos = new FileOutputStream(targetFile.toFile())) {
                            byte[] buffer = new byte[8192];
                            int bytesRead;
                            while ((bytesRead = is.read(buffer)) != -1) {
                                fos.write(buffer, 0, bytesRead);
                            }
                        }
                        
                        // 设置可执行权限
                        targetFile.toFile().setExecutable(true, false);
                        targetFile.toFile().setReadable(true, false);
                        
                        log.info("{} 已提取库文件: {}", LOG_PREFIX, targetFile);
                    }
                }
            }
            
            // 3. 设置 jna.library.path（关键！让 JNA 能找到库文件）
            String existingPath = System.getProperty("jna.library.path");
            String newPath = targetDir.toAbsolutePath().toString();
            if (existingPath != null && !existingPath.isEmpty()) {
                newPath = newPath + File.pathSeparator + existingPath;
            }
            System.setProperty("jna.library.path", newPath);
            log.info("{} 已设置 jna.library.path: {}", LOG_PREFIX, newPath);
            
            // 同时设置 java.library.path（某些 JNA 版本也会用到）
            String javaLibPath = System.getProperty("java.library.path");
            if (javaLibPath != null && !javaLibPath.contains(targetDir.toAbsolutePath().toString())) {
                System.setProperty("java.library.path", 
                    targetDir.toAbsolutePath().toString() + File.pathSeparator + javaLibPath);
                log.info("{} 已更新 java.library.path", LOG_PREFIX);
            }
            
        } catch (Exception e) {
            log.error("{} 准备 Linux SDK 库目录失败: {}", LOG_PREFIX, e.getMessage(), e);
        }
    }

    /**
     * 获取操作系统架构标识
     * 
     * @return 架构标识字符串（linux64/win64/win32）
     * @throws UnsupportedOperationException 不支持的操作系统时抛出
     */
    private static String getOsArch() {
        if (Platform.isLinux() && Platform.is64Bit()) {
            return "linux64";
        } else if (Platform.isWindows() && Platform.is64Bit()) {
            return "win64";
        } else if (Platform.isWindows() && !Platform.is64Bit()) {
            return "win32";
        } else {
            throw new UnsupportedOperationException("不支持的操作系统: " + System.getProperty("os.name"));
        }
    }

    /**
     * 检查库是否已加载
     * 
     * @return 是否已加载
     */
    public static boolean isLoaded() {
        return loaded;
    }

    /**
     * 清理临时库文件
     * 
     * <p>当前实现为占位，JVM 退出时会自动清理临时文件。</p>
     */
    public static void cleanup() {
        // JVM退出时会自动清理临时文件，这里可以不做处理
        // 如果需要手动清理，可以在这里实现
        log.debug("{} 清理临时库文件（当前为占位实现）", LOG_PREFIX);
    }
}
