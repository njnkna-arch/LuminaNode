let currentYear, currentMonth;
let diaries = {}; 

// 初期化
document.addEventListener("DOMContentLoaded", async () => {
    const now = new Date();
    currentYear = now.getFullYear();
    currentMonth = now.getMonth();
    
    // イベントリスナー
    document.getElementById('prev-btn').addEventListener('click', () => changeMonth(-1));
    document.getElementById('next-btn').addEventListener('click', () => changeMonth(1));
    document.getElementById('go-edit-btn').addEventListener('click', goToEdit);
    
    // データ取得と描画
    await fetchDiaries(); 
    renderCalendar();
    startSlideshow();
});

async function fetchDiaries() {
    try {
        const response = await fetch('diary'); 
        if (response.ok) {
            const dataList = await response.json();
            dataList.forEach(item => diaries[item.date] = item);
        }
    } catch (e) { console.error(e); }
}

function renderCalendar() {
    const grid = document.getElementById('calendar-grid');
    document.getElementById('month-label').textContent = `${currentYear}年 ${currentMonth + 1}月`;
    grid.innerHTML = ''; 

    // 月初め
    const firstDayObj = new Date(currentYear, currentMonth, 1);
    const dayOfWeek = (firstDayObj.getDay() + 6) % 7; // 月曜始まり(0:月〜6:日)に変換
    const daysInMonth = new Date(currentYear, currentMonth + 1, 0).getDate();
    const today = new Date();

    // 空白セル
    for (let i = 0; i < dayOfWeek; i++) {
        const cell = document.createElement('div');
        cell.className = 'day-cell';
        cell.style.background = 'transparent';
        cell.style.border = 'none';
        cell.style.cursor = 'default';
        grid.appendChild(cell);
    }

    // 日付セル
    for (let d = 1; d <= daysInMonth; d++) {
        const dateStr = `${currentYear}-${String(currentMonth + 1).padStart(2, '0')}-${String(d).padStart(2, '0')}`;
        const cell = document.createElement('div');
        cell.className = 'day-cell';

        // 日付番号
        const dateSpan = document.createElement('span');
        dateSpan.textContent = d;
        dateSpan.className = 'date-number';
        cell.appendChild(dateSpan);

        // 今日の赤枠
        if (currentYear === today.getFullYear() && currentMonth === today.getMonth() && d === today.getDate()) {
            cell.classList.add('today'); 
        }

        // データ表示
        if (diaries[dateStr]) {
            const img = document.createElement('img');
            img.src = diaries[dateStr].photoPath || 'uploads/default.jpg';
            cell.appendChild(img);
        } else {
            const plus = document.createElement('span');
            plus.textContent = '+';
            plus.className = 'plus';
            cell.appendChild(plus);
        }

        cell.onclick = () => handleDateClick(dateStr);
        grid.appendChild(cell);
    }
}

function handleDateClick(dateStr) {
    if (diaries[dateStr]) {
        // 詳細表示
        document.getElementById('slideshow').classList.add('hidden');
        const detail = document.getElementById('diary-detail');
        detail.classList.remove('hidden');
        detail.dataset.currentDate = dateStr; // 編集用ID保持
        
        document.getElementById('detail-date').textContent = `${dateStr} の日記`;
        document.getElementById('detail-img').src = diaries[dateStr].photoPath;
        document.getElementById('detail-text').textContent = diaries[dateStr].bodyText;
        
        // 感情表示
        const emoDiv = document.getElementById('detail-emotions');
        emoDiv.innerHTML = '';
        if(diaries[dateStr].emotionCode){
            diaries[dateStr].emotionCode.split(',').forEach(c => {
                const s = document.createElement('span');
                s.textContent = c; 
                emoDiv.appendChild(s);
            });
        }
    } else {
        // 新規登録
        window.location.href = `edit.html?date=${dateStr}`;
    }
}

function goToEdit() {
    const date = document.getElementById('diary-detail').dataset.currentDate;
    window.location.href = `edit.html?date=${date}`;
}

function changeMonth(offset) {
    currentMonth += offset;
    if (currentMonth > 11) { currentMonth = 0; currentYear++; }
    else if (currentMonth < 0) { currentMonth = 11; currentYear--; }
    renderCalendar();
}

function startSlideshow() {
    const keys = Object.keys(diaries);
    if (keys.length === 0) return;
    let idx = 0;
    setInterval(() => {
        const d = diaries[keys[idx % keys.length]];
        if(d.photoPath) document.getElementById('slide-img').src = d.photoPath;
        idx++;
    }, 4000);
}