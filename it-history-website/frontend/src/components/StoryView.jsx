import React, { useState, useEffect } from 'react';
import axios from 'axios';
import './StoryView.css';

function StoryView({ month, day, onBack }) {
  const [story, setStory] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    fetchStory();
  }, [month, day]);

  const fetchStory = async () => {
    setLoading(true);
    setError(null);
    
    try {
      const response = await axios.get(`/api/story/${month}/${day}`);
      setStory(response.data);
    } catch (err) {
      console.error('Error fetching story:', err);
      setError(err.response?.data?.errorMessage || 'Failed to load story');
    } finally {
      setLoading(false);
    }
  };

  const formatDate = () => {
    const monthNames = [
      'January', 'February', 'March', 'April', 'May', 'June',
      'July', 'August', 'September', 'October', 'November', 'December'
    ];
    return `${monthNames[month - 1]} ${day}`;
  };

  const formatTextWithLineBreaks = (text) => {
    if (!text) return '';
    // Replace \n with <br> tags for proper line breaks
    return text.replace(/\n/g, '<br>');
  };

  if (loading) {
    return (
      <div className="story-view">
        <div className="loading-container">
          <div className="spinner"></div>
          <h2>Generating Your IT History Story...</h2>
          <p>This may take a few moments as we craft a unique narrative</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="story-view">
        <div className="error-container">
          <h2>Oops! Something went wrong</h2>
          <p className="error-message">{error}</p>
          <button onClick={onBack} className="back-button">
            ← Back to Calendar
          </button>
        </div>
      </div>
    );
  }

  if (!story) {
    return null;
  }

  return (
    <div className="story-view">
      <button onClick={onBack} className="back-button">
        ← Back to Calendar
      </button>

      <article className="story-article">
        <header className="story-header">
          <div className="story-date">{formatDate()}</div>
          <h1 className="story-title">{story.title}</h1>
          {story.cached && (
            <div className="cached-badge">
              <span>📚 From Archive</span>
            </div>
          )}
        </header>

        {story.introduction && (
          <div className="story-introduction">
            <div 
              className="introduction-text" 
              dangerouslySetInnerHTML={{ __html: story.introduction }}
            />
          </div>
        )}

        <div className="story-content">
          {story.images && story.images.length > 0 && (
            <div className="story-image-container">
              <img 
                src={story.images[0].imageUrl} 
                alt={story.images[0].altText}
                className="story-image"
              />
              {story.images[0].caption && (
                <p className="image-caption">{story.images[0].caption}</p>
              )}
            </div>
          )}

          <div 
            className="story-text" 
            dangerouslySetInnerHTML={{ __html: story.content }}
          />

          {story.images && story.images.length > 1 && (
            <div className="additional-images">
              {story.images.slice(1).map((image, index) => (
                <div key={index} className="story-image-container">
                  <img 
                    src={image.imageUrl} 
                    alt={image.altText}
                    className="story-image"
                  />
                  {image.caption && (
                    <p className="image-caption">{image.caption}</p>
                  )}
                </div>
              ))}
            </div>
          )}
        </div>

        {story.epilogue && (
          <div className="story-epilogue">
            <h2 className="epilogue-heading">故事尾声</h2>
            <div 
              className="epilogue-text" 
              dangerouslySetInnerHTML={{ __html: formatTextWithLineBreaks(story.epilogue) }}
            />
          </div>
        )}

        {story.references && (
          <div className="story-references">
            <h2 className="references-heading">参考资料</h2>
            <div 
              className="references-text" 
              dangerouslySetInnerHTML={{ __html: formatTextWithLineBreaks(story.references) }}
            />
          </div>
        )}

        <footer className="story-footer">
          <div className="story-meta">
            <p>Generated: {new Date(story.generatedAt).toLocaleDateString()}</p>
            <p>Story ID: {story.storyId}</p>
          </div>
        </footer>
      </article>
    </div>
  );
}

export default StoryView;
