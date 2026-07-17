import vsdx
import os

output_path = r'C:\Users\28705\Desktop\脐橙检测\图2-1_系统总体功能模块图.vsdx'

# Create a new Visio document
with vsdx.open() as doc:
    # Get the first page
    page = doc.pages[0]
    page.name = "系统总体功能模块图"

    # Helper function to add a shape (rectangle with text)
    def add_box(page, text, x, y, w, h, fill_color="FFFFFF", font_size=10):
        shape = page.add_shape(
            text=text,
            shape_type="rectangle",
            x=x, y=y, width=w, height=h,
        )
        shape.text = text
        # Try to set font size via XML manipulation
        return shape

    # ============================================================
    # Layout constants (in inches, Visio default)
    # Page width ~11 inches, height ~8.5 inches
    # ============================================================
    page_w = 10.5
    page_h = 8.0

    # Title
    title_y = 7.5
    add_box(page, "图2-1  脐橙病虫害监测系统总体功能模块图",
            1.5, title_y, 7.5, 0.4, "FFFFFF")

    # ============================================================
    # Layer 1: 展示层 (top)
    # ============================================================
    layer1_y = 6.8
    layer1_h = 0.55
    layer1_w = 2.2

    # Layer label
    add_box(page, "展示层", 0.2, layer1_y, 0.9, layer1_h, "D6EAF8")

    # Client boxes
    clients = ["PC 管理后台\n(Vue3+Element+)", "微信小程序\n(原生框架)", "IoT 边缘设备\n(YOLO+传感器)"]
    cx_start = 1.3
    cx_gap = 2.8
    for i, c in enumerate(clients):
        add_box(page, c, cx_start + i * cx_gap, layer1_y, layer1_w, layer1_h, "D6EAF8")

    # ============================================================
    # Arrow 1
    # ============================================================
    add_box(page, "▼", 4.8, 6.6, 0.8, 0.3, "FFFFFF")

    # ============================================================
    # Layer 2: API网关层
    # ============================================================
    layer2_y = 5.8
    layer2_h = 0.55

    add_box(page, "API网关层", 0.2, layer2_y, 0.9, layer2_h, "FDEBD0")
    add_box(page, "Spring Boot 3 RESTful API（JWT认证 · API Key认证 · CORS · 参数校验）",
            1.3, layer2_y, 8.5, layer2_h, "FDEBD0")

    # Arrow 2
    add_box(page, "▼", 4.8, 5.5, 0.8, 0.3, "FFFFFF")

    # ============================================================
    # Layer 3: 业务服务层 (biggest layer)
    # ============================================================
    layer3_top = 3.6
    layer3_h = 1.7

    add_box(page, "业务服务层", 0.2, layer3_top, 0.9, layer3_h, "D5F5E3")

    # Module row 1 (M1-M5)
    mod_w = 1.6
    mod_h = 0.65
    mod_gap = 0.12
    mod_row1_y = layer3_top + layer3_h - mod_h - 0.08

    mods_row1 = [
        "M1 用户认证\n注册/登录/JWT",
        "M2 用户管理\nCRUD/角色/审计",
        "M3 果园管理\n果园/成员/设备",
        "M4 数据采集\n病害/传感器/天气",
        "M5 预警规则\n规则引擎/风险评估",
    ]
    mx_start = 1.3
    for i, m in enumerate(mods_row1):
        add_box(page, m, mx_start + i * (mod_w + mod_gap), mod_row1_y, mod_w, mod_h, "D5F5E3")

    # Module row 2 (M6-M10)
    mod_row2_y = mod_row1_y - mod_h - 0.12

    mods_row2 = [
        "M6 消息推送\n微信/短信/邮件",
        "M7 知识库RAG\n向量检索/LLM问答",
        "M8 小程序接口\n聚合数据接口",
        "M9 农事日历\n物候期/节气提醒",
        "M10 用户反馈\n提交/审核/反哺",
    ]
    for i, m in enumerate(mods_row2):
        add_box(page, m, mx_start + i * (mod_w + mod_gap), mod_row2_y, mod_w, mod_h, "D5F5E3")

    # Module row 3 (补充模块)
    mod_row3_y = mod_row2_y - mod_h - 0.12
    extra_mods = ["风险预测\nLogistic回归四分类", "OTA 固件升级\n设备远程升级管理"]
    extra_w = 2.2
    extra_gap = 0.3
    extra_start = mx_start + 1.0
    for i, m in enumerate(extra_mods):
        add_box(page, m, extra_start + i * (extra_w + extra_gap), mod_row3_y, extra_w, mod_h, "D5F5E3")

    # Arrow 3
    add_box(page, "▼", 4.8, 3.3, 0.8, 0.3, "FFFFFF")

    # ============================================================
    # Layer 4: 数据层
    # ============================================================
    layer4_y = 2.5
    layer4_h = 0.6

    add_box(page, "数据层", 0.2, layer4_y, 0.9, layer4_h, "E8DAEF")

    data_items = [
        "PostgreSQL 16\npgvector向量扩展·21张表",
        "Redis 7\n缓存·Token·事件队列",
        "Python RAG 服务\nFastAPI :18001·BGE Embedding",
    ]
    data_w = 2.8
    data_gap = 0.2
    dx_start = 1.3
    for i, d in enumerate(data_items):
        add_box(page, d, dx_start + i * (data_w + data_gap), layer4_y, data_w, layer4_h, "E8DAEF")

    # Arrow 4
    add_box(page, "▼", 4.8, 2.2, 0.8, 0.3, "FFFFFF")

    # ============================================================
    # Layer 5: 外部服务层
    # ============================================================
    layer5_y = 1.3
    layer5_h = 0.65

    add_box(page, "外部服务层", 0.2, layer5_y, 0.9, layer5_h, "FADBD8")

    ext_items = [
        "华为云 OBS\n对象存储",
        "华为云 IoTDA\n设备管理",
        "和风天气 API\n天气数据",
        "高德地图 API\n地理编码",
        "阿里云 SMS\n短信服务",
        "DeepSeek / Qwen3\nLLM推理",
        "微信订阅消息\n消息推送",
        "163 邮箱 SMTP\n邮件推送",
    ]
    ext_w = 1.1
    ext_gap = 0.08
    ex_start = 1.3
    for i, e in enumerate(ext_items):
        add_box(page, e, ex_start + i * (ext_w + ext_gap), layer5_y, ext_w, layer5_h, "FADBD8")

    # Save the document
    doc.save(output_path)

print(f"Visio文件已生成: {output_path}")
print(f"文件大小: {os.path.getsize(output_path)} bytes")
