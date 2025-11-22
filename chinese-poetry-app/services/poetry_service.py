"""
Poetry API Service
Handles all interactions with DeepSeek API for poetry queries
"""

import logging
import json
import re
import traceback
import requests
from typing import Dict, Any, Generator, Optional
from config import constants

logger = logging.getLogger(__name__)


class PoetryAPI:
    """Service class for poetry API interactions"""
    
    def __init__(self):
        """
        Initialize Poetry API service
        API key is loaded from config.constants.DEEPSEEK_API_KEY
        """
        self.api_key = constants.DEEPSEEK_API_KEY
        self.base_url = "https://api.deepseek.com/v1/chat/completions"
        
    def query_poetry(self, verse_line: str, stream_mode: bool) -> Any:
        """
        Query poetry information using DeepSeek API
        
        Args:
            verse_line: Input verse line to query
            stream_mode: Whether to use streaming mode
            
        Returns:
            Poetry data dict or generator for streaming
        """
        if not self.api_key or self.api_key == "your_deepseek_api_key_here":
            logger.warning("API密钥未配置，使用备用数据")
            if stream_mode:
                return self._get_fallback_markdown(verse_line)
            return self._get_fallback_data(verse_line)
            
        headers = {
            "Authorization": f"Bearer {self.api_key}",
            "Content-Type": "application/json"
        }
        
        # Build optimized prompt
        prompt = self._build_prompt(verse_line, stream_mode)
        
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
            "stream": stream_mode
        }
        
        # Add response_format only for non-streaming mode
        if not stream_mode:
            data["response_format"] = {
                "type": "json_object"
            }
        
        try:
            logger.info(f"正在查询诗句: {verse_line}")
            logger.debug(f"请求URL: {self.base_url}")
            logger.debug(f"请求头: Authorization: Bearer ***{self.api_key[-4:] if len(self.api_key) > 4 else '***'}")
            logger.debug(f"请求体: {json.dumps(data, ensure_ascii=False, indent=2)}")
            
            if stream_mode:
                # Streaming mode
                response = requests.post(
                    self.base_url,
                    headers=headers,
                    json=data,
                    timeout=300,
                    stream=True
                )
                return self._handle_stream_response(response, verse_line)
            else:
                # Non-streaming mode
                response = requests.post(
                    self.base_url,
                    headers=headers,
                    json=data,
                    timeout=300
                )
                return self._handle_json_response(response, verse_line)
        except requests.exceptions.Timeout:
            logger.error("API请求超时")
        except requests.exceptions.ConnectionError:
            logger.error("网络连接错误")
        except requests.exceptions.RequestException as e:
            logger.error(f"网络请求异常: {e}", exc_info=True)
        except Exception as e:
            logger.error(f"处理API响应时发生错误: {e}", exc_info=True)
        
        # If API call fails, return fallback data
        logger.info("使用备用数据")
        if stream_mode:
            return self._get_fallback_markdown(verse_line)
        return self._get_fallback_data(verse_line)    

    def _handle_json_response(self, response, verse_line: str) -> Any:

            logger.info(f"非流式API响应状态码: {response.status_code}")
            
            if response.status_code == 200:
                result = response.json()
                logger.info("API调用成功")
                
                # Log complete API response
                logger.debug("=" * 80)
                logger.debug("DeepSeek API 完整返回报文:")
                logger.debug("=" * 80)
                logger.debug(json.dumps(result, ensure_ascii=False, indent=2))
                logger.debug("=" * 80)
                
                # Parse response content
                if 'choices' in result and len(result['choices']) > 0:
                    content = result['choices'][0]['message']['content']
                    logger.debug(f"提取的返回内容长度: {len(content)} 字符")
                    
                    # Clean content and extract JSON
                    cleaned_content = content.strip()
                    json_match = re.search(r'\{.*\}', cleaned_content, re.DOTALL)
                    
                    if json_match:
                        try:
                            json_str = json_match.group()
                            poetry_data = json.loads(json_str)
                            logger.info("JSON解析成功")
                            validated_data = self._validate_poetry_data(poetry_data, verse_line)
                            return validated_data
                        except json.JSONDecodeError as e:
                            logger.error(f"JSON解析失败: {e}")
                            if json_match:
                                logger.debug(f"尝试解析的JSON字符串: {json_match.group()}")
                    else:
                        logger.error("未找到JSON格式内容")
                        logger.debug(f"原始返回内容: {cleaned_content}")
                else:
                    logger.error("API返回格式异常，缺少choices字段")
            else:
                logger.error(f"API请求失败: {response.status_code}")
                logger.error(f"错误响应: {response.text}")
                
    def _build_prompt(self, verse_line: str, stream_mode: bool) -> str:
        """Build prompt for API request from constants"""
        if stream_mode:
            return constants.STREAM_PROMPT_TEMPLATE.format(verse_line=verse_line)
        else:
            return constants.JSON_PROMPT_TEMPLATE.format(verse_line=verse_line)
    
    def _validate_poetry_data(self, data: Dict[str, Any], original_verse: str) -> Dict[str, Any]:
        """
        Validate and clean API returned data
        
        Args:
            data: Raw poetry data from API
            original_verse: Original query verse
            
        Returns:
            Validated and cleaned poetry data
        """
        logger.debug("验证和清理数据...")
        
        required_fields = ['title', 'author', 'dynasty', 'full_text', 'translation', 
                          'background', 'difficult_words', 'appreciation', 'celebrity_reviews']
        
        for field in required_fields:
            if field not in data:
                if field in ['difficult_words', 'celebrity_reviews']:
                    data[field] = []
                    logger.warning(f"缺失字段 {field}，已设置为空列表")
                elif field == 'translation':
                    data[field] = []
                    logger.warning(f"缺失字段 {field}，已设置为空列表")
                else:
                    data[field] = "信息暂缺"
                    logger.warning(f"缺失字段 {field}，已设置默认值")
        
        # Ensure source_verse field exists
        data['source_verse'] = original_verse
        
        # Ensure full_text and translation are lists
        if isinstance(data['full_text'], str):
            data['full_text'] = [data['full_text']]
            logger.debug("将full_text从字符串转换为列表")
        
        if isinstance(data.get('translation'), str):
            data['translation'] = [data['translation']]
            logger.debug("将translation从字符串转换为列表")
        
        logger.info(f"数据验证完成: {data['title']} - {data['author']}")
        return data
    
    def _get_fallback_data(self, verse_line: str) -> Dict[str, Any]:
        """
        Get fallback data when API is unavailable
        
        Args:
            verse_line: Input verse line
            
        Returns:
            Fallback poetry data
        """
        logger.debug(f"使用备用数据 for: {verse_line}")
        
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
            }
        }
        
        if verse_line in fallback_data:
            data = fallback_data[verse_line]
            logger.info(f"找到备用数据: {data['title']}")
            return data
        else:
            # Return default structure
            logger.warning("未找到匹配的备用数据，使用默认结构")
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
    
    def _handle_stream_response(self, response, verse_line: str) -> Generator:
        """
        Handle streaming response from API
        
        Args:
            response: HTTP response object
            verse_line: Input verse line
            
        Returns:
            Generator yielding streaming data
        """
        logger.info(f"流式API响应状态码: {response.status_code}")
        
        if response.status_code != 200:
            logger.error(f"API请求失败: {response.status_code}")
            return self._get_fallback_markdown(verse_line)
        
        def generate():
            try:
                logger.info("开始处理流式数据...")
                chunk_count = 0
                for line in response.iter_lines():
                    if line:
                        chunk_count += 1
                        line_str = line.decode('utf-8')
                        
                        # Log raw response data
                        if constants.IS_OUTPUT_STREAM_LOG:
                            logger.debug(f"流式数据块 {chunk_count}: {line_str}")
                        
                        if line_str.startswith('data: '):
                            data_str = line_str[6:]
                            if data_str.strip() == '[DONE]':
                                logger.info(f"流式传输完成，共处理 {chunk_count} 个数据块")
                                break
                            try:
                                chunk_data = json.loads(data_str)
                                if 'choices' in chunk_data and len(chunk_data['choices']) > 0:
                                    delta = chunk_data['choices'][0].get('delta', {})
                                    content = delta.get('content', '')
                                    if content:
                                        yield f"data: {json.dumps({'content': content}, ensure_ascii=False)}\n\n"
                            except json.JSONDecodeError as e:
                                logger.warning(f"JSON解析错误 (块 {chunk_count}): {e}, 数据: {data_str[:100]}...")
                                continue
                        else:
                            # Log non-standard format lines
                            if line_str.strip() and not line_str.startswith(':'):
                                logger.debug(f"非标准SSE格式 (块 {chunk_count}): {line_str[:100]}...")
            except requests.exceptions.ChunkedEncodingError as e:
                logger.error(f"数据块编码错误: {e}", exc_info=True)
                yield f"data: {json.dumps({'error': '流式数据传输中断，请重试'}, ensure_ascii=False)}\n\n"
            except requests.exceptions.ConnectionError as e:
                logger.error(f"连接错误: {e}", exc_info=True)
                yield f"data: {json.dumps({'error': '网络连接中断'}, ensure_ascii=False)}\n\n"
            except Exception as e:
                logger.error(f"流式处理错误: {type(e).__name__}: {e}", exc_info=True)
                traceback.print_exc()
                yield f"data: {json.dumps({'error': f'流式处理异常: {str(e)}'}, ensure_ascii=False)}\n\n"
        
        return generate()
    
    def _get_fallback_markdown(self, verse_line: str) -> Generator:
        """
        Get fallback markdown data when API is unavailable
        
        Args:
            verse_line: Input verse line
            
        Returns:
            Generator yielding markdown content
        """
        logger.debug(f"使用备用Markdown数据 for: {verse_line}")
        
        fallback_markdown = {
            "床前明月光": """
# 《静夜思》
**作者**: 李白 · 唐代

## 📜 完整诗词
床前明月光（chuáng qián míng yuè guāng）  
疑是地上霜（yí shì dì shàng shuāng）  
举头望明月（jǔ tóu wàng míng yuè）  
低头思故乡（dī tóu sī gù xiāng）

## 💬 译文
明亮的月光洒在床前的窗户纸上  
好像地上泛起了一层白霜  
抬起头来看那天窗外空中的明月  
不由得低头沉思，想起远方的家乡

## 📖 创作背景
这首诗创作于唐玄宗开元十四年（726年），当时李白约25岁，寓居扬州旅舍。在一个月明星稀的夜晚，诗人望见秋月，思乡之情油然而生，写下了这首传诵千古、中外皆知的名诗。

## 🔍 疑难字词解析
- **疑**: 好像，似乎。生动地写出了诗人睡梦初醒，恍惚中将照射在床前的清冷月光误作铺在地面的浓霜。
- **举头**: 抬头。一个简单的动作，真切地描绘出诗人被月光吸引，不自觉地向窗外望去的神态。
- **思故乡**: 思念家乡。直接点明了诗歌的核心情感——乡愁。

## 🎨 整体鉴赏
《静夜思》语言清新朴素，明白如话，内容单纯却又韵味无穷。诗人通过"明月光"、"地上霜"、"举头"、"低头"等一系列动作和景物的白描，鲜明地勾勒出一幅生动的月夜思乡图。全诗从"疑"到"望"再到"思"，形象地揭示了诗人的内心活动，巧妙地表达了旅居思乡的孤寂情怀。其构思细致而深曲，却又不假雕琢，浑然天成，千百年来广泛吸引着读者，成为了中华文化中思乡符号的代表。

## ⭐ 名人点评
- **明代胡应麟**: 太白诸绝句，信口而成，所谓无意于工而无不工者。此诗写月夜思乡，千古传诵。
- **清代王夫之**: 以景寓情，浑然天成，读来如见故乡。通篇不着一个'思'字，而思乡之情溢于言表。
- **现代郭沫若**: 此诗平淡自然，不事雕琢，却能打动千古读者之心，足见李白天才之高妙。
"""
        }
        
        markdown_content = fallback_markdown.get(verse_line, f"""
# 《诗词查询结果》
**作者**: 暂缺 · 暂缺

## 📜 完整诗词
{verse_line}

## 💬 译文
译文获取中...

## 📖 创作背景
正在通过AI分析诗词创作背景...

## 🔍 疑难字词解析
- **示例**: 这是示例解析，实际数据正在获取中

## 🎨 整体鉴赏
正在生成诗词整体鉴赏...

## ⭐ 名人点评
- **获取中**: 正在获取历史名人点评...
""")
        
        def generate():
            # Simulate streaming output
            for char in markdown_content:
                yield f"data: {json.dumps({'content': char}, ensure_ascii=False)}\n\n"
        
        return generate()
