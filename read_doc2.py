import sys
import os
import re

file_path = r'C:\Users\28705\Desktop\脐橙检测\德育综合服务平台v1.0说明书.doc'

# Extract text from binary .doc file
with open(file_path, 'rb') as f:
    raw = f.read()

# Try UTF-16LE text extraction
try:
    texts = raw.decode('utf-16le', errors='ignore')
    # Filter readable Chinese/English text segments
    pattern = r'[\u4e00-\u9fff\u3000-\u303f\uff00-\uffefa-zA-Z0-9\s,.;:!?。，；：！？、（）\-()\[\]【】《》/\\]+'
    readable = re.findall(pattern, texts)
    readable = [t.strip() for t in readable if len(t.strip()) > 3]
    print("=== Extracted text (UTF-16LE) ===")
    for t in readable:
        print(t)
except Exception as e:
    print(f"UTF-16LE extraction error: {e}")

print("\n\n=== WordDocument stream text ===")
try:
    import olefile
    ole = olefile.OleFileIO(file_path)
    word_doc = ole.openstream('WordDocument').read()
    # Try to find text in the WordDocument stream
    text_utf16 = word_doc.decode('utf-16le', errors='ignore')
    pattern2 = r'[\u4e00-\u9fff]+'
    chinese_texts = re.findall(pattern2, text_utf16)
    for t in chinese_texts:
        print(t)
except Exception as e:
    print(f"Stream extraction error: {e}")

# Also try to extract from 0Table stream
print("\n\n=== 0Table stream text ===")
try:
    import olefile
    ole = olefile.OleFileIO(file_path)
    if ole.exists('0Table'):
        table_data = ole.openstream('0Table').read()
        text_utf16 = table_data.decode('utf-16le', errors='ignore')
        pattern3 = r'[\u4e00-\u9fff]+'
        chinese_texts = re.findall(pattern3, text_utf16)
        for t in chinese_texts:
            if len(t) > 1:
                print(t)
except Exception as e:
    print(f"Table extraction error: {e}")
