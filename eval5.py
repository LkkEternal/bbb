# -*- coding: utf-8 -*-
import os, sys
os.chdir(os.path.join(os.environ["USERPROFILE"], "AppData", "Local", "Temp"))
from ultralytics import YOLO
model = YOLO("../../表情检测/python-service/runs/classify/runs/classify/emotion_cls_20260724_101434/weights/best.pt")
results = model.val(data="../../表情检测/python-service/data/processed/rafdb", device="0", verbose=False)
cm = results.confusion_matrix.matrix
names = ["angry","disgust","fear","happy","sad","surprise","neutral"]
with open("per_class_results.txt", "w") as f:
    f.write("Top1: %.4f\n" % results.top1)
    f.write("Top5: %.4f\n\n" % results.top5)
    for i in range(7):
        row_sum = sum(cm[i])
        diag = cm[i][i]
        acc = diag / row_sum if row_sum > 0 else 0
        f.write("%s: %.4f (%d/%d)\n" % (names[i], acc, int(diag), int(row_sum)))
    f.write("\nConfusion Matrix:\n")
    f.write("            " + "".join(["%%10s" %% n for n in names]) + "\n")
    for i in range(7):
        f.write("%%10s   " %% names[i] + "".join(["%%10d" %% int(cm[i][j]) for j in range(7)]) + "\n")
print("DONE")
