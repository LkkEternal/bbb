import sys
import os
import struct

file_path = r'C:\Users\28705\Desktop\脐橙检测\德育综合服务平台v1.0说明书.doc'

try:
    import win32com.client
    word = win32com.client.Dispatch("Word.Application")
    word.Visible = False
    doc = word.Documents.Open(file_path)
    text = doc.Content.Text
    doc.Close()
    word.Quit()
    
    # Write to file with UTF-8 encoding
    output_path = r'C:\Users\28705\Desktop\脐橙检测\德育综合服务平台说明书.txt'
    with open(output_path, 'w', encoding='utf-8') as f:
        f.write(text)
    print(f"Text saved to {output_path}")
    print(f"Total characters: {len(text)}")
except Exception as e:
    print(f"Error: {e}")
    import traceback
    traceback.print_exc()
