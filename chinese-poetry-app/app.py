from flask import Flask, render_template, request, jsonify
import requests
import json
import re
from pypinyin import pinyin, Style

app = Flask(__name__)

# 从 config.py 读取配置
try:
    import config
    app.config.from_object(config)
    print("✅ 配置加载成功")
except ImportError:
    print("❌ 未找到 config.py 文件，使用默认配置")
    class DefaultConfig:
        DEEPSEEK_API_KEY = "your_deepseek_api_key_here"  # 请在config.py中设置
        SECRET_KEY = "dev_secret_key"
        ENABLE_PINYIN = False  # 默认不启用拼音功能
    app.config.from_object(DefaultConfig)

class PoetryAPI:
    def __init__(self):
        self.api_key = app.config.get('DEEPSEEK_API_KEY')
        self.base_url = "https://api.deepseek.com/v1/chat/completions"
        
    def query_poetry(self, verse_line):
        """查询诗词信息 - 使用最新的DeepSeek API格式"""
        if not self.api_key or self.api_key == "your_deepseek_api_key_here":
            print("⚠️  API密钥未配置，使用备用数据")
            return self.get_fallback_data(verse_line)
            
        headers = {
            "Authorization": f"Bearer {self.api_key}",
            "Content-Type": "application/json"
        }
        
        # 构建优化的提示词
        prompt = f"""
        请提供以下句子的完整古诗词信息："{verse_line}"
        
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
        
        示例格式：
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
            ],
            "source_verse": "{verse_line}"
        }}
        
        请确保返回纯JSON格式，不要包含其他文本。
        """
        
        data = {
            "model": "deepseek-chat",
            "messages": [
                {
                    "role": "user",
                    "content": prompt
                }
            ],
            "max_tokens": 8000,
            "temperature": 0.3,
            "stream": False
        }
        
        try:
            print(f"🔍 正在查询诗句: {verse_line}")
            print(f"🌐 请求URL: {self.base_url}")
            print(f"📤 请求头: Authorization: Bearer ***{self.api_key[-4:] if len(self.api_key) > 4 else '***'}")
            print(f"📦 请求体: {json.dumps(data, ensure_ascii=False, indent=2)}")
            
            response = requests.post(
                self.base_url,
                headers=headers,
                json=data,
                timeout=90
            )
            
            print(f"📡 API响应状态码: {response.status_code}")
            
            if response.status_code == 200:
                result = response.json()
                print("✅ API调用成功")
                
                # 📋 打印完整的API返回报文
                print("=" * 80)
                print("📄 DeepSeek API 完整返回报文:")
                print("=" * 80)
                print(json.dumps(result, ensure_ascii=False, indent=2))
                print("=" * 80)
                
                # 解析返回内容
                if 'choices' in result and len(result['choices']) > 0:
                    content = result['choices'][0]['message']['content']
                    print(f"📝 提取的返回内容长度: {len(content)} 字符")
                    
                    # 清理内容并提取JSON
                    cleaned_content = content.strip()
                    json_match = re.search(r'\{.*\}', cleaned_content, re.DOTALL)
                    
                    if json_match:
                        try:
                            json_str = json_match.group()
                            poetry_data = json.loads(json_str)
                            print("✅ JSON解析成功")
                            validated_data = self.validate_poetry_data(poetry_data, verse_line)
                            return validated_data
                        except json.JSONDecodeError as e:
                            print(f"❌ JSON解析失败: {e}")
                            if json_match:
                                print(f"📄 尝试解析的JSON字符串: {json_match.group()}")
                    else:
                        print("❌ 未找到JSON格式内容")
                        print(f"📄 原始返回内容: {cleaned_content}")
                else:
                    print("❌ API返回格式异常，缺少choices字段")
            else:
                print(f"❌ API请求失败: {response.status_code}")
                print(f"📄 错误响应: {response.text}")
                
        except requests.exceptions.Timeout:
            print("❌ API请求超时")
        except requests.exceptions.ConnectionError:
            print("❌ 网络连接错误")
        except requests.exceptions.RequestException as e:
            print(f"❌ 网络请求异常: {e}")
        except Exception as e:
            print(f"❌ 处理API响应时发生错误: {e}")
        
        # 如果API调用失败，返回备用数据
        print("🔄 使用备用数据")
        return self.get_fallback_data(verse_line)
    
    def validate_poetry_data(self, data, original_verse):
        """验证并清理API返回的数据"""
        print("🔧 验证和清理数据...")
        
        required_fields = ['title', 'author', 'dynasty', 'full_text', 'translation', 'background', 'difficult_words', 'appreciation', 'celebrity_reviews']
        
        for field in required_fields:
            if field not in data:
                if field in ['difficult_words', 'celebrity_reviews']:
                    data[field] = []
                    print(f"⚠️  缺失字段 {field}，已设置为空列表")
                elif field == 'translation':
                    data[field] = []
                    print(f"⚠️  缺失字段 {field}，已设置为空列表")
                else:
                    data[field] = "信息暂缺"
                    print(f"⚠️  缺失字段 {field}，已设置默认值")
        
        # 确保source_verse字段存在
        data['source_verse'] = original_verse
        
        # 确保full_text和translation是列表
        if isinstance(data['full_text'], str):
            data['full_text'] = [data['full_text']]
            print("🔄 将full_text从字符串转换为列表")
        
        if isinstance(data.get('translation'), str):
            data['translation'] = [data['translation']]
            print("🔄 将translation从字符串转换为列表")
        
        print(f"✅ 数据验证完成: {data['title']} - {data['author']}")
        return data
    
    def get_fallback_data(self, verse_line):
        """备用数据 - 当API不可用时使用"""
        print(f"🔄 使用备用数据 for: {verse_line}")
        
        fallback_data = {
            "床前明月光": {
                "title": "静夜思",
                "author": "李白", 
                "dynasty": "唐代",
                "full_text": ["床前明月光", "疑是地上霜", "举头望明月", "低头思故乡"],
                "translation": ["明亮的月光洒在床前的窗户纸上", "好像地上泛起了一层白霜", "抬起头来看那天窗外空中的明月", "不由得低头沉思，想起远方的家乡"],
                "background": "这首诗创作于唐玄宗开元十四年（726年），当时李白约25岁，寓居扬州旅舍。在一个月明星稀的夜晚，诗人望见秋月，思乡之情油然而生，写下了这首传诵千古、中外皆知的名诗。",
                "difficult_words": [
                    {"word": "疑", "explanation": "好像，似乎。生动地写出了诗人睡梦初醒，恍惚中将照射在床前的清冷月光误作铺在地面的浓霜。"},
                    {"word": "举头", "explanation": "抬头。一个简单的动作，真切地描绘出诗人被月光吸引，不自觉地向窗外望去的神态。"},
                    {"word": "思故乡", "explanation": "思念家乡。直接点明了诗歌的核心情感——乡愁。"}
                ],
                "appreciation": "《静夜思》语言清新朴素，明白如话，内容单纯却又韵味无穷。诗人通过\"明月光\"、\"地上霜\"、\"举头\"、\"低头\"等一系列动作和景物的白描，鲜明地勾勒出一幅生动的月夜思乡图。全诗从\"疑\"到\"望\"再到\"思\"，形象地揭示了诗人的内心活动，巧妙地表达了旅居思乡的孤寂情怀。其构思细致而深曲，却又不假雕琢，浑然天成，千百年来广泛吸引着读者，成为了中华文化中思乡符号的代表。",
                "celebrity_reviews": [
                    {"reviewer": "明代胡应麟", "review": "太白诸绝句，信口而成，所谓无意于工而无不工者。此诗写月夜思乡，千古传诵。"},
                    {"reviewer": "清代王夫之", "review": "以景寓情，浑然天成，读来如见故乡。通篇不着一个'思'字，而思乡之情溢于言表。"},
                    {"reviewer": "现代郭沫若", "review": "此诗平淡自然，不事雕琢，却能打动千古读者之心，足见李白天才之高妙。"}
                ],
                "source_verse": "床前明月光"
            },
            "春眠不觉晓": {
                "title": "春晓",
                "author": "孟浩然",
                "dynasty": "唐代",
                "full_text": ["春眠不觉晓", "处处闻啼鸟", "夜来风雨声", "花落知多少"],
                "translation": ["春日里贪睡不知不觉天已破晓", "搅乱我酣眠的是那处处啼鸟", "昨天夜里风声雨声一直不断", "那娇美的春花不知被吹落了多少"],
                "background": "这首诗是孟浩然隐居在鹿门山时所作，意境十分优美。诗人抓住春天的早晨刚刚醒来时的一瞬间展开描写和联想，生动地表达了诗人对春天的热爱和怜惜之情。",
                "difficult_words": [
                    {"word": "不觉晓", "explanation": "不知道天已经亮了。形容睡得香甜，不知不觉天就亮了。"},
                    {"word": "闻啼鸟", "explanation": "听到鸟的鸣叫声。表现出春天早晨的生机勃勃。"},
                    {"word": "花落知多少", "explanation": "不知道花被吹落了多少。表达了对春光易逝的惋惜之情。"}
                ],
                "appreciation": "《春晓》这首小诗，初读似觉平淡无奇，反复读之，便觉诗中别有天地。它的艺术魅力不在于华丽的辞藻，不在于奇绝的艺术手法，而在于它的韵味。整首诗的风格就像行云流水一样平易自然，然而悠远深厚，独臻妙境。千百年来，人们传诵它，探讨它，仿佛在这短短的四行诗里，蕴涵着开掘不完的艺术宝藏。",
                "celebrity_reviews": [
                    {"reviewer": "宋代朱熹", "review": "诗意清新自然，如春风拂面，令人心旷神怡。"},
                    {"reviewer": "清代沈德潜", "review": "语淡而味终不薄，真可谓以少少许胜多多许。全诗自然流畅，通俗易懂，却又余味无穷。"}
                ],
                "source_verse": "春眠不觉晓"
            }
        }
        
        if verse_line in fallback_data:
            data = fallback_data[verse_line]
            print(f"✅ 找到备用数据: {data['title']}")
            return data
        else:
            # 返回一个默认结构
            print("⚠️  未找到匹配的备用数据，使用默认结构")
            return {
                "title": "诗词查询结果",
                "author": "暂缺",
                "dynasty": "暂缺", 
                "full_text": [verse_line],
                "translation": ["译文获取中..."],
                "background": "正在通过AI分析诗词创作背景...",
                "difficult_words": [
                    {"word": "示例", "explanation": "这是示例解析，实际数据正在获取中"}
                ],
                "appreciation": "正在生成诗词整体鉴赏...",
                "celebrity_reviews": [
                    {"reviewer": "获取中", "review": "正在获取历史名人点评..."}
                ],
                "source_verse": verse_line
            }

