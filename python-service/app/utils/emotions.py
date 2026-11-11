"""表情常量和映射"""

EMOTIONS = {
    0: {'label': 'angry',    'label_cn': '生气'},
    1: {'label': 'disgust',  'label_cn': '厌恶'},
    2: {'label': 'fear',     'label_cn': '恐惧'},
    3: {'label': 'happy',    'label_cn': '开心'},
    4: {'label': 'sad',      'label_cn': '悲伤'},
    5: {'label': 'surprise', 'label_cn': '惊讶'},
    6: {'label': 'neutral',  'label_cn': '平静'},
    7: {'label': 'unknown',  'label_cn': '未知'},
}

EMOTION_UNKNOWN_INDEX = 7

EMOTION_COLORS = {
    'angry':    (0, 0, 255),
    'disgust':  (128, 0, 128),
    'fear':     (0, 128, 128),
    'happy':    (0, 255, 0),
    'sad':      (255, 0, 0),
    'surprise': (0, 165, 255),
    'neutral':  (128, 128, 128),
    'unknown':  (255, 255, 0),
}

EMOTION_LIST = [EMOTIONS[i] for i in range(8)]

# 置信度质量分级
QUALITY_HIGH = 'high'        # >= 0.70 可信
QUALITY_MEDIUM = 'medium'    # [0.40, 0.70) 不确定
QUALITY_LOW = 'low'          # < 0.40 无效

QUALITY_THRESHOLD_HIGH = 0.70
QUALITY_THRESHOLD_LOW = 0.40

# MTCNN 过滤参数
MTCNN_MIN_CONFIDENCE = 0.90
MIN_FACE_RATIO = 0.01
FACE_ASPECT_MIN = 0.5
FACE_ASPECT_MAX = 2.0


def classify_quality(confidence: float) -> str:
    if confidence >= QUALITY_THRESHOLD_HIGH:
        return QUALITY_HIGH
    elif confidence >= QUALITY_THRESHOLD_LOW:
        return QUALITY_MEDIUM
    else:
        return QUALITY_LOW
