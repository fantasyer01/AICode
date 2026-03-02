# ============================================
# Chinese Poetry App - Docker Image
# Lightweight single-container deployment
# ============================================

FROM python:3.12-slim

# Set environment variables
ENV PYTHONDONTWRITEBYTECODE=1 \
    PYTHONUNBUFFERED=1 \
    TZ=Asia/Shanghai

# Set working directory
WORKDIR /app

# Install dependencies first (better layer caching)
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Copy application code
COPY app.py .
COPY config/ config/
COPY routes/ routes/
COPY services/ services/
COPY templates/ templates/
COPY static/ static/

# Create logs directory
RUN mkdir -p logs

# Expose port
EXPOSE 9000

# Health check
HEALTHCHECK --interval=30s --timeout=10s --retries=3 \
    CMD python -c "import urllib.request; urllib.request.urlopen('http://localhost:9000/')" || exit 1

# Run with Gunicorn (gevent worker for SSE streaming support)
CMD ["gunicorn", \
     "--bind", "0.0.0.0:9000", \
     "--workers", "2", \
     "--worker-class", "gevent", \
     "--timeout", "300", \
     "--keep-alive", "5", \
     "--access-logfile", "-", \
     "--error-logfile", "-", \
     "app:app"]
