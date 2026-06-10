package cn.iocoder.yudao.module.iot.newgateway.util;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Platform;
import com.sun.jna.WString;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * 本地库加载器
 *
 * <p>按平台从外置目录加载大华 NetSDK 原生库。默认目录：</p>
 * <ul>
 *     <li>Windows: C:\works\ch\ruoyi-vue-pro\libs\win64</li>
 *     <li>Linux: /opt/dahua-sdk</li>
 * </ul>
 *
 * <p>支持通过系统属性或环境变量覆盖库目录：</p>
 * <ul>
 *     <li>系统属性: dahua.native.path</li>
 *     <li>环境变量: DAHUA_SDK_PATH</li>
 * </ul>
 *
 * <p>注意：Linux 部署时仍建议在启动脚本中设置 LD_LIBRARY_PATH=/opt/dahua-sdk，
 * 以便系统动态链接器解析 SDK 内部依赖。</p>
 *
 * @author IoT Gateway Team
 */
@Slf4j
public class NativeLibraryLoader {

    private static final String LOG_PREFIX = "[NativeLibraryLoader]";
    private static final String WINDOWS_DEFAULT_LIB_DIR = "C:\\works\\ch\\ruoyi-vue-pro\\yudao-module-iot\\yudao-module-iot-newgateway\\libs\\win64";
    private static final String LINUX_DEFAULT_LIB_DIR = "/opt/dahua-sdk";

    /**
     * 是否已加载
     */
    private static volatile boolean loaded = false;

    /**
     * Windows DLL 搜索路径设置接口。
     */
    private interface Kernel32 extends Library {
        Kernel32 INSTANCE = Native.load("kernel32", Kernel32.class);

        boolean SetDllDirectoryW(WString lpPathName);
    }

    /**
     * 获取本地库存放目录
     */
    private static Path getNativeLibDir() {
        String override = System.getProperty("dahua.native.path");
        if (override == null || override.isBlank()) {
            override = System.getenv("DAHUA_SDK_PATH");
        }
        if (override != null && !override.isBlank()) {
            return Paths.get(override).toAbsolutePath().normalize();
        }
        if (Platform.isLinux()) {
            return Paths.get(LINUX_DEFAULT_LIB_DIR);
        }
        if (Platform.isWindows()) {
            return Paths.get(WINDOWS_DEFAULT_LIB_DIR);
        }
        throw new UnsupportedOperationException("不支持的操作系统: " + System.getProperty("os.name"));
    }

    /**
     * 加载大华 SDK 本地库
     *
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
            Path nativeLibDir = getNativeLibDir();
            log.info("{} 检测到操作系统架构: {}", LOG_PREFIX, osArch);
            log.info("{} 本地库存放目录: {}", LOG_PREFIX, nativeLibDir);

            validateNativeLibDir(nativeLibDir);

            if (Platform.isWindows()) {
                prepareWindowsDllSearchPath(nativeLibDir);
            } else if (Platform.isLinux()) {
                prepareLinuxLibDir(nativeLibDir);
            }

            for (String library : getRequiredLibraries()) {
                loadLibrary(nativeLibDir, library);
            }
            loadOptionalLibraries(nativeLibDir);

            loaded = true;
            log.info("{} ✅ 大华SDK库加载成功", LOG_PREFIX);
        } catch (Exception | LinkageError e) {
            log.error("{} 加载大华SDK库失败", LOG_PREFIX, e);
            throw new RuntimeException("加载大华SDK库失败", e);
        }
    }

    /**
     * 获取必须加载的核心库文件列表。
     */
    private static String[] getRequiredLibraries() {
        if (Platform.isLinux()) {
            return new String[]{
                    "libcrypto.so",
                    "libssl.so",
                    "libavnetsdk.so",
                    "libdhconfigsdk.so",
                    "libdhnetsdk.so"
            };
        } else if (Platform.isWindows()) {
            return new String[]{
                    "libeay32.dll",
                    "ssleay32.dll",
                    "avnetsdk.dll",
                    "dhconfigsdk.dll",
                    "dhnetsdk.dll"
            };
        }
        throw new UnsupportedOperationException("不支持的操作系统: " + System.getProperty("os.name"));
    }

    /**
     * 获取可选播放/渲染库文件列表。
     */
    private static String[] getOptionalLibraries() {
        if (Platform.isLinux()) {
            return new String[]{
                    "libImageAlg.so",
                    "libStreamConvertor.so"
            };
        } else if (Platform.isWindows()) {
            return new String[]{
                    "Infra.dll",
                    "ImageAlg.dll",
                    "StreamConvertor.dll",
                    "play.dll",
                    "IvsDrawer.dll"
            };
        }
        throw new UnsupportedOperationException("不支持的操作系统: " + System.getProperty("os.name"));
    }

