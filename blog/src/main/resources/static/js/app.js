document.addEventListener('DOMContentLoaded', function () {
    // Initialize highlight.js
    if (typeof hljs !== 'undefined') {
        document.querySelectorAll('.article-content pre code').forEach(function (block) {
            hljs.highlightElement(block);
        });
    }

    // Cover image fade-in on load
    document.querySelectorAll('.cover-image').forEach(function (img) {
        if (img.complete && img.naturalHeight > 0) {
            img.classList.add('loaded');
        } else {
            img.addEventListener('load', function () {
                img.classList.add('loaded');
            });
            img.addEventListener('error', function () {
                img.classList.add('loaded');
            });
        }
    });

    // Wrap tables in scrollable container for mobile
    document.querySelectorAll('.article-content table').forEach(function (table) {
        if (!table.parentElement.classList.contains('table-wrapper')) {
            var wrapper = document.createElement('div');
            wrapper.className = 'table-wrapper';
            table.parentNode.insertBefore(wrapper, table);
            wrapper.appendChild(table);
        }
    });

    // Add copy button to code blocks
    document.querySelectorAll('.article-content pre').forEach(function (pre) {
        var wrapper = document.createElement('div');
        wrapper.className = 'code-block-wrapper';
        pre.parentNode.insertBefore(wrapper, pre);
        wrapper.appendChild(pre);

        var btn = document.createElement('button');
        btn.className = 'copy-code-btn';
        btn.textContent = 'Copy';
        btn.addEventListener('click', function () {
            var code = pre.querySelector('code');
            var text = code ? code.textContent : pre.textContent;
            navigator.clipboard.writeText(text).then(function () {
                btn.textContent = 'Copied!';
                setTimeout(function () {
                    btn.textContent = 'Copy';
                }, 2000);
            });
        });
        wrapper.appendChild(btn);
    });
});

// Toggle raw content visibility for snippets
function toggleRawContent(snippetId) {
    var rawContentDiv = document.getElementById('raw-content-' + snippetId);
    var toggleLink = document.getElementById('toggle-link-' + snippetId);

    if (rawContentDiv.classList.contains('hidden')) {
        rawContentDiv.classList.remove('hidden');
        toggleLink.textContent = '收起原始内容';
    } else {
        rawContentDiv.classList.add('hidden');
        toggleLink.textContent = '原始内容';
    }
}

// Copy prompt content to clipboard
function copyPromptContent() {
    var promptEl = document.getElementById('promptContent');
    var btn = document.getElementById('copyBtn');
    if (!promptEl || !btn) return;

    var text = promptEl.textContent || promptEl.innerText;
    navigator.clipboard.writeText(text).then(function () {
        btn.textContent = 'Copied!';
        setTimeout(function () {
            btn.textContent = 'Copy';
        }, 2000);
    }).catch(function () {
        // Fallback for older browsers
        var textarea = document.createElement('textarea');
        textarea.value = text;
        document.body.appendChild(textarea);
        textarea.select();
        document.execCommand('copy');
        document.body.removeChild(textarea);
        btn.textContent = 'Copied!';
        setTimeout(function () {
            btn.textContent = 'Copy';
        }, 2000);
    });
}
