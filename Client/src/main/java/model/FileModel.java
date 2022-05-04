package model;

public class FileModel {
    private String fileName;
    private String iconURL;
    private boolean isDir;
    private boolean isUpperDir;

    public FileModel(String fileName, String iconURL, boolean dir, boolean isUpperDir) {
        this.fileName = fileName;
        this.iconURL = iconURL;
        this.isDir = dir;
        this.isUpperDir = isUpperDir;
    }

    public String getFileName() {
        return fileName;
    }

    public String getIconURL() {
        return iconURL;
    }

    public boolean isDir() {
        return isDir;
    }

    public boolean isUpperDir() {
        return isUpperDir;
    }
}
