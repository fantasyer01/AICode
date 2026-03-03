"""
Image Service
Handles poem-related image lookup, generation via kie.ai API, and local caching
"""

import glob
import json
import logging
import os
import re
from datetime import datetime
from urllib.parse import quote

import requests

from config import constants

logger = logging.getLogger(__name__)


class ImageService:
    """Service class for poem image generation and caching"""

    def __init__(self):
        self.api_key = constants.KIE_API_KEY
        self.base_url = constants.KIE_API_BASE_URL
        self.storage_dir = constants.IMAGE_STORAGE_DIR
        os.makedirs(self.storage_dir, exist_ok=True)

    def get_or_generate_image(self, title: str, author: str, dynasty: str, verse_line: str) -> dict:
        """
        Look up a cached image or initiate generation via kie.ai.

        Returns:
            dict with keys: status, url (optional), taskId (optional), message (optional)
        """
        if not self.api_key:
            logger.info("KIE_API_KEY 未配置，图片功能已禁用")
            return {"status": "disabled", "message": "图片功能未启用"}

        # Check local cache first
        existing_url = self._find_existing_image(title, author)
        if existing_url:
            logger.info(f"找到本地缓存图片: {existing_url}")
            return {"status": "found", "url": existing_url}

        # No local cache — create generation task
        result = self._create_kie_task(title, author, dynasty, verse_line)
        if result.get("error"):
            return {"status": "error", "message": result["error"]}

        logger.info(f"图片生成任务已创建, taskId: {result['taskId']}")
        return {"status": "generating", "taskId": result["taskId"]}

    def check_task_status(self, task_id: str) -> dict:
        """
        Poll kie.ai for the generation task status.

        Returns:
            dict with keys: status (waiting|success|error), url (optional), message (optional)
        """
        if not task_id:
            return {"status": "error", "message": "taskId 不能为空"}

        try:
            url = f"{self.base_url}/recordInfo?taskId={task_id}"
            headers = {"Authorization": f"Bearer {self.api_key}"}
            response = requests.get(url, headers=headers, timeout=30)

            if response.status_code != 200:
                logger.error(f"查询任务状态失败, HTTP {response.status_code}: {response.text}")
                return {"status": "error", "message": f"查询失败: HTTP {response.status_code}"}

            data = response.json()
            if data.get("code") != 200:
                logger.error(f"查询任务状态API错误: {data}")
                return {"status": "error", "message": data.get("msg", "API错误")}

            task_data = data.get("data", {})
            state = task_data.get("state", "")

            if state == "waiting":
                return {"status": "waiting", "message": "图片生成中..."}

            if state == "success":
                result_json_str = task_data.get("resultJson", "{}")
                try:
                    result_json = json.loads(result_json_str)
                except json.JSONDecodeError:
                    logger.error(f"解析 resultJson 失败: {result_json_str}")
                    return {"status": "error", "message": "图片结果解析失败"}

                result_urls = result_json.get("resultUrls", [])
                if not result_urls:
                    return {"status": "error", "message": "未获取到图片URL"}

                # Download and save locally
                saved_url = self._download_and_save_image(
                    result_urls,
                    task_data.get("_title", ""),
                    task_data.get("_author", "")
                )
                if saved_url:
                    return {"status": "success", "url": saved_url}
                return {"status": "error", "message": "图片下载保存失败"}

            if state == "fail":
                fail_msg = task_data.get("failMsg", "未知错误")
                logger.error(f"图片生成失败: {fail_msg}")
                return {"status": "error", "message": f"生成失败: {fail_msg}"}

            return {"status": "waiting", "message": f"当前状态: {state}"}

        except requests.exceptions.RequestException as e:
            logger.error(f"查询任务状态网络异常: {e}")
            return {"status": "error", "message": "网络请求失败"}

    def check_task_status_with_meta(self, task_id: str, title: str, author: str) -> dict:
        """
        Poll kie.ai task status, using provided title/author for saving the image.
        """
        if not task_id:
            return {"status": "error", "message": "taskId 不能为空"}

        if not self.api_key:
            return {"status": "error", "message": "图片功能未启用"}

        # Check if image already exists locally (avoid duplicate downloads from polling)
        existing_url = self._find_existing_image(title, author)
        if existing_url:
            logger.info(f"轮询时发现本地已存在图片: {existing_url}")
            return {"status": "success", "url": existing_url}

        try:
            url = f"{self.base_url}/recordInfo?taskId={task_id}"
            headers = {"Authorization": f"Bearer {self.api_key}"}
            response = requests.get(url, headers=headers, timeout=30)

            if response.status_code != 200:
                logger.error(f"查询任务状态失败, HTTP {response.status_code}: {response.text}")
                return {"status": "error", "message": f"查询失败: HTTP {response.status_code}"}

            data = response.json()
            if data.get("code") != 200:
                logger.error(f"查询任务状态API错误: {data}")
                return {"status": "error", "message": data.get("msg", "API错误")}

            task_data = data.get("data", {})
            state = task_data.get("state", "")

            if state == "waiting":
                return {"status": "waiting", "message": "图片生成中..."}

            if state == "success":
                # Double-check local cache before downloading (race condition protection)
                existing_url = self._find_existing_image(title, author)
                if existing_url:
                    logger.info(f"下载前检查发现本地已存在图片: {existing_url}")
                    return {"status": "success", "url": existing_url}

                result_json_str = task_data.get("resultJson", "{}")
                try:
                    result_json = json.loads(result_json_str)
                except json.JSONDecodeError:
                    logger.error(f"解析 resultJson 失败: {result_json_str}")
                    return {"status": "error", "message": "图片结果解析失败"}

                result_urls = result_json.get("resultUrls", [])
                if not result_urls:
                    return {"status": "error", "message": "未获取到图片URL"}

                saved_url = self._download_and_save_image(result_urls, title, author)
                if saved_url:
                    return {"status": "success", "url": saved_url}
                return {"status": "error", "message": "图片下载保存失败"}

            if state == "fail":
                fail_msg = task_data.get("failMsg", "未知错误")
                logger.error(f"图片生成失败: {fail_msg}")
                return {"status": "error", "message": f"生成失败: {fail_msg}"}

            return {"status": "waiting", "message": f"当前状态: {state}"}

        except requests.exceptions.RequestException as e:
            logger.error(f"查询任务状态网络异常: {e}")
            return {"status": "error", "message": "网络请求失败"}

    # ------------------------------------------------------------------
    # Private helpers
    # ------------------------------------------------------------------

    def _find_existing_image(self, title: str, author: str) -> str | None:
        """Search static/images/ for an existing image matching title_author prefix."""
        safe_title = self._sanitize_filename(title)
        safe_author = self._sanitize_filename(author)
        pattern = os.path.join(self.storage_dir, f"{safe_title}_{safe_author}_*.{constants.IMAGE_OUTPUT_FORMAT}")

        matches = glob.glob(pattern)
        if not matches:
            return None

        # Return the most recent file (highest timestamp in filename)
        matches.sort(reverse=True)
        best = matches[0]
        # Return filename directly - Flask and browsers handle UTF-8 URLs properly
        filename = os.path.basename(best)
        # URL-encode the filename for proper browser handling of Chinese characters
        encoded_filename = quote(filename, safe='')
        return f"/static/images/{encoded_filename}"

    def _sanitize_filename(self, text: str) -> str:
        """Remove characters that are unsafe for filenames."""
        # Remove common brackets and punctuation
        text = re.sub(r'[《》<>:"/\\|?*\[\]()（）\s]', '', text)
        # Truncate to 50 characters
        return text[:50]

    def _create_kie_task(self, title: str, author: str, dynasty: str, verse_line: str) -> dict:
        """Create an image generation task via kie.ai API."""
        prompt = constants.IMAGE_PROMPT_TEMPLATE.format(
            title=title,
            author=author,
            verse_line=verse_line,
        )

        payload = {
            "model": "nano-banana-pro",
            "input": {
                "prompt": prompt,
                "aspect_ratio": constants.IMAGE_ASPECT_RATIO,
                "resolution": constants.IMAGE_RESOLUTION,
                "output_format": constants.IMAGE_OUTPUT_FORMAT,
            },
        }

        headers = {
            "Authorization": f"Bearer {self.api_key}",
            "Content-Type": "application/json",
        }

        try:
            logger.info(f"创建图片生成任务, 诗词: {title}")
            logger.debug(f"kie.ai 请求体: {json.dumps(payload, ensure_ascii=False)}")

            response = requests.post(
                f"{self.base_url}/createTask",
                headers=headers,
                json=payload,
                timeout=30,
            )

            if response.status_code != 200:
                logger.error(f"创建任务失败, HTTP {response.status_code}: {response.text}")
                return {"error": f"创建任务失败: HTTP {response.status_code}"}

            data = response.json()
            if data.get("code") != 200:
                logger.error(f"创建任务API错误: {data}")
                return {"error": data.get("msg", "API错误")}

            task_id = data.get("data", {}).get("taskId")
            if not task_id:
                return {"error": "未获取到 taskId"}

            return {"taskId": task_id}

        except requests.exceptions.RequestException as e:
            logger.error(f"创建任务网络异常: {e}")
            return {"error": f"网络请求失败: {e}"}

    def _download_and_save_image(self, result_urls: list, title: str, author: str) -> str | None:
        """Download generated image and save to local storage."""
        if not result_urls:
            return None

        image_url = result_urls[0]
        try:
            logger.info(f"正在下载图片: {image_url[:80]}...")
            resp = requests.get(image_url, timeout=60)
            if resp.status_code != 200:
                logger.error(f"图片下载失败, HTTP {resp.status_code}")
                return None

            safe_title = self._sanitize_filename(title)
            safe_author = self._sanitize_filename(author)
            # Use YYYYMMDDHHMMSS format for timestamp
            timestamp = datetime.now().strftime('%Y%m%d%H%M%S')
            filename = f"{safe_title}_{safe_author}_{timestamp}.{constants.IMAGE_OUTPUT_FORMAT}"
            filepath = os.path.join(self.storage_dir, filename)

            with open(filepath, 'wb') as f:
                f.write(resp.content)

            logger.info(f"图片已保存: {filepath}")
            # URL-encode the filename for proper browser handling of Chinese characters
            encoded_filename = quote(filename, safe='')
            return f"/static/images/{encoded_filename}"

        except requests.exceptions.RequestException as e:
            logger.error(f"图片下载异常: {e}")
            return None
        except OSError as e:
            logger.error(f"图片保存异常: {e}")
            return None
