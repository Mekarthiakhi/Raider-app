/**
 * SVM FLEET Operating System
 * Lightweight Zero-Dependency Production Web Server
 * For Render / Node.js Deployment
 */

const http = require('http');
const fs = require('fs');
const path = require('path');

const PORT = process.env.PORT || 3000;
const WEB_DIR = path.join(__dirname, 'web');

const MIME_TYPES = {
    '.html': 'text/html',
    '.css': 'text/css',
    '.js': 'text/javascript',
    '.json': 'application/json',
    '.png': 'image/png',
    '.jpg': 'image/jpeg',
    '.svg': 'image/svg+xml',
    '.ico': 'image/x-icon',
    '.woff2': 'font/woff2'
};

const server = http.createServer((req, res) => {
    let safePath = path.normalize(req.url.split('?')[0]);
    if (safePath === '/' || safePath === '\\') {
        safePath = '/index.html';
    }

    let filePath = path.join(WEB_DIR, safePath);

    // Fallback to index.html for SPA routing
    if (!fs.existsSync(filePath)) {
        filePath = path.join(WEB_DIR, 'index.html');
    }

    const ext = path.extname(filePath).toLowerCase();
    const contentType = MIME_TYPES[ext] || 'application/octet-stream';

    fs.readFile(filePath, (err, content) => {
        if (err) {
            res.writeHead(500, { 'Content-Type': 'text/plain' });
            res.end('500 Internal Server Error');
            return;
        }

        res.writeHead(200, {
            'Content-Type': contentType,
            'Cache-Control': 'no-cache',
            'X-Frame-Options': 'SAMEORIGIN'
        });
        res.end(content);
    });
});

server.listen(PORT, () => {
    console.log(`🚀 SVM FLEET Web Operating System running on port ${PORT}`);
    console.log(`📁 Serving directory: ${WEB_DIR}`);
});

