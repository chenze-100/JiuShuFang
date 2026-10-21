package com.JiuShuFangSecondHand.factory;

import java.io.InputStream;

/**
 * @author itheima
 */
public interface FileStorageFactory {


    /**
     *  上传图片文件
     * @param prefix  文件前缀
     * @param filename  文件名
     * @param inputStream 文件流
     * @return  文件全路径
     */
    String uploadImgFile(String prefix, String filename,InputStream inputStream);


    String uploadImgFile(String prefix, String filename, InputStream inputStream , String contentType);


    String uploadImgFile(String filename, InputStream inputStream , String contentType);


    String uploadImgFile(String filename, InputStream inputStream);

    /**
     *  上传html文件
     * @param prefix  文件前缀
     * @param filename   文件名
     * @param inputStream  文件流
     * @return  文件全路径
     */
    public String uploadHtmlFile(String prefix, String filename,InputStream inputStream);


    String uploadHtmlFile(String filename, InputStream inputStream);

    /**
     * 删除文件
     * @param pathUrl  文件全路径
     */
    public void delete(String pathUrl);

    /**
     * 下载文件
     * @param pathUrl  文件全路径
     * @return
     *
     */
    public byte[]  downLoadFile(String pathUrl);

}
