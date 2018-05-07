package com.holyrobot.common;

import java.io.Serializable;
import java.util.Date;

public class Routeinfo implements Serializable{
    private static final long serialVersionUID = 1L;

    private String id;

    private String urlid;

    private String name;

    private String routetype;

    private String price;

    private String itemno;

    private String suppliername;

    private String destination;

    private String departure;

    private String grade;

    private String gradenum;

    private String beennum;

    private String whantto;

    private String datasource;

    private Date createdate;

    private String creator;

    private String creatorid;

    private String teamtype;
    
    private String featureservice;

    private String itineraryoutline;

    private String productrecommend;

    private String reducedprice;

    private String productfeature;

    private String productoutline;

    private String itinerarydetails;

    private String expense;

    private String reserveinfo;

    private String remark;

    public String getFeatureservice() {
        return featureservice;
    }

    public void setFeatureservice(String featureservice) {
        this.featureservice = featureservice == null ? null : featureservice.trim();
    }

    public String getItineraryoutline() {
        return itineraryoutline;
    }

    public void setItineraryoutline(String itineraryoutline) {
        this.itineraryoutline = itineraryoutline == null ? null : itineraryoutline.trim();
    }

    public String getProductrecommend() {
        return productrecommend;
    }

    public void setProductrecommend(String productrecommend) {
        this.productrecommend = productrecommend == null ? null : productrecommend.trim();
    }

    public String getReducedprice() {
        return reducedprice;
    }

    public void setReducedprice(String reducedprice) {
        this.reducedprice = reducedprice == null ? null : reducedprice.trim();
    }

    public String getProductfeature() {
        return productfeature;
    }

    public void setProductfeature(String productfeature) {
        this.productfeature = productfeature == null ? null : productfeature.trim();
    }

    public String getProductoutline() {
        return productoutline;
    }

    public void setProductoutline(String productoutline) {
        this.productoutline = productoutline == null ? null : productoutline.trim();
    }

    public String getItinerarydetails() {
        return itinerarydetails;
    }

    public void setItinerarydetails(String itinerarydetails) {
        this.itinerarydetails = itinerarydetails == null ? null : itinerarydetails.trim();
    }

    public String getExpense() {
        return expense;
    }

    public void setExpense(String expense) {
        this.expense = expense == null ? null : expense.trim();
    }

    public String getReserveinfo() {
        return reserveinfo;
    }

    public void setReserveinfo(String reserveinfo) {
        this.reserveinfo = reserveinfo == null ? null : reserveinfo.trim();
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark == null ? null : remark.trim();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id == null ? null : id.trim();
    }

    public String getUrlid() {
        return urlid;
    }

    public void setUrlid(String urlid) {
        this.urlid = urlid == null ? null : urlid.trim();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    public String getRoutetype() {
        return routetype;
    }

    public void setRoutetype(String routetype) {
        this.routetype = routetype == null ? null : routetype.trim();
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price == null ? null : price.trim();
    }

    public String getItemno() {
        return itemno;
    }

    public void setItemno(String itemno) {
        this.itemno = itemno == null ? null : itemno.trim();
    }

    public String getSuppliername() {
        return suppliername;
    }

    public void setSuppliername(String suppliername) {
        this.suppliername = suppliername == null ? null : suppliername.trim();
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination == null ? null : destination.trim();
    }

    public String getDeparture() {
        return departure;
    }

    public void setDeparture(String departure) {
        this.departure = departure == null ? null : departure.trim();
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade == null ? null : grade.trim();
    }

    public String getGradenum() {
        return gradenum;
    }

    public void setGradenum(String gradenum) {
        this.gradenum = gradenum == null ? null : gradenum.trim();
    }

    public String getBeennum() {
        return beennum;
    }

    public void setBeennum(String beennum) {
        this.beennum = beennum == null ? null : beennum.trim();
    }

    public String getWhantto() {
        return whantto;
    }

    public void setWhantto(String whantto) {
        this.whantto = whantto == null ? null : whantto.trim();
    }

    public String getDatasource() {
        return datasource;
    }

    public void setDatasource(String datasource) {
        this.datasource = datasource == null ? null : datasource.trim();
    }

    public Date getCreatedate() {
        return createdate;
    }

    public void setCreatedate(Date createdate) {
        this.createdate = createdate;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator == null ? null : creator.trim();
    }

    public String getCreatorid() {
        return creatorid;
    }

    public void setCreatorid(String creatorid) {
        this.creatorid = creatorid == null ? null : creatorid.trim();
    }

    public String getTeamtype() {
        return teamtype;
    }

    public void setTeamtype(String teamtype) {
        this.teamtype = teamtype == null ? null : teamtype.trim();
    }

	@Override
	public String toString() {
		return "Routeinfo [id=" + id + ", urlid=" + urlid + ", name=" + name + ", routetype=" + routetype + ", price="
				+ price + ", itemno=" + itemno + ", suppliername=" + suppliername + ", destination=" + destination
				+ ", departure=" + departure + ", grade=" + grade + ", gradenum=" + gradenum + ", beennum=" + beennum
				+ ", whantto=" + whantto + ", datasource=" + datasource + ", createdate=" + createdate + ", creator="
				+ creator + ", creatorid=" + creatorid + ", teamtype=" + teamtype + ", featureservice=" + featureservice
				+ ", itineraryoutline=" + itineraryoutline + ", productrecommend=" + productrecommend
				+ ", reducedprice=" + reducedprice + ", productfeature=" + productfeature + ", productoutline="
				+ productoutline + ", itinerarydetails=" + itinerarydetails + ", expense=" + expense + ", reserveinfo="
				+ reserveinfo + ", remark=" + remark + "]";
	}
    
    
    
    
}