package com.citrus.knowledge.config;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

/**
 * Python RAG 服务进程管理器
 * <p>
 * Spring Boot 启动时自动拉起 Python RAG 服务（如果尚未运行），
 * Spring Boot 关闭时自动终止 Python 进程。
 * <p>
 * 可通过 {@code rag.auto-start=false} 关闭此功能。
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "rag", name = "auto-start", havingValue = "true", matchIfMissing = true)
public class RagProcessManager {

    private final RagProperties properties;
    private Process pythonProcess;
    private volatile boolean startedByUs = false;

    public RagProcessManager(RagProperties properties) {
        this.properties = properties;
    }

    @PostConstruct
    public void startPythonService() {
        if (isPythonRunning()) {
            log.info("Python RAG service already running at {}", properties.getBaseUrl());
            return;
        }

        log.info("Python RAG service not detected. Attempting to auto-start ...");

        try {
            // 找到 navel-orange-rag 目录
            Path ragDir = findRagDirectory();
            if (ragDir == null) {
                log.warn("navel-orange-rag directory not found. Auto-start skipped. " +
                        "Please configure 'rag.rag-dir' in application-dev.yml or start the Python service manually.");
                return;
            }

            log.info("Starting Python RAG service from: {}", ragDir);

            // 构建启动命令 - 直接使用虚拟环境的 Python（不依赖系统 PATH）
            String[] cmd;
            String os = System.getProperty("os.name").toLowerCase();
            Path venvPython = ragDir.resolve(".venv").resolve("Scripts").resolve("python.exe");
            Path venvUvicorn = ragDir.resolve(".venv").resolve("Scripts").resolve("uvicorn.exe");

            if (os.contains("win") && venvPython.toFile().exists()) {
                // Windows: 用 .venv 的 Python 绝对路径启动
                cmd = new String[]{
                        "cmd.exe", "/c", "start", "/B",
                        venvPython.toAbsolutePath().toString(),
                        "-m", "uvicorn", "app.main:app",
                        "--host", "127.0.0.1",
                        "--port", String.valueOf(getPort())
                };
            } else {
                // 其他环境直接用 uv run
                cmd = new String[]{
                        "uv", "run", "uvicorn", "app.main:app",
                        "--host", "127.0.0.1",
                        "--port", String.valueOf(getPort())
                };
            }

            ProcessBuilder pb = new ProcessBuilder(cmd);
            pb.directory(ragDir.toFile());
            pb.redirectErrorStream(true);

            // 继承环境变量（让子进程读到 .env 中的 DEEPSEEK_API_KEY）
            pb.inheritIO();

            pythonProcess = pb.start();
            startedByUs = true;

            // 异步等待服务就绪，不阻塞 Spring Boot 主线程
            Thread waitThread = new Thread(() -> {
                try {
                    log.info("Waiting for Python RAG service to be ready (async, up to 120s)...");
                    waitForPythonReady(120);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }, "rag-waiter");
            waitThread.setDaemon(true);
            waitThread.start();

        } catch (Exception e) {
            log.error("Failed to auto-start Python RAG service: {}", e.getMessage());
            pythonProcess = null;
            startedByUs = false;
        }
    }

    @PreDestroy
    public void stopPythonService() {
        if (pythonProcess != null && pythonProcess.isAlive() && startedByUs) {
            log.info("Shutting down Python RAG service (pid={}) ...", pythonProcess.pid());
            pythonProcess.destroy();
            try {
                boolean stopped = pythonProcess.waitFor(10, TimeUnit.SECONDS);
                if (!stopped) {
                    pythonProcess.destroyForcibly();
                }
            } catch (InterruptedException e) {
                pythonProcess.destroyForcibly();
                Thread.currentThread().interrupt();
            }
            log.info("Python RAG service stopped.");
        }
    }

    // ── 内部 ──────────────────────────────────────────────

    private boolean isPythonRunning() {
        try {
            String url = properties.getBaseUrl() + "/health";
            HttpURLConnection conn = (HttpURLConnection) URI.create(url).toURL().openConnection();
            conn.setConnectTimeout(2000);
            conn.setReadTimeout(2000);
            conn.setRequestMethod("GET");
            int code = conn.getResponseCode();
            conn.disconnect();
            return code == 200;
        } catch (Exception e) {
            return false;
        }
    }

    private void waitForPythonReady(int maxSeconds) throws InterruptedException {
        log.info("Waiting for Python RAG service to be ready ...");
        for (int i = 0; i < maxSeconds; i++) {
            if (isPythonRunning()) {
                log.info("Python RAG service is ready!");
                return;
            }
            Thread.sleep(1000);
        }
        log.warn("Python RAG service did not become ready within {} seconds. " +
                "Check the Python process logs.", maxSeconds);
    }

    /** 检查 Python 是否在 PATH 中 */
    private boolean checkPythonAvailable() {
        try {
            Process p = new ProcessBuilder("python", "--version")
                    .redirectErrorStream(true)
                    .start();
            int exit = p.waitFor(5, TimeUnit.SECONDS) ? p.exitValue() : -1;
            return exit == 0;
        } catch (Exception e) {
            return false;
        }
    }

    private Path findRagDirectory() {
        // 0. 优先用配置的 rag.rag-dir
        String configuredDir = properties.getRagDir();
        if (configuredDir != null && !configuredDir.isBlank()) {
            Path path = Paths.get(configuredDir);
            log.debug("Trying configured rag.rag-dir: {}", path);
            if (path.resolve("app").resolve("main.py").toFile().exists()) {
                log.info("Found rag dir via rag.rag-dir config: {}", path);
                return path;
            }
            log.warn("Configured rag.rag-dir '{}' does not contain app/main.py", configuredDir);
        }

        // 1. 从系统环境变量 NAVEL_ORANGE_RAG_DIR 找
        String envDir = System.getenv("NAVEL_ORANGE_RAG_DIR");
        if (envDir != null) {
            Path path = Paths.get(envDir);
            if (path.resolve("app").resolve("main.py").toFile().exists()) {
                log.info("Found rag dir via env: {}", path);
                return path;
            }
        }

        // 2. 从当前工作目录找
        String cwdStr = System.getProperty("user.dir");
        log.debug("RagProcessManager: user.dir = {}", cwdStr);
        Path cwd = Paths.get(cwdStr);
        Path candidate = cwd.resolve("navel-orange-rag");
        log.debug("Trying: {}", candidate);
        if (candidate.resolve("app").resolve("main.py").toFile().exists()) {
            return candidate;
        }

        // 3. 从父目录找（mvn spring-boot:run 在 navel-orange-server/ 下执行时）
        try {
            candidate = cwd.resolve("..").resolve("navel-orange-rag").normalize();
            log.debug("Trying: {}", candidate);
            if (candidate.resolve("app").resolve("main.py").toFile().exists()) {
                return candidate;
            }
        } catch (Exception ignored) {}

        // 4. 从项目根目录找（基于 pom.xml 位置推断）
        Path root = findProjectRoot(cwd);
        if (root != null) {
            candidate = root.resolve("navel-orange-rag");
            log.debug("Trying (project root): {}", candidate);
            if (candidate.resolve("app").resolve("main.py").toFile().exists()) {
                return candidate;
            }
        }

        // 5. jar 部署时，rag 目录在 jar 旁边
        try {
            Path jarDir = Paths.get(
                    RagProcessManager.class.getProtectionDomain().getCodeSource().getLocation().toURI()
            ).getParent();
            if (jarDir != null) {
                candidate = jarDir.resolve("navel-orange-rag");
                log.debug("Trying (jar dir): {}", candidate);
                if (candidate.resolve("app").resolve("main.py").toFile().exists()) {
                    return candidate;
                }
            }
        } catch (Exception ignored) {}

        log.warn("navel-orange-rag directory not found after checking 6 strategies. " +
                "Set 'rag.rag-dir' in application-dev.yml or set env NAVEL_ORANGE_RAG_DIR.");
        return null;
    }

    /** 根据 pom.xml 位置向上找到项目根目录 */
    private Path findProjectRoot(Path start) {
        Path current = start.toAbsolutePath().normalize();
        for (int i = 0; i < 5; i++) {
            if (current.resolve("pom.xml").toFile().exists() &&
                    current.resolve("navel-orange-server").toFile().isDirectory()) {
                return current;
            }
            Path parent = current.getParent();
            if (parent == null) break;
            current = parent;
        }
        return null;
    }

    private int getPort() {
        try {
            String url = properties.getBaseUrl();
            int portStart = url.lastIndexOf(":");
            int portEnd = url.indexOf("/", portStart);
            if (portEnd == -1) portEnd = url.length();
            return Integer.parseInt(url.substring(portStart + 1, portEnd));
        } catch (Exception e) {
            return 8001;
        }
    }
}
