import React, { useState } from 'react';
import CalendarView from './components/CalendarView';
import StoryView from './components/StoryView';
import './App.css';

function App() {
  const [selectedDate, setSelectedDate] = useState(null);
  const [showStory, setShowStory] = useState(false);

  const handleDateSelect = (month, day) => {
    setSelectedDate({ month, day });
    setShowStory(true);
  };

  const handleBackToCalendar = () => {
    setShowStory(false);
    setSelectedDate(null);
  };

  return (
    <div className="App">
      <header className="app-header">
        <h1>IT History - Today in Tech</h1>
        <p className="tagline">Discover fascinating stories from computer history</p>
      </header>

      <main className="app-main">
        {!showStory ? (
          <CalendarView onDateSelect={handleDateSelect} />
        ) : (
          <StoryView 
            month={selectedDate.month} 
            day={selectedDate.day}
            onBack={handleBackToCalendar}
          />
        )}
      </main>

      <footer className="app-footer">
        <p>&copy; 2024 IT History Website | Powered by AI</p>
      </footer>
    </div>
  );
}

export default App;