def add_pinyin_to_verse(verse):
    """为诗句添加拼音"""
    try:
        pinyin_list = pinyin(verse, style=Style.NORMAL)
        pinyin_line = ' '.join([item[0] for item in pinyin_list])
        print(f"🔤 拼音转换: {verse} -> {pinyin_line}")
        return pinyin_line
    except Exception as e:
        print(f"❌ 拼音转换失败: {e}")
        return verse

@app.route('/')
def index():
    """首页"""
    print("🏠 访问首页")
    return render_template('index.html')

@app.route('/search', methods=['POST'])
def search_poetry():
    """搜索诗词"""
    verse_line = request.form.get('verse', '').strip()
    
    if not verse_line:
        print("❌ 空查询请求")
        return jsonify({'error': '请输入诗句'})
    
    print(f"📨 收到查询请求: {verse_line}")
    
    # 调用API查询
    api = PoetryAPI()
    poetry_data = api.query_poetry(verse_line)
    
    # 根据配置决定是否添加拼音
    enable_pinyin = app.config.get('ENABLE_PINYIN', False)
    print(f"🔤 拼音功能状态: {'启用' if enable_pinyin else '禁用'}")
    
    verses_with_pinyin = []
    for verse in poetry_data.get('full_text', []):
        verse_info = {
            'text': verse
        }
        if enable_pinyin:
            verse_info['pinyin'] = add_pinyin_to_verse(verse)
        else:
            verse_info['pinyin'] = ""  # 空字符串，前端不显示拼音
        
        verses_with_pinyin.append(verse_info)
    
    poetry_data['verses_with_pinyin'] = verses_with_pinyin
    poetry_data['pinyin_enabled'] = enable_pinyin  # 将拼音状态传递给前端
    
    print(f"✅ 返回诗词数据: {poetry_data['title']} - {poetry_data['author']}")
    
    return jsonify(poetry_data)

