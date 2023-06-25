package com.github.redreaperlp.reaperutility.config.files;

public class JFiles {

    private String serverFile;

    public JFiles(String serverFile) {
        this.serverFile = serverFile;
    }

    public JFiles() {
        this.serverFile = "servers.json";
    }

    public String serverFile() {
        return serverFile;
    }

    public void serverFile(String serverFile) {
        this.serverFile = serverFile;
    }
}
