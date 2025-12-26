import React, { useState, useEffect } from 'react';
import Calendar from 'react-calendar';
import 'react-calendar/dist/Calendar.css';
import './CalendarView.css';
import axios from 'axios';

function CalendarView({ onDateSelect }) {
  const [date, setDate] = useState(new Date());
  const [datesWithStories, setDatesWithStories] = useState([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    fetchCalendarData(date.getFullYear(), date.getMonth() + 1);
  }, [date]);

  const fetchCalendarData = async (year, month) => {
    setLoading(true);
    try {
      const response = await axios.get(`/api/calendar/${year}/${month}`);
      setDatesWithStories(response.data.datesWithStories || []);
    } catch (error) {
      console.error('Error fetching calendar data:', error);
      setDatesWithStories([]);
    } finally {
      setLoading(false);
    }
  };

  const handleDateClick = (value) => {
    const month = value.getMonth() + 1;
    const day = value.getDate();
    onDateSelect(month, day);
  };

  const tileClassName = ({ date, view }) => {
    if (view === 'month') {
      const day = date.getDate();
      if (datesWithStories.includes(day)) {
        return 'has-story';
      }
    }
    return null;
  };

  const tileContent = ({ date, view }) => {
    if (view === 'month') {
      const day = date.getDate();
      if (datesWithStories.includes(day)) {
        return <div className="story-indicator">📖</div>;
      }
    }
    return null;
  };

  return (
    <div className="calendar-view">
      <div className="calendar-container">
        <h2>Select a Date to Explore IT History</h2>
        <p className="calendar-hint">
          Click any date to discover what happened on that day in computer history
        </p>
        
        {loading && <div className="loading">Loading calendar...</div>}
        
        <Calendar
          onChange={setDate}
          value={date}
          onClickDay={handleDateClick}
          tileClassName={tileClassName}
          tileContent={tileContent}
        />
        
        <div className="calendar-legend">
          <div className="legend-item">
            <span className="legend-icon has-story-icon">📖</span>
            <span>Dates with cached stories</span>
          </div>
        </div>
      </div>
    </div>
  );
}

export default CalendarView;
