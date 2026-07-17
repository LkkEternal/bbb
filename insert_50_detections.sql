-- ============================================================
-- 50条病害检测记录 — 用于 PT-04 检测统计接口性能测试
-- 覆盖最近7天、50个果园、14类病害
-- 配合 JMeter 线程数=50 并发查询 /api/detections/stats
-- ============================================================

INSERT INTO disease_detection (orchard_id, device_id, disease_catalog_id, disease_name_raw, top_class_code, top_probability, all_probabilities, image_url, device_timestamp, extra_data) VALUES

-- 第1组：最近7天内均匀分布的记录（每天约7条）
-- 7天前
(2001, 1, 3, '疑似斑驳花纹', 'S3', 0.8623, '{"S3":0.8623,"S1":0.0712,"S2":0.0665}', 'https://obs.example.com/detections/2001/001.jpg', now() - interval '7 days', '{"temperature":28.5,"humidity":75}'),
(2002, 1, 14, '叶片溃疡病', 'S14', 0.9341, '{"S14":0.9341,"S13":0.0423,"S1":0.0236}', 'https://obs.example.com/detections/2002/001.jpg', now() - interval '7 days', '{"temperature":29.0,"humidity":80}'),
(2003, 1, 10, '炭疽病初期', 'S10', 0.7812, '{"S10":0.7812,"S11":0.1304,"S1":0.0884}', 'https://obs.example.com/detections/2003/001.jpg', now() - interval '6 days 12 hours', '{"temperature":26.8,"humidity":72}'),
(2004, 1, 5, '缺锌症状', 'S5', 0.8956, '{"S5":0.8956,"S7":0.0644,"S1":0.0400}', 'https://obs.example.com/detections/2004/001.jpg', now() - interval '6 days', '{"temperature":27.2,"humidity":68}'),
(2005, 1, 1, '健康果实', 'S1', 0.9721, '{"S1":0.9721,"S2":0.0279}', 'https://obs.example.com/detections/2005/001.jpg', now() - interval '6 days', '{"temperature":28.0,"humidity":65}'),
(2006, 1, 4, '黄龙病红鼻果', 'S4', 0.9112, '{"S4":0.9112,"S3":0.0523,"S1":0.0365}', 'https://obs.example.com/detections/2006/001.jpg', now() - interval '5 days 18 hours', '{"temperature":30.1,"humidity":82}'),
(2007, 1, 13, '煤污病', 'S13', 0.7621, '{"S13":0.7621,"S10":0.1423,"S1":0.0956}', 'https://obs.example.com/detections/2007/001.jpg', now() - interval '5 days', '{"temperature":25.5,"humidity":88}'),

-- 5天前
(2008, 1, 11, '脂点黄斑病', 'S11', 0.8432, '{"S11":0.8432,"S10":0.0912,"S1":0.0656}', 'https://obs.example.com/detections/2008/001.jpg', now() - interval '5 days', '{"temperature":27.8,"humidity":71}'),
(2009, 1, 6, '叶脉黄化', 'S6', 0.8234, '{"S6":0.8234,"S7":0.1123,"S5":0.0643}', 'https://obs.example.com/detections/2009/001.jpg', now() - interval '5 days', '{"temperature":26.5,"humidity":69}'),
(2010, 1, 2, '健康叶片', 'S2', 0.9611, '{"S2":0.9611,"S1":0.0389}', 'https://obs.example.com/detections/2010/001.jpg', now() - interval '4 days 14 hours', '{"temperature":28.2,"humidity":63}'),
(2011, 1, 3, '斑驳花纹偏重', 'S3', 0.9128, '{"S3":0.9128,"S4":0.0512,"S1":0.0360}', 'https://obs.example.com/detections/2011/001.jpg', now() - interval '4 days', '{"temperature":31.0,"humidity":85}'),
(2012, 1, 14, '溃疡病严重', 'S14', 0.9567, '{"S14":0.9567,"S10":0.0256,"S1":0.0177}', 'https://obs.example.com/detections/2012/001.jpg', now() - interval '4 days', '{"temperature":29.5,"humidity":78}'),
(2013, 1, 12, '藻斑与苔藓', 'S12', 0.7234, '{"S12":0.7234,"S11":0.1567,"S13":0.1199}', 'https://obs.example.com/detections/2013/001.jpg', now() - interval '4 days', '{"temperature":24.8,"humidity":91}'),
(2014, 1, 7, '均匀黄化', 'S7', 0.8456, '{"S7":0.8456,"S6":0.1023,"S5":0.0521}', 'https://obs.example.com/detections/2014/001.jpg', now() - interval '3 days 18 hours', '{"temperature":26.0,"humidity":73}'),

