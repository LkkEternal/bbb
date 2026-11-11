"""从zip中选取动物脸图片 - cat/dog/wild各1000张"""
import zipfile
import random
from pathlib import Path
from PIL import Image
from io import BytesIO
import sys

zip_path = r'C:\Users\28705\AppData\Local\Temp\MicrosoftEdgeDownloads\03147cdf-fc8a-4923-aea1-781efb470819\Animal Faces_datasets.zip'
out_base = Path(r'D:\表情检测\python-service\data\raw\unknown\animal')
TARGET = 1000

print(f'Opening zip: {zip_path}')
z = zipfile.ZipFile(zip_path, 'r')
names = z.namelist()
print(f'Total files in zip: {len(names)}')

for category in ['cat', 'dog', 'wild']:
    out_dir = out_base / category
    out_dir.mkdir(parents=True, exist_ok=True)

    imgs = [n for n in names if f'/train/{category}/' in n and not n.endswith('/')]
    print(f'\n{category}: {len(imgs)} images available')

    random.seed(42)
    selected = random.sample(imgs, min(TARGET * 3, len(imgs)))

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
        except Exception as e:
            continue

        if count % 200 == 0 and count > 0:
            print(f'  {category}: {count}/{TARGET}', flush=True)

    print(f'  {category} DONE: {count} images')

z.close()
print('\nAll categories done!')
