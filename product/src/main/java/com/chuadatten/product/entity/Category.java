package com.chuadatten.product.entity;

import java.time.Instant;
import java.util.List;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.TextIndexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.chuadatten.product.common.Status;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Document(collection = "categories")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Category {
    @Id
    private String id;

    @Indexed(unique = true)
    private String slug;       // seo-friendly unique key: vi-du "dien-thoai"

    @TextIndexed(weight = 10)
    private String name;       // hiển thị trên UI
    
    @TextIndexed(weight = 5)
    private String description; // mô tả category

    @Indexed
    private String parentId;   // null nếu root

    @Indexed
    private List<String> ancestors; // danh sách id tổ tiên (từ root -> parent)

    private String path;       // tuỳ chọn: "dien-thoai/iphone"

    private Integer sortOrder; // dùng cho UI sorting

    private Status status;    

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

}