-- 3天前
(2015, 1, 8, '缺镁症状', 'S8', 0.7823, '{"S8":0.7823,"S7":0.1234,"S9":0.0943}', 'https://obs.example.com/detections/2015/001.jpg', now() - interval '3 days', '{"temperature":27.1,"humidity":70}'),
(2016, 1, 9, '缺硼症状', 'S9', 0.8122, '{"S9":0.8122,"S8":0.1121,"S1":0.0757}', 'https://obs.example.com/detections/2016/001.jpg', now() - interval '3 days', '{"temperature":26.8,"humidity":72}'),
(2017, 1, 1, '健康果实', 'S1', 0.9812, '{"S1":0.9812,"S2":0.0188}', 'https://obs.example.com/detections/2017/001.jpg', now() - interval '3 days', '{"temperature":28.0,"humidity":64}'),
(2018, 1, 10, '炭疽病扩散', 'S10', 0.8567, '{"S10":0.8567,"S11":0.0891,"S13":0.0542}', 'https://obs.example.com/detections/2018/001.jpg', now() - interval '3 days', '{"temperature":29.2,"humidity":76}'),
(2019, 1, 3, '斑驳花纹', 'S3', 0.8834, '{"S3":0.8834,"S4":0.0723,"S1":0.0443}', 'https://obs.example.com/detections/2019/001.jpg', now() - interval '2 days 16 hours', '{"temperature":30.5,"humidity":83}'),
(2020, 1, 5, '缺锌明显', 'S5', 0.9123, '{"S5":0.9123,"S7":0.0512,"S6":0.0365}', 'https://obs.example.com/detections/2020/001.jpg', now() - interval '2 days', '{"temperature":27.5,"humidity":67}'),
(2021, 1, 14, '溃疡病叶片', 'S14', 0.9412, '{"S14":0.9412,"S10":0.0345,"S1":0.0243}', 'https://obs.example.com/detections/2021/001.jpg', now() - interval '2 days', '{"temperature":28.8,"humidity":79}'),

-- 2天前
(2022, 1, 4, '红鼻果典型', 'S4', 0.9356, '{"S4":0.9356,"S3":0.0389,"S1":0.0255}', 'https://obs.example.com/detections/2022/001.jpg', now() - interval '2 days', '{"temperature":29.3,"humidity":81}'),
(2023, 1, 2, '健康叶片', 'S2', 0.9521, '{"S2":0.9521,"S1":0.0479}', 'https://obs.example.com/detections/2023/001.jpg', now() - interval '1 day 18 hours', '{"temperature":27.0,"humidity":62}'),
(2024, 1, 11, '脂点黄斑偏重', 'S11', 0.8678, '{"S11":0.8678,"S10":0.0789,"S13":0.0533}', 'https://obs.example.com/detections/2024/001.jpg', now() - interval '1 day', '{"temperature":26.5,"humidity":74}'),
(2025, 1, 6, '叶脉黄化', 'S6', 0.8011, '{"S6":0.8011,"S7":0.1234,"S5":0.0755}', 'https://obs.example.com/detections/2025/001.jpg', now() - interval '1 day', '{"temperature":25.8,"humidity":71}'),
(2026, 1, 13, '煤污病果实', 'S13', 0.7345, '{"S13":0.7345,"S10":0.1567,"S12":0.1088}', 'https://obs.example.com/detections/2026/001.jpg', now() - interval '1 day', '{"temperature":24.5,"humidity":89}'),
(2027, 1, 8, '缺镁叶脉间黄化', 'S8', 0.7956, '{"S8":0.7956,"S7":0.1234,"S9":0.0810}', 'https://obs.example.com/detections/2027/001.jpg', now() - interval '1 day', '{"temperature":27.2,"humidity":69}'),
(2028, 1, 1, '健康果实', 'S1', 0.9689, '{"S1":0.9689,"S2":0.0311}', 'https://obs.example.com/detections/2028/001.jpg', now() - interval '18 hours', '{"temperature":28.1,"humidity":63}'),

