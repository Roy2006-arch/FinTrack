package com.fintrack.dto;
import com.fintrack.entity.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;

/** Query binding has explicit defaults; only this DTO accepts URL query values. */
public class TransactionQuery {
    private String search;
    private Long categoryId;
    private TransactionType type;
    private PaymentMethod paymentMethod;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) private LocalDate start;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) private LocalDate end;
    private String sort = "transactionDate";
    private String direction = "desc";
    private int page = 0;
    private int size = 15;
    public TransactionFilter filter() { return new TransactionFilter(search, categoryId, type, paymentMethod, start, end, sort, direction, page, size); }
    public String getSearch() { return search; } public void setSearch(String v) { search = v; }
    public Long getCategoryId() { return categoryId; } public void setCategoryId(Long v) { categoryId = v; }
    public TransactionType getType() { return type; } public void setType(TransactionType v) { type = v; }
    public PaymentMethod getPaymentMethod() { return paymentMethod; } public void setPaymentMethod(PaymentMethod v) { paymentMethod = v; }
    public LocalDate getStart() { return start; } public void setStart(LocalDate v) { start = v; }
    public LocalDate getEnd() { return end; } public void setEnd(LocalDate v) { end = v; }
    public String getSort() { return sort; } public void setSort(String v) { sort = v; }
    public String getDirection() { return direction; } public void setDirection(String v) { direction = v; }
    public int getPage() { return page; } public void setPage(int v) { page = v; }
    public int getSize() { return size; } public void setSize(int v) { size = v; }
}
