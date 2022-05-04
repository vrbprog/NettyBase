package model;

public class FileModel {
    private String fileName;
    private String iconURL;
    private boolean isDir;
    private boolean isUpperDir;
    private boolean isSelect;

    public FileModel(String fileName, String iconURL, boolean dir, boolean isUpperDir, boolean isSelect) {
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

    public boolean isSelect() {
        return isSelect;
    }

    public void setSelect(boolean select) {
        isSelect = select;
    }
}