-- 1天前
(2029, 1, 3, '斑驳花纹疑似黄龙', 'S3', 0.9023, '{"S3":0.9023,"S4":0.0612,"S1":0.0365}', 'https://obs.example.com/detections/2029/001.jpg', now() - interval '16 hours', '{"temperature":30.0,"humidity":84}'),
(2030, 1, 14, '溃疡病叶缘', 'S14', 0.9211, '{"S14":0.9211,"S10":0.0434,"S1":0.0355}', 'https://obs.example.com/detections/2030/001.jpg', now() - interval '14 hours', '{"temperature":29.1,"humidity":77}'),
(2031, 1, 10, '炭疽病叶片', 'S10', 0.8123, '{"S10":0.8123,"S11":0.1023,"S13":0.0854}', 'https://obs.example.com/detections/2031/001.jpg', now() - interval '12 hours', '{"temperature":26.9,"humidity":73}'),
(2032, 1, 7, '均匀黄化缺素', 'S7', 0.8345, '{"S7":0.8345,"S6":0.0921,"S5":0.0734}', 'https://obs.example.com/detections/2032/001.jpg', now() - interval '10 hours', '{"temperature":27.3,"humidity":70}'),
(2033, 1, 5, '缺锌簇生叶', 'S5', 0.8765, '{"S5":0.8765,"S7":0.0723,"S8":0.0512}', 'https://obs.example.com/detections/2033/001.jpg', now() - interval '8 hours', '{"temperature":26.5,"humidity":68}'),
(2034, 1, 9, '缺硼果皮', 'S9', 0.7912, '{"S9":0.7912,"S8":0.1234,"S1":0.0854}', 'https://obs.example.com/detections/2034/001.jpg', now() - interval '6 hours', '{"temperature":27.8,"humidity":66}'),
(2035, 1, 12, '藻斑覆盖', 'S12', 0.7123, '{"S12":0.7123,"S13":0.1567,"S11":0.1310}', 'https://obs.example.com/detections/2035/001.jpg', now() - interval '4 hours', '{"temperature":25.0,"humidity":90}'),

-- 今天（最近4小时内）
(2036, 1, 1, '健康果实', 'S1', 0.9789, '{"S1":0.9789,"S2":0.0211}', 'https://obs.example.com/detections/2036/001.jpg', now() - interval '3 hours', '{"temperature":28.2,"humidity":62}'),
(2037, 1, 2, '健康叶片', 'S2', 0.9556, '{"S2":0.9556,"S1":0.0444}', 'https://obs.example.com/detections/2037/001.jpg', now() - interval '3 hours', '{"temperature":27.9,"humidity":61}'),
(2038, 1, 4, '红鼻果', 'S4', 0.9234, '{"S4":0.9234,"S3":0.0456,"S1":0.0310}', 'https://obs.example.com/detections/2038/001.jpg', now() - interval '2 hours', '{"temperature":30.2,"humidity":83}'),
(2039, 1, 11, '脂点黄斑病初期', 'S11', 0.8211, '{"S11":0.8211,"S10":0.1023,"S13":0.0766}', 'https://obs.example.com/detections/2039/001.jpg', now() - interval '2 hours', '{"temperature":26.7,"humidity":75}'),
(2040, 1, 14, '溃疡病', 'S14', 0.9456, '{"S14":0.9456,"S10":0.0321,"S1":0.0223}', 'https://obs.example.com/detections/2040/001.jpg', now() - interval '1 hour', '{"temperature":29.5,"humidity":78}'),
(2041, 1, 6, '叶脉黄化缺素', 'S6', 0.8122, '{"S6":0.8122,"S7":0.1121,"S5":0.0757}', 'https://obs.example.com/detections/2041/001.jpg', now() - interval '1 hour', '{"temperature":26.2,"humidity":72}'),
(2042, 1, 3, '斑驳花纹', 'S3', 0.8912, '{"S3":0.8912,"S4":0.0623,"S1":0.0465}', 'https://obs.example.com/detections/2042/001.jpg', now() - interval '30 minutes', '{"temperature":30.8,"humidity":86}'),
(2043, 1, 8, '缺镁症状', 'S8', 0.7765, '{"S8":0.7765,"S7":0.1345,"S9":0.0890}', 'https://obs.example.com/detections/2043/001.jpg', now() - interval '30 minutes', '{"temperature":27.0,"humidity":70}'),

