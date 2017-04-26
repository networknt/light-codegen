package com.networknt.codegen;

/**
 * Created by steve on 22/10/16.
 */
public class CodegenWebConfig {
    String tmpFolder;
    String zipFolder;
    int zipKeptMinute;

    public CodegenWebConfig() {
    }

    public String getTmpFolder() {
        return tmpFolder;
    }

    public void setTmpFolder(String tmpFolder) {
        this.tmpFolder = tmpFolder;
    }

    public String getZipFolder() {
        return zipFolder;
    }

    public void setZipFolder(String zipFolder) {
        this.zipFolder = zipFolder;
    }

    public int getZipKeptMinute() {
        return zipKeptMinute;
    }

    public void setZipKeptMinute(int zipKeptMinute) {
        this.zipKeptMinute = zipKeptMinute;
    }
}
