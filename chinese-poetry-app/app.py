from flask import Flask
import logging
import os

# Import blueprints
from routes.web import web_bp
from routes.api import api_bp
from config.logging_config import setup_logging
from config import constants

app = Flask(__name__)

# Load configuration
try:
    app.config.from_object(constants)
except ImportError:
    raise ImportError("Failed to load configuration from config.constants")

# Setup logging
logger = setup_logging(app)

# Register blueprints
app.register_blueprint(web_bp)
app.register_blueprint(api_bp)

if __name__ == '__main__':
    # Only log in the reloader child process (not the parent)
    if os.environ.get('WERKZEUG_RUN_MAIN') == 'true':
        logger.info("🎴 中国古诗词鉴赏应用启动中...")
        logger.info("📍 访问地址: http://localhost:5000")
        logger.info("🧪 测试页面: http://localhost:5000/test")
        logger.info("=" * 60)
        
        # Check API key configuration
        api_key = app.config.get('DEEPSEEK_API_KEY')
        if api_key == "your_deepseek_api_key_here" or not api_key:
            logger.warning("警告: 请在 config/constants.py 中配置你的 DeepSeek API 密钥")
            logger.warning("当前将使用备用数据进行演示")
        else:
            logger.info(f"API密钥已配置: ***{api_key[-4:] if len(api_key) > 4 else '***'}")
        
        # Check pinyin feature configuration
        enable_pinyin = app.config.get('ENABLE_PINYIN', False)
        logger.info(f"拼音功能: {'✅ 启用' if enable_pinyin else '❌ 禁用'}")
        
        logger.info("=" * 60)
    
    app.run(debug=True, host='0.0.0.0', port=5000)