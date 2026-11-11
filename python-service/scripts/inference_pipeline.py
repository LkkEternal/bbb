"""
两阶段推理管线: MTCNN人脸检测 + YOLOv8-cls表情分类
=====================================================

【功能说明】
1. MTCNN检测图片中的人脸位置（bounding box）
2. 人脸质量过滤（MTCNN置信度、面积、宽高比）
3. 裁剪每张人脸区域
4. YOLOv8-cls对每张人脸进行表情分类
5. 置信度分级（high/medium/low）
6. 低置信度结果标记为 unknown
7. 在原图上绘制检测框+表情标签

【使用方法】
    # 单张图片
    python scripts/inference_pipeline.py --image test.jpg

    # 批量图片
    python scripts/inference_pipeline.py --image test_images/

    # 摄像头实时
    python scripts/inference_pipeline.py --camera 0

    # 保存结果图片
    python scripts/inference_pipeline.py --image test.jpg --save
"""

import os
import sys
import time
import argparse
from pathlib import Path

import cv2
import torch
import numpy as np
from PIL import Image

os.environ["CUDA_VISIBLE_DEVICES"] = "0"
os.environ["CUDA_DEVICE_ORDER"] = "PCI_BUS_ID"

EMOTIONS = {
    0: ('angry',    '生气'),
    1: ('disgust',  '厌恶'),
    2: ('fear',     '恐惧'),
    3: ('happy',    '开心'),
    4: ('sad',      '悲伤'),
    5: ('surprise', '惊讶'),
    6: ('neutral',  '平静'),
    7: ('unknown',  '未知'),
}

UNKNOWN_INDEX = 7
UNKNOWN_CONFIDENCE_THRESHOLD = 0.35

COLORS = {
    'angry':    (0, 0, 255),
    'disgust':  (128, 0, 128),
    'fear':     (0, 128, 128),
    'happy':    (0, 255, 0),
    'sad':      (255, 0, 0),
    'surprise': (0, 165, 255),
    'neutral':  (128, 128, 128),
    'unknown':  (255, 255, 0),
}

QUALITY_THRESHOLD_HIGH = 0.70
QUALITY_THRESHOLD_LOW = 0.40
MTCNN_MIN_CONFIDENCE = 0.90
MIN_FACE_RATIO = 0.01
FACE_ASPECT_MIN = 0.5
FACE_ASPECT_MAX = 2.0

SCRIPT_DIR = Path(__file__).parent
PROJECT_ROOT = SCRIPT_DIR.parent
RUNS_CLASSIFY = PROJECT_ROOT / 'runs' / 'classify'


def classify_quality(confidence):
    if confidence >= QUALITY_THRESHOLD_HIGH:
        return 'high'
    elif confidence >= QUALITY_THRESHOLD_LOW:
        return 'medium'
    return 'low'


def check_face_quality(box, img_w, img_h, mtcnn_conf):
    x1, y1, x2, y2 = box
    face_w = x2 - x1
    face_h = y2 - y1

    if mtcnn_conf is not None and mtcnn_conf < MTCNN_MIN_CONFIDENCE:
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


def find_model():
    # 优先从 models/ 目录查找
    root_models = PROJECT_ROOT / 'models'
    if root_models.exists():
        for f in sorted(root_models.glob('*cls*.pt'), reverse=True):
            return f

    # 回退到 runs/ 目录
    runs_dir = RUNS_CLASSIFY / 'runs' / 'classify'
    if not runs_dir.exists():
        runs_dir = RUNS_CLASSIFY

    for d in sorted(runs_dir.iterdir(), reverse=True):
        if d.is_dir() and d.name.startswith('emotion_cls'):
            candidate = d / 'weights' / 'best.pt'
            if candidate.exists():
                return candidate

    return None


def load_mtcnn(device):
    from facenet_pytorch import MTCNN as _MTCNN
    mtcnn = _MTCNN(
        keep_all=True,
        device=device,
        min_face_size=40,
        thresholds=[0.6, 0.7, 0.7],
        post_process=True,
    )
    return mtcnn


