const express = require('express');
const mysql = require('mysql2/promise');
const crypto = require('crypto');
require('dotenv').config();

const app = express();
// 最適化されたデータのみを受け入れるため、制限を最小限の2MBに設定
app.use(express.json({ limit: '2mb' }), express.static('public'));

const pool = mysql.createPool({
    host: process.env.MYSQLHOST,
    user: process.env.MYSQLUSER,
    password: process.env.MYSQLPASSWORD,
    database: process.env.MYSQLDATABASE,
    port: process.env.MYSQLPORT || 3306,
    ssl: { rejectUnauthorized: false }
});

// 共通クエリ実行関数
const q = async (res, sql, params) => {
    try {
        const [r] = await pool.query(sql, params);
        res.json(r);
    } catch (e) { res.status(500).json({ error: e.message }); }
};

app.get('/api/list', (req, res) => q(res, 'SELECT group_id, group_name, host_name FROM DIARY_GROUPS ORDER BY updated_at DESC'));

app.post('/api/create', async (req, res) => {
    const id = crypto.randomUUID().substring(0, 8).toUpperCase();
    try {
        await pool.query('INSERT INTO DIARY_GROUPS (group_id, group_name, host_name, password) VALUES (?, ?, ?, ?)', [id, req.body.name, req.body.host, req.body.pass]);
        res.json({ success: true, id });
    } catch (e) { res.status(500).json({ error: e.message }); }
});

app.get('/api/entries', (req, res) => q(res, 'SELECT * FROM DIARY_ENTRIES WHERE group_id = ?', [req.query.groupId]));

app.post('/api/addEntry', async (req, res) => {
    await pool.query('INSERT INTO DIARY_ENTRIES (group_id, diary_date, message, image_data, color) VALUES (?, ?, ?, ?, ?)', [req.body.groupId, req.body.date, req.body.message, req.body.photo, req.body.color]);
    q(res, 'UPDATE DIARY_GROUPS SET updated_at = NOW() WHERE group_id = ?', [req.body.groupId]);
});

app.post('/api/deleteEntry', (req, res) => q(res, 'DELETE FROM DIARY_ENTRIES WHERE id = ?', [req.body.entryId]));

app.listen(process.env.PORT || 8080);
