// CommandHub Frontend Application
(function () {
    'use strict';

    // --- State ---
    let token = localStorage.getItem('ch_token') || '';
    let isAdmin = false;
    let allTags = [];
    let selectedPlatform = '';
    let selectedTags = [];
    let keyword = '';
    let currentPage = 1;
    const pageSize = 20;

    // --- DOM refs ---
    const $ = (sel) => document.querySelector(sel);
    const $$ = (sel) => document.querySelectorAll(sel);

    // --- API helpers ---
    async function api(method, path, body) {
        const opts = {
            method,
            headers: { 'Content-Type': 'application/json' }
        };
        if (token) opts.headers['Authorization'] = 'Bearer ' + token;
        if (body) opts.body = JSON.stringify(body);
        const res = await fetch(path, opts);
        return res.json();
    }

    // --- Toast ---
    function showToast(msg, duration = 1500) {
        const toast = $('#toast');
        const toastMsg = $('#toastMsg');
        toastMsg.textContent = msg;
        toast.classList.remove('hidden');
        toast.firstElementChild.classList.remove('toast-exit');
        toast.firstElementChild.classList.add('toast-enter');
        setTimeout(() => {
            toast.firstElementChild.classList.remove('toast-enter');
            toast.firstElementChild.classList.add('toast-exit');
            setTimeout(() => toast.classList.add('hidden'), 300);
        }, duration);
    }

    // --- Theme ---
    function initTheme() {
        const saved = localStorage.getItem('ch_theme');
        if (saved === 'dark' || (!saved && window.matchMedia('(prefers-color-scheme: dark)').matches)) {
            document.documentElement.classList.add('dark');
        }
        $('#themeToggle').addEventListener('click', () => {
            document.documentElement.classList.toggle('dark');
            localStorage.setItem('ch_theme', document.documentElement.classList.contains('dark') ? 'dark' : 'light');
        });
    }

    // --- Auth ---
    async function checkAuth() {
        if (!token) { setAdmin(false); return; }
        const res = await api('GET', '/api/auth/status');
        setAdmin(res.code === 200 && res.data && res.data.authenticated);
    }

    function setAdmin(val) {
        isAdmin = val;
        $('#loginBtn').classList.toggle('hidden', isAdmin);
        $('#logoutBtn').classList.toggle('hidden', !isAdmin);
        $('#adminBar').classList.toggle('hidden', !isAdmin);
        // Re-render to show/hide edit/delete buttons
        renderTagFilter();
        loadCommands();
    }

    function initAuth() {
        $('#loginBtn').addEventListener('click', () => {
            $('#loginModal').classList.remove('hidden');
            $('#passwordInput').value = '';
            $('#loginError').classList.add('hidden');
            setTimeout(() => $('#passwordInput').focus(), 100);
        });

        $('#loginCancelBtn').addEventListener('click', () => $('#loginModal').classList.add('hidden'));

        $('#loginSubmitBtn').addEventListener('click', doLogin);
        $('#passwordInput').addEventListener('keydown', (e) => { if (e.key === 'Enter') doLogin(); });

        $('#logoutBtn').addEventListener('click', () => {
            token = '';
            localStorage.removeItem('ch_token');
            setAdmin(false);
            showToast('已退出登录');
        });
    }

    async function doLogin() {
        const pw = $('#passwordInput').value.trim();
        if (!pw) return;
        const res = await api('POST', '/api/auth/login', { password: pw });
        if (res.code === 200 && res.data && res.data.token) {
            token = res.data.token;
            localStorage.setItem('ch_token', token);
            $('#loginModal').classList.add('hidden');
            setAdmin(true);
            showToast('登录成功');
        } else {
            $('#loginError').textContent = res.message || '登录失败';
            $('#loginError').classList.remove('hidden');
        }
    }

    // --- Tags ---
    async function loadTags() {
        const res = await api('GET', '/api/tags');
        if (res.code === 200) {
            allTags = res.data || [];
            renderTagFilter();
        }
    }

    function renderTagFilter() {
        const container = $('#tagFilter');
        container.innerHTML = '';
        allTags.forEach(tag => {
            const wrapper = document.createElement('span');
            wrapper.className = 'tag-filter-wrapper';
            wrapper.style.cssText = 'display:inline-flex;align-items:center;position:relative;';

            const btn = document.createElement('button');
            btn.className = 'tag-filter-btn' + (selectedTags.includes(tag) ? ' active' : '');
            btn.textContent = tag;
            if (isAdmin) btn.style.paddingRight = '1.5rem';
            btn.addEventListener('click', () => {
                const idx = selectedTags.indexOf(tag);
                if (idx >= 0) selectedTags.splice(idx, 1);
                else selectedTags.push(tag);
                currentPage = 1;
                renderTagFilter();
                loadCommands();
            });
            wrapper.appendChild(btn);

            if (isAdmin) {
                const delBtn = document.createElement('span');
                delBtn.className = 'tag-del-btn';
                delBtn.innerHTML = '&times;';
                delBtn.title = '删除标签';
                delBtn.addEventListener('click', (e) => {
                    e.stopPropagation();
                    if (confirm('确定要删除标签「' + tag + '」吗？\n（已有命令上的该标签不会被移除）')) {
                        deleteTag(tag);
                    }
                });
                wrapper.appendChild(delBtn);
            }

            container.appendChild(wrapper);
        });
    }

    async function deleteTag(tagName) {
        const res = await api('DELETE', '/api/tags/' + encodeURIComponent(tagName));
        if (res.code === 200) {
            selectedTags = selectedTags.filter(t => t !== tagName);
            showToast('标签「' + tagName + '」已删除');
            await loadTags();
            loadCommands();
        } else {
            showToast(res.message || '删除失败');
        }
    }

    // --- Platform filter ---
    function initPlatformFilter() {
        $$('#platformFilter button').forEach(btn => {
            btn.addEventListener('click', () => {
                selectedPlatform = btn.dataset.platform;
                currentPage = 1;
                $$('#platformFilter button').forEach(b => b.classList.remove('active'));
                btn.classList.add('active');
                loadCommands();
            });
        });
    }

    // --- Search ---
    function initSearch() {
        let timer;
        $('#searchInput').addEventListener('input', (e) => {
            clearTimeout(timer);
            timer = setTimeout(() => {
                keyword = e.target.value.trim();
                currentPage = 1;
                loadCommands();
            }, 300);
        });
    }

    // --- Commands ---
    async function loadCommands() {
        const params = new URLSearchParams();
        if (keyword) params.set('keyword', keyword);
        if (selectedPlatform) params.set('platform', selectedPlatform);
        if (selectedTags.length) params.set('tags', selectedTags.join(','));
        params.set('page', currentPage);
        params.set('size', pageSize);

        const res = await api('GET', '/api/commands?' + params.toString());
        if (res.code === 200) {
            renderCommands(res.data);
            renderPagination(res.data);
        }
    }

    function renderCommands(data) {
        const list = $('#commandList');
        const empty = $('#emptyState');
        const commands = data.content || [];

        if (commands.length === 0) {
            list.innerHTML = '';
            empty.classList.remove('hidden');
            return;
        }
        empty.classList.add('hidden');

        list.innerHTML = commands.map(cmd => {
            const platformLabel = { windows: 'Windows', linux: 'Linux', common: '通用' }[cmd.platform] || cmd.platform;
            const tagsHtml = (cmd.tags || []).map(t => `<span class="tag-badge">${esc(t)}</span>`).join('');
            const adminBtns = isAdmin ? `
                <button class="edit-cmd-btn text-sm text-brand-500 hover:text-brand-600 font-medium transition-colors" data-id="${cmd.id}">
                    <svg class="w-4 h-4 inline -mt-0.5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"/></svg>
                    编辑
                </button>
                <button class="del-cmd-btn text-sm text-red-500 hover:text-red-600 font-medium transition-colors" data-id="${cmd.id}">
                    <svg class="w-4 h-4 inline -mt-0.5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"/></svg>
                    删除
                </button>
            ` : '';

            return `
            <div class="cmd-card">
                <div class="flex items-start justify-between gap-3 mb-3">
                    <div class="flex items-center gap-2 flex-wrap">
                        <h3 class="font-semibold text-gray-900 dark:text-white text-base">${esc(cmd.title)}</h3>
                        <span class="platform-badge ${cmd.platform}">${esc(platformLabel)}</span>
                        ${tagsHtml}
                    </div>
                    <div class="flex items-center gap-3 shrink-0">
                        ${adminBtns}
                    </div>
                </div>
                <div class="code-block">${esc(cmd.command)}<button class="copy-btn" data-cmd="${escAttr(cmd.command)}"><svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 16H6a2 2 0 01-2-2V6a2 2 0 012-2h8a2 2 0 012 2v2m-6 12h8a2 2 0 002-2v-8a2 2 0 00-2-2h-8a2 2 0 00-2 2v8a2 2 0 002 2z"/></svg>复制</button></div>
                ${cmd.description ? `<p class="mt-3 text-sm text-gray-500 dark:text-gray-400 leading-relaxed">${escBr(cmd.description)}</p>` : ''}
            </div>`;
        }).join('');

        // Bind copy buttons
        list.querySelectorAll('.copy-btn').forEach(btn => {
            btn.addEventListener('click', () => {
                navigator.clipboard.writeText(btn.dataset.cmd).then(() => {
                    btn.classList.add('copied');
                    btn.innerHTML = '<svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"/></svg>已复制';
                    showToast('命令已复制到剪贴板');
                    setTimeout(() => {
                        btn.classList.remove('copied');
                        btn.innerHTML = '<svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 16H6a2 2 0 01-2-2V6a2 2 0 012-2h8a2 2 0 012 2v2m-6 12h8a2 2 0 002-2v-8a2 2 0 00-2-2h-8a2 2 0 00-2 2v8a2 2 0 002 2z"/></svg>复制';
                    }, 2000);
                });
            });
        });

        // Bind edit buttons
        list.querySelectorAll('.edit-cmd-btn').forEach(btn => {
            btn.addEventListener('click', () => openEditModal(btn.dataset.id));
        });

        // Bind delete buttons
        list.querySelectorAll('.del-cmd-btn').forEach(btn => {
            btn.addEventListener('click', () => openDeleteModal(btn.dataset.id));
        });
    }

    function renderPagination(data) {
        const container = $('#pagination');
        if (data.totalPages <= 1) { container.innerHTML = ''; return; }

        let html = '';
        html += `<button class="page-btn" ${data.page <= 1 ? 'disabled' : ''} data-page="${data.page - 1}">&larr; 上一页</button>`;

        // Show page numbers
        const start = Math.max(1, data.page - 2);
        const end = Math.min(data.totalPages, data.page + 2);
        for (let i = start; i <= end; i++) {
            html += `<button class="page-btn ${i === data.page ? 'active' : ''}" data-page="${i}">${i}</button>`;
        }

        html += `<button class="page-btn" ${data.page >= data.totalPages ? 'disabled' : ''} data-page="${data.page + 1}">下一页 &rarr;</button>`;
        container.innerHTML = html;

        container.querySelectorAll('.page-btn').forEach(btn => {
            btn.addEventListener('click', () => {
                if (btn.disabled) return;
                currentPage = parseInt(btn.dataset.page);
                loadCommands();
                window.scrollTo({ top: 0, behavior: 'smooth' });
            });
        });
    }

    // --- Edit Modal ---
    function initEditModal() {
        $('#addCmdBtn').addEventListener('click', () => {
            openEditModal(null);
        });
        $('#editCancelBtn').addEventListener('click', () => $('#editModal').classList.add('hidden'));
        $('#editSaveBtn').addEventListener('click', saveCommand);
        $('#addTagBtn').addEventListener('click', addNewTag);
        $('#newTagInput').addEventListener('keydown', (e) => { if (e.key === 'Enter') addNewTag(); });
    }

    async function openEditModal(id) {
        const modal = $('#editModal');
        const title = $('#editModalTitle');
        let editTags = [];

        if (id) {
            title.textContent = '编辑命令';
            const res = await api('GET', '/api/commands/' + id);
            if (res.code !== 200) { showToast('加载失败'); return; }
            const cmd = res.data;
            $('#editId').value = cmd.id;
            $('#editTitle').value = cmd.title || '';
            $('#editCommand').value = cmd.command || '';
            $('#editPlatform').value = cmd.platform || 'windows';
            $('#editDescription').value = cmd.description || '';
            editTags = cmd.tags || [];
        } else {
            title.textContent = '新增命令';
            $('#editId').value = '';
            $('#editTitle').value = '';
            $('#editCommand').value = '';
            $('#editPlatform').value = 'windows';
            $('#editDescription').value = '';
            editTags = [];
        }

        renderEditTags(editTags);
        modal.classList.remove('hidden');
        setTimeout(() => $('#editTitle').focus(), 100);
    }

    function renderEditTags(selected) {
        const container = $('#editTagList');
        const merged = [...new Set([...allTags, ...selected])];
        container.innerHTML = merged.map(tag => {
            const isSelected = selected.includes(tag);
            return `<span class="edit-tag-item ${isSelected ? 'selected' : ''}" data-tag="${esc(tag)}">${esc(tag)}</span>`;
        }).join('');

        container.querySelectorAll('.edit-tag-item').forEach(el => {
            el.addEventListener('click', () => el.classList.toggle('selected'));
        });
    }

    function addNewTag() {
        const input = $('#newTagInput');
        const tag = input.value.trim();
        if (!tag) return;

        const container = $('#editTagList');
        const existing = container.querySelector(`[data-tag="${tag}"]`);
        if (existing) {
            existing.classList.add('selected');
        } else {
            const span = document.createElement('span');
            span.className = 'edit-tag-item selected';
            span.dataset.tag = tag;
            span.textContent = tag;
            span.addEventListener('click', () => span.classList.toggle('selected'));
            container.appendChild(span);
        }
        input.value = '';
    }

    async function saveCommand() {
        const id = $('#editId').value;
        const titleVal = $('#editTitle').value.trim();
        const commandVal = $('#editCommand').value.trim();

        if (!titleVal || !commandVal) {
            showToast('标题和命令不能为空');
            return;
        }

        const selectedEditTags = [];
        $$('#editTagList .edit-tag-item.selected').forEach(el => selectedEditTags.push(el.dataset.tag));

        const body = {
            title: titleVal,
            command: commandVal,
            platform: $('#editPlatform').value,
            tags: selectedEditTags,
            description: $('#editDescription').value.trim()
        };

        let res;
        if (id) {
            res = await api('PUT', '/api/commands/' + id, body);
        } else {
            res = await api('POST', '/api/commands', body);
        }

        if (res.code === 200) {
            $('#editModal').classList.add('hidden');
            showToast(id ? '命令已更新' : '命令已添加');
            loadTags();
            loadCommands();
        } else {
            showToast(res.message || '操作失败');
        }
    }

    // --- Delete Modal ---
    function initDeleteModal() {
        $('#deleteCancelBtn').addEventListener('click', () => $('#deleteModal').classList.add('hidden'));
        $('#deleteConfirmBtn').addEventListener('click', confirmDelete);
    }

    function openDeleteModal(id) {
        $('#deleteId').value = id;
        $('#deleteModal').classList.remove('hidden');
    }

    async function confirmDelete() {
        const id = $('#deleteId').value;
        const res = await api('DELETE', '/api/commands/' + id);
        $('#deleteModal').classList.add('hidden');
        if (res.code === 200) {
            showToast('命令已删除');
            loadCommands();
        } else {
            showToast(res.message || '删除失败');
        }
    }

    // --- Helpers ---
    function esc(str) {
        if (!str) return '';
        return str.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;');
    }

    function escAttr(str) {
        if (!str) return '';
        return str.replace(/&/g, '&amp;').replace(/"/g, '&quot;').replace(/'/g, '&#39;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
    }

    function escBr(str) {
        if (!str) return '';
        return esc(str).replace(/\n/g, '<br>');
    }

    // --- Init ---
    async function init() {
        initTheme();
        initAuth();
        initPlatformFilter();
        initSearch();
        initEditModal();
        initDeleteModal();
        await loadTags();
        await checkAuth();
        if (!isAdmin) {
            // checkAuth calls loadCommands via setAdmin, but if not admin on first load:
            await loadCommands();
        }
    }

    // Close modals on overlay click
    document.addEventListener('click', (e) => {
        if (e.target.classList.contains('modal-overlay')) {
            e.target.classList.add('hidden');
        }
    });

    // Close modals on Escape
    document.addEventListener('keydown', (e) => {
        if (e.key === 'Escape') {
            $$('.modal-overlay').forEach(m => m.classList.add('hidden'));
        }
    });

    init();
})();
