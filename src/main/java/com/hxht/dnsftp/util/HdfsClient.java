package com.hxht.dnsftp.util;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URI;

/**
 * you yong
 * <p>
 * HDFS客户端工具类。
 * 创建日期 2019年3月19日
 *
 * @author chen_hao
 * @version 1.0
 */
public class HdfsClient {

    private static Logger logger = LoggerFactory.getLogger(HdfsClient.class);

    /**
     * 创建文件夹
     *
     * @param hdfsUrl    hdfs集群地址
     * @param folderPath 文件夹路径
     * @return false or true
     */
    public static Boolean mkdirFolder(String hdfsUrl, String folderPath) {

        Path path = new Path(folderPath);
        boolean flag = false;
        FileSystem fs = getFileSystem(hdfsUrl, true);
        try {
            if (!fs.exists(path)) {
                flag = fs.mkdirs(path);
            }
        } catch (IOException e) {
            System.out.println(e);
            logger.error("创建文件夹异常！{}", e);
        }
        return flag;
    }

    /**
     * 获取HDFS文件系统对象
     *
     * @param hdfsUrl 地址
     * @param isOne   是否单节点
     * @return FileSystem
     */
    private static FileSystem getFileSystem(String hdfsUrl, boolean isOne) {

        Configuration conf = new Configuration();
        if (isOne) {
            conf.set("dfs.client.block.write.replace-datanode-on-failure.policy", "NEVER");
            conf.set("dfs.client.block.write.replace-datanode-on-failure.enable", "true");
        }

        conf.set("fs.hdfs.impl", "org.apache.hadoop.hdfs.DistributedFileSystem");
        FileSystem fs = null;
        while (fs == null) {
            try {
                try {
                    fs = FileSystem.get(URI.create(hdfsUrl), conf, "root");
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            } catch (IOException e) {

                System.out.println(e);
                logger.error("hdfs连接异常！{}进行重连...", e);
                continue;
            }

        }
        return fs;
    }

    /**
     * 创建一个文件
     *
     * @param hdfsUrl    地址
     * @param folderPath 文件夹路径
     * @param fileName   文件名称
     */
    public static void createFile(String hdfsUrl, String folderPath, String fileName) {

        FileSystem fs = getFileSystem(hdfsUrl, false);
        Path path = new Path(folderPath + "/" + fileName);

        try {
            if (!fs.exists(path)) {
                fs.create(path).close();
                // 创建
                logger.info(path + " 已创建！");
            } else {
                logger.info(path + " 已存在！");
            }
        } catch (IOException e) {
            logger.error("创建文件异常！");
        }

    }

    /**
     * 向文件中追加一条数据
     *
     * @param hdfsUrl    地址
     * @param folderPath 文件夹路径
     * @param fileName   文件名称
     * @param data       将要追加的数据
     * @param isOne      是否是单节点
     */
    public static void append(String hdfsUrl, String folderPath, String fileName, String data, boolean isOne) {

        FileSystem fs = getFileSystem(hdfsUrl, isOne);
        InputStream is = new BufferedInputStream(new ByteArrayInputStream(data.getBytes()));
        // 对文件夹和文件进行判断和创建
        mkdirFolder(hdfsUrl, folderPath);
        createFile(hdfsUrl, folderPath, fileName);
        Path file = new Path(folderPath + "/" + fileName);
        FSDataOutputStream out;
        try {
            out = fs.append(file);
            IOUtils.copyBytes(is, out, 4096, true);
        } catch (IOException e) {
            logger.error("追加文件异常！{}", e);
        }
        logger.debug(file + " 已append！");

    }

    /**
     * 重命名文件
     *
     * @param hdfsUrl     地址
     * @param folderPath  文件夹路径
     * @param fileName    文件名称
     * @param newFileName 新文件名称
     * @return
     */
    public static boolean renameFile(String hdfsUrl, String folderPath, String fileName, String newFileName) {

        FileSystem fs = getFileSystem(hdfsUrl, false);
        Path src = new Path(folderPath + "/" + fileName);
        Path dst = new Path(folderPath + "/" + newFileName);

        boolean flag = false;
        try {
            flag = fs.rename(src, dst);
        } catch (IOException e) {
            logger.error("重命名文件异常{}！", e);
        }

        return flag;
    }

    /**
     * @param hdfsUrl    地址
     * @param folderPath 文件所在的目录
     * @param fileName   文件名称
     * @return
     */
    public static boolean deleteFile(String hdfsUrl, String folderPath, String day, String fileName) {
        FileSystem fs = getFileSystem(hdfsUrl, false);
        String path = folderPath + day + File.separator + fileName;
        Path src = new Path(path);
        boolean flag = false;
        try {
            flag = fs.delete(src, false);
        } catch (IOException e) {
            logger.error("删除文件异常{}", e);
        } finally {
            try {
                if (fs != null) {
                    fs.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return flag;
    }

    /**
     * 本地文件上传HDFS
     *
     * @param hdfsUrl         地址url
     * @param localFolderPath 本地文件路径
     * @param fileName        文件名称
     * @param hdfsFolderPath  hdfs文件路径
     */
    public static void uploadFile(String hdfsUrl, String localFolderPath, String fileName, String hdfsFolderPath) {

        FileSystem fs = getFileSystem(hdfsUrl, false);
        Path src = new Path(localFolderPath + "/" + fileName);
        Path dst = new Path(hdfsFolderPath + "/" + fileName);
        try {
            fs.copyFromLocalFile(src, dst);
            fs.close();
        } catch (IOException e) {
            logger.error("上传失败！{}", e);
        }

    }

    /**
     * 下载文件至本地
     *
     * @param hdfsUrl    hdfs地址
     * @param folderPath 文件路径
     * @param fileName   文件名称
     * @param pastePath  保存路径
     */
    public void downloadFile(String hdfsUrl, String folderPath, String fileName, String pastePath) {

        String file = folderPath + "/" + fileName;
        FileSystem fs = getFileSystem(hdfsUrl, false);

        InputStream is;
        try {
            is = fs.open(new Path(file));
            IOUtils.copyBytes(is, new FileOutputStream(new File(pastePath + "\\" + fileName + ".txt")), 4096, true);// 保存到本地
            // 最后
            // 关闭输入输出流linux
        } catch (IllegalArgumentException e) {
            logger.error("下载失败！{}", e);
        } catch (IOException e) {
            logger.error("下载失败！{}", e);
        }

    }

    /**
     * 获取文件列表
     *
     * @param hdfsUrl    地址url
     * @param folderPath 文件路径
     * @return
     */
    public static FileStatus[] listFiles(String hdfsUrl, String folderPath) {

        FileSystem fs = getFileSystem(hdfsUrl, false);
        Path dst = new Path(folderPath);
        FileStatus[] files = null;
        try {
            files = fs.listStatus(dst);
            for (FileStatus file : files) {
                System.out.println(file.getPath().toString());
            }
        } catch (IOException e) {
            logger.error("{}", e);
        }
        return files;
    }

    /**
     * 获取文件在集群中的数据块信息
     *
     * @param hdfsUrl
     * @param folderPath
     * @param fileName
     * @return
     */
    public static BlockLocation[] getBlockInfo(String hdfsUrl, String folderPath, String fileName) {

        FileSystem fs = getFileSystem(hdfsUrl, false);
        Path dst = new Path(folderPath + "/" + fileName);
        FileStatus fileStatus;
        BlockLocation[] blkloc = null;
        try {
            fileStatus = fs.getFileStatus(dst);
            blkloc = fs.getFileBlockLocations(fileStatus, 0, fileStatus.getLen());
        } catch (IOException e) {
            logger.error("查询出错！{}", e);
        }

        return blkloc;
    }

    /**
     * <p>
     * toHdfs
     * </p>
     * <p>
     * 向hdfs put 数据:
     * </p>
     *
     * @param hdfsUrl
     * @param inputStream
     * @param fileName
     * @param outputPath
     * @return
     */
    public static boolean toHdfs(String hdfsUrl, InputStream inputStream, String fileName, String outputPath) {
        boolean flag = false;
        FileSystem hdfs = getFileSystem(hdfsUrl, true);
        FSDataOutputStream outputStream = null;
        try {
            outputStream = hdfs.create(new Path(outputPath + File.separator + fileName));
            IOUtils.copyBytes(inputStream, outputStream, new Configuration(), true);
            flag = true;
        } catch (IllegalArgumentException e) {
            logger.error("IllegalArgumentException hdfs上传异常:{}", e);
            flag = false;
        } catch (IOException e) {
            logger.error("IOExceptionhdfs 上传异常:{}", e);
            flag = false;
        }
        return flag;

    }


    public static void main(String[] args) throws FileNotFoundException {

        String hdfsUrl = "hdfs://192.168.100.62:8020/";
        // mkdirFolder(hdfsUrl, "/ips");
        // createFile(hdfsUrl, "/ips/", "1011011");
        // listFiles(hdfsUrl, "/ips");

        // uploadFile(hdfsUrl, "C:\\Users\\HX\\Desktop", "a2000.csv",
        // "/home/data/dt=2019-03-25");

        InputStream io = new FileInputStream("C:\\Users\\HX\\Desktop\\a2000.csv");

        toHdfs(hdfsUrl, io, "a2000.csv", "/home/data/dt=2019-03-25");
    }

}
