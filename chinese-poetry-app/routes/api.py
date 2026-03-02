"""
API Routes Blueprint
Handles API endpoints for poetry search
"""

import logging
from flask import Blueprint, request, jsonify, Response, stream_with_context
from services.poetry_service import PoetryAPI

logger = logging.getLogger(__name__)

api_bp = Blueprint('api', __name__)


@api_bp.route('/search', methods=['POST'])
def search_poetry():
    """Search poetry (non-streaming)"""
    verse_line = request.form.get('verse', '').strip()
    
    if not verse_line:
        logger.warning("空查询请求")
        return jsonify({'error': '请输入诗句'})
    
    logger.info(f"收到查询请求: {verse_line}")
    
    # Call API to query (non-streaming mode)
    api = PoetryAPI()
    poetry_data = api.query_poetry(verse_line, stream_mode=False)
    
    logger.info(f"返回诗词数据: {poetry_data['title']} - {poetry_data['author']}")
    
    return jsonify(poetry_data)


@api_bp.route('/search_stream', methods=['POST'])
def search_poetry_stream():
    """Search poetry (streaming)"""
    verse_line = request.form.get('verse', '').strip()
    
    if not verse_line:
        logger.warning("空查询请求")
        return jsonify({'error': '请输入诗句'})
    
    logger.info(f"收到流式查询请求: {verse_line}")
    
    # Call API streaming query
    api = PoetryAPI()
    stream_generator = api.query_poetry(verse_line, stream_mode=True)
    
    return Response(
        stream_with_context(stream_generator),
        mimetype='text/event-stream',
        headers={
            'Cache-Control': 'no-cache',
            'X-Accel-Buffering': 'no'
        }
    )
