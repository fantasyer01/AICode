"""
API Routes Blueprint
Handles API endpoints for poetry search
"""

import logging
from flask import Blueprint, request, jsonify, Response, stream_with_context
from services.poetry_service import PoetryAPI
from services.image_service import ImageService

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
    
    # Check for error response
    if poetry_data is None or 'error' in poetry_data:
        logger.warning(f"查询失败: {poetry_data}")
        return jsonify(poetry_data if poetry_data else {'error': '查询失败，请稍后重试'})
    
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


@api_bp.route('/image', methods=['POST'])
def generate_image():
    """Look up or generate a poem-related image"""
    title = request.form.get('title', '').strip()
    author = request.form.get('author', '').strip()
    dynasty = request.form.get('dynasty', '').strip()
    verse = request.form.get('verse', '').strip()

    if not title or not author:
        return jsonify({'status': 'error', 'message': '缺少诗词标题或作者'})

    logger.info(f"收到图片请求: {title} - {author}")

    service = ImageService()
    result = service.get_or_generate_image(title, author, dynasty, verse)
    return jsonify(result)


@api_bp.route('/image_status', methods=['GET'])
def image_status():
    """Poll image generation task status"""
    task_id = request.args.get('taskId', '').strip()
    title = request.args.get('title', '').strip()
    author = request.args.get('author', '').strip()

    if not task_id:
        return jsonify({'status': 'error', 'message': '缺少 taskId'})

    service = ImageService()
    result = service.check_task_status_with_meta(task_id, title, author)
    return jsonify(result)