    /**
     * 校验 SDK 外置目录和库文件完整性。
     */
    private static void validateNativeLibDir(Path nativeLibDir) {
        if (!Files.isDirectory(nativeLibDir)) {
            throw new IllegalStateException("大华SDK目录不存在或不可访问: " + nativeLibDir
                    + "。Windows默认目录为 " + WINDOWS_DEFAULT_LIB_DIR
                    + "；Linux默认目录为 " + LINUX_DEFAULT_LIB_DIR
                    + "；也可通过 dahua.native.path 或 DAHUA_SDK_PATH 覆盖");
        }

        List<String> missingLibraries = new ArrayList<>();
        for (String library : getRequiredLibraries()) {
            Path libraryPath = nativeLibDir.resolve(library);
            if (!Files.isRegularFile(libraryPath) || !Files.isReadable(libraryPath)) {
                missingLibraries.add(library);
            }
        }
        if (!missingLibraries.isEmpty()) {
            throw new IllegalStateException("大华SDK目录缺少库文件: " + missingLibraries + "，目录: " + nativeLibDir);
        }
    }

    /**
     * 加载可选播放/渲染库。缺失或依赖不完整不应阻断 NVR 登录与录像查询。
     */
    private static void loadOptionalLibraries(Path nativeLibDir) {
        for (String library : getOptionalLibraries()) {
            Path libraryPath = nativeLibDir.resolve(library);
            if (!Files.isRegularFile(libraryPath) || !Files.isReadable(libraryPath)) {
                log.debug("{} 可选库不存在，跳过: {}", LOG_PREFIX, libraryPath);
                continue;
            }
            try {
                loadLibrary(nativeLibDir, library);
            } catch (Exception | LinkageError e) {
                log.warn("{} 可选库加载失败，已跳过: {}，err={}", LOG_PREFIX, library, e.getMessage());
            }
        }
    }

    /**
     * 加载单个库文件。
     */
    private static void loadLibrary(Path nativeLibDir, String libraryName) {
        Path libraryPath = nativeLibDir.resolve(libraryName).toAbsolutePath().normalize();
        log.info("{} 从外部路径加载库文件: {}", LOG_PREFIX, libraryPath);
        System.load(libraryPath.toString());
        log.debug("{} 库文件加载成功: {}", LOG_PREFIX, libraryName);
    }

    /**
     * Windows 下设置 DLL 搜索目录，解决 System.load 主 DLL 时依赖 DLL 找不到的问题。
     */
    private static void prepareWindowsDllSearchPath(Path nativeLibDir) {
        String dllDir = nativeLibDir.toAbsolutePath().toString();
        try {
            boolean success = Kernel32.INSTANCE.SetDllDirectoryW(new WString(dllDir));
            if (!success) {
                log.warn("{} 设置 Windows DLL 搜索目录失败，请在启动前将目录加入 PATH: {}", LOG_PREFIX, dllDir);
            } else {
                log.info("{} 已设置 Windows DLL 搜索目录: {}", LOG_PREFIX, dllDir);
            }
        } catch (Exception | LinkageError e) {
            log.warn("{} 设置 Windows DLL 搜索目录异常，请在启动前将目录加入 PATH: {}，err={}",
                    LOG_PREFIX, dllDir, e.getMessage());
        }
    }

    /**
     * Linux 下设置 JNA 路径，并提示启动脚本设置 LD_LIBRARY_PATH。
     */
    private static void prepareLinuxLibDir(Path nativeLibDir) {
        String sdkPath = nativeLibDir.toAbsolutePath().toString();
        prependSystemPropertyPath("jna.library.path", sdkPath);
        prependSystemPropertyPath("java.library.path", sdkPath);

        String ldLibraryPath = System.getenv("LD_LIBRARY_PATH");
        if (ldLibraryPath == null || !containsPath(ldLibraryPath, sdkPath)) {
            log.warn("{} 当前 LD_LIBRARY_PATH 未包含 {}。若后续出现 Can't find dependent libraries / cannot open shared object file，请在启动脚本中设置: export LD_LIBRARY_PATH={}:$LD_LIBRARY_PATH",
                    LOG_PREFIX, sdkPath, sdkPath);
        }
        log.info("{} 已设置 Linux SDK 搜索路径: {}", LOG_PREFIX, sdkPath);
    }

    private static void prependSystemPropertyPath(String propertyName, String path) {
        String existingPath = System.getProperty(propertyName);
        if (existingPath == null || existingPath.isBlank()) {
            System.setProperty(propertyName, path);
            return;
        }
        if (!containsPath(existingPath, path)) {
            System.setProperty(propertyName, path + File.pathSeparator + existingPath);
        }
    }

    private static boolean containsPath(String paths, String path) {
        if (paths == null || paths.isBlank()) {
            return false;
        }
        for (String item : paths.split(File.pathSeparator)) {
            if (Paths.get(item).toAbsolutePath().normalize().toString()
                    .equals(Paths.get(path).toAbsolutePath().normalize().toString())) {
                return true;
            }
        }
        return false;
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
     */
    public static void cleanup() {
        log.debug("{} 原生库使用外置目录加载，无需清理临时文件", LOG_PREFIX);
    }
}