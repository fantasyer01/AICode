"""
Logging configuration for Chinese Poetry App
"""

import logging
import os
from logging.handlers import RotatingFileHandler

def setup_logging(app):
    """Configure structured logging for the application"""
    
    # Create logs directory if not exists
    log_dir = os.path.join(os.path.dirname(os.path.dirname(__file__)), 'logs')
    os.makedirs(log_dir, exist_ok=True)
    
    # Set logging level based on debug mode
    log_level = logging.DEBUG if app.config.get('DEBUG', False) else logging.INFO
    
    # Define log format
    log_format = logging.Formatter(
        '[%(asctime)s] %(levelname)s [%(name)s:%(lineno)d] - %(message)s',
        datefmt='%Y-%m-%d %H:%M:%S'
    )
    
    # Console handler
    console_handler = logging.StreamHandler()
    console_handler.setLevel(log_level)
    console_handler.setFormatter(log_format)
    
    # File handler for general logs
    general_log_file = os.path.join(log_dir, 'app.log')
    file_handler = RotatingFileHandler(
        general_log_file,
        maxBytes=10 * 1024 * 1024,  # 10MB
        backupCount=5,
        encoding='utf-8'
    )
    file_handler.setLevel(log_level)
    file_handler.setFormatter(log_format)
    
    # File handler for error logs
    error_log_file = os.path.join(log_dir, 'error.log')
    error_handler = RotatingFileHandler(
        error_log_file,
        maxBytes=10 * 1024 * 1024,  # 10MB
        backupCount=5,
        encoding='utf-8'
    )
    error_handler.setLevel(logging.ERROR)
    error_handler.setFormatter(log_format)
    
    # Configure root logger
    root_logger = logging.getLogger()
    root_logger.setLevel(log_level)
    root_logger.addHandler(console_handler)
    root_logger.addHandler(file_handler)
    root_logger.addHandler(error_handler)
    
    # Configure app logger
    app.logger.setLevel(log_level)
    
    # Reduce werkzeug logging noise
    logging.getLogger('werkzeug').setLevel(logging.WARNING)
    
    return app.logger
