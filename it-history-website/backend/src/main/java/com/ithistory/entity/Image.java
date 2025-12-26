package com.ithistory.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import java.util.UUID;

@Data
@Entity
@Table(name = "images")
public class Image {
    
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;
    
    @Column(name = "story_id", nullable = false)
    private UUID storyId;
    
    @Column(name = "image_url", length = 500)
    private String imageUrl;
    
    @Column(name = "caption", length = 200)
    private String caption;
    
    @Column(name = "alt_text", length = 200)
    private String altText;
    
    @Column(name = "order_index")
    private Integer orderIndex;
    
    @Column(name = "source", length = 50)
    private String source;
}
