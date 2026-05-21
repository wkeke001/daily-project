(function() {
    var streaming = false;
    var ctx = (document.querySelector('meta[name=ctx]') && document.querySelector('meta[name=ctx]').content || '').replace(/\/$/, '');
    var API_URL = ctx + '/ai/chat';
    var API_STREAM_URL = ctx + '/ai/chat/stream';

    var style = document.createElement('style');
    style.textContent = `
        .ai-chat-btn {
            position: fixed; bottom: 24px; right: 24px; width: 56px; height: 56px;
            border-radius: 50%; background: #4a90d9; border: none; cursor: pointer;
            box-shadow: 0 4px 12px rgba(74,144,217,0.4); z-index: 10000;
            display: flex; align-items: center; justify-content: center; transition: transform 0.2s;
        }
        .ai-chat-btn:hover { transform: scale(1.1); }
        .ai-chat-btn svg { width: 28px; height: 28px; fill: white; }
        .ai-chat-panel {
            position: fixed; bottom: 90px; right: 24px; width: 360px; height: 480px;
            background: white; border-radius: 12px; box-shadow: 0 8px 32px rgba(0,0,0,0.15);
            z-index: 10001; display: none; flex-direction: column; overflow: hidden;
        }
        .ai-chat-panel.open { display: flex; }
        .ai-chat-header {
            padding: 10px 16px; background: #4a90d9; color: white;
            display: flex; align-items: center; gap: 10px; flex-shrink: 0;
        }
        .ai-chat-header .title { font-size: 15px; font-weight: 600; flex: 1; }
        .ai-chat-header-btn {
            background: rgba(255,255,255,0.2); border: none; color: white;
            padding: 4px 10px; border-radius: 4px; cursor: pointer; font-size: 12px;
        }
        .ai-chat-header-btn:hover { background: rgba(255,255,255,0.35); }
        .ai-chat-header-btn.active { background: rgba(255,255,255,0.5); }
        .ai-chat-close { background: none; border: none; color: white; font-size: 20px; cursor: pointer; padding: 0 4px; }
        .ai-chat-messages {
            flex: 1; overflow-y: auto; padding: 16px; display: flex; flex-direction: column; gap: 12px;
        }
        .ai-msg { max-width: 80%; padding: 10px 14px; border-radius: 12px; font-size: 14px; line-height: 1.5; word-break: break-word; }
        .ai-msg.user { align-self: flex-end; background: #4a90d9; color: white; border-bottom-right-radius: 4px; }
        .ai-msg.bot { align-self: flex-start; background: #f0f0f0; color: #333; border-bottom-left-radius: 4px; }
        .ai-msg.bot.loading { color: #999; }
        .ai-chat-input-bar {
            display: flex; gap: 8px; padding: 12px; border-top: 1px solid #eee; flex-shrink: 0;
        }
        .ai-chat-input-bar input {
            flex: 1; padding: 10px 12px; border: 1px solid #ddd; border-radius: 8px; font-size: 14px; outline: none;
        }
        .ai-chat-input-bar input:focus { border-color: #4a90d9; }
        .ai-chat-input-bar button {
            padding: 10px 16px; background: #4a90d9; color: white; border: none;
            border-radius: 8px; cursor: pointer; font-size: 14px; flex-shrink: 0;
        }
        .ai-chat-input-bar button:disabled { background: #b0d0f0; cursor: not-allowed; }
        @media (max-width: 600px) {
            .ai-chat-panel { width: calc(100vw - 32px); height: 60vh; right: 16px; bottom: 84px; }
            .ai-chat-btn { bottom: 16px; right: 16px; width: 50px; height: 50px; }
        }
    `;
    document.head.appendChild(style);

    var btn = document.createElement('button');
    btn.className = 'ai-chat-btn';
    btn.title = 'AI 助手';
    btn.innerHTML = '<svg viewBox="0 0 24 24"><path d="M20 2H4c-1.1 0-2 .9-2 2v18l4-4h14c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2zm0 14H5.2L4 17.2V4h16v12z"/><path d="M7 9h2v2H7zm4 0h2v2h11zm4 0h2v2h-2z"/></svg>';
    document.body.appendChild(btn);

    var panel = document.createElement('div');
    panel.className = 'ai-chat-panel';
    panel.innerHTML = '<div class="ai-chat-header">'
        + '<span class="title">AI 助手</span>'
        + '<button class="ai-chat-header-btn" id="ai-new-chat">新对话</button>'
        + '<button class="ai-chat-header-btn" id="ai-stream-toggle">流式</button>'
        + '<button class="ai-chat-close">&times;</button>'
        + '</div>'
        + '<div class="ai-chat-messages"></div>'
        + '<div class="ai-chat-input-bar"><input type="text" placeholder="输入你的问题..." /><button>发送</button></div>';
    document.body.appendChild(panel);

    var messages = panel.querySelector('.ai-chat-messages');
    var input = panel.querySelector('.ai-chat-input-bar input');
    var sendBtn = panel.querySelector('.ai-chat-input-bar button');
    var closeBtn = panel.querySelector('.ai-chat-close');
    var newChatBtn = panel.querySelector('#ai-new-chat');
    var streamToggle = panel.querySelector('#ai-stream-toggle');

    btn.addEventListener('click', function() { panel.classList.toggle('open'); input.focus(); });
    closeBtn.addEventListener('click', function() { panel.classList.remove('open'); });

    newChatBtn.addEventListener('click', function() {
        messages.innerHTML = '';
    });

    streamToggle.addEventListener('click', function() {
        streaming = !streaming;
        streamToggle.classList.toggle('active', streaming);
        streamToggle.textContent = streaming ? '流式 ✓' : '流式';
    });

    function getUser() {
        var el = document.querySelector('[sec\\:authentication]');
        return el ? el.textContent.trim() : 'anonymous';
    }

    function getCsrfToken() {
        var meta = document.querySelector('meta[name=_csrf]');
        return meta ? meta.content : '';
    }

    function getCsrfHeader() {
        var meta = document.querySelector('meta[name=_csrf_parameterName]');
        return meta ? meta.content : '_csrf';
    }

    function addMsg(text, type) {
        var div = document.createElement('div');
        div.className = 'ai-msg ' + type;
        div.textContent = text;
        messages.appendChild(div);
        messages.scrollTop = messages.scrollHeight;
        return div;
    }

    function sendBlocking(query) {
        var loading = addMsg('思考中...', 'bot loading');
        var headers = { 'Content-Type': 'application/json' };
        headers['X-CSRF-TOKEN'] = getCsrfToken();
        fetch(API_URL, {
            method: 'POST',
            headers: headers,
            body: JSON.stringify({
                inputs: { query: query },
                response_mode: 'blocking',
                user: getUser()
            })
        })
        .then(function(res) { return res.json(); })
        .then(function(data) {
            messages.removeChild(loading);
            if (data.error) {
                addMsg('错误: ' + data.error, 'bot');
            } else {
                var answer = data.data && data.data.outputs && data.data.outputs.message || '无回复';
                addMsg(answer, 'bot');
            }
        })
        .catch(function(err) {
            messages.removeChild(loading);
            addMsg('请求失败: ' + err.message, 'bot');
        })
        .finally(function() { sendBtn.disabled = false; });
    }

    function sendStreaming(query) {
        var botDiv = addMsg('', 'bot');
        var headers = { 'Content-Type': 'application/json' };
        headers['X-CSRF-TOKEN'] = getCsrfToken();
        fetch(API_STREAM_URL, {
            method: 'POST',
            headers: headers,
            body: JSON.stringify({
                inputs: { query: query },
                response_mode: 'streaming',
                user: getUser()
            })
        })
        .then(function(res) {
            var reader = res.body.getReader();
            var decoder = new TextDecoder();
            var buffer = '';
            function read() {
                reader.read().then(function(result) {
                    if (result.done) {
                        if (!botDiv.textContent) botDiv.textContent = '无回复';
                        sendBtn.disabled = false;
                        return;
                    }
                    buffer += decoder.decode(result.value, { stream: true });
                    var lines = buffer.split('\n');
                    buffer = lines.pop();
                    lines.forEach(function(line) {
                        if (line.startsWith('data: ')) {
                            try {
                                var json = JSON.parse(line.slice(6));
                                if (json.event === 'text_chunk' && json.data && json.data.text) {
                                    botDiv.textContent += json.data.text;
                                    messages.scrollTop = messages.scrollHeight;
                                } else if (json.event === 'workflow_finished' && json.data && json.data.outputs && json.data.outputs.message) {
                                    if (!botDiv.textContent) {
                                        botDiv.textContent = json.data.outputs.message;
                                        messages.scrollTop = messages.scrollHeight;
                                    }
                                }
                            } catch(e) {}
                        }
                    });
                    read();
                });
            }
            read();
        })
        .catch(function(err) {
            botDiv.textContent = '请求失败: ' + err.message;
            sendBtn.disabled = false;
        });
    }

    function send() {
        var query = input.value.trim();
        if (!query) return;
        addMsg(query, 'user');
        input.value = '';
        sendBtn.disabled = true;
        if (streaming) {
            sendStreaming(query);
        } else {
            sendBlocking(query);
        }
    }

    sendBtn.addEventListener('click', send);
    input.addEventListener('keydown', function(e) { if (e.key === 'Enter') send(); });
})();
