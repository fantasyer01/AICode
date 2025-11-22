"""
Pinyin Helper Utility
Provides pinyin conversion for Chinese poetry
"""

import logging
from pypinyin import pinyin, Style

logger = logging.getLogger(__name__)

def add_pinyin_to_verse(verse: str) -> str:
    """
    Add pinyin annotation to a verse
    
    Args:
        verse: Chinese verse text
        
    Returns:
        Pinyin representation of the verse
    """
    try:
        pinyin_list = pinyin(verse, style=Style.NORMAL)
        pinyin_line = ' '.join([item[0] for item in pinyin_list])
        logger.debug(f"拼音转换: {verse} -> {pinyin_line}")
        return pinyin_line
    except Exception as e:
        logger.error(f"拼音转换失败: {e}", exc_info=True)
        return verse
