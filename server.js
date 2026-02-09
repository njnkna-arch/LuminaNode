const express = require('express');
const mysql = require('mysql2/promise');
const crypto = require('crypto');
require('dotenv').config();

const app = express();
// 画像データが含まれるため制限は維持するが、圧縮前提で10MBに調整
app.use(express.json({ limit: '10mb' })); 
app.use(express.urlencoded({ extended: true, limit: '10mb' }));

app.use(express.static('public'));

const pool = mysql.createPool({
    host: process.env.MYSQLHOST,
    user: process.env.MYSQLUSER,
    password: process.env.MYSQLPASSWORD,
    database: process.env.MYSQLDATABASE,
    port: process.env.MYSQLPORT || 3306,
    ssl: { rejectUnauthorized: false },
    waitForConnections: true,
    connectionLimit: 10,
    // タイムアウトを防ぐ設定
    connectTimeout: 10000 
});

// --- API ---

// 一覧取得（画像データは重いので、ここでは取得しないのが高速化のコツ）
app.get('/api/list', async (req, res) => {
    try {
        const [rows] = await pool.query('SELECT group_id, group_name, host_name, updated_at FROM DIARY_GROUPS ORDER BY updated_at DESC');
        res.json(rows);
    } catch (err) { res.status(500).json({ error: err.message }); }
});

app.post('/api/create', async (req, res) => {
    try {
        const { name, host, pass } = req.body;
        const id = crypto.randomUUID().substring(0, 8).toUpperCase();
        await pool.query(
            'INSERT INTO DIARY_GROUPS (group_id, group_name, host_name, password) VALUES (?, ?, ?, ?)',
            [id, name, host, pass]
        );
        res.json({ success: true, id });
    } catch (err) { res.status(500).json({ error: err.message }); }
});

// 庭園データ取得：画像が圧縮されていれば、ここでの読み込みが劇的に速くなる
app.get('/api/entries', async (req, res) => {
    try {
        const { groupId } = req.query;
        const [rows] = await pool.query('SELECT id, group_id, diary_date, message, image_data, color, created_at FROM DIARY_ENTRIES WHERE group_id = ? ORDER BY created_at ASC', [groupId]);
        res.json(rows);
    } catch (err) { res.status(500).json({ error: err.message }); }
});

app.post('/api/addEntry', async (req, res) => {
    try {
        const { groupId, date, message, photo, color } = req.body;
        await pool.query(
            'INSERT INTO DIARY_ENTRIES (group_id, diary_date, message, image_data, color) VALUES (?, ?, ?, ?, ?)',
            [groupId, date, message, photo, color]
        );
        await pool.query('UPDATE DIARY_GROUPS SET updated_at = NOW() WHERE group_id = ?', [groupId]);
        res.json({ success: true });
    } catch (err) { res.status(500).json({ error: err.message }); }
});

app.post('/api/deleteEntry', async (req, res) => {
    try {
        const { entryId } = req.body;
        await pool.query('DELETE FROM DIARY_ENTRIES WHERE id = ?', [entryId]);
        res.json({ success: true });
    } catch (err) { res.status(500).json({ error: err.message }); }
});

const PORT = process.env.PORT || 8080;
app.listen(PORT, () => console.log(`--- LUMINA Optimized Server started ---`));