def load_classifier(model_path):
    from ultralytics import YOLO
    model = YOLO(str(model_path))
    return model


def detect_faces(mtcnn, pil_image):
    boxes, probs, landmarks = mtcnn.detect(pil_image, landmarks=True)
    results = []
    if boxes is None:
        return results
    w, h = pil_image.size
    for i, box in enumerate(boxes):
        x1, y1, x2, y2 = box.astype(int)
        x1 = max(0, x1)
        y1 = max(0, y1)
        x2 = min(w, x2)
        y2 = min(h, y2)
        mtcnn_conf = float(probs[i]) if probs is not None else 0.0

        is_valid, reject_reason = check_face_quality(
            [x1, y1, x2, y2], w, h, mtcnn_conf
        )

        if x2 - x1 < 20 or y2 - y1 < 20:
            continue

        face_crop = pil_image.crop((x1, y1, x2, y2))
        results.append({
            'box': (x1, y1, x2, y2),
            'confidence': mtcnn_conf,
            'face': face_crop,
            'is_valid': is_valid,
            'reject_reason': reject_reason,
        })
    return results


def classify_faces(classifier, faces):
    for face_info in faces:
        if not face_info.get('is_valid', True):
            face_info['emotion'] = 'unknown'
            face_info['emotion_cn'] = '无效人脸'
            face_info['emotion_conf'] = 0.0
            face_info['cls_id'] = -1
            face_info['quality'] = 'low'
            continue

        face_pil = face_info['face']
        results = classifier.predict(source=face_pil, verbose=False)
        if results and results[0].probs is not None:
            probs = results[0].probs
            cls_id = int(probs.top1)
            conf = float(probs.top1conf)
            quality = classify_quality(conf)

            # 获取所有类别的概率
            all_probs = [0.0] * 8
            for idx, c in zip(probs.top5, probs.top5conf):
                all_probs[idx] = round(float(c), 4)
            unknown_conf = all_probs[UNKNOWN_INDEX]

            # 拒绝逻辑：top1是unknown 或 unknown置信度超过阈值
            if cls_id == UNKNOWN_INDEX:
                face_info['emotion'] = 'unknown'
                face_info['emotion_cn'] = '未知'
                face_info['emotion_conf'] = conf
                face_info['cls_id'] = -1
                face_info['reject_reason'] = 'unknown_face'
            elif unknown_conf >= UNKNOWN_CONFIDENCE_THRESHOLD:
                face_info['emotion'] = 'unknown'
                face_info['emotion_cn'] = '未知'
                face_info['emotion_conf'] = unknown_conf
                face_info['cls_id'] = -1
                face_info['reject_reason'] = 'unknown_high_confidence'
            elif quality == 'low':
                face_info['emotion'] = 'unknown'
                face_info['emotion_cn'] = '未知'
                face_info['emotion_conf'] = conf
                face_info['cls_id'] = -1
                face_info['reject_reason'] = 'low_emotion_confidence'
            else:
                face_info['emotion'] = EMOTIONS[cls_id][0]
                face_info['emotion_cn'] = EMOTIONS[cls_id][1]
                face_info['emotion_conf'] = conf
                face_info['cls_id'] = cls_id
            face_info['quality'] = quality
        else:
            face_info['emotion'] = 'unknown'
            face_info['emotion_cn'] = '未知'
            face_info['emotion_conf'] = 0.0
            face_info['cls_id'] = -1
            face_info['quality'] = 'low'
    return faces


