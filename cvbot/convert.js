const puppeteer = require('puppeteer');

const htmlPath = process.argv[2];
const pdfPath = process.argv[3];

(async () => {
    const browser = await puppeteer.launch({
        headless: 'new',
        args: [
            '--no-sandbox',
            '--disable-setuid-sandbox',
            '--disable-web-security'
        ]
    });
    const page = await browser.newPage();
    await page.goto('file://' + htmlPath, { waitUntil: 'networkidle0', timeout: 60000 });
    await new Promise(resolve => setTimeout(resolve, 2000));
    await page.pdf({
        path: pdfPath,
        format: 'A4',
        printBackground: true
    });
    await browser.close();
})();