"""从zip中选取2000张动漫脸图片"""
import zipfile
import random
import os
from pathlib import Path
from PIL import Image
from io import BytesIO

zip_path = r'C:\Users\28705\AppData\Local\Temp\MicrosoftEdgeDownloads\926fa4bf-307d-4f90-8415-bb53c2163535\Anime Faces_datasets..zip'
out_dir = Path(r'D:\表情检测\python-service\data\raw\unknown\anime')
out_dir.mkdir(parents=True, exist_ok=True)

TARGET = 2000

z = zipfile.ZipFile(zip_path, 'r')
names = z.namelist()
imgs = [n for n in names if n.lower().endswith('.png') and not n.endswith('/')]
print(f'Zip中图片总数: {len(imgs)}')

random.seed(42)
selected = random.sample(imgs, min(TARGET * 5, len(imgs)))
print(f'候选图片数: {len(selected)}')

count = 0
for i, name in enumerate(selected):
    if count >= TARGET:
        break
    try:
        data = z.read(name)
        img = Image.open(BytesIO(data)).convert('RGB')
        if img.width < 32 or img.height < 32:
            continue
        save_path = out_dir / f'{count:05d}.jpg'
        img.save(str(save_path), 'JPEG', quality=85)
        count += 1
    except Exception:
        continue

    if count % 100 == 0 and count > 0:
        print(f'  进度: {count}/{TARGET} (已扫描{i+1}张)')

z.close()
print(f'\n完成! 保存了 {count} 张动漫脸到 {out_dir}')
