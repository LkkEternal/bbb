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
    print(text)
except ImportError:
    print("win32com not available, trying alternative...")
    # Try to use the olefile approach with proper encoding
    try:
        import olefile
        ole = olefile.OleFileIO(file_path)
        
        # Read WordDocument stream
        word_doc = ole.openstream('WordDocument').read()
        
        # In Word .doc format, text is stored in the WordDocument stream
        # The text begins after the FIB (File Information Block)
        # Try to find text by scanning for Chinese character patterns
        
        # Method: Look for runs of valid characters in the raw bytes
        # Word .doc stores text in a mix of encodings
        
        # Try the WPS-specific approach
        if ole.exists('WpsCustomData'):
            wps_data = ole.openstream('WpsCustomData').read()
            print(f"WpsCustomData size: {len(wps_data)}")
        
        # Try to extract using the compound binary format
        # The main text in Word .doc is stored as UTF-16LE in specific offsets
        # Let's try to read the piece table from the FIB
        
        # Simple approach: scan for UTF-16LE encoded Chinese text
        # Chinese characters are in range 0x4E00-0x9FFF
        results = []
        i = 0
        while i < len(word_doc) - 1:
            char_code = struct.unpack('<H', word_doc[i:i+2])[0]
            if 0x4E00 <= char_code <= 0x9FFF or 0x3000 <= char_code <= 0x303F or \
               0xFF00 <= char_code <= 0xFFEF or 0x0020 <= char_code <= 0x007E:
                results.append(chr(char_code))
            else:
                if results and len(results) > 1:
                    text = ''.join(results)
                    if any('\u4e00' <= c <= '\u9fff' for c in text):
                        print(text, end='')
                results = []
            i += 2
        
        if results and len(results) > 1:
            text = ''.join(results)
            if any('\u4e00' <= c <= '\u9fff' for c in text):
                print(text)
                
    except Exception as e:
        print(f"Error: {e}")
