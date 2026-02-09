const express = require('express');
const mysql = require('mysql2/promise');
const crypto = require('crypto');
require('dotenv').config();

const app = express();
// さらに制限を絞り、5MBに設定（リサイズ済み画像ならこれで十分です）
app.use(express.json({ limit: '5mb' }), express.static('public'));

const pool = mysql.createPool({
    host: process.env.MYSQLHOST,
    user: process.env.MYSQLUSER,
    password: process.env.MYSQLPASSWORD,
    database: process.env.MYSQLDATABASE,
    port: process.env.MYSQLPORT || 3306,
    ssl: { rejectUnauthorized: false },
    connectionLimit: 10
});

// ヘルパー関数：エラーハンドリング付きのクエリ実行
const run = async (res, sql, params) => {
    try {
        const [result] = await pool.query(sql, params);
        res.json(result.id ? { success: true, id: result.id } : result);
    } catch (err) { res.status(500).json({ error: err.message }); }
};

app.get('/api/list', (req, res) => 
    run(res, 'SELECT group_id, group_name, host_name, updated_at FROM DIARY_GROUPS ORDER BY updated_at DESC'));

app.post('/api/create', (req, res) => {
    const id = crypto.randomUUID().substring(0, 8).toUpperCase();
    run(res, 'INSERT INTO DIARY_GROUPS (group_id, group_name, host_name, password) VALUES (?, ?, ?, ?)', 
        [id, req.body.name, req.body.host, req.body.pass]).then(() => res.json({ success: true, id }));
});

app.get('/api/entries', (req, res) => 
    run(res, 'SELECT * FROM DIARY_ENTRIES WHERE group_id = ? ORDER BY created_at ASC', [req.query.groupId]));

app.post('/api/addEntry', async (req, res) => {
    await pool.query('INSERT INTO DIARY_ENTRIES (group_id, diary_date, message, image_data, color) VALUES (?, ?, ?, ?, ?)', 
        [req.body.groupId, req.body.date, req.body.message, req.body.photo, req.body.color]);
    run(res, 'UPDATE DIARY_GROUPS SET updated_at = NOW() WHERE group_id = ?', [req.body.groupId]);
});

app.post('/api/deleteEntry', (req, res) => 
    run(res, 'DELETE FROM DIARY_ENTRIES WHERE id = ?', [req.body.entryId]));

app.listen(process.env.PORT || 8080);
