package com.river.service.impl;

import com.river.service.ServerService;
import com.river.vo.server.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.net.InetAddress;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

@Slf4j
@Service
public class ServerServiceImpl implements ServerService {

    private final DecimalFormat df = new DecimalFormat("0.00");

    @Override
    public ServerInfo getServerInfo() {
        ServerInfo info = new ServerInfo();
        try {
            info.setCpu(getCpuInfo());
            info.setMem(getMemInfo());
            info.setSys(getSysInfo());
            info.setJvm(getJvmInfo());
            info.setSysFiles(getSysFiles());
        } catch (Exception e) {
            log.error("获取服务器信息异常", e);
        }
        return info;
    }

    /**
     * 获取CPU信息
     */
    private CpuInfo getCpuInfo() {
        CpuInfo cpu = new CpuInfo();
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        
        cpu.setCpuNum(Runtime.getRuntime().availableProcessors());
        
        if (osBean instanceof com.sun.management.OperatingSystemMXBean) {
            com.sun.management.OperatingSystemMXBean sunOsBean = (com.sun.management.OperatingSystemMXBean) osBean;
            double systemCpuLoad = sunOsBean.getSystemCpuLoad();
            cpu.setUsed(Double.parseDouble(df.format(systemCpuLoad * 100)));
            cpu.setSys(Double.parseDouble(df.format(systemCpuLoad * 100)));
            cpu.setFree(Double.parseDouble(df.format((1 - systemCpuLoad) * 100)));
        }
        
        return cpu;
    }

    /**
     * 获取内存信息
     */
    private MemInfo getMemInfo() {
        MemInfo mem = new MemInfo();
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        
        long totalMemory = Runtime.getRuntime().totalMemory();
        long freeMemory = Runtime.getRuntime().freeMemory();
        long maxMemory = Runtime.getRuntime().maxMemory();
        
        mem.setTotal(maxMemory);
        mem.setUsed(totalMemory - freeMemory);
        mem.setFree(freeMemory);
        mem.setUsage(Double.parseDouble(df.format(mem.getUsed() * 100.0 / mem.getTotal())));
        
        return mem;
    }

    /**
     * 获取系统信息
     */
    private SysInfo getSysInfo() {
        Properties props = System.getProperties();
        SysInfo sys = new SysInfo();
        sys.setComputerName(getHostName());
        sys.setComputerIp(getHostIp());
        sys.setOsName(props.getProperty("os.name"));
        sys.setOsArch(props.getProperty("os.arch"));
        sys.setUserDir(props.getProperty("user.dir"));
        return sys;
    }

    /**
     * 获取JVM信息
     */
    private JvmInfo getJvmInfo() {
        Properties props = System.getProperties();
        JvmInfo jvm = new JvmInfo();
        jvm.setName(ManagementFactory.getRuntimeMXBean().getVmName());
        jvm.setVersion(props.getProperty("java.version"));
        jvm.setHome(props.getProperty("java.home"));
        jvm.setStartTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(ManagementFactory.getRuntimeMXBean().getStartTime())));
        jvm.setRunTime(formatRunTime(ManagementFactory.getRuntimeMXBean().getUptime()));
        return jvm;
    }

    /**
     * 获取磁盘信息
     */
    private List<SysFile> getSysFiles() {
        List<SysFile> sysFiles = new ArrayList<>();
        for (java.io.File root : java.io.File.listRoots()) {
            SysFile sysFile = new SysFile();
            sysFile.setDirName(root.getPath());
            sysFile.setTypeName(root.getPath());
            sysFile.setTotal(root.getTotalSpace());
            sysFile.setFree(root.getFreeSpace());
            sysFile.setUsed(root.getTotalSpace() - root.getFreeSpace());
            sysFile.setUsage(Double.parseDouble(df.format(sysFile.getUsed() * 100.0 / sysFile.getTotal())));
            sysFiles.add(sysFile);
        }
        return sysFiles;
    }

    private String getHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            return "未知";
        }
    }

    private String getHostIp() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            return "未知";
        }
    }

    private String formatRunTime(long ms) {
        long days = ms / (1000 * 60 * 60 * 24);
        long hours = (ms % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60);
        long minutes = (ms % (1000 * 60 * 60)) / (1000 * 60);
        long seconds = (ms % (1000 * 60)) / 1000;
        return String.format("%d天%d小时%d分%d秒", days, hours, minutes, seconds);
    }
} 