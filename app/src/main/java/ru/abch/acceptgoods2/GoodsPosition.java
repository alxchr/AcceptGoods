package ru.abch.acceptgoods2;

public class GoodsPosition {
    String barcode, description, cell, id, article;
    int qnt, total;
    GoodsPosition(String id, String barcode, String description, String cell, int qnt, String article, int total){
        this.barcode = barcode;
        this.description = description;
        this.cell = cell;
        this.qnt = qnt;
        this.id = id;
        this.article = article;
        this.total = total;
    }

    public String getId() {
        return id;
    }

    public int getQnt() {
        return qnt;
    }

    public String getBarcode() {
        return barcode;
    }

    public String getDescription() {
        return description;
    }

    public String getCell() {
        return cell;
    }
}
