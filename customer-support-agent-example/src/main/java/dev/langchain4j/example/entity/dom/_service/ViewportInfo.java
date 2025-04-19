package dev.langchain4j.example.entity.dom._service;

class ViewportInfo {
    private final int width;
    private final int height;

    public ViewportInfo(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
