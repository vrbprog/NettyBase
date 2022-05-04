package model;

// Класс параметров передаваемого файла
public class FileInfo {
    private String filename;
    private int fileSize;

    public FileInfo(String filename, int fileSize) {
        this.filename = filename;
        this.fileSize = fileSize;
    }

    public String getFilename() {
        return filename;
    }

    public int getFileSize() {
        return fileSize;
    }

}
