package com.ithistory.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ithistory.dto.ImageDto;
import com.ithistory.dto.StoryDto;
import com.ithistory.entity.Image;
import com.ithistory.entity.Story;
import com.ithistory.llm.LlmException;
import com.ithistory.llm.LlmClient;
import com.ithistory.llm.LlmRequest;
import com.ithistory.llm.LlmResponse;
import com.ithistory.repository.ImageRepository;
import com.ithistory.repository.StoryRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class StoryService {
    
    private static final Logger logger = LoggerFactory.getLogger(StoryService.class);
    
    @Autowired
    private StoryRepository storyRepository;
    
    @Autowired
    private ImageRepository imageRepository;
    
    @Autowired
    private LlmClient llmClient;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * Get story for a specific date, generate if not cached
     */
    @Transactional
    public StoryDto getStory(Integer month, Integer day, boolean forceRefresh) throws LlmException {
        // Validate date
        if (month < 1 || month > 12 || day < 1 || day > 31) {
            throw new IllegalArgumentException("Invalid date: month=" + month + ", day=" + day);
        }
        
        // Check cache first
        Optional<Story> cachedStory = storyRepository.findByDateMonthAndDateDay(month, day);
        
        if (cachedStory.isPresent() && !forceRefresh) {
            logger.info("Cache hit for date {}/{}", month, day);
            Story story = cachedStory.get();
            story.setViewCount(story.getViewCount() + 1);
            storyRepository.save(story);
            return convertToDto(story, true);
        }
        
        // Generate new story
        logger.info("Generating new story for date {}/{}", month, day);
        Story story = generateAndSaveStory(month, day);
        
        return convertToDto(story, false);
    }
    
    /**
     * Get dates with available stories for a specific month
     */
    public List<Integer> getDatesWithStories(Integer month) {
        return storyRepository.findByDateMonth(month)
                .stream()
                .map(Story::getDateDay)
                .collect(Collectors.toList());
    }
    
    /**
     * Generate story using LLM and save to database
     */
    private Story generateAndSaveStory(Integer month, Integer day) throws LlmException {
        // Prepare LLM request
        LlmRequest request = new LlmRequest();
        request.setMonth(month);
        request.setDay(day);
        
        // Call LLM
        LlmResponse llmResponse = llmClient.generateStory(request);
        
        // Create Story entity
        Story story = new Story();
        story.setId(UUID.randomUUID());
        story.setDateMonth(month);
        story.setDateDay(day);
        story.setTitle(llmResponse.getTitle());
        story.setIntroduction(llmResponse.getIntroduction());
        story.setContent(formatContent(llmResponse.getSections()));
        story.setEpilogue(llmResponse.getEpilogue());
        story.setReferences(llmResponse.getReferences());
        story.setStatus(Story.StoryStatus.PUBLISHED);
        
        // Save metadata
        try {
            story.setMetadata(objectMapper.writeValueAsString(llmResponse));
        } catch (JsonProcessingException e) {
            logger.warn("Failed to serialize metadata", e);
        }
        
        // Save story
        // story = storyRepository.save(story);
        
        // Save images
        // saveImages(story.getId(), llmResponse.getImageDescriptions());
        
        logger.info("Story generated and saved: id={}, title={}", story.getId(), story.getTitle());
        
        return story;
    }
    
    /**
     * Format sections into HTML content
     */
    private String formatContent(List<LlmResponse.Section> sections) {
        StringBuilder html = new StringBuilder();
        
        for (LlmResponse.Section section : sections) {
            html.append("<section>");
            html.append("<h2>").append(section.getHeading()).append("</h2>");
            html.append("<div>").append(section.getContent()).append("</div>");
            html.append("</section>");
        }
        
        return html.toString();
    }
    
    /**
     * Save image descriptions
     */
    private void saveImages(UUID storyId, List<LlmResponse.ImageDescription> imageDescriptions) {
        if (imageDescriptions == null) {
            return;
        }
        
        for (LlmResponse.ImageDescription imgDesc : imageDescriptions) {
            Image image = new Image();
            image.setStoryId(storyId);
            image.setImageUrl("https://placeholder.com/600x400"); // Placeholder for now
            image.setCaption(imgDesc.getCaption());
            image.setAltText(imgDesc.getDescription());
            image.setOrderIndex(imgDesc.getOrderIndex());
            image.setSource("generated");
            
            imageRepository.save(image);
        }
    }
    
    /**
     * Convert Story entity to DTO
     */
    private StoryDto convertToDto(Story story, boolean cached) {
        StoryDto dto = new StoryDto();
        dto.setStoryId(story.getId());
        dto.setDate(new StoryDto.DateDto(story.getDateMonth(), story.getDateDay()));
        dto.setTitle(story.getTitle());
        dto.setIntroduction(story.getIntroduction());
        dto.setContent(story.getContent());
        dto.setEpilogue(story.getEpilogue());
        dto.setReferences(story.getReferences());
        dto.setGeneratedAt(story.getCreatedAt());
        dto.setCached(cached);
        
        // Load images
        List<Image> images = imageRepository.findByStoryIdOrderByOrderIndexAsc(story.getId());
        List<ImageDto> imageDtos = images.stream()
                .map(this::convertImageToDto)
                .collect(Collectors.toList());
        dto.setImages(imageDtos);
        
        return dto;
    }
    
    /**
     * Convert Image entity to DTO
     */
    private ImageDto convertImageToDto(Image image) {
        ImageDto dto = new ImageDto();
        dto.setImageUrl(image.getImageUrl());
        dto.setCaption(image.getCaption());
        dto.setAltText(image.getAltText());
        dto.setOrderIndex(image.getOrderIndex());
        return dto;
    }
}
