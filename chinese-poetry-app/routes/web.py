"""
Web Routes Blueprint
Handles web page routes
"""

import logging
from flask import Blueprint, render_template, current_app

logger = logging.getLogger(__name__)

web_bp = Blueprint('web', __name__)


@web_bp.route('/')
def index():
    """Homepage"""
    logger.info("访问首页")
    return render_template('index.html')


@web_bp.route('/test')
def test():
    """Test page"""
    logger.info("访问测试页面")
    api_status = "已配置" if current_app.config.get('DEEPSEEK_API_KEY') != "your_deepseek_api_key_here" else "未配置"
    
    return f"""
    <h1>Flask应用运行正常！</h1>
    <p>配置状态：</p>
    <ul>
        <li>API密钥: {api_status}</li>
    </ul>
    <p>测试步骤：</p>
    <ol>
        <li>访问 <a href="/">首页</a> 使用搜索功能</li>
        <li>或者直接测试以下诗句：</li>
        <ul>
            <li>床前明月光</li>
            <li>春眠不觉晓</li>
        </ul>
    </ol>
    """