def draw_results(cv_image, faces):
    for face_info in faces:
        x1, y1, x2, y2 = face_info['box']
        emotion = face_info.get('emotion', 'unknown')
        emotion_cn = face_info.get('emotion_cn', '未知')
        conf = face_info.get('emotion_conf', 0.0)
        quality = face_info.get('quality', 'low')

        if emotion == 'unknown':
            color = (0, 255, 255)
            dash_style = True
        else:
            color = COLORS.get(emotion, (255, 255, 255))
            dash_style = False

        if dash_style:
            for k in range(x1, x2, 8):
                cv2.line(cv_image, (k, y1), (min(k + 4, x2), y1), color, 2)
                cv2.line(cv_image, (k, y2), (min(k + 4, x2), y2), color, 2)
            for k in range(y1, y2, 8):
                cv2.line(cv_image, (x1, k), (x1, min(k + 4, y2)), color, 2)
                cv2.line(cv_image, (x2, k), (x2, min(k + 4, y2)), color, 2)
        else:
            cv2.rectangle(cv_image, (x1, y1), (x2, y2), color, 2)

        quality_mark = {'high': '', 'medium': '?', 'low': '!'}.get(quality, '')
        label = f"{emotion_cn}{quality_mark} {conf:.0%}"

        font_scale = 0.7
        thickness = 2
        (tw, th), _ = cv2.getTextSize(label, cv2.FONT_HERSHEY_SIMPLEX, font_scale, thickness)
        cv2.rectangle(cv_image, (x1, y1 - th - 10), (x1 + tw + 4, y1), color, -1)
        cv2.putText(cv_image, label, (x1 + 2, y1 - 6),
                    cv2.FONT_HERSHEY_SIMPLEX, font_scale, (255, 255, 255), thickness)
    return cv_image


def process_image(mtcnn, classifier, image_path, save=False, save_dir=None):
    img_path = Path(image_path)
    if not img_path.exists():
        print(f"[ERROR] 文件不存在: {img_path}")
        return None

    pil_image = Image.open(img_path).convert('RGB')
    cv_image = cv2.imread(str(img_path))

    t0 = time.time()
    faces = detect_faces(mtcnn, pil_image)
    t_detect = time.time() - t0

    if not faces:
        print(f"[INFO] 未检测到人脸: {img_path.name}")
        if save and save_dir:
            out_path = save_dir / f"result_{img_path.name}"
            cv2.imwrite(str(out_path), cv_image)
        return []

    t0 = time.time()
    faces = classify_faces(classifier, faces)
    t_classify = time.time() - t0

    cv_image = draw_results(cv_image, faces)

    total_ms = (t_detect + t_classify) * 1000
    valid_count = sum(1 for f in faces if f.get('quality', 'low') != 'low')
    print(f"[{img_path.name}] {len(faces)}张人脸 (有效{valid_count}) | "
          f"检测{t_detect*1000:.0f}ms 分类{t_classify*1000:.0f}ms 合计{total_ms:.0f}ms")

    for i, f in enumerate(faces):
        quality_mark = {'high': '', 'medium': ' [?不确定]', 'low': ' [!拒绝]'}.get(f.get('quality', 'low'), '')
        print(f"  [{i+1}] {f['emotion_cn']}({f['emotion']}) 置信度={f['emotion_conf']:.2%}{quality_mark}")

    if save and save_dir:
        out_path = save_dir / f"result_{img_path.name}"
        cv2.imwrite(str(out_path), cv_image)
        print(f"  -> 保存: {out_path}")

    return cv_image


def process_batch(mtcnn, classifier, image_dir, save=False):
    img_dir = Path(image_dir)
    if not img_dir.exists():
        print(f"[ERROR] 目录不存在: {img_dir}")
        return

    save_dir = None
    if save:
        save_dir = img_dir / 'results'
        save_dir.mkdir(exist_ok=True)

    exts = {'.jpg', '.jpeg', '.png', '.bmp', '.webp'}
    files = sorted([f for f in img_dir.iterdir() if f.suffix.lower() in exts])

    if not files:
        print(f"[INFO] 目录中无图片文件: {img_dir}")
        return

    print(f"共找到 {len(files)} 张图片\n")
    for f in files:
        process_image(mtcnn, classifier, f, save=save, save_dir=save_dir)
        print()


