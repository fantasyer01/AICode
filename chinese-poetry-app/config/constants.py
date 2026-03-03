# constants.py - Application constants and configuration

import os

# DeepSeek API配置
DEEPSEEK_API_KEY = os.environ.get('DEEPSEEK_API_KEY', 'your_deepseek_api_key_here')

# 流式返回配置
DEFAULT_STREAM_MODE = True  # 是否默认启用流式返回，True-启用，False-禁用

# 流式日志输出配置
IS_OUTPUT_STREAM_LOG = True  # 是否输出流式返回日志，True-输出，False-不输出

# kie.ai 图片生成API配置
KIE_API_KEY = os.environ.get('KIE_API_KEY', '')
KIE_API_BASE_URL = "https://api.kie.ai/api/v1/jobs"
IMAGE_STORAGE_DIR = os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))), 'static', 'images')
IMAGE_GENERATION_TIMEOUT = 180  # 前端轮询超时时间（秒）
IMAGE_ASPECT_RATIO = "3:2"
IMAGE_RESOLUTION = "1K"
IMAGE_OUTPUT_FORMAT = "png"

# 图片生成提示词模板
IMAGE_PROMPT_TEMPLATE = """A traditional Chinese Song Dynasty (宋代) landscape painting (山水画) inspired by the poem "{title}" by {author}.
The painting captures the mood and imagery of: {verse_line}.
Style requirements: classical Chinese ink wash painting, fresh and elegant (清新淡雅), generous white space (留白), soft muted colors, misty mountains, flowing water, delicate brushwork, serene atmosphere, traditional Song Dynasty artistic aesthetics.
No text, no characters, no calligraphy in the image."""

# 其他配置
DEBUG = os.environ.get('DEBUG', 'false').lower() == 'true'

# AI提示词模板
STREAM_PROMPT_TEMPLATE = """请提供以下句子的完整古诗词信息："{verse_line}"

要求：
1. 必须包含以下所有部分，不能省略：
   # 《实际诗词标题》
   **作者**: 作者名 · 朝代
   
   ## 📜 完整诗词
   （必须包含每一句诗词，每句独立一行，括号标注拼音，诗词内容中间部分不能省略，比如用......）
   
   ## 💬 译文
   （必须对每一句诗词进行白话文翻译，与原文一一对应，译文内容中间部分不能省略，比如用......）
   
   ## 📖 创作背景
   （详细描述200字左右，包括创作时间、地点、背景事件）
   
   ## 🔍 疑难字词解析
   - **字词1**: 详细解释
   - **字词2**: 详细解释
   （至少包含3-5个重要字词）
   
   ## 🎨 整体鉴赏
   （必须包含200字以上的详细鉴赏，分析艺术手法、情感表达、主题思想）
   
   ## ⭐ 名人点评
   - **点评人1**: 具体点评内容
   - **点评人2**: 具体点评内容
   （必须包含2-3条历史名人点评）

注意：
- 标题部分直接输出实际的诗词标题（如《静夜思》《登鹳雀楼》），不要输出"诗词标题"这几个字
- 必须完整输出所有部分，不要省略或简化输出......
- 每个部分都要有实质内容，不能只是标题
"""

JSON_PROMPT_TEMPLATE = """请提供以下句子的完整古诗词信息："{verse_line}"

要求：
1. 以JSON格式返回
2. 包含以下字段：
   - title: 诗词标题
   - author: 作者
   - dynasty: 朝代
   - full_text: 诗词全文，每一句诗词下方，需用括号标注其汉语拼音，拼音标注需规范，每个字的拼音单独标注，诗句中的每个字都应有拼音。（数组，每句一个元素）
   - translation: 现代白话文译文（对诗词内容进行逐句翻译，数组，每句一个元素，与full_text对应）
   - background: 创作背景（200字左右）
   - difficult_words: 疑难字词解析（数组，每个元素包含word和explanation字段）
   - appreciation: 整体鉴赏（300字左右）
   - celebrity_reviews: 历史名人点评（数组，每个元素包含reviewer和review字段，包含2-3条历史名人的点评）

JSON示例格式：
{{
    "title": "静夜思",
    "author": "李白",
    "dynasty": "唐代",
    "full_text": ["床前明月光（chuáng qián míng yuè guāng）", "疑是地上霜（yí shì dì shàng shuāng）", "举头望明月（jǔ tóu wàng míng yuè）", "低头思故乡（dī tóu sī gù xiāng）"],
    "translation": ["明亮的月光洒在床前的窗户纸上", "好像地上泛起了一层霜", "抬起头来看那天窗外空中的明月", "不由得低头沉思，想起远方的家乡"],
    "background": "这首诗创作于唐玄宗开元十四年（726年），当时李白约25岁，寓居扬州旅舍。在一个月明星稀的夜晚，诗人望见秋月，思乡之情油然而生，写下了这首传诵千古、中外皆知的名诗。",
    "difficult_words": [
        {{"word": "疑", "explanation": "好像，似乎。生动地写出了诗人睡梦初醒，恍惚中将照射在床前的清冷月光误作铺在地面的浓霜。"}},
        {{"word": "举头", "explanation": "抬头。一个简单的动作，真切地描绘出诗人被月光吸引，不自觉地向窗外望去的神态。"}},
        {{"word": "思故乡", "explanation": "思念家乡。直接点明了诗歌的核心情感——乡愁。"}}
    ],
    "appreciation": "《静夜思》语言清新朴素...",
    "celebrity_reviews": [
        {{"reviewer": "明代胡应麟", "review": "太白诸绝句，信口而成，所谓无意于工而无不工者。"}},
        {{"reviewer": "清代王夫之", "review": "以景寓情，浑然天成，读来如见故乡。"}}
    ]
}}

请确保返回纯JSON格式，不要包含其他文本。
"""
