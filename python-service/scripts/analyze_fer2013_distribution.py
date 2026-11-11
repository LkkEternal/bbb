#!/usr/bin/env python3
"""
FER2013 数据集类别分布分析脚本

基于公开数据统计（Hugging Face datasets: abhilash88/fer2013-enhanced）
FER2013 总计 35,887 张图片，7 个类别
"""

import math

# FER2013 各类别图片数量（来自公开数据集统计）
CLASS_DISTRIBUTION = {
    'angry':    {'id': 0, 'count': 4953, 'cn': '生气'},
    'disgust':  {'id': 1, 'count': 547,  'cn': '厌恶'},
    'fear':     {'id': 2, 'count': 5121, 'cn': '恐惧'},
    'happy':    {'id': 3, 'count': 8989, 'cn': '开心'},
    'sad':      {'id': 4, 'count': 6077, 'cn': '悲伤'},
    'surprise': {'id': 5, 'count': 4002, 'cn': '惊讶'},
    'neutral':  {'id': 6, 'count': 6198, 'cn': '平静'},
}

TOTAL = sum(v['count'] for v in CLASS_DISTRIBUTION.values())
TARGET = 9000

def print_section(title):
    print(f"\n{'='*60}")
    print(f"  {title}")
    print(f"{'='*60}")

def main():
    # ── 1. 各类别数量统计 ──
    print_section("1. FER2013 各类别数量统计")
    print(f"  {'类别ID':<8} {'类别':<10} {'中文':<8} {'数量':<8} {'占比':<8} {'比例条'}")
    print(f"  {'-'*52}")

    for name, info in CLASS_DISTRIBUTION.items():
        ratio = info['count'] / TOTAL
        bar = '█' * int(ratio * 40)
        print(f"  {info['id']:<8} {name:<10} {info['cn']:<8} {info['count']:<8} {ratio:>6.1%}   {bar}")

    print(f"  {'-'*52}")
    print(f"  {'总计':<8} {'':10} {'':8} {TOTAL:<8} {'100.0%':>6}")

    # ── 2. 总图片数量 ──
    print_section("2. 总图片数量")
    print(f"  总计: {TOTAL:,} 张")
    print(f"  训练集(Training):  ~28,709 张 (~80%)")
    print(f"  验证集(Val):       ~3,589 张  (~10%)")
    print(f"  测试集(Test):      ~3,589 张  (~10%)")

    # ── 3. 类别均衡性分析 ──
    print_section("3. 类别均衡性分析")
    counts = [v['count'] for v in CLASS_DISTRIBUTION.values()]
    max_count = max(counts)
    min_count = min(counts)
    mean_count = TOTAL / len(counts)
    imbalance_ratio = max_count / min_count

    print(f"  最大类 (happy):     {max_count:>6,} 张")
    print(f"  最小类 (disgust):   {min_count:>6,} 张")
    print(f"  类间不平衡比:       1 : {imbalance_ratio:.1f}")
    print(f"  均值:               {mean_count:>6,.0f} 张/类")
    print(f"  标准差:             {__import__('statistics').stdev(counts):>6,.0f}")

    print(f"\n  [!] 严重不均衡:")
    print(f"     - happy (开心) 是 disgust (厌恶) 的 {max_count/min_count:.1f} 倍")
    print(f"     - happy 占比 25.0%, disgust 仅占 1.5%")
    print(f"     - 模型会偏向预测多数类(happy), 少数类(disgust)识别率差")

    # ── 4. 筛选9000张的分配方案 ──
    print_section(f"4. 筛选 {TARGET:,} 张的分配方案")

    # 方案A: 按原始比例分配（保持分布特性）
    print(f"\n  [A] 方案A: 按原始比例分配(保持原始分布)")
    print(f"  {'类别':<10} {'原始数':<8} {'比例':>6}  {'分配数':<8} {'实际占比'}")
    print(f"  {'-'*48}")
    plan_a = {}
    remainder = TARGET
    sorted_classes = sorted(CLASS_DISTRIBUTION.items(), key=lambda x: -x[1]['count'])
    for i, (name, info) in enumerate(sorted_classes):
        exact = TARGET * info['count'] / TOTAL
        plan_a[name] = round(exact)
        remainder -= plan_a[name]
    # 分配余数给最大类
    largest = sorted_classes[0][0]
    plan_a[largest] += remainder
    for name, info in sorted_classes:
        pct = plan_a[name] / TARGET * 100
        orig_pct = info['count'] / TOTAL * 100
        print(f"  {info['cn']:<10} {info['count']:<8} {orig_pct:>5.1f}%   {plan_a[name]:<8} {pct:>5.1f}%")
    print(f"  {'-'*48}")
    print(f"  {'总计':<10} {TOTAL:<8} 100.0%   {sum(plan_a.values()):<8} 100.0%")

    # 方案B: 均衡分配（缩小类间差距）
    print(f"\n  [B] 方案B: 均衡分配(每类约 {TARGET//7:,} 张, 缩小类间差距)")
    print(f"  {'类别':<10} {'原始数':<8} {'分配数':<8} {'裁剪比例'}")
    print(f"  {'-'*42}")
    per_class = TARGET // 7  # 1285
    extra = TARGET % 7        # 5
    plan_b = {}
    for i, (name, info) in enumerate(sorted_classes):
        plan_b[name] = per_class + (1 if i < extra else 0)
    for name, info in sorted_classes:
        cut_ratio = plan_b[name] / info['count'] * 100
        print(f"  {info['cn']:<10} {info['count']:<8} {plan_b[name]:<8} 保留 {cut_ratio:>5.1f}%")
    print(f"  {'-'*42}")
    print(f"  {'总计':<10} {TOTAL:<8} {sum(plan_b.values()):<8}")

    # 方案C: 推荐方案（均衡与原始比例之间取折中）
    print(f"\n  [C] 方案C(推荐): 折中方案 -- 设最低保底 + 按比例分配剩余")
    print(f"  策略: 每类保底 800 张，剩余按原始比例分配")
    print(f"  {'类别':<10} {'原始数':<8} {'分配数':<8} {'实际占比'}")
    print(f"  {'-'*42}")
    BASE = 800
    plan_c = {}
    reserved = BASE * 7  # 5600
    remaining = TARGET - reserved  # 3400
    total_orig = sum(v['count'] for v in CLASS_DISTRIBUTION.values())
    for name, info in sorted_classes:
        plan_c[name] = BASE + round(remaining * info['count'] / total_orig)
    # 修正总和
    diff = TARGET - sum(plan_c.values())
    plan_c[sorted_classes[0][0]] += diff
    for name, info in sorted_classes:
        pct = plan_c[name] / TARGET * 100
        print(f"  {info['cn']:<10} {info['count']:<8} {plan_c[name]:<8} {pct:>5.1f}%")
    print(f"  {'-'*42}")
    print(f"  {'总计':<10} {TOTAL:<8} {sum(plan_c.values()):<8} 100.0%")

    print(f"\n{'='*60}")
    print(f"  [OK] 推荐使用方案C(折中方案):")
    print(f"     - 保证每类至少有足够样本供模型学习")
    print(f"     - 多数类不被过度裁剪")
    print(f"     - 少数类(disgust)不会因过少而被忽略")
    print(f"{'='*60}")


if __name__ == '__main__':
    main()