def run_camera(mtcnn, classifier, camera_id=0):
    cap = cv2.VideoCapture(camera_id)
    if not cap.isOpened():
        print(f"[ERROR] 无法打开摄像头: {camera_id}")
        return

    cap.set(cv2.CAP_PROP_FRAME_WIDTH, 640)
    cap.set(cv2.CAP_PROP_FRAME_HEIGHT, 480)

    print("按 Q 退出 | 按 S 截图保存")
    frame_count = 0
    fps_time = time.time()
    fps_display = 0

    while True:
        ret, frame = cap.read()
        if not ret:
            break

        frame_count += 1
        if frame_count % 3 != 0:
            cv2.imshow('Emotion Detection', frame)
            if cv2.waitKey(1) & 0xFF == ord('q'):
                break
            continue

        rgb_frame = cv2.cvtColor(frame, cv2.COLOR_BGR2RGB)
        pil_image = Image.fromarray(rgb_frame)

        faces = detect_faces(mtcnn, pil_image)
        if faces:
            faces = classify_faces(classifier, faces)
            frame = draw_results(frame, faces)

        now = time.time()
        fps_display = 1.0 / max(now - fps_time, 1e-6)
        fps_time = now

        cv2.putText(frame, f"FPS: {fps_display:.0f}", (10, 30),
                    cv2.FONT_HERSHEY_SIMPLEX, 1.0, (0, 255, 255), 2)

        n_valid = sum(1 for f in faces if f.get('quality', 'low') != 'low') if faces else 0
        n_total = len(faces) if faces else 0
        cv2.putText(frame, f"Faces: {n_valid}/{n_total}", (10, 65),
                    cv2.FONT_HERSHEY_SIMPLEX, 0.8, (0, 255, 255), 2)

        cv2.imshow('Emotion Detection', frame)

        key = cv2.waitKey(1) & 0xFF
        if key == ord('q'):
            break
        elif key == ord('s'):
            save_path = f"capture_{int(time.time())}.jpg"
            cv2.imwrite(save_path, frame)
            print(f"截图已保存: {save_path}")

    cap.release()
    cv2.destroyAllWindows()


def main():
    parser = argparse.ArgumentParser(description='两阶段表情推理: MTCNN人脸检测 + YOLOv8-cls表情分类')
    parser.add_argument('--image', type=str, help='图片路径或图片目录')
    parser.add_argument('--camera', type=str, help='摄像头ID (如 0)')
    parser.add_argument('--model', type=str, help='模型路径')
    parser.add_argument('--save', action='store_true', help='保存结果图片')
    parser.add_argument('--device', type=str, default='0', help='GPU设备')
    args = parser.parse_args()

    if not args.image and not args.camera:
        parser.print_help()
        return

    device = torch.device(f'cuda:{args.device}' if torch.cuda.is_available() else 'cpu')
    print(f"设备: {device}")

    model_path = args.model or find_model()
    if model_path is None:
        print("[ERROR] 未找到模型，请用 --model 指定")
        return
    print(f"模型: {model_path}")

    print("加载MTCNN...")
    mtcnn = load_mtcnn(device)
    print("MTCNN就绪")

    print("加载YOLOv8-cls分类器...")
    classifier = load_classifier(model_path)
    print("分类器就绪\n")

    if args.camera is not None:
        cam_id = int(args.camera) if args.camera.isdigit() else args.camera
        run_camera(mtcnn, classifier, cam_id)
    elif args.image:
        p = Path(args.image)
        if p.is_dir():
            process_batch(mtcnn, classifier, p, save=args.save)
        elif p.is_file():
            save_dir = None
            if args.save:
                save_dir = p.parent / 'results'
                save_dir.mkdir(exist_ok=True)
            process_image(mtcnn, classifier, p, save=args.save, save_dir=save_dir)
        else:
            print(f"[ERROR] 路径不存在: {p}")


if __name__ == '__main__':
    main()
