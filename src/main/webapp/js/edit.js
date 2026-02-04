// 感情データ定義
const emotions = [
    // --- ポジティブ（暖色系を中心に、安心は寒色へ） ---
    // 満足: 輝くような明るい黄色
    { label: '満足', cls: 'emo-pos-1', color: '#FFD600' },
    // 楽しい: 元気なオレンジイエロー
    { label: '楽しい', cls: 'emo-pos-2', color: '#FFAB00' },
    // 感動: 深みのある鮮やかなオレンジ
    { label: '感動', cls: 'emo-pos-3', color: '#FF6D00' },
    // 好き: 情熱的な赤みの強いオレンジ（朱色）
    { label: '好き', cls: 'emo-pos-4', color: '#FF3D00' },
    // 普通: 中立的でフレッシュなライムグリーン
    { label: '普通', cls: 'emo-pos-5', color: '#AEEA00' },
    // 安心: 落ち着きと明るさを兼ね備えたミントグリーン（ターコイズ）
    { label: '安心', cls: 'emo-pos-6', color: '#00E676' },

    // --- ネガティブ（寒色・暗色系を中心に鮮やかに） ---
    // 退屈: 少し彩度を落としたブルーグレー（無気力感）
    { label: '退屈', cls: 'emo-neg-1', color: '#607D8B' },
    // モヤモヤ: はっきりしない紫（霧がかかったような心境）
    { label: 'モヤモヤ', cls: 'emo-neg-2', color: '#7C4DFF' },
    // 怒り: 激しい鮮烈な赤
    { label: '怒り', cls: 'emo-neg-3', color: '#D50000' },
    // 不安: 冷たく強い青
    { label: '不安', cls: 'emo-neg-4', color: '#2962FF' },
    // 悲しい: 深く沈んだ濃紺
    { label: '悲しい', cls: 'emo-neg-5', color: '#0D47A1' },
    // 嫌い: 最も暗く強い、深い紫（拒絶）
    { label: '嫌い', cls: 'emo-neg-6', color: '#311B92' }
];

document.addEventListener("DOMContentLoaded", () => {
    // URLから日付を取得してセット
    const params = new URLSearchParams(window.location.search);
    const dateStr = params.get('date');
    document.getElementById('date-input').value = dateStr;

    // 感情ボタンの描画
    renderEmotions();

    // ★各ボタンにイベントリスナーを設定
    document.getElementById('save-btn').addEventListener('click', saveData);
    document.getElementById('delete-btn').addEventListener('click', deleteData);
    document.getElementById('back-btn').addEventListener('click', goBack);
});

// 感情ボタン生成
function renderEmotions() {
    const grid = document.getElementById('emotion-grid');
    emotions.forEach(emo => {
        const btn = document.createElement('button');
        btn.type = 'button';
        btn.className = `emotion-btn ${emo.cls}`;
        btn.textContent = emo.label;
        btn.dataset.color = emo.color; // 色情報の保持
        
        btn.onclick = () => {
            btn.classList.toggle('selected');
            updateBgColor(); // 背景色の更新
        };
        grid.appendChild(btn);
    });
}

// 背景色更新（感情の混色）
function updateBgColor() {
    const selected = document.querySelectorAll('.emotion-btn.selected');
    const bg = document.getElementById('edit-bg');
    
    if (selected.length === 0) {
        bg.style.backgroundColor = '#ffffff'; // デフォルト白
        return;
    }
    
    let r=0, g=0, b=0;
    selected.forEach(btn => {
        const hex = btn.dataset.color.replace('#','');
        r += parseInt(hex.substring(0,2), 16);
        g += parseInt(hex.substring(2,4), 16);
        b += parseInt(hex.substring(4,6), 16);
    });
    
    r = Math.round(r / selected.length);
    g = Math.round(g / selected.length);
    b = Math.round(b / selected.length);
    
    bg.style.backgroundColor = `rgb(${r},${g},${b})`;
}

// --- ★共通：メッセージを表示して3秒後にホームへ ---
function showMessageAndRedirect(message, isSuccess) {
    // 画面にメッセージを表示（alertで代用。リッチにするならDOM操作でHTMLに表示）
    alert(message); 
    
    // 3秒後に遷移
    setTimeout(() => {
        window.location.href = 'index.html';
    }, 100);
}

// --- ★保存処理 ---
async function saveData() {
    try {
        const form = document.getElementById('edit-form');
        const formData = new FormData(form);
        
        // 選択された感情を取得してカンマ区切りで追加
        const selectedEmos = Array.from(document.querySelectorAll('.emotion-btn.selected'))
                                  .map(btn => btn.textContent).join(',');
        formData.append('emotionCode', selectedEmos);

        // サーバーへ送信 (POST)
        const res = await fetch('diary', {
            method: 'POST',
            body: formData
        });
        
        // 結果の判定
        if (res.ok) {
            const result = await res.json();
            if(result.success) {
                showMessageAndRedirect("保存完了", true);
            } else {
                showMessageAndRedirect("保存に失敗しました", false);
            }
        } else {
            showMessageAndRedirect("保存に失敗しました (Server Error)", false);
        }

    } catch (e) {
        console.error(e);
        showMessageAndRedirect("保存に失敗しました (通信エラー)", false);
    }
}

// --- ★削除処理 ---
async function deleteData() {
    // 誤操作防止の確認（仕様にはないが念のためあると良い。不要なら削除してください）
    if(!confirm("本当に削除しますか？")) return;

    try {
        const date = document.getElementById('date-input').value;
        
        // サーバーへ送信 (DELETEメソッド)
        const res = await fetch(`diary?date=${date}`, {
            method: 'DELETE'
        });

        if (res.ok) {
            const result = await res.json();
            if(result.success) {
                showMessageAndRedirect("削除完了", true);
            } else {
                showMessageAndRedirect("削除に失敗しました", false);
            }
        } else {
            showMessageAndRedirect("削除に失敗しました (Server Error)", false);
        }

    } catch (e) {
        console.error(e);
        showMessageAndRedirect("削除に失敗しました (通信エラー)", false);
    }
}

// --- ★戻る処理 ---
function goBack() {
    window.location.href = 'index.html';
}