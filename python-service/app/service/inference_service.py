"""推理服务：MTCNN人脸检测 + YOLOv8-cls表情分类"""
import io
import time

import numpy as np
from PIL import Image

from app.config import MODEL_PATH, DEVICE
from app.utils.emotions import (
    EMOTIONS,
    EMOTION_UNKNOWN_INDEX,
    classify_quality,
    QUALITY_LOW,
    MTCNN_MIN_CONFIDENCE,
    MIN_FACE_RATIO,
    FACE_ASPECT_MIN,
    FACE_ASPECT_MAX,
)

UNKNOWN_CONFIDENCE_THRESHOLD = 0.35

_mtcnn = None
_classifier = None


def _get_device():
    import torch
    if torch.cuda.is_available():
        return f'cuda:{DEVICE}'
    print('[InferenceService] CUDA不可用，回退到CPU')
    return torch.device('cpu')


def load_models():
    global _mtcnn, _classifier
    import torch
    from facenet_pytorch import MTCNN
    from ultralytics import YOLO

    device = _get_device()
    print(f'[InferenceService] 设备: {device}')

    _mtcnn = MTCNN(
        keep_all=True,
        device=device,
        min_face_size=40,
        thresholds=[0.6, 0.7, 0.7],
        post_process=True,
    )
    print('[InferenceService] MTCNN 加载完成')

    if MODEL_PATH:
        _classifier = YOLO(MODEL_PATH)
        print(f'[InferenceService] YOLOv8-cls 加载完成: {MODEL_PATH}')
    else:
        raise RuntimeError('未找到模型，请设置 MODEL_PATH 环境变量')


def is_ready():
    return _mtcnn is not None and _classifier is not None


def _check_face_quality(box, img_w, img_h, mtcnn_conf):
    x1, y1, x2, y2 = box
    face_w = x2 - x1
    face_h = y2 - y1

    if mtcnn_conf is not None and float(mtcnn_conf) < MTCNN_MIN_CONFIDENCE:
        return False, 'low_mtcnn_confidence'

    face_area = face_w * face_h
    img_area = img_w * img_h
    if img_area > 0 and (face_area / img_area) < MIN_FACE_RATIO:
        return False, 'face_too_small'

    if face_h > 0:
        aspect = face_w / face_h
        if aspect < FACE_ASPECT_MIN or aspect > FACE_ASPECT_MAX:
            return False, 'invalid_aspect_ratio'

    return True, None


def inference(image_bytes: bytes) -> dict:
    pil_image = Image.open(io.BytesIO(image_bytes)).convert('RGB')

    t0 = time.time()
    boxes, probs, _ = _mtcnn.detect(pil_image, landmarks=True)
    t_detect = time.time() - t0

    faces = []
    skipped_count = 0

    if boxes is not None:
        w, h = pil_image.size
        for i, box in enumerate(boxes):
            x1, y1, x2, y2 = box.astype(int)
            x1 = max(0, x1)
            y1 = max(0, y1)
            x2 = min(w, x2)
            y2 = min(h, y2)

            mtcnn_conf = float(probs[i]) if probs is not None else 0.0

            face_ok, reject_reason = _check_face_quality(
                [x1, y1, x2, y2], w, h, mtcnn_conf
            )

            if not face_ok:
                skipped_count += 1
                faces.append({
                    'bbox': [int(x1), int(y1), int(x2), int(y2)],
                    'face_confidence': round(mtcnn_conf, 4),
                    'emotion': {
                        'label': 'unknown',
                        'label_cn': '未知',
                        'confidence': 0.0,
                        'quality': 'low',
                    },
                    'all_emotions': [],
                    'is_valid': False,
                    'reject_reason': reject_reason,
                })
                continue

            if x2 - x1 < 20 or y2 - y1 < 20:
                skipped_count += 1
                continue

            face_crop = pil_image.crop((x1, y1, x2, y2))

            t1 = time.time()
            results = _classifier.predict(source=face_crop, verbose=False)
            t_classify = time.time() - t1

            face_result = {
                'bbox': [int(x1), int(y1), int(x2), int(y2)],
                'face_confidence': round(mtcnn_conf, 4),
                'detect_ms': round(t_detect * 1000, 1),
                'classify_ms': round(t_classify * 1000, 1),
                'reject_reason': None,
            }

            if results and results[0].probs is not None:
                probs_data = results[0].probs
                top1_cls = int(probs_data.top1)
                top1_conf = float(probs_data.top1conf)
                top5_idx = probs_data.top5
                top5_conf = [float(c) for c in probs_data.top5conf]

                all_probs = [0.0] * 8
                for idx, conf in zip(top5_idx, top5_conf):
                    all_probs[idx] = round(conf, 4)

                quality = classify_quality(top1_conf)
                unknown_conf = all_probs[EMOTION_UNKNOWN_INDEX]

                if top1_cls == EMOTION_UNKNOWN_INDEX:
                    face_result['emotion'] = {
                        'label': 'unknown',
                        'label_cn': '未知',
                        'confidence': round(top1_conf, 4),
                        'quality': 'low',
                    }
                    face_result['is_valid'] = False
                    face_result['reject_reason'] = 'unknown_face'
                elif unknown_conf >= UNKNOWN_CONFIDENCE_THRESHOLD:
                    face_result['emotion'] = {
                        'label': 'unknown',
                        'label_cn': '未知',
                        'confidence': round(unknown_conf, 4),
                        'quality': 'low',
                    }
                    face_result['is_valid'] = False
                    face_result['reject_reason'] = 'unknown_high_confidence'
                elif quality == QUALITY_LOW:
                    face_result['emotion'] = {
                        'label': 'unknown',
                        'label_cn': '未知',
                        'confidence': round(top1_conf, 4),
                        'quality': quality,
                    }
                    face_result['is_valid'] = False
                    face_result['reject_reason'] = 'low_emotion_confidence'
                else:
                    face_result['emotion'] = {
                        'label': EMOTIONS[top1_cls]['label'],
                        'label_cn': EMOTIONS[top1_cls]['label_cn'],
                        'confidence': round(top1_conf, 4),
                        'quality': quality,
                    }
                    face_result['is_valid'] = True

                face_result['all_emotions'] = [
                    {
                        'label': EMOTIONS[j]['label'],
                        'label_cn': EMOTIONS[j]['label_cn'],
                        'confidence': round(all_probs[j], 4),
                    }
                    for j in range(8)
                ]
            else:
                face_result['emotion'] = {
                    'label': 'unknown',
                    'label_cn': '未知',
                    'confidence': 0.0,
                    'quality': 'low',
                }
                face_result['all_emotions'] = []
                face_result['is_valid'] = False
                face_result['reject_reason'] = 'classify_failed'

            faces.append(face_result)

    valid_faces = [f for f in faces if f.get('is_valid')]

    return {
        'faces_count': len(faces),
        'valid_count': len(valid_faces),
        'skipped_count': skipped_count,
        'faces': faces,
        'total_detect_ms': round(t_detect * 1000, 1),
    }
