import sys
import os

file_path = r'C:\Users\28705\Desktop\脐橙检测\德育综合服务平台v1.0说明书.doc'

print(f"File exists: {os.path.exists(file_path)}")
print(f"File size: {os.path.getsize(file_path)} bytes")

# Try olefile
try:
    import olefile
    if olefile.isOleFile(file_path):
        ole = olefile.OleFileIO(file_path)
        print("OLE streams:")
        for s in ole.listdir():
            print(f"  {'/'.join(s)}")
        
        if ole.exists('WordDocument'):
            data = ole.openstream('WordDocument').read()
            print(f"WordDocument stream size: {len(data)} bytes")
    else:
        print("Not an OLE file")
except Exception as e:
    print(f"olefile error: {e}")

# Try to extract text using regex from binary
try:
    import re
    with open(file_path, 'rb') as f:
        raw = f.read()
    # UTF-16LE text extraction
    texts = raw.decode('utf-16le', errors='ignore')
    # Filter readable Chinese/English text
    readable = re.findall(r'[\u4e00-\u9fff\u3000-\u303f\uff00-\uffefa-zA-Z0-9\s,.;:!?。，；：！？、（）\-\(\)\[\]【】""''<>《》/\\]+', texts)
    readable = [t.strip() for t in readable if len(t.strip()) > 2]
    print("\n=== Extracted text (UTF-16LE) ===")
    for t in readable:
        print(t)
except Exception as e:
    print(f"Text extraction error: {e}")