@app.route('/test')
def test():
    """测试页面"""
    print("🧪 访问测试页面")
    enable_pinyin = app.config.get('ENABLE_PINYIN', False)
    api_status = "✅ 已配置" if app.config.get('DEEPSEEK_API_KEY') != "your_deepseek_api_key_here" else "❌ 未配置"
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

@app.route('/toggle-pinyin')
def toggle_pinyin():
    """切换拼音功能状态（仅用于测试）"""
    current_state = app.config.get('ENABLE_PINYIN', False)
    new_state = not current_state
    app.config['ENABLE_PINYIN'] = new_state
    
    print(f"🔄 切换拼音功能: {'启用' if new_state else '禁用'}")
    return f"""
    <h1>拼音功能已{'启用' if new_state else '禁用'}</h1>
    <p><a href="/test">返回测试页面</a></p>
    <p><a href="/">返回首页</a></p>
    """

@app.route('/health')
def health_check():
    """健康检查端点"""
    api_status = "configured" if app.config.get('DEEPSEEK_API_KEY') != "your_deepseek_api_key_here" else "not_configured"
    pinyin_status = "enabled" if app.config.get('ENABLE_PINYIN', False) else "disabled"
    
    return jsonify({
        'status': 'healthy',
        'service': 'Chinese Poetry App',
        'version': '1.0',
        'api_key_status': api_status,
        'pinyin_enabled': pinyin_status
    })

if __name__ == '__main__':
    print("=" * 60)
    print("🎴 中国古诗词鉴赏应用启动中...")
    print("=" * 60)
    print("📍 访问地址: http://localhost:5000")
    print("🧪 测试页面: http://localhost:5000/test")
    print("❤️  健康检查: http://localhost:5000/health")
    print("=" * 60)
    
    # 检查API密钥配置
    api_key = app.config.get('DEEPSEEK_API_KEY')
    if api_key == "your_deepseek_api_key_here" or not api_key:
        print("⚠️  警告: 请在 config.py 中配置你的 DeepSeek API 密钥")
        print("📝 当前将使用备用数据进行演示")
    else:
        print(f"✅ API密钥已配置: ***{api_key[-4:] if len(api_key) > 4 else '***'}")
    
    # 检查拼音功能配置
    enable_pinyin = app.config.get('ENABLE_PINYIN', False)
    print(f"🔤 拼音功能: {'✅ 启用' if enable_pinyin else '❌ 禁用'}")
    
    print("=" * 60)
    
    app.run(debug=True, host='0.0.0.0', port=5000)