-- 补充剩余果园（覆盖所有50个果园）
(2044, 1, 10, '炭疽病', 'S10', 0.8432, '{"S10":0.8432,"S11":0.0912,"S13":0.0656}', 'https://obs.example.com/detections/2044/001.jpg', now() - interval '3 days 6 hours', '{"temperature":27.5,"humidity":74}'),
(2045, 1, 5, '缺锌', 'S5', 0.8654, '{"S5":0.8654,"S7":0.0812,"S6":0.0534}', 'https://obs.example.com/detections/2045/001.jpg', now() - interval '4 days 8 hours', '{"temperature":26.8,"humidity":69}'),
(2046, 1, 1, '健康果实', 'S1', 0.9712, '{"S1":0.9712,"S2":0.0288}', 'https://obs.example.com/detections/2046/001.jpg', now() - interval '5 days 4 hours', '{"temperature":27.5,"humidity":64}'),
(2047, 1, 13, '煤污病', 'S13', 0.7456, '{"S13":0.7456,"S10":0.1567,"S12":0.0977}', 'https://obs.example.com/detections/2047/001.jpg', now() - interval '2 days 12 hours', '{"temperature":25.2,"humidity":87}'),
(2048, 1, 9, '缺硼', 'S9', 0.8012, '{"S9":0.8012,"S8":0.1123,"S1":0.0865}', 'https://obs.example.com/detections/2048/001.jpg', now() - interval '3 days 10 hours', '{"temperature":27.1,"humidity":71}'),
(2049, 1, 7, '均匀黄化', 'S7', 0.8234, '{"S7":0.8234,"S6":0.1023,"S5":0.0743}', 'https://obs.example.com/detections/2049/001.jpg', now() - interval '1 day 6 hours', '{"temperature":26.4,"humidity":70}'),
(2050, 1, 2, '健康叶片', 'S2', 0.9589, '{"S2":0.9589,"S1":0.0411}', 'https://obs.example.com/detections/2050/001.jpg', now() - interval '12 hours', '{"temperature":27.8,"humidity":62}');

-- ============================================================
-- 验证数据
-- ============================================================
SELECT 'disease_detection_count' AS metric, COUNT(*) AS value FROM disease_detection
UNION ALL
SELECT 'date_range', MIN(device_timestamp::date) || ' ~ ' || MAX(device_timestamp::date) FROM disease_detection
UNION ALL
SELECT 'distinct_orchards', COUNT(DISTINCT orchard_id)::text FROM disease_detection
UNION ALL
SELECT 'distinct_diseases', COUNT(DISTINCT top_class_code)::text FROM disease_detection;

-- ============================================================
-- PT-04 JMeter 测试用的查询（7天统计）
-- 接口: GET /api/detections/stats?startDate=YYYY-MM-DD&endDate=YYYY-MM-DD
-- ============================================================
-- 对应后端执行的实际SQL（EXPLAIN ANALYZE 验证性能）:
--
-- SELECT dd.top_class_code, dc.disease_name, COUNT(*) AS cnt
-- FROM disease_detection dd
-- JOIN disease_catalog dc ON dc.disease_code = dd.top_class_code
-- WHERE dd.device_timestamp >= '2025-07-06' AND dd.device_timestamp < '2025-07-14'
--   AND dd.orchard_id IN (SELECT ou.orchard_id FROM orchard_user ou WHERE ou.user_id = :userId)
-- GROUP BY dd.top_class_code, dc.disease_name
-- ORDER BY cnt DESC;
