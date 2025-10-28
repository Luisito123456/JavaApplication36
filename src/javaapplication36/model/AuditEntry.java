/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package javaapplication36.model;




import java.util.Date;

public class AuditEntry {

    private int id;
    private String table;
    private String recordId;
    private String action;
    private User user;
    private String details;
    private Date createdAt;

    public AuditEntry() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTable() { return table; }
    public void setTable(String table) { this.table = table; }

    public String getRecordId() { return recordId; }
    public void setRecordId(String recordId) { this.recordId = recordId; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}
