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
    enable_pinyin = current_app.config.get('ENABLE_PINYIN', False)
    api_status = "✅ 已配置" if current_app.config.get('DEEPSEEK_API_KEY') != "your_deepseek_api_key_here" else "❌ 未配置"
    pinyin_status = "✅ 启用" if enable_pinyin else "❌ 禁用"
    
    return f"""
    <h1>Flask应用运行正常！</h1>
    <p>配置状态：</p>
    <ul>
        <li>API密钥: {api_status}</li>
        <li>拼音功能: {pinyin_status}</li>
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
    <p><a href="/toggle-pinyin">切换拼音功能</a> (当前: {pinyin_status})</p>
    """


@web_bp.route('/toggle-pinyin')
def toggle_pinyin():
    """Toggle pinyin feature (for testing only)"""
    current_state = current_app.config.get('ENABLE_PINYIN', False)
    new_state = not current_state
    current_app.config['ENABLE_PINYIN'] = new_state
    
    logger.info(f"切换拼音功能: {'启用' if new_state else '禁用'}")
    return f"""
    <h1>拼音功能已{'启用' if new_state else '禁用'}</h1>
    <p><a href="/test">返回测试页面</a></p>
    <p><a href="/">返回首页</a></p>
    """
