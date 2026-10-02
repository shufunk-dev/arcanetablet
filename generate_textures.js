const fs = require('fs');
const path = require('path');
const zlib = require('zlib');

function createPNG(width, height, getPixel) {
    const rawData = Buffer.alloc(height * (1 + width * 4));
    let offset = 0;
    for (let y = 0; y < height; y++) {
        rawData[offset++] = 0; // Filter byte: None
        for (let x = 0; x < width; x++) {
            const [r, g, b, a] = getPixel(x, y);
            rawData[offset++] = r;
            rawData[offset++] = g;
            rawData[offset++] = b;
            rawData[offset++] = a;
        }
    }

    const compressed = zlib.deflateSync(rawData);

    function crc32(buf) {
        let crc = 0xFFFFFFFF;
        for (let i = 0; i < buf.length; i++) {
            crc = crc ^ buf[i];
            for (let j = 0; j < 8; j++) {
                crc = (crc >>> 1) ^ ((crc & 1) ? 0xEDB88320 : 0);
            }
        }
        return (crc ^ 0xFFFFFFFF) >>> 0;
    }

    function createChunk(type, data) {
        const len = data.length;
        const typeBuf = Buffer.from(type);
        const chunk = Buffer.alloc(8 + len + 4);
        chunk.writeUInt32BE(len, 0);
        typeBuf.copy(chunk, 4);
        data.copy(chunk, 8);
        const crc = crc32(Buffer.concat([typeBuf, data]));
        chunk.writeUInt32BE(crc, 8 + len);
        return chunk;
    }

    const signature = Buffer.from([0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A]);
    const ihdrData = Buffer.alloc(13);
    ihdrData.writeUInt32BE(width, 0);
    ihdrData.writeUInt32BE(height, 4);
    ihdrData[8] = 8; // bit depth
    ihdrData[9] = 6; // color type RGBA
    ihdrData[10] = 0; // compression
    ihdrData[11] = 0; // filter
    ihdrData[12] = 0; // interlace

    const ihdr = createChunk('IHDR', ihdrData);
    const idat = createChunk('IDAT', compressed);
    const iend = createChunk('IEND', Buffer.alloc(0));

    return Buffer.concat([signature, ihdr, idat, iend]);
}

// 16x16 Arcane Tablet Item Texture
const itemBuf = createPNG(16, 16, (x, y) => {
    // Transparent borders outside tablet body
    if (x < 2 || x > 13 || y < 1 || y > 14) return [0, 0, 0, 0];

    // Frame outer corners
    if ((x === 2 || x === 13) && (y === 1 || y === 14)) return [0, 0, 0, 0];

    // Gold Bezel Corners
    if ((x <= 3 || x >= 12) && (y <= 2 || y >= 13)) {
        return [255, 200, 40, 255]; // Golden Trim
    }

    // Outer Obsidian / Dark Slate Case
    if (x === 2 || x === 13 || y === 1 || y === 14) {
        return [30, 32, 45, 255]; // Dark chassis
    }

    // Inner Bezel
    if (x === 3 || x === 12 || y === 2 || y === 13) {
        return [20, 24, 35, 255];
    }

    // Glowing Neon Screen Matrix (Cyan / Arcane Energy)
    if (x === 7 && y === 7) return [255, 255, 255, 255]; // Center core spark
    if ((x >= 6 && x <= 8) && (y >= 6 && y <= 8)) return [0, 255, 240, 255]; // Core glow

    // Arcane Glyphs / Grid lines
    if (x === 5 && y === 4) return [0, 229, 255, 255];
    if (x === 9 && y === 4) return [0, 229, 255, 255];
    if (x === 7 && y === 10) return [0, 229, 255, 255];
    if (x === 5 && y === 10) return [179, 136, 255, 255];
    if (x === 9 && y === 10) return [179, 136, 255, 255];

    // Hologram screen surface
    return [8, 45, 60, 255];
});

// Mod Icon (64x64)
const iconBuf = createPNG(64, 64, (x, y) => {
    const nx = Math.floor(x / 4);
    const ny = Math.floor(y / 4);
    // Use item palette scaled up with glowing border
    if (nx < 2 || nx > 13 || ny < 1 || ny > 14) {
        // Outer glow
        const cx = 32, cy = 32;
        const dist = Math.hypot(x - cx, y - cy);
        if (dist < 30) {
            const alpha = Math.max(0, Math.min(100, Math.floor((30 - dist) * 10)));
            return [0, 229, 255, alpha];
        }
        return [0, 0, 0, 0];
    }

    if ((nx <= 3 || nx >= 12) && (ny <= 2 || ny >= 13)) {
        return [255, 215, 60, 255]; // Gold corners
    }

    if (nx === 2 || nx === 13 || ny === 1 || ny === 14) {
        return [25, 28, 40, 255];
    }

    if (nx === 3 || nx === 12 || ny === 2 || ny === 13) {
        return [15, 20, 30, 255];
    }

    // Core
    if (nx === 7 && ny === 7) return [255, 255, 255, 255];
    if ((nx >= 6 && nx <= 8) && (ny >= 6 && ny <= 8)) return [0, 255, 240, 255];

    if ((nx + ny) % 3 === 0) return [0, 200, 255, 255];

    return [6, 35, 50, 255];
});

const itemDir = path.join(__dirname, 'src', 'main', 'resources', 'assets', 'arcanetablet', 'textures', 'item');
const iconDir = path.join(__dirname, 'src', 'main', 'resources', 'assets', 'arcanetablet');

fs.mkdirSync(itemDir, { recursive: true });
fs.mkdirSync(iconDir, { recursive: true });

fs.writeFileSync(path.join(itemDir, 'arcane_tablet.png'), itemBuf);
fs.writeFileSync(path.join(iconDir, 'icon.png'), iconBuf);

console.log('Successfully generated arcane_tablet.png and icon.png!');
