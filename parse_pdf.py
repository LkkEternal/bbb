import sys

def parse_pdf(pdf_path):
    try:
        import fitz  # PyMuPDF
        doc = fitz.open(pdf_path)
        text = ""
        for page in doc:
            text += page.get_text()
        doc.close()
        return text
    except ImportError:
        pass
    
    try:
        from PyPDF2 import PdfReader
        reader = PdfReader(pdf_path)
        text = ""
        for page in reader.pages:
            text += page.extract_text()
        return text
    except ImportError:
        pass
    
    try:
        import pdfplumber
        with pdfplumber.open(pdf_path) as pdf:
            text = ""
            for page in pdf.pages:
                text += page.extract_text() or ""
        return text
    except ImportError:
        pass
    
    print("错误: 未安装PDF解析库，请执行以下命令安装:")
    print("pip install PyMuPDF 或 pip install PyPDF2 或 pip install pdfplumber")
    return None

def extract_iotda_info(text):
    if not text:
        return
    
    lines = text.split('\n')
    
    print("=" * 60)
    print("华为云IoTDA关键信息提取")
    print("=" * 60)
    
    api_keywords = ["GET ", "POST ", "PUT ", "DELETE ", "PATCH ", "/v5/", "/v3/", "https://"]
    auth_keywords = ["AK", "SK", "Access Key", "Secret Key", "token", "X-Subject-Token", "认证", "鉴权"]
    device_keywords = ["device_id", "device_name", "product_id", "设备ID", "设备标识", "产品ID"]
    property_keywords = ["service_id", "property", "属性", "服务", "report", "shadow"]
    endpoint_keywords = ["endpoint", "接入地址", "Endpoint", "myhuaweicloud.com"]
    
    print("\n【API接口】")
    for line in lines:
        for keyword in api_keywords:
            if keyword in line:
                print(f"  {line.strip()[:120]}")
                break
    
    print("\n【认证方式】")
    for line in lines:
        for keyword in auth_keywords:
            if keyword in line:
                print(f"  {line.strip()[:120]}")
                break
    
    print("\n【设备相关】")
    for line in lines:
        for keyword in device_keywords:
            if keyword in line:
                print(f"  {line.strip()[:120]}")
                break
    
    print("\n【属性/服务】")
    for line in lines:
        for keyword in property_keywords:
            if keyword in line:
                print(f"  {line.strip()[:120]}")
                break
    
    print("\n【Endpoint地址】")
    for line in lines:
        for keyword in endpoint_keywords:
            if keyword in line:
                print(f"  {line.strip()[:120]}")
                break

if __name__ == "__main__":
    pdf_files = [
        r"C:\Users\xingyu\Downloads\设备接入 IoTDA 快速入门.pdf",
        r"C:\Users\xingyu\Downloads\设备接入 IoTDA API参考.pdf"
    ]
    
    for pdf_path in pdf_files:
        print(f"\n{'=' * 60}")
        print(f"解析文件: {pdf_path}")
        print('=' * 60)
        
        text = parse_pdf(pdf_path)
        if text:
            extract_iotda_info(text)
            
            output_file = pdf_path.replace('.pdf', '_parsed.txt')
            with open(output_file, 'w', encoding='utf-8') as f:
                f.write(text)
            print(f"\n完整文本已保存到: {output_file}")
